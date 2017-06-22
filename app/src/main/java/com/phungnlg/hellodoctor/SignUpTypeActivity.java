package com.phungnlg.hellodoctor;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

//import butterknife.Bind;
import butterknife.ButterKnife;

public class SignUpTypeActivity extends AppCompatActivity {
    private static final String TAG = "SignUpTypeActivity";
    private static final int REQUEST_SIGNUP = 0;

    private Button btnBacSy;

    private Button btnNguoiDung;

    private TextView linkSignIn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_type);
        ButterKnife.bind(this);

        btnBacSy = (Button) findViewById(R.id.activity_sign_up_type_btn_bacsy);
        btnNguoiDung = (Button) findViewById(R.id.activity_sign_up_type_btn_nguoidung);
        linkSignIn = (TextView) findViewById(R.id.activity_sign_up_type_link_signin);

        btnBacSy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
        btnNguoiDung.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignUpForNormalUserActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });

        linkSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                Intent intent = new Intent(getApplicationContext(), LogInActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
            }
        });
    }

    public void onBackPressed() {
        // Disable going back to the MainActivity
        // moveTaskToBack(true);
        Intent intent = new Intent(getApplicationContext(), LogInActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }
}
