package cn.almsound.www.myblesample;

import android.app.Application;

import com.jackiepenghe.baselibrary.files.FileUtil;
import com.jackiepenghe.baselibrary.tools.ToastUtil;
import com.jackiepenghe.baselibrary.tools.Tool;
import com.jackiepenghe.blelibrary.BleManager;
import com.jackiepenghe.blelibrary.DebugUtil;


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
        DebugUtil.setDebugFlag(true);
        ToastUtil.setToastReuse(true);
        //初始化文件工具类
        FileUtil.init(this.getApplicationContext());
        BleManager.init(MyApplication.this);
        com.jackiepenghe.baselibrary.tools.DebugUtil.setDebugFlag(true);
    }
}
