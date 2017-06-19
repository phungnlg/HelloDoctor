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
    private Spinner spnMajor;
    private EditText etTitle;
    private EditText etBody;
    private TextView tvUserName;
    private ImageButton ibSend;

    private FirebaseUser mCurrentUser;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Posts");

    public WritePostFragment() {
    }

    public void onBackPressed() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_write_post, container, false);
        spnMajor = (Spinner) view.findViewById(R.id.fragment_write_post_spn_major);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter
                .createFromResource(getContext(), R.array.major, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnMajor.setAdapter(adapter);

        tvUserName = (TextView) view.findViewById(R.id.fragment_write_post_tv_username);
        etTitle = (EditText) view.findViewById(R.id.fragment_write_post_et_title);
        etBody = (EditText) view.findViewById(R.id.fragment_write_post_et_content);
        ibSend = (ImageButton) view.findViewById(R.id.fragment_write_post_ib_send);

        String outputPattern = "h:mm a dd-MM-yyyy";
        String inputPattern = "yyyy-MM-dd HH:mm:ss";
        final SimpleDateFormat OUTPUTFORMAT = new SimpleDateFormat(outputPattern);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+1:00"));
        final Date CURENTLOCALTIME = cal.getTime();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference POST = mDatabase.push();
        DatabaseReference userInfo = database.getReference("User").child(mCurrentUser.getUid());
        userInfo.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                tvUserName.setText(dataSnapshot.child("name").getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        ibSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etTitle.length() < 16 || etBody.length() <= 25) {
                    Toast.makeText(getContext(),
                                   R.string.post_too_short,
                                   Toast.LENGTH_SHORT).show();
                } else {
                    POST.child("answer").setValue(0);
                    POST.child("body").setValue(etBody.getText().toString());
                    POST.child("tag").setValue(spnMajor.getSelectedItem().toString());
                    POST.child("time").setValue(OUTPUTFORMAT.format(CURENTLOCALTIME));
                    POST.child("title").setValue(etTitle.getText().toString());
                    POST.child("uid").setValue(mCurrentUser.getUid().toString());
                    POST.child("username").setValue(tvUserName.getText().toString());
                    POST.child("vote").setValue(0);

                    Toast.makeText(getContext(), "Bài viêt '" + etTitle.getText() + "' đã được đăng thành công!",
                                   Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }
}
