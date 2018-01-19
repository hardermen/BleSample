配置：

1.直接将library依赖到项目中

2.gradle配置依赖
```xml
compile 'com.jackiepenghe:blelibrary:0.4.7'
```
3.maven配置依赖
```xml
<dependency>
  <groupId>com.jackiepenghe</groupId>
  <artifactId>blelibrary</artifactId>
  <version>0.4.7</version>
  <type>pom</type>
</dependency
```
4.vy配置依赖
```xml
<dependency org='com.jackiepenghe' name='blelibrary' rev='0.4.7'>
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
### BLE扫描进阶设置(需要API21支持)
#### 设置过滤条件
```java
 if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            ArrayList<ScanFilter> scanFilters = new ArrayList<>();
            String serviceUUID = "C3E6FEA0-E966-1000-8000-BE99C223DF6A";
            ScanFilter scanFilter = new ScanFilter.Builder()
                    //设置过滤设备地址
                    .setDeviceAddress("00:02:5B:00:15:AA")
                    //设置过滤设备名称
                    .setDeviceName("Y11-")
                    //根据厂商自定义的广播id和广播内容过滤
                    .setManufacturerData(2, new byte[]{0, 2})
                    //根据服务数据进行过滤
                    .setServiceUuid(new ParcelUuid(UUID.fromString(serviceUUID)))
                    //构建
                    .build();
            scanFilters.add(scanFilter);
            //设置过滤条件
            bleScanner.setScanFilters(scanFilters);
        }
```
#### 设置扫描参数
```java
 if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            ScanSettings scanSettings = new ScanSettings.Builder()
                    //设置回调触发方式（需要API23及以上）
//                    .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                    //如果只有传统（我猜测是经典蓝牙，并不确定）的广播，是否回调callback函数(需要API26及以上)
//                    .setLegacy(false)
                    //设置扫描匹配方式（需要API23及以上）
//                    .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                    //设置扫描匹配次数（需要API23及以上）
//                    .setNumOfMatches(2)
                    //在扫描过程中设置物理层(需要API23及以上)
//                    .setPhy(BluetoothDevice.PHY_LE_1M)
                    //设置报告延迟时间
                    .setReportDelay(100)
                    //设置扫描模式
                    .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                    //构建
                    .build();
            //设置扫描参数
          bleScanner.setScanSettings(scanSettings);
        }
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

### 蓝牙广播（Android Bluetooth LE Peripheral）
这是在安卓5.0（API21）之后加入的库，用于蓝牙BLE广播，较常用与iBeacon数据广播。以下的用法都是在API21及以上的时候使用。（iBeacon此处我就不详细去说了，请看下方的用法即可）

获取蓝牙广播实例
```java
 private BleBroadCastor bleBroadCastor;
 //获取一个新的广播实例
 //bleBroadCastor = BleManager.newBleBroadCastor(this);
 //获取单例
 bleBroadCastor = BleManager.getBleBroadCastor(this);
```
初始化
```java
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
//默认的初始化
//        bleBroadCastor.init();
            //服务UUID 
            String serviceUUID = "C3E6FEA0-E966-1000-8000-BE99C223DF6A";
            //广播设置
            AdvertiseSettings advertiseSettings = new AdvertiseSettings.Builder()
                    //设置广播的模式
                    .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                    //设置是否可连接
                    .setConnectable(true)
                    //设置广播时间（0为永不停止，直到回调stopAdvertising()）
                    .setTimeout(0)
                    //设置广播功率等级（等级越高，信号越强，也更加耗电）
                    .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                    //构建
                    .build();
            //ParcelUuid
            ParcelUuid parcelUuid = new ParcelUuid(UUID.fromString(serviceUUID));
            //广播数据
            AdvertiseData advertiseData = new AdvertiseData.Builder()
                    //设置广播内容是否包含信号发送等级
                    .setIncludeTxPowerLevel(true)
                    //设置广播内容是否包含蓝牙名称（此名称为在手机蓝牙设置中的蓝牙名称）
                    .setIncludeDeviceName(true)
                    //设置厂商自定义广播数据
                    .addManufacturerData(2, new byte[]{2, 1})
                    //添加服务UUD
                    .addServiceUuid(parcelUuid)
                    //添加服务UUD与数据
                    .addServiceData(parcelUuid, new byte[]{2, 2, 5})
                    .build();
            //广播回调
            AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
                /**
                 * Callback triggered in response to {@link BluetoothLeAdvertiser#startAdvertising} indicating
                 * that the advertising has been started successfully.
                 *
                 * @param settingsInEffect The actual settings used for advertising, which may be different from
                 *                         what has been requested.
                 */
                @Override
                public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                    Tool.warnOut(TAG, "onStartSuccess");
                    if (settingsInEffect != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            Tool.warnOut(TAG, "onStartSuccess TxPowerLv=" + settingsInEffect.getTxPowerLevel() + " mode=" + settingsInEffect.getMode()
                                    + " timeout=" + settingsInEffect.getTimeout());
                        }
                    } else {
                        Tool.warnOut(TAG, "onStartSuccess, settingInEffect is null");
                    }
                    Tool.warnOut(TAG, "onStartSuccess settingsInEffect" + settingsInEffect);
                }

                /**
                 * Callback when advertising could not be started.
                 *
                 * @param errorCode Error code (see ADVERTISE_FAILED_* constants) for advertising start
                 *                  failures.
                 */
                @Override
                public void onStartFailure(int errorCode) {
                    Tool.warnOut(TAG, "onStartFailure");
                    if (errorCode == ADVERTISE_FAILED_DATA_TOO_LARGE) {
                        Tool.errorOut(TAG, "Failed to start advertising as the advertise data to be broadcasted is larger than 31 bytes.");
                    } else if (errorCode == ADVERTISE_FAILED_TOO_MANY_ADVERTISERS) {
                        Tool.errorOut(TAG, "Failed to start advertising because no advertising instance is available.");
                    } else if (errorCode == ADVERTISE_FAILED_ALREADY_STARTED) {
                        Tool.errorOut(TAG, "Failed to start advertising as the advertising is already started");
                    } else if (errorCode == ADVERTISE_FAILED_INTERNAL_ERROR) {
                        Tool.errorOut(TAG, "Operation failed due to an internal error");
                    } else if (errorCode == ADVERTISE_FAILED_FEATURE_UNSUPPORTED) {
                        Tool.errorOut(TAG, "This feature is not supported on this platform");
                    }
                }
            };
            //初始化
            bleBroadCastor.init(advertiseSettings, advertiseData, advertiseData, advertiseCallback);
        }
 
```
当设置手机广播可被连接时，需要设置此回调来配合与外部设备的通讯，设置不可连接时，可不必设置词汇掉。（即便是可连接时，不设置此回调也不会有问题，但是这样会导致无法进行任何操作）
```java
BleInterface.OnBluetoothGattServerCallbackListener onBluetoothGattServerCallbackListener = new BleInterface.OnBluetoothGattServerCallbackListener() {
            /**
             * Callback indicating when a remote device has been connected or disconnected.
             *
             * @param device   Remote device that has been connected or disconnected.
             * @param status   Status of the connect or disconnect operation.
             * @param newState Returns the new connection state. Can be one of
             *                 {@link BluetoothProfile#STATE_DISCONNECTED} or
             *                 {@link BluetoothProfile#STATE_CONNECTED}
             */
            @Override
            public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {

            }

            /**
             * Indicates whether a local service has been added successfully.
             *
             * @param status  Returns {@link BluetoothGatt#GATT_SUCCESS} if the service
             *                was added successfully.
             * @param service The service that has been added
             */
            @Override
            public void onServiceAdded(int status, BluetoothGattService service) {

            }

            /**
             * A remote client has requested to read a local characteristic.
             * <p>
             * <p>An application must call {@link BluetoothGattServer#sendResponse}
             * to complete the request.
             *
             * @param device         The remote device that has requested the read operation
             * @param requestId      The Id of the request
             * @param offset         Offset into the value of the characteristic
             * @param characteristic Characteristic to be read
             */
            @Override
            public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {

            }

            /**
             * A remote client has requested to write to a local characteristic.
             * <p>
             * <p>An application must call {@link BluetoothGattServer#sendResponse}
             * to complete the request.
             *
             * @param device         The remote device that has requested the write operation
             * @param requestId      The Id of the request
             * @param characteristic Characteristic to be written to.
             * @param preparedWrite  true, if this write operation should be queued for
             *                       later execution.
             * @param responseNeeded true, if the remote device requires a response
             * @param offset         The offset given for the value
             * @param value          The value the client wants to assign to the characteristic
             */
            @Override
            public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {

            }

            /**
             * A remote client has requested to read a local descriptor.
             * <p>
             * <p>An application must call {@link BluetoothGattServer#sendResponse}
             * to complete the request.
             *
             * @param device     The remote device that has requested the read operation
             * @param requestId  The Id of the request
             * @param offset     Offset into the value of the characteristic
             * @param descriptor Descriptor to be read
             */
            @Override
            public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {

            }

            /**
             * A remote client has requested to write to a local descriptor.
             * <p>
             * <p>An application must call {@link BluetoothGattServer#sendResponse}
             * to complete the request.
             *
             * @param device         The remote device that has requested the write operation
             * @param requestId      The Id of the request
             * @param descriptor     Descriptor to be written to.
             * @param preparedWrite  true, if this write operation should be queued for
             *                       later execution.
             * @param responseNeeded true, if the remote device requires a response
             * @param offset         The offset given for the value
             * @param value          The value the client wants to assign to the descriptor
             */
            @Override
            public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {

            }

            /**
             * Execute all pending write operations for this device.
             * <p>
             * <p>An application must call {@link BluetoothGattServer#sendResponse}
             * to complete the request.
             *
             * @param device    The remote device that has requested the write operations
             * @param requestId The Id of the request
             * @param execute   Whether the pending writes should be executed (true) or
             */
            @Override
            public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {

            }

            /**
             * Callback invoked when a notification or indication has been sent to
             * a remote device.
             * <p>
             * <p>When multiple notifications are to be sent, an application must
             * wait for this callback to be received before sending additional
             * notifications.
             *
             * @param device The remote device the notification has been sent to
             * @param status {@link BluetoothGatt#GATT_SUCCESS} if the operation was successful
             */
            @Override
            public void onNotificationSent(BluetoothDevice device, int status) {

            }

            /**
             * Callback indicating the MTU for a given device connection has changed.
             * <p>
             * <p>This callback will be invoked if a remote client has requested to change
             * the MTU for a given connection.
             *
             * @param device The remote device that requested the MTU change
             * @param mtu    The new MTU size
             */
            @Override
            public void onMtuChanged(BluetoothDevice device, int mtu) {

            }

            /**
             * Callback triggered as result of {@link BluetoothGattServer#setPreferredPhy}, or as a result
             * of remote device changing the PHY.
             *
             * @param device The remote device
             * @param txPhy  the transmitter PHY in use. One of {@link BluetoothDevice#PHY_LE_1M},
             *               {@link BluetoothDevice#PHY_LE_2M}, and {@link BluetoothDevice#PHY_LE_CODED}
             * @param rxPhy  the receiver PHY in use. One of {@link BluetoothDevice#PHY_LE_1M},
             *               {@link BluetoothDevice#PHY_LE_2M}, and {@link BluetoothDevice#PHY_LE_CODED}
             * @param status Status of the PHY update operation.
             *               {@link BluetoothGatt#GATT_SUCCESS} if the operation succeeds.
             */
            @Override
            public void onPhyUpdate(BluetoothDevice device, int txPhy, int rxPhy, int status) {

            }

            /**
             * Callback triggered as result of {@link BluetoothGattServer#readPhy}
             *
             * @param device The remote device that requested the PHY read
             * @param txPhy  the transmitter PHY in use. One of {@link BluetoothDevice#PHY_LE_1M},
             *               {@link BluetoothDevice#PHY_LE_2M}, and {@link BluetoothDevice#PHY_LE_CODED}
             * @param rxPhy  the receiver PHY in use. One of {@link BluetoothDevice#PHY_LE_1M},
             *               {@link BluetoothDevice#PHY_LE_2M}, and {@link BluetoothDevice#PHY_LE_CODED}
             * @param status Status of the PHY read operation.
             *               {@link BluetoothGatt#GATT_SUCCESS} if the operation succeeds.
             */
            @Override
            public void onPhyRead(BluetoothDevice device, int txPhy, int rxPhy, int status) {

            }
        };
        bleBroadCastor.setOnBluetoothGattServerCallbackListener(onBluetoothGattServerCallbackListener);
```
开始广播
```java
  if (bleBroadCastor != null) {
            boolean b = bleBroadCastor.startAdvertising();
            Tool.warnOut(TAG, "startAdvertising = " + b);
            if (b) {
                Tool.warnOut(TAG, "广播请求发起成功（是否真的成功，在init传入的advertiseCallback回调中查看）");
            }else {
                Tool.warnOut(TAG, "广播请求发起失败（这是真的失败了，连请求都没有发起成功）");
            }
        }
```
停止广播并关闭实例
```java
 /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (bleBroadCastor != null) {
            //停止广播
            bleBroadCastor.stopAdvertising();
            //关闭广播实例
            bleBroadCastor.close();
        }
    }
```
# 特别注意
安卓手机因为系统各个厂家定制的原因，可能会有一些莫名其妙的问题。如：UUID发现后跟设备本身不一致等。这种问题通常可以通过重启蓝牙解决。但是也有那种顽固无比的手机。如：三星盖乐世3.这个手机必须要回复出厂设置才能正确发现UUID，原因是：系统记录了同一个设备地址的UUID。一旦连接的是同一个地址，UUID第一次发现之后，后续不论怎么更改设备的UUID，系统的缓存都是不会更新的。对于这种手机，只想说：别用BLE了。没救了
