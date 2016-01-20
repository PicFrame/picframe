package picframe.at.picframe.settings.detailsPrefScreen;

import android.content.Context;
import android.preference.Preference;
import android.view.ViewGroup;

import java.util.ArrayList;

import picframe.at.picframe.R;
import picframe.at.picframe.activities.SettingsActivity;
import picframe.at.picframe.settings.AppData;
import picframe.at.picframe.settings.SimpleFileDialog;

public class ExtSdPrefs implements IDetailsPreferenceScreen {
    private ArrayList<Preference> allPrefs = new ArrayList<>();
    private Context mSettAct;
    private Preference folderPicker;

    public ExtSdPrefs(SettingsActivity mSettAct) {
        this.mSettAct = mSettAct;

        //createStatusView();
        createSourcePref();
    }

    private void createSourcePref() {
        folderPicker = new Preference(mSettAct);
        folderPicker.setTitle(mSettAct.getString(R.string.sett_srcPath_externalSD));
        folderPicker.setSummary(R.string.sett_srcPath_externalSDSumm);
        folderPicker.setDefaultValue("");
        folderPicker.setKey(mSettAct.getString(R.string.sett_key_srcpath_sd));
        folderPicker.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            String _chosenDir = AppData.getSourcePath();

            @Override
            public boolean onPreferenceClick(Preference preference) {
                SimpleFileDialog FolderChooseDialog =
                        new SimpleFileDialog(
                                mSettAct,
                                new SimpleFileDialog.SimpleFileDialogListener() {
                                    @Override
                                    // The code in this function will be executed when the dialog OK button is pushed
                                    public void onChosenDir(String chosenDir) {
                                        _chosenDir = chosenDir;
                                        AppData.setSdSourcePath(_chosenDir);
                                    }
                                });
                FolderChooseDialog.chooseFile_or_Dir(_chosenDir);
                return true;
            }
        });
        allPrefs.add(folderPicker);
    }

    public Preference getFolderPicker() {
        return folderPicker;
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
