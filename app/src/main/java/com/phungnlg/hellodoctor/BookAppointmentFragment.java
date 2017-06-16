package com.phungnlg.hellodoctor;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Phil on 07/05/2017.
 */

public class BookAppointmentFragment extends Fragment {
    private TextView tvWelcome;
    private TextView tvBookTime;
    private TextView bronze;
    private TextView silver;
    private TextView gold;
    private TextView diamond;
    private String username;
    private Date mDate;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference clientNotificationDatabase;
    private DatabaseReference doctorNotificationDatabase;
    private FirebaseUser user;
    private String doctorKey;
    private String doctorName;

    public BookAppointmentFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            doctorKey = bundle.getString("doctor_key");
            doctorName = bundle.getString("doctor_name");
        }
        user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference userInfo = database.getReference("User").child(user.getUid());
        userInfo.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                username = (String) dataSnapshot.child("name").getValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_appointment, container, false);

        AppCompatButton btnProfile, btnSchedule;
        btnSchedule = (AppCompatButton) view.findViewById(R.id.fragment_book_btnSchedule);
        btnProfile = (AppCompatButton) view.findViewById(R.id.fragment_book_btnProfile);

        tvWelcome = (TextView) view.findViewById(R.id.fragment_book_doctorname);
        tvWelcome.setText(getText(R.string.welcome_to_doctor) + " " + doctorName);

        String outputPattern = "h:mm a dd-MM-yyyy";
        String inputPattern = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        final SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        tvBookTime = (TextView) view.findViewById(R.id.fragment_book_time);
        tvBookTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SingleDateAndTimePickerDialog.Builder(getContext())
                        .bottomSheet()
                        .mustBeOnFuture()
                        .listener(new SingleDateAndTimePickerDialog.Listener() {
                            @Override
                            public void onDateSelected(Date date) {
                                tvBookTime.setText(outputFormat.format(date));
                                mDate = date;
                            }
                        })
                        .display();
            }
        });

        bronze = (TextView) view.findViewById(R.id.fragment_book_btn_bronze);
        silver = (TextView) view.findViewById(R.id.fragment_book_btn_silver);
        gold = (TextView) view.findViewById(R.id.fragment_book_btn_gold);
        diamond = (TextView) view.findViewById(R.id.fragment_book_btn_diamond);

        clientNotificationDatabase = database.getReference("Notifications").child(user.getUid()).push();
        doctorNotificationDatabase = database.getReference("Notifications").child(doctorKey).push();

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+1:00"));
        final Date CURRENT_LOCAL_TIME = cal.getTime();

        btnSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putBoolean("isEditMode", false);
                bundle.putString("key", doctorKey);
                bundle.putString("doctorName", doctorName);

                ScheduleFragment f = new ScheduleFragment();
                f.setArguments(bundle);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out);
                ft.replace(R.id.book_appointment, f);
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putBoolean("isEditMode", false);
                bundle.putString("key", doctorKey);
                bundle.putString("doctorName", doctorName);

                CVFragment f = new CVFragment();
                f.setArguments(bundle);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out);
                ft.replace(R.id.book_appointment, f);
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        bronze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder b = new AlertDialog.Builder(getContext());
                b.setTitle(R.string.confirm_appointment);
                b.setMessage("Bạn có muốn đặt gói khám BRONZE, vào lúc " + tvBookTime.getText() + " không?");
                b.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        clientNotificationDatabase.child("isReaded").setValue(false);
                        clientNotificationDatabase.child("notification").setValue(
                                "Đặt lịch khám gói BRONZE với bác sỹ " + doctorName + " lúc " + tvBookTime.getText() +
                                " thành công!");
                        clientNotificationDatabase.child("time").setValue(outputFormat.format(CURRENT_LOCAL_TIME));

                        doctorNotificationDatabase.child("isReaded").setValue(false);
                        doctorNotificationDatabase.child("notification")
                                                  .setValue(username + " đã đặt lịch khám gói BRONZE lúc " +
                                                            tvBookTime.getText());
                        doctorNotificationDatabase.child("time").setValue(outputFormat.format(CURRENT_LOCAL_TIME));

                        Toast.makeText(getContext(), R.string.appointment_success, Toast.LENGTH_SHORT).show();
                    }
                });
                b.create().show();
            }
        });
        silver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder b = new AlertDialog.Builder(getContext());
                b.setTitle(R.string.confirm_appointment);
                b.setMessage("Bạn có muốn đặt gói khám SILVER, vào lúc " + tvBookTime.getText() + " không?");
                b.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        clientNotificationDatabase.child("isReaded").setValue(false);
                        clientNotificationDatabase.child("notification").setValue(
                                "Đặt lịch khám gói SILVER với bác sỹ " + doctorName + " lúc " + tvBookTime.getText() +
                                " thành công!");
                        clientNotificationDatabase.child("time").setValue(outputFormat.format(CURRENT_LOCAL_TIME));

                        doctorNotificationDatabase.child("isReaded").setValue(false);
                        doctorNotificationDatabase.child("notification")
                                                  .setValue(username + " đã đặt lịch khám gói SILVER lúc " +
                                                            tvBookTime.getText());
                        doctorNotificationDatabase.child("time").setValue(outputFormat.format(CURRENT_LOCAL_TIME));

                        Toast.makeText(getContext(), R.string.appointment_success, Toast.LENGTH_SHORT).show();
                    }
                });
                b.create().show();
            }
        });
        gold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder b = new AlertDialog.Builder(getContext());
                b.setTitle(R.string.confirm_appointment);
                b.setMessage("Bạn có muốn đặt gói khám GOLD, vào lúc " + tvBookTime.getText() + " không?");
                b.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        clientNotificationDatabase.child("isReaded").setValue(false);
                        clientNotificationDatabase.child("notification").setValue(
                                "Đặt lịch khám gói GOLD với bác sỹ " + doctorName + " lúc " + tvBookTime.getText() +
                                " thành công!");
                        clientNotificationDatabase.child("time").setValue(outputFormat.format(CURRENT_LOCAL_TIME));

                        doctorNotificationDatabase.child("isReaded").setValue(false);
                        doctorNotificationDatabase.child("notification")
                                                  .setValue(username + " đã đặt lịch khám gói GOLD lúc " +
                                                            tvBookTime.getText());
                        doctorNotificationDatabase.child("time").setValue(outputFormat.format(CURRENT_LOCAL_TIME));

                        Toast.makeText(getContext(), R.string.appointment_success, Toast.LENGTH_SHORT).show();
                    }
                });
                b.create().show();
            }
        });
        diamond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder b = new AlertDialog.Builder(getContext());
                b.setTitle(R.string.confirm_appointment);
                b.setMessage("Bạn có muốn đặt gói khám DIAMOND, vào lúc " + tvBookTime.getText() + " không?");
                b.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        clientNotificationDatabase.child("isReaded").setValue(false);
                        clientNotificationDatabase.child("notification").setValue(
                                "Đặt lịch khám gói DIAMOND với bác sỹ " + doctorName + " lúc " + tvBookTime.getText() +
                                " thành công!");
                        clientNotificationDatabase.child("time").setValue(outputFormat.format(CURRENT_LOCAL_TIME));

                        doctorNotificationDatabase.child("isReaded").setValue(false);
                        doctorNotificationDatabase.child("notification").setValue(
                                username + " " + getText(R.string.diamond_da_dat_lich_kham_luc) + " " +
                                tvBookTime.getText());
                        doctorNotificationDatabase.child("time").setValue(outputFormat.format(CURRENT_LOCAL_TIME));

                        Toast.makeText(getContext(), R.string.appointment_success, Toast.LENGTH_SHORT).show();
                    }
                });
                b.create().show();
            }
        });
        return view;
    }
}
