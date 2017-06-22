package com.phungnlg.hellodoctor;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_login)
public class LogInActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;
    private static final int RC_SIGN_IN = 0;

    @ViewById(R.id.activity_login_tv_email)
    protected EditText etEmail;
    @ViewById(R.id.activity_login_tv_password)
    protected EditText etPassword;
    @ViewById(R.id.activity_login_btn_log_in)
    protected Button btnLogIn;
    @ViewById(R.id.activity_login_link)
    protected TextView linkSignUp;

    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseAuth.AuthStateListener mAuthListener = new FirebaseAuth.AuthStateListener() {
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
    private boolean isLogInSuccessfully;
    private boolean isLogInByFacebook = false;

    @AfterViews
    void init() {
        progressDialog = new ProgressDialog(LogInActivity.this,
                                            R.style.AppTheme_Dark_Dialog);
    }

    @Click(R.id.activity_login_link)
    void setLink() {
        Intent intent = new Intent(getApplicationContext(), SignUpTypeActivity.class);
        startActivityForResult(intent, REQUEST_SIGNUP);
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    @Click(R.id.activity_login_btn_log_in)
    void setBtnLogIn() {
        login();
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

    public void login() {
        if (!validate()) {
            onLoginFailed();
            return;
        }

        btnLogIn.setEnabled(false);

        showDialog();

        auth();

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        if (isLogInSuccessfully) {
                            onLoginSuccess();
                        }
                        else {
                            onLoginFailed();
                        }
                        progressDialog.dismiss();
                    }
                }, 3000);
    }

    private void auth() {
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        mAuth.signInWithEmailAndPassword(email, password)
             .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                 @Override
                 public void onComplete(
                         @NonNull
                                 Task<AuthResult> task) {
                     isLogInSuccessfully = true;
                     isLogInByFacebook = false;
                     if (!task.isSuccessful()) {
                         isLogInSuccessfully = false;
                         Toast.makeText(LogInActivity.this, R.string.auth_failed,
                                        Toast.LENGTH_SHORT).show();
                     }
                 }
             });
    }

    private void showDialog() {
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getText(R.string.logging_in));
        progressDialog.show();
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
