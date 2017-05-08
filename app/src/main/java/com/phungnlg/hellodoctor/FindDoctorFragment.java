package com.phungnlg.hellodoctor;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

/**
 * Created by Phil on 07/05/2017.
 */

public class FindDoctorFragment extends Fragment {
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("message").child("user-doctor");
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    RecyclerView doctorlist;

    Spinner major;

    Query sortMajor;
    ImageButton btnSearch;

    public FindDoctorFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.tab_find_doctor, container, false);

        doctorlist = (RecyclerView) view.findViewById(R.id.find_list);
        doctorlist.setHasFixedSize(true);
        doctorlist.setLayoutManager(new LinearLayoutManager(this.getContext()));

        major = (Spinner) view.findViewById(R.id.find_major);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.major, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        major.setAdapter(adapter);

        btnSearch = (ImageButton) view.findViewById(R.id.find_search);

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sortMajor = mDatabase.orderByChild("major").equalTo(major.getSelectedItem().toString());

                FirebaseRecyclerAdapter<Doctor, dHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Doctor, dHolder>(
                        Doctor.class,
                        R.layout.doctor_item,
                        dHolder.class,
                        //mDatabase.orderByChild("major").equalTo("Nhi khoa")
                        sortMajor
                ) {
                    @Override
                    protected void populateViewHolder(dHolder viewHolder, Doctor model, int position) {
                        viewHolder.setName(model.name);
                        viewHolder.setAddress(model.address);
                        viewHolder.setBio("Bác sỹ "+ model.major + " tại " + model.workplace);
                        viewHolder.setMobile(model.mobile);

                        final String doctor_key = getRef(position).getKey();
                        final String doctor_name = model.name;

                        viewHolder.view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Bundle bundle = new Bundle();
                                bundle.putString("doctor_key", doctor_key);
                                bundle.putString("doctor_name", doctor_name);

                                BookAppointmentFragment f = new BookAppointmentFragment();
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

                doctorlist.setAdapter(firebaseRecyclerAdapter);
            }
        });

        return view;
    }

    public static class dHolder extends RecyclerView.ViewHolder{
        View view;

        public dHolder(View itemView){
            super(itemView);
            view = itemView;
        }
        public void setName(String _name){
            TextView name = (TextView) view.findViewById(R.id.find_name);
            name.setText(_name);
        }
        public void setBio(String _bio){
            TextView bio = (TextView) view.findViewById(R.id.find_bio);
            bio.setText(_bio);
        }
        public void setAddress(String _a){
            TextView add = (TextView) view.findViewById(R.id.find_address);
            add.setText(_a);
        }
        public void setMobile(String _mobile){
            TextView mobile = (TextView) view.findViewById(R.id.find_mobile);
            mobile.setText(_mobile);
        }
    }
}
