package com.neo1946.todolist.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.neo1946.todolist.R;

/**
 * @author ouyangzhaoxian on 2019/3/28.
 */
public class ThemeManagerUtil {
    public final static String USER_SETTING = "userSetting";
    public static String EXTRA_IS_UPDATE_THEME = "com.copasso.cocobill.IS_UPDATE_THEME";


    private String[] mThemes = {"原谅绿", "玛雅黑", "波尔多红", "托帕蓝", "鸠羽紫", "珊瑚橙", "亚麻棕"};

    private static ThemeManagerUtil instance;

    public static ThemeManagerUtil getInstance() {
        if (instance == null) {
            instance = new ThemeManagerUtil();
        }
        return instance;
    }

    public String[] getThemes(){
        return mThemes;
    }

    /**
     * 设置主题色
     * @param context   activity
     * @param theme     主题名称
     */
    public void setTheme(Activity context, String theme){
        String curTheme = getCurrentTheme(context);
        if(curTheme != null && curTheme.equals(theme)){
            return;
        }

        setCurrentTheme(context,theme);

        context.finish();
        Intent intent = context.getIntent();
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(EXTRA_IS_UPDATE_THEME, true);
        context.startActivity(intent);
    }

    /**
     * 获取当前主题名
     * @param context 上下文
     * @return
     */
    public String getCurThemeName(Context context){
        return getCurrentTheme(context);
    }



    public void init(Context context) {
        String theme = getCurrentTheme(context);
        if(theme.equals(mThemes[0])){
            context.setTheme(R.style.AppTheme);
        }else if(theme.equals(mThemes[1])){
            context.setTheme(R.style.AppTheme_Black);
        }else if(theme.equals(mThemes[2])){
            context.setTheme(R.style.AppTheme_Green);
        }else if(theme.equals(mThemes[3])){
            context.setTheme(R.style.AppTheme_Blue);
        }else if(theme.equals(mThemes[4])){
            context.setTheme(R.style.AppTheme_Purple);
        }else if(theme.equals(mThemes[5])){
            context.setTheme(R.style.AppTheme_Orange);
        }else if(theme.equals(mThemes[6])){
            context.setTheme(R.style.AppTheme_Brown);
        }
    }

    /**
     * 获取当前用户主题
     */
    public static String getCurrentTheme(Context context) {
        SharedPreferences sp = context.getSharedPreferences(USER_SETTING, Context.MODE_PRIVATE);
        if (sp != null)
            return sp.getString("theme", "原谅绿");
        return null;
    }

    /**
     * 获取当前用户主题
     */
    public static void setCurrentTheme(Context context, String theme) {
        SharedPreferences sp = context.getSharedPreferences(USER_SETTING, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        if (editor != null) {
            editor.putString("theme", theme);
            editor.commit();
        }
    }
}

