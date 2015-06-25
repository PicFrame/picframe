package picframe.at.picframe.helper.viewpager;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Created by ussher on 15.06.15.
 */
public class DrawFromBackTransformer implements ViewPager.PageTransformer {
    private static final float MIN_SCALE = 0.75f;

    @Override
    public void transformPage(View page, float position) {
        int pageWidth = page.getWidth();

        if (position < -1 || position > 1) { // [-Infinity,-1)
            // This page is way off-screen to the left.
            page.setAlpha(0);
            return;
        }

        if (position <= 0) { // [-1,0]
            // Use the default slide transition when moving to the left page
            // Fade the page out.
            page.setAlpha(1 + position);
            // Counteract the default slide transition
            page.setTranslationX(pageWidth * -position);

            // Scale the page down (between MIN_SCALE and 1)
            float scaleFactor = MIN_SCALE
                    + (1 - MIN_SCALE) * (1 - Math.abs(position));
            page.setScaleX(scaleFactor);
            page.setScaleY(scaleFactor);
            return;

        }

        if (position > 0.5 && position <= 1) { // (0,1]
            // Fade the page out.
            page.setAlpha(0);

            // Counteract the default slide transition
            page.setTranslationX(pageWidth * -position);
            return;
        }
        if (position > 0.3 && position <= 0.5) { // (0,1]
            // Fade the page out.
            page.setAlpha(1);

            // Counteract the default slide transition
            page.setTranslationX(pageWidth * position);

            float scaleFactor = MIN_SCALE;
            page.setScaleX(scaleFactor);
            page.setScaleY(scaleFactor);
            return;
        }
        if (position <= 0.3) { // (0,1]
            // Fade the page out.
            page.setAlpha(1);
            // Counteract the default slide transition
            page.setTranslationX(pageWidth * position);

            // Scale the page down (between MIN_SCALE and 1)
            float v = (float) (0.3 - position);
            v = v >= 0.25f ? 0.25f : v;
            float scaleFactor = MIN_SCALE + v;
            page.setScaleX(scaleFactor);
            page.setScaleY(scaleFactor);
        }
    }
}
