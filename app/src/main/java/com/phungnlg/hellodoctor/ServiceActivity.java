package com.phungnlg.hellodoctor;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;

/**
 * Created by Phil on 7/3/2017.
 */
@EActivity(R.layout.activity_service_main)
public class ServiceActivity extends AppCompatActivity {

    @AfterViews
    public void init() {
        FragmentManager fm = getSupportFragmentManager();
        ServiceDashboardFragment exclusiveFragment =
                ServiceDashboardFragment_.builder()
                                         .title("Độc quyền").drawableBackground(R.drawable.bg_sign_in_1).build();
        FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(R.anim.anim_fade_in, R.anim.anim_fade_out);
        ft.add(R.id.activity_service_main_exclusive, exclusiveFragment);
        ft.commit();

        ServiceDashboardFragment medicareFragment =
                ServiceDashboardFragment_.builder().
                        title("Dịch vụ y tế").drawableBackground(R.drawable.bg_sign_in_2).build();
        FragmentTransaction ftMedicare = fm.beginTransaction();
        ftMedicare.setCustomAnimations(R.anim.anim_fade_in, R.anim.anim_fade_out);
        ftMedicare.add(R.id.activity_service_main_medicare, medicareFragment);
        ftMedicare.commit();

        ServiceDashboardFragment testFragment =
                ServiceDashboardFragment_.builder()
                                         .title("Xét nghiệm").drawableBackground(R.drawable.bg_sign_in_3).build();
        FragmentTransaction ftTest = fm.beginTransaction();
        ftTest.setCustomAnimations(R.anim.anim_fade_in, R.anim.anim_fade_out);
        ftTest.add(R.id.activity_service_main_test, testFragment);
        ftTest.commit();
    }
}
