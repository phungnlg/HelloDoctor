package com.phungnlg.hellodoctor;

//import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.phungnlg.hellodoctor.others.ChildAnimation;
import com.phungnlg.hellodoctor.others.SliderLayout;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.HashMap;

//import android.net.Uri;
//import android.support.customtabs.CustomTabsIntent;
//import android.widget.Button;
//import com.google.firebase.database.Transaction;

@EActivity(R.layout.activity_sign_in)
public class LogInActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;
    private static final int RC_SIGN_IN = 0;
    private SliderLayout sliderLayout;

    @ViewById(R.id.activity_login_tv_email)
    protected EditText etEmail;
    @ViewById(R.id.activity_login_tv_password)
    protected EditText etPassword;
    @ViewById(R.id.activity_login_btn_log_in)
    protected TextView btnLogIn;
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
    private FragmentManager fragmentManager = getSupportFragmentManager();

    @AfterViews
    void init() {
        setUpSlider();
        progressDialog = new ProgressDialog(LogInActivity.this,
                                            R.style.AppTheme_Dark_Dialog);
        final FragmentManager FM = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = FM.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.anim_fade_in, R.anim.anim_fade_out);
        SplashFragment splashFragment = new SplashFragment();
        fragmentTransaction.add(R.id.activity_login_container, splashFragment, "HELLO");
        fragmentTransaction.commit();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                SignInFragment_ hello = new SignInFragment_();
                FragmentTransaction ft = FM.beginTransaction();
                ft.setCustomAnimations(R.anim.anim_fade_in, R.anim.anim_fade_out);
                ft.replace(R.id.activity_login_container, hello);
                ft.commit();
            }
        }, 3000);
    }

    public void setUpSlider() {
        sliderLayout = (SliderLayout) findViewById(R.id.activity_login_slider);
        HashMap<String, Integer> fileMaps = new HashMap<String, Integer>();
        fileMaps.put(getResources().getString(R.string.link3), R.drawable.bg_sign_in_1);
        fileMaps.put(getResources().getString(R.string.link4), R.drawable.bg_sign_in_2);
        fileMaps.put(getResources().getString(R.string.link6), R.drawable.bg_sign_in_3);
        fileMaps.put(getResources().getString(R.string.link7), R.drawable.bg_sign_in_4);
        fileMaps.put(getResources().getString(R.string.link5), R.drawable.bg_sign_in_5);
        fileMaps.put(getResources().getString(R.string.link1), R.drawable.bg_sign_in_6);
        for (final String name : fileMaps.keySet()) {
            TextSliderView textSliderView = new TextSliderView(this);
            textSliderView
                    .description(name)
                    .image(fileMaps.get(name))
                    .setScaleType(BaseSliderView.ScaleType.CenterCrop);
            textSliderView.bundle(new Bundle());
            textSliderView.getBundle().putString("extra", name);

            sliderLayout.addSlider(textSliderView);
        }
        sliderLayout.setPresetTransformer(SliderLayout.Transformer.Default);
        sliderLayout.setPresetIndicator(SliderLayout.PresetIndicators.Right_Bottom);
        sliderLayout.setCustomAnimation(new ChildAnimation());
        sliderLayout.setDuration(5000);
        sliderLayout.startAutoCycle();
    }

    @Click(R.id.activity_login_link)
    void setLink() {
        Intent intent = new Intent(getApplicationContext(), SignUpTypeActivity_.class);
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

        new Handler().postDelayed(
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
    
    @EFragment(R.layout.fragment_bottom_log_in_fields)
    public static class FieldsFragment extends android.support.v4.app.Fragment {

    }

    public static class SplashFragment extends android.support.v4.app.Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            /** Inflating the layout for this fragment **/
            View v = inflater.inflate(R.layout.fragment_bottom_log_in_splash, null);
            TextView tv = (TextView) v.findViewById(R.id.activity_login_splash_name);
            Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.anim_fade_in);
            animation.setDuration(2000);
            tv.startAnimation(animation);
            Typeface type = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Painter.ttf");
            tv.setTypeface(type);
            return v;
        }
    }

}
