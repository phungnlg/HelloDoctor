package com.phungnlg.hellodoctor;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
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
import com.phungnlg.hellodoctor.adapter.CommentAdapter;
import com.phungnlg.hellodoctor.adapter.Connect;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Phil on 07/05/2017.
 */
@EFragment(R.layout.fragment_single_post)
public class SinglePostFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this.getContext());

    //private LinkedHashMap<String, CommentItem> listComment = new LinkedHashMap<>();
    private CommentAdapter commentAdapter;

    //private String postKey;
    private String outputPattern = "h:mm a dd-MM-yyyy";
    private String inputPattern = "yyyy-MM-dd HH:mm:ss";
    private SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
    private SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);
    private Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+1:00"));
    private Date localTime = cal.getTime();

    private DatabaseReference mDatabase;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference notificationDatabase;

    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @FragmentArg
    protected String postKey;

    @ViewById(R.id.fragment_single_post_tv_user_name)
    protected TextView postName;
    @ViewById(R.id.fragment_single_post_tv_body)
    protected TextView postStatus;
    @ViewById(R.id.fragment_single_post_tv_title)
    protected TextView postTitle;
    @ViewById(R.id.fragment_single_post_tv_category)
    protected TextView postTag;
    @ViewById(R.id.fragment_single_post_tv_time)
    protected TextView postTime;
    @ViewById(R.id.fragment_single_post_tv_like_count)
    protected TextView postLikeCount;
    @ViewById(R.id.fragment_single_post_ib_send)
    protected ImageButton btnAnswer;
    @ViewById(R.id.fragment_single_post_et_comment)
    protected EditText txtAnswer;
    @ViewById(R.id.fragment_single_post_list_comment)
    protected RecyclerView mCommentList;

    private String name;

    private long postPreviousVote;
    private long postPreviousAnswer;

    public SinglePostFragment() {
        // Required empty public constructor
    }

    @Click(R.id.fragment_single_post_iv_user_pic)
    public void setBtnBack() {
        getActivity().getSupportFragmentManager().beginTransaction()
                     .setCustomAnimations(R.anim.push_right_in, R.anim.push_right_out)
                     .remove(this).commit();
    }

    @Click(R.id.cardView)
    public void setBack() {
        getActivity().getSupportFragmentManager().beginTransaction()
                     .setCustomAnimations(R.anim.push_right_in, R.anim.push_right_out)
                     .remove(this).commit();
    }

    @Click(R.id.fragment_single_post_ib_send)
    public void setBtnAnswer() {
        increaseCommentCount();
        saveCommentToDatabase();
        pushNotification();
        loadComments();

    }

    public void increaseCommentCount() {
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mDatabase.child(postKey).child("answer").setValue(postPreviousAnswer + 1);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void saveCommentToDatabase() {
        final DatabaseReference DATABASECOMMENT = FirebaseDatabase.getInstance().getReference().child("Comments")
                                                                  .child(postKey).push();
        DATABASECOMMENT.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DATABASECOMMENT.child("uid").setValue(user.getUid());
                DATABASECOMMENT.child("comment").setValue(txtAnswer.getText().toString().trim());
                DATABASECOMMENT.child("name").setValue(user.getDisplayName());
                DATABASECOMMENT.child("time").setValue(outputFormat.format(localTime));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void pushNotification() {
        mDatabase.child(postKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String postuid = (String) dataSnapshot.child("uid").getValue();
                String postTitle1 = (String) dataSnapshot.child("title").getValue();
                DatabaseReference n = notificationDatabase.child(postuid).push();
                n.child("isReaded").setValue(false);
                n.child("notification").setValue(
                        user.getDisplayName() + " đã bình luận bài viết \"" + postTitle1.substring(0, 15) +
                        "...\" của bạn");
                n.child("time").setValue(outputFormat.format(localTime));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public static SinglePostFragment newInstance(String param1, String param2) {
        SinglePostFragment fragment = new SinglePostFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public void initDatabaseConnection() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Posts");
        notificationDatabase = database.getReference("Notifications");
    }

    public void getPost() {
        mDatabase.child(postKey).addValueEventListener(new ValueEventListener() {
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
    }

    private Observable test() {
        getPreviousComment();
        return Observable.just(1);
    }

    private void doSomething() {
        test().observeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    public void getPreviousComment() {
        final DatabaseReference DATABASECOMMENTLIST = FirebaseDatabase.getInstance().getReference().child("Comments")
                                                                      .child(postKey);
        final FirebaseRecyclerAdapter<CommentItem, CommentHolder> COMMENTADAPTER
                = new FirebaseRecyclerAdapter<CommentItem, CommentHolder>(
                CommentItem.class,
                R.layout.item_comment,
                CommentHolder.class,
                DATABASECOMMENTLIST
        ) {
            @Override
            protected void populateViewHolder(CommentHolder viewHolder, CommentItem model, int position) {
                viewHolder.setName(model.getName());
                viewHolder.setContent(model.getComment());
                viewHolder.setTime(model.getTime());
            }
        };

        COMMENTADAPTER.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(final int positionStart, final int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = COMMENTADAPTER.getItemCount();
                int lastVisiblePosition =
                        linearLayoutManager.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == -1
                    || (positionStart >= (friendlyMessageCount - 1)
                        && lastVisiblePosition == (positionStart - 1))) {
                    mCommentList.scrollToPosition(positionStart);
                }
            }
        });

        mCommentList.setAdapter(COMMENTADAPTER);
    }

    public void registerAdapterDataObserver() {
        commentAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(final int positionStart, final int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = commentAdapter.getItemCount();
                int lastVisiblePosition =
                        linearLayoutManager.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == -1
                    || (positionStart >= (friendlyMessageCount - 1)
                        && lastVisiblePosition == (positionStart - 1))) {
                    mCommentList.scrollToPosition(positionStart);
                }
            }
        });
    }

    public void loadComments() {
        Observable.defer(() -> Connect.getRetrofit().getComments(postKey))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(CommentItemLinkedHashMap -> {
                    commentAdapter = new CommentAdapter(CommentItemLinkedHashMap);
                    commentAdapter.notifyDataSetChanged();
                    registerAdapterDataObserver();
                    mCommentList.setAdapter(commentAdapter);
                });
    }

    @AfterViews
    public void init() {
        initDatabaseConnection();
        getPost();
        getCommentList();
        //getPreviousComment();
        doSomething();
        //loadComments();
        //registerAdapterDataObserver();
    }

    public void getCommentList() {
        mCommentList.setNestedScrollingEnabled(false);
        mCommentList.setHasFixedSize(true);
        linearLayoutManager.setReverseLayout(true);
        mCommentList.setLayoutManager(linearLayoutManager);
    }

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
        private View mView;

        public CommentHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setName(String _name) {
            TextView name = (TextView) mView.findViewById(R.id.item_comment_tv_name);
            name.setText(_name);
        }

        public void setTime(String _time) {
            TextView time = (TextView) mView.findViewById(R.id.item_comment_tv_time);
            time.setText(_time);
        }

        public void setContent(String _title) {
            TextView body = (TextView) mView.findViewById(R.id.item_comment_tv_content);
            body.setText(_title);
        }
    }
}
