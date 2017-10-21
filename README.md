关于安卓4.4手机连接不上BLE设备的问题回复:

原因：当使用minSDKVersion 19的时候，系统默认调用的BluetoothDevice类是在android-19目录下的，与程序中的回调BluetoothDevice不是同一个类为什么会这样目前还不清楚

解决方案：
只要把minSDKVersion改成其他任何一个版本，如18 20等都不会出现问题。唯独19会出现问题。


配置：

1.直接将library依赖到项目中
2.gradle配置依赖
```
compile 'com.jackiepenghe:blelibrary:0.1.0'
```
3.maven配置依赖
```
<dependency>
  <groupId>com.jackiepenghe</groupId>
  <artifactId>blelibrary</artifactId>
  <version>0.1.0</version>
  <type>pom</type>
</dependency
```
4.vy配置依赖
```
<dependency org='com.jackiepenghe' name='blelibrary' rev='0.1.0'>
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
```
//实例化扫描器
BleScanner bleScanner = new BleScanner(context);
//调用open方法，传入相关的回调，并打开扫描器功能
bleScanner.open(scanList, onScanFindANewDeviceListener, 10000, false, onScanCompleteListener);
//开始扫描，扫描的结果在回调中，扫描的设备列表会自动添加到上方open函数中的scanList中
bleScanner.startScan();
```

注销：
一定要记得在activity被销毁之前，注销扫描器

```
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

```
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
```
List<BluetoothGattService> deviceServices = bleConnector.getServices();
```

对目标进行数据的传输

发送数据
```
bleConnector.writeData(serviceUUID,characteristicUUID,value);
```

获取数据
```
bleConnector.readData(serviceUUID,characteristicUUID);
```

上面的发送与获取数据的方法返回的都是boolean类型，代表成功与失败(其实bleConnector的函数基本上都是返回boolean类型的)

获取到的数据在回调中查看
```
bleConnector.setOnCharacteristicReadListener(onCharacteristicReadListener);
```

还有通知

打开通知：
```
bleConnector.openNotification(serviceUUID,characteristicUUID);
```

关闭通知
```
bleConnector.closeNotification(serviceUUID,characteristicUUID);
```

通知的回调
```
bleConnector.setOnReceiveNotificationListener(onReceiveNotificationListener);
```

还有其他的回调，看情况自己使用

销毁

在准备销毁activity的时候，调用close方法
```
    @Override
    public void onBackPressed() {
        bleConnector.close();
    }
```

然后在回调中销毁activity
```
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
