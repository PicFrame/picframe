package picframe.at.picframe.helper.settings.detailsPrefScreen;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.support.v4.content.LocalBroadcastManager;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import picframe.at.picframe.Keys;
import picframe.at.picframe.R;
import picframe.at.picframe.activities.SettingsActivity;
import picframe.at.picframe.helper.GlobalPhoneFuncs;
import picframe.at.picframe.helper.settings.AppData;
import picframe.at.picframe.service_broadcast.connectionChecker.ConnectionCheck_OC;

public class OwnCloudPrefs implements IDetailsPreferenceScreen {
    private ArrayList<Preference> allPrefs = new ArrayList<>();
    private Context mSettAct;

    public OwnCloudPrefs(SettingsActivity mSettAct) {
        this.mSettAct = mSettAct;

        //createStatusView();
        createUrlPref();
        createUsernamePref();
        createPasswordPref();
        createDownloadIntervalPref();
        createLoginCheckButton();
    }

    private void createUrlPref() {
        EditTextPreference mySrcPathPref = new EditTextPreference(mSettAct);
        mySrcPathPref.setTitle(R.string.sett_srcPath_OwnCloud);
        mySrcPathPref.setSummary(R.string.sett_srcPath_OwnCloudSumm);
        mySrcPathPref.setDefaultValue("www.owncloud.org");  // TODO change to resource
        mySrcPathPref.setKey(mSettAct.getString(R.string.sett_key_srcpath_owncloud));
        mySrcPathPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue.toString().endsWith(File.separator)) {
                    Toast
                            .makeText(
                                    mSettAct,
                                    "forbidden character at end of url: \"" + File.separator + "\"",    //TODO
                                    Toast.LENGTH_SHORT)
                            .show();
                    return false;
                }
                AlertDialog.Builder myDialQBuilder = new AlertDialog.Builder(mSettAct);
                myDialQBuilder.setMessage(R.string.sett_dialog_changedURL_message) //
                        .setNegativeButton(R.string.sett_deleteDataDialog_negBtn, null)
                        .setPositiveButton(R.string.sett_deleteDataDialog_posBtn,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Toast
                                                .makeText(mSettAct, R.string.sett_toast_delFiles, Toast.LENGTH_SHORT)
                                                .show();
                                        GlobalPhoneFuncs.recursiveDeletionInBackgroundThread(
                                                new File(AppData.getExtFolderAppRoot()),
                                                false);
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
        EditTextPreference userPref = new EditTextPreference(mSettAct);
        userPref.setTitle(mSettAct.getString(R.string.sett_username));
        userPref.setSummary(mSettAct.getString(R.string.sett_usernameSumm));
        userPref.setKey(mSettAct.getString(R.string.sett_key_username));
        userPref.setDefaultValue("");
        userPref.getEditText().setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        userPref.getEditText().setInputType(EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS | EditorInfo.TYPE_TEXT_VARIATION_PERSON_NAME);
        allPrefs.add(userPref);
    }

    private void createPasswordPref() {
        EditTextPreference passordPref = new EditTextPreference(mSettAct);
        passordPref.setTitle(mSettAct.getString(R.string.sett_password));
        passordPref.setSummary(mSettAct.getString(R.string.sett_passwordSumm));
        passordPref.setKey(mSettAct.getString(R.string.sett_key_password));
        passordPref.setDefaultValue("");
        passordPref.getEditText().setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        passordPref.getEditText().setInputType(EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
        allPrefs.add(passordPref);
    }

    private void createDownloadIntervalPref() {
        ListPreference myUpdatePref = new ListPreference(mSettAct);
        myUpdatePref.setTitle(R.string.sett_downloadInterval);
        myUpdatePref.setSummary(R.string.sett_downloadIntervalSumm);
        myUpdatePref.setKey(mSettAct.getString(R.string.sett_key_downloadInterval));
        myUpdatePref.setEntries(R.array.downloadIntervalEntries);
        myUpdatePref.setEntryValues(R.array.downloadIntervalValues);
        myUpdatePref.setDefaultValue("12");
        myUpdatePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if ("-1".equals(String.valueOf(newValue))) {
                    // "-1" = download never -> deleteAlarm
                    sendBroadcast(Keys.ACTION_DELETEALARM);
                } else {
                    if (AppData.getLoginSuccessful()) {
                        sendBroadcast(Keys.ACTION_SETALARM);
                    } else {
                        sendBroadcast(Keys.ACTION_DELETEALARM);
                    }
                }
                return true;
            }
        });
        allPrefs.add(myUpdatePref);
    }

    private void createLoginCheckButton() {
        Preference connCeckButton = new Preference(mSettAct);
        connCeckButton.setTitle("Login-Check");
        connCeckButton.setSummary("Click here, to test the connection.");
        connCeckButton.setKey(mSettAct.getString(R.string.sett_key_loginCheckButton));
        connCeckButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new Handler().post(new ConnectionCheck_OC());
                return true;
            }
        });
        allPrefs.add(connCeckButton);
    }

    public void sendBroadcast(String alarmAction) {
        LocalBroadcastManager
                .getInstance(mSettAct)
                .sendBroadcast(new Intent().setAction(alarmAction));
    }

    @Override
    public ArrayList<Preference> getAllDetailPreferenceFields() {
        return allPrefs;
    }

    @Override
    public ViewGroup getStatusViewGroup() {
        return null;
    }
}
