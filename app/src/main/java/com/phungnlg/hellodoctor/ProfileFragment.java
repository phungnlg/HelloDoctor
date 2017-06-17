package com.phungnlg.hellodoctor;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    private TextView name, bio, following, follower;

    private TextView test;
    private RecyclerView post;

    private Boolean isDoctor;
    private Boolean isLogInByFacebook = true;
    private com.makeramen.roundedimageview.RoundedImageView pic;
    private AppCompatButton profile, schedule;

    private DatabaseReference mDatabase;
    private DatabaseReference Post = FirebaseDatabase.getInstance().getReference("Posts");
    private FirebaseUser user;

    public ProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("User");
        user = FirebaseAuth.getInstance().getCurrentUser();

        profile = (AppCompatButton) view.findViewById(R.id.fragment_profile_btn_profile);
        schedule = (AppCompatButton) view.findViewById(R.id.fragment_profile_btn_Schedule);

        name = (TextView) view.findViewById(R.id.fragment_profile_tv_user_name);
        bio = (TextView) view.findViewById(R.id.fragment_profile_tv_user_bio);
        following = (TextView) view.findViewById(R.id.fragment_profile_tv_user_following);
        follower = (TextView) view.findViewById(R.id.fragment_profile_tv_user_follower);
        pic = (com.makeramen.roundedimageview.RoundedImageView) view.findViewById(R.id.fragment_profile_iv_image);
        test = (TextView) view.findViewById(R.id.fragment_profile_tv_Test);
        post = (RecyclerView) view.findViewById(R.id.fragment_profile_list_user_post);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        post.setNestedScrollingEnabled(false);
        post.setHasFixedSize(true);
        post.setLayoutManager(layoutManager);
        post.setFocusable(false);

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String n = (String) dataSnapshot.child(user.getUid()).child("name").getValue();
                String b = (String) dataSnapshot.child(user.getUid()).child("bio").getValue();
                long f1 = (Long) dataSnapshot.child(user.getUid()).child("following").getValue();
                long f2 = (Long) dataSnapshot.child(user.getUid()).child("follower").getValue();

                isDoctor = (Boolean) dataSnapshot.child(user.getUid()).child("isDoctor").getValue();

                name.setText(n);
                //name.setText(user.getDisplayName());
                bio.setText(b);
                following.setText("" + f1);
                follower.setText("" + f2);

                if (!isDoctor) {
                    profile.setVisibility(View.GONE);
                    schedule.setVisibility(View.GONE);
                } else {
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        Query sortByTime = Post.orderByChild("uid").equalTo(user.getUid());

        final FirebaseRecyclerAdapter<Post, NewsFeedFragment.Holder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Post, NewsFeedFragment.Holder>(
                Post.class,
                R.layout.item_news_feed,
                NewsFeedFragment.Holder.class,
                //mDatabase
                sortByTime
        ) {
            @Override
            protected void populateViewHolder(final NewsFeedFragment.Holder viewHolder, Post model, int position) {
                final String post_key = getRef(position).getKey();

                viewHolder.setBody(model.body);
                viewHolder.setHashTag(model.tag);
                viewHolder.setName(model.username);
                viewHolder.setTime(model.time);
                viewHolder.setTitle(model.title);
                viewHolder
                        .setLikeCount("   " + model.vote + " người có câu hỏi tương tự, " + model.answer + " trả lời.");
                viewHolder.setPhoto(model.photoUrl);

                final long postPreviousVote = model.vote;

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Bundle bundle = new Bundle();
                        bundle.putString("post_key", post_key);

                        SinglePostFragment f = new SinglePostFragment();
                        f.setArguments(bundle);
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out);
                        ft.replace(R.id.tab_profile, f);
                        ft.addToBackStack(null);
                        ft.commit();
                    }
                });

            }
        };
        post.setAdapter(firebaseRecyclerAdapter);

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putBoolean("isEditMode", true);
                bundle.putString("key", user.getUid());

                CVFragment f = new CVFragment();
                f.setArguments(bundle);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out);
                ft.replace(R.id.tab_profile, f);
                ft.addToBackStack(null);
                ft.commit();
            }
        });
        schedule.setOnClickListener(new View.OnClickListener() {
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

        firebaseRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver(){
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = firebaseRecyclerAdapter.getItemCount();
                int lastVisiblePosition =
                        layoutManager.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == -1 ||
                    (positionStart >= (friendlyMessageCount - 1) &&
                     lastVisiblePosition == (positionStart - 1))) {
                    post.scrollToPosition(positionStart);
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
