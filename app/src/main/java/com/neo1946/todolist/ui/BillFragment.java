package com.neo1946.todolist.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.neo1946.todolist.R;

/**
 * @author ouyangzhaoxian on 2019/03/28
 */
public class BillFragment extends Fragment {

    private View root = null;
    private FloatingActionsMenu mFAB;
    private RecyclerView rv_list;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_bill, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rv_list = root.findViewById(R.id.rv_list);
        //悬浮球
        mFAB = root.findViewById(R.id.fab);

        root.findViewById(R.id.fab_action1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFAB.collapse();
            }
        });

        root.findViewById(R.id.fab_action2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFAB.collapse();
            }
        });

        root.findViewById(R.id.fab_action3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFAB.collapse();
            }
        });

        root.findViewById(R.id.fab_action4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFAB.collapse();
            }
        });
    }
}
