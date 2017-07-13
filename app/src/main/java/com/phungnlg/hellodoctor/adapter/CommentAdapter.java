package com.phungnlg.hellodoctor.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.phungnlg.hellodoctor.CommentItem;
import com.phungnlg.hellodoctor.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by Phil on 7/13/2017.
 */

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private LinkedHashMap<String, CommentItem> listComment;
    private ArrayList<String> key;
    private ArrayList<CommentItem> value;

    public CommentAdapter(LinkedHashMap<String, CommentItem> listComment) {
        this.listComment = listComment;
        if (listComment != null) {
            key = new ArrayList<>(listComment.keySet());
            value = new ArrayList<>(listComment.values());
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setName(value.get(position).getName());
        holder.setTime(value.get(position).getTime());
        holder.setContent(value.get(position).getComment());
    }

    @Override
    public int getItemCount() {
        return (listComment != null) ? listComment.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private View mView;
        private TextView name;
        private TextView time;
        private TextView body;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            name = (TextView) mView.findViewById(R.id.item_comment_tv_name);
            time = (TextView) mView.findViewById(R.id.item_comment_tv_time);
            body = (TextView) mView.findViewById(R.id.item_comment_tv_content);
        }

        public void setName(String _name) {
            name.setText(_name);
        }

        public void setTime(String _time) {
            time.setText(_time);
        }

        public void setContent(String _title) {
            body.setText(_title);
        }
    }
}
