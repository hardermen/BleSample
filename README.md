配置：

1.直接将library依赖到项目中

2.gradle配置依赖
```xml
compile 'com.jackiepenghe:blelibrary:0.3.5'
```
3.maven配置依赖
```xml
<dependency>
  <groupId>com.jackiepenghe</groupId>
  <artifactId>blelibrary</artifactId>
  <version>0.3.5</version>
  <type>pom</type>
</dependency
```
4.vy配置依赖
```xml
<dependency org='com.jackiepenghe' name='blelibrary' rev='0.3.5'>
  <artifact name='blelibrary' ext='pom' ></artifact>
</dependency>
```

###  权限配置：
```xml
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
### 判断设备本身是否支持BLE：
```java
if(!BleManager.isSupportBle()){
  Log.w(TAG,"设备不支持BLE");
  return;
}
//设备支持BLE，继续执行代码
```
### BLE扫描：
```java
//创建化扫描器
BleScanner bleScanner = BleManager.newBleScanner(context);
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

```xml 
<service
    android:name="com.jackiepenghe.blelibrary.BluetoothLeService"
    android:enabled="true"
    android:exported="false" />

``` 
接下来就是Java代码了

```java
//创建连接器
 BleConnector bleConnector = BleManager.newBleConnector(context);
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
        
     /*if (bleConnector.checkAndSetAddress(address)) {
            //发起连接时传入true代表断链后自动重连
            if (bleConnector.startConnect(true)) {
                LogUtil.w("开始连接");    
            } else {
                LogUtil.w("连接失败");              
            }
        }*/
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
### 多连接
首先要在AndroidManifest.xml添加一个服务
```xml
<service android:name="com.jackiepenghe.blelibrary.BluetoothMultiService"
  android:enabled="true"
  android:exported="false"/>
```
获取多连接的连接器
```java
BleMultiConnector bleMultiConnectorWeakReference = BleManager.getBleMultiConnector(context);
```
连接多个设备
```java
    String device1Address = "00:02:5B:00:15:A4";
    String device2Address = "00:02:5B:00:15:A2";

    //使用默认的回调连接
//  bleMultiConnector.connect(device1Address);
//  bleMultiConnector.connect(device2Address);

    //断开后自动连接（此函数调用的是系统的API，由系统自动连接设备）
    bleMultiConnector.connect(device1Address,true);
    bleMultiConnector.connect(device2Address,true);

    //连接时传入对应的回调，方便进行操作,通常使用这个就行了
//  bleMultiConnector.connect(device1Address, device1BleCallback);
//  bleMultiConnector.connect(device2Address, device2BleCallback);


    //连接时传入对应的回调，方便进行操作,并且在连接断开之后自动尝试连接（系统会默认自动去连接该设备，这是系统自身的重连参数，推荐用这个参数进行重连）
//  bleMultiConnector.connect(device1Address,device1BleCallback,true);
//  bleMultiConnector.connect(device2Address,device2BleCallback,true);
```
上方的callback是继承自BaseConnectCallback
```

public class Device1BleCallback extends BaseConnectCallback {
    private static final String TAG = "Device1BleCallback";

    /**
     * 蓝牙连接后无法正常进行服务发现时回调
     *
     * @param gatt BluetoothGatt
     */
    @Override
    public void onDiscoverServicesFailed(BluetoothGatt gatt) {
        Tool.warnOut(TAG,"onDiscoverServicesFailed");
    }

    /**
     * 蓝牙GATT被关闭时回调
     */
    @Override
    public void onGattClosed() {
        Tool.warnOut(TAG,"onGattClosed");
    }
}
```
同时连接多个设备后，如果想要对单独某一个设备进行操作
```java
BleDeviceController bleDeviceController =  bleMultiConnectorWeakReference.getBleDeviceController(address);
bleDeviceController.writData(serviceUUID,characteristicUUID,data);
...
```
在程序退出时或者当前Activity销毁前close
```java
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //最好是先清空一下缓存
        bleMultiConnectorWeakReference.refreshAllGattCache();
        //关闭所有gatt
        bleMultiConnectorWeakReference.closeAll();
    }
 
```
# 特别注意
安卓手机因为系统各个厂家定制的原因，可能会有一些莫名其妙的问题。如：UUID发现后跟设备本身不一致等。这种问题通常可以通过重启蓝牙解决。但是也有那种顽固无比的手机。如：三星盖乐世3.这个手机必须要回复出厂设置才能正确发现UUID，原因是：系统记录了同一个设备地址的UUID。一旦连接的是同一个地址，UUID第一次发现之后，后续不论怎么更改设备的UUID，系统的缓存都是不会更新的。对于这种手机，只想说：别用BLE了。没救了
