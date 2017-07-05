package com.phungnlg.hellodoctor;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Phil on 07/05/2017.
 */
@EFragment(R.layout.fragment_more_utilities)
public class MoreFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";
    private static final String TAG = "More";

    @ViewById(R.id.fragment_more_btn_sign_out)
    protected EditText btnSignOut;
    @ViewById(R.id.fragment_more_btn_find_doctor)
    protected EditText book;
    @ViewById(R.id.fragment_more_btn_appointment)
    protected  EditText btnService;

    public MoreFragment() {
        // Required empty public constructor
    }

    @Click(R.id.fragment_more_btn_sign_out)
    public void setBtnSignOut() {
        Log.d(TAG, "Clicked Sign Out");
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getContext(), LogInActivity_.class);
        startActivity(intent);
    }

    @Click(R.id.fragment_more_btn_find_doctor)
    public void setBook() {
        FindDoctorFragment f = new FindDoctorFragment();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out);
        ft.replace(R.id.more_fragment, f);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Click(R.id.fragment_more_btn_appointment)
    public void setBtnT() {
        Intent intent = new Intent(getActivity(), ServiceActivity_.class);
        startActivity(intent);
        //getActivity().finish();
        getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    public static MoreFragment newInstance(int pageNo) {

        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, pageNo);
        MoreFragment fragment = new MoreFragment_();
        fragment.setArguments(args);
        return fragment;
    }
}
