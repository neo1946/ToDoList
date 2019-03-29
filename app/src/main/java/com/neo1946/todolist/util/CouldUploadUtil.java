package com.neo1946.todolist.util;

import android.content.Context;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.SaveCallback;
import com.neo1946.todolist.bean.Note;
import com.neo1946.todolist.db.NoteDao;

import org.greenrobot.eventbus.EventBus;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import static com.neo1946.todolist.util.CouldUtil.TABLE_NAME;

/**
 * @author ouyangzhaoxian on 2019/03/13
 */
public class CouldUploadUtil extends SaveCallback {
    private Integer[] mUploadIndex = {0, 0};
    private Integer[] mImageUrlIndex = {0};
    private List<String> mUploadImages = new ArrayList<>();
    private List<Note> mUploadNote = new ArrayList<>();
    private List<AVObject> uploadList = new ArrayList<>();
    private AVFile mUploadTemFile;
    private Context context;
    private String userName;

    public CouldUploadUtil(Context context, String userName) {
        this.context = context;
        this.userName = userName;
    }

    /**
     * 获取需要上传的本地note
     */
    public void upLoad() {
        NoteDao dao = new NoteDao(context);
        mUploadNote = dao.queryNotesByGroup();
        LogUtil.e("读取数据完成 开始上传");
        upLoadInner();
    }

    /**
     * 获取笔记中所有的图片
     */
    private void upLoadInner() {
        synchronized (this) {
            for (Note note : mUploadNote) {
                mUploadImages.addAll(note.getImageUrls());
            }
            mUploadIndex[0] = 0;
            mUploadIndex[1] = 0;
            mImageUrlIndex[0] = 0;
            LogUtil.e("遍历笔记 图片资源完成 开始下载图片");
            EventBus.getDefault().post(new CouldUtil.SyncProgressEvent(0,"开始上传 所有笔记中的"+mUploadImages.size()+"张图片"));
            if (mUploadImages.size() > 0) {
                uploadImages(mUploadImages.get(0), this);
            }
        }
    }
    @Override
    public void done(AVException e) {
        synchronized (CouldUploadUtil.class) {
            if (e == null ||(e != null && e.getCode() == 137)) {
                final String mUploadImageUrl = mUploadImages.get(mUploadIndex[0]);
                if(e != null && e.getCode() == 137){
                    LogUtil.e(  "该图片已存与云端(" + (mUploadIndex[0] + 1) + "/" + mUploadImages.size() + ")");
                    EventBus.getDefault().post(new CouldUtil.SyncProgressEvent(0,"该图片已存与云端(" + (mUploadIndex[0] + 1) + "/" + mUploadImages.size() + ")"));
                    AVQuery<AVObject> query = new AVQuery<>("_File");
                    query.whereEqualTo("name", mUploadImageUrl.substring(mUploadImageUrl.lastIndexOf("/")+1,mUploadImageUrl.length()));
                    query.findInBackground(new FindCallback<AVObject>() {
                        @Override
                        public void done(List<AVObject> avObjects, AVException avException) {
                            if(avException == null){
                                while (mUploadNote.size() > mUploadIndex[1]) {
                                    Note uploadNote = mUploadNote.get(mUploadIndex[1]);
                                    if (uploadNote.getContent().contains(mUploadImageUrl)) {
                                        uploadNote.setContent(uploadNote.getContent().replace(mUploadImageUrl, avObjects.get(0).getString("url")+"?name="+mUploadImageUrl.substring(mUploadImageUrl.lastIndexOf("/")+1,mUploadImageUrl.length())));
//                            LogUtil.e(  "修改云端图片URL完成 " + mUploadImageUrl + "→" + targetUrl);
                                        mImageUrlIndex[0]++;
                                        uploadallNote();
                                        break;
                                    }
                                    mUploadIndex[1]++;
                                }
                            }else{

                            }
                        }
                    });
                }else{
                    LogUtil.e(  "图片上传成功 (" + (mUploadIndex[0] + 1) + "/" + mUploadImages.size() + ")");
                    EventBus.getDefault().post(new CouldUtil.SyncProgressEvent(0,"图片上传成功 (" + (mUploadIndex[0] + 1) + "/" + mUploadImages.size() + ")"));
                    String targetUrl = mUploadTemFile.getUrl();
                    while (mUploadNote.size() > mUploadIndex[1]) {
                        Note uploadNote = mUploadNote.get(mUploadIndex[1]);
                        if (uploadNote.getContent().contains(mUploadImageUrl)) {
                            uploadNote.setContent(uploadNote.getContent().replace(mUploadImageUrl, targetUrl+"?name="+mUploadImageUrl.substring(mUploadImageUrl.lastIndexOf("/")+1,mUploadImageUrl.length())));
//                            LogUtil.e(  "修改云端图片URL完成 " + mUploadImageUrl + "→" + targetUrl);
                            mImageUrlIndex[0]++;
                            uploadallNote();
                            break;
                        }
                        mUploadIndex[1]++;
                    }
                }
                mUploadIndex[0]++;//计数+1
                if (mUploadImages.size() > mUploadIndex[0]) {
                    //没下载完 下载下一张图片
                    uploadImages(mUploadImages.get(mUploadIndex[0]), this);
                }
            } else {
                LogUtil.e(  "图片上传失败  (" + (mUploadIndex[0] + 1) + "/" + mUploadImages.size() + ") 错误码:"+e.getCode());
            }
        }
    }

    /**
     * 因为文件要单独上传
     * 先上传图片
     */
    private void uploadImages(final String ImageUrl, final SaveCallback callback) {
        try {
            mUploadTemFile = AVFile.withAbsoluteLocalPath(ImageUrl.substring(ImageUrl.lastIndexOf("/") + 1, ImageUrl.length()), ImageUrl);
            mUploadTemFile.saveInBackground(callback);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 上传完成之后上传note
     */

    private void uploadallNote() {
        if(mImageUrlIndex[0] < mUploadImages.size()){
            return;
        }
        mUploadIndex[0] = 0;
        //下载完成
        LogUtil.e(  "所有图片上传完成 开始上传笔记");
        for (Note note : mUploadNote) {
            uploadList.add(CouldUtil.getObjectFromNote(note, userName));
            LogUtil.e(  "整理后笔记内容:" + note.getContent());
        }
        LogUtil.e(  "所有笔记整理完成 开始上传:");
        CouldDownLoadUtil downLoadUtil = new CouldDownLoadUtil(context, userName);
        downLoadUtil.downloadOnly(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> avObjects, AVException avException) {
                if (avException == null) {
                    for (final AVObject object : uploadList) {
                        boolean hacCould = false;
                        for (final AVObject dObject : avObjects) {
                            if (object.getString("userName").equals(dObject.getString("userName")) && object.getLong("createTime") == dObject.getLong("createTime")) {
                                CouldUtil.copyObjectData(object, dObject);
                                dObject.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(AVException e) {
                                        if (e == null) {
                                            LogUtil.e(  "更新成功 笔记标题："+dObject.getString("title"));
                                            mUploadIndex[0]++;
                                            checkFinish();
                                        } else {
                                            LogUtil.e(  "已有该笔记数据 更新失败 原因" + e.getMessage()+" 错误码："+e.getCode());
                                        }
                                    }
                                });
                                hacCould = true;
                                break;
                            }
                        }
                        if(!hacCould){
                            object.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(AVException e) {
                                    if (e == null) {
                                        LogUtil.e(  "云端新建笔记成功 笔记标题："+object.getString("title"));
                                        mUploadIndex[0]++;
                                        checkFinish();
                                    } else {
                                        LogUtil.e(  "云端新建笔记失败 原因" + e.getMessage()+" 错误码："+e.getCode());
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });
    }

    private void checkFinish(){
        if(mUploadIndex[0] < mUploadNote.size()){
            return;
        }
        CouldUtil.SyncFinishEvent event = new CouldUtil.SyncFinishEvent();
        event.finish = true;
        EventBus.getDefault().post(new CouldUtil.SyncProgressEvent(0,"上传完成"));
        EventBus.getDefault().post(event);
    }
}
