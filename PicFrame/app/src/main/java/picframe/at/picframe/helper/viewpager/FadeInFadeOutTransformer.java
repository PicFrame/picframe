package picframe.at.picframe.helper.viewpager;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Created by ussher on 15.05.15.
 */
public class FadeInFadeOutTransformer implements ViewPager.PageTransformer {
    @Override
    public void transformPage(View page, float position) {
        page.setTranslationX(page.getWidth() * -position);

        if(position <= -1.0F || position >= 1.0F) {
            page.setAlpha(0.0F);
        } else if( position == 0.0F ) {
            page.setAlpha(1.0F);
        } else {
            // position is between -1.0F & 0.0F OR 0.0F & 1.0F
            page.setAlpha(1.0F - Math.abs(position));
        }
    }
}
