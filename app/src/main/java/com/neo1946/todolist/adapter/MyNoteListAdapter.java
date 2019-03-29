package com.neo1946.todolist.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.neo1946.todolist.R;
import com.neo1946.todolist.bean.Note;
import com.neo1946.todolist.db.NoteDao;
import com.neo1946.todolist.util.ColorUtil;
import com.neo1946.todolist.util.TimeUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 列表Adapter
 */

public class MyNoteListAdapter extends RecyclerView.Adapter<MyNoteListAdapter.ViewHolder>
        implements View.OnClickListener, View.OnLongClickListener {
    private Context mContext;
    private List<Note> mNotes;
    private OnRecyclerViewItemClickListener mOnItemClickListener ;
    private OnRecyclerViewItemLongClickListener mOnItemLongClickListener ;

    public MyNoteListAdapter() {
        mNotes = new ArrayList<>();
    }

    public void setmNotes(List<Note> notes) {
        this.mNotes = notes;
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            //注意这里使用getTag方法获取数据
            mOnItemClickListener.onItemClick(v,(Note)v.getTag());
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (mOnItemLongClickListener != null) {
            //注意这里使用getTag方法获取数据
            mOnItemLongClickListener.onItemLongClick(v,(Note)v.getTag());
        }
        return true;
    }

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, Note note);
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public interface OnRecyclerViewItemLongClickListener {
        void onItemLongClick(View view, Note note);
    }

    public void setOnItemLongClickListener(OnRecyclerViewItemLongClickListener listener) {
        this.mOnItemLongClickListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Log.i(TAG, "###onCreateViewHolder: ");
        //inflate(R.layout.list_item_record,parent,false) 如果不这么写，cardview不能适应宽度
        mContext = parent.getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_note,parent,false);
        //将创建的View注册点击事件
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        //Log.i(TAG, "###onBindViewHolder: ");
        final Note note = mNotes.get(position);
        //将数据保存在itemView的Tag中，以便点击时进行获取
        holder.itemView.setTag(note);
        ColorUtil.setViewColor(holder.v_color,note.getGroupId());
        //LogUtil.e("adapter", "###record="+record);
        holder.tv_list_title.setText(note.getTitle());
        holder.tv_list_summary.setText(note.getContent());
        holder.tv_list_time.setText(TimeUtil.getTimeFormat(note.getCreateTime()));
        holder.tv_list_group.setText(note.getLocation());
        holder.isFinish = note.getIsFinish();
        if(holder.isFinish == 1){
            holder.iv_finish.setBackgroundResource(R.drawable.tick);
        }else{
            holder.iv_finish.setBackgroundResource(R.drawable.untick);
        }
        holder.iv_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.isFinish == 1){
                    holder.iv_finish.setBackgroundResource(R.drawable.untick);
                    holder.isFinish = 0;
                    note.setIsFinish(0);
                    NoteDao dao = new NoteDao(mContext);
                    dao.updateNote(note);
                }else{
                    holder.iv_finish.setBackgroundResource(R.drawable.tick);
                    holder.isFinish = 1;
                    note.setIsFinish(1);
                    NoteDao dao = new NoteDao(mContext);
                    dao.updateNote(note);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mNotes != null && mNotes.size()>0){
            return mNotes.size();
        }
        return 0;
    }

    //自定义的ViewHolder，持有每个Item的的所有界面元素
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_list_title;//笔记标题
        public TextView tv_list_summary;//笔记摘要
        public TextView tv_list_time;//创建时间
        public TextView tv_list_group;//笔记分类
        public CardView card_view_note;
        public View v_color;
        public ImageView iv_finish;
        public int isFinish = 0;

        public ViewHolder(View view){
            super(view);
            card_view_note = (CardView) view.findViewById(R.id.card_view_note);
            iv_finish = (ImageView) view.findViewById(R.id.iv_finish);
            v_color = (View) view.findViewById(R.id.v_color);
            tv_list_title = (TextView) view.findViewById(R.id.tv_list_title);
            tv_list_summary = (TextView) view.findViewById(R.id.tv_list_summary);
            tv_list_time = (TextView) view.findViewById(R.id.tv_list_time);
            tv_list_group = (TextView) view.findViewById(R.id.tv_list_group);
        }
    }
}
