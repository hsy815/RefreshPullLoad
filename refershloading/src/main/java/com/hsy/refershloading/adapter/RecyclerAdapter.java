package com.hsy.refershloading.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hsy.refershloading.R;
import com.hsy.refershloading.util.MyViewHolder;

import java.util.List;

/**
 * Created by hsy on 16/9/28.
 */

public class RecyclerAdapter extends RecyclerView.Adapter {
    private Context mContext;
    private LayoutInflater inflater;
    private List<String> stringList;
    private MyViewHolder.OnItemClickListener mOnItemClickListener;
    private MyViewHolder.OnItemLongClickListener mOnItemLongClickListener;

    public RecyclerAdapter(Context context, List<String> stringList) {
        this.mContext = context;
        this.stringList = stringList;
        inflater = LayoutInflater.from(mContext);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_recycler, parent, false);
        return new ViewHolder(view, mOnItemClickListener, mOnItemLongClickListener);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.textview.setText(stringList.get(position));
    }

    @Override
    public int getItemCount() {
            return stringList.size();
    }

    private class ViewHolder extends MyViewHolder {
        private TextView textview;

        public ViewHolder(View itemView, MyViewHolder.OnItemClickListener listener1, MyViewHolder.OnItemLongClickListener listener2) {
            super(itemView, listener1, listener2);
            textview = (TextView) itemView.findViewById(R.id.textview);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }
    }

    public void setOnItemClickListener(MyViewHolder.OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void setOnItemLongClickListener(MyViewHolder.OnItemLongClickListener listener) {
        mOnItemLongClickListener = listener;
    }

}
