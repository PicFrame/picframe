package picframe.at.picframe.helper.viewpager;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Created by ussher on 20.06.15.
 */
public class StackTransformer implements ViewPager.PageTransformer {
    @Override
    public void transformPage(View page, float position) {
        page.setTranslationX(position < 0 ? 0f : -page.getWidth() * position);
    }
}
