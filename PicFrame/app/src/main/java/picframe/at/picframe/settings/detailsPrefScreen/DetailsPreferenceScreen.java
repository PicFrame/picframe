package picframe.at.picframe.settings.detailsPrefScreen;

import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.view.ViewGroup;

import java.util.Arrays;

import picframe.at.picframe.R;
import picframe.at.picframe.activities.SettingsActivity;
import picframe.at.picframe.settings.AppData;

public class DetailsPreferenceScreen {
    private AppData.sourceTypes mSrcType;
    private String mLocalizedSrcTypeValue;
    private PreferenceScreen mPreferenceScreen;
    private SettingsActivity mSettAct;
    private ViewGroup mStatusViewGroup;
    private IDetailsPreferenceScreen detailsPreferenceScreen;

    public DetailsPreferenceScreen(int srcType, PreferenceScreen preferenceScreen,
                                   SettingsActivity settingsActivity) {

        mSrcType = AppData.sourceTypes.getSourceTypesForInt(srcType);
        mPreferenceScreen = preferenceScreen;
        mSettAct = settingsActivity;

        String[] srcValueArray = mSettAct.getResources().getStringArray(R.array.srcTypeEntries);
        mLocalizedSrcTypeValue = Arrays.asList(srcValueArray).get(srcType);

        fetchCorrectDetailsPrefScreen();
        if (detailsPreferenceScreen != null) {
            mStatusViewGroup = detailsPreferenceScreen.getStatusViewGroup();
            prefScreenSetup();
        }
    }

    private void fetchCorrectDetailsPrefScreen() {
        if (AppData.sourceTypes.OwnCloud.equals(mSrcType)) {
            detailsPreferenceScreen = new OwnCloudPrefs(mSettAct);
        } else if (AppData.sourceTypes.Dropbox.equals(mSrcType)) {
            // TODO
            //detailsPreferenceScreen = new
        } else {
            detailsPreferenceScreen = new ExtSdPrefs(mSettAct);
        }
    }

    private void prefScreenSetup() {
        mPreferenceScreen.setTitle(mLocalizedSrcTypeValue + " " +
                mSettAct.getString(R.string.sett_detailsPrefScreen_title));
        mPreferenceScreen.setSummary(mSettAct.getString(R.string.sett_detailsPrefScreen_summ) +
                " " + mLocalizedSrcTypeValue);
        mPreferenceScreen.setKey(mSettAct.getString(R.string.sett_key_prefScreenDetails));

        for (Preference p : detailsPreferenceScreen.getAllDetailPreferenceFields()) {
            if (p != null) {
                mPreferenceScreen.addPreference(p);
            }
        }
    }

    public PreferenceScreen getPreferenceScreen() {
        return mPreferenceScreen;
    }
    public ViewGroup getStatusViewGroup() {
        return mStatusViewGroup;
    }
}
