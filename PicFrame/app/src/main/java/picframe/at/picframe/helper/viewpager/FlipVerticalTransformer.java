package picframe.at.picframe.helper.viewpager;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Created by ussher on 20.06.15.
 */
public class FlipVerticalTransformer implements ViewPager.PageTransformer {
    @Override
    public void transformPage(View page, float position) {
        final float rotation = -180f * position;

        page.setAlpha(rotation > 90f || rotation < -90f ? 0f : 1f);
        page.setPivotX(page.getWidth() * 0.5f);
        page.setPivotY(page.getHeight() * 0.5f);
        page.setRotationX(rotation);
    }
}
