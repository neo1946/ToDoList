package com.neo1946.todolist.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.neo1946.todolist.R;
import com.neo1946.todolist.util.LogUtil;

/**
 * @author ouyangzhaoxian on 2019/3/14.
 */
public class MyDialog extends Dialog {

    private TextView tv_title;
    private TextView tv_message;
    private Button bt_done;
    private String titleData;
    private String messageData;
    public boolean isFinish = false;

    public MyDialog(Context context) {
        super(context, R.style.MyDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_loading);
        //按空白处不能取消动画
        setCanceledOnTouchOutside(false);

        //初始化界面控件
        initView();
        //初始化界面数据
        initData();

    }

    /**
     * 初始化界面控件的显示数据
     */
    private void initData() {
        tv_title.setText(titleData);
        tv_message.setText(messageData);
    }

    /**
     * 初始化界面控件
     */
    private void initView() {
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_message = (TextView) findViewById(R.id.tv_message);
        bt_done = (Button) findViewById(R.id.bt_done);
        bt_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isFinish){
                    dismiss();
                }
            }
        });
    }

    /**
     * 从外界Activity为Dialog设置标题
     *
     * @param title
     */
    public void setTitle(String title) {
        if (tv_title == null) {
            titleData = title;
        } else {
            tv_title.setText(title);
        }
    }

    /**
     * 从外界Activity为Dialog设置dialog的message
     *
     * @param message
     */
    public void setMessage(String message) {
        if (tv_message == null) {
            messageData = message;
//            LogUtil.e("set data"+message);
        } else {
//            LogUtil.e("set text"+message);
            tv_message.setText(message);
        }
    }

    public void setFinish(boolean finish){
        isFinish = finish;
    }

}