package com.neo1946.todolist.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.GetDataCallback;
import com.avos.avoscloud.ProgressCallback;
import com.avos.avoscloud.SaveCallback;
import com.neo1946.todolist.richtext.RichTextView;
import com.neo1946.todolist.R;
import com.neo1946.todolist.bean.Group;
import com.neo1946.todolist.bean.Note;
import com.neo1946.todolist.db.GroupDao;
import com.neo1946.todolist.db.NoteDao;
import com.neo1946.todolist.util.ColorUtil;
import com.neo1946.todolist.util.CommonUtil;
import com.neo1946.todolist.util.ImageUtils;
import com.neo1946.todolist.util.LogUtil;
import com.neo1946.todolist.util.SDCardUtil;
import com.neo1946.todolist.util.StringUtils;
import com.neo1946.todolist.util.TimeUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * @author ouyangzhaoxian on 2019/3/11.
 * 笔记详情
 */
public class NoteActivity extends BaseActivity {
    private static final String TAG = "NoteActivity";

    private TextView tv_note_title;//笔记标题
    private RichTextView tv_note_content;//笔记内容
    private TextView tv_note_time;//笔记创建时间
    private TextView tv_note_group;//选择笔记分类
    private View v_color;//紧急度
    private ImageView iv_finish;//是否完成

    //private ScrollView scroll_view;
    private Note note;//笔记对象
    private String myTitle;
    private String myContent;
    private String myGroupName;
    private NoteDao noteDao;
    private GroupDao groupDao;
    private int isFinish;

    private ProgressDialog loadingDialog;
    private Disposable mDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        initView();

    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_note);
//        toolbar.setTitle("笔记详情");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        //toolbar.setNavigationIcon(R.drawable.ic_dialog_info);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_note);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        noteDao = new NoteDao(this);
        groupDao = new GroupDao(this);

        loadingDialog = new ProgressDialog(this);
        loadingDialog.setMessage("数据加载中...");
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.show();

        tv_note_title = (TextView) findViewById(R.id.tv_note_title);//标题
        tv_note_title.setTextIsSelectable(true);
        tv_note_content = (RichTextView) findViewById(R.id.tv_note_content);//内容
        tv_note_time = (TextView) findViewById(R.id.tv_note_time);
        tv_note_group = (TextView) findViewById(R.id.tv_note_group);
        v_color = (View) findViewById(R.id.v_color);
        iv_finish = (ImageView) findViewById(R.id.iv_finish);

        iv_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isFinish == 1){
                    iv_finish.setBackgroundResource(R.drawable.untick);
                    isFinish = 0;
                    note.setIsFinish(0);
                    NoteDao dao = new NoteDao(NoteActivity.this);
                    dao.updateNote(note);
                }else{
                    iv_finish.setBackgroundResource(R.drawable.tick);
                    isFinish = 1;
                    note.setIsFinish(1);
                    NoteDao dao = new NoteDao(NoteActivity.this);
                    dao.updateNote(note);
                }
            }
        });
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("data");
        note = (Note) bundle.getSerializable("note");

        if (note != null) {
            myTitle = note.getTitle();
            myContent = note.getContent();
            Group group = groupDao.queryGroupById(note.getGroupId());
            if (group != null) {
                myGroupName = group.getName();
                tv_note_group.setText(myGroupName);
            }
            ColorUtil.setViewColor(v_color,note.getGroupId());
            tv_note_title.setText(myTitle);
            tv_note_content.post(new Runnable() {
                @Override
                public void run() {
                    dealWithContent();
                }
            });
            tv_note_time.setText(TimeUtil.getTimeFormat(note.getCreateTime()));
            isFinish = note.getIsFinish();
            if(isFinish == 1){
                iv_finish.setBackgroundResource(R.drawable.tick);
            }else{
                iv_finish.setBackgroundResource(R.drawable.untick);
            }
        }else{
            finish();
        }

    }

    private void dealWithContent(){
        //showEditData(myContent);
        tv_note_content.clearAllLayout();
        showDataSync(myContent);

        // 图片点击事件
        tv_note_content.setOnRtImageClickListener(new RichTextView.OnRtImageClickListener() {
            @Override
            public void onRtImageClick(String imagePath) {
                ArrayList<String> imageList = StringUtils.getTextFromHtml(myContent, true);
                int currentPosition = imageList.indexOf(imagePath);
                showToast("点击图片："+currentPosition+"："+imagePath);

                //点击图片预览
//                PhotoPreview.builder()
//                        .setPhotos(imageList)
//                        .setCurrentItem(currentPosition)
//                        .setShowDeleteButton(false)
//                        .start(NoteActivity.this);
            }
        });
    }

    /**
     * 异步方式显示数据
     * @param html
     */
    private void showDataSync(final String html){

        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) {
                showEditData(emitter, html);
            }
        })
        //.onBackpressureBuffer()
        .subscribeOn(Schedulers.io())//生产事件在io
        .observeOn(AndroidSchedulers.mainThread())//消费事件在UI线程
        .subscribe(new Observer<String>() {
            @Override
            public void onComplete() {
                if (loadingDialog != null){
                    loadingDialog.dismiss();
                }
            }

            @Override
            public void onError(Throwable e) {
                if (loadingDialog != null){
                    loadingDialog.dismiss();
                }
                showToast("解析错误：图片不存在或已损坏");
                LogUtil.e("onError: " + e.getMessage());
            }

            @Override
            public void onSubscribe(Disposable d) {
                mDisposable = d;
            }

            @Override
            public void onNext(String text) {
                if (tv_note_content !=null) {
                    if (text.contains("<img") && text.contains("src=")) {
                        //imagePath可能是本地路径，也可能是网络地址
                        String imagePath = StringUtils.getImgSrc(text);
                        tv_note_content.addImageViewAtIndex(tv_note_content.getLastIndex(), imagePath);
                    } else {
                        tv_note_content.addTextViewAtIndex(tv_note_content.getLastIndex(), text);
                    }
                }
            }
        });

    }

    /**
     * 显示数据
     * @param html
     */
    private void showEditData(ObservableEmitter<String> emitter, String html) {
        try {
            List<String> textList = StringUtils.cutStringByImgTag(html);
            for (int i = 0; i < textList.size(); i++) {
                String text = textList.get(i);
                emitter.onNext(text);
            }
            emitter.onComplete();
        } catch (Exception e){
            e.printStackTrace();
            emitter.onError(e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_note_edit://编辑笔记
                Intent intent = new Intent(NoteActivity.this, NewActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("note", note);
                intent.putExtra("data", bundle);
                intent.putExtra("flag", 1);//编辑笔记
                startActivity(intent);
                finish();
                break;
            case R.id.action_note_share://分享笔记
                CommonUtil.shareTextAndImage(this, note.getTitle(), note.getContent(), null);//分享图文
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mDisposable != null && !mDisposable.isDisposed()){
            mDisposable.dispose();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
