package cn.almsound.www.myblesample.guid;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;

import com.jackiepenghe.baselibrary.activity.BaseWelcomeActivity;
import com.jackiepenghe.baselibrary.tools.CrashHandler;
import com.jackiepenghe.baselibrary.tools.ToastUtil;
import com.jackiepenghe.baselibrary.tools.Tool;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;
import com.yanzhenjie.permission.PermissionListener;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RationaleListener;

import java.util.List;

import cn.almsound.www.myblesample.R;
import cn.almsound.www.myblesample.main.MainActivity;

/**
 * @author jacke
 */
public class WelcomeActivity extends BaseWelcomeActivity {

    /*-----------------------成员变量-----------------------*/

    /**
     * 权限请求码
     */
    private static final int REQUEST_CODE = 1;
    /**
     * 进入设置界面的权限请求码
     */
    private static final int REQUEST_CODE_SETTING = 2;
    /**
     * 权限回调
     */
    private PermissionListener permissionListener = new PermissionListener() {
        @Override
        public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {
            toNext();
        }

        @Override
        public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
            // 用户否勾选了不再提示并且拒绝了权限，那么提示用户到设置中授权。
            if (AndPermission.hasAlwaysDeniedPermission(WelcomeActivity.this, deniedPermissions)) {
                // 第一种：用默认的提示语。
                AndPermission.defaultSettingDialog(WelcomeActivity.this, REQUEST_CODE_SETTING).show();
            }
        }
    };
    /**
     * 二次权限请求回调
     */
    private RationaleListener rationaleListener = new RationaleListener() {
        @Override
        public void showRequestPermissionRationale(int requestCode, Rationale rationale) {
            AndPermission.rationaleDialog(WelcomeActivity.this, rationale).show();
        }
    };

    /*-----------------------实现父类函数-----------------------*/

    @Override
    protected void doAfterAnimation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NotificationManagerCompat manager = NotificationManagerCompat.from(WelcomeActivity.this.getApplicationContext());
            boolean isOpened = manager.areNotificationsEnabled();
            if (!isOpened) {
                ToastUtil.toastL(WelcomeActivity.this, R.string.no_notification_permission);
                //去打开通知权限
                showOpenNotificationPermissionDialog();
                return;
            }
        }
        requestPermission();
    }

    /**
     * 设置ImageView的图片资源
     *
     * @return 图片资源ID
     */
    @Override
    protected int setImageViewSource() {
        return 0;
    }

    /*-----------------------重写父类函数-----------------------*/

    /**
     * Dispatch incoming result to the correct fragment.
     *
     * @param requestCode 请求码
     * @param resultCode  返回码
     * @param data        返回的数据集
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_SETTING:
                requestPermission();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    /*-----------------------自定义函数-----------------------*/

    /**
     * 开始请求权限
     */
    private void requestPermission() {
        AndPermission.with(this)
                .requestCode(REQUEST_CODE)
                .permission(Permission.LOCATION, Permission.STORAGE)
                .callback(permissionListener)
                .rationale(rationaleListener)
                .start();
    }

    /**
     * 进入主界面
     */
    private void toNext() {
        //权限完全请求并获取完毕后，初始化全局异常捕获类
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(this.getApplicationContext());
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * 跳转到设置中，让用户打开通知权限
     */
    private void showOpenNotificationPermissionDialog() {
        new AlertDialog.Builder(WelcomeActivity.this)
                .setTitle(R.string.no_notification_permission_title)
                .setMessage(R.string.no_notification_permission_message)
                .setPositiveButton(R.string.allow, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getApplication().getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        requestPermission();
                    }
                })
                .setCancelable(false)
                .show();

    }
}
