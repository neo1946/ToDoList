package com.neo1946.todolist.ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.neo1946.todolist.R;
import com.neo1946.todolist.adapter.MyNoteListAdapter;
import com.neo1946.todolist.bean.Note;
import com.neo1946.todolist.db.NoteDao;
import com.neo1946.todolist.view.SpacesItemDecoration;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.neo1946.todolist.MyApplication.SP_NAME;

/**
 * @author ouyangzhaoxian on 2019/03/28
 */
public class NoteFragment extends Fragment {

    public final static int REFRESH_TYPE_EMERGENCY = 1;
    public final static int REFRESH_TYPE_CREATE_TIME = 2;
    public final static int REFRESH_TYPE_UPDATE_TIME = 3;
    public final static int REFRESH_TYPE_FINISH = 4;
    private int mRefreshMode = REFRESH_TYPE_EMERGENCY;

    private View root = null;
    private RecyclerView rv_list_main;
    private MyNoteListAdapter mNoteListAdapter;
    private FloatingActionsMenu mFAB;

    private List<Note> noteList;
    private NoteDao noteDao;
    private SharedPreferences sharedPreferences;
    protected BaseActivity mActivity;
    protected Context mContext;

    @Override
    public void onAttach(Context context) {
        mActivity = (BaseActivity) context;
        mContext = context;
        super.onAttach(context);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshNoteList();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        saveRefreshMode();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_note, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getRefreshMode();
        noteDao = new NoteDao(mActivity);
        rv_list_main = (RecyclerView) root.findViewById(R.id.rv_list_main);
        rv_list_main.addItemDecoration(new SpacesItemDecoration(0));//设置item间距
        LinearLayoutManager layoutManager = new LinearLayoutManager(mActivity);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);//竖向列表
        rv_list_main.setLayoutManager(layoutManager);

        mNoteListAdapter = new MyNoteListAdapter();
        mNoteListAdapter.setmNotes(noteList);
        rv_list_main.setAdapter(mNoteListAdapter);

        mNoteListAdapter.setOnItemClickListener(new MyNoteListAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, Note note) {
                Intent intent = new Intent(mActivity, NoteActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("note", note);
                intent.putExtra("data", bundle);
                startActivity(intent);
            }
        });
        mNoteListAdapter.setOnItemLongClickListener(new MyNoteListAdapter.OnRecyclerViewItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, final Note note) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setTitle("提示");
                builder.setMessage("确定删除笔记？");
                builder.setCancelable(false);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int ret = noteDao.deleteNote(note.getId());
                        if (ret > 0) {
                            mActivity.showToast("删除成功");
                            //TODO 删除笔记成功后，记得删除图片（分为本地图片和网络图片）
                            //获取笔记中图片的列表 StringUtils.getTextFromHtml(note.getContent(), true);
                            refreshNoteList();
                        }
                    }
                });
                builder.setNegativeButton("取消", null);
                builder.create().show();
            }
        });

        //悬浮球
        mFAB = root.findViewById(R.id.fab);

        root.findViewById(R.id.fab_action1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startNewNodeWithGroup(1);
                mFAB.collapse();
            }
        });

        root.findViewById(R.id.fab_action2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startNewNodeWithGroup(2);
                mFAB.collapse();
            }
        });

        root.findViewById(R.id.fab_action3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startNewNodeWithGroup(3);
                mFAB.collapse();
            }
        });

        root.findViewById(R.id.fab_action4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startNewNodeWithGroup(4);
                mFAB.collapse();
            }
        });
    }



    //刷新笔记列表
    public void refreshNoteList() {
        if (noteDao == null) {
            noteDao = new NoteDao(mActivity);
        }
        switch (mRefreshMode) {
            case REFRESH_TYPE_EMERGENCY:
                noteList = noteDao.queryNotesByGroup();
                break;
            case REFRESH_TYPE_CREATE_TIME:
                noteList = noteDao.queryNotesByCreateTime();
                break;
            case REFRESH_TYPE_UPDATE_TIME:
                noteList = noteDao.queryNotesByUpadteTime();
                break;
        }
        mNoteListAdapter.setmNotes(noteList);
        mNoteListAdapter.notifyDataSetChanged();
    }


    private void getRefreshMode() {
        if (sharedPreferences == null) {
            sharedPreferences = mActivity.getSharedPreferences(SP_NAME, MODE_PRIVATE);
        }
        mRefreshMode = sharedPreferences.getInt("refreshMode", REFRESH_TYPE_EMERGENCY);
    }

    private void saveRefreshMode() {
        if (sharedPreferences == null) {
            sharedPreferences = mActivity.getSharedPreferences(SP_NAME, MODE_PRIVATE);
        }
        sharedPreferences.edit().putInt("refreshMode", mRefreshMode).apply();
    }


    private void startNewNodeWithGroup(int groupId) {
        Intent intent = new Intent(mActivity, NewActivity.class);
        intent.putExtra("groupId", groupId);
        intent.putExtra("flag", 0);
        startActivity(intent);
    }

    public void changeModeAndRefreshNoteList(int mode) {
        mRefreshMode = mode;
        if (noteDao == null) {
            noteDao = new NoteDao(mActivity);
        }
        switch (mRefreshMode) {
            case REFRESH_TYPE_EMERGENCY:
                noteList = noteDao.queryNotesByGroup();
                break;
            case REFRESH_TYPE_CREATE_TIME:
                noteList = noteDao.queryNotesByCreateTime();
                break;
            case REFRESH_TYPE_UPDATE_TIME:
                noteList = noteDao.queryNotesByUpadteTime();
                break;
            case REFRESH_TYPE_FINISH:
                noteList = noteDao.queryNotesByFinish();
                break;
        }
        mNoteListAdapter.setmNotes(noteList);
        mNoteListAdapter.notifyDataSetChanged();
    }

}
