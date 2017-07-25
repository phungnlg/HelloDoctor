package com.phungnlg.hellodoctor;

import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.phungnlg.hellodoctor.object.AppointmentItem;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;


/**
 * Created by Phil on 7/17/2017.
 */
@EFragment(R.layout.fragment_appointment_doctor)
public class AppointmentDoctorFragment extends Fragment {
    @ViewById(R.id.fragment_appointment_doctor_confirmed)
    protected RecyclerView listConfirmed;
    @ViewById(R.id.fragment_appointment_doctor_unconfirmed)
    protected RecyclerView listUnconfirmed;

    private LinearLayoutManager layoutManager;
    private LinearLayoutManager layoutManagerPending;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference appointmentDatabase;
    private FirebaseRecyclerAdapter<AppointmentItem, Holder> confirmedAppointmentAdapter;
    private FirebaseRecyclerAdapter<AppointmentItem, Holder> pendingAppointmentAdapter;


    public void initDatabaseConnection() {
        appointmentDatabase = FirebaseDatabase.getInstance().getReference("Appointment").child(user.getUid());
    }

    public void setUpLayoutManager() {
        layoutManager = new LinearLayoutManager(this.getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        layoutManagerPending = new LinearLayoutManager(this.getContext());
        layoutManagerPending.setReverseLayout(true);
        layoutManagerPending.setStackFromEnd(true);
    }

    public void loadConfirmedList() {
        Query query = appointmentDatabase.orderByChild("isConfirmed").equalTo(true);

        confirmedAppointmentAdapter = new FirebaseRecyclerAdapter<AppointmentItem, Holder>(
                AppointmentItem.class,
                R.layout.item_appointment_confirmed,
                Holder.class,
                query
        ) {
            @Override
            protected void populateViewHolder(Holder viewHolder, AppointmentItem model,
                                              int position) {
                viewHolder.setTvTitle(model.getTitle());
                viewHolder.setTvAddress(model.getAddress());
                viewHolder.setTvConfirm("CONFIRMED");
                viewHolder.setTvTime(model.getTime());

                viewHolder.ibPostpone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        appointmentDatabase.child(getRef(position).getKey()).child("isConfirmed").setValue(false);
                    }
                });
                viewHolder.ibCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        appointmentDatabase.child(getRef(position).getKey()).removeValue();
                    }
                });
            }
        };

        listConfirmed.setAdapter(confirmedAppointmentAdapter);
        listConfirmed.setLayoutManager(layoutManager);
        listConfirmed.setNestedScrollingEnabled(false);
    }

    public void loadPendingList() {
        Query query = appointmentDatabase.orderByChild("isConfirmed").equalTo(false);

        pendingAppointmentAdapter = new FirebaseRecyclerAdapter<AppointmentItem, Holder>(
                AppointmentItem.class,
                R.layout.item_appointment_pending,
                Holder.class,
                query
        ) {
            @Override
            protected void populateViewHolder(Holder viewHolder, AppointmentItem model,
                                              int position) {
                viewHolder.setTvTitle(model.getTitle());
                viewHolder.setTvAddress(model.getAddress());
                viewHolder.setTvConfirm("PENDING");
                viewHolder.tvConfirm.setTextColor(getResources().getColor(R.color.orange_500));
                viewHolder.setTvTime(model.getTime());

                viewHolder.ibAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        appointmentDatabase.child(getRef(position).getKey()).child("isConfirmed").setValue(true);
                    }
                });

                viewHolder.ibCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        appointmentDatabase.child(getRef(position).getKey()).removeValue();
                    }
                });
            }
        };

        listUnconfirmed.setAdapter(pendingAppointmentAdapter);
        listUnconfirmed.setLayoutManager(layoutManagerPending);
        listUnconfirmed.setNestedScrollingEnabled(false);
    }

    @AfterViews
    public void init() {
        setUpLayoutManager();
        initDatabaseConnection();
        loadConfirmedList();
        loadPendingList();
    }

    public static class Holder extends RecyclerView.ViewHolder {
        private TextView tvTitle;
        private TextView tvTime;
        private TextView tvAddress;
        private TextView tvConfirm;
        private ImageButton ibAccept;
        private ImageButton ibCancel;
        private ImageButton ibPostpone;

        public Holder(View itemView) {
            super(itemView);

            tvTitle = (TextView) itemView.findViewById(R.id.item_appointment_tv_title);
            tvAddress = (TextView) itemView.findViewById(R.id.item_appointment_tv_address);
            tvConfirm = (TextView) itemView.findViewById(R.id.item_appointment_tv_confirm);
            tvTime = (TextView) itemView.findViewById(R.id.item_appointment_tv_time);
            ibAccept = (ImageButton) itemView.findViewById(R.id.item_appointment_ib_accept);
            ibCancel = (ImageButton) itemView.findViewById(R.id.item_appointment_ib_cancel);
            ibPostpone = (ImageButton) itemView.findViewById(R.id.item_appointment_ib_postpone);
        }

        public void setTvTitle(String tvTitle) {
            this.tvTitle.setText(tvTitle);
        }

        public void setTvTime(String tvTime) {
            this.tvTime.setText(tvTime);
        }

        public void setTvAddress(String tvAddress) {
            this.tvAddress.setText(tvAddress);
        }

        public void setTvConfirm(String tvConfirm) {
            this.tvConfirm.setText(tvConfirm);
        }
    }
}