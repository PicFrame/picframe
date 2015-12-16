/*
    Copyright (C) 2015 Myra Fuchs, Linda Spindler, Clemens Hlawacek, Ebenezer Bonney Ussher

    This file is part of PicFrame.

    PicFrame is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    PicFrame is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with PicFrame.  If not, see <http://www.gnu.org/licenses/>.
*/

package picframe.at.picframe.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import picframe.at.picframe.R;
import picframe.at.picframe.helper.GlobalPhoneFuncs;
import picframe.at.picframe.helper.settings.AppData;
import picframe.at.picframe.helper.settings.DetailsPreferenceScreen;
import picframe.at.picframe.helper.settings.MySwitchPref;

@SuppressWarnings("deprecation")
public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = SettingsActivity.class.getSimpleName();
    private PreferenceCategory myCat2;
    private AppData settingsObj = AppData.getINSTANCE();
    @SuppressWarnings("FieldCanBeLocal")
    private SharedPreferences mPrefs;
    private ArrayList<String> editableTitleFields = new ArrayList<>();
    private ArrayList<String> fieldsToRemove = new ArrayList<>();
    /*
            {
            getString(R.string.sett_key_displaytime),
            getString(R.string.sett_key_transition),
            getString(R.string.sett_key_srctype)
    };*/
    private final static boolean DEBUG = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager prefMgr = getPreferenceManager();
        prefMgr.setSharedPreferencesName(AppData.mySettingsFilename);
        prefMgr.setSharedPreferencesMode(MODE_PRIVATE);
        mPrefs = this.getSharedPreferences(AppData.mySettingsFilename, MODE_PRIVATE);
        mPrefs.registerOnSharedPreferenceChangeListener(this);

        if (DEBUG)  printAllPreferences();
        addPreferencesFromResource(R.xml.settings);
        myCat2 = (PreferenceCategory) findPreference(getString(R.string.sett_key_cat2));

        populateEditableFieldsList();
        populateFieldsToRemove();
        createCat2Fields();
        updateAllFieldTitles();

        // set all missing fields

//        setCorrectSrcPathField();
//        updateTitlePrefsWithValues(mPrefs, "all");
        //System.out.println(settingsObj.toString());
    }

    private void printAllPreferences() {
        Map<String, ?> keyMap = mPrefs.getAll();
        for (String e : keyMap.keySet()) {
            debug("DUMP| Key: " + e + " ++ Value: " + keyMap.get(e));
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Toolbar bar;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
            bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
            root.addView(bar, 0); // insert at top
        } else {
            ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
            ListView content = (ListView) root.getChildAt(0);
            root.removeAllViews();
            bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);

            int height;
            TypedValue tv = new TypedValue();
            if (getTheme().resolveAttribute(R.attr.actionBarSize, tv, true)) {
                height = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
            }else{
                height = bar.getHeight();
            }
            content.setPadding(0, height, 0, 0);

            root.addView(content);
            root.addView(bar);
        }
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void populateEditableFieldsList() {
        editableTitleFields.add(getString(R.string.sett_key_displaytime));
        editableTitleFields.add(getString(R.string.sett_key_transition));
        editableTitleFields.add(getString(R.string.sett_key_srctype));
    }

    private void populateFieldsToRemove() {
        fieldsToRemove.add(getString(R.string.sett_key_recursiveSearch));
        fieldsToRemove.add(getString(R.string.sett_key_deleteData));
        fieldsToRemove.add(getString(R.string.sett_key_restoreDefaults));
    }
/*
    private void updateTitlePrefsWithValues(SharedPreferences sharedPreferences, String key) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            if (findPreference(key) instanceof CheckBoxPreference) return;
        } else {
            if (findPreference(key) instanceof CheckBoxPreference ||
                    findPreference(key) instanceof TwoStatePreference) return;
        }
        String[] SwitchPrefsAndOthersToSkip = { getString(R.string.sett_key_slideshow),    // TODO: add the custom-sett-keys too (like tutorial), else app dies at [String keyValue = sharedPreferences.getString(key, "-1");]
                getString(R.string.sett_key_scaling),
                getString(R.string.sett_key_randomize),
                getString(R.string.sett_key_tutorial),
                getString(R.string.sett_key_firstStart),
                getString(R.string.sett_key_recursiveSearch)};
        for (String path : SwitchPrefsAndOthersToSkip) {
            if (key.contains(path)) {
                return;
            }
        }
        // UNTIL HERE ARE CHECKS FOR SWITCH-PREFERENCES, APP WOULD CRASH OTHERWISE

        boolean loadAll = false;
        String titleStr = null;
        //String summStr = null;
        String keyValue = sharedPreferences.getString(key, "-1");
        if (keyValue.equals("-1")) {
            keyValue = null;
        }
        //System.out.println("mprefs- key: " +key + " -- value: " +mPrefs.getString(key,"-1"));       // sharedPrefs and mprefs values = the same

        switch (key) {
            case "all":
                loadAll = true;
                break;
            case "Username":
                titleStr = getString(R.string.sett_username);
                break;
            case "Password":
                if (keyValue != null)
                    //noinspection ReplaceAllDot
                    keyValue = keyValue.replaceAll(".", "*");
                titleStr = getString(R.string.sett_password);
                break;
            case "SrcType":
                keyValue = settingsObj.getSourceType().toString();
                // String was number => to correct string (like External SD/Externe SD Karte)
                //keyValue = getResources().getStringArray(R.array.srcTypeEntries)[settingsObj.getSrcTypeInt()];
                //System.err.println("KeyValue after: " + keyValue);
                titleStr = getString(R.string.sett_srcType);
                toggleUnusedFields(key, keyValue);
                break;
            case "DisplayTime":
                ListPreference tmp = (ListPreference) findPreference(key);
                if (tmp != null) {
                    if (tmp.getEntry() != null) {
                        keyValue = tmp.getEntry().toString();
                    }
                }
                titleStr = getString(R.string.sett_displayTime);
                break;
            case "SrcPath_sd":
                titleStr = getString(R.string.sett_srcPath_externalSD);
            //    summStr = getString(R.string.sett_srcPath_externalSDSumm);
                break;
            case "SrcPath_owncloud":
                titleStr = settingsObj.getSourceType().toString() + " URL";
             //   titleStr = getResources().getStringArray(R.array.srcTypeEntries)[settingsObj.getSrcTypeInt()] + " URL";
             //   summStr = getString((R.string.sett_srcPath_OwnCloudSumm));
                break;
            case "SrcPath_samba":

                break;
            default:
                //System.out.println("Default?! => " + key);
                return;
        }
        if (loadAll) {
            //System.out.println("Seems like u want to update everything now.");
            updateTitlePrefsWithValues(sharedPreferences, "Username");
            updateTitlePrefsWithValues(sharedPreferences, "Password");
            updateTitlePrefsWithValues(sharedPreferences, "SrcType");
            updateTitlePrefsWithValues(sharedPreferences, "DisplayTime");
            return;
        }
        Preference connPref = SettingsActivity.this.findPreference(key);
        if (connPref == null)
            return;
        //System.out.println("VALUE CHECK:\ntitleString: " + titleStr +  "\nkeyValue: " + keyValue);
        if (titleStr != null) {
            if (keyValue == null) {
                connPref.setTitle(titleStr);
            } else {
                connPref.setTitle(titleStr + ": " +keyValue);
                //System.out.println("Title changed. Trigger: >" +key + "< Value: >" + titleStr + ": " + keyValue +"<");
                //if (summStr != null) {
                 //   connPref.setSummary(summStr);
               // }
            }
        }
        if (key.contains("SrcType")) {
            Preference mySrcPathPref;
            String[] SrcPaths = { getString(R.string.sett_key_srcpath_sd),
                    getString(R.string.sett_key_srcpath_owncloud),
                    getString(R.string.sett_key_srcpath_dropbox)};
            for (String path : SrcPaths) {
                mySrcPathPref = findPreference(path);
                if (mySrcPathPref != null) {
                    //myCat2.removePreference(mySrcPathPref);
                    updateTitlePrefsWithValues(sharedPreferences, path);
                }
            }
        }
    }

   */

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (sharedPreferences != null && key != null) {
            debug("CHANGED| Key:" + key + " ++ Value: " + sharedPreferences.getAll().get(key));
        }
        if (editableTitleFields.contains(key)) {
            //update display/transition title
            updateListFieldTitle(key);
            if (getString(R.string.sett_key_srctype).equals(key)) {
                createCat2Fields();
            }
        }
    }

    public void updateAllFieldTitles() {
        for (String prefKey : editableTitleFields) {
            updateListFieldTitle(prefKey);
        }
    }

    public void updateListFieldTitle(String key) {
        ListPreference mPref = (ListPreference) findPreference(key);
        String mPrefTitle = "";
        String mPrefValue = "";

        if (mPref != null) {
            if (mPref.getEntry() != null) {
                mPrefValue = mPref.getEntry().toString();
            }
            if (getString(R.string.sett_key_displaytime).equals(key)) {
                mPrefTitle = getString(R.string.sett_displayTime);
            } else if (getString(R.string.sett_key_transition).equals(key)) {
                mPrefTitle = getString(R.string.sett_transition);
            } else if (getString(R.string.sett_key_srctype).equals(key)) {
                mPrefTitle = getString(R.string.sett_srcType);
            }
            mPref.setTitle(mPrefTitle + ": " + mPrefValue);
        }
    }

    public void createCat2Fields() {
        removeCat2Fields();
        // add PreferenceScreens depending on which sourceType
        setDetailsPrefScreen();
        setIncludeSubdirsSwitchPref();
        setDeleteDataButton();
        setResetToDefaultButton();
    }

    private void removeCat2Fields() {
        // remove the preference screen, before adding it again
        PreferenceScreen removeScreen = (PreferenceScreen) findPreference(getString(R.string.sett_key_prefScreenDetails));
        if (removeScreen != null) {
            myCat2.removePreference(removeScreen);
            debug("removed old Pref screen");
        }
        // remove the fields, before adding them again
        Preference removePref;
        for (String path : fieldsToRemove) {
            removePref = findPreference(path);
            if (removePref != null) {
                myCat2.removePreference(removePref);
                debug("removed:" + removePref.getTitle().toString());
            }
        }
    }

    public void setDetailsPrefScreen() {
        AppData.sourceTypes srcType = settingsObj.getSourceType();
        String[] srcValueArray = getResources().getStringArray(R.array.srcTypeEntries);
        String localizedSrcValue = Arrays.asList(srcValueArray).get(settingsObj.getSrcTypeInt());
        PreferenceScreen preferenceScreenToAdd = new DetailsPreferenceScreen(
                srcType, localizedSrcValue, getPreferenceManager().createPreferenceScreen(this))
                    .getPreferenceScreen();
        if (myCat2 != null && preferenceScreenToAdd != null) {
            myCat2.addPreference(preferenceScreenToAdd);
        }
        // TODO
    }

    private void setIncludeSubdirsSwitchPref() {
        Preference myRecCheckbox;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            myRecCheckbox = new CheckBoxPreference(this);
            ((CheckBoxPreference)myRecCheckbox).setSummaryOff(R.string.sett_recursiveSearchSummOff);
            ((CheckBoxPreference)myRecCheckbox).setSummaryOn(R.string.sett_recursiveSearchSummOn);
        } else {
            myRecCheckbox = new MySwitchPref(this);
            ((MySwitchPref)myRecCheckbox).setSummaryOff(R.string.sett_recursiveSearchSummOff);
            ((MySwitchPref)myRecCheckbox).setSummaryOn(R.string.sett_recursiveSearchSummOn);
        }
        myRecCheckbox.setTitle(R.string.sett_recursiveSearch);
        myRecCheckbox.setSummary(R.string.sett_recursiveSearchSumm);
        myRecCheckbox.setDefaultValue(true);
        myRecCheckbox.setKey(getString(R.string.sett_key_recursiveSearch));
        if (myCat2 != null) {
            myCat2.addPreference(myRecCheckbox);
        }
    }

    private void setDeleteDataButton() {
        Preference myDelDataButton = new Preference(this);
        myDelDataButton.setTitle(R.string.sett_deleteData);
        myDelDataButton.setSummary(R.string.sett_deleteDataSumm);
        myDelDataButton.setKey(getString(R.string.sett_key_deleteData));
        myDelDataButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder ensureDialogB = new AlertDialog.Builder(SettingsActivity.this);
                ensureDialogB
                        .setCancelable(false)
                        .setMessage(R.string.sett_deleteDataDialog_msg)
                        .setNegativeButton(R.string.sett_deleteDataDialog_negBtn, null)
                        .setPositiveButton(R.string.sett_deleteDataDialog_posBtn, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(SettingsActivity.this, R.string.sett_toast_delFiles, Toast.LENGTH_SHORT).show();
                                new Handler().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        GlobalPhoneFuncs.recursiveDelete(
                                                new File(settingsObj.getExtFolderAppRoot()), false);
                                    }
                                });
                            }
                        });
                ensureDialogB.show();
                return true;
            }
        });
        if (myCat2 != null) {
            myCat2.addPreference(myDelDataButton);
        }
    }

    public void setResetToDefaultButton() {
        Preference myResetButton = new Preference(this);
        myResetButton.setTitle("Restore default settings");
        myResetButton.setSummary("Reset settings to default");
        myResetButton.setKey(getString(R.string.sett_key_restoreDefaults));
        myResetButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder ensureDialogB = new AlertDialog.Builder(SettingsActivity.this);
                ensureDialogB
                        .setCancelable(false)
                        .setMessage("Do you really want to delete your settings information and reset everything to default?")
                        .setNegativeButton("No", null)
                        .setPositiveButton("Yes!", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                resetSettingsToDefault();
                                Toast.makeText(SettingsActivity.this, "Reset settings!", Toast.LENGTH_SHORT).show();
                            }
                        });
                ensureDialogB.show();
                return true;
            }
        });
        if (myCat2 != null) {
            myCat2.addPreference(myResetButton);
        }

    }

    public void resetSettingsToDefault() {
        // TODO
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        super.onPreferenceTreeClick(preferenceScreen, preference);
        // If the user has clicked on a preference screen, set up the screen
        if (preference instanceof PreferenceScreen) {
            setUpNestedScreen((PreferenceScreen) preference);
        }
        return false;
    }

    /************************************************************************************
    *   needed because else the nested preference screen don't have a actionbat/toolbar *
    *   see the fix and the given problem here: http://stackoverflow.com/a/27455363     *
    ************************************************************************************/
    public void setUpNestedScreen(PreferenceScreen preferenceScreen) {
        final Dialog dialog = preferenceScreen.getDialog();
        Toolbar bar;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            LinearLayout root = (LinearLayout) dialog.findViewById(android.R.id.list).getParent();
            bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
            root.addView(bar, 0); // insert at top
        } else {
            ViewGroup root = (ViewGroup) dialog.findViewById(android.R.id.content);
            ListView content = (ListView) root.getChildAt(0);
            root.removeAllViews();
            bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);

            int height;
            TypedValue tv = new TypedValue();
            if (getTheme().resolveAttribute(R.attr.actionBarSize, tv, true)) {
                height = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
            }else{
                height = bar.getHeight();
            }

            content.setPadding(0, height, 0, 0);

            root.addView(content);
            root.addView(bar);
        }
        bar.setTitle(preferenceScreen.getTitle());
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void debug(String msg) { if (DEBUG) { Log.d(TAG, msg); } }
}