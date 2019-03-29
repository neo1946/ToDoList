package com.neo1946.todolist.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.neo1946.todolist.R;
import com.neo1946.todolist.bean.BBill;
import com.neo1946.todolist.bean.MonthListBean;
import com.neo1946.todolist.util.ImageUtils;
import com.neo1946.todolist.util.TimeUtil;
import com.neo1946.todolist.view.SwipeMenuView;

import java.util.List;

import static com.neo1946.todolist.util.TimeUtil.FORMAT_HMS_CN;
import static com.neo1946.todolist.util.TimeUtil.FORMAT_YMD_CN;

/**
 *  @author ouyangzhaoxian on 2019/03/12
 * 悬浮头部项
 * 可侧滑编辑、删除
 */
public class MonthListAdapter extends StickyHeaderGridAdapter {

    private Context mContext;

    private OnStickyHeaderClickListener onStickyHeaderClickListener;

    private List<MonthListBean.DaylistBean> mDatas;

    public MonthListAdapter(Context context, List<MonthListBean.DaylistBean> datas) {
        this.mContext = context;
        this.mDatas = datas;
    }

    public void setmDatas(List<MonthListBean.DaylistBean> mDatas) {
        this.mDatas = mDatas;
    }

    public void setOnStickyHeaderClickListener(OnStickyHeaderClickListener listener) {
        if (onStickyHeaderClickListener == null)
            this.onStickyHeaderClickListener = listener;
    }

    public void remove(int section, int offset) {
        mDatas.get(section).getList().remove(offset);
        notifySectionItemRemoved(section, offset);
    }

    public void clear() {
        this.mDatas = null;
        notifyAllSectionsDataSetChanged();
    }

    @Override
    public int getSectionCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    @Override
    public int getSectionItemCount(int section) {
        return (mDatas == null || mDatas.get(section).getList() == null) ? 0 : mDatas.get(section).getList().size();
    }

    @Override
    public HeaderViewHolder onCreateHeaderViewHolder(ViewGroup parent, int headerType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_monthlist_header, parent, false);
        return new MyHeaderViewHolder(view);
    }

    @Override
    public ItemViewHolder onCreateItemViewHolder(ViewGroup parent, int itemType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_monthlist_item, parent, false);
        return new MyItemViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(HeaderViewHolder viewHolder, int section) {
        final MyHeaderViewHolder holder = (MyHeaderViewHolder) viewHolder;
        holder.header_date.setText(mDatas.get(section).getTime());
        holder.header_money.setText(mDatas.get(section).getMoney());
    }

    @Override
    public void onBindItemViewHolder(ItemViewHolder viewHolder, final int section, final int position) {
        final MyItemViewHolder holder = (MyItemViewHolder) viewHolder;

        final BBill bBill = mDatas.get(section).getList().get(position);
        holder.item_title.setText(bBill.getSortName());
        holder.item_img.setImageDrawable(ImageUtils.getDrawable(bBill.getSortImg()));
        if (bBill.isIncome()) {
            holder.item_money.setText("+" + bBill.getCost());
        } else {
            holder.item_money.setText("-" + bBill.getCost());
        }

        //监听侧滑删除事件
        holder.item_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                final int section1 = getAdapterPositionSection(holder.getAdapterPosition());
                final int offset1 = getItemSectionOffset(section1, holder.getAdapterPosition());
                new AlertDialog.Builder(mContext).setTitle("是否删除此条记录")
                        .setNegativeButton("取消", null)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                onStickyHeaderClickListener
                                        .OnDeleteClick(mDatas.get(section1).getList().get(offset1), section1, offset1);
                            }
                        })
                        .show();
            }
        });
        //监听侧滑编辑事件
        holder.item_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int section1 = getAdapterPositionSection(holder.getAdapterPosition());
                final int offset1 = getItemSectionOffset(section1, holder.getAdapterPosition());
                onStickyHeaderClickListener.OnEditClick(
                        mDatas.get(section1).getList().get(offset1), section1, offset1);
            }
        });
        //监听单击显示详情事件
        holder.item_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(mContext)
                        .title(bBill.getSortName())
                        .content("\t\t" + Math.abs(bBill.getCost()) + "元\n\t\t" + bBill.getContent()
                                +"\n\n\t\t"+ TimeUtil.long2Str(bBill.getCrdate(), FORMAT_YMD_CN)
                                +"\n\t\t"+TimeUtil.long2Str(bBill.getCrdate(), FORMAT_HMS_CN))
                        .positiveText("朕知道了")
                        .icon(ImageUtils.getDrawable(bBill.getSortImg()))
                        .limitIconToDefaultSize()
                        .show();
            }
        });
    }

    /**
     * 自定义编辑、删除接口
     */
    public interface OnStickyHeaderClickListener {
        void OnDeleteClick(BBill item, int section, int offset);

        void OnEditClick(BBill item, int section, int offset);
    }

    public static class MyHeaderViewHolder extends HeaderViewHolder {
        TextView header_date;
        TextView header_money;

        MyHeaderViewHolder(View itemView) {
            super(itemView);
            header_date = itemView.findViewById(R.id.header_date);
            header_money = itemView.findViewById(R.id.header_money);
        }
    }

    public static class MyItemViewHolder extends ItemViewHolder {
        TextView item_title;
        TextView item_money;
        Button item_delete;
        Button item_edit;
        ImageView item_img;
        RelativeLayout item_layout;
        SwipeMenuView mSwipeMenuView;

        MyItemViewHolder(View itemView) {
            super(itemView);
            item_title = itemView.findViewById(R.id.item_title);
            item_money = itemView.findViewById(R.id.item_money);
            item_delete = itemView.findViewById(R.id.item_delete);
            item_edit = itemView.findViewById(R.id.item_edit);
            item_img = itemView.findViewById(R.id.item_img);
            item_layout = itemView.findViewById(R.id.item_layout);
            mSwipeMenuView = itemView.findViewById(R.id.swipe_menu);
        }
    }
}
