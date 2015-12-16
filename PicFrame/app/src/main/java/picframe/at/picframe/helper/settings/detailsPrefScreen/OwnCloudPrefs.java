package picframe.at.picframe.helper.settings.detailsPrefScreen;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import picframe.at.picframe.MainApp;
import picframe.at.picframe.R;
import picframe.at.picframe.helper.GlobalPhoneFuncs;
import picframe.at.picframe.helper.settings.AppData;

public class OwnCloudPrefs implements IDetailsPreferenceScreen {
    private ArrayList<Preference> allPrefs = new ArrayList<>();
    private Context mContext = MainApp.getINSTANCE().getApplicationContext();
    private AppData settObj = AppData.getINSTANCE();

    public OwnCloudPrefs() {
        //createStatusView();
        createUrlPref();
        createUsernamePref();
        createPasswordPref();
        //createUpdateIntervalPref();
        //createLoginCheckButton();
    }

    /*
    private void setUpdateDialogWithButton() {
        PreferenceCategory myCat2 = (PreferenceCategory) findPreference(getString(R.string.sett_key_cat2));
        ListPreference myUpdatePref = new ListPreference(this);
        myUpdatePref.setTitle(R.string.sett_updateInterval);
        myUpdatePref.setSummary(R.string.sett_updateIntervalSumm);
        myUpdatePref.setKey(getString(R.string.sett_key_updateInterval));
        myUpdatePref.setEntries(R.array.updateIntervalEntries);
        myUpdatePref.setEntryValues(R.array.updateIntervalValues);
        myUpdatePref.setShouldDisableView(true);
        myUpdatePref.setDefaultValue("12");
        myUpdatePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if ("-1".equals(String.valueOf(newValue))) {
                    // clicked "never" .. remove alarm here TODO
                    return true;
                } else {
                    return true;
                }
            }
        });
        if (myCat2 != null) {
            myCat2.addPreference(myUpdatePref);
        }
    }
    */

    private void createUrlPref() {
        EditTextPreference mySrcPathPref = new EditTextPreference(mContext);
        mySrcPathPref.setTitle(settObj.getSourceType().toString() + " URL");
        mySrcPathPref.setSummary(R.string.sett_srcPath_OwnCloudSumm);
        mySrcPathPref.setDefaultValue("www.owncloud.org");  // TODO change to resource
        mySrcPathPref.setKey(mContext.getString(R.string.sett_key_srcpath_owncloud));
        mySrcPathPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue.toString().endsWith(File.separator)) {
                    Toast
                        .makeText(
                                mContext,
                                "forbidden character at end of url: \"" + File.separator + "\"",
                                Toast.LENGTH_SHORT)
                        .show();
                    return false;
                }
                AlertDialog.Builder myDialQBuilder = new AlertDialog.Builder(mContext);
                myDialQBuilder.setMessage(R.string.sett_dialog_changedURL_message) //
                        .setNegativeButton(R.string.sett_deleteDataDialog_negBtn, null)
                        .setPositiveButton(R.string.sett_deleteDataDialog_posBtn,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Toast
                                            .makeText(
                                                    mContext,
                                                    R.string.sett_toast_delFiles,
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                        new Handler().post(new Runnable() {
                                            @Override
                                            public void run() {
                                                GlobalPhoneFuncs.recursiveDelete(
                                                        new File(
                                                            settObj.getExtFolderAppRoot()),
                                                            false);
                                            }
                                        });
                                    }
                                });
                myDialQBuilder.show();
                return true;
            }
        });
        mySrcPathPref.getEditText().setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        allPrefs.add(mySrcPathPref);
    }

    private void createUsernamePref() {
        EditTextPreference userPref = new EditTextPreference(mContext);
        userPref.setTitle(mContext.getString(R.string.sett_username));
        userPref.setSummary(mContext.getString(R.string.sett_usernameSumm));
        userPref.setKey(mContext.getString(R.string.sett_key_username));
        userPref.setDefaultValue("");
        userPref.getEditText().setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        userPref.getEditText().setInputType(EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS | EditorInfo.TYPE_TEXT_VARIATION_PERSON_NAME);
        allPrefs.add(userPref);
    }

    private void createPasswordPref() {
        EditTextPreference passordPref = new EditTextPreference(mContext);
        passordPref.setTitle(mContext.getString(R.string.sett_password));
        passordPref.setSummary(mContext.getString(R.string.sett_passwordSumm));
        passordPref.setKey(mContext.getString(R.string.sett_key_password));
        passordPref.setDefaultValue("");
        passordPref.getEditText().setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        passordPref.getEditText().setInputType(EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
        allPrefs.add(passordPref);
    }

    @Override
    public ArrayList<Preference> getAllDetailPreferenceFields() {
        return allPrefs;
    }
}
