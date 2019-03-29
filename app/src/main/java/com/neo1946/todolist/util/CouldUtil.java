package com.neo1946.todolist.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.avos.avoscloud.AVObject;
import com.neo1946.todolist.bean.Note;

import static android.content.Context.MODE_PRIVATE;
import static com.neo1946.todolist.MyApplication.SP_NAME;

/**
 * @author ouyangzhaoxian on 2019/03/13
 * 同步模块 管理上传数据和下载数据
 */
public class CouldUtil {
    public static final String TABLE_NAME = "main_note";
    private static final int STATUS_DOWNLOAD = 1;
    private static final int STATUS_UPLOAD = 2;
    private static final int STATUS_NONE = 0;
    private Context context;
    private String userName;
    private CouldDownLoadUtil downLoadUtil;
    private CouldUploadUtil uploadUtil;

    public CouldUtil(Context context) {
        this.context = context;
        SharedPreferences sharedPreferences = context.getSharedPreferences(SP_NAME, MODE_PRIVATE);
        userName = sharedPreferences.getString("userName", "");
    }

    static AVObject getObjectFromNote(Note note, String userName) {
        AVObject object = new AVObject(TABLE_NAME);
        object.put("id", note.getId());
        object.put("userName", userName);
        object.put("title", note.getTitle());
        object.put("content", note.getContent());
        object.put("groupId", note.getGroupId());
        object.put("location", note.getLocation());
        object.put("isFinish", note.getIsFinish());
        object.put("bgColor", note.getBgColor());
        object.put("createTime", note.getCreateTime());
        object.put("updateTime", note.getUpdateTime());
        return object;
    }

    static Note getNoteFromObject(AVObject object) {
        Note note = new Note();
        note.setId(object.getInt("id"));
        note.setTitle(object.getString("title"));
        note.setContent(object.getString("content"));
        note.setGroupId(object.getInt("groupId"));
        note.setLocation(object.getString("location"));
        note.setIsFinish(object.getInt("isFinish"));
        note.setBgColor(object.getString("bgColor"));
        note.setCreateTime(object.getLong("createTime"));
        note.setUpdateTime(object.getLong("updateTime"));
        return note;
    }
    static void copyObjectData(AVObject from,AVObject to) {
        to.put("id", from.getInt("id"));
        to.put("userName", from.getString("userName"));
        to.put("title", from.getString("title"));
        to.put("content", from.getString("content"));
        to.put("groupId", from.getInt("groupId"));
        to.put("location", from.getString("location"));
        to.put("isFinish",from.getInt("isFinish"));
        to.put("bgColor", from.getString("bgColor"));
        to.put("createTime",from.getLong("createTime"));
        to.put("updateTime",from.getLong("updateTime"));
    }

    /**
     * 同步 先将数据 下载&合并 然后将合并结果上传
     */
    public void sync() {
        if ("".equals(userName)) {
            Toast.makeText(context, "你还没有登录呢", Toast.LENGTH_SHORT).show();
            return;
        }
        downLoadUtil = new CouldDownLoadUtil(context, userName);
        downLoadUtil.isSync = true;
        downLoadUtil.downloadAndMerge();
    }
    /**
     * 仅下载
     */
    public void downloadOnly() {
        if ("".equals(userName)) {
            Toast.makeText(context, "你还没有登录呢", Toast.LENGTH_SHORT).show();
            return;
        }
        downLoadUtil = new CouldDownLoadUtil(context, userName);
        downLoadUtil.downloadAndMerge();
    }
    /**
     * 仅上传
     */
    public void uploadOnly() {
        if ("".equals(userName)) {
            Toast.makeText(context, "你还没有登录呢", Toast.LENGTH_SHORT).show();
            return;
        }
        uploadUtil = new CouldUploadUtil(context, userName);
        uploadUtil.upLoad();
    }


    public static class SyncFinishEvent{
        public boolean finish = false;
    }

    public static class SyncProgressEvent{
        public int percent = 0;
        public String message = "";

        public SyncProgressEvent(int percent, String message) {
            this.percent = percent;
            this.message = message;
        }
    }

}
