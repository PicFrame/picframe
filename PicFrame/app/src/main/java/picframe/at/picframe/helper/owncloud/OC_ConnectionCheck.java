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

package picframe.at.picframe.helper.owncloud;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.network.OnDatatransferProgressListener;
import com.owncloud.android.lib.common.operations.OnRemoteOperationListener;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.FileUtils;
import com.owncloud.android.lib.resources.files.ReadRemoteFolderOperation;

import java.util.concurrent.atomic.AtomicInteger;

import picframe.at.picframe.R;
import picframe.at.picframe.activities.MainActivity;

public class OC_ConnectionCheck extends AsyncTask<Object, Float, Object>
        implements OnRemoteOperationListener, OnDatatransferProgressListener {

    private final String TAG = this.getClass().getSimpleName();
    private final static boolean DEBUG = false;
    private Context mContext;
    private AtomicInteger mThreadCounter;   // Count active threads so task only finishes once all threads are done

    @Override
    protected Object doInBackground(Object[] params) {
        OwnCloudClient mClient = (OwnCloudClient) params[0];
        Handler mHandler = (Handler) params[1];
        mContext = (Context) params[2];

        mThreadCounter = new AtomicInteger(0);
        if (DEBUG) Log.i(TAG, "###### STARTING OWNCLOUD CONNECTION CHECK #####");
        publishProgress();
        // read root on the server
        mThreadCounter.getAndIncrement();
        ReadRemoteFolderOperation refreshOperation = new ReadRemoteFolderOperation(FileUtils.PATH_SEPARATOR);
        refreshOperation.execute(mClient, this, mHandler);

        // await for all read operations to finish
        while(mThreadCounter.get() > 0) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                if (DEBUG) Log.e(TAG, "Exception happened while trying to await end of remote reads");
                //e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Float... values) {
        super.onProgressUpdate(values);
    //    MainActivity.updateDownloadProgress(50f, true); TODO
    }

    @Override
    public void onRemoteOperationFinish(RemoteOperation operation, RemoteOperationResult result) {
        if (operation instanceof ReadRemoteFolderOperation) {
            if (result.isSuccess()) {
                if (DEBUG) Log.i(TAG, "Could connect to owncloud: " + result.getLogMessage());
                //MainActivity.mConnCheckOC = true;
            } else {
                if (DEBUG) Log.i(TAG, "Could NOT connect to owncloud: " + result.getLogMessage());
                //MainActivity.mConnCheckOC = false;
            }
            mThreadCounter.getAndDecrement();
        }
    }

    @Override
    protected void onPostExecute(Object o) {
        //MainActivity.updateDownloadProgress(0f, false);
/*        if (MainActivity.mConnCheckOC) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    MainActivity.startFileDownload();
                }
            }).run();
        } else {
            Toast.makeText(mContext, R.string.octask_toast_loginFailed, Toast.LENGTH_LONG).show();
        }*/
        if (DEBUG) Log.i(TAG, "###### FINISHED OWNCLOUD CONNECTION CHECK #####");
    }

    @Override
    protected void onPreExecute() {
        //MainActivity.updateDownloadProgress(50f, true);
    }

    @Override
    public void onTransferProgress(long rate, long soFar, long total, String fileAbsoluteName) { }
}