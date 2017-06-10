package com.phungnlg.hellodoctor;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Phil on 07/05/2017.
 */

public class NewsFeedFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";
    private static final String TAG = "NewsFeedFragment";
    String userName;
    String userEmail;
    TextView _userName;
    TextView _userEmail;

    public String tempName;

    EditText write_post;

    DatabaseReference ref;
    DatabaseReference userRef;

    private DatabaseReference mDatabase;

    ArrayList<Post> listPost;

    private RecyclerView mBlogList;

    private Boolean isLiked = false;

    public NewsFeedFragment() {
    }

    private int mPageNo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageNo = getArguments().getInt(ARG_PAGE);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        mDatabase = database.getReference("Posts");
        mDatabase.keepSynced(true);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_newsfeed, container, false);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        mBlogList = (RecyclerView) view.findViewById(R.id.blog_list);
        mBlogList.setNestedScrollingEnabled(false);
        mBlogList.setHasFixedSize(true);

        write_post = (EditText) view.findViewById(R.id.autotext);
        write_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WritePostFragment f = new WritePostFragment();
                //f.setArguments(bundle);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out);
                ft.replace(R.id.newsfeed, f);
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        Query sortByTime = mDatabase.orderByKey().limitToLast(50);

        final FirebaseRecyclerAdapter<Post, Holder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Post, Holder>(
                Post.class,
                R.layout.feed_item,
                Holder.class,
                //mDatabase
                sortByTime
        ) {
            @Override
            protected void populateViewHolder(final Holder viewHolder, Post model, int position) {
                final String post_key = getRef(position).getKey();

                Log.d(TAG, "Data added");

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
                        ft.replace(R.id.newsfeed, f);
                        ft.addToBackStack(null);
                        ft.commit();
                    }
                });

                viewHolder.btnLike.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int myColor = getResources().getColor(R.color.themecolor);
                        viewHolder.btnLike.setColorFilter(myColor, PorterDuff.Mode.SRC_ATOP);

                        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                mDatabase.child(post_key).child("vote").setValue(postPreviousVote + 1);
                                isLiked = false;
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });
            }
        };
        //firebaseRecyclerAdapter.notifyDataSetChanged();

        firebaseRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(final int positionStart, final int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = firebaseRecyclerAdapter.getItemCount();
                int lastVisiblePosition =
                        layoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1
                    || (positionStart >= (friendlyMessageCount - 1)
                        && lastVisiblePosition == (positionStart - 1))) {
                    mBlogList.scrollToPosition(positionStart);
                }
            }
        });
        mBlogList.setLayoutManager(layoutManager);
        mBlogList.setAdapter(firebaseRecyclerAdapter);

        getActivity().setTitle("Bảng tin");

        return view;
    }

    public static NewsFeedFragment newInstance(int pageNo) {

        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, pageNo);
        NewsFeedFragment fragment = new NewsFeedFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public static class Holder extends RecyclerView.ViewHolder {
        View mView;
        ImageButton btnLike;

        public Holder(View itemView) {
            super(itemView);
            mView = itemView;
            btnLike = (ImageButton) mView.findViewById(R.id.newsfeed_btnLike);
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

        public void setPhoto(String photoUrl){
            ImageView iv = (ImageView) mView.findViewById(R.id.feed_iv_photo);
            Picasso.with(mView.getContext())
                   .load(photoUrl)
                   .resize(300, 150)
                   .centerCrop()
                   .into(iv);
        }
    }
}
