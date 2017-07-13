package com.phungnlg.hellodoctor;

import android.app.ProgressDialog;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.phungnlg.hellodoctor.adapter.Connect;
import com.phungnlg.hellodoctor.adapter.NewsAdapter;
import com.phungnlg.hellodoctor.others.ChildAnimation;
import com.phungnlg.hellodoctor.others.SliderLayout;
import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.HashMap;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Phil on 07/05/2017.
 */
@EFragment(R.layout.fragment_news_feed)
public class NewsFeedFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";
    private static final String TAG = "NewsFeedFragment";

    @ViewById(R.id.fragment_newsfeed_et_question)
    protected EditText etQuestion;
    @ViewById(R.id.fragment_newsfeed_list)
    protected RecyclerView mBlogList;
    @ViewById(R.id.fragment_newsfeed_slider)
    protected SliderLayout sliderLayout;
    @ViewById(R.id.fragment_newsfeed_news)
    protected RecyclerView newsList;
    private DatabaseReference mDatabase;
    private DatabaseReference newsDatabaseReference;
    private DatabaseReference voteData = FirebaseDatabase.getInstance().getReference("Vote");
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private Boolean isLiked = false;
    private int mPageNo;
    private LinearLayoutManager postLayoutManager;
    private LinearLayoutManager newsLayoutManager;
    private ProgressDialog progressDialog;
    private FirebaseRecyclerAdapter<PostItem, Holder> postAdapter;
    private StorageReference imageRef = FirebaseStorage.getInstance().getReference("avatar");

    private NewsAdapter newsAdapter;

    public NewsFeedFragment() {
    }

    private void showDialog() {
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Đang tải bài viết");
        progressDialog.show();
    }

    public static NewsFeedFragment newInstance(int pageNo) {

        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, pageNo);
        NewsFeedFragment fragment = new NewsFeedFragment_();
        fragment.setArguments(args);
        return fragment;
    }

    public void setUpLayoutManager() {
        postLayoutManager = new LinearLayoutManager(this.getContext()) {
            @Override
            public void onLayoutChildren(final RecyclerView.Recycler recycler, final RecyclerView.State state) {
                super.onLayoutChildren(recycler, state);
                //TODO if the items are filtered, considered hiding the fast scroller here
                final int firstVisibleItemPosition = findFirstVisibleItemPosition();
                if (firstVisibleItemPosition != 0) {
                    if (firstVisibleItemPosition == -1) {
                        showDialog();
                    }
                    return;
                }
                final int lastVisibleItemPosition = findLastVisibleItemPosition();
                int itemsShown = lastVisibleItemPosition - firstVisibleItemPosition + 1;
                if (postAdapter.getItemCount() > itemsShown) {
                    showDialog();
                } else {
                    progressDialog.dismiss();
                }
            }
        };
        postLayoutManager.setReverseLayout(true);
        postLayoutManager.setStackFromEnd(true);
        newsLayoutManager = new LinearLayoutManager(this.getContext(),
                                                    LinearLayoutManager.HORIZONTAL, false);
    }

    public void initDatabaseConnection() {
        mPageNo = getArguments().getInt(ARG_PAGE);
        mDatabase = FirebaseDatabase.getInstance().getReference("Posts");
        mDatabase.keepSynced(true);
        newsDatabaseReference = FirebaseDatabase.getInstance().getReference("News");
    }

    public void setUpSliderBanner() {
        HashMap<String, Integer> fileMaps = new HashMap<String, Integer>();
        fileMaps.put(getResources().getString(R.string.link3), R.drawable.bg_slide3);
        fileMaps.put(getResources().getString(R.string.link4), R.drawable.bg_slide4);
        fileMaps.put(getResources().getString(R.string.link6), R.drawable.bg_slide6);
        fileMaps.put(getResources().getString(R.string.link7), R.drawable.bg_slide7);
        fileMaps.put(getResources().getString(R.string.link5), R.drawable.bg_slide5);
        fileMaps.put(getResources().getString(R.string.link1), R.drawable.bg_slide1);
        for (final String name : fileMaps.keySet()) {
            TextSliderView textSliderView = new TextSliderView(getContext());
            textSliderView
                    .description(name)
                    .image(fileMaps.get(name))
                    .setScaleType(BaseSliderView.ScaleType.CenterCrop)
                    .setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                        @Override
                        public void onSliderClick(BaseSliderView slider) {
                            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                            builder.setToolbarColor(47219);
                            CustomTabsIntent customTabsIntent = builder.build();
                            //Toast.makeText(getContext(), name, Toast.LENGTH_LONG).show();
                            customTabsIntent.launchUrl(getContext(), Uri.parse(name));
                        }
                    });
            textSliderView.bundle(new Bundle());
            textSliderView.getBundle().putString("extra", name);

            sliderLayout.addSlider(textSliderView);
        }
        sliderLayout.setPresetTransformer(SliderLayout.Transformer.Default);
        sliderLayout.setPresetIndicator(SliderLayout.PresetIndicators.Right_Bottom);
        sliderLayout.setCustomAnimation(new ChildAnimation());
        sliderLayout.setDuration(3000);
        //sliderLayout.setIndicatorVisibility(PagerIndicator.IndicatorVisibility.Invisible);
        sliderLayout.startAutoCycle();
        //sliderLayout.addOnPageChangeListener(getContext());
    }

    public void setUpRecyclerView() {
        mBlogList.setNestedScrollingEnabled(false);
        mBlogList.setHasFixedSize(true);
        newsList.setNestedScrollingEnabled(false);
        newsList.setHasFixedSize(true);
    }

    public void loadPost() {
        Query sortByTime = mDatabase.orderByKey().limitToLast(50);

        postAdapter = new FirebaseRecyclerAdapter<PostItem, Holder>(
                PostItem.class,
                R.layout.item_news_feed,
                Holder.class,
                mDatabase
                //sortByTime
        ) {
            @Override
            protected void populateViewHolder(final Holder viewHolder,
                                              final PostItem model,
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
                StorageReference avatar = imageRef.child(model.getUid() + ".jpg");
                avatar.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        viewHolder.setAvatar(uri.toString());
                    }
                });
                //viewHolder.setAvatar(avatarUrl);

                final long PREVIOUSVOTE = model.getVote();
                final String PREVIOUSVOTER = model.getVoter();

                if (PREVIOUSVOTER.contains(user.getUid())) {
                    int myColor = getResources().getColor(R.color.themecolor);
                    viewHolder.btnLike.setColorFilter(myColor, PorterDuff.Mode.SRC_ATOP);
                }

                viewHolder.tvTitle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Bundle bundle = new Bundle();
                        bundle.putString("post_key", POSTKEY);

                        SinglePostFragment f = SinglePostFragment_.builder().postKey(POSTKEY).build();
                        //f.setArguments(bundle);
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out);
                        ft.replace(R.id.newsfeed, f);
                        ft.addToBackStack(null);
                        ft.commit();
                    }
                });

                viewHolder.tvUserName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putString("uid", model.getUid());
                        bundle.putString("userName", model.getUsername());

                        ChatFragment chatFragment = new ChatFragment_();
                        chatFragment.setArguments(bundle);
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out);
                        ft.replace(R.id.newsfeed, chatFragment);
                        ft.addToBackStack(null);
                        ft.commit();
                    }
                });

                viewHolder.btnLike.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (PREVIOUSVOTER.contains(user.getUid())) {
                            int myColor = getResources().getColor(R.color.black);
                            viewHolder.btnLike.setColorFilter(myColor, PorterDuff.Mode.SRC_ATOP);

                            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    mDatabase.child(POSTKEY).child("vote").setValue(PREVIOUSVOTE - 1);
                                    mDatabase.child(POSTKEY).child("voter")
                                             .setValue(PREVIOUSVOTER.replaceAll(user.getUid() + "_", ""));
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });
                        } else {
                            int myColor = getResources().getColor(R.color.themecolor);
                            viewHolder.btnLike.setColorFilter(myColor, PorterDuff.Mode.SRC_ATOP);

                            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    mDatabase.child(POSTKEY).child("vote").setValue(PREVIOUSVOTE + 1);
                                    mDatabase.child(POSTKEY).child("voter")
                                             .setValue(PREVIOUSVOTER + user.getUid() + "_");
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });
                        }
                    }
                });
            }
        };

        postAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(final int positionStart, final int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = postAdapter.getItemCount();
                int lastVisiblePosition =
                        postLayoutManager.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == -1
                    || (positionStart >= (friendlyMessageCount - 1)
                        && lastVisiblePosition == (positionStart - 1))) {
                    mBlogList.scrollToPosition(positionStart);
                }
            }
        });

        mBlogList.setLayoutManager(postLayoutManager);
        mBlogList.setAdapter(postAdapter);
    }

    public void loadNews() {
        final FirebaseRecyclerAdapter<NewsItem, NewsHolder> NEWSADAPTER =
                new FirebaseRecyclerAdapter<NewsItem, NewsHolder>(
                        NewsItem.class,
                        R.layout.item_news,
                        NewsHolder.class,
                        newsDatabaseReference
                ) {
                    @Override
                    protected void populateViewHolder(NewsHolder viewHolder, final NewsItem model, int position) {
                        viewHolder.setPhoto(model.getPhotoUrl());
                        viewHolder.setTitle(model.getTitle());

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                                builder.setToolbarColor(47219);
                                CustomTabsIntent customTabsIntent = builder.build();
                                customTabsIntent.launchUrl(v.getContext(), Uri.parse(model.getContentUrl()));
                            }
                        });
                    }
                };
        newsList.setLayoutManager(newsLayoutManager);
        newsList.setAdapter(NEWSADAPTER);
    }

    public void rloadNews() {
        Observable.defer(() -> Connect.getRetrofit().getNewsList())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(NewsItemHashMap -> {
                    newsAdapter = new NewsAdapter(NewsItemHashMap);
                    newsAdapter.notifyDataSetChanged();
                    newsList.setLayoutManager(newsLayoutManager);
                    newsList.setAdapter(newsAdapter);
                });
    }

    @AfterViews
    public void init() {
        progressDialog = new ProgressDialog(getActivity(),
                                            R.style.AppTheme_Dark_Dialog);
        setUpLayoutManager();
        initDatabaseConnection();
        setUpSliderBanner();
        setUpRecyclerView();
        loadPost();
        rloadNews();
    }

    @Click(R.id.fragment_newsfeed_et_question)
    public void setEtQuestion() {
        WritePostFragment f = WritePostFragment_.builder().build();
        //f.setArguments(bundle);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out);
        ft.replace(R.id.newsfeed, f);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public void onStop() {
        sliderLayout.stopAutoCycle();
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public static class Holder extends RecyclerView.ViewHolder {
        private View mView;
        private ImageButton btnLike;
        private TextView tvUserName;
        private TextView tvTitle;

        public Holder(View itemView) {
            super(itemView);
            mView = itemView;
            btnLike = (ImageButton) mView.findViewById(R.id.item_newsfeed_ib_like);
            tvUserName = (TextView) mView.findViewById(R.id.item_newsfeed_tv_user_name);
            tvTitle = (TextView) mView.findViewById(R.id.item_newsfeed_tv_post_title);
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

        public void setAvatar(String photoUrl) {
            ImageView iv = (ImageView) mView.findViewById(R.id.fragment_single_post_iv_user_pic);
            Picasso.with(mView.getContext())
                   .load(photoUrl)
                   .resize(105, 105)
                   .centerCrop()
                   .into(iv);
        }
    }

    public static class NewsHolder extends RecyclerView.ViewHolder {
        private View mView;

        public NewsHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setPhoto(String photoUrl) {
            ImageView iv = (ImageView) mView.findViewById(R.id.item_news_photo);
            Picasso.with(mView.getContext())
                   .load(photoUrl)
                   .resize(200, 305)
                   .centerCrop()
                   .into(iv);
        }

        public void setTitle(String _title) {
            TextView body = (TextView) mView.findViewById(R.id.item_news_tv_title);
            body.setText(_title);
        }
    }
}
