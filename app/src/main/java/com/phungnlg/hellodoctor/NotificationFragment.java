package com.phungnlg.hellodoctor;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Phil on 07/05/2017.
 */

public class NotificationFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";
    private int mPageNo;
    private DatabaseReference mDatabase;
    private FirebaseUser mUser;
    private RecyclerView notificationList;
    private int themeColor;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageNo = getArguments().getInt(ARG_PAGE);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        mUser = FirebaseAuth.getInstance().getCurrentUser();

        mDatabase = database.getReference("Notifications")
                            .child(mUser.getUid());
        mDatabase.keepSynced(true);

        themeColor = getResources().getColor(R.color.themecolor);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_notification, container, false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);


        notificationList = (RecyclerView) view.findViewById(R.id.fragment_notification_list_notification);
        notificationList.setHasFixedSize(true);
        notificationList.setNestedScrollingEnabled(false);
        notificationList.setLayoutManager(layoutManager);

        FirebaseRecyclerAdapter<Notification, NotiHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Notification, NotiHolder>(
                Notification.class,
                R.layout.item_notification,
                NotiHolder.class,
                mDatabase
        ) {
            @Override
            protected void populateViewHolder(final NotiHolder viewHolder,
                                              final Notification model,
                                              final int position) {
                final String notificationKey = getRef(position).getKey();
                final Boolean isReaded;
                viewHolder.setTime(model.getTime());
                viewHolder.setBody(model.getNotification());
                viewHolder.btnCheck.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mDatabase.child(notificationKey).removeValue();
                    }
                });
            }
        };
        notificationList.setAdapter(firebaseRecyclerAdapter);
        return view;
    }

    public static class NotiHolder extends RecyclerView.ViewHolder {
        private View mView;
        private ImageButton btnCheck;
        private TextView noti;

        public NotiHolder(final View itemView) {
            super(itemView);
            mView = itemView;
            btnCheck = (ImageButton) mView.findViewById(R.id.item_notification_ib_Check);
            noti = (TextView) mView.findViewById(R.id.item_notification_tv_body);
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
        NotificationFragment fragment = new NotificationFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
