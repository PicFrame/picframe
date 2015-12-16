package picframe.at.picframe.helper.settings.detailsPrefScreen;

import android.content.Context;
import android.preference.Preference;

import java.util.ArrayList;

import picframe.at.picframe.MainApp;
import picframe.at.picframe.R;
import picframe.at.picframe.helper.settings.AppData;
import picframe.at.picframe.helper.settings.SimpleFileDialog;

public class ExtSdPrefs implements IDetailsPreferenceScreen {
    private ArrayList<Preference> allPrefs = new ArrayList<>();
    private Context mContext = MainApp.getINSTANCE().getApplicationContext();
    private AppData settObj = AppData.getINSTANCE();

    public ExtSdPrefs() {
        //createStatusView();
        createSourcePref();
    }

    private void createSourcePref() {
        Preference mySrcPathPref = new Preference(mContext);
        mySrcPathPref.setTitle(mContext.getString(R.string.sett_srcPath_externalSD));
        mySrcPathPref.setSummary(R.string.sett_srcPath_externalSDSumm);
        mySrcPathPref.setDefaultValue("");
        mySrcPathPref.setKey(mContext.getString(R.string.sett_key_srcpath_sd));
        mySrcPathPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            String _chosenDir;

            @Override
            public boolean onPreferenceClick(Preference preference) {
                SimpleFileDialog FolderChooseDialog =
                        new SimpleFileDialog(
                                mContext,
                                new SimpleFileDialog.SimpleFileDialogListener() {
                                    @Override
                                    // The code in this function will be executed when the dialog OK button is pushed
                                    public void onChosenDir(String chosenDir) {
                                        _chosenDir = chosenDir;
                                        settObj.setSdSourcePath(_chosenDir);
                                    }
                                });
                FolderChooseDialog.chooseFile_or_Dir();
                return false;
            }
        });
    }


    @Override
    public ArrayList<Preference> getAllDetailPreferenceFields() {
        return allPrefs;
    }
}
