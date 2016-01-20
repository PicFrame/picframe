package picframe.at.picframe.settings;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.preference.SwitchPreference;
import android.util.AttributeSet;

/**
 * Created by MrAdmin on 22.04.2015.
 * used because of known and submitted bug
 * details: http://stackoverflow.com/a/15744076/4716861
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class MySwitchPref extends SwitchPreference {
    public MySwitchPref(Context context) {
        super(context);
    }

    public MySwitchPref(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MySwitchPref(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
}
