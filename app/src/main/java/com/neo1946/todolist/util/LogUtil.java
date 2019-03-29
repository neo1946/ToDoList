package com.neo1946.todolist.util;

import android.util.Log;

import com.neo1946.todolist.MyApplication;
import com.neo1946.todolist.ui.MainActivity;

/**
 * @author ouyangzhaoxian on 2019/03/13
 */
public class LogUtil {
    private final static String TAG = "kayzing";
    public static void e(String message){
        if(MyApplication.isTest){
            Log.e(TAG,message);
        }
    }
    public static void v(String message){
        if(MyApplication.isTest){
            Log.v(TAG,message);
        }
    }
}
