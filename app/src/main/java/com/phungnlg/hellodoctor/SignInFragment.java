package com.phungnlg.hellodoctor;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.Length;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.phungnlg.hellodoctor.others.SliderLayout;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.List;

/**
 * Created by Phil on 7/2/2017.
 */
@EFragment(R.layout.fragment_bottom_log_in_fields)
public class SignInFragment extends android.support.v4.app.Fragment implements Validator.ValidationListener {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;
    private static final int RC_SIGN_IN = 0;
    private SliderLayout sliderLayout;

    @NotEmpty(message = "Bạn chưa nhập email")
    @Email(message = "Email không hợp lệ")
    @ViewById(R.id.activity_login_tv_email)
    protected EditText etEmail;
    @NotEmpty(message = "Bạn chưa nhập mật khẩu")
    //@Password có vấn đề, do chỉ cần kiểm tra độ dài mật khẩu, tạm dùng @Length để thay thế
    @Length(min = 4, max = 10, message = "Mật khẩu có từ 4 - 10 kí tự")
    @ViewById(R.id.activity_login_tv_password)
    protected EditText etPassword;
    @ViewById(R.id.activity_login_btn_log_in)
    protected TextView btnLogIn;
    @ViewById(R.id.activity_login_link)
    protected TextView linkSignUp;

    private ProgressDialog progressDialog;
    private Boolean isValid;
    private Validator validator;

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
        progressDialog = new ProgressDialog(getActivity(),
                                            R.style.AppTheme_Dark_Dialog);
        validator = new Validator(this);
        validator.setValidationListener(this);
    }

    @Click(R.id.activity_login_link)
    void setLink() {
        Intent intent = new Intent(getActivity(), SignUpTypeActivity_.class);
        startActivityForResult(intent, REQUEST_SIGNUP);
        getActivity().finish();
        getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
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

        if (!saripaarValidate()) {
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
                        } else {
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
             .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                 @Override
                 public void onComplete(
                         @NonNull
                                 Task<AuthResult> task) {
                     isLogInSuccessfully = true;
                     isLogInByFacebook = false;
                     if (!task.isSuccessful()) {
                         isLogInSuccessfully = false;
                         Toast.makeText(getActivity(), R.string.auth_failed,
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }


    /*@Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }*/

    public void onLoginSuccess() {
        btnLogIn.setEnabled(true);

        //TODO Get user's name

        final Intent INTENT = new Intent(getContext(), TabHomeActivity_.class);
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
        getActivity().finish();
        getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    public void onLoginFailed() {
        Toast.makeText(getActivity(), R.string.please_sign_in_again, Toast.LENGTH_LONG).show();
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

    public boolean saripaarValidate() {
        if (validator != null) {
            validator.validate();
        }
        return isValid;
    }

    @Override
    public void onValidationSucceeded() {
        isValid = true;
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        isValid = false;
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(getContext());

            if (view instanceof EditText) {
                ((EditText) view).setError(message);
            } else {
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            }
        }
    }
}
