package com.phungnlg.hellodoctor;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.phungnlg.hellodoctor.Others.PlaceAutocompleteAdapter;

//import butterknife.Bind;
import butterknife.ButterKnife;

public class SignUpForNormalUserActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "SignupActivity";

    private EditText etName;
    private AutoCompleteTextView etAddress;
    private EditText etEmail;
    private EditText etMobileNumber;
    private EditText etPassword;
    private EditText etReenterPassword;
    private Button btnSignUp;
    private TextView linkSignIn;

    private boolean isSignUpSuccessfully;

    private FirebaseAuth mAuth;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_for_normal_user);
        ButterKnife.bind(this);

        etName = (EditText) findViewById(R.id.activity_sign_up_et_name);
        etAddress = (AutoCompleteTextView) findViewById(R.id.activity_sign_up_et_address);
        etEmail = (EditText) findViewById(R.id.activity_sign_up_et_email);
        etMobileNumber = (EditText) findViewById(R.id.activity_sign_up_et_mobile);
        etPassword = (EditText) findViewById(R.id.activity_sign_up_et_password);
        etReenterPassword = (EditText) findViewById(R.id.activity_sign_up_et_reenter_password);
        btnSignUp = (Button) findViewById(R.id.activity_sign_up_btn_signup);
        linkSignIn = (TextView) findViewById(R.id.activity_sign_up_link);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0 /* clientId */, this)
                .addApi(Places.GEO_DATA_API)
                .build();
        mAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient, BOUNDS_GREATER_SYDNEY,
                                                null);
        etAddress.setAdapter(mAdapter);

        mAuth = FirebaseAuth.getInstance();

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signup();
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

    public void signup() {

        if (!validate()) {
            onSignupFailed();
            return;
        }
        btnSignUp.setEnabled(false);
        //Hiển  thị dialog tạo tài khoản
        final ProgressDialog PROGRESSDIALOG = new ProgressDialog(SignUpForNormalUserActivity.this,
                                                                 R.style.AppTheme_Dark_Dialog);
        PROGRESSDIALOG.setIndeterminate(true);
        PROGRESSDIALOG.setMessage(getText(R.string.creating_account));
        PROGRESSDIALOG.show();

        final String NAME = etName.getText().toString();
        final String ADDRESS = etAddress.getText().toString();
        String email = etEmail.getText().toString();
        final String MOBILE = etMobileNumber.getText().toString();
        String password = etPassword.getText().toString();
        String reEnterPassword = etReenterPassword.getText().toString();

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
        mAuth.signInWithEmailAndPassword(email, password)
             .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                 @Override
                 public void onComplete(
                         @NonNull
                                 Task<AuthResult> task) {
                     //Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                     mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                     String uid = mFirebaseUser.getUid();
                     myRef.child("user-normal").child(uid).child("name").setValue(NAME);
                     myRef.child("user-normal").child(uid).child("address").setValue(ADDRESS);
                     myRef.child("user-normal").child(uid).child("mobile").setValue(MOBILE);

                     myUser.child(uid).child("bio").setValue(ADDRESS);
                     myUser.child(uid).child("following").setValue(0);
                     myUser.child(uid).child("follower").setValue(0);
                     myUser.child(uid).child("isDoctor").setValue(false);
                     myUser.child(uid).child("name").setValue(NAME);

                     myNotification.child(uid).child("welcome").child("isReaded").setValue(false);
                     myNotification.child(uid).child("welcome").child("notification")
                                   .setValue("Chào mừng bạn đến với HelloDoctor!");
                     myNotification.child(uid).child("welcome").child("time").setValue("Xin chào!");


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

    public void onBackPressed() {
        // Disable going back to the MainActivity
        // moveTaskToBack(true);
        Intent intent = new Intent(getApplicationContext(), SignUpTypeActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }

    public void onSignupSuccess() {
        btnSignUp.setEnabled(true);
        setResult(RESULT_OK, null);
        Toast.makeText(getBaseContext(), R.string.create_account_successfully, Toast.LENGTH_LONG).show();

        Intent intent = new Intent(getApplicationContext(), TabHomeActivity.class);
        intent.putExtra("isDoctor", false);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), R.string.create_account_unsuccessfully, Toast.LENGTH_LONG).show();
        btnSignUp.setEnabled(true);
    }

    public boolean validate() {
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
}
