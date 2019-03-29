package com.neo1946.todolist.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.neo1946.todolist.bean.Note;
import com.neo1946.todolist.util.ImageUtils;
import com.neo1946.todolist.util.LogUtil;
import com.neo1946.todolist.util.StringUtils;

import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ouyangzhaoxian on 2019/3/11.
 * 笔记处理
 */

public class NoteDao {
    private MyOpenHelper helper;

    public NoteDao(Context context) {
        helper = new MyOpenHelper(context);
    }

    /**
     * 查询所有笔记
     */
    public List<Note> queryNotesAll(int groupId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        List<Note> noteList = new ArrayList<>();
        Note note;
        String sql;
        Cursor cursor = null;
        try {
            if (groupId > 0) {
                sql = "select * from db_note where n_group_id =" + groupId +
                        "order by n_create_time desc";
            } else {
                sql = "select * from db_note ";
            }
            cursor = db.rawQuery(sql, null);
            //cursor = db.query("note", null, null, null, null, null, "n_id desc");
            while (cursor.moveToNext()) {
                //循环获得展品信息
                note = new Note();
                note.setId(cursor.getInt(cursor.getColumnIndex("n_id")));
                note.setTitle(cursor.getString(cursor.getColumnIndex("n_title")));
                note.setContent(cursor.getString(cursor.getColumnIndex("n_content")));
                note.setGroupId(cursor.getInt(cursor.getColumnIndex("n_group_id")));
                note.setLocation(cursor.getString(cursor.getColumnIndex("n_location_name")));
                note.setIsFinish(cursor.getInt(cursor.getColumnIndex("n_type")));
                note.setBgColor(cursor.getString(cursor.getColumnIndex("n_bg_color")));
                note.setCreateTime(cursor.getLong(cursor.getColumnIndex("n_create_time")));
                note.setUpdateTime(cursor.getLong(cursor.getColumnIndex("n_update_time")));
                noteList.add(note);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return noteList;
    }

    /**
     * 紧急度排序
     */
    public List<Note> queryNotesByGroup() {
        SQLiteDatabase db = helper.getWritableDatabase();
        List<Note> noteList = new ArrayList<>();
        Note note;
        String sql;
        Cursor cursor = null;
        try {
            sql = "select * from db_note " +
                    "order by n_group_id";
            cursor = db.rawQuery(sql, null);
            //cursor = db.query("note", null, null, null, null, null, "n_id desc");
            while (cursor.moveToNext()) {
                //循环获得展品信息
                note = new Note();
                note.setId(cursor.getInt(cursor.getColumnIndex("n_id")));
                note.setTitle(cursor.getString(cursor.getColumnIndex("n_title")));
                note.setContent(cursor.getString(cursor.getColumnIndex("n_content")));
                note.setGroupId(cursor.getInt(cursor.getColumnIndex("n_group_id")));
                note.setLocation(cursor.getString(cursor.getColumnIndex("n_location_name")));
                note.setIsFinish(cursor.getInt(cursor.getColumnIndex("n_finish")));
                note.setBgColor(cursor.getString(cursor.getColumnIndex("n_bg_color")));
                note.setCreateTime(cursor.getLong(cursor.getColumnIndex("n_create_time")));
                note.setUpdateTime(cursor.getLong(cursor.getColumnIndex("n_update_time")));
                noteList.add(note);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return noteList;
    }
    /**
     * 创建时间排序
     */
    public List<Note> queryNotesByCreateTime() {
        SQLiteDatabase db = helper.getWritableDatabase();
        List<Note> noteList = new ArrayList<>();
        Note note;
        String sql;
        Cursor cursor = null;
        try {
            sql = "select * from db_note order by n_create_time desc";
            cursor = db.rawQuery(sql, null);
            //cursor = db.query("note", null, null, null, null, null, "n_id desc");
            while (cursor.moveToNext()) {
                //循环获得展品信息
                note = new Note();
                note.setId(cursor.getInt(cursor.getColumnIndex("n_id")));
                note.setTitle(cursor.getString(cursor.getColumnIndex("n_title")));
                note.setContent(cursor.getString(cursor.getColumnIndex("n_content")));
                note.setGroupId(cursor.getInt(cursor.getColumnIndex("n_group_id")));
                note.setLocation(cursor.getString(cursor.getColumnIndex("n_location_name")));
                note.setIsFinish(cursor.getInt(cursor.getColumnIndex("n_finish")));
                note.setBgColor(cursor.getString(cursor.getColumnIndex("n_bg_color")));
                note.setCreateTime(cursor.getLong(cursor.getColumnIndex("n_create_time")));
                note.setUpdateTime(cursor.getLong(cursor.getColumnIndex("n_update_time")));
                noteList.add(note);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return noteList;
    }
    /**
     * 修改时间排序
     */
    public List<Note> queryNotesByUpadteTime() {
        SQLiteDatabase db = helper.getWritableDatabase();
        List<Note> noteList = new ArrayList<>();
        Note note;
        String sql;
        Cursor cursor = null;
        try {
            sql = "select * from db_note " +
                    "order by n_update_time desc";
            cursor = db.rawQuery(sql, null);
            //cursor = db.query("note", null, null, null, null, null, "n_id desc");
            while (cursor.moveToNext()) {
                //循环获得展品信息
                note = new Note();
                note.setId(cursor.getInt(cursor.getColumnIndex("n_id")));
                note.setTitle(cursor.getString(cursor.getColumnIndex("n_title")));
                note.setContent(cursor.getString(cursor.getColumnIndex("n_content")));
                note.setGroupId(cursor.getInt(cursor.getColumnIndex("n_group_id")));
                note.setLocation(cursor.getString(cursor.getColumnIndex("n_location_name")));
                note.setIsFinish(cursor.getInt(cursor.getColumnIndex("n_finish")));
                note.setBgColor(cursor.getString(cursor.getColumnIndex("n_bg_color")));
                note.setCreateTime(cursor.getLong(cursor.getColumnIndex("n_create_time")));
                note.setUpdateTime(cursor.getLong(cursor.getColumnIndex("n_update_time")));
                noteList.add(note);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return noteList;
    }
    /**
     * 完成排序
     */
    public List<Note> queryNotesByFinish() {
        SQLiteDatabase db = helper.getWritableDatabase();
        List<Note> noteList = new ArrayList<>();
        Note note;
        String sql;
        Cursor cursor = null;
        try {
            sql = "select * from db_note " +
                    "order by n_finish ";
            cursor = db.rawQuery(sql, null);
            //cursor = db.query("note", null, null, null, null, null, "n_id desc");
            while (cursor.moveToNext()) {
                //循环获得展品信息
                note = new Note();
                note.setId(cursor.getInt(cursor.getColumnIndex("n_id")));
                note.setTitle(cursor.getString(cursor.getColumnIndex("n_title")));
                note.setContent(cursor.getString(cursor.getColumnIndex("n_content")));
                note.setGroupId(cursor.getInt(cursor.getColumnIndex("n_group_id")));
                note.setLocation(cursor.getString(cursor.getColumnIndex("n_location_name")));
                note.setIsFinish(cursor.getInt(cursor.getColumnIndex("n_finish")));
                note.setBgColor(cursor.getString(cursor.getColumnIndex("n_bg_color")));
                note.setCreateTime(cursor.getLong(cursor.getColumnIndex("n_create_time")));
                note.setUpdateTime(cursor.getLong(cursor.getColumnIndex("n_update_time")));
                noteList.add(note);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return noteList;
    }

    /**
     * 插入笔记
     */
    public long insertNote(Note note) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String sql = "insert into db_note(n_title,n_content,n_group_id,n_location_name," +
                "n_finish,n_bg_color,n_create_time,n_update_time) " +
                "values(?,?,?,?,?,?,?,?)";

        long ret = 0;
        SQLiteStatement stat = db.compileStatement(sql);
        db.beginTransaction();
        try {
            stat.bindString(1, note.getTitle());
            stat.bindString(2, note.getContent());
            stat.bindLong(3, note.getGroupId());
            stat.bindString(4, note.getLocation());
            stat.bindLong(5, note.getIsFinish());
            stat.bindString(6, note.getBgColor());
            stat.bindLong(7, note.getCreateTime());
            stat.bindLong(8, note.getUpdateTime());
            ret = stat.executeInsert();
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }
        return ret;
    }
    /**
     * 插入或更新笔记
     */
    public void insertOrUpdateNote(Note note) {
        if(getNoteFromCreateTime(note.getCreateTime()) != null){
            //更新
            LogUtil.e("更新本地笔记数据 标题："+note.getTitle());
            updateNote(note);
        }else{
            //插入
            LogUtil.e("新建本地笔记数据 标题："+note.getTitle());
            insertNote(note);
        }
    }

    /**
     * 更新笔记
     *
     * @param note
     */
    public void updateNote(Note note) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("n_title", note.getTitle());
        values.put("n_content", note.getContent());
        values.put("n_group_id", note.getGroupId());
        values.put("n_location_name", note.getLocation());
        values.put("n_finish", note.getIsFinish());
        values.put("n_bg_color", note.getBgColor());
        values.put("n_update_time", note.getUpdateTime());
        db.update("db_note", values, "n_id=?", new String[]{note.getId() + ""});
        db.close();
    }
    /**
     * 获取笔记
     *
     * @param noteId
     */
    public Note getNote(int noteId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        List<Note> noteList = new ArrayList<>();
        Note note;
        String sql;
        Cursor cursor = null;
        try {
            sql = "select * from db_note where n_id =" + noteId;
            cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                //循环获得展品信息
                note = new Note();
                note.setId(cursor.getInt(cursor.getColumnIndex("n_id")));
                note.setTitle(cursor.getString(cursor.getColumnIndex("n_title")));
                note.setContent(cursor.getString(cursor.getColumnIndex("n_content")));
                note.setGroupId(cursor.getInt(cursor.getColumnIndex("n_group_id")));
                note.setLocation(cursor.getString(cursor.getColumnIndex("n_location_name")));
                note.setIsFinish(cursor.getInt(cursor.getColumnIndex("n_finish")));
                note.setBgColor(cursor.getString(cursor.getColumnIndex("n_bg_color")));
                note.setCreateTime(cursor.getLong(cursor.getColumnIndex("n_create_time")));
                note.setUpdateTime(cursor.getLong(cursor.getColumnIndex("n_update_time")));
                noteList.add(note);
                return note;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return null;
    }
    /**
     * 获取笔记
     *
     * @param createTime
     */
    public Note getNoteFromCreateTime(long createTime) {
        SQLiteDatabase db = helper.getWritableDatabase();
        List<Note> noteList = new ArrayList<>();
        Note note;
        String sql;
        Cursor cursor = null;
        try {
            sql = "select * from db_note where n_create_time =" + createTime;
            cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                //循环获得展品信息
                note = new Note();
                note.setId(cursor.getInt(cursor.getColumnIndex("n_id")));
                note.setTitle(cursor.getString(cursor.getColumnIndex("n_title")));
                note.setContent(cursor.getString(cursor.getColumnIndex("n_content")));
                note.setGroupId(cursor.getInt(cursor.getColumnIndex("n_group_id")));
                note.setLocation(cursor.getString(cursor.getColumnIndex("n_location_name")));
                note.setIsFinish(cursor.getInt(cursor.getColumnIndex("n_finish")));
                note.setBgColor(cursor.getString(cursor.getColumnIndex("n_bg_color")));
                note.setCreateTime(cursor.getLong(cursor.getColumnIndex("n_create_time")));
                note.setUpdateTime(cursor.getLong(cursor.getColumnIndex("n_update_time")));
                noteList.add(note);
                return note;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return null;
    }

    /**
     * 删除笔记
     */
    public int deleteNote(int noteId) {
        Note note = getNote(noteId);
        if(note != null){
            deleteImageInNote(note);
        }
        SQLiteDatabase db = helper.getWritableDatabase();
        int ret = 0;
        try {
            ret = db.delete("db_note", "n_id=?", new String[]{noteId + ""});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return ret;
    }

    public void deleteImageInNote(Note note) {
        List<String> urls = note.getImageUrls();
        for(String imageUrl:urls){
            LogUtil.e("deleting image:"+imageUrl);
            ImageUtils.deleteTempFile(imageUrl);
        }

//        if (content.contains("<img") && content.contains("src=")) {
//            int startIndex = content.indexOf("src=")+5;
//            int endIndex = content.indexOf("\"/>");
//            deleteImageInNote(content.substring(endIndex+3,content.length()));
//            String imageUrl = content.substring(startIndex,endIndex);
//            Log.v("NoteDao","deleting image:"+imageUrl);
//            ImageUtils.deleteTempFile(imageUrl);
//        }
    }

    /**
     * 批量删除笔记
     *
     * @param mNotes
     */
    public int deleteNote(List<Note> mNotes) {
        SQLiteDatabase db = helper.getWritableDatabase();
        int ret = 0;
        try {
            if (mNotes != null && mNotes.size() > 0) {
                db.beginTransaction();//开始事务
                try {
                    for (Note note : mNotes) {
                        ret += db.delete("db_note", "n_id=?", new String[]{note.getId() + ""});
                    }
                    db.setTransactionSuccessful();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    db.endTransaction();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return ret;
    }
}
