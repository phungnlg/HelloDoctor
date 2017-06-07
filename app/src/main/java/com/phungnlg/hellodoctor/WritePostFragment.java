package com.phungnlg.hellodoctor;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

public class WritePostFragment extends Fragment {
    Spinner major;
    EditText title, body;
    TextView username;
    ImageButton send;

    private FirebaseUser mCurrentUser;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Posts");

    public WritePostFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_write_post, container, false);

        major = (Spinner) view.findViewById(R.id.post_major);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter
                .createFromResource(getContext(), R.array.major, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        major.setAdapter(adapter);

        username = (TextView) view.findViewById(R.id.post_username);
        title = (EditText) view.findViewById(R.id.post_title);
        body = (EditText) view.findViewById(R.id.post_body);
        send = (ImageButton) view.findViewById(R.id.post_send);

        String outputPattern = "h:mm a dd-MM-yyyy";
        String inputPattern = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        final SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+1:00"));
        final Date currentLocalTime = cal.getTime();

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference p = mDatabase.push();

        DatabaseReference userInfo = database.getReference("User").child(mCurrentUser.getUid());
        userInfo.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                username.setText(dataSnapshot.child("name").getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (title.length() < 16 || body.length() <= 25) {
                    Toast.makeText(getContext(),
                                   R.string.post_too_short,
                                   Toast.LENGTH_SHORT).show();
                } else {
                    p.child("answer").setValue(0);
                    p.child("body").setValue(body.getText().toString());
                    p.child("tag").setValue(major.getSelectedItem().toString());
                    p.child("time").setValue(outputFormat.format(currentLocalTime));
                    p.child("title").setValue(title.getText().toString());
                    p.child("uid").setValue(mCurrentUser.getUid().toString());
                    p.child("username").setValue(username.getText().toString());
                    p.child("vote").setValue(0);

                    Toast.makeText(getContext(), "Bài viêt '" + title.getText() + "' đã được đăng thành công!",
                                   Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }
}
