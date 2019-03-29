package com.neo1946.todolist.ui;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.neo1946.todolist.MyApplication;
import com.neo1946.todolist.R;
import com.neo1946.todolist.adapter.MyGroupSpinnerAdapter;
import com.neo1946.todolist.bean.Group;
import com.neo1946.todolist.bean.Note;
import com.neo1946.todolist.db.GroupDao;
import com.neo1946.todolist.db.NoteDao;
import com.neo1946.todolist.richtext.RichTextEditor;
import com.neo1946.todolist.util.CommonUtil;
import com.neo1946.todolist.util.ImageUtils;
import com.neo1946.todolist.util.LocationUtil;
import com.neo1946.todolist.util.LogUtil;
import com.neo1946.todolist.util.MyGlideEngine;
import com.neo1946.todolist.util.SDCardUtil;
import com.neo1946.todolist.util.StringUtils;
import com.neo1946.todolist.util.TimeUtil;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.neo1946.todolist.util.DialogUtil.showAuthDialog;

/**
 * @author ouyangzhaoxian on 2019/3/11.
 * 新建笔记
 */
public class NewActivity extends BaseActivity {
    private final static String TAG = "NewActivity";
    public static final int REQUEST_CODE_CHOOSE = 23;//定义请求码常量

    public static final int WRITE_PERMISSION = 1;//定义请求码常量
    public static final int GPS_PERMISSION = 2;//定义请求码常量

    private EditText et_new_title;
    private RichTextEditor et_new_content;
    private TextView tv_new_time;
    private TextView tv_new_location;
    private Spinner sp_group;
    private ImageView iv_finish;

    private GroupDao groupDao;
    private NoteDao noteDao;
    private Note note;//笔记对象
    private String myTitle;
    private String myContent;
    private int mOldGouupId;
    private int mGroupId;
    private String myNoteTime;
    private int flag;//区分是新建笔记还是编辑笔记

    private static final int cutTitleLength = 20;//截取的标题长度

    private ProgressDialog loadingDialog;
    private ProgressDialog insertDialog;
    private int screenWidth;
    private int screenHeight;
    private Disposable subsLoading;
    private Disposable subsInsert;
    private int isFinish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);

        initView();
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_new);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dealwithExit();
            }
        });
        //相关数据初始化
        groupDao = new GroupDao(this);
        noteDao = new NoteDao(this);
        note = new Note();
        //提示
        screenWidth = CommonUtil.getScreenWidth(this);
        screenHeight = CommonUtil.getScreenHeight(this);
        insertDialog = new ProgressDialog(this);
        insertDialog.setMessage("正在插入图片...");
        insertDialog.setCanceledOnTouchOutside(false);

        //紧急度选择控件初始化
        sp_group = (Spinner) findViewById(R.id.sp_group);
        final List<Group> groups = groupDao.queryGroupAll();
        //spinner
        MyGroupSpinnerAdapter adapter = new MyGroupSpinnerAdapter(this,groups);
        sp_group.setAdapter(adapter);
        sp_group.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mGroupId = groups.get(position).getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGroupId = 1;
            }
        });

        et_new_title = (EditText) findViewById(R.id.et_new_title);
        et_new_content = (RichTextEditor) findViewById(R.id.et_new_content);
        tv_new_time = (TextView) findViewById(R.id.tv_new_time);
        tv_new_location = (TextView) findViewById(R.id.tv_new_group);
        iv_finish = (ImageView) findViewById(R.id.iv_finish);
        tv_new_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation();
            }
        });
        openSoftKeyInput();//打开软键盘显示
        iv_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isFinish == 1){
                    iv_finish.setBackgroundResource(R.drawable.untick);
                    isFinish = 0;
                    note.setIsFinish(0);
                    NoteDao dao = new NoteDao(NewActivity.this);
                    dao.updateNote(note);
                }else{
                    iv_finish.setBackgroundResource(R.drawable.tick);
                    isFinish = 1;
                    note.setIsFinish(1);
                    NoteDao dao = new NoteDao(NewActivity.this);
                    dao.updateNote(note);
                }
            }
        });

        Intent intent = getIntent();
        flag = intent.getIntExtra("flag", 0);//0新建，1编辑
        if (flag == 1) {//编辑
            setTitle("编辑笔记");
            Bundle bundle = intent.getBundleExtra("data");
            note = (Note) bundle.getSerializable("note");

            if (note != null) {
                myTitle = note.getTitle();
                myContent = note.getContent();
                myNoteTime = TimeUtil.getTimeFormat(note.getCreateTime());
                Group group = groupDao.queryGroupById(note.getGroupId());
                if (group != null ) {
                    mOldGouupId = group.getId();
                    if(sp_group != null) {
                        sp_group.setSelection(groups.indexOf(group));
                    }
                }
                tv_new_location.setText(note.getLocation());
                loadingDialog = new ProgressDialog(this);
                loadingDialog.setMessage("数据加载中...");
                loadingDialog.setCanceledOnTouchOutside(false);
                loadingDialog.show();

                tv_new_time.setText(TimeUtil.getTimeFormat(note.getCreateTime()));
                et_new_title.setText(note.getTitle());
                et_new_content.post(new Runnable() {
                    @Override
                    public void run() {
                        dealWithContent();
                    }
                });
                isFinish = note.getIsFinish();
                if(isFinish == 1){
                    iv_finish.setBackgroundResource(R.drawable.tick);
                }else{
                    iv_finish.setBackgroundResource(R.drawable.untick);
                }
            }
        } else {
            //新建笔记时自动获取一次位置
            getLocation();
            setTitle("新建笔记");
            tv_new_location.setText(note.getLocation());
            mGroupId = getIntent().getIntExtra("groupId",1);
            if(sp_group != null) {
                Group tem = new Group();
                tem.setId(mGroupId);
                sp_group.setSelection(groups.indexOf(tem));
            }
            myNoteTime = TimeUtil.getTimeFormat(System.currentTimeMillis());
            tv_new_time.setText(myNoteTime);

            isFinish = 0;
            iv_finish.setBackgroundResource(R.drawable.untick);
        }



        et_new_title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if("3115005158".equals(s.toString())){
                    Intent intent = new Intent(NewActivity.this, AuthActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    private void dealWithContent() {
        //showEditData(note.getContent());
        et_new_content.clearAllLayout();
        showDataSync(note.getContent());

        // 图片删除事件
        et_new_content.setOnRtImageDeleteListener(new RichTextEditor.OnRtImageDeleteListener() {

            @Override
            public void onRtImageDelete(String imagePath) {
                if (!TextUtils.isEmpty(imagePath)) {
                    boolean isOK = SDCardUtil.deleteFile(imagePath);
                    if (isOK) {
                        showToast("删除成功：" + imagePath);
                    }
                }
            }
        });
        // 图片点击事件
        et_new_content.setOnRtImageClickListener(new RichTextEditor.OnRtImageClickListener() {
            @Override
            public void onRtImageClick(String imagePath) {
                myContent = getEditData();
                if (!TextUtils.isEmpty(myContent)) {
                    List<String> imageList = StringUtils.getTextFromHtml(myContent, true);
                    if (!TextUtils.isEmpty(imagePath)) {
                        int currentPosition = imageList.indexOf(imagePath);
                        showToast("点击图片：" + currentPosition + "：" + imagePath);
                    }
                }
            }
        });
    }

    /**
     * 关闭软键盘
     */
    private void closeSoftKeyInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        //boolean isOpen=imm.isActive();//isOpen若返回true，则表示输入法打开
        if (imm != null && imm.isActive() && getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
            //imm.hideSoftInputFromInputMethod();//据说无效
            //imm.hideSoftInputFromWindow(et_content.getWindowToken(), 0); //强制隐藏键盘
            //如果输入法在窗口上已经显示，则隐藏，反之则显示
            //imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * 打开软键盘
     */
    private void openSoftKeyInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        //boolean isOpen=imm.isActive();//isOpen若返回true，则表示输入法打开
        if (imm != null && !imm.isActive() && et_new_content != null) {
            et_new_content.requestFocus();
            //第二个参数可设置为0
            //imm.showSoftInput(et_content, InputMethodManager.SHOW_FORCED);//强制显示
            imm.showSoftInputFromInputMethod(et_new_content.getWindowToken(),
                    InputMethodManager.SHOW_FORCED);
        }
    }

    /**
     * 异步方式显示数据
     *
     * @param html
     */
    private void showDataSync(final String html) {
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
                        if (loadingDialog != null) {
                            loadingDialog.dismiss();
                        }
                        if (et_new_content != null) {
                            //在图片全部插入完毕后，再插入一个EditText，防止最后一张图片后无法插入文字
                            et_new_content.addEditTextAtIndex(et_new_content.getLastIndex(), "");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (loadingDialog != null) {
                            loadingDialog.dismiss();
                        }
                        showToast("解析错误：图片不存在或已损坏");
                    }

                    @Override
                    public void onSubscribe(Disposable d) {
                        subsLoading = d;
                    }

                    @Override
                    public void onNext(String text) {
                        if (et_new_content != null) {
                            if (text.contains("<img") && text.contains("src=")) {
                                //imagePath可能是本地路径，也可能是网络地址
                                String imagePath = StringUtils.getImgSrc(text);
                                //插入空的EditText，以便在图片前后插入文字
                                et_new_content.addEditTextAtIndex(et_new_content.getLastIndex(), "");
                                et_new_content.addImageViewAtIndex(et_new_content.getLastIndex(), imagePath);
                            } else {
                                et_new_content.addEditTextAtIndex(et_new_content.getLastIndex(), text);
                            }
                        }
                    }
                });
    }

    /**
     * 显示数据
     */
    protected void showEditData(ObservableEmitter<String> emitter, String html) {
        try {
            List<String> textList = StringUtils.cutStringByImgTag(html);
            for (int i = 0; i < textList.size(); i++) {
                String text = textList.get(i);
                emitter.onNext(text);
            }
            emitter.onComplete();
        } catch (Exception e) {
            e.printStackTrace();
            emitter.onError(e);
        }
    }

    /**
     * 负责处理编辑数据提交等事宜，请自行实现
     */
    private String getEditData() {
        List<RichTextEditor.EditData> editList = et_new_content.buildEditData();
        StringBuilder content = new StringBuilder();
        for (RichTextEditor.EditData itemData : editList) {
            if (itemData.inputStr != null) {
                content.append(itemData.inputStr);
            } else if (itemData.imagePath != null) {
                content.append("<img src=\"").append(itemData.imagePath).append("\"/>");
            }
        }
        return content.toString();
    }

    /**
     * 保存数据,=0销毁当前界面，=1不销毁界面，为了防止在后台时保存笔记并销毁，应该只保存笔记
     */
    private void saveNoteData(boolean isBackground) throws Exception {
        String noteTitle = et_new_title.getText().toString();
        String noteContent = getEditData();
        String location = tv_new_location.getText().toString();
        String noteTime = tv_new_time.getText().toString();

        Group group = groupDao.queryGroupById(mGroupId);
        if (group != null) {
            if (noteTitle.length() == 0) {//如果标题为空，则截取内容为标题
                if (noteContent.length() > cutTitleLength) {
                    noteTitle = noteContent.substring(0, cutTitleLength);
                } else if (noteContent.length() > 0) {
                    noteTitle = noteContent;
                }
            }
            int groupId = group.getId();
            note.setTitle(noteTitle);
            note.setContent(noteContent);
            note.setGroupId(groupId);
            note.setLocation(location);
            note.setIsFinish(isFinish);
            note.setBgColor("#FFFFFF");
            note.setUpdateTime(System.currentTimeMillis());
            if (flag == 0) {//新建笔记
                note.setCreateTime(System.currentTimeMillis());
                if (noteTitle.length() == 0 && noteContent.length() == 0) {
                    if (!isBackground) {
                        Toast.makeText(NewActivity.this, "请输入内容", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    long noteId = noteDao.insertNote(note);
                    //Log.i("", "noteId: "+noteId);
                    //查询新建笔记id，防止重复插入
                    note.setId((int) noteId);
                    flag = 1;//插入以后只能是编辑
                    if (!isBackground) {
                        Intent intent = new Intent();
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }
            } else if (flag == 1) {//编辑笔记
                if (!noteTitle.equals(myTitle) || !noteContent.equals(myContent)
                        || mOldGouupId != mGroupId
                        || !noteTime.equals(myNoteTime)) {
                    noteDao.updateNote(note);
                }
                if (!isBackground) {
                    finish();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_insert_image:
                closeSoftKeyInput();//关闭软键盘
                callGallery();
                break;
            case R.id.action_new_save:
                try {
                    saveNoteData(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 第一次调用时先检查权限
     *
     * @param context
     * @return
     */
    public boolean checkIOPermission(
            final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        //写权限
        if (Build.VERSION.SDK_INT > 22) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION);
            }
        }
        //读权限
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (Activity) context,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showAuthDialog("External storage", context,
                            Manifest.permission.READ_EXTERNAL_STORAGE);
                } else {
                    ActivityCompat
                            .requestPermissions(
                                    (Activity) context,
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                    REQUEST_CODE_CHOOSE);
                }
                return false;
            } else {
                return true;
            }

        } else {
            return true;
        }
    }

    /**
     * 检查定位权限
     * PS：这里没有调用到gps权限 只是名字用gps
     * @param context
     * @return
     */
    public boolean checkGPSPermission(
            final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        //读权限
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (Activity) context,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                    showAuthDialog("External storage", context,
                            Manifest.permission.ACCESS_FINE_LOCATION);
                } else {
                    ActivityCompat
                            .requestPermissions(
                                    (Activity) context,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    GPS_PERMISSION);
                }
                return false;
            } else {
                return true;
            }

        } else {
            return true;
        }
    }

    /**
     * 调用图库选择
     */
    private void callGallery() {
//        //调用系统图库
//        Intent intent = plus Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");// 相片类型
//        startActivityForResult(intent, 1);


        if (checkIOPermission(this)) {
            Matisse.from(this)
                    .choose(MimeType.of(MimeType.JPEG, MimeType.PNG, MimeType.GIF))//照片视频全部显示MimeType.allOf()
                    .countable(true)//true:选中后显示数字;false:选中后显示对号
                    .maxSelectable(3)//最大选择数量为9
                    //.addFilter(plus GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                    .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))//图片显示表格的大小
                    .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)//图像选择和预览活动所需的方向
                    .thumbnailScale(0.85f)//缩放比例
                    .theme(R.style.Matisse_Zhihu)//主题  暗色主题 R.style.Matisse_Dracula
                    .imageEngine(new MyGlideEngine())//图片加载方式，Glide4需要自定义实现
                    .capture(true) //是否提供拍照功能，兼容7.0系统需要下面的配置
                    //参数1 true表示拍照存储在共有目录，false表示存储在私有目录；参数2与 AndroidManifest中authorities值相同，用于适配7.0系统 必须设置
                    .captureStrategy(new CaptureStrategy(true, "com.sendtion.matisse.fileprovider"))//存储到哪里
                    .forResult(REQUEST_CODE_CHOOSE);//请求码
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (data != null) {
                if (requestCode == 1) {
                    //处理调用系统图库
                } else if (requestCode == REQUEST_CODE_CHOOSE) {
                    //异步方式插入图片
                    insertImagesSync(data);
                }
            }
        }
    }

    /**
     * 异步方式插入图片
     *
     * @param data
     */
    private void insertImagesSync(final Intent data) {
        insertDialog.show();

        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) {
                try {
                    et_new_content.measure(0, 0);
                    List<Uri> mSelected = Matisse.obtainResult(data);
                    // 可以同时插入多张图片
                    for (Uri imageUri : mSelected) {
                        String imagePath = SDCardUtil.getFilePathFromUri(NewActivity.this, imageUri);
                        //LogUtil.e(TAG, "###path=" + imagePath);
                        Bitmap bitmap = ImageUtils.getSmallBitmap(imagePath, screenWidth, screenHeight);//压缩图片
                        //bitmap = BitmapFactory.decodeFile(imagePath);
                        imagePath = SDCardUtil.saveToSdCard(bitmap);
                        //LogUtil.e(TAG, "###imagePath="+imagePath);
                        emitter.onNext(imagePath);
                    }

                    // 测试插入网络图片 http://p695w3yko.bkt.clouddn.com/18-5-5/44849367.jpg
                    //subscriber.onNext("http://p695w3yko.bkt.clouddn.com/18-5-5/30271511.jpg");

                    emitter.onComplete();
                } catch (Exception e) {
                    e.printStackTrace();
                    emitter.onError(e);
                }
            }
        })
                //.onBackpressureBuffer()
                .subscribeOn(Schedulers.io())//生产事件在io
                .observeOn(AndroidSchedulers.mainThread())//消费事件在UI线程
                .subscribe(new Observer<String>() {
                    @Override
                    public void onComplete() {
                        if (insertDialog != null && insertDialog.isShowing()) {
                            insertDialog.dismiss();
                        }
                        showToast("图片插入成功");
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (insertDialog != null && insertDialog.isShowing()) {
                            insertDialog.dismiss();
                        }
                        showToast("图片插入失败:" + e.getMessage());
                    }

                    @Override
                    public void onSubscribe(Disposable d) {
                        subsInsert = d;
                    }

                    @Override
                    public void onNext(String imagePath) {
                        et_new_content.insertImage(imagePath, et_new_content.getMeasuredWidth());
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case WRITE_PERMISSION: {
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    if (!(grantResults.length > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                        LogUtil.e( "Permission denied to access your location.222");
                    }
                    LogUtil.e("Permission denied to access your location.");
                } else {
                    callGallery();
                }
            }break;
            case GPS_PERMISSION: {
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    if (!(grantResults.length > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                        LogUtil.e( "Permission denied to access your location.222");
                    }
                    LogUtil.e("Permission denied to access your location.");
                } else {
                    getLocation();
                }
            }break;
            default:break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            //如果APP处于后台，或者手机锁屏，则保存数据
            if (CommonUtil.isAppOnBackground(getApplicationContext()) ||
                    CommonUtil.isLockScreeen(getApplicationContext())) {
                saveNoteData(true);//处于后台时保存数据
            }

            if (subsLoading != null && subsLoading.isDisposed()) {
                subsLoading.dispose();
            }
            if (subsInsert != null && subsInsert.isDisposed()) {
                subsInsert.dispose();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 退出处理
     */
    private void dealwithExit() {
        try {
            String noteTitle = et_new_title.getText().toString();
            String noteContent = getEditData();
            String groupName = tv_new_location.getText().toString();
            String noteTime = tv_new_time.getText().toString();
            if (flag == 0) {//新建笔记
                if (noteTitle.length() > 0 || noteContent.length() > 0) {
                    saveNoteData(false);
                }
            } else if (flag == 1) {//编辑笔记
                if (!noteTitle.equals(myTitle) || !noteContent.equals(myContent)
                         || !noteTime.equals(myNoteTime)) {
                    saveNoteData(false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        dealwithExit();
    }

    public void getLocation() {
        if(checkGPSPermission(this)){
            Location location = LocationUtil.getInstance(NewActivity.this).showLocation();
            if (location != null) {
                String address = "纬度：" + location.getLatitude() + "经度：" + location.getLongitude();
                Log.d("FLY.LocationUtils", address);
                queryLocationString(location);
            }
        }
    }

    private void queryLocationString(final Location location) {
        if (location == null) {
            setLocationText("");
            return;
        }
        //第二步 查询该位置对应的文字
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .get()
                .url("http://api.map.baidu.com/geocoder/v2/?&location=" + location.getLatitude() + "," + location.getLongitude() + "&output=json&pois=1&latest_admin=1&ak=GXMygKtAWhPMrgQrKDNDrYjM9e1xlhI3")
                .build();
        Call call = client.newCall(request);

        //异步调用,并设置回调函数
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(NewActivity.this, "位置查询错误", Toast.LENGTH_SHORT).show();
                        setLocationText("");
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String res = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (MyApplication.isTest) {
//                            Toast.makeText(NewActivity.this, "get location successful:" + res, Toast.LENGTH_LONG).show();
                        }
                        try {
                            JSONObject result = new JSONObject(res);
                            if (result.getInt("status") == 0) {
                                JSONObject location = result.getJSONObject("result");
                                setLocationText(location.getString("formatted_address"));
                            } else {
                                Toast.makeText(NewActivity.this, "位置查询错误", Toast.LENGTH_SHORT).show();
                                setLocationText("");
                            }
                        } catch (JSONException e) {
                            setLocationText("");
                            Toast.makeText(NewActivity.this, "获取位置错误", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private void setLocationText(final String location) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (tv_new_location != null) {
                    tv_new_location.setText(location);
                }
            }
        });
    }

    //关闭时解除监听器
    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocationUtil.getInstance(this).removeLocationUpdatesListener();
    }
}
