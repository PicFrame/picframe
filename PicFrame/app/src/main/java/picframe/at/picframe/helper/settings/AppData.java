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
 * Stores App Settings, to get and load easily
 * Created by ClemensH on 04.04.2015.
 */
public class AppData {
    private static AppData INSTANCE;
    private SharedPreferences mPrefs;
    public static final String mySettingsFilename = "PicFrameSettings";

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

    private String extFolderAppRoot;        // sc-card-dir/Pictures/picframe
    private String extFolderDisplayPath;    // sc-card-dir/Pictures/picframe/pictures
    private String extFolderCachePath;      // sc-card-dir/Pictures/picframe/cache

    public static AppData getINSTANCE() {
        if (INSTANCE == null) {
            INSTANCE = new AppData();
        }
        return INSTANCE;
    }

    private AppData() {
        mPrefs = MainApp.getINSTANCE().getSharedPreferences(mySettingsFilename, Context.MODE_PRIVATE);

        extFolderAppRoot = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) +
                File.separator + "picframe";
        extFolderDisplayPath = extFolderAppRoot + File.separator + "pictures";
        extFolderCachePath = extFolderAppRoot + File.separator + "cache";
    }

// ONLY TO BE MODIFIED BY SETTINGS ACTIVITY
    // flag whether slideshow is selected(on=true) or not(off=false)
    public boolean getSlideshow() {
        return mPrefs.getBoolean(getAppContext().getString(R.string.sett_key_slideshow), true);
    }

    // holds the time to display each picture in seconds
    public int getDisplayTime() {
        return Integer.parseInt(mPrefs.getString(getAppContext().getString(R.string.sett_key_displaytime), "2"));
    }

    // holds the int of the transitionStyle - @res.values.arrays.transitionTypeValues
    public int getTransitionStyle(){
        return Integer.parseInt(
                mPrefs.getString(getAppContext().getString(R.string.sett_key_transition), "0"));
    }

    // flag whether to randomize the order of the displayed images (on=true)
    public boolean getRandomize() {
        return mPrefs.getBoolean(getAppContext().getString(R.string.sett_key_randomize), false);
    }

    // flag whether to scale the displayed image (on=true)
    public boolean getScaling() {
        return mPrefs.getBoolean(getAppContext().getString(R.string.sett_key_scaling), false);
    }

    // holds the type of the selected source as int
    public int getSrcTypeInt() {
        return Integer.parseInt(
                mPrefs.getString(getAppContext().getString(R.string.sett_key_srctype), "0"));
    }
    // holds the type of the selected source
    public sourceTypes getSourceType() {
        return sourceTypes.getSourceTypesForInt(Integer.parseInt(
                mPrefs.getString(getAppContext().getString(R.string.sett_key_srctype), "0")));
    }

    // holds the username to log into the owncloud account
    public String getUserName() {
        return mPrefs.getString(getAppContext().getString(R.string.sett_key_username), "");
    }

    // holds the password to log into the owncloud account
    public String getUserPassword() {
        return mPrefs.getString(getAppContext().getString(R.string.sett_key_password), "");
    }

    // Returns selected SD-Card directory, or URL to owncloud or samba server
    // holds the path to the image source (from where to (down)-load them
    public String getSourcePath() {
        sourceTypes tmpType = getSourceType();
        if (sourceTypes.ExternalSD.equals(tmpType)) {
            return mPrefs.getString(getAppContext().getString(R.string.sett_key_srcpath_sd), "");
        } else if (sourceTypes.OwnCloud.equals(tmpType)) {
            return mPrefs.getString(getAppContext().getString(R.string.sett_key_srcpath_owncloud), "");
        } else if (sourceTypes.Dropbox.equals(tmpType)) {
            return mPrefs.getString(getAppContext().getString(R.string.sett_key_srcpath_dropbox), "");
        } else {
            return null;
        }
    }

    // holds the time-interval to initiate the next download of images in hours
    public int getUpdateIntervalInHours() {
        return Integer.parseInt(mPrefs.getString(getAppContext().getString(R.string.sett_key_updateInterval), "12"));
    }

    // flag whether to include images in subfolders (on=true)
    public boolean getRecursiveSearch() {
        return mPrefs.getBoolean(getAppContext().getString(R.string.sett_key_recursiveSearch), false);
    }

    // Always returns the path to the img folder of current src type
    // holds the root-path to the displayed images
    public String getImagePath() {
        sourceTypes tmpType = getSourceType();
        if (sourceTypes.ExternalSD.equals(tmpType)) {
            return mPrefs.getString(getAppContext().getString(R.string.sett_key_srcpath_sd), "");
        } else {
            return extFolderDisplayPath;
        }
    }

// CAN ALWAYS BE MODIFIED
    // flag whether this is the first app start
    public boolean getFirstAppStart() {
        return mPrefs.getBoolean(getAppContext().getString(R.string.sett_key_firstStart), true);
    }
    public void setFirstAppStart(boolean firstAppStart) {
        mPrefs.edit().putBoolean(getAppContext().getString(R.string.sett_key_firstStart), firstAppStart).commit();
    }

    // flag whether to display the tutorial-dialog (on=true)
    public boolean getTutorial() {
        return mPrefs.getBoolean(getAppContext().getString(R.string.sett_key_tutorial), true);
    }
    public void setTutorial(boolean showTutorial) {
        mPrefs.edit().putBoolean(getAppContext().getString(R.string.sett_key_tutorial), showTutorial).commit();
    }
/* TODO
    // holds the remaining time to display the current image
    public int getRemainingDisplayTime() {
        return mPrefs.getInt(getAppContext().getString(R.string.sett_key_), 0);
    }
    public void setRemainingDisplayTime(int remainingDisplayTime) {
        mPrefs.edit().putInt(getAppContext().getString(R.string.sett_key_), remainingDisplayTime).commit();
    }
*/
// CAN NEVER BE MODIFIED!   (holds local paths, desc at vars)
    public String getExtFolderAppRoot() {
        return extFolderAppRoot;
    }
    public String getExtFolderCachePath() {
        return extFolderCachePath;
    }
    public String getExtFolderDisplayPath() {
        return extFolderDisplayPath;
    }

    private Context getAppContext() {
        return MainApp.getINSTANCE().getApplicationContext();
    }

    @Override
    public String toString() {
        return(
                "┌----AppData----*\n" +
                " | Username: " +   this.getUserName() + "\n" +
                " | Password: " +   this.getUserPassword() + "\n" +
                " | Slideshow: " +  this.getSlideshow() + "\n" +
                " | Scaling: " +    this.getScaling() + "\n" +
                " | Randomize: " +  this.getRandomize() + "\n" +
                " | Displaytime: "+ this.getDisplayTime() + "\n" +
                " | SrcType: " +    this.getSourceType() + "\n" +
                " | SrcPath: " +    this.getSourcePath() + "\n" +
                " | Recursive: " +  this.getRecursiveSearch() + "\n" +
                "└---------------*");
    }
}