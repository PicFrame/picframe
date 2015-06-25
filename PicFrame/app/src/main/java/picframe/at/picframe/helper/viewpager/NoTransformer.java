package picframe.at.picframe.helper.viewpager;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Created by ussher on 20.06.15.
 */
public class NoTransformer implements ViewPager.PageTransformer {
    @Override
    public void transformPage(View page, float position) {
        if (position < 0) {
            page.setScrollX((int)((float)(page.getWidth()) * position));
        } else if (position > 0) {
            page.setScrollX(-(int) ((float) (page.getWidth()) * -position));
        } else {
            page.setScrollX(0);
        }
    }
}
