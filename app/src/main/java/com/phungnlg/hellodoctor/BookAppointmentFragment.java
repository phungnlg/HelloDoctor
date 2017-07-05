package com.phungnlg.hellodoctor;

import android.app.Dialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
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

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

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

@EFragment(R.layout.fragment_book_appointment)
public class BookAppointmentFragment extends Fragment {
    @ViewById(R.id.fragment_book_doctorname)
    protected TextView tvWelcome;
    @ViewById(R.id.fragment_book_time)
    protected TextView tvBookTime;
    @ViewById(R.id.fragment_book_btn_bronze)
    protected TextView bronze;
    @ViewById(R.id.fragment_book_btn_silver)
    protected TextView silver;
    @ViewById(R.id.fragment_book_btn_gold)
    protected TextView gold;
    @ViewById(R.id.fragment_book_btn_diamond)
    protected TextView diamond;
    @ViewById(R.id.fragment_book_iv_cover)
    protected ImageView coverPhoto;

    private String username;
    private Date mDate;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference clientNotificationDatabase;
    private DatabaseReference doctorNotificationDatabase;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+1:00"));
    private Date localtime = cal.getTime();
    private String outputPattern = "h:mm a dd-MM-yyyy";
    private String inputPattern = "yyyy-MM-dd HH:mm:ss";
    private SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
    private SimpleDateFormat outputformat = new SimpleDateFormat(outputPattern);
    @FragmentArg
    protected String doctorKey;
    @FragmentArg
    protected String doctorName;

    public static final String K = " không?";
    public static final String IS_READED = "isReaded";
    public static final String NOTIFICATION = "notification";
    public static final String L = " lúc ";
    public static final String TC = " thành công!";
    public static final String TIME = "time";

    public BookAppointmentFragment() {
    }

    @Click(R.id.fragment_book_time)
    public void setTvBookTime() {
        outputPattern = "h:mm a dd-MM-yyyy";
        inputPattern = "yyyy-MM-dd HH:mm:ss";
        inputFormat = new SimpleDateFormat(inputPattern);
        new SingleDateAndTimePickerDialog.Builder(getContext())
                .bottomSheet()
                .mustBeOnFuture()
                .listener(new SingleDateAndTimePickerDialog.Listener() {
                    @Override
                    public void onDateSelected(Date date) {
                        tvBookTime.setText(outputformat.format(date));
                        mDate = date;
                    }
                })
                .display();
    }

    @Click(R.id.fragment_book_btnSchedule)
    public void setBtnSchedule() {
        ScheduleFragment f = ScheduleFragment_.builder().isEditMode(false).key(doctorKey)
                                              .doctorName(doctorName).build();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out);
        ft.replace(R.id.book_appointment, f);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Click(R.id.fragment_book_btnProfile)
    public void setBtnProfile() {
        CVFragment f = CVFragment_.builder().isEditMode(false).key(doctorKey)
                                  .doctorName(doctorName).build();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out);
        ft.replace(R.id.book_appointment, f);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Click(R.id.fragment_book_ib_back)
    public void setBtnBack() {
        getActivity().getSupportFragmentManager().beginTransaction()
                     .setCustomAnimations(R.anim.push_right_in, R.anim.push_right_out)
                     .remove(BookAppointmentFragment.this).commit();
    }

    @Click(R.id.fragment_book_btn_bronze)
    public void setBronze() {
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
                        clientNotificationDatabase.child(TIME).setValue(outputformat.format(localtime));

                        doctorNotificationDatabase.child(IS_READED).setValue(false);
                        doctorNotificationDatabase.child(NOTIFICATION)
                                                  .setValue(username + " đã đặt lịch khám gói BRONZE lúc " +
                                                            tvBookTime.getText());
                        doctorNotificationDatabase.child(TIME).setValue(outputformat.format(localtime));

                        Toast.makeText(getContext(), R.string.appointment_success, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                })
                .setButtonsGravity(FancyAlertDialog.PanelGravity.CENTER)
                .build();
        alert.show();
    }

    @Click(R.id.fragment_book_btn_silver)
    public void setSilver() {
        clientNotificationDatabase.child(IS_READED).setValue(false);
        clientNotificationDatabase.child(NOTIFICATION).setValue(
                "Đặt lịch khám gói SILVER với bác sỹ " + doctorName + L + tvBookTime.getText() +
                TC);
        clientNotificationDatabase.child(TIME).setValue(outputformat.format(localtime));

        doctorNotificationDatabase.child(IS_READED).setValue(false);
        doctorNotificationDatabase.child(NOTIFICATION)
                                  .setValue(username + " đã đặt lịch khám gói SILVER lúc " +
                                            tvBookTime.getText());
        doctorNotificationDatabase.child(TIME).setValue(outputformat.format(localtime));

        Toast.makeText(getContext(), R.string.appointment_success, Toast.LENGTH_SHORT).show();
    }

    @Click(R.id.fragment_book_btn_gold)
    public void setGold() {
        clientNotificationDatabase.child(IS_READED).setValue(false);
        clientNotificationDatabase.child(NOTIFICATION).setValue(
                "Đặt lịch khám gói GOLD với bác sỹ " + doctorName + L + tvBookTime.getText() +
                TC);
        clientNotificationDatabase.child(TIME).setValue(outputformat.format(localtime));

        doctorNotificationDatabase.child(IS_READED).setValue(false);
        doctorNotificationDatabase.child(NOTIFICATION)
                                  .setValue(username + " đã đặt lịch khám gói GOLD lúc " +
                                            tvBookTime.getText());
        doctorNotificationDatabase.child(TIME).setValue(outputformat.format(localtime));

        Toast.makeText(getContext(), R.string.appointment_success, Toast.LENGTH_SHORT).show();
    }

    @Click(R.id.fragment_book_btn_diamond)
    public void setDiamond() {
        clientNotificationDatabase.child(IS_READED).setValue(false);
        clientNotificationDatabase.child(NOTIFICATION).setValue(
                "Đặt lịch khám gói DIAMOND với bác sỹ " + doctorName + L + tvBookTime.getText() +
                TC);
        clientNotificationDatabase.child(TIME).setValue(outputformat.format(localtime));

        doctorNotificationDatabase.child(IS_READED).setValue(false);
        doctorNotificationDatabase.child(NOTIFICATION).setValue(
                username + " " + getText(R.string.diamond_da_dat_lich_kham_luc) + " " +
                tvBookTime.getText());
        doctorNotificationDatabase.child(TIME).setValue(outputformat.format(localtime));

        Toast.makeText(getContext(), R.string.appointment_success, Toast.LENGTH_SHORT).show();
    }

    public void loadUserInfo() {
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

    public void loadDoctorInfo() {
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
        tvWelcome.setText(getText(R.string.welcome_to_doctor) + " " + doctorName);
    }

    @AfterViews
    public void init() {
        loadUserInfo();
        loadDoctorInfo();
        clientNotificationDatabase = database.getReference("Notifications").child(user.getUid()).push();
        doctorNotificationDatabase = database.getReference("Notifications").child(doctorKey).push();
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
