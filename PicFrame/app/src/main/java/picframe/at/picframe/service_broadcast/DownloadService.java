package picframe.at.picframe.service_broadcast;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.common.OwnCloudCredentialsFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import picframe.at.picframe.Keys;
import picframe.at.picframe.R;
import picframe.at.picframe.activities.MainActivity;
import picframe.at.picframe.downloader.Downloader;
import picframe.at.picframe.downloader.Downloader_OC;
import picframe.at.picframe.helper.GlobalPhoneFuncs;
import picframe.at.picframe.helper.settings.AppData;


public class DownloadService extends Service implements ServiceCallbacks {
    private final String TAG = this.getClass().getSimpleName();
    private static final boolean DEBUG = true;

    private AppData settObj;
    private LocalBroadcastManager broadcastManager;
    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager notificationManager;
    private Intent mainActIntent;
    private Intent stopDownloadIntent;
    private Downloader downloader;
    private boolean downloading;
    private boolean finished;

    // arguments for the downloader class
    private HashMap<String, Object> args;

    @Override
    public void onCreate() {
        if (DEBUG)  Log.d(TAG, this.getClass().getSimpleName() + "  -- onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (DEBUG)  Log.d(TAG, this.getClass().getSimpleName() + " started");
        /* stop checks */
        // if the service wasn't called with the correct action, stop!
        if (intent == null || !intent.getAction().equals(Keys.ACTION_STARTDOWNLOAD)) {
            if (intent != null && intent.getAction().equals(Keys.ACTION_STOPDOWNLOAD)) {
                if (DEBUG) Log.d(TAG, "got STOPDOWNLOAD action");
                stopDownload();
            } else {
                if (DEBUG) //noinspection ConstantConditions
                    Log.d(TAG, "stopping due to wrong intent action: " + intent==null ? "intent==null" : intent.getAction());
                stopSelf();
            }
            return Service.START_NOT_STICKY;
        }

        // get localBroadCastManager instance to send broadCast to activity
        if (broadcastManager == null)
            broadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        // get notificationManager service to manage notifications
        if (notificationManager == null)
            notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        // get new Notification Builder
        if (notificationBuilder == null)
            notificationBuilder = new NotificationCompat.Builder(this);

        // set up Intent to open activity
        mainActIntent = new Intent(this, MainActivity.class);
        mainActIntent.addCategory(Intent.CATEGORY_DEFAULT);
        // set up Intent to stop download/service
        stopDownloadIntent = new Intent(this, DownloadService.class);
        stopDownloadIntent.setAction(Keys.ACTION_STOPDOWNLOAD);
        stopDownloadIntent.addCategory(Intent.CATEGORY_DEFAULT);

        // load shared preferences settingsObject
        settObj = AppData.getINSTANCE();

        if (!downloading) {
            // check for problems only in mainactivity (after settings changed) (checkForProblemsAndShowToasts)
            // since at this point we are mostly sure to not run into problems, we check for wifi
            if (!GlobalPhoneFuncs.wifiConnected()) {
                stopForeground(false);
                downloadFailed(Downloader.Failure.WIFI);
                return Service.START_NOT_STICKY;
            } else if (!initializedFolders()){
                stopForeground(false);
                showNotification(Keys.NotificationStates.FAILURE, 0, getString(R.string.service_notif_failureFolderInitFailure));
                stopSelf();
                return Service.START_NOT_STICKY;
            }

            // create arguments hashmap - used for downloader arguments
            args = new HashMap<>();

            /* create downloader object according to selected type */
            // if type is external sd, no download should happen (should never be the case)
            AppData.sourceTypes tmpSource;
            tmpSource = settObj.getSourceType();
            if (AppData.sourceTypes.ExternalSD.equals(tmpSource)) {
                Log.d(TAG, "FAILURE! DownloadService started, while SD Card is selected");
                stopSelf();
                return Service.START_NOT_STICKY;
            } else if (AppData.sourceTypes.OwnCloud.equals(tmpSource)) {
                args = setUpOcClientArguments();
                downloader = new Downloader_OC(args);
            } // else if (AppData.sourceTypes.Dropbox.equals(tmpSource) {}    //  TODO Dropbox
            if (args == null) {
                Log.d(TAG, "Arguments cannot be null, aborting!");
                stopSelf();
                return Service.START_NOT_STICKY;
            }
            // start download here, progress will be published via callback to this class and then broadcast
            downloading = true;
            downloader.start();
            // show basic start notification
            showNotification(Keys.NotificationStates.START, 0, null);
            // more resources to handle service (forces notification with ongoing flag)
            startForeground(Keys.NOTIFICATION_ID, notificationBuilder.build());
        }
        // restart service until it is stopped
        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Service - onBind");
        return null;
    }

    @Override
    public void onDestroy() {
        interruptDownloaderAndStopService();
        Log.d(TAG, "Service - onDestroy");
    }

    private void interruptDownloaderAndStopService() {
        if (downloader != null) {
            if (downloading || !finished) {
                if (!downloader.isInterrupted()) {
                    Log.d(TAG, "- interrupting downloader");
                    downloader.interrupt();
                }
            }
            try {
                downloader.join();
                downloading = false;
            } catch (InterruptedException e) {
                Log.d(TAG, "InterruptedException while waiting for downloader.join (onDestroy)", e);
            }
            downloader = null;
        }
        stopSelf();
    }

    private boolean initializedFolders() {
        // mExtFolderCachePath + mExtFolderDisplayPath
        boolean dirCreated;
        // check if folders exist, if not, create them
        ArrayList<String> folderList = new ArrayList<>();
        if (DEBUG) Log.d(TAG, settObj.getExtFolderAppRoot());
        folderList.add(settObj.getExtFolderAppRoot());
        folderList.add(settObj.getExtFolderCachePath());
        folderList.add(settObj.getExtFolderDisplayPath());
        for (String folder : folderList) {
            File dir = new File(folder);
            if (dir.exists() && dir.isDirectory())
                continue;
            dirCreated = dir.mkdir();
            if (dirCreated)
                if (DEBUG) Log.i(TAG, "Creating folder: >" +dir+ "< successful");
                else {
                    if (DEBUG) Log.i(TAG, "Creating folder: >" +dir+ "< FAILED!");
                    return false;
                }
        }
        File folder = new File(folderList.get(1));
        if (DEBUG) Log.i(TAG, "deleting files in cache dir before downloading");
        // delete all files and folders in cache folder
        if (!recursiveDelete(folder, false)) {
            return false;
        }
        File nomedia = new File(folderList.get(1) + File.separator + ".nomedia");
        if (!nomedia.exists()) {
            try {
                if (nomedia.createNewFile()) {
                    if (DEBUG) Log.i(TAG, "Created .nomedia file successfully");
                }
            } catch (IOException e) {
                if (DEBUG) Log.e(TAG, "Couldn't create .nomedia file");
            }
        }
        return true;
    }

    public boolean recursiveDelete(File dir, boolean delRoot) {
        if (dir.exists()) {
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    recursiveDelete(new File(file.getAbsolutePath()), true);
                } else {
                    if (!file.delete()) {
                        if (DEBUG) Log.e(TAG, "Couldn't delete >" + file.getName() + "<");
                        return false;
                    }
                }
            }
        }
        //noinspection SimplifiableIfStatement
        if (delRoot) {
            return dir.delete();
        }
        return true;
    }

    private HashMap<String, Object> setUpOcClientArguments() {
        OwnCloudClient mClientOwnCloud;
        Uri serverUri = Uri.parse(settObj.getSourcePath());
        if (settObj.getUserName().equals("") || settObj.getUserPassword().equals("") ||
                settObj.getSourcePath().equals("") ||
                settObj.getSourcePath().equals("https://") || // TODO get from Default
                serverUri == null) {
            return null;
        }
        if (DEBUG) Log.i(TAG, "OwnCloud serverUri: " + serverUri);
        // Create client object to perform remote operations
        mClientOwnCloud = OwnCloudClientFactory.createOwnCloudClient(serverUri, getApplicationContext(), true);
        mClientOwnCloud.setCredentials(
                OwnCloudCredentialsFactory.newBasicCredentials(
                        settObj.getUserName(),
                        settObj.getUserPassword()
                )
        );
        args.put(Downloader_OC.CLIENT, mClientOwnCloud);
        args.put(Downloader_OC.HANDLER, new Handler());
        args.put(Keys.PICFRAMEPATH, settObj.getExtFolderAppRoot());
        args.put(Keys.CONTEXT, getApplicationContext());
    //    args.put(Downloader_OC.REMOTEFOLDER,   settObj. getRemoteFolderPath); TODO FOLDERPICKER STUFF
        args.put(Keys.CALLBACK, this);
        return args;
    }

    // to stop download from notification - download->false, interrupt downloader
    public void stopDownload() {
        if (DEBUG)  Log.d(TAG, "user clicked 'stopDownload' -- ending");
        downloading = false;
        finished = false;
        interruptDownloaderAndStopService();
    }

    // handling the notifications
    private void showNotification(Keys.NotificationStates notification_state, int progress, String msg) {

        PendingIntent mainPendIntent = PendingIntent
                .getActivity(this, 0, mainActIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent stopDownloadPendIntent = PendingIntent
                .getService(this, 0, stopDownloadIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder
            .setSmallIcon(android.R.drawable.stat_sys_download_done)// set tickerIcon (download-icon)
            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher)) // set app-icon
            .setContentIntent(mainPendIntent)                       // set intent for onTap
            .setPriority(NotificationCompat.PRIORITY_MAX)           // to stay on top
            .setContentTitle(getString(R.string.app_name))          // set title
            .setWhen(System.currentTimeMillis())                    // timestamp
            .setDefaults(NotificationCompat.FLAG_AUTO_CANCEL)       // to dismiss notifiction on click
            .setOnlyAlertOnce(true)                                 // sound, vibrate and ticker only ONCE
            .setVisibility(NotificationCompat.VISIBILITY_SECRET);   // to hide from lockScreen

        if (downloading &&
                (   Keys.NotificationStates.START.equals(notification_state) ||
                    Keys.NotificationStates.PROGRESS.equals(notification_state))) {
            notificationBuilder
                    .setOngoing(true)
                    .setAutoCancel(false);                          // to stop from dismissing notif. on click
        // START
            if (Keys.NotificationStates.START.equals(notification_state)) {
                notificationBuilder
                        .setTicker(getString(R.string.app_name) + " - " + getString(R.string.service_notif_startDownloadTicker))
                        .setContentText(getString(R.string.service_notif_startDownloadText))
                        .setSubText("")
                        .addAction(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.service_notif_actionStop), stopDownloadPendIntent)
                        .setProgress(100, 50, true);
        // PROGRESS
            } else if (Keys.NotificationStates.PROGRESS.equals(notification_state)) {
                notificationBuilder
                        .setContentText(getString(R.string.service_notif_progressText) + " " + progress + "%")
                        .setSubText("")
                        .setProgress(100, progress, false);
            }
        } else if (Keys.NotificationStates.FAILURE.equals(notification_state)
                || Keys.NotificationStates.INTERRUPT.equals(notification_state)
                || Keys.NotificationStates.STOP.equals(notification_state)
                || Keys.NotificationStates.FINISHED.equals(notification_state)) {
            stopForeground(true);
            // Reflection to remove the stop action: https://code.google.com/p/android/issues/detail?id=68063
            //noinspection TryWithIdenticalCatches
            try {
                Field f = notificationBuilder.getClass().getDeclaredField("mActions");
                f.setAccessible(true);
                f.set(notificationBuilder, new ArrayList<NotificationCompat.Action>());
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            //notificationManager.cancel(Keys.NOTIFICATION_ID);
            notificationBuilder
                    .setOngoing(false)
                    .setAutoCancel(true);
        // FAILURE
            if (Keys.NotificationStates.FAILURE.equals(notification_state)) {
                notificationBuilder
                        .setTicker(getString(R.string.app_name) + " - " + getString(R.string.service_notif_failureTicker))
                        .setContentText(getString(R.string.service_notif_failureText))
                        .setSubText(msg);
        // INTERRUPT
        /*    } else if (Keys.NotificationStates.INTERRUPT.equals(notification_state)) {
                notificationBuilder
                        .setTicker(getString(R.string.app_name) + " - " + getString(R.string.service_notif_interruptTicker))
                        .setContentText(getString(R.string.service_notif_interruptText))
                        .setSubText(getString(R.string.service_notif_stoppedSubText));
        */
        // FINISHED
            } else if (Keys.NotificationStates.FINISHED.equals(notification_state)) {
                notificationBuilder
                        .setTicker(getString(R.string.app_name) + " - " + getString(R.string.service_notif_finishedTicker))
                        .setContentText(getString(R.string.service_notif_finishedText) + " " + msg)
                        .setSubText(getString(R.string.service_notif_stoppedSubText));
        // STOPPED
            } else if (Keys.NotificationStates.STOP.equals(notification_state)) {
                notificationBuilder
                        .setTicker(getString(R.string.app_name) + " - " + getString(R.string.service_notif_stoppedTicker))
                        .setContentText(getString(R.string.service_notif_stoppedText))
                        .setSubText(getString(R.string.service_notif_stoppedSubText));
            }
        }
        notificationManager.notify(Keys.NOTIFICATION_ID, notificationBuilder.build());
    }

    /* CALLBACKS */
    @Override
    public void publishProgress(float progressPercent, boolean progressIndeterminate) {
        Log.d(TAG, "SHOW NOTIFICATION PROGRESS: " + progressPercent);
        showNotification(Keys.NotificationStates.PROGRESS, Math.round(progressPercent), null);
    }

    @Override
    public void finishedDownload(int count) {
        // when finished, show changed notification with "open" button
        // DEPRECATED: [or in app, the full pogressbar, which get's invisible after several seconds again.]
        // now the download is finished, we can stop the service.
        // if service is stopped, notification is active and app gets opened, delete notification
        if (DEBUG)  Log.d(TAG, "downloader sent 'finishedDownload' -- ending");
        downloading = false;
        finished = true;
        if (count <= 0) {
            showNotification(Keys.NotificationStates.FINISHED, 0, getString(R.string.service_notif_finishedNoNewFiles));
        } else {
            showNotification(Keys.NotificationStates.FINISHED, 0, (String.valueOf(count) + " " + getString(R.string.service_notif_finishedTextFiles)));
            broadcastManager.sendBroadcast(new Intent().setAction(Keys.ACTION_DOWNLOAD_FINISHED));  // TODO not only after finished
        }
        stopSelf();
    }

    @Override
    public void downloadFailed(Downloader.Failure failureMsg) {
        if (DEBUG)  Log.d(TAG, "downloader sent 'downloadFailed' -- ending");
        downloading = false;
        finished = false;
        if (Downloader.Failure.LOGIN.equals(failureMsg)) {
            showNotification(Keys.NotificationStates.FAILURE, 0, getString(R.string.service_notif_failureLogin));
        } else if (Downloader.Failure.WIFI.equals(failureMsg)) {
            showNotification(Keys.NotificationStates.FAILURE, 0, getString(R.string.service_notif_failureNoWifi));
        } else if (Downloader.Failure.STORAGESPACE.equals(failureMsg)) {
            showNotification(Keys.NotificationStates.FAILURE, 0, getString(R.string.service_notif_failureNotEnoughStorage));
        } else if (Downloader.Failure.INTERRUPT.equals(failureMsg)) {
            // aborted by user - delete notification, end service
            showNotification(Keys.NotificationStates.STOP, 0, null);
        }
        interruptDownloaderAndStopService();
    }

    /*
    @Override
    public void interruptedDownload() {
        // downloader has lost wifi connection or got interrupted by the destruction of the service.
        if (DEBUG)  Log.d(TAG, "downloader sent 'interruptedDownload' -- ending");
        if (downloading) {
            // interrupted - set interrupted notification, end service
            showNotification(Keys.NotificationStates.INTERRUPT, 0, null);
        } else {
            // aborted by user - delete notification, end service
            showNotification(Keys.NotificationStates.STOP, 0, null);
        }
        downloading = false;
        finished = false;
        stopSelf();
    }
*/
}
