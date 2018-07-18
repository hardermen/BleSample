package cn.almsound.www.myblesample;

import android.app.Application;

import com.jackiepenghe.baselibrary.CrashHandler;
import com.jackiepenghe.baselibrary.FileUtil;
import com.jackiepenghe.baselibrary.Tool;
import com.jackiepenghe.blelibrary.BleManager;


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
        //打开debug信息开关
        Tool.setDebugFlag(true);
        //初始化文件工具类
        FileUtil.init(this.getApplicationContext());
        BleManager.setDebugFlag(true);
    }
}
