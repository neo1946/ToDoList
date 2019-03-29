package com.neo1946.todolist.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.neo1946.todolist.util.CommonUtil;

import java.util.Date;

/**
 * @author ouyangzhaoxian on 2019/3/11.
 * 数据库帮助类
 */

public class MyOpenHelper extends SQLiteOpenHelper {

    private final static String DB_NAME = "note.db";// 数据库文件名
    private final static int DB_VERSION = 1;// 数据库版本

    public MyOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //创建分类表
        db.execSQL("create table db_group(g_id integer primary key autoincrement, " +
                "g_name varchar, g_order integer, g_color varchar," +
                "g_create_time datetime, g_update_time datetime )");
        //创建笔记表
        db.execSQL("create table db_note(n_id integer primary key autoincrement, n_title varchar, " +
                "n_content varchar, n_group_id integer, n_location_name varchar, n_finish integer, " +
                "n_bg_color varchar, n_create_time datetime," +
                "n_update_time datetime )");
        db.execSQL("insert into db_group(g_name, g_order, g_color, g_create_time, g_update_time) " +
                "values(?,?,?,?,?)", new String[]{"1", "1", "#FF0000", System.currentTimeMillis()+"",System.currentTimeMillis()+""});
        db.execSQL("insert into db_group(g_name, g_order, g_color, g_create_time, g_update_time) " +
                "values(?,?,?,?,?)", new String[]{"2", "2", "#00FF00", System.currentTimeMillis()+"",System.currentTimeMillis()+""});
        db.execSQL("insert into db_group(g_name, g_order, g_color, g_create_time, g_update_time) " +
                "values(?,?,?,?,?)", new String[]{"3", "3", "#0000FF", System.currentTimeMillis()+"",System.currentTimeMillis()+""});
        db.execSQL("insert into db_group(g_name, g_order, g_color, g_create_time, g_update_time) " +
                "values(?,?,?,?,?)", new String[]{"4", "4", "#000000", System.currentTimeMillis()+"",System.currentTimeMillis()+""});
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
