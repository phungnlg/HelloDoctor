package com.phungnlg.hellodoctor;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.goka.blurredgridmenu.GridMenu;
import com.goka.blurredgridmenu.GridMenuFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.blurry.Blurry;

//import android.support.annotation.Nullable;
//import android.support.design.widget.TabLayout;
//import android.support.v7.widget.AppCompatButton;
//import android.view.LayoutInflater;
//import android.view.ViewGroup;
//import android.view.ViewGroup;
//import org.androidannotations.annotations.FragmentArg;

/**
 * Created by Phil on 07/05/2017.
 */
@EFragment(R.layout.fragment_profile)
public class ProfileFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";

    @ViewById(R.id.fragment_profile_tv_user_name)
    protected TextView name;
    @ViewById(R.id.fragment_profile_tv_user_bio)
    protected TextView bio;
    @ViewById(R.id.fragment_profile_tv_user_following)
    protected TextView following;
    @ViewById(R.id.fragment_profile_tv_user_follower)
    protected TextView follower;
    @ViewById(R.id.fragment_profile_list_user_post)
    protected RecyclerView listPost;
    @ViewById(R.id.tab_profile)
    protected RelativeLayout rootView;
    @ViewById(R.id.fragment_profile_iv_image)
    protected com.makeramen.roundedimageview.RoundedImageView ivProfilePic;

    private Boolean isDoctor;
    //private com.makeramen.roundedimageview.RoundedImageView ivProfilePic;
    @ViewById(R.id.fragment_profile_btn_profile1)
    protected ImageButton btnProfile;
    @ViewById(R.id.fragment_profile_btn_Schedule1)
    protected ImageButton btnSchedule;
    @ViewById(R.id.fragment_profile_btn_like)
    protected ImageButton btnLike;

    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("User");
    private DatabaseReference postDatabase = FirebaseDatabase.getInstance().getReference("Posts");
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private LinearLayoutManager layoutManagerProfile;

    private GridMenuFragment mGridMenuFragment;

    private FirebaseRecyclerAdapter<PostItem, NewsFeedFragment.Holder> firebaseRecyclerAdapter;

    public ProfileFragment() {
    }

    public void updateUserProfile() {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setPhotoUri(Uri.parse("http://james.microsite.com/wp-content/uploads/2014/11/doctor-profile-04.jpg"))
                .build();
        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(
                            @NonNull
                                    Task<Void> task) {
                        Log.e("Profile Fragment", "Avatar updated");
                    }
                });
    }

    public void setAvatar() {
        Picasso.with(getContext()).load(user.getPhotoUrl())
                .resize(200, 200)
                .centerCrop()
                .into(ivProfilePic);
    }

    private void setupGridMenu() {
        List<GridMenu> menus = new ArrayList<>();
        menus.add(new GridMenu("Dị ứng", R.drawable.app_images_specialities_di_ung));
        menus.add(new GridMenu("Da liễu", R.drawable.app_images_specialities_da_lieu));
        menus.add(new GridMenu("Dinh dưỡng", R.drawable.app_images_specialities_dinh_duong));
        menus.add(new GridMenu("Hô hấp", R.drawable.app_images_specialities_ho_hap));
        menus.add(new GridMenu("Nhãn khoa", R.drawable.app_images_specialities_mat));
        menus.add(new GridMenu("Nam khoa", R.drawable.app_images_specialities_nam_khoa));
        menus.add(new GridMenu("Ngạo khoa", R.drawable.app_images_specialities_ngoai_khoa));
        menus.add(new GridMenu("Nhi khoa", R.drawable.app_images_specialities_nhi_khoa));
        menus.add(new GridMenu("Nội tiết", R.drawable.app_images_specialities_noi_tiet));
        menus.add(new GridMenu("Phục hồi chức năng", R.drawable.app_images_specialities_phuc_hoi_chuc_nang));
        menus.add(new GridMenu("Răng hàm mặt", R.drawable.app_images_specialities_rang_ham_mat));
        menus.add(new GridMenu("Sản phụ khoa", R.drawable.app_images_specialities_san_phu_khoa));
        menus.add(new GridMenu("Tai mũi họng", R.drawable.app_images_specialities_tai_mui_hong));
        menus.add(new GridMenu("Tâm lý", R.drawable.app_images_specialities_tam_ly_tam_than));
        mGridMenuFragment.setupMenu(menus);
    }

    @Click(R.id.fragment_profile_btn_like)
    public void setBtnLike() {
        Blurry.with(getContext()).radius(25).sampling(2).onto(rootView);
        FragmentTransaction transaction = getActivity().getSupportFragmentManager()
                                                       .beginTransaction().
                                                               setCustomAnimations(R.anim.anim_fade_in,
                                                                                   R.anim.anim_fade_out);
        transaction.replace(R.id.tab_profile, mGridMenuFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Click(R.id.fragment_profile_btn_profile1)
    public void setBtnProfile() {
        CVFragment f = CVFragment_.builder().isEditMode(true).key(user.getUid()).build();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out);
        ft.replace(R.id.tab_profile, f);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Click(R.id.fragment_profile_btn_Schedule1)
    public void setBtnSchedule() {
        ScheduleFragment f = ScheduleFragment_.builder().isEditMode(true).key(user.getUid()).build();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out);
        ft.replace(R.id.tab_profile, f);
        ft.addToBackStack(null);
        ft.commit();
    }

    @AfterViews
    public void init() {
        loadUserInfo();
        //updateUserProfile();
        setAvatar();
        mGridMenuFragment = GridMenuFragment.newInstance(R.drawable.bg_gradient);
        mGridMenuFragment.setOnClickMenuListener(new GridMenuFragment.OnClickMenuListener() {
            @Override
            public void onClickMenu(GridMenu gridMenu, int position) {
                Blurry.delete(rootView);
                Toast.makeText(getActivity(), "Title:" + gridMenu.getTitle() + ", Position:" + position,
                               Toast.LENGTH_SHORT).show();
                getActivity().getSupportFragmentManager().beginTransaction()
                             .setCustomAnimations(R.anim.anim_fade_in, R.anim.anim_fade_out).
                                     remove(mGridMenuFragment).commit();
//                mGridMenuFragment.setupMenu(null);
//                setupGridMenu();
            }
        });
        setupGridMenu();

        initRecyclerView();

        loadUserPosts();
    }

    public void loadUserInfo() {
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
    }

    public void initRecyclerView() {
        layoutManagerProfile = new LinearLayoutManager(this.getContext());
        layoutManagerProfile.setReverseLayout(true);
        layoutManagerProfile.setStackFromEnd(true);
        listPost.setNestedScrollingEnabled(false);
        listPost.setHasFixedSize(true);
        listPost.setLayoutManager(layoutManagerProfile);
        listPost.setFocusable(false);
    }

    public void loadUserPosts() {
        Query sortByTime = postDatabase.orderByChild("uid").equalTo(user.getUid());
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<PostItem, NewsFeedFragment.Holder>(
                PostItem.class,
                R.layout.item_news_feed,
                NewsFeedFragment.Holder.class,
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
                viewHolder.setAvatar(user.getPhotoUrl().toString());
                viewHolder.getmView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        SinglePostFragment f = SinglePostFragment_.builder().postKey(POSTKEY).build();
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out);
                        ft.replace(R.id.tab_profile, f);
                        ft.addToBackStack(null);
                        ft.commit();
                    }
                });
            }
        };
        registerAdapterDataObserver();
        listPost.setAdapter(firebaseRecyclerAdapter);
    }

    public void registerAdapterDataObserver() {
        firebaseRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = firebaseRecyclerAdapter.getItemCount();
                int lastVisiblePosition =
                        layoutManagerProfile.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == -1 ||
                    (positionStart >= (friendlyMessageCount - 1) &&
                     lastVisiblePosition == (positionStart - 1))) {
                    listPost.scrollToPosition(positionStart);
                }
            }
        });
    }

    public static ProfileFragment newInstance(int pageNo) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, pageNo);
        ProfileFragment fragment = new ProfileFragment_();
        fragment.setArguments(args);
        return fragment;
    }
}
