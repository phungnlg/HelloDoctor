package com.phungnlg.hellodoctor.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.phungnlg.hellodoctor.NotificationItem;
import com.phungnlg.hellodoctor.R;

import java.util.ArrayList;

/**
 * Created by Phil on 7/11/2017.
 */

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private ArrayList<NotificationItem> listNotification;

    public NotificationAdapter(ArrayList<NotificationItem> listNotification) {
        this.listNotification = listNotification;
    }

    @Override
    public NotificationAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_notification2, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NotificationAdapter.ViewHolder holder, int position) {
        holder.setBody(listNotification.get(position).getNotification());
        holder.setTime(listNotification.get(position).getTime());
    }

    @Override
    public int getItemCount() {
        return listNotification.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private View mView;
        private ImageButton btnDelete;
        private TextView noti;

        public ViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            noti = (TextView) mView.findViewById(R.id.item_notification_tv_body);
            btnDelete = (ImageButton) mView.findViewById(R.id.item_notification_btn_delete);
        }

        public void setTime(final String notificationTime) {
            TextView time = (TextView) mView.findViewById(R.id.item_notification_tv_time);
            time.setText(notificationTime);
        }

        public void setBody(final String notificationBody) {
            TextView body = (TextView) mView.findViewById(R.id.item_notification_tv_body);
            body.setText(notificationBody);
        }
    }
}
