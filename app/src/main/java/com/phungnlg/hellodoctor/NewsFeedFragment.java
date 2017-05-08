package com.phungnlg.hellodoctor;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Phil on 07/05/2017.
 */

public class NewsFeedFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";
    String userName;
    String userEmail;
    TextView _userName;
    TextView _userEmail;

    public String tempName;

    EditText write_post;

    private String urlProfilePicture;
    private Boolean isLogInByFacebook;
    private Boolean isDoctor;

    DatabaseReference ref;
    DatabaseReference userRef;

    private ListView listView;

    private List<Post> feedItems;

    //private long postPreviousVote;
    //private long postPreviousAnswer;

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

        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        mBlogList = (RecyclerView) view.findViewById(R.id.blog_list);
        mBlogList.setHasFixedSize(true);
        //mBlogList.setLayoutManager(new LinearLayoutManager(this.getContext()));
        mBlogList.setLayoutManager(layoutManager);

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

        Query sortByTime = mDatabase.orderByKey().limitToLast(10);

        FirebaseRecyclerAdapter<Post, Holder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Post, Holder>(
                Post.class,
                R.layout.feed_item,
                Holder.class,
                //mDatabase
                sortByTime
        ) {
            @Override
            protected void populateViewHolder(final Holder viewHolder, Post model, int position) {
                final String post_key = getRef(position).getKey();

                viewHolder.setBody(model.body);
                viewHolder.setHashTag(model.tag);
                viewHolder.setName(model.username);
                viewHolder.setTime(model.time);
                viewHolder.setTitle(model.title);
                viewHolder.setLikeCount("   " + model.vote + " người có câu hỏi tương tự, " + model.answer + " trả lời.");

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
        firebaseRecyclerAdapter.notifyDataSetChanged();
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
            //TextView name = (TextView)mView.findViewById(R.id.name);
            TextView name = (TextView) mView.findViewById(R.id.newsname);
            name.setText(_name);
        }

        public void setTime(String _time) {
            //tạm thời dùng titile cho mục này
            //TextView time = (TextView)mView.findViewById(R.id.timestamp);
            TextView time = (TextView) mView.findViewById(R.id.newstime);
            time.setText(_time);
        }

        public void setTitle(String _title) {
            TextView body = (TextView) mView.findViewById(R.id.news);
            body.setText(_title);
        }

        public void setBody(String _body) {
            //TextView body = (TextView)mView.findViewById(R.id.txtStatusMsg);
            TextView body = (TextView) mView.findViewById(R.id.newssub);
            body.setText(_body);
        }

        public void setHashTag(String _hashTag) {
            //TextView hashTag = (TextView)mView.findViewById(R.id.txtHastag);
            TextView hashTag = (TextView) mView.findViewById(R.id.intrest);
            hashTag.setText(_hashTag);
        }

        public void setLikeCount(String _like) {
            TextView t = (TextView) mView.findViewById(R.id.newsfeed_txtLikeCount);
            t.setText(_like);
        }
    }
}
