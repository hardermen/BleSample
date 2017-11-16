package cn.almsound.www.blelibrary;

import android.content.Context;
import android.content.pm.PackageManager;

/**
 * Created by alm on 2017/11/10.
 * BlE管理类
 */

@SuppressWarnings("WeakerAccess")
public class BleManager {

    private static BleConnector bleConnector;
    private static BleScanner bleScanner;
    private static BleMultiConnector bleMultiConnector;

    public static boolean isSupportBle(Context context){
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    public static BleConnector newBleConnector(Context context){
        if (!isSupportBle(context)){
            return null;
        }
        return new BleConnector(context);
    }

    public static BleConnector getBleConnectorInstance(Context context){
        if (!isSupportBle(context)){
            return null;
        }
        if (bleConnector == null){
            synchronized (BleManager.class){
                if (bleConnector == null){
                    bleConnector = new BleConnector(context);
                }
            }
        }
        return bleConnector;
    }

    public static BleScanner newBleScanner(Context context){
        if (!isSupportBle(context)){
            return null;
        }
        return new BleScanner(context);
    }

    public static BleScanner getBleScannerInstance(Context context){
        if (!isSupportBle(context)){
            return null;
        }
        if (bleScanner == null){
            synchronized (BleManager.class){
                if (bleScanner == null){
                    bleScanner = new BleScanner(context);
                }
            }
        }
        return bleScanner;
    }

    public static BleMultiConnector getBleMultiConnector(Context context){
        if (!isSupportBle(context)){
            return null;
        }
        if (bleMultiConnector == null){
            synchronized (BleManager.class){
                if (bleMultiConnector == null){
                    bleMultiConnector = new BleMultiConnector(context.getApplicationContext());
                }
            }
        }
        return bleMultiConnector;
    }

    static void resetBleMultiConnector() {
        bleMultiConnector = null;
    }
}
