package com.phungnlg.hellodoctor;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Phil on 07/05/2017.
 */

public class CVFragment extends Fragment {
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Profile").child(user.getUid());
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private TextView tvName;
    private EditText etAcademicLevel;
    private EditText etBackground;
    private EditText etClinicName;
    private EditText etClinicAddress;
    private EditText etAward;
    private EditText etAssociation;
    private ImageButton btnSave;
    private Boolean isEditMode;
    private String key;
    private String doctorName;

    public CVFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            isEditMode = bundle.getBoolean("isEditMode");
            key = bundle.getString("key");
            doctorName = bundle.getString("doctorName");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View VIEW = inflater.inflate(R.layout.fragment_doctor_background, container, false);

        tvName = (TextView) VIEW.findViewById(R.id.fragment_cv_tv_username);
        etAcademicLevel = (EditText) VIEW.findViewById(R.id.fragment_cv_et_academiclevel);
        etBackground = (EditText) VIEW.findViewById(R.id.fragment_cv_et_background);
        etClinicName = (EditText) VIEW.findViewById(R.id.fragment_cv_et_clinicname);
        etClinicAddress = (EditText) VIEW.findViewById(R.id.fragment_cv_et_clinicaddress);
        etAward = (EditText) VIEW.findViewById(R.id.fragment_cv_et_adward);
        etAssociation = (EditText) VIEW.findViewById(R.id.fragment_cv_et_association);
        btnSave = (ImageButton) VIEW.findViewById(R.id.fragment_cv_btn_Save);

        if (!isEditMode) {
            btnSave.setVisibility(View.GONE);
            tvName.setText(getText(R.string.cv_doctor) + " - BS " + doctorName);
            tvName.setGravity(Gravity.CENTER);
            etAcademicLevel.setEnabled(false);
            etBackground.setEnabled(false);
            etClinicAddress.setEnabled(false);
            etClinicName.setEnabled(false);
            etAward.setEnabled(false);
            etAssociation.setEnabled(false);
        } else {
            DatabaseReference userInfo = database.getReference("User").child(key);
            userInfo.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    tvName.setText("Hồ sơ bác sỹ " + dataSnapshot.child("name").getValue().toString());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        DatabaseReference m = database.getReference("Profile").child(key);
        m.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                etAcademicLevel.setHint(dataSnapshot.child("al").getValue().toString());
                etBackground.setHint(dataSnapshot.child("bg").getValue().toString());
                etClinicName.setHint(dataSnapshot.child("cn").getValue().toString());
                etClinicAddress.setHint(dataSnapshot.child("ca").getValue().toString());
                etAward.setHint(dataSnapshot.child("aw").getValue().toString());
                etAssociation.setHint(dataSnapshot.child("as").getValue().toString());


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etAcademicLevel.getText().toString() != null) {
                    mDatabase.child("al").setValue(etAcademicLevel.getText().toString());
                } else {
                    mDatabase.child("al").setValue(getText(R.string.cv_no_info));
                }

                if (etBackground.getText().toString() != null) {
                    mDatabase.child("bg").setValue(etBackground.getText().toString());
                } else {
                    mDatabase.child("bg").setValue(getText(R.string.cv_no_info));
                }

                if (etClinicName.getText().toString() != null) {
                    mDatabase.child("cn").setValue(etClinicName.getText().toString());
                } else {
                    mDatabase.child("cn").setValue(getText(R.string.cv_no_info));
                }

                if (etClinicAddress.getText().toString() != null) {
                    mDatabase.child("ca").setValue(etClinicAddress.getText().toString());
                } else {
                    mDatabase.child("ca").setValue(getText(R.string.cv_no_info));
                }

                if (etAward.getText().toString() != null) {
                    mDatabase.child("aw").setValue(etAward.getText().toString());
                } else {
                    mDatabase.child("aw").setValue(getText(R.string.cv_no_info));
                }

                if (etAssociation.getText().toString() != null) {
                    mDatabase.child("as").setValue(etAssociation.getText().toString());
                } else {
                    mDatabase.child("as").setValue(getText(R.string.cv_no_info));
                }

                Toast.makeText(getContext(), R.string.ho_so_cap_nhat_thanh_cong, Toast.LENGTH_SHORT).show();
            }
        });

        return VIEW;
    }
}
