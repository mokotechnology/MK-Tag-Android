package com.moko.bxp.tag.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.moko.bxp.tag.R;
import com.moko.bxp.tag.dialog.PermissionDialog;
import com.moko.bxp.tag.utils.Utils;
import com.permissionx.guolindev.PermissionX;

/**
 * @Date 2017/12/7 0007
 * @Author wenzheng.liu
 * @Description
 */
public class GuideActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
        requestPermission();
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            //申请存储权限 10以下的版本
            if (!isWriteStoragePermissionOpen()) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, getResources().getString(R.string.permission_storage_need_content),
                        getResources().getString(R.string.permission_storage_close_content));
                return;
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            //判断GPS是否打开
            if (!Utils.isLocServiceEnable(this)) {
                showOpenLocationDialog();
                return;
            }
            //申请定位权限 11及以下的版本
            if (!isLocationPermissionOpen()) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, getResources().getString(R.string.permission_location_need_content),
                        getResources().getString(R.string.permission_location_close_content));
                return;
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            //申请蓝牙权限 12及以上办版本
            if (!hasBlePermission()) {
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN}, getResources().getString(R.string.permission_ble_content),
                        getResources().getString(R.string.permission_ble_close_content));
                return;
            }
        }
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }, 1000);
    }

    private void requestPermissions(String[] permissions, String requestContent, String closeContent) {
        PermissionX.init(this).permissions(permissions).
                onExplainRequestReason((scope, deniedList) -> scope.showRequestReasonDialog(new PermissionDialog(deniedList, requestContent))).
                onForwardToSettings((scope, deniedList) -> scope.showForwardToSettingsDialog(new PermissionDialog(deniedList, closeContent))).
                request((allGranted, grantedList, deniedList) -> {
                    if (allGranted) requestPermission();
                    else finish();
                });
    }

    @TargetApi(Build.VERSION_CODES.S)
    private boolean hasBlePermission() {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;
    }

    private void showOpenLocationDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.location_need_title)
                .setMessage(R.string.location_need_content)
                .setPositiveButton(getString(R.string.permission_open), (dialog1, which) -> {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startLauncher.launch(intent);
                }).setNegativeButton(getString(R.string.cancel), (dialog12, which) -> finish())
                .create();
        dialog.show();
    }

    private final ActivityResultLauncher<Intent> startLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> requestPermission());
}
