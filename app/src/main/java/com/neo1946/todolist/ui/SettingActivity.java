package com.neo1946.todolist.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.neo1946.todolist.R;
import com.neo1946.todolist.util.CouldUtil;
import com.neo1946.todolist.util.FingerPrintUtil;
import com.neo1946.todolist.view.MyDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import static com.neo1946.todolist.MyApplication.SP_NAME;
import static com.neo1946.todolist.util.FingerPrintUtil.FINGER_AUTH_SP_NAME;

/**
 * 设置界面
 */

public class SettingActivity extends BaseActivity {
    private Switch s_auth;
    private MyDialog dialog;
    private StringBuilder stringBuilder;
    private CouldUtil couldUtil;
    private LinearLayout ll_upload;
    private LinearLayout ll_download;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        s_auth = findViewById(R.id.s_auth);
        final SharedPreferences sharedPreferences = getSharedPreferences(SP_NAME, MODE_PRIVATE);
        if (FingerPrintUtil.supportFingerprint(this)) {
            boolean hasStartAuth = sharedPreferences.getBoolean(FINGER_AUTH_SP_NAME, false);
            s_auth.setChecked(hasStartAuth);
            s_auth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    sharedPreferences.edit().putBoolean(FINGER_AUTH_SP_NAME, isChecked).apply();
                }
            });
        } else {
            s_auth.setVisibility(View.GONE);
        }
        boolean islogin;
        if (sharedPreferences.getBoolean("isLogin", false)) {
            islogin = true;
        } else {
            islogin = false;
        }
        ll_upload = findViewById(R.id.ll_upload);
        ll_download = findViewById(R.id.ll_download);
        if (islogin) {
            ll_upload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startUpload();
                }
            });
            ll_download.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startDownload();
                }
            });
        } else {
            ll_upload.setVisibility(View.GONE);
            ll_download.setVisibility(View.GONE);
        }
        couldUtil = new CouldUtil(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }


    @Subscribe
    public void onSyncFinish(CouldUtil.SyncFinishEvent event) {
        if (event.finish) {
            Toast.makeText(this, "同步成功", Toast.LENGTH_SHORT).show();
            if (dialog != null) {
                dialog.setFinish(true);
                ll_upload.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                }, 3000);
            }
        } else {
            Toast.makeText(this, "同步失败", Toast.LENGTH_SHORT).show();
            if (dialog != null) {
                dialog.setFinish(true);
                ll_upload.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                }, 3000);
            }
        }

    }


    @Subscribe
    public void onSyncProgress(CouldUtil.SyncProgressEvent event) {
        if (dialog != null) {
            dialog.setMessage(stringBuilder.append("\n" + event.message).toString());
        }
    }


    private void setUpDialog(String title, String message) {
        dialog = new MyDialog(this);
        dialog.setTitle(title);//设置标题
        dialog.setMessage(message);
        dialog.setCancelable(false);//设置进度条是否可以按退回键取消 ;
        dialog.show();

        stringBuilder = new StringBuilder(message);
    }

    private void startDownload() {
        setUpDialog("下载中", "开始下载");
        couldUtil.downloadOnly();
    }

    private void startUpload() {
        setUpDialog("上传中", "开始上传");
        couldUtil.uploadOnly();
    }
}