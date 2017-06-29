package com.phungnlg.hellodoctor;

import android.support.v4.app.Fragment;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Phil on 07/05/2017.
 */
@EFragment(R.layout.fragment_write_post)
public class WritePostFragment extends Fragment {
    private FirebaseUser mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Posts");

    private String outputPattern = "h:mm a dd-MM-yyyy";

    private SimpleDateFormat outputformat = new SimpleDateFormat(outputPattern);
    private Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+1:00"));
    private Date curentLocalTime = cal.getTime();

    @ViewById(R.id.fragment_write_post_spn_major)
    protected Spinner spnMajor;
    @ViewById(R.id.fragment_write_post_et_title)
    protected EditText etTitle;
    @ViewById(R.id.fragment_write_post_et_content)
    protected EditText etBody;
    @ViewById(R.id.fragment_write_post_tv_username)
    protected TextView tvUserName;
    @ViewById(R.id.fragment_write_post_ib_send)
    protected ImageButton ibSend;

    public WritePostFragment() {
    }

    public void setAdapterSpnMajor() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter
                .createFromResource(getContext(), R.array.major, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnMajor.setAdapter(adapter);
    }

    @Click(R.id.fragment_write_post_ib_send)
    public void setBtnSend() {
        if (etTitle.length() < 16 || etBody.length() <= 25) {
            Toast.makeText(getContext(),
                           R.string.post_too_short,
                           Toast.LENGTH_SHORT).show();
        } else {
            final DatabaseReference POST = mDatabase.push();

            POST.child("answer").setValue(0);
            POST.child("body").setValue(etBody.getText().toString());
            POST.child("tag").setValue(spnMajor.getSelectedItem().toString());
            POST.child("time").setValue(outputformat.format(curentLocalTime));
            POST.child("title").setValue(etTitle.getText().toString());
            POST.child("uid").setValue(mCurrentUser.getUid().toString());
            POST.child("username").setValue(tvUserName.getText().toString());
            POST.child("vote").setValue(0);
            POST.child("voter").setValue("_");

            Toast.makeText(getContext(), "Bài viết '" + etTitle.getText() + "' đã được đăng thành công!",
                           Toast.LENGTH_SHORT).show();
        }
    }

    @AfterViews
    public void init() {
        setAdapterSpnMajor();
        tvUserName.setText(mCurrentUser.getDisplayName());
    }

    public void onBackPressed() {

    }
}
