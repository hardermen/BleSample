关于安卓4.4手机连接不上BLE设备的问题回复:

原因：当使用minSDKVersion 19的时候，系统默认调用的BluetoothDevice类是在android-19目录下的，与程序中的回调BluetoothDevice不是同一个类为什么会这样目前还不清楚

解决方案：
只要把minSDKVersion改成其他任何一个版本，如18 20等都不会出现问题。唯独19会出现问题。


配置：

1.直接将library依赖到项目中

2.gradle配置依赖
```
compile 'com.jackiepenghe:blelibrary:0.1.5'
```
3.maven配置依赖
```
<dependency>
  <groupId>com.jackiepenghe</groupId>
  <artifactId>blelibrary</artifactId>
  <version>0.1.5</version>
  <type>pom</type>
</dependency
```
4.vy配置依赖
```
<dependency org='com.jackiepenghe' name='blelibrary' rev='0.1.5'>
  <artifact name='blelibrary' ext='pom' ></artifact>
</dependency>
```

###  权限配置：
```
<!--蓝牙权限-->
   <uses-permission android:name="android.permission.BLUETOOTH" />
   <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
<!--BLE权限-->
    <uses-feature
        android:name="android.hardware.bluetooth_le"
        android:required="true" />
<!-- 5.0以上的手机可能会需要这个权限 -->
<uses-feature android:name="android.hardware.location.gps" />
<!-- 6.0的手机需要定位权限权限 -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
```
### BLE扫描：
```java
//实例化扫描器
BleScanner bleScanner = new BleScanner(context);
//调用open方法，传入相关的回调，并打开扫描器功能
bleScanner.open(scanList, onScanFindOneNewDeviceListener, 10000, false, onScanCompleteListener);
//开始扫描，扫描的结果在回调中，扫描的设备列表会自动添加到上方open函数中的scanList中
bleScanner.startScan();
```

注销：
一定要记得在activity被销毁之前，注销扫描器

```java
bleScanner.close();
```

### BLE设备的连接：

在进行连接之前，一定要检查是否在AndroidManifest中配置已一个必须的服务！

``` 
<service
    android:name="cn.almsound.www.almblelibrary.service.BluetoothLeService"
    android:enabled="true"
    android:exported="false" />

``` 
接下来就是Java代码了

```java
//实例化连接器
 BleConnector bleConnector = new BleConnector(ConnectActivity.this);
//设置回调，在这个回调中判断连接成功最为保险
 bleConnector.setOnServicesDiscoveredListener(onServicesDiscoveredListener);
//设置要连接的设备的地址，并发起连接
private void startConnect() {
        if (bleConnector.checkAndSetAddress(address)) {
            if (bleConnector.startConnect()) {
                LogUtil.w("开始连接");    
            } else {
                LogUtil.w("连接失败");              
            }
        }
    }
```

在连接成功之后，就可以获取设备的服务列表
```java
List<BluetoothGattService> deviceServices = bleConnector.getServices();
```

对目标进行数据的传输

发送数据
```java
bleConnector.writeData(serviceUUID,characteristicUUID,value);
```

获取数据
```java
bleConnector.readData(serviceUUID,characteristicUUID);
```

上面的发送与获取数据的方法返回的都是boolean类型，代表成功与失败(其实bleConnector的函数基本上都是返回boolean类型的)

获取到的数据在回调中查看
```java
bleConnector.setOnCharacteristicReadListener(onCharacteristicReadListener);
```

还有通知

打开通知：
```java
bleConnector.openNotification(serviceUUID,characteristicUUID);
```

关闭通知
```java
bleConnector.closeNotification(serviceUUID,characteristicUUID);
```

通知的回调
```java
bleConnector.setOnReceiveNotificationListener(onReceiveNotificationListener);
```

还有其他的很多回调，可以自己下载源码，根据实际需求使用

销毁

在准备销毁activity的时候，调用close方法。推荐在此处屏蔽super.onBackpressed()方法。
```java
    @Override
    public void onBackPressed() {
        bleConnector.close();
    }
```

然后在回调中销毁activity
```java
BleConnector.OnCloseCompleteListener onCloseCompleteListener;
onCloseCompleteListener = new BleConnector.OnCloseCompleteListener() {
            @Override
            public void onCloseComplete() {
                //ThisActivity.super.onBackPressed();
                finish();
            }
        };
bleConnector.setOnCloseCompleteListener(onCloseCompleteListener);
```

### BLE设备的绑定(也可以说是配对)：

```java
        /*
         * 调用绑定的方法（如果需要绑定)，否则请直接调用连接的方法
         * 注意：如果该设备不支持绑定，会直接回调绑定成功的回调，在绑定成功的回调中发起连接即可
         * 第一次绑定某一个设备会触发回调，之后再次绑定，可根据绑定时的函数的返回值来判断绑定状态，以进行下一步操作
         */
        switch (bleConnector.startBound(address)) {
            case BleConstants.DEVICE_BOND_START_SUCCESS:
                LogUtil.w(TAG, "开始绑定");
                break;
            case BleConstants.DEVICE_BOND_START_FAILED:
                LogUtil.w(TAG, "发起绑定失败");
                break;
            case BleConstants.DEVICE_BOND_BONDED:
                LogUtil.w(TAG, "此设备已经被绑定了");
                startConnect();
                break;
            case BleConstants.DEVICE_BOND_BONDING:
                LogUtil.w(TAG, "此设备正在绑定中");
                break;
            case BleConstants.BLUETOOTH_ADAPTER_NULL:
                LogUtil.w(TAG, "没有蓝牙适配器存在");
                break;
            case BleConstants.BLUETOOTH_ADDRESS_INCORRECT:
                LogUtil.w(TAG, "蓝牙地址错误");
                break;
            case BleConstants.BLUETOOTH_MANAGER_NULL:
                LogUtil.w(TAG, "没有蓝牙管理器存在");
                break;
            default:
                LogUtil.w(TAG, "default");
                break;
        }
```
相关的回调是：
```java
  //设备的绑定(也可以说配对)状态改变后触发此回调
        BleInterface.OnDeviceBondStateChangedListener onBondStateChangedListener = new BleInterface.OnDeviceBondStateChangedListener() {
            /**
             * 正在绑定设备
             */
            @Override
            public void deviceBinding() {

            }

            /**
             * 绑定完成
             */
            @Override
            public void deviceBonded() {
                //发起连接
                startConnect();
            }

            /**
             * 取消绑定或者绑定失败
             */
            @Override
            public void deviceBindNone() {

            }
        };
        //设置绑定的回调
         bleConnector.setOnBondStateChangedListener(onBondStateChangedListener);
```
