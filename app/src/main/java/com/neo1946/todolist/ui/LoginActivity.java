package com.neo1946.todolist.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.LogInCallback;
import com.avos.avoscloud.SignUpCallback;
import com.neo1946.todolist.R;

/**
 * @author ouyangzhaoxian on 2019/03/12
 */
public class LoginActivity extends BaseActivity {
        private EditText et_username;
        private EditText et_password;
        private Button bt_login;
        private Button bt_register;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_login);
            et_username = findViewById(R.id.et_name);
            et_password = findViewById(R.id.et_password);
            bt_login = findViewById(R.id.bt_login);
            bt_register = findViewById(R.id.bt_register);

            bt_login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if("".equals(et_username.getText().toString()) || "".equals(et_password.getText().toString())){
                        Toast.makeText(LoginActivity.this, "用户名/密码 不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String username = et_username.getText().toString();
                    String password = et_password.getText().toString();

                    AVUser.logInInBackground(username, password, new LogInCallback<AVUser>() {
                        @Override
                        public void done(AVUser avUser, AVException e) {
                            if (e == null) {
                                Intent intent = new Intent();
                                LoginActivity.this.setResult(1, intent);
                                LoginActivity.this.finish();
                            } else {
                                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });

            bt_register.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if("".equals(et_username.getText().toString()) || "".equals(et_password.getText().toString())){
                        Toast.makeText(LoginActivity.this, "用户名/密码 不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String username = et_username.getText().toString();
                    String password = et_password.getText().toString();

                    AVUser user = new AVUser();// 新建 AVUser 对象实例
                    user.setUsername(username);// 设置用户名
                    user.setPassword(password);// 设置密码
//                    user.setEmail(email);//设置邮箱
                    user.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(AVException e) {
                            if (e == null) {
                                Intent intent = new Intent();
                                LoginActivity.this.setResult(1, intent);
                                LoginActivity.this.finish();
                                Toast.makeText(LoginActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });
        }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        LoginActivity.this.setResult(0, intent);
        LoginActivity.this.finish();
    }
}
