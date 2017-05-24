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

    private RecyclerView noti_list;

    int themeColor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageNo = getArguments().getInt(ARG_PAGE);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        mUser = FirebaseAuth.getInstance().getCurrentUser();

        mDatabase = database.getReference("Notifications").child(mUser.getUid());
        mDatabase.keepSynced(true);

        themeColor = getResources().getColor(R.color.themecolor);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.tab_notification, container, false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);


        noti_list = (RecyclerView) view.findViewById(R.id.noti_list);
        noti_list.setHasFixedSize(true);
        //noti_list.setLayoutManager(new LinearLayoutManager(this.getContext()));
        noti_list.setLayoutManager(layoutManager);

        FirebaseRecyclerAdapter<Notification, NotiHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Notification, NotiHolder>(
                Notification.class,
                R.layout.notification_item,
                NotiHolder.class,
                mDatabase
        ) {
            @Override
            protected void populateViewHolder(NotiHolder viewHolder, Notification model, int position) {

                final String noti_key = getRef(position).getKey();
                final Boolean isReaded;

                viewHolder.setTime(model.getTime());
                viewHolder.setBody(model.getNotification());
                viewHolder.btnCheck.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mDatabase.child(noti_key).removeValue();
                    }
                });
            }
        };
        noti_list.setAdapter(firebaseRecyclerAdapter);
        return view;
    }

    public static class NotiHolder extends RecyclerView.ViewHolder {
        View mView;
        ImageButton btnCheck;
        TextView noti;

        public NotiHolder(View itemView) {
            super(itemView);
            mView = itemView;
            btnCheck = (ImageButton) mView.findViewById(R.id.noti_btnCheck);
            noti = (TextView) mView.findViewById(R.id.noti_body);
        }

        public void setTime(String _time) {
            TextView time = (TextView) mView.findViewById(R.id.noti_time);
            time.setText(_time);
        }

        public void setBody(String _body) {
            TextView body = (TextView) mView.findViewById(R.id.noti_body);
            body.setText(_body);
        }
    }

    public static NotificationFragment newInstance(int pageNo) {

        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, pageNo);
        NotificationFragment fragment = new NotificationFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
