package com.phungnlg.hellodoctor;

//import android.content.DialogInterface;
import android.app.Dialog;
import android.content.Intent;
//import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
//import android.os.ParcelUuid;
//import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
//import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
//import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.geniusforapp.fancydialog.FancyAlertDialog;
import com.github.florent37.singledateandtimepicker.dialog.SingleDateAndTimePickerDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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
    private ImageView coverPhoto;

    public static final String K = " không?";
    public static final String IS_READED = "isReaded";
    public static final String NOTIFICATION = "notification";
    public static final String L = " lúc ";
    public static final String TC = " thành công!";
    public static final String TIME = "time";

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
        coverPhoto = (ImageView) view.findViewById(R.id.fragment_book_iv_cover);

        database.getReference("message").child("user-doctor").child(doctorKey).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String DOCTORADDRESS = dataSnapshot.child("address").getValue().toString();
                        setCoverPhoto(getMapUrl(DOCTORADDRESS));

                        coverPhoto.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + DOCTORADDRESS);
                                //Uri gmmIntentUri = Uri.parse("google.direction:q=" + DOCTORADDRESS);
                                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                                mapIntent.setPackage("com.google.android.apps.maps");
                                startActivity(mapIntent);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        tvWelcome = (TextView) view.findViewById(R.id.fragment_book_doctorname);
        tvWelcome.setText(getText(R.string.welcome_to_doctor) + " " + doctorName);

        String outputPattern = "h:mm a dd-MM-yyyy";
        String inputPattern = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        final SimpleDateFormat OUTPUTFORMAT = new SimpleDateFormat(outputPattern);

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
                                tvBookTime.setText(OUTPUTFORMAT.format(date));
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
        final Date LOCALTIME = cal.getTime();

        btnSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Bundle bundle = new Bundle();
                bundle.putBoolean("isEditMode", false);
                bundle.putString("key", doctorKey);
                bundle.putString("doctorName", doctorName);*/

                ScheduleFragment f = ScheduleFragment_.builder().isEditMode(false).key(doctorKey)
                                                      .doctorName(doctorName).build();
                //f.setArguments(bundle);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out);
                ft.replace(R.id.book_appointment, f);
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        view.findViewById(R.id.fragment_book_ib_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().beginTransaction()
                             .setCustomAnimations(R.anim.push_right_in, R.anim.push_right_out)
                             .remove(BookAppointmentFragment.this).commit();
            }
        });

        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Bundle bundle = new Bundle();
                bundle.putBoolean("isEditMode", false);
                bundle.putString("key", doctorKey);
                bundle.putString("doctorName", doctorName);*/

                CVFragment f = CVFragment_.builder().isEditMode(false).key(doctorKey)
                                          .doctorName(doctorName).build();
                //f.setArguments(bundle);
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

                FancyAlertDialog.Builder alert = new FancyAlertDialog.Builder(getContext())
                        .setImageRecourse(R.drawable.ic_vector_schedule)
                        .setTextSubTitle("Gói BRONZE")
                        .setBody("Bạn có muốn đặt gói khám SILVER, vào lúc " + tvBookTime.getText() + K)
                        .setNegativeColor(R.color.jet)
                        .setNegativeButtonText("Để sau")
                        .setOnNegativeClicked(new FancyAlertDialog.OnNegativeClicked() {
                            @Override
                            public void OnClick(View view, Dialog dialog) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButtonText("Đặt lịch")
                        .setPositiveColor(R.color.themecolor)
                        .setOnPositiveClicked(new FancyAlertDialog.OnPositiveClicked() {
                            @Override
                            public void OnClick(View view, Dialog dialog) {
                                clientNotificationDatabase.child(IS_READED).setValue(false);
                                clientNotificationDatabase.child(NOTIFICATION).setValue(
                                        "Đặt lịch khám gói BRONZE với bác sỹ " + doctorName + L + tvBookTime.getText() +
                                        TC);
                                clientNotificationDatabase.child(TIME).setValue(OUTPUTFORMAT.format(LOCALTIME));

                                doctorNotificationDatabase.child(IS_READED).setValue(false);
                                doctorNotificationDatabase.child(NOTIFICATION)
                                                          .setValue(username + " đã đặt lịch khám gói BRONZE lúc " +
                                                                    tvBookTime.getText());
                                doctorNotificationDatabase.child(TIME).setValue(OUTPUTFORMAT.format(LOCALTIME));

                                Toast.makeText(getContext(), R.string.appointment_success, Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        })
                        .setButtonsGravity(FancyAlertDialog.PanelGravity.CENTER)
                        .build();
                alert.show();
            }
        });



        silver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                clientNotificationDatabase.child(IS_READED).setValue(false);
                clientNotificationDatabase.child(NOTIFICATION).setValue(
                        "Đặt lịch khám gói SILVER với bác sỹ " + doctorName + L + tvBookTime.getText() +
                        TC);
                clientNotificationDatabase.child(TIME).setValue(OUTPUTFORMAT.format(LOCALTIME));

                doctorNotificationDatabase.child(IS_READED).setValue(false);
                doctorNotificationDatabase.child(NOTIFICATION)
                                          .setValue(username + " đã đặt lịch khám gói SILVER lúc " +
                                                    tvBookTime.getText());
                doctorNotificationDatabase.child(TIME).setValue(OUTPUTFORMAT.format(LOCALTIME));

                Toast.makeText(getContext(), R.string.appointment_success, Toast.LENGTH_SHORT).show();
            }
        });
        gold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                clientNotificationDatabase.child(IS_READED).setValue(false);
                clientNotificationDatabase.child(NOTIFICATION).setValue(
                        "Đặt lịch khám gói GOLD với bác sỹ " + doctorName + L + tvBookTime.getText() +
                        TC);
                clientNotificationDatabase.child(TIME).setValue(OUTPUTFORMAT.format(LOCALTIME));

                doctorNotificationDatabase.child(IS_READED).setValue(false);
                doctorNotificationDatabase.child(NOTIFICATION)
                                          .setValue(username + " đã đặt lịch khám gói GOLD lúc " +
                                                    tvBookTime.getText());
                doctorNotificationDatabase.child(TIME).setValue(OUTPUTFORMAT.format(LOCALTIME));

                Toast.makeText(getContext(), R.string.appointment_success, Toast.LENGTH_SHORT).show();
            }
        });
        diamond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                clientNotificationDatabase.child(IS_READED).setValue(false);
                clientNotificationDatabase.child(NOTIFICATION).setValue(
                        "Đặt lịch khám gói DIAMOND với bác sỹ " + doctorName + L + tvBookTime.getText() +
                        TC);
                clientNotificationDatabase.child(TIME).setValue(OUTPUTFORMAT.format(LOCALTIME));

                doctorNotificationDatabase.child(IS_READED).setValue(false);
                doctorNotificationDatabase.child(NOTIFICATION).setValue(
                        username + " " + getText(R.string.diamond_da_dat_lich_kham_luc) + " " +
                        tvBookTime.getText());
                doctorNotificationDatabase.child(TIME).setValue(OUTPUTFORMAT.format(LOCALTIME));

                Toast.makeText(getContext(), R.string.appointment_success, Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }

    public void setCoverPhoto (String url) {
        Picasso.with(getContext())
               .load(url)
               .centerCrop()
               .resize(400, 400)
               .into(coverPhoto);
    }

    public String getMapUrl(String addressName) {
        String url = null;
        try {
            Geocoder geocoder = new Geocoder(this.getContext(), Locale.getDefault());
            List<Address> doctorLocation = geocoder.getFromLocationName(addressName, 1);
            Address location = doctorLocation.get(0);
            url = "http://maps.google.com/maps/api/staticmap?center=" + location.getLatitude() + "," +
                  location.getLongitude() + "&zoom=15&size=400x400&maptype=roadmap&key";
        } catch (IOException e) {
            //e.printStackTrace();
        }
        return url;
    }
}
