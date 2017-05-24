package com.phungnlg.hellodoctor;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Phil on 07/05/2017.
 */

public class SinglePostFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private String post_key;

    private DatabaseReference mDatabase;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference noti;

    private FirebaseUser user;

    TextView postName, postStatus, postTitle, postTag, postTime, postLikeCount;
    ImageButton btnAnswer;

    EditText txtAnswer;

    String name;

    private long postPreviousVote;
    private long postPreviousAnswer;

    private RecyclerView mCommentList;

    private OnFragmentInteractionListener mListener;

    public SinglePostFragment() {
        // Required empty public constructor
    }


    public static SinglePostFragment newInstance(String param1, String param2) {
        SinglePostFragment fragment = new SinglePostFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Posts");
        noti = database.getReference("Notifications");

        user = FirebaseAuth.getInstance().getCurrentUser();
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            post_key = bundle.getString("post_key");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_single_post, container, false);

        postName = (TextView) view.findViewById(R.id.tab_name);
        postStatus = (TextView) view.findViewById(R.id.tab_body);
        postTime = (TextView) view.findViewById(R.id.tab_time);
        postTitle = (TextView) view.findViewById(R.id.tab_title);
        postLikeCount = (TextView) view.findViewById(R.id.tab_txtLikeCount);
        btnAnswer = (ImageButton) view.findViewById(R.id.tab_btnSendComment);
        txtAnswer = (EditText) view.findViewById(R.id.tab_txtComment);
        postTag = (TextView) view.findViewById(R.id.tab_tag);

        String outputPattern = "h:mm a dd-MM-yyyy";
        String inputPattern = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        final SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+1:00"));
        final Date currentLocalTime = cal.getTime();

        mCommentList = (RecyclerView) view.findViewById(R.id.comment_list);
        mCommentList.setHasFixedSize(true);
        mCommentList.setLayoutManager(new LinearLayoutManager(this.getContext()));

        name = txtAnswer.getText().toString();

        mDatabase.child(post_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String postTitle1 = (String) dataSnapshot.child("title").getValue();
                String postBody = (String) dataSnapshot.child("body").getValue();
                String postuid = (String) dataSnapshot.child("uid").getValue();
                long postAnswer = (Long) dataSnapshot.child("answer").getValue();
                long postVote = (Long) dataSnapshot.child("vote").getValue();
                String postUser = (String) dataSnapshot.child("username").getValue();
                String postTag1 = (String) dataSnapshot.child("tag").getValue();
                String postTime1 = (String) dataSnapshot.child("time").getValue();

                postPreviousVote = postVote;
                postPreviousAnswer = postAnswer;

                postName.setText(postUser);
                postLikeCount.setText("   " + postVote + " người có câu hỏi tương tự, " + postAnswer + " trả lời.");
                postStatus.setText(postBody);
                postTime.setText(postTime1);
                postTitle.setText(postTitle1);
                postTag.setText(postTag1);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        final DatabaseReference mDatabaseCommentList = FirebaseDatabase.getInstance().getReference().child("Comments").child(post_key);
        FirebaseRecyclerAdapter<Comment, CommentHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Comment, CommentHolder>(
                Comment.class,
                R.layout.comment_item,
                CommentHolder.class,
                mDatabaseCommentList
        ) {
            @Override
            protected void populateViewHolder(CommentHolder viewHolder, Comment model, int position) {
                viewHolder.setName(model.getName());
                viewHolder.setContent(model.getComment());
                viewHolder.setTime(model.getTime());
            }
        };
        mCommentList.setAdapter(firebaseRecyclerAdapter);

        final DatabaseReference mDatabaseComment = FirebaseDatabase.getInstance().getReference().child("Comments").child(post_key).push();

        btnAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mDatabase.child(post_key).child("answer").setValue(postPreviousAnswer + 1);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                mDatabaseComment.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mDatabaseComment.child("uid").setValue(user.getUid());
                        mDatabaseComment.child("comment").setValue(txtAnswer.getText().toString().trim());
                        mDatabaseComment.child("name").setValue(user.getDisplayName());
                        mDatabaseComment.child("time").setValue(outputFormat.format(currentLocalTime));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                mDatabase.child(post_key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String postuid = (String) dataSnapshot.child("uid").getValue();
                        String postTitle1 = (String) dataSnapshot.child("title").getValue();
                        DatabaseReference n = noti.child(postuid).push();
                        n.child("isReaded").setValue(false);
                        n.child("notification").setValue(user.getDisplayName() + " đã bình luận bài viết \"" + postTitle1.substring(0, 15) + "...\" của bạn");
                        n.child("time").setValue(outputFormat.format(currentLocalTime));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public static class CommentHolder extends RecyclerView.ViewHolder {
        View mView;

        public CommentHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setName(String _name) {
            TextView name = (TextView) mView.findViewById(R.id.comment_name);
            name.setText(_name);
        }

        public void setTime(String _time) {
            TextView time = (TextView) mView.findViewById(R.id.comment_time);
            time.setText(_time);
        }

        public void setContent(String _title) {
            TextView body = (TextView) mView.findViewById(R.id.comment_content);
            body.setText(_title);
        }
    }
}
