package com.neo1946.todolist.ui;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.avos.avoscloud.AVUser;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.neo1946.todolist.R;
import com.neo1946.todolist.adapter.MyNoteListAdapter;
import com.neo1946.todolist.adapter.MyPagerAdapter;
import com.neo1946.todolist.bean.Note;
import com.neo1946.todolist.db.NoteDao;
import com.neo1946.todolist.util.CouldUploadUtil;
import com.neo1946.todolist.util.CouldUtil;
import com.neo1946.todolist.util.FingerPrintUtil;
import com.neo1946.todolist.util.LogUtil;
import com.neo1946.todolist.util.ThemeManagerUtil;
import com.neo1946.todolist.view.MyDialog;
import com.neo1946.todolist.view.SpacesItemDecoration;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import static com.neo1946.todolist.MyApplication.SP_NAME;
import static com.neo1946.todolist.ui.NoteFragment.REFRESH_TYPE_CREATE_TIME;
import static com.neo1946.todolist.ui.NoteFragment.REFRESH_TYPE_EMERGENCY;
import static com.neo1946.todolist.ui.NoteFragment.REFRESH_TYPE_FINISH;
import static com.neo1946.todolist.ui.NoteFragment.REFRESH_TYPE_UPDATE_TIME;
import static com.neo1946.todolist.util.FingerPrintUtil.FINGER_AUTH_SP_NAME;

/**
 * @author ouyangzhaoxian on 2019/3/11.
 * 主界面
 */

public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";


    private TextView tv_account;
    private SharedPreferences sharedPreferences;
    private LinearLayout ll_sync;
    private CouldUtil couldUtil;
    private DrawerLayout drawerLayout;
    private MyDialog dialog;
    private StringBuilder stringBuilder;
    private static boolean firstStart = true;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private MyPagerAdapter mFragmentPagerAdapter;
    private FragmentManager mFragmentManager;
    private NoteFragment noteFragment;
    private BillFragment billFragment;

    private String[] titles = new String[]{"最新", "热门"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initAuth();
        initView();
        initLogin();
    }


    private void initView() {
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayShowTitleEnabled(false);
        tv_account = findViewById(R.id.tv_account);


        tabLayout = findViewById(R.id.tablayout);
        viewPager = findViewById(R.id.vp_main);
        noteFragment = new NoteFragment();
        billFragment = new BillFragment();
        mFragmentManager = getSupportFragmentManager();
        mFragmentPagerAdapter = new MyPagerAdapter(mFragmentManager);
        mFragmentPagerAdapter.addFragment(noteFragment, "笔记");
        mFragmentPagerAdapter.addFragment(billFragment, "账单");
        viewPager.setAdapter(mFragmentPagerAdapter);

        //初始化TabLayout
        tabLayout.addTab(tabLayout.newTab().setText("笔记"));
        tabLayout.addTab(tabLayout.newTab().setText("账单"));
        tabLayout.setupWithViewPager(viewPager);

        //侧滑栏
        drawerLayout = (DrawerLayout) findViewById(R.id.dl_main);

        findViewById(R.id.ll_setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
                drawerLayout.closeDrawers();
            }
        });
        findViewById(R.id.ll_account).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLogin()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("提示");
                    builder.setMessage("确定退出登录？");
                    builder.setCancelable(false);
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sharedPreferences.edit().putBoolean("isLogin", false).apply();
                            if (tv_account != null) {
                                tv_account.setText("登录");
                            }
                            AVUser.logOut();
                        }
                    });
                    builder.setNegativeButton("取消", null);
                    builder.create().show();
                } else {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivityForResult(intent, 10);
                }
                drawerLayout.closeDrawers();
            }
        });
        findViewById(R.id.ll_emergency).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteFragment.changeModeAndRefreshNoteList(REFRESH_TYPE_EMERGENCY);
                drawerLayout.closeDrawers();
            }
        });
        findViewById(R.id.ll_update_time).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteFragment.changeModeAndRefreshNoteList(REFRESH_TYPE_UPDATE_TIME);
                drawerLayout.closeDrawers();
            }
        });
        findViewById(R.id.ll_create_time).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteFragment.changeModeAndRefreshNoteList(REFRESH_TYPE_CREATE_TIME);
                drawerLayout.closeDrawers();
            }
        });
        findViewById(R.id.ll_finish).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteFragment.changeModeAndRefreshNoteList(REFRESH_TYPE_FINISH);
                drawerLayout.closeDrawers();
            }
        });
        ll_sync = findViewById(R.id.ll_sync);
        ll_sync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSync();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_main, menu);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new_note:
                Toast.makeText(this, "开始游戏", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void changeTheme() {

        /**
         * 显示修改主题色 Dialog
         */
        final String[] themes = ThemeManagerUtil.getInstance().getThemes();
        new MaterialDialog.Builder(this)
                .title("选择主题")
                .titleGravity(GravityEnum.CENTER)
                .items(themes)
                .negativeText("取消")
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                        ThemeManagerUtil.getInstance().setTheme(MainActivity.this, themes[position]);
                    }
                })
                .show();
    }


    private void initAuth() {
        if (sharedPreferences == null) {
            sharedPreferences = getSharedPreferences(SP_NAME, MODE_PRIVATE);
        }
        boolean isFirstStart = sharedPreferences.getBoolean("isFirstStart", true);
        sharedPreferences.edit().putBoolean("isFirstStart", false).apply();
        if (isFirstStart && FingerPrintUtil.supportFingerprint(this)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("提示");
            builder.setMessage("本设备支持指纹识别，是否在app启动时使用指纹验证");
            builder.setCancelable(false);
            builder.setPositiveButton("验证并启用", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivityForResult(new Intent(MainActivity.this, AuthActivity.class), 1);
                }
            });
            builder.setNegativeButton("取消", null);
            builder.create().show();
        }
        boolean hasStartAuth = sharedPreferences.getBoolean(FINGER_AUTH_SP_NAME, false);
        if (hasStartAuth && firstStart) {
            startActivityForResult(new Intent(MainActivity.this, AuthActivity.class), 2);
        }
    }

    @Subscribe
    public void onSyncFinish(CouldUtil.SyncFinishEvent event) {
        drawerLayout.closeDrawers();
        noteFragment.refreshNoteList();
        if (event.finish) {
            Toast.makeText(this, "同步成功", Toast.LENGTH_SHORT).show();
            if (dialog != null) {
                dialog.setFinish(true);
                drawerLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                }, 3000);
            }
        } else {
            Toast.makeText(this, "同步失败", Toast.LENGTH_SHORT).show();
            if (dialog != null) {
                dialog.setFinish(true);
                drawerLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                }, 3000);
            }
        }
    }


    @Subscribe
    public void onSyncProgress(CouldUtil.SyncProgressEvent event) {
        if (dialog != null) {
            dialog.setMessage(stringBuilder.append("\n" + event.message).toString());
        }
    }

    private void startSync() {
        setUpDialog("同步中", "开始同步");
        couldUtil.sync();
    }

    private void setUpDialog(String title, String message) {
        dialog = new MyDialog(this);
        dialog.setTitle(title);//设置标题
        dialog.setMessage(message);
        dialog.setCancelable(false);//设置进度条是否可以按退回键取消 ;
        dialog.show();

        stringBuilder = new StringBuilder(message);
    }

    private void startDownload() {
        setUpDialog("下载中", "开始下载");
        couldUtil.downloadOnly();
    }

    private void startUpload() {
        setUpDialog("上传中", "开始上传");
        couldUtil.uploadOnly();
    }

    private void initLogin() {
        if (couldUtil == null) {
            couldUtil = new CouldUtil(this);
        }
        if (isLogin() && tv_account != null) {
            tv_account.setText(sharedPreferences.getString("userName", "登录"));
            ll_sync.setVisibility(View.VISIBLE);
        } else {
            if (tv_account != null) {
                tv_account.setText("登录");
            }
            ll_sync.setVisibility(View.GONE);
        }
    }

    private boolean isLogin() {
        if (sharedPreferences == null) {
            sharedPreferences = getSharedPreferences(SP_NAME, MODE_PRIVATE);
        }
        if (sharedPreferences.getBoolean("isLogin", false)) {
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (sharedPreferences == null) {
            sharedPreferences = getSharedPreferences(SP_NAME, MODE_PRIVATE);
        }
        if (requestCode == 1) {
            //验证并启用
            if (resultCode == 1) {
                Toast.makeText(this, "验证成功 已启用", Toast.LENGTH_SHORT).show();
                sharedPreferences.edit().putBoolean(FINGER_AUTH_SP_NAME, true).apply();
            } else {
                Toast.makeText(this, "验证失败，可到设置中重新开启", Toast.LENGTH_SHORT).show();
                sharedPreferences.edit().putBoolean(FINGER_AUTH_SP_NAME, false).apply();
            }
        } else if (requestCode == 2) {
            //验证结果
            if (resultCode != 1) {
                finish();
                firstStart = false;
            }
        }
        //从登录界面返回刷新一下登录数据
        if (AVUser.getCurrentUser() != null && tv_account != null) {
            tv_account.setText(AVUser.getCurrentUser().getUsername());
            sharedPreferences.edit().putBoolean("isLogin", true).apply();
            sharedPreferences.edit().putString("userName", AVUser.getCurrentUser().getUsername()).apply();
        }
        initLogin();
    }
}
