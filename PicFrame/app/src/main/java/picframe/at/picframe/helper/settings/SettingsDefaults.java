package picframe.at.picframe.helper.settings;

import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

import picframe.at.picframe.MainApp;
import picframe.at.picframe.R;

public class SettingsDefaults {

    private static final Map<Integer, Object> defValues = new HashMap<>();
    private static Boolean tBool = true;
    private static Boolean fBool = false;

    static {
        defValues.put(R.string.sett_key_username,"");
        defValues.put(R.string.sett_key_password,"");
        defValues.put(R.string.sett_key_slideshow, tBool);
        defValues.put(R.string.sett_key_scaling, fBool);
        defValues.put(R.string.sett_key_randomize, fBool);
        defValues.put(R.string.sett_key_displaytime, "4");
        defValues.put(R.string.sett_key_srctype, "0");
        defValues.put(R.string.sett_key_srcpath_sd, "");
        defValues.put(R.string.sett_key_srcpath_owncloud, "https://");
//        defValues.put(R.string.sett_key_srcpath_dropbox, "https://");
        defValues.put(R.string.sett_key_recursiveSearch, fBool);
        defValues.put(R.string.sett_key_transition, "10");
        defValues.put(R.string.sett_key_downloadInterval, "12");
        defValues.put(R.string.sett_key_tutorial, true);
    }
/*  <string name="sett_key_firstStart" translatable="false">FirstStart</string>     */

    public static Object getDefaultValueForKey(int key) {
        return defValues.get(key);
    }

    public static void resetSettings() {
        SharedPreferences.Editor prefEditor = AppData.getSharedPreferences().edit();
        for (Map.Entry<Integer, Object> prefSet : defValues.entrySet()) {
            if (prefSet.getValue() instanceof String) {
                prefEditor
                    .putString(
                        MainApp.getINSTANCE().getApplicationContext().getString(prefSet.getKey()),
                            (String)prefSet.getValue())
                    .commit();
            } else if (prefSet.getValue() instanceof Boolean) {
                prefEditor
                    .putBoolean(
                        MainApp.getINSTANCE().getApplicationContext().getString(prefSet.getKey()),
                            (Boolean) prefSet.getValue())
                    .commit();
            }
        }
    }
}
