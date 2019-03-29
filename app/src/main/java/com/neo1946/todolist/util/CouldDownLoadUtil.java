package com.neo1946.todolist.util;

import android.content.Context;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.GetDataCallback;
import com.neo1946.todolist.bean.Note;
import com.neo1946.todolist.db.NoteDao;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.neo1946.todolist.util.CouldUtil.TABLE_NAME;
import static com.neo1946.todolist.util.CouldUtil.getNoteFromObject;
import static com.neo1946.todolist.util.SDCardUtil.getPictureDir;

/**
 * @author ouyangzhaoxian on 2019/03/13
 */
public class CouldDownLoadUtil extends GetDataCallback {

    private Context context;
    private String userName;
    private List<String> mDownloadImages = new ArrayList<>();
    private List<Note> mMergeNotes = new ArrayList<>();
    private AVFile mDownloadFile;
    private Integer[] mDownLoadIndex = {0,0};
    public boolean isSync = false;


    public CouldDownLoadUtil(Context context, String userName) {
        this.context = context;
        this.userName = userName;
    }

    /**
     * 只下载AVObject原始格式 不作处理
     * 用于比对
     * @param callback
     */
    public void downloadOnly(FindCallback<AVObject> callback){
        AVQuery<AVObject> query = new AVQuery<>(TABLE_NAME);
        query.whereEqualTo("userName", userName);
        query.findInBackground(callback);
    }

    /**
     * 下载云端数据然后合并
     */
    public void downloadAndMerge(){
        LogUtil.e("开始下载云端笔记数据");
        AVQuery<AVObject> query = new AVQuery<>(TABLE_NAME);
        query.whereEqualTo("userName", userName);
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> list, AVException e) {
                List<Note> mDownLoadNotes = new ArrayList<>();
                for(AVObject avObject:list){
                    Note note = getNoteFromObject(avObject);
                    LogUtil.e(
                            note.toString());
                    mDownLoadNotes.add(note);
                }
                LogUtil.e("下载数据成功");
                EventBus.getDefault().post(new CouldUtil.SyncProgressEvent(0,"下载笔记成功"));
                dealWithDownLoadNotes(mDownLoadNotes);
            }
        });
    }

    /**
     * 下载完成 开始合并
     * @param notes
     */
    private void dealWithDownLoadNotes(List<Note> notes) {
        NoteDao dao = new NoteDao(context);
        List<Note> mLocalNotes = dao.queryNotesByGroup();
        LogUtil.e("读取本地数据成功 开始比对");
        EventBus.getDefault().post(new CouldUtil.SyncProgressEvent(0,"与本地笔记比对中"));
        mMergeNotes = new ArrayList<>();
        int index = 1;
        for(Note note:notes){
            if(mLocalNotes.contains(note)){
                //存在云端和本地备份
                Note localNote = mLocalNotes.get(mLocalNotes.indexOf(note));

                //比对最后更改时间 以最后更改时间为条件
                if(note.getUpdateTime() > localNote.getUpdateTime()){
                    LogUtil.e("该笔记最后修改在云端 以云端为准\t\t("+index+"/"+notes.size()+")");
                    EventBus.getDefault().post(new CouldUtil.SyncProgressEvent(0,"该笔记最后修改在云端 以云端为准\t\t("+index+"/"+notes.size()+")"));
                    mMergeNotes.add(note);
                }else{
                    LogUtil.e("该笔记最后修改在本地 以本地为准\t\t("+index+"/"+notes.size()+")");
                    EventBus.getDefault().post(new CouldUtil.SyncProgressEvent(0,"该笔记最后修改在本地 以本地为准\t\t("+index+"/"+notes.size()+")"));
                    mMergeNotes.add(localNote);
                }
            }else{
                //仅在云端有备份 直接下载
                LogUtil.e("该笔记仅在云端有备份\t\t("+index+"/"+notes.size()+")");
                EventBus.getDefault().post(new CouldUtil.SyncProgressEvent(0,"该笔记仅在云端有备份\t\t("+index+"/"+notes.size()+")"));
                mMergeNotes.add(note);
            }
            index++;
        }
        LogUtil.e("比对完成 开始扫描笔记中的图片");
        mDownloadImages = new ArrayList<>();
        for (Note note:mMergeNotes){
            mDownloadImages.addAll(note.getNetworkImageUrls());
        }
        LogUtil.e("扫描完成 开始下载图片");
        EventBus.getDefault().post(new CouldUtil.SyncProgressEvent(0,"开始下载图片"));
        while (mDownloadImages.size() >= mDownLoadIndex[0]) {
            if(mDownloadImages.size() == mDownLoadIndex[0]){
                downloadNote();
                break;
            }
            String imageUrl = mDownloadImages.get(mDownLoadIndex[0]);
            String localUrl = SDCardUtil.getPictureDir()+imageUrl.substring(imageUrl.lastIndexOf("?name=")+6,imageUrl.length());
            if(SDCardUtil.imageExist(localUrl)){
                LogUtil.e("该图片已经存在 无需下载(" + (mDownLoadIndex[0] + 1) + "/" + mDownloadImages.size() + ")");
                EventBus.getDefault().post(new CouldUtil.SyncProgressEvent(0,"该图片已经存在 无需下载(" + (mDownLoadIndex[0] + 1) + "/" + mDownloadImages.size() + ")"));
                while (mMergeNotes.size() > mDownLoadIndex[1]) {
                    if (mMergeNotes.get(mDownLoadIndex[1]).getContent().contains(mDownloadImages.get(mDownLoadIndex[0]))) {
                        mMergeNotes.get(mDownLoadIndex[1]).setContent(mMergeNotes.get(mDownLoadIndex[1]).getContent().replace(mDownloadImages.get(mDownLoadIndex[0]), localUrl));
                        LogUtil.e("处理笔记内容完成 " + mDownloadImages.get(mDownLoadIndex[0]) + " → " + localUrl);
                        break;
                    }
                    mDownLoadIndex[1]++;
                }
                mDownLoadIndex[0]++;
            }else{
                LogUtil.e("开始下载图片 (" + (mDownLoadIndex[0] + 1) + "/" + mDownloadImages.size() + ")");
                downloadImages(imageUrl,this);
                break;
            }
        }


    }
    private void downloadImages(final String ImageUrl, final GetDataCallback callback) {
        mDownloadFile = new AVFile("test.jpeg", ImageUrl, new HashMap<String, Object>());
        mDownloadFile.getDataInBackground(callback);
    }

    @Override
    public void done(byte[] bytes, AVException e) {
        synchronized (CouldUploadUtil.class) {
            if (e == null) {
                LogUtil.e("下载图片完成(" + (mDownLoadIndex[0] + 1) + "/" + mDownloadImages.size() + ")");
                EventBus.getDefault().post(new CouldUtil.SyncProgressEvent(0,"下载图片完成(" + (mDownLoadIndex[0] + 1) + "/" + mDownloadImages.size() + ")"));
                String targetUrl = mDownloadFile.getUrl();
                String localUrl = SDCardUtil.saveToSdCardWithName(bytes,targetUrl.substring(targetUrl.lastIndexOf("?name=")+6,targetUrl.length()));
                LogUtil.e("保存完成(" + (mDownLoadIndex[0] + 1) + "/" + mDownloadImages.size() + ")");
                while (mMergeNotes.size() > mDownLoadIndex[1]) {
                    if (mMergeNotes.get(mDownLoadIndex[1]).getContent().contains(mDownloadImages.get(mDownLoadIndex[0]))) {
                        mMergeNotes.get(mDownLoadIndex[1]).setContent(mMergeNotes.get(mDownLoadIndex[1]).getContent().replace(mDownloadImages.get(mDownLoadIndex[0]), localUrl));
                        LogUtil.e("处理笔记内容完成 " + mDownloadImages.get(mDownLoadIndex[0]) + " → " + localUrl);
                        break;
                    }
                    mDownLoadIndex[1]++;
                }
                mDownLoadIndex[0]++;
                while (mDownloadImages.size() > mDownLoadIndex[0]) {
                    String imageUrl = mDownloadImages.get(mDownLoadIndex[0]);
                    String temLocalUrl = SDCardUtil.getPictureDir()+imageUrl.substring(imageUrl.lastIndexOf("?name=")+6,imageUrl.length());
                    if(SDCardUtil.imageExist(temLocalUrl)){
                        LogUtil.e("该图片已经存在 无需下载(" + (mDownLoadIndex[0] + 1) + "/" + mDownloadImages.size() + ")");
                        EventBus.getDefault().post(new CouldUtil.SyncProgressEvent(0,"该图片已经存在 无需下载(" + (mDownLoadIndex[0] + 1) + "/" + mDownloadImages.size() + ")"));
                        while (mMergeNotes.size() > mDownLoadIndex[1]) {
                            if (mMergeNotes.get(mDownLoadIndex[1]).getContent().contains(mDownloadImages.get(mDownLoadIndex[0]))) {
                                mMergeNotes.get(mDownLoadIndex[1]).setContent(mMergeNotes.get(mDownLoadIndex[1]).getContent().replace(mDownloadImages.get(mDownLoadIndex[0]), temLocalUrl));
                                LogUtil.e("处理笔记内容完成 " + mDownloadImages.get(mDownLoadIndex[0]) + " → " + temLocalUrl);
                                break;
                            }
                            mDownLoadIndex[1]++;
                        }
                        mDownLoadIndex[0]++;
                    }else{
                        LogUtil.e("开始下载图片 (" + (mDownLoadIndex[0] + 1) + "/" + mDownloadImages.size() + ")");
                        downloadImages(imageUrl,this);
                        break;
                    }
                }
                if (mDownloadImages.size() == mDownLoadIndex[0]) {
                    LogUtil.e(  "所有图片下载完成 开始更新本地笔记数据");
                    downloadNote();
                    return;
                }
            } else {
                LogUtil.e(  "下载图片失败!!  (" + (mDownLoadIndex[0] + 1) + "/" + mDownloadImages.size() + ")");
            }
        }
    }

    private void downloadNote() {
        NoteDao dao = new NoteDao(context);
        for(Note note:mMergeNotes){
            dao.insertOrUpdateNote(note);
        }
        LogUtil.e( "更新云端数据完成！");
        EventBus.getDefault().post(new CouldUtil.SyncProgressEvent(0,"更新云端数据完成！"));
        if(!isSync){
            CouldUtil.SyncFinishEvent event = new CouldUtil.SyncFinishEvent();
            event.finish = true;
            EventBus.getDefault().post(event);
        }
        CouldUploadUtil uploadUtil = new CouldUploadUtil(context, userName);
        uploadUtil.upLoad();
    }

}
