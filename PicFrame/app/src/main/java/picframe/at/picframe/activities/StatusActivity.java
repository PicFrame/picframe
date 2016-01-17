package picframe.at.picframe.activities;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.GregorianCalendar;
import java.util.logging.Handler;

import picframe.at.picframe.R;
import picframe.at.picframe.helper.GlobalPhoneFuncs;
import picframe.at.picframe.helper.alarm.AlarmScheduler;
import picframe.at.picframe.helper.alarm.TimeConverter;
import picframe.at.picframe.service.connectionChecker.ConnectionCheck_OC;
import picframe.at.picframe.service.folderChecker.FolderReaderService;
import picframe.at.picframe.service.folderChecker.RemoteFolderListDownloader_OC;
import picframe.at.picframe.settings.AppData;

public class StatusActivity extends ActionBarActivity {

    private Button aboutButton;
    private TextView nbFiles;
    private TextView currentFolder;
    private TextView nextDownload;
    private TextView nbRemoteOCFiles;
    private TextView lastLoginCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        nbFiles = (TextView) findViewById(R.id.status_nbFiles);
        currentFolder = (TextView) findViewById(R.id.status_currentFolder);
        nextDownload = (TextView) findViewById(R.id.statusOC_nextAlarm);
//        nbRemoteOCFiles= (TextView) findViewById(R.id.statusOC_nbRomoteFiles);
        lastLoginCheck = (TextView) findViewById(R.id.statusOC_loginCheck);
        aboutButton = (Button) findViewById(R.id.status_buttonAbout);
        aboutButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AboutActivity.class);
                startActivity(intent);
            }
        });


    }

    protected void onResume(){
        super.onResume();
        setLocalFolderAndFileCount();
        setOwnCloudStatus();

    }

    private void setLocalFolderAndFileCount(){
        String localFolder = AppData.getImagePath();
        String localFileCount;
        if(localFolder.equals("")){
            localFolder = "No path set";
            localFileCount = "-";
        } else {
            localFileCount = String.valueOf(GlobalPhoneFuncs.getFileList(localFolder).size());
        }
        currentFolder.setText(localFolder);
        nbFiles.setText(localFileCount);
    }

    private void setOwnCloudStatus(){
//        String remoteOCFolder = "";
//        String remoteOCFileCount = "-";
        String nextDownload = getString(R.string.status_downloadIntervalNoDownload);
        String loginCheckResult = "-";

        if((AppData.getSourceType() == AppData.sourceTypes.OwnCloud) ) {

            Long nextAlarm = AppData.getNextAlarmTime();
            Log.d("nextAlarm", String.valueOf(nextAlarm));
            if (nextAlarm != -1 && nextAlarm > new GregorianCalendar().getTimeInMillis()) {
                TimeConverter tc = new TimeConverter();
                nextDownload = tc.millisecondsToDate(nextAlarm);
            }

            loginCheckResult = (AppData.getLoginSuccessful()) ? "Successful" : "Failed";
//            android.os.Handler handler = new android.os.Handler();
//            handler.post(new RemoteFolderListDownloader_OC(handler))
        }
        this.nextDownload.setText(nextDownload);
//        this.nbRemoteOCFiles.setText(remoteOCFileCount);
        this.lastLoginCheck.setText(loginCheckResult);
    }

}
