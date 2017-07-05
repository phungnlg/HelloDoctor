package com.phungnlg.hellodoctor;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.HashMap;
import java.util.Map;

//import android.graphics.drawable.Drawable;
//import android.view.animation.Animation;
//import android.view.animation.AnimationUtils;
//import android.widget.Toast;

/**
 * Created by Phil on 6/23/2017.
 */

@EFragment(R.layout.fragment_chat)
public class ChatFragment extends Fragment {
    @ViewById(R.id.fragment_chat_tv_user_name)
    protected TextView tvUserName;
    @ViewById(R.id.fragment_chat_tv_bio)
    protected TextView tvUserBio;

    private String userName;
    private String userid;

    @ViewById(R.id.fragment_chat_ib_send)
    protected ImageButton ibSend;
    @ViewById(R.id.fragment_chat_et_message)
    protected EditText etMessage;
    @ViewById(R.id.fragment_chat_layout1)
    protected LinearLayout layout;
    @ViewById(R.id.fragment_chat_layout2)
    protected RelativeLayout layoutRelative;
    @ViewById(R.id.fragment_chat_scrollView)
    protected ScrollView scrollView;

    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private DatabaseReference refFrom;
    private DatabaseReference refTo;

    public ChatFragment() {

    }

    public void getBundle() {
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            userName = bundle.getString("userName");
            userid = bundle.getString("uid");
        }
    }

    @Click(R.id.fragment_chat_ib_send)
    public void setBtnSend() {
        String message = etMessage.getText().toString();
        if (!message.equals("")) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("message", message);
            map.put("user", user.getUid());
            refFrom.child(userid).push().setValue(map);
            refTo.child(user.getUid()).push().setValue(map);
            etMessage.setText("");
        }
    }

    @Click(R.id.fragment_chat_ib_back)
    public void setBtnBack() {
        getActivity().getSupportFragmentManager().beginTransaction()
                     .setCustomAnimations(R.anim.push_right_in, R.anim.push_right_out)
                     .remove(this).commit();
    }

    public void initDatabaseConnection() {
        refFrom = FirebaseDatabase.getInstance().getReference().child("Chat").child(user.getUid());
        refTo = FirebaseDatabase.getInstance().getReference().child("Chat").child(userid);
    }

    public void getPreviousMessages() {
        refFrom.child(userid).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                GenericTypeIndicator<Map<String, String>> genericTypeIndicator
                        = new GenericTypeIndicator<Map<String, String>>() { };
                Map<String, String> map = dataSnapshot.getValue(genericTypeIndicator);
                String message = map.get("message");
                String userName = map.get("user");

                if (userName.equals(userid)) {
                    //addMessageBox(userName + "\n" + message, 2);
                    addMessageBox(message, 2);
                } else {
                    //addMessageBox("You: \n" + message, 1);
                    addMessageBox(message, 1);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void performFullScroll() {
        scrollView.post(new Runnable() {
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    public void setChatWithUserInfo() {
        tvUserName.setText(userName);
        tvUserBio.setText("Đang online");
//        Drawable img = getContext().getResources().getDrawable(R.drawable.tickmark);
//        tvUserBio.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
    }

    @AfterViews
    public void init() {
        getBundle();
        performFullScroll();
        setChatWithUserInfo();
        initDatabaseConnection();
        getPreviousMessages();
        //etMessage.requestFocus();
    }

    public void addMessageBox(String message, int type) {
        TextView textView = new TextView(getContext());
        textView.setText(message);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                                                      ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 1.0f;

        if (type == 1) {
            layoutParams.gravity = Gravity.END;
            textView.setTextColor(Color.BLUE);
            textView.setBackgroundResource(R.drawable.bubble_in);

//            View viewChatMine = View.inflate(getContext(), R.layout.item_chat_mine, layout);
//            TextView txtChatMessage = (TextView) viewChatMine.findViewById(R.id.text_view_chat_message);
//            TextView txtUserAlphabet = (TextView) viewChatMine.findViewById(R.id.text_view_user_alphabet);
//            txtChatMessage.setText(message);
//            viewChatMine.setLayoutParams(layoutParams);
//            txtUserAlphabet.setText("A");
//            if(viewChatMine.getParent() != null)
//                ((ViewGroup)viewChatMine.getParent()).removeView(viewChatMine);
//            layout.addView(viewChatMine);
        } else {
            layoutParams.gravity = Gravity.START;
            textView.setBackgroundResource(R.drawable.bubble_out);
        }
        textView.setLayoutParams(layoutParams);

//        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.push_left_in);
//        animation.reset();
//        textView.clearAnimation();
//        textView.startAnimation(animation);

        textView.setMaxWidth(600);
        layout.addView(textView);
        performFullScroll();
    }
}
