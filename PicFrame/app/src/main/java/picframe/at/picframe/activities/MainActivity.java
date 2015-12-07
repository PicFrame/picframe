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
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.PageTransformer;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.content.Intent;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import picframe.at.picframe.R;
import picframe.at.picframe.downloader.AlarmReceiver;
import picframe.at.picframe.helper.viewpager.AccordionTransformer;
import picframe.at.picframe.helper.viewpager.BackgroundToForegroundTransformer;
import picframe.at.picframe.helper.viewpager.CubeOutTransformer;
import picframe.at.picframe.helper.viewpager.CustomViewPager;
import picframe.at.picframe.helper.settings.AppData;
import picframe.at.picframe.helper.viewpager.DrawFromBackTransformer;
import picframe.at.picframe.helper.viewpager.EXIF_helper;
import picframe.at.picframe.helper.viewpager.FadeInFadeOutTransformer;
import picframe.at.picframe.helper.viewpager.FlipVerticalTransformer;
import picframe.at.picframe.helper.viewpager.ForegroundToBackgroundTransformer;
import picframe.at.picframe.helper.viewpager.Gestures;
import picframe.at.picframe.helper.GlobalPhoneFuncs;
import picframe.at.picframe.helper.viewpager.RotateDownTransformer;
import picframe.at.picframe.helper.viewpager.StackTransformer;
import picframe.at.picframe.helper.viewpager.ZoomInTransformer;
import picframe.at.picframe.helper.viewpager.ZoomOutPageTransformer;
import picframe.at.picframe.service_broadcast.DownloadService;
import picframe.at.picframe.service_broadcast.Keys;


@SuppressWarnings("deprecation")
public class MainActivity extends ActionBarActivity{

    private static final String TAG = MainActivity.class.getSimpleName();
    private ResponseReceiver receiver;
    public static final String mySettingsFilename = "PicFrameSettings";
    private SharedPreferences mPrefs = null;
    public static AppData settingsObj = null;
    LocalBroadcastManager broadcastManager;

    private ImageView pauseIcon;
    private static DisplayImages setUp;
    private static Context mContext;
    private static CustomViewPager pager;
    private Timer timer;
    private Timer downloadTimer;
    private int page;
    private String mOldPath;
    private boolean mOldRecursive;
    private RelativeLayout mainLayout;
    private boolean paused;

    public static ProgressBar mProgressBar;                             //TODO still needed?
    private static Object[] mParamsOwnCloud;                            //TODO still needed?
    private static AsyncTask<Object, Float, Object> mBackgroundTask;    //TODO still needed?
    private static Animation mFadeInAnim, mFadeOutAnim;                 //TODO still needed?

    private ArrayList<PageTransformer> transformers;
    private static List<String> mFilePaths;
    private static int size;
    private static int currentPageSaved;
    private static boolean toggleDirection;
    private Handler actionbarHideHandler;

    //public static boolean mConnCheckOC, mConnCheckSMB; //TODO still needed?
    public boolean mDoubleBackToExitPressedOnce;

    private final static boolean DEBUG = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pauseIcon = (ImageView) findViewById(R.id.pauseIcon);
        //mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mainLayout = (RelativeLayout) findViewById(R.id.mainLayout);
        mFadeInAnim = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        mFadeOutAnim = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        //mConnCheckOC = false;
        //mConnCheckSMB = false;
        toggleDirection = false;
        paused = false;
        enableGestures();
        mPrefs = getSharedPreferences(mySettingsFilename, MODE_PRIVATE);
        //deletePreferences();
        //createSettingsIfInexistent();
        if(getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        loadSettings();
        deleteTimerz();
        initializeTransitions();

        pager = (CustomViewPager) findViewById(R.id.pager);
        mContext = getApplicationContext();

        loadAdapter();
        slideShow();

        mOldPath = settingsObj.getImagePath();
        mOldRecursive = settingsObj.getRecursiveSearch();

        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {}
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            public void onPageSelected(int position) {
                selectTransformer();
            }
        });

        //getlastpage
        page = mPrefs.getInt("currentpage", 1);
        if(pager.getAdapter().getCount() < page){
            page = 1;
        }

        scheduleAlarm();
    }

    // TODO: make download task a service (+react to wifi-connection broadcast)     !! C
    // TODO: Make download timer to alarm                                           !! L
    // TODO: Use info of "include sub dirs" for file download                       -> later update
    // TODO: folderpicker for owncloud server folder                                ! M
        // TODO: Read operation for getting folder structure                        ! C

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        startActivity(new Intent(this, SettingsActivity.class));
        return super.onMenuOpened(featureId, menu);
    }

    protected void onResume() {
        super.onResume();
        supportInvalidateOptionsMenu();
        loadSettings();
        if (mPrefs.getBoolean(getString(R.string.app_key_firstRun), true)) {
            mPrefs.edit().putBoolean(getString(R.string.app_key_firstRun), false).commit();
            startActivity(new Intent(this, SettingsActivity.class));
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // get localBroadcastManager instance to receive localBroadCasts
        if (broadcastManager == null) {
            broadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        }
        // register broadcast receiver for UI update from service
        if (receiver == null) {
            IntentFilter filter = new IntentFilter(Keys.ACTION_DOWNLOAD_FINISHED);
            filter.addCategory(Intent.CATEGORY_DEFAULT);
            receiver = new ResponseReceiver();
            broadcastManager.registerReceiver(receiver, filter);
        }

        if(settingsObj.getSrcType() == AppData.sourceTypes.OwnCloud) {
            System.out.println("new timer");
            deleteTimerz(true);
            this.downloadTimer = new Timer();
            int downloadInterval = settingsObj.getUpdateIntervalInHours(); // number of hours to wait for next download
            this.downloadTimer.schedule(new DownloadingTimerTask(), downloadInterval * 1000 * 60 * 60, downloadInterval * 1000 * 60 * 60); // delay in hours
        } else {
            System.out.println("no new timer");
            deleteTimerz(true);
        }

        if("-1".equals(mPrefs.getString(this.getString(R.string.sett_key_srcpath_sd), "-1")) &&
                settingsObj.getSrcType().equals(AppData.sourceTypes.ExternalSD)) {
            AlertDialog.Builder path_not_set_dialog = new AlertDialog.Builder(MainActivity.this);
            path_not_set_dialog
                    .setCancelable(true)
                    .setMessage(R.string.main_dialog_path_not_set);
            path_not_set_dialog.show();
        }

        if(GlobalPhoneFuncs.getFileList(settingsObj.getImagePath()).size() > 0) {
            if (!settingsObj.getImagePath().equals(mOldPath) || mOldRecursive != settingsObj.getRecursiveSearch()) {
                loadAdapter();
            }
        }

        updateFileList();

        // start on the page we left in onPause, unless it was the first or last picture (as this freezes the slideshow
        if(currentPageSaved < pager.getAdapter().getCount() -1 && currentPageSaved > 0) {
            pager.setCurrentItem(currentPageSaved);
            page=currentPageSaved;
        }
        slideShow();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (menu == null || !menu.hasVisibleItems())
            return super.onPrepareOptionsMenu(menu);

        if (settingsObj.getSrcType() == AppData.sourceTypes.OwnCloud) {
            menu.findItem(R.id.action_download).setVisible(true);
        } else {
            menu.findItem(R.id.action_download).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent myIntent;
        switch (item.getItemId()) {
            case R.id.action_settings:
                myIntent = new Intent(this, SettingsActivity.class);
                break;
            case R.id.action_download:
                Log.d(TAG, "dowdnload now clicked");
                Intent startDownloadIntent = new Intent(mContext, DownloadService.class);
                startDownloadIntent.setAction(Keys.ACTION_STARTDOWNLOAD);
                startService(startDownloadIntent);
                return true;
            case R.id.action_about:
                myIntent = new Intent(this, AboutActivity.class);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        startActivity(myIntent);
        return true;
    }

    protected void onPause() {
        super.onPause();
        if (mBackgroundTask != null && mBackgroundTask.getStatus() == AsyncTask.Status.RUNNING) {
            mBackgroundTask.cancel(true);
        }
        deleteTimerz();
        mOldPath = settingsObj.getImagePath();
        mOldRecursive = settingsObj.getRecursiveSearch();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // unregister receiver, because if the activity is not in focus, we want no UI updates
        broadcastManager.unregisterReceiver(receiver);
        receiver = null;

        currentPageSaved = pager.getCurrentItem();
    }

    public void onBackPressed() {
        if (mDoubleBackToExitPressedOnce) {
            if (mBackgroundTask != null && mBackgroundTask.getStatus() == AsyncTask.Status.RUNNING) {
                mBackgroundTask.cancel(true);
            }
            super.onBackPressed();
        } else {
            this.mDoubleBackToExitPressedOnce = true;
            if (mBackgroundTask != null && mBackgroundTask.getStatus() == AsyncTask.Status.RUNNING) {
                Toast.makeText(this, R.string.main_toast_download_interrupted, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.main_toast_exitmsg, Toast.LENGTH_SHORT).show();
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDoubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }

    private void loadSettings() {
        settingsObj = AppData.getINSTANCE();
        settingsObj.loadConfig(getApplicationContext(), mPrefs);
    }

    public void showActionBar() {
        if (this.getSupportActionBar() != null) {
            this.getSupportActionBar().show();
        }
        if (actionbarHideHandler != null) {
            actionbarHideHandler.removeCallbacksAndMessages(null);
            actionbarHideHandler = null;
        }
        actionbarHideHandler = new Handler();
        actionbarHideHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getSupportActionBar().hide();
            }
        }, 2500);
    }

    public void pageSwitcher(int seconds) {
        // At this line a new Thread will be created
        this.timer = new Timer();
        this.timer.scheduleAtFixedRate(new RemindTask(), 0, seconds * 1000); // delay

    }
    private class RemindTask extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                public void run() {
                    if (setUp.getCount() > 0 && !paused) {
                        pager.setCurrentItem(page, true);
                        if(!toggleDirection) {
                            page++;
                        }
                        else {
                            page--;
                        }
                        if(page == setUp.getCount()-1 || page == 0){
                            toggleDirection = !toggleDirection;
                        }
                    }
                }
            });
        }
    }

    public class DownloadingTimerTask extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                public void run() {
                    // set to false again every time
                    //mConnCheckOC = false;
                    checkForProblemsAndShowToasts();  // check for connection or file reading problems
                }
            });
        }
    }

    public void scheduleAlarm()
    {
        System.out.println("Starting Alarm to go off " + settingsObj.getUpdateIntervalInHours());
        // The time at which the alarm will be scheduled. Here the alarm is scheduled for 1 day from the current time.
        // We fetch the current time in milliseconds and add 1 day's time
        // i.e. 24*60*60*1000 = 86,400,000 milliseconds in a day.
        //  Long time = new GregorianCalendar().getTimeInMillis()+24*60*60*1000;


       // Long time = new GregorianCalendar().getTimeInMillis()+settingsObj.getUpdateIntervalInHours() * 60 * 60 * 1000;

        Long time = new GregorianCalendar().getTimeInMillis()+ 2 * 60 * 1000;



        // Create an Intent and set the class that will execute when the Alarm triggers. Here we have
        // specified AlarmReceiver in the Intent. The onReceive() method of this class will execute when the broadcast from your alarm is received.
        Intent intentAlarm = new Intent(this, AlarmReceiver.class);

        // Get the Alarm Service.
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // Set the alarm for a particular time.
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, time, time * 100, PendingIntent.getBroadcast(this, 1, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));

        //Testing purpose
        Date date = new Date(time);
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        String dateFormatted = formatter.format(date);
        System.out.println("Start time: " + formatter.format(new GregorianCalendar().getTimeInMillis()) + " " + (new GregorianCalendar().getTimeInMillis()));
        System.out.println("Go OFF time: " + formatter.format(time) + " " + time);
        Toast.makeText(this, "Alarm Scheduled for " + dateFormatted, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStop(){
        super.onStop();
        // Save the current Page to resume after next start
        // maybe not right here will test
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putInt("currentpage", page);
        editor.commit();

    }



    public void slideShow(){
        if (settingsObj.getSlideshow()){
            pager.setScrollDurationFactor(8);
            pager.setPagingEnabled(false);
            pageSwitcher(settingsObj.getDisplayTime());
        }
        else{
            pager.setScrollDurationFactor(3);
            pager.setPagingEnabled(true);
            deleteTimerz(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deleteTimerz();
    }

    private class DisplayImages extends PagerAdapter {
//        private List<String> mFilePaths;
        private Activity activity;
        private LayoutInflater inflater;
        private ImageView imgDisplay;
        private int localpage;
//        private int size;

        public DisplayImages(Activity activity) {
            this.activity = activity;
            updateSettings();
        }

        @Override
        public int getCount() {
            return size;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }


        @Override
        public Object instantiateItem(ViewGroup container, int position) {


            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View viewLayout = inflater.inflate(R.layout.fullscreen_layout, container,
                    false);

            imgDisplay = (ImageView) viewLayout.findViewById(R.id.photocontainer);

            if (settingsObj.getScaling()) {
                imgDisplay.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else {
                imgDisplay.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }
            this.localpage = position;
            imgDisplay.setImageBitmap(EXIF_helper.decodeFile(mFilePaths.get(this.localpage), mContext));

            imgDisplay.setOnTouchListener(new Gestures(getApplicationContext()) {
                @Override
                public void onSwipeBottom() {
                    showActionBar();
                }
                @Override
                public void onSwipeTop() {
                    if (getSupportActionBar() != null){
                        getSupportActionBar().hide();
                    }
                }

                @Override
                public void onTap() {
                    if (settingsObj.getSlideshow()) {
                        paused = !paused;

                        if (paused) {
                            if (pauseIcon.getVisibility()!= View.VISIBLE)
                                pauseIcon.setVisibility(View.VISIBLE);
                            Toast.makeText(MainActivity.mContext, R.string.main_paused_yes, Toast.LENGTH_SHORT).show();
                        } else {
                            if (pauseIcon.getVisibility()!= View.GONE)
                                pauseIcon.setVisibility(View.GONE);
                            if(settingsObj.getDisplayTime() >= 5)
                                Toast.makeText(MainActivity.mContext, R.string.main_paused_no, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
            container.addView(viewLayout);

            return viewLayout;

        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((RelativeLayout) object);
        }

        private void updateSettings() {
            System.out.println("updateSetting setUp.getCount "+size);
            mFilePaths = GlobalPhoneFuncs.getFileList(settingsObj.getImagePath());
            //setUp.notifyDataSetChanged();
            size = mFilePaths.size();
        }

        public int getPage(){
            return this.localpage;
        }

        /*
        @Override
        public float getPageWidth (int position) {
            if (position == POS_MAIN_PAGE) {
                return WIDTH_MAIN_PAGE;
            }
            return 1f;
        }
*/
    }

    private void deleteTimerz() { deleteTimerz(true); }
    private void deleteTimerz(boolean deleteDLTimer){
        if(timer != null){
            this.timer.cancel();
            this.timer.purge();
            this.timer = null;
        }
        if(downloadTimer != null && deleteDLTimer){
            this.downloadTimer.cancel();
            this.downloadTimer.purge();
            this.downloadTimer= null;
        }
    }

    public void updateFileList() {
        mFilePaths = GlobalPhoneFuncs.getFileList(settingsObj.getImagePath());
        size = mFilePaths.size();
        setUp.notifyDataSetChanged();
    }


    private void loadAdapter(){
        setUp = new DisplayImages(MainActivity.this);
//        pager.setPagingEnabled(true);
        setUp = new DisplayImages(MainActivity.this);
        try {
            pager.setAdapter(setUp);
            page = setUp.getPage();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void enableGestures(){
        mainLayout.setOnTouchListener(new Gestures(getApplicationContext()) {
            @Override
            public void onSwipeBottom() {
                showActionBar();
            }

            @Override
            public void onSwipeTop() {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().hide();
                }
            }
        });
    }

    private boolean checkForProblemsAndShowToasts() {
        // OwnCloud or Samba selected
        if (!settingsObj.getSrcType().equals(AppData.sourceTypes.ExternalSD)) {
            // if no write rights, we don't need to download
            if (!GlobalPhoneFuncs.isExternalStorageWritable()) {
                Toast.makeText(this, R.string.main_toast_noSDWriteRights, Toast.LENGTH_SHORT).show();
            } else {
                // If no Username set although source is not SD Card
                if (settingsObj.getUserName().equals("") || settingsObj.getUserPassword().equals("")) {
                    Toast.makeText(this, R.string.main_toast_noUsernameSet, Toast.LENGTH_SHORT).show();
                } else {
                    if (DEBUG) Log.i(TAG, "username and pw set");
                    // Try to connect & login to selected source server
                    if (settingsObj.getSrcType().equals(AppData.sourceTypes.OwnCloud)) {
                        if (DEBUG) Log.i(TAG, "trying OC check");
                        //startConnectionCheck();
                        Intent startDownloadIntent = new Intent(mContext, DownloadService.class);
                        startDownloadIntent.setAction(Keys.ACTION_STARTDOWNLOAD);
                        startService(startDownloadIntent);
                        return true;
                    }// else if (settingsObj.getSrcType().equals(AppData.sourceTypes.Samba))
                    {
                        // TODO: Samba checks go here
                    }
                }
            }
            // SD Card selected
        } else {
            if (!GlobalPhoneFuncs.isExternalStorageReadable()) {
                // If no read rights for SD although source is SD Card
                Toast.makeText(this, R.string.main_toast_noSDReadRights, Toast.LENGTH_SHORT).show();
            } else {
                if (!GlobalPhoneFuncs.hasAllowedFiles()) {
                    Toast.makeText(this, R.string.main_toast_noFileFound, Toast.LENGTH_SHORT).show();
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    public void selectTransformer(){
        if(settingsObj.getSlideshow() && settingsObj.getTransitionType() == 11) {
            pager.setPageTransformer(true,transformers.get(random()));
        } else if(settingsObj.getSlideshow()){
            pager.setPageTransformer(true,transformers.get(settingsObj.getTransitionType()));
        } else {
            pager.setPageTransformer(true,new ZoomOutPageTransformer());
        }
    }

    private int random(){
        //Random from 0 to 13
        return (int)(Math.random() * 11);
    }

    private void initializeTransitions(){
        transformers = new ArrayList<>();
        this.transformers.add(new AccordionTransformer());
        this.transformers.add(new BackgroundToForegroundTransformer());
        this.transformers.add(new CubeOutTransformer());
        this.transformers.add(new DrawFromBackTransformer());
        this.transformers.add(new FadeInFadeOutTransformer());
        this.transformers.add(new FlipVerticalTransformer());
        this.transformers.add(new ForegroundToBackgroundTransformer());
        this.transformers.add(new RotateDownTransformer());
        this.transformers.add(new StackTransformer());
        this.transformers.add(new ZoomInTransformer());
        this.transformers.add(new ZoomOutPageTransformer());
    }

    public class ResponseReceiver extends BroadcastReceiver {
        public static final String BOOTED_MSG = "booted";
        private float percent;
        private boolean indeterminate;

        // to prevent instantiation
        private ResponseReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                // received an intent to update the viewpager
                if (Keys.ACTION_DOWNLOAD_FINISHED.equals(intent.getAction())) {
                    if (DEBUG)  Log.d(TAG, "received 'download_finished' action via broadcast");
                    updateFileList();
                }
            }
        }
    }
}
