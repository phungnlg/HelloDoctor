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
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

/**
 * Created by Phil on 07/05/2017.
 */

public class NewsFeedFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";
    private static final String TAG = "NewsFeedFragment";

    private EditText etQuestion;

    private DatabaseReference mDatabase;

    private RecyclerView mBlogList;

    private Boolean isLiked = false;
    private int mPageNo;

    public NewsFeedFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageNo = getArguments().getInt(ARG_PAGE);

        final FirebaseDatabase DATABASE = FirebaseDatabase.getInstance();

        mDatabase = DATABASE.getReference("Posts");
        mDatabase.keepSynced(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news_feed, container, false);

        final LinearLayoutManager LAYOUTMANAGER = new LinearLayoutManager(this.getContext());
        LAYOUTMANAGER.setReverseLayout(true);
        LAYOUTMANAGER.setStackFromEnd(true);

        mBlogList = (RecyclerView) view.findViewById(R.id.fragment_newsfeed_list);
        mBlogList.setNestedScrollingEnabled(false);
        mBlogList.setHasFixedSize(true);

        etQuestion = (EditText) view.findViewById(R.id.fragment_newsfeed_et_question);
        etQuestion.setOnClickListener(new View.OnClickListener() {
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

        final FirebaseRecyclerAdapter<Post, Holder> ADAPTER = new FirebaseRecyclerAdapter<Post, Holder>(
                Post.class,
                R.layout.item_news_feed,
                Holder.class,
                mDatabase
                //sortByTime
        ) {
            @Override
            protected void populateViewHolder(final Holder viewHolder,
                                              Post model,
                                              int position) {
                final String POSTKEY = getRef(position).getKey();

                Log.d(TAG, "Data added");

                viewHolder.setBody(model.getBody());
                viewHolder.setHashTag(model.getTag());
                viewHolder.setName(model.getUsername());
                viewHolder.setTime(model.getTime());
                viewHolder.setTitle(model.getTitle());
                viewHolder
                        .setLikeCount("   " + model.getVote() +
                                      " người có câu hỏi tương tự, " + model.getAnswer() +
                                      " trả lời.");
                viewHolder.setPhoto(model.getPhotoUrl());

                final long PREVIOUSVOTE = model.getVote();

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Bundle bundle = new Bundle();
                        bundle.putString("post_key", POSTKEY);

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
                                mDatabase.child(POSTKEY).child("vote").setValue(PREVIOUSVOTE + 1);
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

        ADAPTER.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(final int positionStart, final int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = ADAPTER.getItemCount();
                int lastVisiblePosition =
                        LAYOUTMANAGER.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == -1
                    || (positionStart >= (friendlyMessageCount - 1)
                        && lastVisiblePosition == (positionStart - 1))) {
                    mBlogList.scrollToPosition(positionStart);
                }
            }
        });

        mBlogList.setLayoutManager(LAYOUTMANAGER);
        mBlogList.setAdapter(ADAPTER);

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
        private View mView;
        private ImageButton btnLike;

        public Holder(View itemView) {
            super(itemView);
            mView = itemView;
            btnLike = (ImageButton) mView.findViewById(R.id.item_newsfeed_ib_like);
        }

        public View getmView() {
            return mView;
        }

        public void setmView(View mView) {
            this.mView = mView;
        }

        public ImageButton getBtnLike() {
            return btnLike;
        }

        public void setBtnLike(ImageButton btnLike) {
            this.btnLike = btnLike;
        }

        public void setName(String _name) {
            TextView name = (TextView) mView.findViewById(R.id.item_newsfeed_tv_user_name);
            name.setText(_name);
        }

        public void setTime(String _time) {
            TextView time = (TextView) mView.findViewById(R.id.item_newsfeed_tv_post_time);
            time.setText(_time);
        }

        public void setTitle(String _title) {
            TextView body = (TextView) mView.findViewById(R.id.item_newsfeed_tv_post_title);
            body.setText(_title);
        }

        public void setBody(String _body) {
            TextView body = (TextView) mView.findViewById(R.id.item_newsfeed_tv_post_body);
            body.setText(_body);
        }

        public void setHashTag(String _hashTag) {
            TextView hashTag = (TextView) mView.findViewById(R.id.item_newsfeed_tv_post_category);
            hashTag.setText(_hashTag);
        }

        public void setLikeCount(String _like) {
            TextView t = (TextView) mView.findViewById(R.id.item_newsfeed_tv_like_count);
            t.setText(_like);
        }

        public void setPhoto(String photoUrl) {
            ImageView iv = (ImageView) mView.findViewById(R.id.item_newsfeed_iv_cover_photo);
            Picasso.with(mView.getContext())
                   .load(photoUrl)
                   .resize(300, 150)
                   .centerCrop()
                   .into(iv);
        }

    }
}
