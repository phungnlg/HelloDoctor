package com.phungnlg.hellodoctor;

import android.app.Dialog;
import android.app.ProgressDialog;
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
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.geniusforapp.fancydialog.FancyAlertDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.phungnlg.hellodoctor.others.PlaceAutocompleteAdapter;
import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by Phil on 07/05/2017.
 */
@EFragment(R.layout.fragment_find_doctor)
public class FindDoctorFragment extends Fragment implements LocationSource.OnLocationChangedListener,
                                                            GoogleApiClient.OnConnectionFailedListener {
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("message")
                                                          .child("user-doctor");
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private StorageReference imageRef = FirebaseStorage.getInstance().getReference("avatar");

    @ViewById(R.id.fragment_find_doctor_list)
    protected RecyclerView doctorList;
    @ViewById(R.id.fragment_find_doctor_tv_location)
    protected TextView tvLocation;
    @ViewById(R.id.fragment_find_doctor_et_location)
    protected AutoCompleteTextView etLocation;
    @ViewById(R.id.fragment_find_doctor_spn_major)
    protected Spinner spnMajor;
    private GoogleApiClient mGoogleApiClient;

    private FirebaseRecyclerAdapter<Doctor, dHolder> adapter;
    private Location lastLocation;
    private LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext()) {
        @Override
        public void onLayoutChildren(final RecyclerView.Recycler recycler, final RecyclerView.State state) {
            super.onLayoutChildren(recycler, state);
            //TODO if the items are filtered, considered hiding the fast scroller here
            final int firstVisibleItemPosition = findFirstVisibleItemPosition();
            if (firstVisibleItemPosition != 0) {
                // this avoids trying to handle un-needed calls
                if (firstVisibleItemPosition == -1) {
                    //not initialized, or no items shown, so hide fast-scroller
                    showDialog();
                }
                return;
            }
            final int lastVisibleItemPosition = findLastVisibleItemPosition();
            int itemsShown = lastVisibleItemPosition - firstVisibleItemPosition + 1;
            //if all items are shown, hide the fast-scroller
            if (adapter.getItemCount() > itemsShown) {
                showDialog();
            } else {
                progressDialog.dismiss();
            }
        }
    };
    private Query sortMajor;
    private ImageButton btnSearch;
    private PlaceAutocompleteAdapter mAdapter;
    private static final LatLngBounds BOUNDS_GREATER_SYDNEY = new LatLngBounds(
            new LatLng(10.562400, 106.580979), new LatLng(10.998982, 106.699151));
    private ProgressDialog progressDialog;
    private Geocoder geocoder;

    public FindDoctorFragment() {
    }

    private void showDialog() {
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Đang tải danh sách");
        progressDialog.show();
    }

    @Click(R.id.fragment_find_doctor_ib_back)
    public void setBtnBack() {
        getActivity().getSupportFragmentManager().beginTransaction()
                     .setCustomAnimations(R.anim.push_right_in, R.anim.push_right_out)
                     .remove(FindDoctorFragment.this).commit();
    }

    @AfterViews
    public void init() {
        progressDialog = new ProgressDialog(getActivity(),
                                            R.style.AppTheme_Dark_Dialog);
        setEtAddress();
        setEtLocation();
        setSpnMajor();
    }

    public void setEtAddress() {
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .enableAutoManage(getActivity(), 0, FindDoctorFragment.this)
                .addApi(Places.GEO_DATA_API)
                .build();

        mAdapter = new PlaceAutocompleteAdapter(getContext(), mGoogleApiClient, BOUNDS_GREATER_SYDNEY,
                                                null);
        etLocation.setAdapter(mAdapter);
    }

    public void setEtLocation() {
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

        lastLocation = locationManager.getLastKnownLocation(
                LocationManager.NETWORK_PROVIDER);


        List<android.location.Address> addresses = null;

        geocoder = new Geocoder(this.getContext(), Locale.getDefault());
        if (lastLocation != null) {
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
        }
        etLocation.setHint(addresses.get(0).getAddressLine(0) + ", " + addresses.get(0).getLocality());
    }

    public void setSpnMajor() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter
                .createFromResource(getContext(), R.array.major, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnMajor.setAdapter(adapter);
    }

    @Click(R.id.fragment_find_doctor_btn_search)
    public void setBtnSearch() {
        sortMajor = mDatabase.orderByChild("major").equalTo(spnMajor.getSelectedItem().toString());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        adapter = new FirebaseRecyclerAdapter<Doctor, dHolder>(
                Doctor.class,
                R.layout.item_doctor,
                dHolder.class,
                sortMajor
        ) {
            @Override
            protected void populateViewHolder(final dHolder viewHolder, final Doctor model, int position) {
                viewHolder.setName(model.getName());
                viewHolder.setAddress(model.getAddress());
                viewHolder.setBio("Bác sỹ " + model.getMajor() + " tại " + model.getWorkplace());
                StorageReference avatar = imageRef.child(getRef(position).getKey() + ".jpg");
                avatar.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        viewHolder.setAvatar(uri.toString());
                    }
                });

                try {
                    List<Address> doctorLocation = geocoder.getFromLocationName(
                            String.valueOf(model.getAddress()), 1);
                    Address location = doctorLocation.get(0);
                    Double distance = distance(
                            lastLocation.getLatitude(),
                            lastLocation.getLongitude(),
                            location.getLatitude(),
                            location.getLongitude());
                    viewHolder.setRating("  " + Math.round(distance) + " km");
                    if (Math.round((distance)) > 5) {
                        //viewHolder.view.setVisibility(View.GONE);
                        viewHolder.hideView();
                    }
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
                        BookAppointmentFragment f = BookAppointmentFragment_
                                .builder().doctorName(DOCTORNAME).doctorKey(DOCTORKEY).build();
                        //f.setArguments(BUNDLE);
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out);
                        ft.replace(R.id.find_doctor, f);
                        ft.addToBackStack(null);
                        ft.commit();
                    }
                });

                viewHolder.btnMessage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putString("uid", DOCTORKEY);
                        bundle.putString("userName", DOCTORNAME);
                        ChatFragment chatFragment = new ChatFragment_();
                        chatFragment.setArguments(bundle);
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out);
                        ft.replace(R.id.find_doctor, chatFragment);
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

                                        if (ActivityCompat.checkSelfPermission(getContext()
                                                , android.Manifest.permission.CALL_PHONE) !=
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
                                /*Bundle bundle = new Bundle();
                                bundle.putBoolean("isEditMode", false);
                                bundle.putString("key", DOCTORKEY);
                                bundle.putString("doctorName", DOCTORNAME);*/

                        ScheduleFragment f = ScheduleFragment_.builder().isEditMode(false).key(DOCTORKEY)
                                                              .doctorName(DOCTORNAME).build();
                        //f.setArguments(bundle);
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

                        CVFragment f = CVFragment_.builder().isEditMode(false).key(DOCTORKEY)
                                                  .doctorName(DOCTORNAME).build();

                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out);
                        ft.replace(R.id.find_doctor, f);
                        ft.addToBackStack(null);
                        ft.commit();
                    }
                });
            }
        };
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(final int positionStart, final int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = adapter.getItemCount();
                int lastVisiblePosition =
                        layoutManager.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == -1
                    || (positionStart >= (friendlyMessageCount - 1)
                        && lastVisiblePosition == (positionStart - 1))) {
                    doctorList.scrollToPosition(positionStart);
                }
            }
        });
//        adapter.notifyAll();
        doctorList.setNestedScrollingEnabled(false);
        doctorList.setLayoutManager(layoutManager);
        doctorList.setAdapter(adapter);

        /*adapter.notifyDataSetChanged();
        showDialog();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
            }
        }, 3000);*/
    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.stopAutoManage(getActivity());
        mGoogleApiClient.disconnect();
        Log.d("Location P", "onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.stopAutoManage(getActivity());
        mGoogleApiClient.disconnect();
        Log.d("Location P", "onDestroy");
    }

    @Override
    public void onLocationChanged(Location location) {
        if (lastLocation == null) {
            lastLocation = location;

            List<android.location.Address> addresses = null;
            Log.e("Location", location.toString());

            if (lastLocation != null) {
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
            }

            //etLocation.setHint(addresses.get(0).getAddressLine(0) + ", " + addresses.get(0).getLocality());
            etLocation.setText("" + location.toString());
        }
    }

    public static class dHolder extends RecyclerView.ViewHolder {
        private View view;
        private ImageButton btnCall;
        private ImageButton btnMessage;
        private ImageButton btnSchedule;
        private ImageButton btnWorkingExperience;
        private LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                                                 ViewGroup.LayoutParams.WRAP_CONTENT);

        public dHolder(View itemView) {
            super(itemView);
            view = itemView;
            btnCall = (ImageButton) itemView.findViewById(R.id.item_doctor_ib_call);
            btnMessage = (ImageButton) itemView.findViewById(R.id.item_doctor_ib_message);
            btnSchedule = (ImageButton) itemView.findViewById(R.id.item_doctor_ib_schedule);
            btnWorkingExperience = (ImageButton) itemView.findViewById(R.id.item_doctor_ib_working_experience);
        }

        public void hideView() {
            params.height = 0;
            view.setLayoutParams(params);
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

        public void setAvatar(String photoUrl) {
            ImageView iv = (ImageView) view.findViewById(R.id.fragment_profile_iv_image);
            Picasso.with(view.getContext())
                   .load(photoUrl)
                   .resize(300, 300)
                   .centerCrop()
                   .into(iv);
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

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Log.e("", "onConnectionFailed: ConnectionResult.getErrorCode() = "
                   + connectionResult.getErrorCode());
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
}
