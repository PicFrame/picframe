package picframe.at.picframe.service_broadcast.connectionChecker;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.common.OwnCloudCredentialsFactory;
import com.owncloud.android.lib.common.operations.OnRemoteOperationListener;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.FileUtils;
import com.owncloud.android.lib.resources.files.ReadRemoteFolderOperation;

import picframe.at.picframe.Keys;
import picframe.at.picframe.MainApp;
import picframe.at.picframe.R;
import picframe.at.picframe.helper.settings.AppData;
import picframe.at.picframe.helper.settings.SettingsDefaults;

public class ConnectionCheck_OC implements Runnable {
    private final String TAG = ConnectionCheck_OC.class.getSimpleName();

    @Override
    public void run() {
        new ConnectionChecker(new Handler()).start();
    }

    private class ConnectionChecker extends Thread implements OnRemoteOperationListener {
        private boolean loginSuccess = false;
        private boolean taskDone = false;
        public OwnCloudClient mClient;
        public Uri serverUri;
        private Handler mHandler;
        private final String mPath = FileUtils.PATH_SEPARATOR;

        public ConnectionChecker(Handler handler) {
            mHandler = handler;
        }

        private void dataDump() {
            Log.i(TAG, "dump:\n" +
                    "#srcPath:" + AppData.getSourcePath() + "\n" +
                    "#uri:" + serverUri.toString() + "\n" +
                    "#username:" + AppData.getUserName() + "\n" +
                    "#password:" + AppData.getUserPassword() + "\n");
        }

        private void buildClient() {
            serverUri = Uri.parse(AppData.getSourcePath());
            mClient = OwnCloudClientFactory
                    .createOwnCloudClient(
                            serverUri,
                            MainApp.getINSTANCE().getApplicationContext(),
                            true);
            mClient.setCredentials(
                    OwnCloudCredentialsFactory.newBasicCredentials(
                            AppData.getUserName(),
                            AppData.getUserPassword()
                    ));
        }

        @Override
        public void run() {
            if ( AppData.getSourcePath() == null || AppData.getSourcePath().equals("") ||
                    AppData.getSourcePath().equals(SettingsDefaults.
                            getDefaultValueForKey(R.string.sett_key_srcpath_owncloud)) ||
                    AppData.getUserName().equals("") || AppData.getUserPassword().equals("")) {
                Log.d(TAG, "received invalid data");
            } else {
                buildClient();
                dataDump();
                if (mClient != null) {
                    Log.d(TAG, "data not invalid, checking login");
                    ReadRemoteFolderOperation loginCheckOperation = new ReadRemoteFolderOperation(mPath);
                    loginCheckOperation.execute(mClient, this, mHandler);
                    while (!taskDone) {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    Log.d(TAG, "client is null!");
                }
            }
            sendBroadcast();
        }

        @Override
        public void onRemoteOperationFinish(RemoteOperation caller, RemoteOperationResult result) {
            if (caller instanceof ReadRemoteFolderOperation) {
                if (result.isSuccess()) {
                    loginSuccess = true;
                }
                taskDone = true;
            }
        }

        protected void sendBroadcast() {
            String status;
            if (loginSuccess) {
                Log.d(TAG, "login successful");
                status = Keys.ACTION_LOGINSTATUSSUCCESS;
            } else {
                Log.d(TAG, "login failure");
                status = Keys.ACTION_LOGINSTATUSFAILURE;
            }
            LocalBroadcastManager
                    .getInstance(MainApp.getINSTANCE().getApplicationContext())
                    .sendBroadcast(new Intent().setAction(status));
        }
    }
}
