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

package picframe.at.picframe.helper.DEPRECATED_owncloud;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.network.OnDatatransferProgressListener;
import com.owncloud.android.lib.common.operations.OnRemoteOperationListener;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.FileUtils;
import com.owncloud.android.lib.resources.files.ReadRemoteFolderOperation;
import com.owncloud.android.lib.resources.files.RemoteFile;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class OC_ReadFolderStructure extends AsyncTask<Object, Float, Object>
        implements OnRemoteOperationListener, OnDatatransferProgressListener {

    private final String TAG = this.getClass().getSimpleName();
    private String mExtFolderDisplayPath;
    private String mExtFolderCachePath;
    private OwnCloudClient mClient;
    private Handler mHandler;
    private Context mContext;
    private ArrayList<RemoteFile> remoteFileList;               // all files on remote server
    private ArrayList<RemoteFile> mRemoteFilesToDownloadList;       // files that need to be downloaded (full remote paths to the files)
    public LinkedBlockingQueue<String> mDownloadedFiles;        // files downloaded successfully - empty after processed all files
    private AtomicInteger mDownloadedFilesCount;                // total count of sucessfully downloaded files
    private AtomicInteger mThreadCounter;                       // Count active threads so task only finishes once all threads are done
    private final static boolean DEBUG = false;

    @Override
    protected Object doInBackground(Object[] params) {
        mClient = (OwnCloudClient) params[0];
        mHandler = (Handler) params[1];
        mContext = (Context) params[2];
        String mExtFolderAppRoot = (String) params[3];
        mThreadCounter = new AtomicInteger(0);
        mDownloadedFilesCount = new AtomicInteger(0);

        remoteFileList = new ArrayList<>();
        mRemoteFilesToDownloadList = new ArrayList<>();
        mDownloadedFiles = new LinkedBlockingQueue<>();

        mExtFolderDisplayPath = mExtFolderAppRoot + File.separator + "pictures";
        mExtFolderCachePath = mExtFolderAppRoot + File.separator + "cache";

        if (DEBUG) Log.i(TAG, "AppRootFolder: "+ mExtFolderAppRoot +
                "\nCacheFolder: " +mExtFolderCachePath +
                "\nDisplayFoler: " +mExtFolderDisplayPath);
        if (DEBUG) Log.i(TAG, "thread counter at first: " + mThreadCounter.get());

        // read all files on the server
        remoteReadAnyFolder(FileUtils.PATH_SEPARATOR);
        
        // await for all read operations to finish
        while(mThreadCounter.get() > 0) {
            if (isCancelled()) {
                if (DEBUG) Log.e(TAG, "Thread cancelled! -- position: doInB -waited for reads");
                return null;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                if (DEBUG) Log.e(TAG, "Exception happened while trying to await end of remote reads");
                //e.printStackTrace();
            }
        }
        return null;
    }

    private void remoteReadAnyFolder(String path) {
        if (isCancelled()) {
            if (DEBUG) Log.e(TAG, "Thread cancelled! -- position: remoteReadAnyFolder");
            return;
        }
        mThreadCounter.getAndIncrement();
        ReadRemoteFolderOperation refreshOperation = new ReadRemoteFolderOperation(path);
        refreshOperation.execute(mClient, this, mHandler);
    }

    @Override
    public void onRemoteOperationFinish(RemoteOperation operation, RemoteOperationResult result) {
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
        }
    }

    @Override
    public void onTransferProgress(long progressRate, long totalTransferredSoFar,
                                   long totalToTransfer, String fileAbsoluteName) { }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        if (DEBUG) Log.i(TAG, "###### FINISHED OWNCLOUD TASK #####");
    }
    @Override
    protected void onCancelled() {
        handleCancelled();
    }
    @Override
    protected void onCancelled(Object o) {
        handleCancelled();
    }
    private void handleCancelled() {
        if (DEBUG) Log.e(TAG, "Thread 'successfully' cancelled.");
    }
}