package picframe.at.picframe.downloader;

import android.content.Context;
import android.graphics.Bitmap;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import picframe.at.picframe.Keys;
import picframe.at.picframe.helper.GlobalPhoneFuncs;
import picframe.at.picframe.helper.viewpager.EXIF_helper;
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
    private String remoteFolder;                                // folder, in which the download should start   TODO folderpicker stuff
    private Handler mHandler;
    private Context mContext;                                   // only needed for EXIF-helper to get screen width and height
    private String mExtFolderAppRoot;
    private String mExtFolderDisplayPath;
    private String mExtFolderCachePath;
    private ArrayList<String> mLocalFileList;                   // files (relative path), that are currently in the display folder
    private ArrayList<RemoteFile> mRemoteFileList;              // all files on remote server
    private ArrayList<RemoteFile> mRemoteFilesToDownloadList;   // files that need to be downloaded (full remote paths to the files)
    public LinkedBlockingQueue<String> mDownloadedFiles;        // files downloaded successfully - empty after processed all files
    private AtomicInteger mDownloadedFilesCount;                // total count of successfully downloaded files
    private AtomicInteger mThreadCounter;                       // Count active threads so task only finishes once all threads are done



    private boolean failedOnce = false;                         // only relay the first failure to the service (flag to ensure this)
    private final int MIN_MB_REMAINING = 15;                    // if storage is smaller than 15 MB, stop the download!
    private final int MIN_BYTES_REMAINING = MIN_MB_REMAINING * 1024 * 1024;
    private AtomicBoolean loginRequest = new AtomicBoolean(false);
    private AtomicBoolean loginResultSuccess = new AtomicBoolean(false);



    private boolean loginFailed = false;
    private boolean firstOperation = true;



    public Downloader_OC(HashMap<String, Object> args) {
        if (DEBUG) Log.d(TAG, "created OC Downloader");
        mClient = (OwnCloudClient) args.get(CLIENT);
        mHandler = (Handler) args.get(HANDLER);
        mExtFolderAppRoot = (String) args.get(Keys.PICFRAMEPATH);
        remoteFolder = (String) args.get(REMOTEFOLDER);
        serviceCallbacks = (ServiceCallbacks) args.get(Keys.CALLBACK);
        mContext = (Context) args.get(Keys.CONTEXT);
    }

    @Override
    public void run() {
        if (DEBUG) Log.d(TAG, "started OC Downloader");
        initializeCounterArraysQueuesPaths();

        if (DEBUG) Log.i(TAG,
                    "AppRootFolder: "   + mExtFolderAppRoot +
                " -- CacheFolder: "     + mExtFolderCachePath +
                " -- DisplayFoler: "    + mExtFolderDisplayPath);
        if (DEBUG) Log.i(TAG, "thread counter at first: " + mThreadCounter.get());
        if (DEBUG) Log.i(TAG, "###### STARTING OWNCLOUD DOWNLOAD TASK #####");


        if (!checkLogin()) {
            downloadFailure(Failure.LOGIN);
            return;
        }
        // populate the mRemoteFileList with all the (accepted) remote files found on server
        if (!readAllRemoteFoldersInDirAndWait()) {
            downloadFailure(Failure.INTERRUPT);
            return;
        }
        // check which files have to be downloaded (not downloaded yet) (false if none/0 to download)
        if (!compareListsAndCheckForNewFilesToDownload()) {
            finishedDownload();
            return;
        }
        // process and download all images (false if interrupted)
        if (!processAndDownloadLoop()) {
            downloadFailure(Failure.INTERRUPT);
            return;
        }

        finishedDownload();
        // END OF THREAD!
    }

    private void initializeCounterArraysQueuesPaths() {
        // set counter to zero
        mThreadCounter = new AtomicInteger(0);
        mDownloadedFilesCount = new AtomicInteger(0);

        // initialise empty arraylists and queue
        mRemoteFileList = new ArrayList<>();
        mLocalFileList = new ArrayList<>();
        mRemoteFilesToDownloadList = new ArrayList<>();
        mDownloadedFiles = new LinkedBlockingQueue<>();

        // build the paths to the needed folders    // TODO: once new AppData is merged, change these 2 lines
        mExtFolderDisplayPath = mExtFolderAppRoot + File.separator + "pictures";
        mExtFolderCachePath = mExtFolderAppRoot + File.separator + "cache";
    }

    private boolean checkLogin() {
        loginRequest.set(true);
        mThreadCounter.getAndIncrement();
        ReadRemoteFolderOperation loginCheckOperation =
                new ReadRemoteFolderOperation(FileUtils.PATH_SEPARATOR);
        loginCheckOperation.execute(mClient, this, mHandler);
        while (loginRequest.get()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                return false;
            }
        }
        return loginResultSuccess.get();
    }

    private boolean readAllRemoteFoldersInDirAndWait() {
        // read all files on the server                     // TODO: instead of "/" use remoteFolderPath
        remoteReadAnyFolder(FileUtils.PATH_SEPARATOR);      // TODO: limit read too (like download)
        // wait for all read operations to finish
        while(mThreadCounter.get() > 0) {
            if (isInterrupted()) {
                if (DEBUG) Log.e(TAG, "Thread cancelled! -- position: run() - waited for reads to end");
                return false;
            }
        }
        return true;
    }

    private void remoteReadAnyFolder(String path) {
        if (isInterrupted()) {
            if (DEBUG) Log.e(TAG, "Thread cancelled! -- position: remoteReadAnyFolder");
            downloadFailure(Failure.INTERRUPT);
            return;
        }
        mThreadCounter.getAndIncrement();
        ReadRemoteFolderOperation refreshOperation = new ReadRemoteFolderOperation(path);
        refreshOperation.execute(mClient, this, mHandler);
    }

    private boolean compareListsAndCheckForNewFilesToDownload(){
        File mExtFolderCacheObject = new File(mExtFolderDisplayPath);
        File[] mLocalFilesInDisplayFolder = mExtFolderCacheObject.listFiles();

        if (mLocalFilesInDisplayFolder == null || mLocalFilesInDisplayFolder.length == 0) {
            if (DEBUG) Log.i(TAG, "Display folder is empty, all remote files are to be downloaded.");
            for(RemoteFile remoteFile : mRemoteFileList){
                mRemoteFilesToDownloadList.add(remoteFile);
            }
        } else {
            if (DEBUG) Log.i(TAG, "Display folder not empty, checking files...");
            // save names (with relative path) of all files in local folder
            addFilepathsToLocalFileList(mLocalFilesInDisplayFolder);
            // for every remote file, check if it already exists locally
            for (RemoteFile remoteFile : mRemoteFileList) {
                if (mLocalFileList.contains(remoteFile.getRemotePath())) {
                    if (DEBUG) Log.i(TAG, "File(" + remoteFile.getRemotePath() + ") exists locally!");
                } else {
                    // if the file is new, save its path
                    if (DEBUG) Log.i(TAG, "File(" + remoteFile.getRemotePath() + ") doesn't exist locally! -> download it!");
                    mRemoteFilesToDownloadList.add(remoteFile);
                }
            }
        }
        if (mRemoteFilesToDownloadList.size() == 0) {
            if (DEBUG) Log.i(TAG, "No file needs to be downloaded!");
            return false;
        } else {
            if (DEBUG) Log.i(TAG, "Starting download of missing files (" + mRemoteFilesToDownloadList.size() + ") shortly");
            return true;
        }
    }

    private void addFilepathsToLocalFileList(File[] files){
        for (File file : files) {
            if (file.isDirectory()) {
                addFilepathsToLocalFileList(file.listFiles());
            } else {
                // substring to get the relative path
                String fileRelPath = file.getPath().substring(mExtFolderDisplayPath.length());
                //if (DEBUG) Log.i(TAG + " -addFilepaths", "filename:" + fileRelPath);
                mLocalFileList.add(fileRelPath);
            }
        }
    }

    private boolean processAndDownloadLoop() {
        int fileIndex = 0;
        File mExtFolderCacheObject = new File(mExtFolderCachePath);
        boolean cacheFolderExists = mExtFolderCacheObject.exists();
        while( mThreadCounter.get() > 0 ||
                    (mDownloadedFilesCount.get() != mRemoteFilesToDownloadList.size()) ||
                    mDownloadedFiles.peek() != null) {
            if (mDownloadedFiles.peek() != null) {
                //scale, rotate, save smaller image, delete cached/big image
                processFile();
            }
            if (isInterrupted()) {
            // if the user aborted the download, process the remaining files
                if (DEBUG) Log.e(TAG, "Thread cancelled! -- position: while-loop in 'run()'");
                if (mDownloadedFiles.peek() == null) {
                    return false;
                }
            } else {
            // else carry on downloading the remaining images
                if (mRemoteFilesToDownloadList.size() > 0 &&
                        mDownloadedFilesCount.get() < mRemoteFilesToDownloadList.size()) {
                    // check every 3 files, if still connected to wifi
                    if ((fileIndex % 3) == 0) {
                        if (!GlobalPhoneFuncs.wifiConnected()) {
                            downloadFailure(Failure.WIFI);
                            continue;
                        }
                    }
                    // start download
                    if (cacheFolderExists && mThreadCounter.get() < 2 &&
                            fileIndex < mRemoteFilesToDownloadList.size()) {
                        RemoteFile remoteFile = mRemoteFilesToDownloadList.get(fileIndex);
                        // if more than MIN_BYTES_REMAINING bytes would be free after the download -> download
                        if ((GlobalPhoneFuncs.getSdCardFreeBytes() - remoteFile.getLength()) > MIN_BYTES_REMAINING) {
                            if (DEBUG) Log.i(TAG, "Starting download: " + remoteFile.getRemotePath());
                            mThreadCounter.getAndIncrement();
                            DownloadRemoteFileOperation downloadOperation =
                                    new DownloadRemoteFileOperation(remoteFile.getRemotePath(), mExtFolderCacheObject.getAbsolutePath());
                            downloadOperation.execute( mClient, this, mHandler);
                            fileIndex++;
                        } else {
                            if (DEBUG) Log.e(TAG, "No more space available on the sd card!");
                            downloadFailure(Failure.STORAGESPACE);
                        }
                    }
                }
            }
        }
        return true;
    }

    private void processFile() {
        boolean processed;
        boolean deleted;
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

    @Override
    public void onRemoteOperationFinish(RemoteOperation operation, RemoteOperationResult result) {
        if (loginRequest.get()) {
            if (operation instanceof ReadRemoteFolderOperation) {
                if (result.isSuccess()) {
                    if (DEBUG)  Log.d(TAG, "operation successful, login/connection success");
                    loginResultSuccess.set(true);
                } else {
                    if (DEBUG)  Log.d(TAG, "operation unsuccessful, login/connection failed");
                }
            }
            mThreadCounter.getAndDecrement();
            loginRequest.set(false);
            return;
        }
        if (operation instanceof ReadRemoteFolderOperation) {
            handleReadOperation(result);
        } else if (operation instanceof DownloadRemoteFileOperation) {
            handleDownloadOperation(result);
        }
    }

    private void handleReadOperation(RemoteOperationResult result) {
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
                        if (!mRemoteFileList.contains(remoteFile)) {
                            mRemoteFileList.add(remoteFile);
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
    }

    private void handleDownloadOperation(RemoteOperationResult result) {
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
        } else {
            if (DEBUG) Log.e(TAG, "Download: FAILURE -- info:" + result.getFilename() + " = " + result.getLogMessage());
        }
        mThreadCounter.getAndDecrement();
    }

    @Override
    public void onTransferProgress(long progressRate, long totalTransferredSoFar,
                                   long totalToTransfer, String fileAbsoluteName) { }


    /*  CALLBACKS TO SERVICE */

    private void publishProgress(float percentAsFloat, boolean indeterminate) {
        if (serviceCallbacks != null) {
            if (percentAsFloat > 0)
                percentAsFloat *= 100;
            serviceCallbacks.publishProgress(percentAsFloat, indeterminate);
            if (DEBUG) Log.i(TAG, "% update: " + percentAsFloat);
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

    private void downloadFailure(Downloader.Failure failType) {
        if (!failedOnce) {
            // Only send the first failureType to the Service (could be Wifi->Interrupt)
            failedOnce = true;
            Thread.currentThread().interrupt();
            if (DEBUG) Log.e(TAG, "Thread interrupted!");
            if (serviceCallbacks != null) {
                serviceCallbacks.downloadFailed(failType);
            } else {
                if (DEBUG)  Log.d(TAG, "couldn't fire 'downloadFailed' , event obj.is null");
            }
        } else {
            if (Failure.INTERRUPT.equals(failType)) {
                if (DEBUG)  Log.e(TAG, "Thread interrupted");
                // TODO: release resources?!
            }
        }
    }
}
