package picframe.at.picframe.helper.settings;

import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceScreen;

import java.util.ArrayList;

import picframe.at.picframe.MainApp;
import picframe.at.picframe.R;
import picframe.at.picframe.helper.settings.detailsPrefScreen.ExtSdPrefs;
import picframe.at.picframe.helper.settings.detailsPrefScreen.OwnCloudPrefs;

public class DetailsPreferenceScreen {
    private AppData.sourceTypes mSrcType;
    private String mLocalizedSrcTypeValue;
    private PreferenceScreen mPreferenceScreen;
    private Context mContext = MainApp.getINSTANCE().getApplicationContext();

    public DetailsPreferenceScreen(AppData.sourceTypes srcType, String srcTypeVal,
                                   PreferenceScreen preferenceScreen) {
        mSrcType = srcType;
        mLocalizedSrcTypeValue = srcTypeVal;
        mPreferenceScreen = preferenceScreen;

        prefScreenSetup();
    }

    private void prefScreenSetup() {
        mPreferenceScreen.setTitle(mLocalizedSrcTypeValue + " " +
                mContext.getString(R.string.sett_detailsPrefScreen_title));
        mPreferenceScreen.setSummary(mContext.getString(R.string.sett_detailsPrefScreen_summ) +
                " " + mLocalizedSrcTypeValue);
        mPreferenceScreen.setKey(mContext.getString(R.string.sett_key_prefScreenDetails));

        ArrayList<Preference> allPrefs = new ArrayList<>();
        if (AppData.sourceTypes.OwnCloud.equals(mSrcType)) {
            allPrefs = new OwnCloudPrefs().getAllDetailPreferenceFields();
        } else if (AppData.sourceTypes.Dropbox.equals(mSrcType)) {
            // TODO
        } else {
            allPrefs = new ExtSdPrefs().getAllDetailPreferenceFields();
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
