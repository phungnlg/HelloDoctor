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
    TextView name;
    EditText AL, BG, CN, CA, AW, AS;
    ImageButton btnSave;
    Boolean isEditMode;
    String key;

    public CVFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            isEditMode = bundle.getBoolean("isEditMode");
            key = bundle.getString("key");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.tab_cv, container, false);

        name = (TextView) view.findViewById(R.id.cv_username);
        AL = (EditText) view.findViewById(R.id.cv_academiclevel);
        BG = (EditText) view.findViewById(R.id.cv_background);
        CN = (EditText) view.findViewById(R.id.cv_clinicname);
        CA = (EditText) view.findViewById(R.id.cv_clinicaddress);
        AW = (EditText) view.findViewById(R.id.cv_adward);
        AS = (EditText) view.findViewById(R.id.cv_association);
        btnSave = (ImageButton) view.findViewById(R.id.cv_btnSave);

        if (!isEditMode) {
            btnSave.setVisibility(View.GONE);
            name.setText("HỒ SƠ BÁC SỸ");
            name.setGravity(Gravity.CENTER);
            AL.setEnabled(false);
            BG.setEnabled(false);
            CA.setEnabled(false);
            CN.setEnabled(false);
            AW.setEnabled(false);
            AS.setEnabled(false);
        } else {
            DatabaseReference userInfo = database.getReference("User").child(key);
            userInfo.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    name.setText("Hồ sơ bác sỹ " + dataSnapshot.child("name").getValue().toString());
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
                AL.setHint(dataSnapshot.child("al").getValue().toString());
                BG.setHint(dataSnapshot.child("bg").getValue().toString());
                CN.setHint(dataSnapshot.child("cn").getValue().toString());
                CA.setHint(dataSnapshot.child("ca").getValue().toString());
                AW.setHint(dataSnapshot.child("aw").getValue().toString());
                AS.setHint(dataSnapshot.child("as").getValue().toString());


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (AL.getText().toString() != null)
                    mDatabase.child("al").setValue(AL.getText().toString());
                else mDatabase.child("al").setValue("Chưa có thông tin");

                if (BG.getText().toString() != null)
                    mDatabase.child("bg").setValue(BG.getText().toString());
                else mDatabase.child("bg").setValue("Chưa có thông tin");

                if (CN.getText().toString() != null)
                    mDatabase.child("cn").setValue(CN.getText().toString());
                else mDatabase.child("cn").setValue("Chưa có thông tin");

                if (CA.getText().toString() != null)
                    mDatabase.child("ca").setValue(CA.getText().toString());
                else mDatabase.child("ca").setValue("Chưa có thông tin");

                if (AW.getText().toString() != null)
                    mDatabase.child("aw").setValue(AW.getText().toString());
                else mDatabase.child("aw").setValue("Chưa có thông tin");

                if (AS.getText().toString() != null)
                    mDatabase.child("as").setValue(AS.getText().toString());
                else mDatabase.child("as").setValue("Chưa có thông tin");

                Toast.makeText(getContext(), "Hồ sơ đã được cập nhật thành công", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
