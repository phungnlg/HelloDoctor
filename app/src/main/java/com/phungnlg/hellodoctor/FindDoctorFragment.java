package com.phungnlg.hellodoctor;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.geniusforapp.fancydialog.FancyAlertDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.io.IOException;
//import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Phil on 07/05/2017.
 */

public class FindDoctorFragment extends Fragment {
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("message")
                                                          .child("user-doctor");
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private RecyclerView doctorList;
    private TextView tvLocation;
    private EditText etLocation;

    private Spinner spnMajor;

    private Query sortMajor;
    private ImageButton btnSearch;

    private Location location;
    private Geocoder geocoder;

    public FindDoctorFragment() {
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View VIEW = inflater.inflate(R.layout.fragment_find_doctor, container, false);
        tvLocation = (TextView) VIEW.findViewById(R.id.fragment_find_doctor_tv_location);
        etLocation = (EditText) VIEW.findViewById(R.id.fragment_find_doctor_et_location);

        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED) {

        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{
                                                      android.Manifest.permission.ACCESS_FINE_LOCATION,
                                                      android.Manifest.permission.ACCESS_COARSE_LOCATION},
                                              1);
        }

        LocationManager locationManager = (LocationManager) getActivity()
                .getSystemService(getContext().LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        //Vị trí hiện tại
        final Location LASTLOCATION = locationManager.getLastKnownLocation(
                locationManager.getBestProvider(criteria, false));
        List<android.location.Address> addresses = null;

        geocoder = new Geocoder(this.getContext(), Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(
                    LASTLOCATION.getLatitude(),
                    LASTLOCATION.getLongitude(),
                    1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
        }

        etLocation.setHint(addresses.get(0).getAddressLine(0) + ", " + addresses.get(0).getLocality());

        doctorList = (RecyclerView) VIEW.findViewById(R.id.fragment_find_doctor_list);
        doctorList.setHasFixedSize(true);
        doctorList.setNestedScrollingEnabled(false);
        doctorList.setLayoutManager(new LinearLayoutManager(this.getContext()));

        spnMajor = (Spinner) VIEW.findViewById(R.id.fragment_find_doctor_spn_major);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter
                .createFromResource(getContext(), R.array.major, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnMajor.setAdapter(adapter);

        btnSearch = (ImageButton) VIEW.findViewById(R.id.fragment_find_doctor_btn_search);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                sortMajor = mDatabase.orderByChild("major").equalTo(spnMajor.getSelectedItem().toString());

                FirebaseRecyclerAdapter<Doctor, dHolder> firebaseRecyclerAdapter
                        = new FirebaseRecyclerAdapter<Doctor, dHolder>(
                        Doctor.class,
                        R.layout.item_doctor,
                        dHolder.class,
                        //mDatabase.orderByChild("major").equalTo("Nhi khoa")
                        sortMajor
                ) {
                    @Override
                    protected void populateViewHolder(dHolder viewHolder, final Doctor model, int position) {
                        viewHolder.setName(model.getName());
                        viewHolder.setAddress(model.getAddress());
                        viewHolder.setBio("Bác sỹ " + model.getMajor() + " tại " + model.getWorkplace());
                        //viewHolder.setRating("  " + model.mobile);

                        try {
                            List<Address> doctorLocation = geocoder.getFromLocationName(
                                    String.valueOf(model.getAddress()), 1);
                            Address location = doctorLocation.get(0);
                            Double distance = distance(
                                    LASTLOCATION.getLatitude(),
                                    LASTLOCATION.getLongitude(),
                                    location.getLatitude(),
                                    location.getLongitude());
                            viewHolder.setRating("  " + Math.round(distance) + " km");
                        } catch (IOException e) {
                            //e.printStackTrace();
                        }

                        final String DOCTORKEY = getRef(position).getKey();
                        final String DOCTORNAME = model.getName();

                        final Bundle BUNDLE = new Bundle();
                        BUNDLE.putString("doctor_key", DOCTORKEY);
                        BUNDLE.putString("doctor_name", DOCTORNAME);

                        viewHolder.view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                BookAppointmentFragment f = new BookAppointmentFragment();
                                f.setArguments(BUNDLE);
                                FragmentTransaction ft = getFragmentManager().beginTransaction();
                                ft.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out);
                                ft.replace(R.id.find_doctor, f);
                                ft.addToBackStack(null);
                                ft.commit();
                            }
                        });

                        viewHolder.btnCall.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                FancyAlertDialog.Builder alert = new FancyAlertDialog.Builder(getContext())
                                        .setImageRecourse(R.drawable.ic_test)
                                        .setTextSubTitle(model.getMobile())
                                        .setBody("Gọi cho bác sĩ " + model.getName())
                                        .setNegativeColor(R.color.jet)
                                        .setNegativeButtonText("Để sau")
                                        .setOnNegativeClicked(new FancyAlertDialog.OnNegativeClicked() {
                                            @Override
                                            public void OnClick(View view, Dialog dialog) {
                                                dialog.dismiss();
                                            }
                                        })
                                        .setPositiveButtonText("Gọi")
                                        .setPositiveColor(R.color.themecolor)
                                        .setOnPositiveClicked(new FancyAlertDialog.OnPositiveClicked() {
                                            @Override
                                            public void OnClick(View view, Dialog dialog) {
                                                Intent intent = new Intent(Intent.ACTION_CALL);
                                                intent.setData(Uri.parse("tel:" + model.getMobile()));

                                                if (ActivityCompat.checkSelfPermission(getContext(),
                                                                                       android.Manifest.permission.CALL_PHONE) !=
                                                    PackageManager.PERMISSION_GRANTED) {
                                                    return;
                                                } else {
                                                    startActivity(intent);
                                                }
                                            }
                                        })
                                        .setButtonsGravity(FancyAlertDialog.PanelGravity.CENTER)
                                        .build();
                                alert.show();
                            }
                        });

                        viewHolder.btnSchedule.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Bundle bundle = new Bundle();
                                bundle.putBoolean("isEditMode", false);
                                bundle.putString("key", DOCTORKEY);
                                bundle.putString("doctorName", DOCTORNAME);

                                ScheduleFragment f = new ScheduleFragment();
                                f.setArguments(bundle);
                                FragmentTransaction ft = getFragmentManager().beginTransaction();
                                ft.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out);
                                ft.replace(R.id.find_doctor, f);
                                ft.addToBackStack(null);
                                ft.commit();
                            }
                        });

                        viewHolder.btnWorkingExperience.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Bundle bundle = new Bundle();
                                bundle.putBoolean("isEditMode", false);
                                bundle.putString("key", DOCTORKEY);
                                bundle.putString("doctorName", DOCTORNAME);

                                CVFragment f = new CVFragment();
                                f.setArguments(bundle);
                                FragmentTransaction ft = getFragmentManager().beginTransaction();
                                ft.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out);
                                ft.replace(R.id.find_doctor, f);
                                ft.addToBackStack(null);
                                ft.commit();
                            }
                        });
                    }
                };
                doctorList.setAdapter(firebaseRecyclerAdapter);
            }
        });

        return VIEW;
    }

    public static class dHolder extends RecyclerView.ViewHolder {
        private View view;
        private ImageButton btnCall;
        private ImageButton btnMessage;
        private ImageButton btnSchedule;
        private ImageButton btnWorkingExperience;

        public dHolder(View itemView) {
            super(itemView);
            view = itemView;
            btnCall = (ImageButton) itemView.findViewById(R.id.item_doctor_ib_call);
            btnMessage = (ImageButton) itemView.findViewById(R.id.item_doctor_ib_message);
            btnSchedule = (ImageButton) itemView.findViewById(R.id.item_doctor_ib_schedule);
            btnWorkingExperience = (ImageButton) itemView.findViewById(R.id.item_doctor_ib_working_experience);
        }

        public void setName(String _name) {
            TextView name = (TextView) view.findViewById(R.id.item_doctor_tv_name);
            name.setText(_name);
        }

        public void setBio(String _bio) {
            TextView bio = (TextView) view.findViewById(R.id.item_doctor_tv_description);
            bio.setText(_bio);
        }

        public void setAddress(String _a) {
            TextView add = (TextView) view.findViewById(R.id.item_doctor_tv_address);
            add.setText(_a);
        }

        public void setRating(String _mobile) {
            TextView mobile = (TextView) view.findViewById(R.id.item_doctor_tv_rating);
            mobile.setText(_mobile);
        }
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                      * Math.sin(deg2rad(lat2))
                      + Math.cos(deg2rad(lat1))
                        * Math.cos(deg2rad(lat2))
                        * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
}
