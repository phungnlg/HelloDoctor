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

public class ScheduleFragment extends Fragment {
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Schedule").child(user.getUid());
    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    TextView name;
    EditText mon, tue, wed, thu, fri, sat, sun;
    EditText mon2, tue2, wed2, thu2, fri2, sat2, sun2;
    ImageButton btnSave;
    Boolean isEditMode;
    String key;

    public ScheduleFragment() {
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
        final View view = inflater.inflate(R.layout.tab_schedule, container, false);

        name = (TextView) view.findViewById(R.id.sche_username);
        btnSave = (ImageButton) view.findViewById(R.id.sche_btnSave);
        mon = (EditText) view.findViewById(R.id.sche_mon);
        tue = (EditText) view.findViewById(R.id.sche_tue);
        wed = (EditText) view.findViewById(R.id.sche_wed);
        thu = (EditText) view.findViewById(R.id.sche_thu);
        fri = (EditText) view.findViewById(R.id.sche_fri);
        sat = (EditText) view.findViewById(R.id.sche_sat);
        sun = (EditText) view.findViewById(R.id.sche_sun);
        mon2 = (EditText) view.findViewById(R.id.sche_mon2);
        tue2 = (EditText) view.findViewById(R.id.sche_tue2);
        wed2 = (EditText) view.findViewById(R.id.sche_wed2);
        thu2 = (EditText) view.findViewById(R.id.sche_thu2);
        fri2 = (EditText) view.findViewById(R.id.sche_fri2);
        sat2 = (EditText) view.findViewById(R.id.sche_sat2);
        sun2 = (EditText) view.findViewById(R.id.sche_sun2);

        if (!isEditMode) {
            name.setText(R.string.sche_1);
            name.setGravity(Gravity.CENTER);
            btnSave.setVisibility(View.GONE);
            mon.setEnabled(false);
            mon2.setEnabled(false);
            tue.setEnabled(false);
            tue2.setEnabled(false);
            wed.setEnabled(false);
            wed2.setEnabled(false);
            thu.setEnabled(false);
            thu2.setEnabled(false);
            fri.setEnabled(false);
            fri2.setEnabled(false);
            sat.setEnabled(false);
            sat2.setEnabled(false);
            sun.setEnabled(false);
            sun2.setEnabled(false);
        } else {
            DatabaseReference userInfo = database.getReference("User").child(key);
            userInfo.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    name.setText(
                            "Chào " + dataSnapshot.child("name").getValue().toString() + ", hãy sắp xếp lịch của bạn");
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        DatabaseReference schedule = database.getReference("Schedule").child(key);
        schedule.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mon.setText(dataSnapshot.child("Mon").child("from").getValue().toString());
                mon2.setText(dataSnapshot.child("Mon").child("to").getValue().toString());
                tue.setText(dataSnapshot.child("Tue").child("from").getValue().toString());
                tue2.setText(dataSnapshot.child("Tue").child("to").getValue().toString());
                wed.setText(dataSnapshot.child("Wed").child("from").getValue().toString());
                wed2.setText(dataSnapshot.child("Wed").child("to").getValue().toString());
                thu.setText(dataSnapshot.child("Thu").child("from").getValue().toString());
                thu2.setText(dataSnapshot.child("Thu").child("to").getValue().toString());
                fri.setText(dataSnapshot.child("Fri").child("from").getValue().toString());
                fri2.setText(dataSnapshot.child("Fri").child("to").getValue().toString());
                sat.setText(dataSnapshot.child("Sat").child("from").getValue().toString());
                sat2.setText(dataSnapshot.child("Sat").child("to").getValue().toString());
                sun.setText(dataSnapshot.child("Sun").child("from").getValue().toString());
                sun2.setText(dataSnapshot.child("Sun").child("to").getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatabase.child("Mon").child("from").setValue(mon.getText().toString());
                mDatabase.child("Mon").child("to").setValue(mon2.getText().toString());
                mDatabase.child("Tue").child("from").setValue(tue.getText().toString());
                mDatabase.child("Tue").child("to").setValue(tue2.getText().toString());
                mDatabase.child("Wed").child("from").setValue(wed.getText().toString());
                mDatabase.child("Wed").child("to").setValue(wed2.getText().toString());
                mDatabase.child("Thu").child("from").setValue(thu.getText().toString());
                mDatabase.child("Thu").child("to").setValue(thu2.getText().toString());
                mDatabase.child("Fri").child("from").setValue(fri.getText().toString());
                mDatabase.child("Fri").child("to").setValue(fri2.getText().toString());
                mDatabase.child("Sat").child("from").setValue(sat.getText().toString());
                mDatabase.child("Sat").child("to").setValue(sat2.getText().toString());
                mDatabase.child("Sun").child("from").setValue(sun.getText().toString());
                mDatabase.child("Sun").child("to").setValue(sun2.getText().toString());

                Toast.makeText(getContext(), R.string.sche_update_successfully, Toast.LENGTH_SHORT).show();
            }
        });


        return view;
    }
}
