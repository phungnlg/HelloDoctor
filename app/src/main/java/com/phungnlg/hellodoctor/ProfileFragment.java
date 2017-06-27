package com.phungnlg.hellodoctor;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
//import android.support.v7.widget.AppCompatButton;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Phil on 07/05/2017.
 */

public class ProfileFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";

    private TextView name;
    private TextView bio;
    private TextView following;
    private TextView follower;

    private TextView tvTest;
    private RecyclerView listPost;

    private Boolean isDoctor;
    private com.makeramen.roundedimageview.RoundedImageView ivProfilePic;
    private ImageButton btnProfile;
    private ImageButton btnSchedule;

    private DatabaseReference mDatabase;
    private DatabaseReference postDatabase = FirebaseDatabase.getInstance().getReference("Posts");
    private FirebaseUser user;

    public ProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("User");
        user = FirebaseAuth.getInstance().getCurrentUser();

        btnProfile = (ImageButton) view.findViewById(R.id.fragment_profile_btn_profile1);
        btnSchedule = (ImageButton) view.findViewById(R.id.fragment_profile_btn_Schedule1);

        name = (TextView) view.findViewById(R.id.fragment_profile_tv_user_name);
        bio = (TextView) view.findViewById(R.id.fragment_profile_tv_user_bio);
        following = (TextView) view.findViewById(R.id.fragment_profile_tv_user_following);
        follower = (TextView) view.findViewById(R.id.fragment_profile_tv_user_follower);
        ivProfilePic = (com.makeramen.roundedimageview.RoundedImageView) view
                .findViewById(R.id.fragment_profile_iv_image);
        tvTest = (TextView) view.findViewById(R.id.fragment_profile_tv_Test);
        listPost = (RecyclerView) view.findViewById(R.id.fragment_profile_list_user_post);

        final LinearLayoutManager LAYOUTMANAGER = new LinearLayoutManager(this.getContext());
        LAYOUTMANAGER.setReverseLayout(true);
        LAYOUTMANAGER.setStackFromEnd(true);
        listPost.setNestedScrollingEnabled(false);
        listPost.setHasFixedSize(true);
        listPost.setLayoutManager(LAYOUTMANAGER);
        listPost.setFocusable(false);

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String n = (String) dataSnapshot.child(user.getUid()).child("name").getValue();
                String b = (String) dataSnapshot.child(user.getUid()).child("bio").getValue();
                long followingCount = (Long) dataSnapshot.child(user.getUid()).child("following").getValue();
                long followerCount = (Long) dataSnapshot.child(user.getUid()).child("follower").getValue();

                isDoctor = (Boolean) dataSnapshot.child(user.getUid()).child("isDoctor").getValue();

                name.setText(n);
                //name.setText(user.getDisplayName());
                bio.setText(b);
                following.setText("" + followingCount);
                follower.setText("" + followerCount);

                if (!isDoctor) {
                    btnProfile.setVisibility(View.GONE);
                    btnSchedule.setVisibility(View.GONE);
                } else {
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        Query sortByTime = postDatabase.orderByChild("uid").equalTo(user.getUid());

        final FirebaseRecyclerAdapter<PostItem, NewsFeedFragment.Holder> ADAPTER
                = new FirebaseRecyclerAdapter<PostItem, NewsFeedFragment.Holder>(
                PostItem.class,
                R.layout.item_news_feed,
                NewsFeedFragment.Holder.class,
                //mDatabase
                sortByTime
        ) {
            @Override
            protected void populateViewHolder(final NewsFeedFragment.Holder viewHolder, PostItem model, int position) {
                final String POSTKEY = getRef(position).getKey();

                viewHolder.setBody(model.getBody());
                viewHolder.setHashTag(model.getTag());
                viewHolder.setName(model.getUsername());
                viewHolder.setTime(model.getTime());
                viewHolder.setTitle(model.getTitle());
                viewHolder
                        .setLikeCount("   " + model.getVote() + " người có câu hỏi tương tự, " +
                                      model.getAnswer() + " trả lời.");
                viewHolder.setPhoto(model.getPhotoUrl());

                final long PREVIOUSVOTE = model.getVote();

                viewHolder.getmView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Bundle bundle = new Bundle();
                        bundle.putString("post_key", POSTKEY);

                        SinglePostFragment f = SinglePostFragment_.builder().postKey(POSTKEY).build();
                        //f.setArguments(bundle);
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out);
                        ft.replace(R.id.tab_profile, f);
                        ft.addToBackStack(null);
                        ft.commit();
                    }
                });

            }
        };
        listPost.setAdapter(ADAPTER);

        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Bundle bundle = new Bundle();
                bundle.putBoolean("isEditMode", true);
                bundle.putString("key", user.getUid());*/

                //CVFragment f = new CVFragment_();
                CVFragment f = CVFragment_.builder().isEditMode(true).key(user.getUid()).build();
                //f.setArguments(bundle);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out);
                ft.replace(R.id.tab_profile, f);
                ft.addToBackStack(null);
                ft.commit();
            }
        });
        btnSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putBoolean("isEditMode", true);
                bundle.putString("key", user.getUid());

                ScheduleFragment f = new ScheduleFragment();
                f.setArguments(bundle);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out);
                ft.replace(R.id.tab_profile, f);
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        ADAPTER.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = ADAPTER.getItemCount();
                int lastVisiblePosition =
                        LAYOUTMANAGER.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == -1 ||
                    (positionStart >= (friendlyMessageCount - 1) &&
                     lastVisiblePosition == (positionStart - 1))) {
                    listPost.scrollToPosition(positionStart);
                }
            }
        });

        return view;
    }

    public static ProfileFragment newInstance(int pageNo) {

        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, pageNo);
        ProfileFragment fragment = new ProfileFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
