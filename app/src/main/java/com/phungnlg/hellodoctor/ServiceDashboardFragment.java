package com.phungnlg.hellodoctor;

import android.widget.ImageView;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Phil on 7/3/2017.
 */
@EFragment(R.layout.layout_service_dashboard)
public class ServiceDashboardFragment extends android.support.v4.app.Fragment {
    @ViewById(R.id.layout_service_dashboard_tv_title)
    protected TextView tvTitle;
    @ViewById(R.id.layout_service_dashboard_iv)
    protected ImageView ivBackground;
    @FragmentArg
    protected String title;
    @FragmentArg
    protected int drawableBackground;

    @AfterViews()
    public void init() {
        tvTitle.setText(title);
        ivBackground.setImageResource(drawableBackground);
    }
}
