package com.phungnlg.hellodoctor.others;

import android.view.View;

import com.daimajia.slider.library.Animations.BaseAnimationInterface;

/**
 * Created by Phil on 6/30/2017.
 */

public class ChildAnimationWithDescription implements BaseAnimationInterface {
    @Override
    public void onPrepareCurrentItemLeaveScreen(View current) {
        View descriptionLayout = current.findViewById(com.daimajia.slider.library.R.id.description_layout);
        if (descriptionLayout != null) {
            current.findViewById(com.daimajia.slider.library.R.id.description_layout).setVisibility(View.VISIBLE);
        }
        //Log.e(TAG, "onPrepareCurrentItemLeaveScreen called");
    }

    @Override
    public void onPrepareNextItemShowInScreen(View next) {
        View descriptionLayout = next.findViewById(com.daimajia.slider.library.R.id.description_layout);
        if (descriptionLayout != null) {
            next.findViewById(com.daimajia.slider.library.R.id.description_layout).setVisibility(View.VISIBLE);
        }
        //Log.e(TAG, "onPrepareNextItemShowInScreen called");
    }

    @Override
    public void onCurrentItemDisappear(View view) {
        //Log.e(TAG, "onCurrentItemDisappear called");
    }

    @Override
    public void onNextItemAppear(View view) {

        View descriptionLayout = view.findViewById(com.daimajia.slider.library.R.id.description_layout);
        if (descriptionLayout != null) {
            view.findViewById(com.daimajia.slider.library.R.id.description_layout).setVisibility(View.VISIBLE);

        }
        //Log.e(TAG, "onCurrentItemDisappear called");
    }
}
