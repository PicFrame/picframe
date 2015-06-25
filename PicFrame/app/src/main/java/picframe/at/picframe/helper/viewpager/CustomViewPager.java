package picframe.at.picframe.helper.viewpager;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;

import java.lang.reflect.Field;

/**
 * Created by ussher on 13.05.15.
 */
public class CustomViewPager extends ViewPager {

    private boolean enabled;
    private Context context;

    public CustomViewPager(Context context) {
        super(context);
        postInitViewPager();
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.enabled = true;
        this.context = context;
        postInitViewPager();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.enabled) {
            return super.onTouchEvent(event);
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (this.enabled) {
            return super.onInterceptTouchEvent(event);
        }
        return false;
    }

    public void setPagingEnabled(boolean enabled) {
        this.enabled = enabled;
    }


    private CustomScroller mScroller = null;

    /**
     * Override the Scroller instance with our own class so we can change the
     * duration
     */
    private void postInitViewPager() {

        try {
            Field scroller = ViewPager.class.getDeclaredField("mScroller");
            scroller.setAccessible(true);
            Field interpolator = ViewPager.class.getDeclaredField("sInterpolator");
            interpolator.setAccessible(true);

            mScroller = new CustomScroller(context,
                    (Interpolator) interpolator.get(null));
            scroller.set(this, mScroller);
        } catch (Exception e) {
        }
    }

    /**
     * Set the factor by which the duration will change
     */
    public void setScrollDurationFactor(double scrollFactor) {
        mScroller.setScrollDurationFactor(scrollFactor);
    }

}



/*    public ViewPagerCustomDuration(Context context, AttributeSet attrs) {
        super(context, attrs);
        postInitViewPager();
    }
*/