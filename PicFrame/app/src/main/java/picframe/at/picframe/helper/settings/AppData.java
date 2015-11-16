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

import picframe.at.picframe.R;

/**
 * Stores App Settings, to get and load easily
 * Created by ClemensH on 04.04.2015.
 */
public class AppData {

    public enum sourceTypes {
        ExternalSD, OwnCloud, Samba ;
        private static sourceTypes[] allValues = values();
        public static sourceTypes getSourceTypesForInt(int num){
            try{
                return allValues[num];
            }catch(ArrayIndexOutOfBoundsException e){
                return ExternalSD;
            }
        }
    }
    private int transitionType;
    private sourceTypes srcType;
    private String srcPath;
    private int displayTime;
    private boolean slideshow;
    private String userName;
    private String userPassword;
    private boolean scaling;
    private boolean randomize;
    private boolean recursiveSearch;
    private String imagePath;
    private int updateInterval;

    private String extFolderAppRoot;        // sc-card-dir/Pictures/picframe
    private String extFolderDisplayPath;    // sc-card-dir/Pictures/picframe/pictures
    private String extFolderCachePath;      // sc-card-dir/Pictures/picframe/cache

    private static AppData INSTANCE;

    public static AppData getINSTANCE() {
        if (INSTANCE == null) {
            INSTANCE = new AppData();
        }
        return INSTANCE;
    }

    private AppData () {
        this.srcType = sourceTypes.ExternalSD;
        this.srcPath = "./";
        this.displayTime = 2;
        this.slideshow = true;
        this.userName = "";
        this.userPassword = "";
        this.scaling = false;
        this.randomize = false;
        this.recursiveSearch = false;
        this.imagePath = srcPath;
        this.transitionType = 2;
        this.updateInterval = 12;

        extFolderAppRoot = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) +
                File.separator + "picframe";
        extFolderDisplayPath = extFolderAppRoot + File.separator + "pictures";
        extFolderCachePath = extFolderAppRoot + File.separator + "cache";
    }

    public void loadConfig(Context ctx, SharedPreferences prefs) {
        // set AppData to Shared Pref Data
        this.userName = prefs.getString(ctx.getString(R.string.sett_key_username), "");
        this.userPassword = prefs.getString(ctx.getString(R.string.sett_key_password), "");
        this.slideshow = prefs.getBoolean(ctx.getString(R.string.sett_key_slideshow), true);
        this.displayTime = Integer.parseInt(prefs.getString(ctx.getString(R.string.sett_key_displaytime), "2"));
        this.srcType = sourceTypes.getSourceTypesForInt(Integer.parseInt(prefs.getString("SrcType", "0")));
        if (srcType == sourceTypes.ExternalSD) {
            this.srcPath = prefs.getString(ctx.getString(R.string.sett_key_srcpath_sd), "");
            this.imagePath = this.srcPath;
        } else if (srcType == sourceTypes.OwnCloud) {
            this.srcPath = prefs.getString((ctx.getString(R.string.sett_key_srcpath_owncloud)), "");
            this.imagePath = extFolderDisplayPath;
        } else if (srcType == sourceTypes.Samba) {
            this.srcPath = prefs.getString((ctx.getString(R.string.sett_key_srcpath_samba)), "");
            this.imagePath = extFolderDisplayPath;
        }
        this.scaling = prefs.getBoolean((ctx.getString(R.string.sett_key_scaling)), false);
        this.randomize = prefs.getBoolean((ctx.getString(R.string.sett_key_randomize)), false);
        this.recursiveSearch = prefs.getBoolean((ctx.getString(R.string.sett_key_recursiveSearch)), false);
        this.transitionType = Integer.parseInt(prefs.getString(ctx.getString(R.string.sett_key_transition), "0"));
        this.updateInterval = Integer.parseInt(prefs.getString(ctx.getString(R.string.sett_key_updateInterval), "12"));
    }

    @Override
    public String toString() {
        return(
            "┌----AppData----*\n" +
            " | Username: " + this.userName + "\n" +
            " | Password: " + this.userPassword + "\n" +
            " | Slideshow: " + this.slideshow + "\n" +
            " | Scaling: " + this.scaling + "\n" +
            " | Randomize: " + this.randomize + "\n" +
            " | Displaytime: " + this.displayTime + "\n" +
            " | SrcType: " + this.srcType + "\n" +
            " | SrcPath: " + this.srcPath + "\n" +
            " | Recursive: " + this.recursiveSearch + "\n" +
            "└---------------*");
    }

    public int getDisplayTime() {
        return displayTime;
    }
    public int getSrcTypeInt() {
        return srcType.ordinal();
    }
    public sourceTypes getSrcType () {
        return srcType;
    }
    // Returns selected SD-Card directory, or URL to owncloud or samba server
    public String getSrcPath() {
        return srcPath;
    }
    // Always returns the path to the img folder of current src type
    public String getImagePath() { return imagePath; }
    public String getUserName() {
        return userName;
    }
    public String getUserPassword() {
        return userPassword;
    }
    public boolean getSlideshow() {
        return slideshow;
    }
    public boolean getRandomize() {
        return randomize;
    }
    public boolean getScaling() {
        return scaling;
    }
    public boolean getRecursiveSearch() {
        return recursiveSearch;
    }
    public String getExtFolderAppRoot() {
        return extFolderAppRoot;
    }
    public String getExtFolderCachePath() {
        return extFolderCachePath;
    }
    public String getExtFolderDisplayPath() {
        return extFolderDisplayPath;
    }
    public int getTransitionType(){
        return this.transitionType;
    }
    public int getUpdateIntervalInHours() {
        return this.updateInterval;
    }
}