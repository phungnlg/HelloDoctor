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

    private TextView tvName;
    private EditText edMonday;
    private EditText edTuesday;
    private EditText edWednesday;
    private EditText edThursday;
    private EditText edFriday;
    private EditText edSaturday;
    private EditText edSunday;
    private EditText edMonday2;
    private EditText edTuesday2;
    private EditText edWednesday2;
    private EditText edThursday2;
    private EditText edFriday2;
    private EditText edSaturday2;
    private EditText edSunday2;
    private ImageButton btnSave;
    private Boolean isEditMode;
    private String key;
    private String doctorName;

    public ScheduleFragment() {
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
        final View VIEW = inflater.inflate(R.layout.fragment_doctor_schedule, container, false);

        tvName = (TextView) VIEW.findViewById(R.id.fragment_schedule_tv_username);
        btnSave = (ImageButton) VIEW.findViewById(R.id.fragment_schedule_btn_Save);
        edMonday = (EditText) VIEW.findViewById(R.id.fragment_schedule_et_mon);
        edTuesday = (EditText) VIEW.findViewById(R.id.fragment_schedule_et_tue);
        edWednesday = (EditText) VIEW.findViewById(R.id.fragment_schedule_et_wed);
        edThursday = (EditText) VIEW.findViewById(R.id.fragment_schedule_et_thu);
        edFriday = (EditText) VIEW.findViewById(R.id.fragment_schedule_et_fri);
        edSaturday = (EditText) VIEW.findViewById(R.id.fragment_schedule_et_sat);
        edSunday = (EditText) VIEW.findViewById(R.id.fragment_schedule_et_sun);
        edMonday2 = (EditText) VIEW.findViewById(R.id.fragment_schedule_et_mon2);
        edTuesday2 = (EditText) VIEW.findViewById(R.id.fragment_schedule_et_tue2);
        edWednesday2 = (EditText) VIEW.findViewById(R.id.fragment_schedule_et_wed2);
        edThursday2 = (EditText) VIEW.findViewById(R.id.fragment_schedule_et_thu2);
        edFriday2 = (EditText) VIEW.findViewById(R.id.fragment_schedule_et_fri2);
        edSaturday2 = (EditText) VIEW.findViewById(R.id.fragment_schedule_et_sat2);
        edSunday2 = (EditText) VIEW.findViewById(R.id.fragment_schedule_et_sun2);

        if (!isEditMode) {
            tvName.setText(getText(R.string.sche_1) + " - BS " + doctorName);
            tvName.setGravity(Gravity.CENTER);
            btnSave.setVisibility(View.GONE);
            edMonday.setEnabled(false);
            edMonday2.setEnabled(false);
            edTuesday.setEnabled(false);
            edTuesday2.setEnabled(false);
            edWednesday.setEnabled(false);
            edWednesday2.setEnabled(false);
            edThursday.setEnabled(false);
            edThursday2.setEnabled(false);
            edFriday.setEnabled(false);
            edFriday2.setEnabled(false);
            edSaturday.setEnabled(false);
            edSaturday2.setEnabled(false);
            edSunday.setEnabled(false);
            edSunday2.setEnabled(false);
        } else {
            DatabaseReference userInfo = database.getReference("User").child(key);
            userInfo.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    tvName.setText(
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
                edMonday.setText(dataSnapshot.child("Mon").child("from").getValue().toString());
                edMonday2.setText(dataSnapshot.child("Mon").child("to").getValue().toString());
                edTuesday.setText(dataSnapshot.child("Tue").child("from").getValue().toString());
                edTuesday2.setText(dataSnapshot.child("Tue").child("to").getValue().toString());
                edWednesday.setText(dataSnapshot.child("Wed").child("from").getValue().toString());
                edWednesday2.setText(dataSnapshot.child("Wed").child("to").getValue().toString());
                edThursday.setText(dataSnapshot.child("Thu").child("from").getValue().toString());
                edThursday2.setText(dataSnapshot.child("Thu").child("to").getValue().toString());
                edFriday.setText(dataSnapshot.child("Fri").child("from").getValue().toString());
                edFriday2.setText(dataSnapshot.child("Fri").child("to").getValue().toString());
                edSaturday.setText(dataSnapshot.child("Sat").child("from").getValue().toString());
                edSaturday2.setText(dataSnapshot.child("Sat").child("to").getValue().toString());
                edSunday.setText(dataSnapshot.child("Sun").child("from").getValue().toString());
                edSunday2.setText(dataSnapshot.child("Sun").child("to").getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatabase.child("Mon").child("from").setValue(edMonday.getText().toString());
                mDatabase.child("Mon").child("to").setValue(edMonday2.getText().toString());
                mDatabase.child("Tue").child("from").setValue(edTuesday.getText().toString());
                mDatabase.child("Tue").child("to").setValue(edTuesday2.getText().toString());
                mDatabase.child("Wed").child("from").setValue(edWednesday.getText().toString());
                mDatabase.child("Wed").child("to").setValue(edWednesday2.getText().toString());
                mDatabase.child("Thu").child("from").setValue(edThursday.getText().toString());
                mDatabase.child("Thu").child("to").setValue(edThursday2.getText().toString());
                mDatabase.child("Fri").child("from").setValue(edFriday.getText().toString());
                mDatabase.child("Fri").child("to").setValue(edFriday2.getText().toString());
                mDatabase.child("Sat").child("from").setValue(edSaturday.getText().toString());
                mDatabase.child("Sat").child("to").setValue(edSaturday2.getText().toString());
                mDatabase.child("Sun").child("from").setValue(edSunday.getText().toString());
                mDatabase.child("Sun").child("to").setValue(edSunday2.getText().toString());

                Toast.makeText(getContext(), R.string.sche_update_successfully, Toast.LENGTH_SHORT).show();
            }
        });


        return VIEW;
    }
}
