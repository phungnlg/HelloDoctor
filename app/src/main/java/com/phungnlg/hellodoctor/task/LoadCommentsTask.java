package com.phungnlg.hellodoctor.task;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.phungnlg.hellodoctor.CommentItem;
import com.phungnlg.hellodoctor.R;
import com.phungnlg.hellodoctor.SinglePostFragment;

/**
 * Created by Phil on 7/14/2017.
 */

public class LoadCommentsTask extends AsyncTask<Void, FirebaseRecyclerAdapter, Void> {
    private Activity context;
    private String postKey;
    private LinearLayoutManager linearLayoutManager;

    public LoadCommentsTask(Activity context, String postKey, LinearLayoutManager linearLayoutManager) {
        this.context = context;
        this.postKey = postKey;
        this.linearLayoutManager = linearLayoutManager;
    }

    @Override
    protected Void doInBackground(Void... params) {
        final DatabaseReference DATABASECOMMENTLIST = FirebaseDatabase.getInstance().getReference().child("Comments")
                                                                      .child(postKey);
        final FirebaseRecyclerAdapter<CommentItem, SinglePostFragment.CommentHolder> COMMENTADAPTER
                = new FirebaseRecyclerAdapter<CommentItem, SinglePostFragment.CommentHolder>(
                CommentItem.class,
                R.layout.item_comment,
                SinglePostFragment.CommentHolder.class,
                DATABASECOMMENTLIST
        ) {
            @Override
            protected void populateViewHolder(SinglePostFragment.CommentHolder viewHolder,
                                              CommentItem model, int position) {
                viewHolder.setName(model.getName());
                viewHolder.setContent(model.getComment());
                viewHolder.setTime(model.getTime());
            }
        };


        publishProgress(COMMENTADAPTER);
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Toast.makeText(context, "Comments are loading", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Toast.makeText(context, "Comments have been loaded successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onProgressUpdate(FirebaseRecyclerAdapter... adapters) {
        super.onProgressUpdate(adapters);
        RecyclerView listComment = (RecyclerView) context.findViewById(R.id.fragment_single_post_list_comment);

        adapters[0].registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(final int positionStart, final int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = adapters[0].getItemCount();
                int lastVisiblePosition =
                        linearLayoutManager.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == -1
                    || (positionStart >= (friendlyMessageCount - 1)
                        && lastVisiblePosition == (positionStart - 1))) {
                    listComment.scrollToPosition(positionStart);
                }
            }
        });

        listComment.setAdapter(adapters[0]);
    }
}
