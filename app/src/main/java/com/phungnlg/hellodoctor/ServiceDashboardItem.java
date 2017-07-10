package com.phungnlg.hellodoctor;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;


/**
 * Created by Phil on 7/6/2017.
 */

public class ServiceDashboardItem extends AbstractItem<ServiceDashboardItem, ServiceDashboardItem.ViewHolder> {

    private String name;
    private String detail;
    private int drawable;

    public int getDrawable() {
        return drawable;
    }

    public void setDrawable(int drawable) {
        this.drawable = drawable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    public int getType() {
        return R.id.layout_service_dashboard;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.layout_service_dashboard;
    }

    @Override
    public void bindView(ViewHolder viewHolder, List<Object> payloads) {
        super.bindView(viewHolder, payloads);
        viewHolder.tvName.setText(name);
        viewHolder.tvDetail.setText(detail);
        viewHolder.ivCover.setImageResource(drawable);
    }

    @Override
    public void unbindView(ViewHolder holder) {
        super.unbindView(holder);
        holder.tvName.setText(name);
        holder.tvDetail.setText(detail);
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        protected TextView tvName;
        protected TextView tvDetail;
        protected ImageView ivCover;

        public ViewHolder(View view) {
            super(view);
            this.tvName = (TextView) view.findViewById(R.id.layout_service_dashboard_tv_title);
            this.tvDetail = (TextView) view.findViewById(R.id.layout_service_dashboard_tv_detail);
            this.ivCover = (ImageView) view.findViewById(R.id.layout_service_dashboard_iv);
        }
    }
}
