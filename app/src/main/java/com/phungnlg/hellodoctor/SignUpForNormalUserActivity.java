package com.phungnlg.hellodoctor;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.ConfirmPassword;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.Length;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Password;
import com.phungnlg.hellodoctor.others.PlaceAutocompleteAdapter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.List;

@EActivity(R.layout.activity_sign_up_for_normal_user)
public class SignUpForNormalUserActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener, Validator.ValidationListener {
    private static final String TAG = "SignupActivity";

    private boolean isSignUpSuccessfully;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser mFirebaseUser;

    private PlaceAutocompleteAdapter mAdapter;
    protected GoogleApiClient mGoogleApiClient;

    private static final LatLngBounds BOUNDS_GREATER_SYDNEY = new LatLngBounds(
            new LatLng(10.562400, 106.580979), new LatLng(10.998982, 106.699151));

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference("message");
    private DatabaseReference myUser = database.getReference("User");
    private DatabaseReference myNotification = database.getReference("Notifications");

    private ProgressDialog progressDialog;
    private Boolean isValid;
    private Validator validator;

    @Length(min = 3, message = "Ít nhất 3 kí tự")
    @ViewById(R.id.activity_sign_up_et_name)
    protected EditText etName;

    @NotEmpty(message = "Vui lòng nhập địa chỉ chính xác")
    @ViewById(R.id.activity_sign_up_et_address)
    protected AutoCompleteTextView etAddress;

    @Email(message = "Định dạng email không đúng")
    @ViewById(R.id.activity_sign_up_et_email)
    protected EditText etEmail;

    @Length(min = 10, message = "Số điện thoại phải có ít nhất 10 kí tự số")
    @ViewById(R.id.activity_sign_up_et_mobile)
    protected EditText etMobileNumber;

    @Password
    @Length(min = 4, max = 10, message = "Mật khẩu có từ 4 - 10 kí tự")
    @ViewById(R.id.activity_sign_up_et_password)
    protected EditText etPassword;

    @ConfirmPassword(message = "Mật khẩu không khớp")
    @ViewById(R.id.activity_sign_up_et_reenter_password)
    protected EditText etReenterPassword;

    @ViewById(R.id.activity_sign_up_btn_signup)
    protected Button btnSignUp;
    @ViewById(R.id.activity_sign_up_link)
    protected TextView linkSignIn;

    @Click(R.id.activity_sign_up_btn_signup)
    public void setBtnSignUp() {
        signup();
    }

    @Click(R.id.activity_sign_up_link)
    public void setLinkSignIn() {
        Intent intent = new Intent(getApplicationContext(), LogInActivity_.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }

    @AfterViews
    public void init() {
        progressDialog = new ProgressDialog(SignUpForNormalUserActivity.this,
                                            R.style.AppTheme_Dark_Dialog);
        validator = new Validator(this);
        validator.setValidationListener(this);
        setEtAddress();
        setmAuthListener();
    }

    public void setEtAddress() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0 /* clientId */, this)
                .addApi(Places.GEO_DATA_API)
                .build();
        mAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient, BOUNDS_GREATER_SYDNEY,
                                                null);
        etAddress.setAdapter(mAdapter);
    }

    public void setmAuthListener() {
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(
                    @NonNull
                            FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
    }

    public void showProgressDialog() {
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getText(R.string.creating_account));
        progressDialog.show();
    }

    public void createUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
             .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                 @Override
                 public void onComplete(
                         @NonNull
                                 Task<AuthResult> task) {
                     isSignUpSuccessfully = true;
                     if (!task.isSuccessful()) {
                         Toast.makeText(SignUpForNormalUserActivity.this, R.string.create_account_successfully,
                                        Toast.LENGTH_SHORT).show();
                         isSignUpSuccessfully = false;
                     }
                 }
             });
    }

    public void createUserNormalProfile(String uid, String name, String address, String mobile) {
        myRef.child("user-normal").child(uid).child("name").setValue(name);
        myRef.child("user-normal").child(uid).child("address").setValue(address);
        myRef.child("user-normal").child(uid).child("mobile").setValue(mobile);
    }

    public void createUserProfile(String uid, String address, String name) {
        myUser.child(uid).child("bio").setValue(address);
        myUser.child(uid).child("following").setValue(0);
        myUser.child(uid).child("follower").setValue(0);
        myUser.child(uid).child("isDoctor").setValue(false);
        myUser.child(uid).child("name").setValue(name);
    }

    public void createUserNotification(String uid) {
        myNotification.child(uid).child("welcome").child("isReaded").setValue(false);
        myNotification.child(uid).child("welcome").child("notification")
                      .setValue("Chào mừng bạn đến với HelloDoctor!");
        myNotification.child(uid).child("welcome").child("time").setValue("Xin chào!");
    }

    public void createUserData(String email,
                               String password,
                               final String NAME,
                               final String ADDRESS,
                               final String MOBILE) {
        mAuth.signInWithEmailAndPassword(email, password)
             .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                 @Override
                 public void onComplete(
                         @NonNull Task<AuthResult> task) {
                     mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                     String uid = mFirebaseUser.getUid();
                     createUserNormalProfile(uid, NAME, ADDRESS, MOBILE);
                     createUserProfile(uid, ADDRESS, NAME);
                     createUserNotification(uid);
                     UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                             .setDisplayName(NAME).build();

                     mFirebaseUser.updateProfile(profileChangeRequest)
                                  .addOnCompleteListener(new OnCompleteListener<Void>() {
                                      @Override
                                      public void onComplete(
                                              @NonNull
                                                      Task<Void> task) {
                                          if (task.isSuccessful()) {
                                              Log.d(TAG, "User profile updated.");
                                          }
                                      }
                                  });
                     mFirebaseUser.sendEmailVerification();
                     onSignupSuccess();
                     if (!task.isSuccessful()) {
                     }
                 }
             });
    }

    public void signup() {

        if (!saripaarValidate()) {
            onSignupFailed();
            return;
        }
        btnSignUp.setEnabled(false);
        showProgressDialog();
        final String NAME = etName.getText().toString();
        final String ADDRESS = etAddress.getText().toString();
        String email = etEmail.getText().toString();
        final String MOBILE = etMobileNumber.getText().toString();
        String password = etPassword.getText().toString();
        String reEnterPassword = etReenterPassword.getText().toString();
        createUser(email, password);
        createUserData(email, password, NAME, ADDRESS, MOBILE);
    }

    public void onBackPressed() {
        // Disable going back to the MainActivity
        // moveTaskToBack(true);
        Intent intent = new Intent(getApplicationContext(), SignUpTypeActivity_.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }

    public void onSignupSuccess() {
        btnSignUp.setEnabled(true);
        setResult(RESULT_OK, null);
        Toast.makeText(getBaseContext(), R.string.create_account_successfully, Toast.LENGTH_LONG).show();

        Intent intent = new Intent(getApplicationContext(), TabHomeActivity_.class);
        intent.putExtra("isDoctor", false);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), R.string.create_account_unsuccessfully, Toast.LENGTH_LONG).show();
        btnSignUp.setEnabled(true);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Log.e(TAG, "onConnectionFailed: ConnectionResult.getErrorCode() = "
                   + connectionResult.getErrorCode());

        // TODO(Developer): Check error code and notify the user of error state and resolution.
        Toast.makeText(this,
                       "Could not connect to Google API Client: Error " + connectionResult.getErrorCode(),
                       Toast.LENGTH_SHORT).show();
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
            String message = error.getCollatedErrorMessage(this);

            if (view instanceof EditText) {
                ((EditText) view).setError(message);
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }
    }
}
