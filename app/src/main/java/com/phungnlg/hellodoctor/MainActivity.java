package com.phungnlg.hellodoctor;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    TextView t;
    private boolean isLoggedIn;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            isLoggedIn = true;
        } else isLoggedIn = false;

        if (isLoggedIn) {
            Intent intent = new Intent(this, TabHome.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, LogIn.class);
            startActivity(intent);
        }
    }
}
