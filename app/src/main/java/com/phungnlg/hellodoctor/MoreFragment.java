package com.phungnlg.hellodoctor;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by Phil on 07/05/2017.
 */

public class MoreFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";
    private static final String TAG = "More";

    EditText signOut;
    EditText book;

    public MoreFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_more_utilities, container, false);

        signOut = (EditText) view.findViewById(R.id.more_4);
        book = (EditText) view.findViewById(R.id.more_2);

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Clicked Sign Out");
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getContext(), LogIn.class);
                startActivity(intent);
            }
        });

        book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FindDoctorFragment f = new FindDoctorFragment();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.push_left_in, R.anim.push_left_out);
                ft.replace(R.id.more_fragment, f);
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        return view;
    }

    public static MoreFragment newInstance(int pageNo) {

        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, pageNo);
        MoreFragment fragment = new MoreFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
