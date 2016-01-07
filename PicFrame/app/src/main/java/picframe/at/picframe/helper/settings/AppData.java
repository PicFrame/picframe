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

package picframe.at.picframe.helper.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import java.io.File;

import picframe.at.picframe.MainApp;
import picframe.at.picframe.R;

/**
 * Stores App Settings, to get and load easily
 * Created by ClemensH on 04.04.2015.
 */
public class AppData {
    public static final String mySettingsFilename = "PicFrameSettings";
    private static final SharedPreferences mPrefs = MainApp.getINSTANCE().getSharedPreferences(mySettingsFilename, Context.MODE_PRIVATE);

    public static void resetSettings() {
        SettingsDefaults.resetSettings();
    }

    // enums for available source types (like images from SD-Card, OwnCloud or Dropbox)
    public enum sourceTypes {
        ExternalSD, OwnCloud, Dropbox;
        private static sourceTypes[] allValues = values();
        public static sourceTypes getSourceTypesForInt(int num){
            try{
                return allValues[num];
            }catch(ArrayIndexOutOfBoundsException e){
                return ExternalSD;
            }
        }
    }

// ONLY TO BE MODIFIED BY SETTINGS ACTIVITY
    // flag whether slideshow is selected(on=true) or not(off=false)
    public static boolean getSlideshow() {
        return mPrefs.getBoolean(getAppContext().getString(R.string.sett_key_slideshow),
                (Boolean) SettingsDefaults.getDefaultValueForKey(R.string.sett_key_slideshow));
    }

    // holds the time to display each picture in seconds
    public static int getDisplayTime() {
        return Integer.parseInt(mPrefs.getString(getAppContext().getString(R.string.sett_key_displaytime),
                (String) SettingsDefaults.getDefaultValueForKey(R.string.sett_key_displaytime)));
    }

    // holds the int of the transitionStyle - @res.values.arrays.transitionTypeValues
    public static int getTransitionStyle(){
        return Integer.parseInt(
                mPrefs.getString(getAppContext().getString(R.string.sett_key_transition),
                        (String) SettingsDefaults.getDefaultValueForKey(R.string.sett_key_transition)));
    }

    // flag whether to randomize the order of the displayed images (on=true)
    public static boolean getRandomize() {
        return mPrefs.getBoolean(getAppContext().getString(R.string.sett_key_randomize),
                (Boolean) SettingsDefaults.getDefaultValueForKey(R.string.sett_key_randomize));
    }

    // flag whether to scale the displayed image (on=true)
    public static boolean getScaling() {
        return mPrefs.getBoolean(getAppContext().getString(R.string.sett_key_scaling),
                (Boolean) SettingsDefaults.getDefaultValueForKey(R.string.sett_key_scaling));
    }

    // holds the type of the selected source as int
    public static int getSrcTypeInt() {
        return Integer.parseInt(
                mPrefs.getString(getAppContext().getString(R.string.sett_key_srctype),
                        (String) SettingsDefaults.getDefaultValueForKey(R.string.sett_key_srctype)));
    }
    // holds the type of the selected source
    public static sourceTypes getSourceType() {
        return sourceTypes.getSourceTypesForInt(Integer.parseInt(
                mPrefs.getString(getAppContext().getString(R.string.sett_key_srctype),
                        (String) SettingsDefaults.getDefaultValueForKey(R.string.sett_key_srctype))));
    }

    // holds the username to log into the owncloud account
    public static String getUserName() {
        return mPrefs.getString(getAppContext().getString(R.string.sett_key_username),
                (String) SettingsDefaults.getDefaultValueForKey(R.string.sett_key_username));
    }

    // holds the password to log into the owncloud account
    public static String getUserPassword() {
        return mPrefs.getString(getAppContext().getString(R.string.sett_key_password),
                (String) SettingsDefaults.getDefaultValueForKey(R.string.sett_key_password));
    }

    // Returns selected SD-Card directory, or URL to owncloud or samba server
    // holds the path to the image source (from where to (down)-load them
    public static String getSourcePath() {
        sourceTypes tmpType = getSourceType();
        if (sourceTypes.OwnCloud.equals(tmpType)) {
            return mPrefs.getString(getAppContext().getString(R.string.sett_key_srcpath_owncloud),
                    (String) SettingsDefaults.getDefaultValueForKey(R.string.sett_key_srcpath_dropbox));
        } else if (sourceTypes.Dropbox.equals(tmpType)) {
            return mPrefs.getString(getAppContext().getString(R.string.sett_key_srcpath_dropbox), "");
        } else {    // if SD or undefined, get SD path
            return mPrefs.getString(getAppContext().getString(R.string.sett_key_srcpath_sd),
                    (String) SettingsDefaults.getDefaultValueForKey(R.string.sett_key_srcpath_sd));
        }
    }

    // holds the time-interval to initiate the next download of images in hours
    public static int getUpdateIntervalInHours() {
        return Integer.parseInt(mPrefs.getString(getAppContext().getString(R.string.sett_key_downloadInterval),
                (String) SettingsDefaults.getDefaultValueForKey(R.string.sett_key_downloadInterval)));
    }

    // flag whether to include images in subfolders (on=true)
    public static boolean getRecursiveSearch() {
        return mPrefs.getBoolean(getAppContext().getString(R.string.sett_key_recursiveSearch),
                (Boolean) SettingsDefaults.getDefaultValueForKey(R.string.sett_key_recursiveSearch));
    }

    // Always returns the path to the img folder of current src type
    // holds the root-path to the displayed images
    public static String getImagePath() {
        sourceTypes tmpType = getSourceType();
        if (sourceTypes.ExternalSD.equals(tmpType)) {
            return mPrefs.getString(getAppContext().getString(R.string.sett_key_srcpath_sd),
                    (String) SettingsDefaults.getDefaultValueForKey(R.string.sett_key_srcpath_sd));
        } else {
            return getExtFolderDisplayPath();
        }
    }

// CAN ALWAYS BE MODIFIED
/*
    public static String getSdSourcePath() {
        return mPrefs.getString(getAppContext().getString(R.string.sett_key_srcpath_sd), "");
    }
*/
    public static void setSdSourcePath(String path) {
        mPrefs.edit().putString(getAppContext().getString(R.string.sett_key_srcpath_sd), path).commit();
    }

    // flag whether this is the first app start
    public static boolean getFirstAppStart() {
        return mPrefs.getBoolean(getAppContext().getString(R.string.sett_key_firstStart), true);
    }
    public static void setFirstAppStart(boolean firstAppStart) {
        mPrefs.edit().putBoolean(getAppContext().getString(R.string.sett_key_firstStart), firstAppStart).commit();
    }

    // flag whether to display the tutorial-dialog (on=true)
    public static boolean getTutorial() {
        return mPrefs.getBoolean(getAppContext().getString(R.string.sett_key_tutorial), true);
    }
    public static void setTutorial(boolean showTutorial) {
        mPrefs.edit().putBoolean(getAppContext().getString(R.string.sett_key_tutorial), showTutorial).commit();
    }

/* TODO
    // holds the remaining time to display the current image
    public static int getRemainingDisplayTime() {
        return mPrefs.getInt(getAppContext().getString(R.string.sett_key_), 0);
    }
    public static void setRemainingDisplayTime(int remainingDisplayTime) {
        mPrefs.edit().putInt(getAppContext().getString(R.string.sett_key_), remainingDisplayTime).commit();
    }
*/
// CAN NEVER BE MODIFIED!   (holds local paths, desc at vars)
    // sd-card-dir/Pictures/picframe
    public static String getExtFolderAppRoot() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) +
                File.separator + "picframe";
    }
    // sd-card-dir/Pictures/picframe/cache
    public static String getExtFolderCachePath() {
        return (getExtFolderAppRoot() + File.separator + "cache");
    }
    // sd-card-dir/Pictures/picframe/pictures
    public static String getExtFolderDisplayPath() {
        return (getExtFolderAppRoot() + File.separator + "pictures");
    }

    public static SharedPreferences getSharedPreferences() {
        return mPrefs;
    }
    private static Context getAppContext() {
        return MainApp.getINSTANCE().getApplicationContext();
    }
}