package picframe.at.picframe.helper.viewpager;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Created by ussher on 20.06.15.
 */
public class RotateDownTransformer implements ViewPager.PageTransformer {
    private static final float ROT_MOD = -15f;
    @Override
    public void transformPage(View page, float position) {

        final float width = page.getWidth();
        final float height = page.getHeight();
        final float rotation = ROT_MOD * position * -1.25f;

        page.setPivotX(width * 0.5f);
        page.setPivotY(height);
        page.setRotation(rotation);
    }
}
