package com.ccojocea.aanews.ui.common;

import android.view.View;

import androidx.viewpager2.widget.ViewPager2;

import timber.log.Timber;

public class HorizontalFlipTransformation implements ViewPager2.PageTransformer {

    private final static int CAMERA_DISTANCE = 12000;

    @Override
    public void transformPage(View page, float position) {
        Timber.d("FlipTransform Position: %s and width: %s", position, page.getWidth());

        page.setTranslationX(-position * page.getWidth());
        page.setCameraDistance(CAMERA_DISTANCE);

        if (position < 0.5 && position > -0.5) {
            page.setVisibility(View.VISIBLE);
        } else {
            page.setVisibility(View.INVISIBLE);
        }

        if (position < -1) {     // [-offScreenPageLimit,-1)
            page.setAlpha(0);

        } else if (position <= 0) {    // [-1,0]
            page.setAlpha(1);
            page.setRotationY(180 * (1 - Math.abs(position) + 1)); //need to add something extra so the page is not flipped when position is 0

        } else if (position <= 1) {    // (0,1]
            page.setAlpha(1);
            page.setRotationY(-180 * (1 - Math.abs(position) + 1)); //need to add something extra so the page is not flipped when position is 0

        } else { // [1, offScreenPageLimit)
            page.setAlpha(0);

        }
    }
}