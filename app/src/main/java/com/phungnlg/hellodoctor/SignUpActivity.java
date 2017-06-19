package com.phungnlg.hellodoctor;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.phungnlg.hellodoctor.Others.PlaceAutocompleteAdapter;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SignUpActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "SignupActivity";
    private static final LatLngBounds BOUNDS_GREATER_SYDNEY = new LatLngBounds(
            new LatLng(10.562400, 106.580979), new LatLng(10.998982, 106.699151));
    protected GoogleApiClient mGoogleApiClient;
    private EditText etName;
    private AutoCompleteTextView etAddress;
    private EditText etEmail;
    private EditText etMobileNumber;
    private EditText etPassword;
    private EditText etReenterPassword;
    private Button btnSignUp;
    private TextView linkLogIn;
    private TextView etWorkplace;
    private Spinner spnMajor;
    private boolean isSignUpSuccessfully;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser mFirebaseUser;
    private PlaceAutocompleteAdapter mAdapter;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference("message");
    private DatabaseReference myUser = database.getReference("User");
    private DatabaseReference myNotification = database.getReference("Notifications");
    private DatabaseReference mySche = database.getReference("Schedule");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.bind(this);

        etName = (EditText) findViewById(R.id.activity_sign_up_et_name);
        etAddress = (AutoCompleteTextView) findViewById(R.id.activity_sign_up_et_address);
        etEmail = (EditText) findViewById(R.id.activity_sign_up_et_email);
        etMobileNumber = (EditText) findViewById(R.id.activity_sign_up_et_mobile);
        etPassword = (EditText) findViewById(R.id.activity_sign_up_et_password);
        etReenterPassword = (EditText) findViewById(R.id.activity_sign_up_et_reenter_password);
        btnSignUp = (Button) findViewById(R.id.activity_sign_up_btn_signup);
        linkLogIn = (TextView) findViewById(R.id.activity_sign_up_link);
        etWorkplace = (EditText) findViewById(R.id.activity_sign_up_et_workplace);

        spnMajor = (Spinner) findViewById(R.id.activity_sign_up_spn_major);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter
                .createFromResource(this, R.array.major, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnMajor.setAdapter(adapter);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0 /* clientId */, this)
                .addApi(Places.GEO_DATA_API)
                .build();

        mAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient, BOUNDS_GREATER_SYDNEY,
                                                null);
        etAddress.setAdapter(mAdapter);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        linkLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                Intent intent = new Intent(getApplicationContext(), LogInActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
            }
        });

        mAuth = FirebaseAuth.getInstance();
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
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                /*String value = dataSnapshot.getValue(String.class);
                Log.d(TAG, "Value is: " + value);*/
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });
    }

    public void signup() {
        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignupFailed();
            return;
        }

        btnSignUp.setEnabled(false);

        final ProgressDialog PROGRESSDIALOG = new ProgressDialog(SignUpActivity.this,
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
        final String WORKPLACE = etWorkplace.getText().toString();


        mAuth.createUserWithEmailAndPassword(email, password)
             .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                 @Override
                 public void onComplete(
                         @NonNull
                                 Task<AuthResult> task) {
                     Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                     // If sign in fails, display a message to the user. If sign in succeeds
                     // the auth state listener will be notified and logic to handle the
                     // signed in user can be handled in the listener.
                     isSignUpSuccessfully = true;
                     if (!task.isSuccessful()) {
                         Toast.makeText(SignUpActivity.this, R.string.auth_failed,
                                        Toast.LENGTH_SHORT).show();
                         isSignUpSuccessfully = false;
                     }

                     // ...
                 }
             });

        // TODO: Cập nhật các thông tin khác ở đây(tên, địa chỉ, ...)

        mAuth.signInWithEmailAndPassword(email, password)
             .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                 @Override
                 public void onComplete(
                         @NonNull
                                 Task<AuthResult> task) {
                     Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                     mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                     DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Profile")
                                                                   .child(mFirebaseUser.getUid());


                     String uid = mFirebaseUser.getUid();
                     myRef.child("user-doctor").child(uid).child("name").setValue(NAME);
                     myRef.child("user-doctor").child(uid).child("address").setValue(ADDRESS);
                     myRef.child("user-doctor").child(uid).child("mobile").setValue(MOBILE);
                     myRef.child("user-doctor").child(mFirebaseUser.getUid()).child("major")
                          .setValue(spnMajor.getSelectedItem().toString());
                     myRef.child("user-doctor").child(uid).child("workplace").setValue(WORKPLACE);

                     myUser.child(uid).child("bio")
                           .setValue("Bác sỹ " + spnMajor.getSelectedItem() + " tại " + WORKPLACE);
                     myUser.child(uid).child("following").setValue(0);
                     myUser.child(uid).child("follower").setValue(0);
                     myUser.child(uid).child("isDoctor").setValue(true);
                     myUser.child(uid).child("name").setValue(NAME);

                     myNotification.child(uid).child("welcome").child("isReaded").setValue(false);
                     myNotification.child(uid).child("welcome").child("notification")
                                   .setValue("Chào mừng bạn đến với HelloDoctor!");
                     myNotification.child(uid).child("welcome").child("time").setValue("Xin chào!");

                     mDatabase.child("al").setValue("Chưa có thông tin");
                     mDatabase.child("bg").setValue("Chưa có thông tin");
                     mDatabase.child("cn").setValue("Chưa có thông tin");
                     mDatabase.child("ca").setValue("Chưa có thông tin");
                     mDatabase.child("aw").setValue("Chưa có thông tin");
                     mDatabase.child("as").setValue("Chưa có thông tin");

                     mySche.child(uid).child("Mon").child("from").setValue("");
                     mySche.child(uid).child("Mon").child("to").setValue("");
                     mySche.child(uid).child("Tue").child("from").setValue("");
                     mySche.child(uid).child("Tue").child("to").setValue("");
                     mySche.child(uid).child("Wed").child("from").setValue("");
                     mySche.child(uid).child("Wed").child("to").setValue("");
                     mySche.child(uid).child("Thu").child("from").setValue("");
                     mySche.child(uid).child("Thu").child("to").setValue("");
                     mySche.child(uid).child("Fri").child("from").setValue("");
                     mySche.child(uid).child("Fri").child("to").setValue("");
                     mySche.child(uid).child("Sat").child("from").setValue("");
                     mySche.child(uid).child("Sat").child("to").setValue("");
                     mySche.child(uid).child("Sun").child("from").setValue("");
                     mySche.child(uid).child("Sun").child("to").setValue("");


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
                         Log.w(TAG, "signInWithEmail:failed", task.getException());
                     }
                 }
             });


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
                        PROGRESSDIALOG.dismiss();
                    }
                }, 3000);
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
        intent.putExtra("isDoctor", true);
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
