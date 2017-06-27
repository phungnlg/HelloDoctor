package com.phungnlg.hellodoctor;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
//import android.view.LayoutInflater;
import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Phil on 07/05/2017.
 */
@EFragment(R.layout.fragment_notification)
public class NotificationFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";
    private int mPageNo;

    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Notifications");
    private FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();

    @ViewById(R.id.fragment_notification_list_notification)
    protected RecyclerView notificationList;
    @ViewById(R.id.fragment_notification_ib_check_all)
    protected ImageButton btnCheckAll;
    @ViewById(R.id.fragment_notification_tv_check_all)
    protected TextView tvCheckAll;

    private Boolean isConfirmedCheckAll = false;

    @AfterViews
    public void afterView() {
        mDatabase = mDatabase.child(mUser.getUid());
        tvCheckAll.setVisibility(View.GONE);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        notificationList.setHasFixedSize(true);
        notificationList.setNestedScrollingEnabled(false);
        notificationList.setLayoutManager(layoutManager);

        FirebaseRecyclerAdapter<Notification, NotiHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Notification, NotiHolder>(
                Notification.class,
                R.layout.item_notification2,
                NotiHolder.class,
                mDatabase
        ) {
            @Override
            protected void populateViewHolder(final NotiHolder viewHolder,
                                              final Notification model,
                                              final int position) {
                final String NOTIFICATIONKEY = getRef(position).getKey();
                final Boolean ISREADED;
                viewHolder.setTime(model.getTime());
                viewHolder.setBody(model.getNotification());

                viewHolder.btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDatabase.child(NOTIFICATIONKEY).removeValue();
                    }
                });
            }
        };
        notificationList.setAdapter(firebaseRecyclerAdapter);
    }

    @Click(R.id.fragment_notification_ib_check_all)
    public void setBtnCheckAll() {
        if (isConfirmedCheckAll) {
            mDatabase.removeValue();
            tvCheckAll.setVisibility(View.GONE);
            isConfirmedCheckAll = false;
        }
        else {
            isConfirmedCheckAll = true;
            tvCheckAll.setVisibility(View.VISIBLE);
        }
    }

//    @Override
//    public void onCreate(final Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        mPageNo = getArguments().getInt(ARG_PAGE);
//
//        final FirebaseDatabase DATABASE = FirebaseDatabase.getInstance();
//
//        mUser = FirebaseAuth.getInstance().getCurrentUser();
//
//        mDatabase = DATABASE.getReference("Notifications")
//                            .child(mUser.getUid());
//        mDatabase.keepSynced(true);
//
//        //themeColor = getResources().getColor(R.color.themecolor);
//    }

//    @Override
//    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
//                             final Bundle savedInstanceState) {
//        final View VIEW = inflater.inflate(R.layout.fragment_notification, container, false);
//
//        btnCheckAll = (ImageButton) VIEW.findViewById(R.id.fragment_notification_ib_check_all);
//        tvCheckAll = (TextView) VIEW.findViewById(R.id.fragment_notification_tv_check_all);
//        tvCheckAll.setVisibility(View.GONE);
//
//        btnCheckAll.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (isConfirmedCheckAll) {
//                    mDatabase.removeValue();
//                    tvCheckAll.setVisibility(View.GONE);
//                    isConfirmedCheckAll = false;
//                }
//                else {
//                    isConfirmedCheckAll = true;
//                    tvCheckAll.setVisibility(View.VISIBLE);
//                }
//            }
//        });
//
//        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
//        layoutManager.setReverseLayout(true);
//        layoutManager.setStackFromEnd(true);
//
//
//        notificationList = (RecyclerView) VIEW.findViewById(R.id.fragment_notification_list_notification);
//        notificationList.setHasFixedSize(true);
//        notificationList.setNestedScrollingEnabled(false);
//        notificationList.setLayoutManager(layoutManager);
//
//        FirebaseRecyclerAdapter<Notification, NotiHolder> firebaseRecyclerAdapter
//                = new FirebaseRecyclerAdapter<Notification, NotiHolder>(
//                Notification.class,
//                R.layout.item_notification2,
//                NotiHolder.class,
//                mDatabase
//        ) {
//            @Override
//            protected void populateViewHolder(final NotiHolder viewHolder,
//                                              final Notification model,
//                                              final int position) {
//                final String NOTIFICATIONKEY = getRef(position).getKey();
//                final Boolean ISREADED;
//                viewHolder.setTime(model.getTime());
//                viewHolder.setBody(model.getNotification());
//
//                viewHolder.btnDelete.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        mDatabase.child(NOTIFICATIONKEY).removeValue();
//                    }
//                });
//            }
//        };
//        notificationList.setAdapter(firebaseRecyclerAdapter);
//        return VIEW;
//    }

    public static class NotiHolder extends RecyclerView.ViewHolder {
        private View mView;
        //private ImageButton btnCheck;
        private ImageButton btnDelete;
        private TextView noti;

        public NotiHolder(final View itemView) {
            super(itemView);
            mView = itemView;
            //btnCheck = (ImageButton) mView.findViewById(R.id.item_notification_ib_Check);
            noti = (TextView) mView.findViewById(R.id.item_notification_tv_body);
            btnDelete = (ImageButton) mView.findViewById(R.id.item_notification_btn_delete);
        }

        public void setTime(final String notificationTime) {
            TextView time = (TextView) mView.findViewById(R.id.item_notification_tv_time);
            time.setText(notificationTime);
        }

        public void setBody(final String notificationBody) {
            TextView body = (TextView) mView.findViewById(R.id.item_notification_tv_body);
            body.setText(notificationBody);
        }
    }

    public static NotificationFragment newInstance(final int pageNo) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, pageNo);
        NotificationFragment fragment = new NotificationFragment_();
        fragment.setArguments(args);
        return fragment;
    }
}
