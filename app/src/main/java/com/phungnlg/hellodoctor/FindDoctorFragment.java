package com.phungnlg.hellodoctor;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
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
        final View view = inflater.inflate(R.layout.fragment_find_doctor, container, false);
        tvLocation = (TextView) view.findViewById(R.id.fragment_find_doctor_tv_location);
        etLocation = (EditText) view.findViewById(R.id.fragment_find_doctor_et_location);

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
        Location lastLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
        List<android.location.Address> addresses = null;

        geocoder = new Geocoder(this.getContext(), Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(
                    lastLocation.getLatitude(),
                    lastLocation.getLongitude(),
                    1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
        }

        etLocation.setHint(addresses.get(0).getAddressLine(0) + ", " + addresses.get(0).getLocality());

        doctorList = (RecyclerView) view.findViewById(R.id.fragment_find_doctor_list);
        doctorList.setHasFixedSize(true);
        doctorList.setNestedScrollingEnabled(false);
        doctorList.setLayoutManager(new LinearLayoutManager(this.getContext()));

        spnMajor = (Spinner) view.findViewById(R.id.fragment_find_doctor_spn_major);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter
                .createFromResource(getContext(), R.array.major, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnMajor.setAdapter(adapter);

        btnSearch = (ImageButton) view.findViewById(R.id.fragment_find_doctor_btn_search);

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
                        viewHolder.setName(model.name);
                        viewHolder.setAddress(model.address);
                        viewHolder.setBio("Bác sỹ " + model.major + " tại " + model.workplace);
                        viewHolder.setRating("  " + model.mobile);

                        final String DOCTOR_KEY = getRef(position).getKey();
                        final String DOCTOR_NAME = model.name;

                        final Bundle bundle = new Bundle();
                        bundle.putString("doctor_key", DOCTOR_KEY);
                        bundle.putString("doctor_name", DOCTOR_NAME);

                        viewHolder.view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                BookAppointmentFragment f = new BookAppointmentFragment();
                                f.setArguments(bundle);
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
                                        .setTextSubTitle(model.mobile)
                                        .setBody("Gọi cho bác sĩ " + model.name)
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
                                                intent.setData(Uri.parse("tel:" + model.mobile));

                                                if (ActivityCompat.checkSelfPermission(getContext(),
                                                                                       android.Manifest.permission.CALL_PHONE) !=
                                                    PackageManager.PERMISSION_GRANTED) {
                                                    return;
                                                } else startActivity(intent);
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
                                bundle.putString("key", DOCTOR_KEY);
                                bundle.putString("doctorName", DOCTOR_NAME);

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
                                bundle.putString("key", DOCTOR_KEY);
                                bundle.putString("doctorName", DOCTOR_NAME);

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

        return view;
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
}
