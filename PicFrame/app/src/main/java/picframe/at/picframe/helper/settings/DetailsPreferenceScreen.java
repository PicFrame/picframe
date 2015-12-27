package picframe.at.picframe.helper.settings;

import android.preference.Preference;
import android.preference.PreferenceScreen;

import java.util.ArrayList;
import java.util.Arrays;

import picframe.at.picframe.R;
import picframe.at.picframe.activities.SettingsActivity;
import picframe.at.picframe.helper.settings.detailsPrefScreen.ExtSdPrefs;
import picframe.at.picframe.helper.settings.detailsPrefScreen.OwnCloudPrefs;

public class DetailsPreferenceScreen {
    private AppData.sourceTypes mSrcType;
    private String mLocalizedSrcTypeValue;
    private PreferenceScreen mPreferenceScreen;
    private SettingsActivity mSettAct;

    public DetailsPreferenceScreen(int srcType, PreferenceScreen preferenceScreen,
                                   SettingsActivity settingsActivity) {

        mSrcType = AppData.sourceTypes.getSourceTypesForInt(srcType);
        mPreferenceScreen = preferenceScreen;
        mSettAct = settingsActivity;

        String[] srcValueArray = mSettAct.getResources().getStringArray(R.array.srcTypeEntries);
        mLocalizedSrcTypeValue = Arrays.asList(srcValueArray).get(srcType);

        prefScreenSetup();
    }

    private void prefScreenSetup() {
        mPreferenceScreen.setTitle(mLocalizedSrcTypeValue + " " +
                mSettAct.getString(R.string.sett_detailsPrefScreen_title));
        mPreferenceScreen.setSummary(mSettAct.getString(R.string.sett_detailsPrefScreen_summ) +
                " " + mLocalizedSrcTypeValue);
        mPreferenceScreen.setKey(mSettAct.getString(R.string.sett_key_prefScreenDetails));

        ArrayList<Preference> allPrefs = new ArrayList<>();
        if (AppData.sourceTypes.OwnCloud.equals(mSrcType)) {
            allPrefs = new OwnCloudPrefs(mSettAct).getAllDetailPreferenceFields();
        } else //noinspection StatementWithEmptyBody
            if (AppData.sourceTypes.Dropbox.equals(mSrcType)) {
            // TODO
        } else {
            allPrefs = new ExtSdPrefs(mSettAct).getAllDetailPreferenceFields();
        }
        for (Preference p : allPrefs) {
            if (p != null) {
                mPreferenceScreen.addPreference(p);
            }
        }
    }

    public PreferenceScreen getPreferenceScreen() {
        return mPreferenceScreen;
    }
}
