package picframe.at.picframe.helper.viewpager;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Created by ussher on 15.06.15.
 */
public class ForegroundToBackgroundTransformer implements ViewPager.PageTransformer{
    @Override
    public void transformPage(View page, float position) {
        final float height = page.getHeight();
        final float width = page.getWidth();
        final float scale = min(position > 0 ? 1f : Math.abs(1f + position), 0.5f);

        page.setScaleX(scale);
        page.setScaleY(scale);
        page.setPivotX(width * 0.5f);
        page.setPivotY(height * 0.5f);
        page.setTranslationX(position > 0 ? width * position : -width * position * 0.25f);
    }

    private static final float min(float val, float min) {
        return val < min ? min : val;
    }
}
