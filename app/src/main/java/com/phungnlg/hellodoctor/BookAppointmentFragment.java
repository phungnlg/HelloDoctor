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
    private String doctor_key, doctor_name;
    TextView welcome;
    TextView book_time;

    TextView bronze, silver, gold, diamond;

    String username;

    Date mDate;

    AppCompatButton profile, schedule;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference client_noti;
    DatabaseReference doctor_noti;
    FirebaseUser user;

    public BookAppointmentFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        if(bundle != null) {
            doctor_key = bundle.getString("doctor_key");
            doctor_name = bundle.getString("doctor_name");
        }
        user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference userInfo = database.getReference("User").child(user.getUid());
        userInfo.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                username = (String)dataSnapshot.child("name").getValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_book_appointment, container, false);

        schedule = (AppCompatButton) view.findViewById(R.id.book_btnSchedule);
        profile = (AppCompatButton) view.findViewById(R.id.book_btnProfile);

        welcome = (TextView) view.findViewById(R.id.book_doctorname);
        welcome.setText("Chào mừng bạn đến với phòng mạch bác sỹ "+doctor_name);

        final int myColor = getResources().getColor(R.color.themecolor);

        String outputPattern = "h:mm a dd-MM-yyyy";
        String inputPattern = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        final SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        book_time = (TextView) view.findViewById(R.id.book_time);
        book_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SingleDateAndTimePickerDialog.Builder(getContext())
                        .bottomSheet()
                        .mustBeOnFuture()
                        .listener(new SingleDateAndTimePickerDialog.Listener() {
                            @Override
                            public void onDateSelected(Date date) {
                                book_time.setText(outputFormat.format(date));
                                mDate = date;

                            }
                        })
                        .display();
            }
        });

        bronze = (TextView) view.findViewById(R.id.book_bronze);
        silver = (TextView) view.findViewById(R.id.book_silver);
        gold = (TextView) view.findViewById(R.id.book_gold);
        diamond = (TextView) view.findViewById(R.id.book_diamond);

        client_noti = database.getReference("Notifications").child(user.getUid()).push();
        doctor_noti = database.getReference("Notifications").child(doctor_key).push();

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+1:00"));
        final Date currentLocalTime = cal.getTime();

        schedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putBoolean("isEditMode", false);
                bundle.putString("key", doctor_key);

                ScheduleFragment f = new ScheduleFragment();
                f.setArguments(bundle);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out);
                ft.replace(R.id.book_appointment, f);
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putBoolean("isEditMode", false);
                bundle.putString("key", doctor_key);

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
                b.setTitle("Xác nhận lịch khám?");
                b.setMessage("Bạn có muốn đặt gói khám BRONZE, vào lúc " + book_time.getText() + " không?" );
                b.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        client_noti.child("isReaded").setValue(false);
                        client_noti.child("notification").setValue("Đặt lịch khám gói BRONZE với bác sỹ " + doctor_name + " lúc " + book_time.getText() + " thành công!");
                        client_noti.child("time").setValue(outputFormat.format(currentLocalTime));

                        doctor_noti.child("isReaded").setValue(false);
                        doctor_noti.child("notification").setValue(username + " đã đặt lịch khám gói BRONZE lúc " + book_time.getText());
                        doctor_noti.child("time").setValue(outputFormat.format(currentLocalTime));

                        Toast.makeText(getContext(), "Đặt lịch khám thành công!", Toast.LENGTH_SHORT).show();
                    }
                });
                b.create().show();
            }
        });
        silver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder b = new AlertDialog.Builder(getContext());
                b.setTitle("Xác nhận lịch khám?");
                b.setMessage("Bạn có muốn đặt gói khám SILVER, vào lúc " + book_time.getText() + " không?" );
                b.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        client_noti.child("isReaded").setValue(false);
                        client_noti.child("notification").setValue("Đặt lịch khám gói SILVER với bác sỹ " + doctor_name + " lúc " + book_time.getText() + " thành công!");
                        client_noti.child("time").setValue(outputFormat.format(currentLocalTime));

                        doctor_noti.child("isReaded").setValue(false);
                        doctor_noti.child("notification").setValue(username + " đã đặt lịch khám gói SILVER lúc " + book_time.getText());
                        doctor_noti.child("time").setValue(outputFormat.format(currentLocalTime));

                        Toast.makeText(getContext(), "Đặt lịch khám thành công!", Toast.LENGTH_SHORT).show();
                    }
                });
                b.create().show();
            }
        });
        gold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder b = new AlertDialog.Builder(getContext());
                b.setTitle("Xác nhận lịch khám?");
                b.setMessage("Bạn có muốn đặt gói khám GOLD, vào lúc " + book_time.getText() + " không?" );
                b.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        client_noti.child("isReaded").setValue(false);
                        client_noti.child("notification").setValue("Đặt lịch khám gói GOLD với bác sỹ " + doctor_name + " lúc " + book_time.getText() + " thành công!");
                        client_noti.child("time").setValue(outputFormat.format(currentLocalTime));

                        doctor_noti.child("isReaded").setValue(false);
                        doctor_noti.child("notification").setValue(username + " đã đặt lịch khám gói GOLD lúc " + book_time.getText());
                        doctor_noti.child("time").setValue(outputFormat.format(currentLocalTime));

                        Toast.makeText(getContext(), "Đặt lịch khám thành công!", Toast.LENGTH_SHORT).show();
                    }
                });
                b.create().show();
            }
        });
        diamond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder b = new AlertDialog.Builder(getContext());
                b.setTitle("Xác nhận lịch khám?");
                b.setMessage("Bạn có muốn đặt gói khám DIAMOND, vào lúc " + book_time.getText() + " không?" );
                b.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        client_noti.child("isReaded").setValue(false);
                        client_noti.child("notification").setValue("Đặt lịch khám gói DIAMOND với bác sỹ " + doctor_name + " lúc " + book_time.getText() + " thành công!");
                        client_noti.child("time").setValue(outputFormat.format(currentLocalTime));

                        doctor_noti.child("isReaded").setValue(false);
                        doctor_noti.child("notification").setValue(username + " đã đặt lịch khám gói DIAMOND lúc " + book_time.getText());
                        doctor_noti.child("time").setValue(outputFormat.format(currentLocalTime));

                        Toast.makeText(getContext(), "Đặt lịch khám thành công!", Toast.LENGTH_SHORT).show();
                    }
                });
                b.create().show();
            }
        });
        return view;
    }
}
