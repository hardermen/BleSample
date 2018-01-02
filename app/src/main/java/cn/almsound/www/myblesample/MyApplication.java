package cn.almsound.www.myblesample;

import android.app.Application;

import com.jackiepenghe.baselibrary.FileUtil;
import com.jackiepenghe.baselibrary.Tool;

/**
 * @author jacke
 * @date 2017/12/11 0011
 * <p>
 * 应用程序
 */

public class MyApplication extends Application {

    /**
     * Called when the application is starting, before any activity, service,
     * or receiver objects (excluding content providers) have been created.
     * Implementations should be as quick as possible (for example using
     * lazy initialization of state) since the time spent in this function
     * directly impacts the performance of starting the first activity,
     * service, or receiver in a process.
     * If you override this method, be sure to call super.onCreate().
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Tool.setDebugFlag(true);
        Tool.setDefaultTAG("BleLibrary");
        //初始化文件工具类
        FileUtil.init(this);

//        初始化全局异常捕获类
//        CrashHandler crashHandler = CrashHandler.getInstance();
//        crashHandler.init(this);
    }
}
