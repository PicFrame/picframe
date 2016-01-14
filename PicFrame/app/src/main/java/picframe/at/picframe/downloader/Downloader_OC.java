package picframe.at.picframe.downloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;

import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.network.OnDatatransferProgressListener;
import com.owncloud.android.lib.common.operations.OnRemoteOperationListener;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.DownloadRemoteFileOperation;
import com.owncloud.android.lib.resources.files.FileUtils;
import com.owncloud.android.lib.resources.files.ReadRemoteFolderOperation;
import com.owncloud.android.lib.resources.files.RemoteFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import picframe.at.picframe.helper.GlobalPhoneFuncs;
import picframe.at.picframe.helper.settings.AppData;
import picframe.at.picframe.helper.viewpager.EXIF_helper;
import picframe.at.picframe.Keys;
import picframe.at.picframe.service_broadcast.ServiceCallbacks;


@SuppressWarnings("ConstantConditions")
public class Downloader_OC extends Downloader implements OnRemoteOperationListener, OnDatatransferProgressListener {
    private final String TAG = this.getClass().getSimpleName();
    private final boolean DEBUG = true;

    // Keys for the argument hashmap
    public static final String CLIENT = "OcClient";
    public static final String HANDLER = "OcHandler";
    public static final String REMOTEFOLDER = "OcRemoteFolder";

    // private fields holding the download-parameter-data
    private OwnCloudClient mClient;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private String remoteFolder;                                // folder, in which the download should start   TODO folderpicke stuff
    private Handler mHandler;
    private Context mContext;                                   // only needed for EXIF-helper to get screen width and height
    private String mExtFolderAppRoot;
    private String mExtFolderDisplayPath;
    private String mExtFolderCachePath;
    private ArrayList<String> mLocalFileList;                   // files (relative path), that are currently in the display folder
    private ArrayList<RemoteFile> remoteFileList;               // all files on remote server
    private ArrayList<RemoteFile> mRemoteFilesToDownloadList;       // files that need to be downloaded (full remote paths to the files)
    public LinkedBlockingQueue<String> mDownloadedFiles;        // files downloaded successfully - empty after processed all files
    private AtomicInteger mDownloadedFilesCount;                // total count of sucessfully downloaded files
    private AtomicInteger mThreadCounter;                       // Count active threads so task only finishes once all threads are done
    private boolean loginFailed = false;
    private boolean firstOperation = true;


    public Downloader_OC(HashMap<String, Object> args) {
        mClient = (OwnCloudClient) args.get(CLIENT);
        mHandler = (Handler) args.get(HANDLER);
        mExtFolderAppRoot = (String) args.get(Keys.PICFRAMEPATH);
        remoteFolder = (String) args.get(REMOTEFOLDER);
        serviceCallbacks = (ServiceCallbacks) args.get(Keys.CALLBACK);
        mContext = (Context) args.get(Keys.CONTEXT);
        if (DEBUG) Log.d(TAG, "created OC Downloader");
    }

    @Override
    public void run() {
        if (DEBUG) Log.d(TAG, "started OC Downloader");
        // set counter to zero
        mThreadCounter = new AtomicInteger(0);
        mDownloadedFilesCount = new AtomicInteger(0);

        // initialise empty arraylists and queue
        remoteFileList = new ArrayList<>();
        mLocalFileList = new ArrayList<>();
        mRemoteFilesToDownloadList = new ArrayList<>();
        mDownloadedFiles = new LinkedBlockingQueue<>();

        // build the paths to the needed folders
        mExtFolderDisplayPath = mExtFolderAppRoot + File.separator + "pictures";
        mExtFolderCachePath = mExtFolderAppRoot + File.separator + "cache";

        if (DEBUG) Log.i(TAG, "AppRootFolder: "+ mExtFolderAppRoot +
                " -- CacheFolder: " +mExtFolderCachePath +
                " -- DisplayFoler: " +mExtFolderDisplayPath);
        if (DEBUG) Log.i(TAG, "thread counter at first: " + mThreadCounter.get());
        if (DEBUG) Log.i(TAG, "###### STARTING OWNCLOUD DOWNLOAD TASK #####");

        // read all files on the server
        remoteReadAnyFolder(FileUtils.PATH_SEPARATOR);      // TODO: limit read too (like download)

        // await for all read operations to finish
        while(mThreadCounter.get() > 0) {
            if (isInterrupted()) {
                if (DEBUG) Log.e(TAG, "Thread cancelled! -- position: doInB -waited for reads");
                interruptDownload();
                return;
            }
            if (loginFailed) {
                return;
            }
        }

        // check which files aren't in the picture folder yet
        compareLocalAndRemoteFolder();
        if (mRemoteFilesToDownloadList.size() == 0) {
            return;
        }

        boolean processed, deleted;
        int fileIndex = 0;
        File mExtFolderCacheObject = new File(mExtFolderCachePath);
        boolean cacheFolderExists = mExtFolderCacheObject.exists();
        //noinspection PointlessBooleanExpression
        if (DEBUG && cacheFolderExists) Log.i(TAG, "mExtFolderCacheObject exists! (" + mExtFolderCachePath + ")");
        if (DEBUG) Log.i(TAG, "Number of files to download: " + mRemoteFilesToDownloadList.size());
        // while there are still files to process and the download isn't done yet, loop
        while(
                mThreadCounter.get() > 0 ||
                        (mDownloadedFilesCount.get() != mRemoteFilesToDownloadList.size())  ||
                        mDownloadedFiles.peek() != null) {    // Retrieves but doesn't remove from FIFO
            if (mDownloadedFiles.peek() != null) {      // Check if DownloadList has files to process
                String fileToProcess = mDownloadedFiles.poll();
                processed = scaleRotateAndSave(fileToProcess);
                if (processed) {    // if rotate, scale and move worked, delete from list, and delete src file
                    if (DEBUG) Log.i(TAG, "rot/scaled/saved >" + fileToProcess + "<");
                    File fileToDelete = new File(fileToProcess);
                    if (fileToDelete.exists() && fileToDelete.isFile()) {
                        deleted = fileToDelete.delete();
                        if (deleted)
                            if (DEBUG) Log.i(TAG, "deleted >" + fileToProcess + "<");
                            else
                            if (DEBUG) Log.e(TAG, "couldn't delete >" + fileToProcess + "<");
                    }
                } else {
                    if (DEBUG) Log.e(TAG, "Failure while processing file[" + fileToProcess + "]. (scaleRotateAndSave");
                }
            }
            // if cancelled, process downloaded pictures and then stop
            if (isInterrupted()) {
                if (DEBUG) Log.e(TAG, "Thread cancelled! -- position: doInBackground - downloadFiles");
                if (mDownloadedFiles.peek() == null)
                    interruptDownload();
                    return;
            }  else {
                if (mRemoteFilesToDownloadList.size() > 0 &&
                        mDownloadedFilesCount.get() < mRemoteFilesToDownloadList.size()) {
                    // download here (but check every six files, if still connected to wifi
                    if ((fileIndex % 6) == 0) {
                        if (!wifiConnected()) {
                            Thread.currentThread().interrupt();
                            continue;
                        }
                    }
                    if (cacheFolderExists
                            && mThreadCounter.get() < 2
                            && fileIndex < mRemoteFilesToDownloadList.size()) {
                        RemoteFile remoteFile = mRemoteFilesToDownloadList.get(fileIndex);
                        if (15 * 1024 * 1024 < GlobalPhoneFuncs.getSdCardFreeBytes() - remoteFile.getLength()) {
                            if (DEBUG) Log.i(TAG, "Starting download: " + remoteFile.getRemotePath());
                            mThreadCounter.getAndIncrement();
                            DownloadRemoteFileOperation downloadOperation =
                                    new DownloadRemoteFileOperation(remoteFile.getRemotePath(), mExtFolderCacheObject.getAbsolutePath());
                            downloadOperation.execute( mClient, this, mHandler);
                            fileIndex++;
                        } else {
                            if (DEBUG) Log.e(TAG, "No more space available on the sd card!");
                            publishProgress(-1.5f, false);
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }
            /* catch (InterruptedException e) {            // no interrupted exception anymore, due to no Thread.sleeps
                if (DEBUG) Log.e(TAG, "Exception happened while trying to await end of remote downloads");
                //e.printStackTrace();
            }*/
        }

        finishedDownload();
        // END OF THREAD!
    }

    private void remoteReadAnyFolder(String path) {
        if (isInterrupted()) {
            if (DEBUG) Log.e(TAG, "Thread cancelled! -- position: remoteReadAnyFolder");
            interruptDownload();
            return;
        }
        mThreadCounter.getAndIncrement();
        ReadRemoteFolderOperation refreshOperation = new ReadRemoteFolderOperation(path);
        refreshOperation.execute(mClient, this, mHandler);
    }

    @Override
    public void onRemoteOperationFinish(RemoteOperation operation, RemoteOperationResult result) {
        if (firstOperation) {
            if (!result.isSuccess()) {
                if (DEBUG)  Log.d(TAG, "operation unsuccessful, login/connection failed");
                AppData.setLoginSuccessful(false);
                this.loginFailed();
                return;
            } else {
                AppData.setLoginSuccessful(true);
            }
            firstOperation = false;
        }
        if (operation instanceof ReadRemoteFolderOperation) {
            if (result.isSuccess()) {
                ArrayList files = (ArrayList)result.getData();
                for(int i=0; i < files.size(); i++){
                    if (i==0) continue;     // don't include first element in list as that is the read folder itself
                    RemoteFile remoteFile = (RemoteFile) files.get(i);
                    String mimeType = remoteFile.getMimeType();
                    switch (mimeType) {
                        case "DIR":
                            if (DEBUG) Log.i(TAG, "remote folder[" + mimeType + "]: " + remoteFile.getRemotePath());
                            remoteReadAnyFolder(remoteFile.getRemotePath());
                            break;
                        case "image/jpeg":
                        case "image/png":
                            if (DEBUG) Log.i(TAG, "remote file[" + mimeType + "]: " + remoteFile.getRemotePath());
                            if (!remoteFileList.contains(remoteFile)) {
                                remoteFileList.add(remoteFile);
                            }
                            break;
                        default:
                            if (DEBUG) Log.i(TAG, "WRONG file[" + mimeType + "]: " + remoteFile.getRemotePath());
                            break;
                    }
                }
            }
            mThreadCounter.getAndDecrement();
            if (DEBUG) Log.i(TAG, "Threadcounter after read operation: " + mThreadCounter.get());
        } else if (operation instanceof DownloadRemoteFileOperation) {
            if (result.isSuccess()) {
                if (DEBUG) Log.d(TAG, "Download: success -- info:" + result.getFilename()); // LogMessage: success, status code 200
                try {
                    mDownloadedFiles.put(result.getFilename());
                } catch (InterruptedException e) {
                    if (DEBUG) Log.e(TAG, "Couldn't put the downloaded file (" + result.getFilename() + ") on the list of downloaded files");
                    e.printStackTrace();
                }
                mDownloadedFilesCount.getAndIncrement();
                publishProgress((float)mDownloadedFilesCount.get() / (float)mRemoteFilesToDownloadList.size(), false);  // is between 0 and 1
            }
            mThreadCounter.getAndDecrement();
        }
    }

    private void compareLocalAndRemoteFolder(){
        File mExtFolderCacheObject = new File(mExtFolderDisplayPath);
        File[] mLocalFilesInDisplayFolder = mExtFolderCacheObject.listFiles();

        if (mLocalFilesInDisplayFolder == null || mLocalFilesInDisplayFolder.length == 0) {
            if (DEBUG) Log.i(TAG, "Display folder is empty, all remote files are being downloaded.");
            for(RemoteFile remoteFile : remoteFileList){
                mRemoteFilesToDownloadList.add(remoteFile);
            }
        } else {
            if (DEBUG) Log.i(TAG, "Display folder not empty, checking files...");
            addFilepathsToLocalFileList(mLocalFilesInDisplayFolder);  // save names (with relative path) of all files in local folder
            // For every remote file, check if it already exists locally
            for (RemoteFile remoteFile : remoteFileList) {
                if (mLocalFileList.contains(remoteFile.getRemotePath())) {
                    if (DEBUG) Log.i(TAG, "File(" + remoteFile.getRemotePath() + ") exists locally!");
                } else {
                    // if the file is new, save its path
                    if (DEBUG) Log.i(TAG, "File(" + remoteFile.getRemotePath() + ") doesn't exist locally! -> download it!");
                    mRemoteFilesToDownloadList.add(remoteFile);
                }
            }
            if (mRemoteFilesToDownloadList.size() == 0) {
                if (DEBUG) Log.i(TAG, "No file needs to be downloaded!");
                publishProgress(-1f, false);
            } else {
                if (DEBUG) Log.i(TAG, "Starting download of missing files shortly");
            }
        }
    }

    private void addFilepathsToLocalFileList(File[] files){
        for (File file : files) {
            if (file.isDirectory()) {
                addFilepathsToLocalFileList(file.listFiles());
            } else {
                // substring to get the relative path
                String fileRelPath = file.getPath().substring(mExtFolderDisplayPath.length());
                if (DEBUG) Log.i(TAG + " -addFilepaths", "filename:" + fileRelPath);
                mLocalFileList.add(fileRelPath);
            }
        }
    }

    private boolean wifiConnected() {
        ConnectivityManager connManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifi != null && wifi.isConnected();
    }

    @Override
    public void onTransferProgress(long progressRate, long totalTransferredSoFar,
                                   long totalToTransfer, String fileAbsoluteName) { }

    private boolean scaleRotateAndSave(String filepath) {
        if (filepath.contains(mExtFolderCachePath)) {
            filepath = filepath.substring(mExtFolderCachePath.length());
        }
        String srcPath = mExtFolderCachePath + filepath;
        if (DEBUG) Log.i(TAG, "srcPath: " + srcPath);
        String newPath = mExtFolderDisplayPath + filepath;
        if (DEBUG) Log.i(TAG, "newPath: " + newPath);

        Bitmap image = EXIF_helper.decodeFile(srcPath, mContext);
        String fs = File.separator;
        boolean dirCreationWorked;
        if (filepath.startsWith(File.separator)) {
            filepath = filepath.substring(1, filepath.length());
        }
        String[] parts = filepath.split(Pattern.quote(fs));
        if(filepath.contains(fs)) {
            String pathToCreate = mExtFolderDisplayPath;
            for(int i = 0; i < parts.length-1; i++) {
                if (!pathToCreate.endsWith(File.separator))
                    pathToCreate = pathToCreate.concat(File.separator);
                pathToCreate = pathToCreate.concat(parts[i]);
                File dirMaking =  new File(pathToCreate);
                if (dirMaking.exists() && dirMaking.isDirectory())
                    continue;
                dirCreationWorked = dirMaking.mkdir();
                if (!dirCreationWorked) {
                    if (DEBUG) Log.i(TAG, "FAILED to create: " + dirMaking);
                    return false;
                }
            }
        }
        File f = new File(newPath);
        // Check whether scaled img exists
        if(f.exists()) {
            if (DEBUG) Log.i(TAG, "testfile exists - deleting now");
            if (f.delete()) {
                if (DEBUG) Log.i(TAG, "deletion successfull");
            } else {
                if (DEBUG) Log.e(TAG, "deletion failed");
            }
        }
        if (DEBUG) Log.i(TAG, "Saving...  Filepath: " + f.getAbsolutePath());
        FileOutputStream fOut;
        try {
            fOut  = new FileOutputStream(f);
            if(parts[parts.length-1].toLowerCase().endsWith(".jpg") ||
                    parts[parts.length-1].toLowerCase().endsWith(".jpeg")) {
                image.compress(Bitmap.CompressFormat.JPEG, 80, fOut);
            }
            else {
                image.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            }
            fOut.flush();
            fOut.close();
        } catch (NullPointerException npe) {
            if (DEBUG) Log.e(TAG, "Couldn't compress bitmap");
            //npe.printStackTrace();
            return false;
        } catch (FileNotFoundException e) {
            if (DEBUG) Log.e(TAG, "Couldn't create FileOutputStream - file not found");
            //e.printStackTrace();
            return false;
        } catch (IOException e) {
            if (DEBUG) Log.e(TAG, "Couldn't flush or close");
            //e.printStackTrace();
            return false;
        }
        return true;
    }


    /*  CALLBACKS TO SERVICE */

    private void publishProgress(float f, boolean b) {
        if (serviceCallbacks != null) {
            if (f > 0)
                f *= 100;
            serviceCallbacks.publishProgress(f, b);
            if (DEBUG) Log.i(TAG, "% update: " + f);
        } else {
            if (DEBUG)  Log.d(TAG, "couldn't publish progress, event object is null");
        }
    }

    private void finishedDownload() {
        if (serviceCallbacks != null) {
            if (DEBUG) Log.i(TAG, "###### FINISHED OWNCLOUD TASK #####");
            serviceCallbacks.finishedDownload(mDownloadedFilesCount.get());
        } else {
            if (DEBUG)  Log.d(TAG, "couldn't 'finish' download, event obj.is null");
        }
    }

    private void interruptDownload() {
        if (serviceCallbacks != null) {
            serviceCallbacks.interruptedDownload();
        } else {
            if (DEBUG)  Log.d(TAG, "couldn't 'interrupt' download, event obj.is null");
        }
    }

    private void loginFailed() {
        loginFailed = true;
        if (serviceCallbacks != null) {
            serviceCallbacks.loginFailed();
        } else {
            if (DEBUG)  Log.d(TAG, "couldn't fire 'loginFailed' , event obj.is null");
        }
    }
}
