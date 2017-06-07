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

    TextView name, bio, following, follower;

    TextView test;
    RecyclerView post;

    Boolean isDoctor;
    Boolean isLogInByFacebook = true;
    com.makeramen.roundedimageview.RoundedImageView pic;
    AppCompatButton profile, schedule;

    private DatabaseReference mDatabase;
    private DatabaseReference Post = FirebaseDatabase.getInstance().getReference("Posts");
    private FirebaseUser user;

    public ProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_profile, container, false);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("User");
        user = FirebaseAuth.getInstance().getCurrentUser();

        profile = (AppCompatButton) view.findViewById(R.id.profile_btnProfile);
        schedule = (AppCompatButton) view.findViewById(R.id.profile_btnSchedule);

        name = (TextView) view.findViewById(R.id.profile_name);
        bio = (TextView) view.findViewById(R.id.profile_bio);
        following = (TextView) view.findViewById(R.id.profile_following);
        follower = (TextView) view.findViewById(R.id.profile_follower);
        pic = (com.makeramen.roundedimageview.RoundedImageView) view.findViewById(R.id.profile_image);
        test = (TextView) view.findViewById(R.id.profile_forTest);
        post = (RecyclerView) view.findViewById(R.id.profile_post);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        post.setHasFixedSize(true);
        post.setLayoutManager(layoutManager);

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

        FirebaseRecyclerAdapter<Post, NewsFeedFragment.Holder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Post, NewsFeedFragment.Holder>(
                Post.class,
                R.layout.feed_item,
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

        return view;
    }

    public static ProfileFragment newInstance(int pageNo) {

        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, pageNo);
        ProfileFragment fragment = new ProfileFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static class ProfileHolder extends RecyclerView.ViewHolder {
        View mView;

        public ProfileHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setName(String _name) {
            TextView name = (TextView) mView.findViewById(R.id.newsname);
            name.setText(_name);
        }

        public void setTime(String _time) {
            TextView time = (TextView) mView.findViewById(R.id.newstime);
            time.setText(_time);
        }

        public void setTitle(String _title) {
            TextView body = (TextView) mView.findViewById(R.id.news);
            body.setText(_title);
        }

        public void setBody(String _body) {
            TextView body = (TextView) mView.findViewById(R.id.newssub);
            body.setText(_body);
        }

        public void setHashTag(String _hashTag) {
            TextView hashTag = (TextView) mView.findViewById(R.id.intrest);
            hashTag.setText(_hashTag);
        }

        public void setLikeCount(String _like) {
            TextView t = (TextView) mView.findViewById(R.id.newsfeed_txtLikeCount);
            t.setText(_like);
        }
    }
}
