package com.phungnlg.hellodoctor;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

//import android.os.Bundle;
//import android.view.View;
//import butterknife.Bind;

//import butterknife.ButterKnife;

@EActivity(R.layout.activity_sign_up_type)
public class SignUpTypeActivity extends AppCompatActivity {
    private static final String TAG = "SignUpTypeActivity";
    private static final int REQUEST_SIGNUP = 0;

    @ViewById(R.id.activity_sign_up_type_btn_bacsy)
    protected Button btnBacSy;
    @ViewById(R.id.activity_sign_up_type_btn_nguoidung)
    protected Button btnNguoiDung;
    @ViewById(R.id.activity_sign_up_type_link_signin)
    protected TextView linkSignIn;

    @AfterViews
    public void init() {

    }

    @Click(R.id.activity_sign_up_type_btn_bacsy)
    public void setBtnBacSy() {
        Intent intent = new Intent(getApplicationContext(), SignUpActivity_.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    @Click(R.id.activity_sign_up_type_btn_nguoidung)
    public void setBtnNguoiDung() {
        Intent intent = new Intent(getApplicationContext(), SignUpForNormalUserActivity_
                        .class);
                startActivityForResult(intent, REQUEST_SIGNUP);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    @Click(R.id.activity_sign_up_type_link_signin)
    public void setLinkSignIn() {
        Intent intent = new Intent(getApplicationContext(), LogInActivity_.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }

    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), LogInActivity_.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }
}
