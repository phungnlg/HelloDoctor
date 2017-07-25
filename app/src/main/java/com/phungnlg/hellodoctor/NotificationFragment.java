package com.phungnlg.hellodoctor;

import android.os.Bundle;
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
import com.phungnlg.hellodoctor.adapter.IApiService;
import com.phungnlg.hellodoctor.adapter.NotificationAdapter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//import android.view.LayoutInflater;
//import android.view.ViewGroup;
//import android.widget.Button;

/**
 * Created by Phil on 07/05/2017.
 */
@EFragment(R.layout.fragment_notification)
public class NotificationFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";
    private int mPageNo;

    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Notifications");
    private FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();

    private FirebaseRecyclerAdapter<NotificationItem, NotiHolder> firebaseRecyclerAdapter;
    private LinearLayoutManager layoutManager;
    private Retrofit retrofit;
    private IApiService iApiService;

    @ViewById(R.id.fragment_notification_list_notification)
    protected RecyclerView notificationList;
    @ViewById(R.id.fragment_notification_ib_check_all)
    protected ImageButton btnCheckAll;
    @ViewById(R.id.fragment_notification_tv_check_all)
    protected TextView tvCheckAll;

    private Boolean isConfirmedCheckAll = false;

    private ArrayList<NotificationItem> listNotification = new ArrayList<>();
    private NotificationAdapter notificationAdapter;

    void loadNotification() {
        initRetrofit();

        iApiService.getNotification(mUser.getUid()).enqueue(new Callback<LinkedHashMap<String, NotificationItem>>() {
            @Override
            public void onResponse(Call<LinkedHashMap<String, NotificationItem>> call,
                                   Response<LinkedHashMap<String, NotificationItem>> response) {
                for (LinkedHashMap.Entry<String, NotificationItem> list : response.body().entrySet()) {
                    listNotification.add(list.getValue());
                }
                notificationAdapter = new NotificationAdapter(listNotification);
                notificationAdapter.notifyDataSetChanged();
                notificationList.setAdapter(notificationAdapter);
            }

            @Override
            public void onFailure(Call<LinkedHashMap<String, NotificationItem>> call, Throwable t) {

            }
        });
    }

    public void initRetrofit() {
        retrofit = new Retrofit.Builder()
        .baseUrl("https://newhellodoctor.firebaseio.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build();

        iApiService = retrofit.create(IApiService.class);
    }

    public void initRecyclerView() {
        mDatabase = mDatabase.child(mUser.getUid());
        tvCheckAll.setVisibility(View.GONE);

        layoutManager = new LinearLayoutManager(this.getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        //notificationList.setHasFixedSize(true);
        notificationList.setNestedScrollingEnabled(false);
        notificationList.setLayoutManager(layoutManager);
    }

    public void registerAdapterDataObserver() {
        firebaseRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = firebaseRecyclerAdapter.getItemCount();
                int lastVisiblePosition =
                        layoutManager.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == -1 ||
                    (positionStart >= (friendlyMessageCount - 1) &&
                     lastVisiblePosition == (positionStart - 1))) {
                    notificationList.scrollToPosition(positionStart);
                }
            }
        });
    }

    public void initFirebaseRecyclerAdapter() {
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<NotificationItem, NotiHolder>(
                NotificationItem.class,
                R.layout.item_notification2,
                NotiHolder.class,
                mDatabase
        ) {
            @Override
            protected void populateViewHolder(final NotiHolder viewHolder,
                                              final NotificationItem model,
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
    }

    @AfterViews
    public void afterView() {
        initRecyclerView();
        //loadNotification();

        initFirebaseRecyclerAdapter();
        registerAdapterDataObserver();
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
