package com.phungnlg.hellodoctor;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

@EActivity(R.layout.activity_sign_up)
public class SignUpActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener, Validator.ValidationListener {
    private static final String TAG = "SignupActivity";
    private static final LatLngBounds BOUNDS_GREATER_SYDNEY = new LatLngBounds(
            new LatLng(10.562400, 106.580979), new LatLng(10.998982, 106.699151));
    protected GoogleApiClient mGoogleApiClient;

    private boolean isSignUpSuccessfully;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser mFirebaseUser;
    private PlaceAutocompleteAdapter mAdapter;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference("message");
    private DatabaseReference myUser = database.getReference("User");
    private DatabaseReference myNotification = database.getReference("Notifications");
    private DatabaseReference mySche = database.getReference("Schedule");
    private DatabaseReference doctorBackground = database.getReference("Profile");

    private ProgressDialog progressDialog;
    private Boolean isValid;
    private Validator validator;

    public static final String USERDOCTOR = "user-doctor";
    public static final String NOINFO = "Chưa có thông tin";
    public static final String FROM = "from";
    public static final String TO = "to";

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

    @NotEmpty(message = "Vui lòng nhập nơi bạn làm việc")
    @ViewById(R.id.activity_sign_up_et_workplace)
    protected EditText etWorkplace;
    @ViewById(R.id.activity_sign_up_link)
    protected TextView linkLogIn;
    @ViewById(R.id.activity_sign_up_spn_major)
    protected Spinner spnMajor;

    @Click(R.id.activity_sign_up_link)
    public void setLinkLogIn() {
        Intent intent = new Intent(getApplicationContext(), LogInActivity_.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }

    @Click(R.id.activity_sign_up_btn_signup)
    public void setBtnSignUp() {
        signup();
    }

    public void setSpnMajor() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter
                .createFromResource(this, R.array.major, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnMajor.setAdapter(adapter);
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

    @AfterViews
    public void init() {
        progressDialog = new ProgressDialog(SignUpActivity.this,
                                            R.style.AppTheme_Dark_Dialog);
        validator = new Validator(this);
        validator.setValidationListener(this);
        setEtAddress();
        setSpnMajor();
        setmAuthListener();
    }

    public void createUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
             .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                 @Override
                 public void onComplete(
                         @NonNull
                                 Task<AuthResult> task) {
                     Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                     isSignUpSuccessfully = true;
                     if (!task.isSuccessful()) {
                         Toast.makeText(SignUpActivity.this, R.string.auth_failed,
                                        Toast.LENGTH_SHORT).show();
                         isSignUpSuccessfully = false;
                     }

                     // ...
                 }
             });
    }

    public void createUserData(String email, String password, final String name,
                               final String address, final String mobile, final String workplace) {
        mAuth.signInWithEmailAndPassword(email, password)
             .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                 @Override
                 public void onComplete(
                         @NonNull Task<AuthResult> task) {
                     //Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                     mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                     DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Profile")
                                                                   .child(mFirebaseUser.getUid());
                     String uid = mFirebaseUser.getUid();
                     createUserDoctorProfile(uid, name, address, mobile, workplace);
                     createUserProfile(uid, workplace, name);
                     createUserNotification(uid);
                     createDoctorBackground(uid);
                     createDoctorSchedule(uid);
                     UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                             .setDisplayName(name).build();
                     mFirebaseUser.updateProfile(profileChangeRequest)
                                  .addOnCompleteListener(new OnCompleteListener<Void>() {
                                      @Override
                                      public void onComplete(
                                              @NonNull
                                                      Task<Void> task) {
                                          if (task.isSuccessful()) {
                                              //Log.d(TAG, "User profile updated.");
                                          }
                                      }
                                  });

                     mFirebaseUser.sendEmailVerification();

                     onSignupSuccess();

                     if (!task.isSuccessful()) {
                         //Log.w(TAG, "signInWithEmail:failed", task.getException());
                     }
                 }
             });
    }

    public void createUserDoctorProfile(String uid, String name, String address, String mobile, String workplace) {
        myRef.child(USERDOCTOR).child(uid).child("name").setValue(name);
        myRef.child(USERDOCTOR).child(uid).child("address").setValue(address);
        myRef.child(USERDOCTOR).child(uid).child("mobile").setValue(mobile);
        myRef.child(USERDOCTOR).child(mFirebaseUser.getUid()).child("major")
             .setValue(spnMajor.getSelectedItem().toString());
        myRef.child(USERDOCTOR).child(uid).child("workplace").setValue(workplace);
    }

    public void createUserProfile(String uid, String WORKPLACE, String NAME) {
        myUser.child(uid).child("bio")
              .setValue("Bác sỹ " + spnMajor.getSelectedItem() + " tại " + WORKPLACE);
        myUser.child(uid).child("following").setValue(0);
        myUser.child(uid).child("follower").setValue(0);
        myUser.child(uid).child("isDoctor").setValue(true);
        myUser.child(uid).child("name").setValue(NAME);
    }

    public void createUserNotification(String uid) {
        myNotification.child(uid).child("welcome").child("isReaded").setValue(false);
        myNotification.child(uid).child("welcome").child("notification")
                      .setValue("Chào mừng bạn đến với HelloDoctor!");
        myNotification.child(uid).child("welcome").child("time").setValue("Xin chào!");
    }

    public void createDoctorBackground(String uid) {
        doctorBackground.child(uid).child("al").setValue(NOINFO);
        doctorBackground.child(uid).child("bg").setValue(NOINFO);
        doctorBackground.child(uid).child("cn").setValue(NOINFO);
        doctorBackground.child(uid).child("ca").setValue(NOINFO);
        doctorBackground.child(uid).child("aw").setValue(NOINFO);
        doctorBackground.child(uid).child("as").setValue(NOINFO);
    }

    public void createDoctorSchedule(String uid) {
        mySche.child(uid).child("Mon").child(FROM).setValue("");
        mySche.child(uid).child("Mon").child(TO).setValue("");
        mySche.child(uid).child("Tue").child(FROM).setValue("");
        mySche.child(uid).child("Tue").child(TO).setValue("");
        mySche.child(uid).child("Wed").child(FROM).setValue("");
        mySche.child(uid).child("Wed").child(TO).setValue("");
        mySche.child(uid).child("Thu").child(FROM).setValue("");
        mySche.child(uid).child("Thu").child(TO).setValue("");
        mySche.child(uid).child("Fri").child(FROM).setValue("");
        mySche.child(uid).child("Fri").child(TO).setValue("");
        mySche.child(uid).child("Sat").child(FROM).setValue("");
        mySche.child(uid).child("Sat").child(TO).setValue("");
        mySche.child(uid).child("Sun").child(FROM).setValue("");
        mySche.child(uid).child("Sun").child(TO).setValue("");
    }

    public void signup() {
        Log.d(TAG, "Signup");

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
        final String WORKPLACE = etWorkplace.getText().toString();

        createUser(email, password);

        createUserData(email, password, NAME, ADDRESS, MOBILE, WORKPLACE);

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onSignupSuccess or onSignupFailed
                        // depending on success
                        if (isSignUpSuccessfully) {
                            onSignupSuccess();
                        } else {
                            onSignupFailed();
                        }
                        progressDialog.dismiss();
                    }
                }, 3000);
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
        intent.putExtra("isDoctor", true);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), R.string.create_account_unsuccessfully, Toast.LENGTH_LONG).show();

        btnSignUp.setEnabled(true);
    }

   /* public boolean validate() {
        boolean valid = true;
        String name = etName.getText().toString();
        String address = etAddress.getText().toString();
        String email = etEmail.getText().toString();
        String mobile = etMobileNumber.getText().toString();
        String password = etPassword.getText().toString();
        String reEnterPassword = etReenterPassword.getText().toString();
        if (name.isEmpty() || name.length() < 3) {
            etName.setError(getText(R.string.enter_valid_name));
            valid = false;
        } else {
            etName.setError(null);
        }
        if (address.isEmpty()) {
            etAddress.setError(getText(R.string.enter_valid_address));
            valid = false;
        } else {
            etAddress.setError(null);
        }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getText(R.string.enter_valid_email));
            valid = false;
        } else {
            etEmail.setError(null);
        }
        if (mobile.isEmpty() || mobile.length() < 10) {
            etMobileNumber.setError(getText(R.string.enter_valid_phone_number));
            valid = false;
        } else {
            etMobileNumber.setError(null);
        }
        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            etPassword.setError(getText(R.string.enter_valid_password));
            valid = false;
        } else {
            etPassword.setError(null);
        }
        if (reEnterPassword.isEmpty() || reEnterPassword.length() < 4 || reEnterPassword.length() > 10 ||
            !(reEnterPassword.equals(password))) {
            etReenterPassword.setError(getText(R.string.enter_matched_password));
            valid = false;
        } else {
            etReenterPassword.setError(null);
        }
        return valid;
    }*/

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
