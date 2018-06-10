/*
   Copyright (C) 2015 Myra Fuchs, Linda Spindler, Clemens Hlawacek, Ebenezer Bonney Ussher,
   Martin Bayerl, Christoph Krasa

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
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.PageTransformer;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import picframe.at.picframe.R;
import picframe.at.picframe.helper.GlobalPhoneFuncs;
import picframe.at.picframe.helper.Keys;
import picframe.at.picframe.helper.alarm.AlarmScheduler;
import picframe.at.picframe.helper.viewpager.AccordionTransformer;
import picframe.at.picframe.helper.viewpager.BackgroundToForegroundTransformer;
import picframe.at.picframe.helper.viewpager.CubeOutTransformer;
import picframe.at.picframe.helper.viewpager.CustomViewPager;
import picframe.at.picframe.helper.viewpager.DrawFromBackTransformer;
import picframe.at.picframe.helper.viewpager.EXIF_helper;
import picframe.at.picframe.helper.viewpager.FadeInFadeOutTransformer;
import picframe.at.picframe.helper.viewpager.FlipVerticalTransformer;
import picframe.at.picframe.helper.viewpager.ForegroundToBackgroundTransformer;
import picframe.at.picframe.helper.viewpager.Gestures;
import picframe.at.picframe.helper.viewpager.RotateDownTransformer;
import picframe.at.picframe.helper.viewpager.StackTransformer;
import picframe.at.picframe.helper.viewpager.ZoomInTransformer;
import picframe.at.picframe.helper.viewpager.ZoomOutPageTransformer;
import picframe.at.picframe.service.DownloadService;
import picframe.at.picframe.settings.AppData;

public class MainActivity extends ActionBarActivity {

    private final static boolean DEBUG = true;
    private static final String TAG = MainActivity.class.getSimpleName();
    private ResponseReceiver receiver;
    LocalBroadcastManager broadcastManager;

    private ImagePagerAdapter imagePagerAdapter;
    private CustomViewPager pager;
    private final long countdownIntervalInMilliseconds = 2*60*1000-50; // interval to slightly less than 2 minutes
    private CountDownTimer countDownTimer;

    private static Context mContext;
    private String mOldPath;
    private boolean mOldRecursive;
    private RelativeLayout mainLayout;
    private boolean paused;
    private long remainingDisplayTime = 0; // in seconds

    private static final int nbOfExamplePictures = 6;
    private static boolean showExamplePictures = false;

    @SuppressWarnings("unused")
    public static ProgressBar mProgressBar;                             //TODO still needed?
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private static Animation mFadeInAnim, mFadeOutAnim;                 //TODO still needed?

    private ArrayList<PageTransformer> transformers;
    private List<String> mFilePaths;
    private int size;
    private int currentPage;
    private boolean rightToLeft;
    private Handler actionbarHideHandler;
    private ImageView mPause;
    private LinearLayout mRemainingTimeLayout;
    private AlertDialog click_on_settings_dialog;

    //public static boolean mConnCheckOC, mConnCheckSMB; //TODO still needed?
    public boolean mDoubleBackToExitPressedOnce;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mainLayout = (RelativeLayout) findViewById(R.id.mainLayout);
        mPause = (ImageView) findViewById(R.id.pauseIcon);
        mRemainingTimeLayout = (LinearLayout) findViewById(R.id.remaining_time);
        mFadeInAnim = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        mFadeOutAnim = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        //mConnCheckOC = false;
        //mConnCheckSMB = false;
        rightToLeft = true;
        paused = false;
        enableGestures();
        //deletePreferences();
        //createSettingsIfInexistent();
        if(getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        initializeTransitions();
        pager = (CustomViewPager) findViewById(R.id.pager);
        mContext = getApplicationContext();
        loadAdapter();
        setUpSlideShow();

        mOldPath = AppData.getImagePath();
        mOldRecursive = AppData.getRecursiveSearch();

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state) {
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                selectTransformer();
            }
        });

        //getlastpage
        currentPage = AppData.getCurrentPage();
        if(pager.getAdapter().getCount() < currentPage){
            currentPage = 1;
        }

        System.out.println("STARTING PAGE  " + currentPage);

        rightToLeft = AppData.getDirection();

        new AlarmScheduler().scheduleAlarm();
    }



    // TODO: folderpicker for owncloud server folder                                ! L

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        startActivity(new Intent(this, SettingsActivity.class));
        return super.onMenuOpened(featureId, menu);
    }

    protected void onResume() {
        super.onResume();

        debug("onResume");
        // refresh toolbar options (hide/show downloadNow)
        supportInvalidateOptionsMenu();
        if (AppData.getFirstAppStart()) {
            AppData.setFirstAppStart(false);
            AppData.setTutorial(true);
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        tutorial();
        // get localBroadcastManager instance to receive localBroadCasts
        if (broadcastManager == null) {
            broadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        }
        // register broadcast receiver for UI update from service
        if (receiver == null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Keys.ACTION_DOWNLOAD_FINISHED);
            filter.addAction(Keys.ACTION_PROGRESSUPDATE);
            filter.addCategory(Intent.CATEGORY_DEFAULT);
            receiver = new ResponseReceiver();
            broadcastManager.registerReceiver(receiver, filter);
        }

        if(GlobalPhoneFuncs.getFileList(AppData.getImagePath()).size() > 0) {
            if (!AppData.getImagePath().equals(mOldPath) || mOldRecursive != AppData.getRecursiveSearch()) {
                loadAdapter();
            }
        }

        updateFileList();

        // start on the page we left in onPause, unless it was the first or last picture (as this freezes the slideshow
        if(currentPage < pager.getAdapter().getCount() -1 && currentPage > 0) {
            pager.setCurrentItem(currentPage);
        }

        setUpSlideShow();

        if(AppData.getSlideshow() && !paused){
            startSlideshowCountDown();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (menu == null || !menu.hasVisibleItems())
            return super.onPrepareOptionsMenu(menu);

        if (AppData.getSourceType() == AppData.sourceTypes.OwnCloud) {
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
                debug("dowdnload now clicked");
                Intent startDownloadIntent = new Intent(mContext, DownloadService.class);
                startDownloadIntent.setAction(Keys.ACTION_STARTDOWNLOAD);
                startService(startDownloadIntent);
                return true;
            case R.id.action_about:
                myIntent = new Intent(this, StatusActivity.class);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        startActivity(myIntent);
        return true;
    }

    protected void onPause() {
        super.onPause();
        cancelSlideShowCoundown();
        mOldPath = AppData.getImagePath();
        mOldRecursive = AppData.getRecursiveSearch();

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // unregister receiver, because if the activity is not in focus, we want no UI updates
        broadcastManager.unregisterReceiver(receiver);
        receiver = null;
        if(click_on_settings_dialog != null){
            click_on_settings_dialog.cancel();
            click_on_settings_dialog.dismiss();
        }
        currentPage = pager.getCurrentItem();
    }

    public void onBackPressed() {
        if (mDoubleBackToExitPressedOnce) {
            super.onBackPressed();
        } else {
            this.mDoubleBackToExitPressedOnce = true;
            Toast.makeText(this, R.string.main_toast_exitmsg, Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDoubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
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

    private void pageSwitcher() {
        if (imagePagerAdapter.getCount() > 0 && !paused) {
            int localpage = pager.getCurrentItem();
            // switch diretion if extremity of slideshow is reacher
            if (localpage >= imagePagerAdapter.getCount() - 1) {
                rightToLeft = false;
            } else if (localpage == 0) {
                rightToLeft = true;
            }
            if (rightToLeft) {
                localpage++;
            } else {
                localpage--;
            }
            pager.setCurrentItem(localpage, true);
            debug("localpage " + localpage);
        }
    }

    public static Context getContext() {
        return mContext;
    }

    @Override
    protected void onStop(){
        super.onStop();
        // Save the current Page to resume after next start
        // maybe not right here will test
        AppData.setCurrentPage(currentPage);
        debug("SAVING PAGE  " + currentPage);

        // Save the direction of the pageviewer
        AppData.setDirection(rightToLeft);
    }

    private void setUpSlideShow(){
        if (AppData.getSlideshow()){
            pager.setScrollDurationFactor(8);
//            pager.setPagingEnabled(false);
        }
        else{
            pager.setScrollDurationFactor(3);
//            pager.setPagingEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelSlideShowCoundown();
    }

    private class ImagePagerAdapter extends PagerAdapter {
        //        private List<String> mFilePaths;
        private Activity activity;
        private LayoutInflater inflater;
        private ImageView imgDisplay;
        private int localpage;
//        private int size;

        public ImagePagerAdapter(Activity activity) {
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
        public Object instantiateItem(ViewGroup container, final int position) {
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View viewLayout = inflater.inflate(R.layout.fullscreen_layout, container,
                    false);

            imgDisplay = (ImageView) viewLayout.findViewById(R.id.photocontainer);

            if (AppData.getScaling()) {
                imgDisplay.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else {
                imgDisplay.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }
            this.localpage = position;
            if(!showExamplePictures){
                imgDisplay.setImageBitmap(EXIF_helper.decodeFile(mFilePaths.get(this.localpage), mContext));
            } else {
                String currentImage = "ex" + this.localpage;
                int currentImageID = mContext.getResources().getIdentifier(currentImage, "drawable", mContext.getPackageName());
                imgDisplay.setImageResource(currentImageID);
            }
            imgDisplay.setOnTouchListener(new Gestures(getApplicationContext()) {
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
                @Override
                public void onTap() {
                    if (AppData.getSlideshow()) {
                        paused = !paused;

                        if (paused) {
                            cancelSlideShowCoundown();
//                            pager.setPagingEnabled(true);
                            if (mPause != null)
                                mPause.setVisibility(View.VISIBLE);

//                            remainingDisplayTime = 4; // TODO: comment in once we show remaing alarmtime
//                            if(settingsObj.getDisplayTime() >= 60){
//                                String remainingTimeString = String.valueOf(remainingDisplayTime);
//                                TextView textView = (TextView) findViewById(R.id.remaining_time_value);
//                                textView.setText(remainingTimeString);
//                                mRemainingTimeLayout.setVisibility(View.VISIBLE);
//                            }

                            showActionBar();
                        } else {
                            startSlideshowCountDown();
                            mPause.setVisibility(View.INVISIBLE);
/*                            if(position < pager.getAdapter().getCount() -1 && position > 0) {
                                pager.setCurrentItem(position);
                                page=position;
                                debug("position in range: "+position);
                            } else {
                                debug("not in range: "+position);
                            }
*/             //               pager.setPagingEnabled(false);
                            if (mRemainingTimeLayout.getVisibility() == View.VISIBLE)
                                mRemainingTimeLayout.setVisibility(View.INVISIBLE);
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
            mFilePaths = GlobalPhoneFuncs.getFileList(AppData.getImagePath());
            //setUp.notifyDataSetChanged();
            setSize();
        }

        public int getPage(){
            return this.localpage;
        }
    }

    public void updateFileList() {
        if(AppData.getImagePath().equals("")) {
            showExamplePictures = true;
            Toast.makeText(mContext,R.string.main_toast_noFolderPathSet, Toast.LENGTH_SHORT).show();
        } else {
            mFilePaths = GlobalPhoneFuncs.getFileList(AppData.getImagePath());
            showExamplePictures = mFilePaths.isEmpty() || mFilePaths.size() <= 0;
            if (showExamplePictures) {
                Toast.makeText(mContext,R.string.main_toast_noFileFound, Toast.LENGTH_SHORT).show();
            }
        }
        setSize(); // size is count of images in folder, or constant if example pictures are used
        imagePagerAdapter.notifyDataSetChanged();
    }


    private void loadAdapter(){
        imagePagerAdapter = new ImagePagerAdapter(MainActivity.this);
//        pager.setPagingEnabled(true);
//        setUp = new DisplayImages(MainActivity.this);
        try {
            pager.setAdapter(imagePagerAdapter);
            currentPage = imagePagerAdapter.getPage();
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
        // OwnCloud or Dropbox selected
        if (!AppData.sourceTypes.ExternalSD.equals(AppData.getSourceType())) {
            // if no write rights, we don't need to download
            if (!GlobalPhoneFuncs.isExternalStorageWritable()) {
                Toast.makeText(this, R.string.main_toast_noSDWriteRights, Toast.LENGTH_SHORT).show();
            } else {
                // If no Username set although source is not SD Card
                if ("".equals(AppData.getUserName()) || "".equals(AppData.getUserPassword())) {
                    Toast.makeText(this, R.string.main_toast_noUsernameSet, Toast.LENGTH_SHORT).show();
                } else {
                    debug("username and pw set");
                    // Try to connect & login to selected source server
                    if (AppData.sourceTypes.OwnCloud.equals(AppData.getSourceType())) {
                        debug("trying OC check");
                        //startConnectionCheck();
                        Intent startDownloadIntent = new Intent(mContext, DownloadService.class);
                        startDownloadIntent.setAction(Keys.ACTION_STARTDOWNLOAD);
                        startService(startDownloadIntent);
                        return true;
                    }// else if (AppData.getSourceType().equals(AppData.sourceTypes.Dropbox))
                    {
                        // TODO: Dropbox checks go here
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
        if(AppData.getSlideshow() && AppData.getTransitionStyle() == 11){
            pager.setPageTransformer(true,transformers.get(random()));
        } else if(AppData.getSlideshow()){
            pager.setPageTransformer(true,transformers.get(AppData.getTransitionStyle()));
        } else {
            pager.setPageTransformer(true,new ZoomOutPageTransformer());
        }
    }

    private int random(){
        //Random from 0 to 13
        return (int)(Math.random() * 11);
    }

    private void setSize(){
        if(!showExamplePictures)
            size = mFilePaths.size();
        else
            size = nbOfExamplePictures;
    }

    private Intent getSettingsActivityIntent(){
        return new Intent(this, SettingsActivity.class);
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

    private void tutorial (){
        if (!AppData.getTutorial()) {
            return;
        }
        AlertDialog.Builder click_on_settings_dialog_builder = new AlertDialog.Builder(MainActivity.this);
        click_on_settings_dialog_builder
                .setMessage(R.string.main_dialog_tutorial_text)
                .setPositiveButton(R.string.main_dialog_tutorial_okButton,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        })
                .setNeutralButton(R.string.main_dialog_tutorial_dontShowAgainButton,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                AppData.setTutorial(false);
                            }
                        })
                .setNegativeButton(R.string.main_dialog_tutorial_openSettingsNowButton,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                startActivity(getSettingsActivityIntent());
                            }
                        });
        click_on_settings_dialog = click_on_settings_dialog_builder.create();
        click_on_settings_dialog.getWindow().setGravity(Gravity.TOP | Gravity.START);
        click_on_settings_dialog.setCancelable(true);
        click_on_settings_dialog.show();
        showActionBar();
    }


    public class ResponseReceiver extends BroadcastReceiver {
        private int progressBroadCastsReceived = 0;
        public static final String BOOTED_MSG = "booted";
        private float percent;
        private boolean indeterminate;

        // to prevent instantiation
        private ResponseReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            debug("In onReceive!");
            if (intent != null) {
                // received an intent to update the viewpager
                if (Keys.ACTION_DOWNLOAD_FINISHED.equals(intent.getAction())) {
                    debug("received 'download_finished' action via broadcast");
                    progressBroadCastsReceived = 0;
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            updateFileList();
                        }
                    });
                } else if (Keys.ACTION_PROGRESSUPDATE.equals(intent.getAction())) {
                    progressBroadCastsReceived++;
                    int progressPercent = intent.getIntExtra(Keys.MSG_PROGRESSUPDATE_PERCENT, 0);
                    Boolean indeterminate = intent.getBooleanExtra(Keys.MSG_PROGRESSUPDATE_INDITERMINATE, true);
                    debug("received 'progress_update' - " +
                            progressPercent + "% - indeterminate?" + indeterminate);
                    if (progressBroadCastsReceived > 2) {
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                updateFileList();
                            }
                        });
                        progressBroadCastsReceived = 0;
                    }

                }
            }
        }
    }

    private void startSlideshowCountDown() {
        debug("startSlideshowCountDown");
        if(remainingDisplayTime != 0 && remainingDisplayTime < AppData.getDisplayTime()) {
            debug("remainingDisplayTime: " + remainingDisplayTime + " < " + AppData.getDisplayTime()+", displayTime");
            countDownTimer = new CountDownTimer((AppData.getDisplayTime()-remainingDisplayTime)*1000, countdownIntervalInMilliseconds) {
                @Override
                public void onTick(long l) {
                    debug("unique tick!" + l / 1000);
                    remainingDisplayTime = l;
                }

                @Override
                public void onFinish() {
                    debug("done with this timer!");
                    pageSwitcher();
                    startRepeatingCountDowns();
                }
            }.start();
        } else {
            debug("no leftover displaytime!");
            startRepeatingCountDowns();
        }

    }

    private void startRepeatingCountDowns() {
        debug("startRepeatingCountDowns");
        countDownTimer = new CountDownTimer(AppData.getDisplayTime()*1000, countdownIntervalInMilliseconds) {
            @Override
            public void onTick(long l) {
                remainingDisplayTime = l/1000;
                debug("tick!" + remainingDisplayTime);
            }

            @Override
            public void onFinish() {
                pageSwitcher();
                countDownTimer.start();
            }
        }.start();
    }

    private void cancelSlideShowCoundown(){
        /* remainingg display time is always imprecise, the real value located somewhere between
            0 and countdownIntervalInMilliseconds.
            If the display time is smaller than countdownIntervalInMilliseconds/1000,
            then the value of countdownIntervalInMilliseconds will never change, and resetting
            the value will be more accurate.
         */
        if(AppData.getDisplayTime() < countdownIntervalInMilliseconds/1000) {
            debug(AppData.getDisplayTime() + " < " + countdownIntervalInMilliseconds/1000);
            remainingDisplayTime = 0;
        }
        if(countDownTimer != null){
            countDownTimer.cancel();
        }
    }

    private void debug(String msg) {
        if (DEBUG) {
            Log.d(TAG, msg);
        }
    }
}
