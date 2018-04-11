package cn.almsound.www.myblesample.guid;

import android.content.Intent;
import android.support.annotation.NonNull;

import com.jackiepenghe.baselibrary.BaseWelcomeActivity;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;
import com.yanzhenjie.permission.PermissionListener;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RationaleListener;

import java.util.List;

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
        requestPermission();
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
                .permission(Permission.LOCATION)
                .callback(permissionListener)
                .rationale(rationaleListener)
                .start();
    }

    /**
     * 进入主界面
     */
    private void toNext() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        onBackPressed();
    }
}
