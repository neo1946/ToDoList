package com.neo1946.todolist.ui;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.neo1946.todolist.MyApplication;
import com.neo1946.todolist.R;
import com.neo1946.todolist.util.FingerPrintUtil;

public class AuthActivity extends BaseActivity implements FingerPrintUtil.FingerAuthCallBack {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty);
        FingerPrintUtil util = new FingerPrintUtil(this,this);
        util.start();
    }

    @Override
    public void onSuccess() {
        if(MyApplication.isTest){
            Toast.makeText(AuthActivity.this,"认证成功",Toast.LENGTH_LONG).show();
        }
        Intent intent = new Intent();
        this.setResult(1, intent);
        finish();
    }

    @Override
    public void onFail() {
        if(MyApplication.isTest) {
            Toast.makeText(AuthActivity.this, "认证失败", Toast.LENGTH_LONG).show();
        }
        Intent intent = new Intent();
        this.setResult(0, intent);
        finish();
    }
}