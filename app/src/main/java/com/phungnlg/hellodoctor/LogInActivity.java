package com.phungnlg.hellodoctor;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import butterknife.ButterKnife;

public class LogInActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;
    private static final int RC_SIGN_IN = 0;

    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogIn;
    private TextView linkSignUp;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private boolean isLogInSuccessfully;
    private boolean isLogInByFacebook = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        etEmail = (EditText) findViewById(R.id.activity_login_tv_email);
        etPassword = (EditText) findViewById(R.id.activity_login_tv_password);
        btnLogIn = (Button) findViewById(R.id.activity_login_btn_log_in);
        linkSignUp = (TextView) findViewById(R.id.activity_login_link);

        linkSignUp.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), SignUpTypeActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });

        btnLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

        // Gọi dịch vụ xác thực tài khoản của Firebase
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(
                    @NonNull
                            FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    //Hàm đăng nhập, sử dụng xác thực email+pasword
    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }

        btnLogIn.setEnabled(false);

        final ProgressDialog PROGRESSDIALOG = new ProgressDialog(LogInActivity.this,
                                                                 R.style.AppTheme_Dark_Dialog);
        PROGRESSDIALOG.setIndeterminate(true);
        PROGRESSDIALOG.setMessage(getText(R.string.logging_in));
        PROGRESSDIALOG.show();

        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        mAuth.signInWithEmailAndPassword(email, password)
             .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                 @Override
                 public void onComplete(
                         @NonNull
                                 Task<AuthResult> task) {
                     Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                     isLogInSuccessfully = true;
                     isLogInByFacebook = false;
                     // If sign in fails, display a message to the user. If sign in succeeds
                     // the auth state listener will be notified and logic to handle the
                     // signed in user can be handled in the listener.
                     if (!task.isSuccessful()) {
                         Log.w(TAG, "signInWithEmail:failed", task.getException());
                         isLogInSuccessfully = false;
                         Toast.makeText(LogInActivity.this, R.string.auth_failed,
                                        Toast.LENGTH_SHORT).show();
                     }
                 }
             });

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onLoginSuccess or onLoginFailed
                        if (isLogInSuccessfully) {
                            onLoginSuccess();
                        }
                        else {
                            onLoginFailed();
                        }
                        PROGRESSDIALOG.dismiss();
                    }
                }, 3000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    //Không sử dụng nữa
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
             .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                 @Override
                 public void onComplete(
                         @NonNull
                                 Task<AuthResult> task) {
                     Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
                     Toast.makeText(LogInActivity.this, "Đã đăng nhập với Google",
                                    Toast.LENGTH_SHORT).show();
                     // If sign in fails, display a message to the user. If sign in succeeds
                     // the auth state listener will be notified and logic to handle the
                     // signed in user can be handled in the listener.
                     if (!task.isSuccessful()) {
                         Log.w(TAG, "signInWithCredential", task.getException());
                         Toast.makeText(LogInActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                     }
                     // ...
                 }
             });
    }

    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        btnLogIn.setEnabled(true);

        //TODO Get user's name

        final Intent INTENT = new Intent(getApplicationContext(), TabHomeActivity.class);
        final FirebaseUser USER = FirebaseAuth.getInstance().getCurrentUser();


        if (isLogInByFacebook) {
            if (USER != null) {
                INTENT.putExtra("userName", USER.getDisplayName());
                INTENT.putExtra("userEmail", USER.getEmail());
                INTENT.putExtra("isLogInByFacebook", isLogInByFacebook);
                INTENT.putExtra("isDoctor", true);
            }
        } else {
            INTENT.putExtra("userEmail", USER.getEmail());
            INTENT.putExtra("isLogInByFacebook", isLogInByFacebook);
            INTENT.putExtra("isDoctor", true);
        }
        startActivity(INTENT);
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), R.string.please_sign_in_again, Toast.LENGTH_LONG).show();
        etPassword.setText("");

        btnLogIn.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getText(R.string.enter_valid_email));
            valid = false;
        } else {
            etEmail.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            etPassword.setError(getText(R.string.enter_valid_password));
            valid = false;
        } else {
            etPassword.setError(null);
        }

        return valid;
    }

}
