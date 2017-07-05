package com.phungnlg.hellodoctor;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;

//import android.widget.TextView;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

    private boolean isLoggedIn;

    private FirebaseAuth mAuth;

    @AfterViews
    public void init() {
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            isLoggedIn = true;
        } else {
            isLoggedIn = false;
        }

        if (isLoggedIn) {
            Intent intent = new Intent(this, TabHomeActivity_.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, LogInActivity_.class);
            startActivity(intent);
        }
    }
}
