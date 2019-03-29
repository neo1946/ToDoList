package com.neo1946.todolist.util;

import android.view.View;

import com.neo1946.todolist.R;

/**
 * @author ouyangzhaoxian on 2019/03/12
 */
public class ColorUtil {
    public static void setViewColor(View view,int groupId){
        if(view == null) {
            return;
        }
        switch (groupId){
            case 1:
                view.setBackgroundResource(R.color.color_emergency_level_1);
                break;
            case 2:
                view.setBackgroundResource(R.color.color_emergency_level_2);
                break;
            case 3:
                view.setBackgroundResource(R.color.color_emergency_level_3);
                break;
            case 4:
                view.setBackgroundResource(R.color.color_emergency_level_4);
                break;
            default:
                break;
        }
    }
}
