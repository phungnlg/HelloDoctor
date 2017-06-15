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

public class SignUp extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "SignupActivity";

    @Bind(R.id.input_name)
    EditText _nameText;
    @Bind(R.id.input_address)
    AutoCompleteTextView _addressText;
    @Bind(R.id.input_email)
    EditText _emailText;
    @Bind(R.id.input_mobile)
    EditText _mobileText;
    @Bind(R.id.input_password)
    EditText _passwordText;
    @Bind(R.id.input_reEnterPassword)
    EditText _reEnterPasswordText;
    @Bind(R.id.btn_signup)
    Button _signupButton;
    @Bind(R.id.link_login)
    TextView _loginLink;
    @Bind(R.id.input_workplace)
    TextView _workplace;
    Spinner _major;


    private boolean isSignUpSuccessfully;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser mFirebaseUser;

    private PlaceAutocompleteAdapter mAdapter;
    protected GoogleApiClient mGoogleApiClient;

    private static final LatLngBounds BOUNDS_GREATER_SYDNEY = new LatLngBounds(
            new LatLng(10.562400, 106.580979), new LatLng(10.998982, 106.699151));

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("message");
    DatabaseReference myUser = database.getReference("User");
    DatabaseReference myNotification = database.getReference("Notifications");
    DatabaseReference mySche = database.getReference("Schedule");


    private String mUserId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.bind(this);

        _major = (Spinner) findViewById(R.id.input_major);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter
                .createFromResource(this, R.array.major, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        _major.setAdapter(adapter);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0 /* clientId */, this)
                .addApi(Places.GEO_DATA_API)
                .build();

        String[] province = getResources().getStringArray(R.array.province);
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, province);

        mAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient, BOUNDS_GREATER_SYDNEY,
                                                null);
        _addressText.setAdapter(mAdapter);

        //_addressText.setAdapter(adapter1);

        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                Intent intent = new Intent(getApplicationContext(), LogIn.class);
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

        _signupButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(SignUp.this,
                                                                 R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getText(R.string.creating_account));
        progressDialog.show();

        final String name = _nameText.getText().toString();
        final String address = _addressText.getText().toString();
        String email = _emailText.getText().toString();
        final String mobile = _mobileText.getText().toString();
        String password = _passwordText.getText().toString();
        String reEnterPassword = _reEnterPasswordText.getText().toString();
        final String workplace = _workplace.getText().toString();


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
                         Toast.makeText(SignUp.this, R.string.auth_failed,
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


                     String uID = mFirebaseUser.getUid();
                     myRef.child("user-doctor").child(uID).child("name").setValue(name);
                     myRef.child("user-doctor").child(uID).child("address").setValue(address);
                     myRef.child("user-doctor").child(uID).child("mobile").setValue(mobile);
                     myRef.child("user-doctor").child(mFirebaseUser.getUid()).child("major")
                          .setValue(_major.getSelectedItem().toString());
                     myRef.child("user-doctor").child(uID).child("workplace").setValue(workplace);

                     myUser.child(uID).child("bio")
                           .setValue("Bác sỹ " + _major.getSelectedItem() + " tại " + workplace);
                     myUser.child(uID).child("following").setValue(0);
                     myUser.child(uID).child("follower").setValue(0);
                     myUser.child(uID).child("isDoctor").setValue(true);
                     myUser.child(uID).child("name").setValue(name);

                     myNotification.child(uID).child("welcome").child("isReaded").setValue(false);
                     myNotification.child(uID).child("welcome").child("notification")
                                   .setValue("Chào mừng bạn đến với HelloDoctor!");
                     myNotification.child(uID).child("welcome").child("time").setValue("Xin chào!");

                     mDatabase.child("al").setValue("Chưa có thông tin");
                     mDatabase.child("bg").setValue("Chưa có thông tin");
                     mDatabase.child("cn").setValue("Chưa có thông tin");
                     mDatabase.child("ca").setValue("Chưa có thông tin");
                     mDatabase.child("aw").setValue("Chưa có thông tin");
                     mDatabase.child("as").setValue("Chưa có thông tin");

                     mySche.child(uID).child("Mon").child("from").setValue("");
                     mySche.child(uID).child("Mon").child("to").setValue("");
                     mySche.child(uID).child("Tue").child("from").setValue("");
                     mySche.child(uID).child("Tue").child("to").setValue("");
                     mySche.child(uID).child("Wed").child("from").setValue("");
                     mySche.child(uID).child("Wed").child("to").setValue("");
                     mySche.child(uID).child("Thu").child("from").setValue("");
                     mySche.child(uID).child("Thu").child("to").setValue("");
                     mySche.child(uID).child("Fri").child("from").setValue("");
                     mySche.child(uID).child("Fri").child("to").setValue("");
                     mySche.child(uID).child("Sat").child("from").setValue("");
                     mySche.child(uID).child("Sat").child("to").setValue("");
                     mySche.child(uID).child("Sun").child("from").setValue("");
                     mySche.child(uID).child("Sun").child("to").setValue("");


                     UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                             .setDisplayName(name).build();

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

                         //Toast.makeText(LoginActivity.this, R.string.auth_failed,Toast.LENGTH_SHORT).show();
                     }
                 }
             });


        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onSignupSuccess or onSignupFailed
                        // depending on success
                        if (isSignUpSuccessfully)
                            onSignupSuccess();
                        else
                            onSignupFailed();
                        // onSignupFailed();
                        progressDialog.dismiss();
                    }
                }, 3000);
    }

    public void onBackPressed() {
        // Disable going back to the MainActivity
        // moveTaskToBack(true);
        Intent intent = new Intent(getApplicationContext(), LogIn.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }

    public void onSignupSuccess() {
        _signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        Toast.makeText(getBaseContext(), R.string.create_account_successfully, Toast.LENGTH_LONG).show();

        Intent intent = new Intent(getApplicationContext(), TabHome.class);
        intent.putExtra("isDoctor", true);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), R.string.create_account_unsuccessfully, Toast.LENGTH_LONG).show();

        _signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = _nameText.getText().toString();
        String address = _addressText.getText().toString();
        String email = _emailText.getText().toString();
        String mobile = _mobileText.getText().toString();
        String password = _passwordText.getText().toString();
        String reEnterPassword = _reEnterPasswordText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            _nameText.setError(getText(R.string.enter_valid_name));
            valid = false;
        } else {
            _nameText.setError(null);
        }

        if (address.isEmpty()) {
            _addressText.setError(getText(R.string.enter_valid_address));
            valid = false;
        } else {
            _addressText.setError(null);
        }


        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError(getText(R.string.enter_valid_email));
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (mobile.isEmpty() || mobile.length() < 10) {
            _mobileText.setError(getText(R.string.enter_valid_phone_number));
            valid = false;
        } else {
            _mobileText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError(getText(R.string.enter_valid_password));
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        if (reEnterPassword.isEmpty() || reEnterPassword.length() < 4 || reEnterPassword.length() > 10 ||
            !(reEnterPassword.equals(password))) {
            _reEnterPasswordText.setError(getText(R.string.enter_matched_password));
            valid = false;
        } else {
            _reEnterPasswordText.setError(null);
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
