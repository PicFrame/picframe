package picframe.at.picframe.activities;

import android.app.AlarmManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.GregorianCalendar;

import picframe.at.picframe.R;
import picframe.at.picframe.helper.GlobalPhoneFuncs;
import picframe.at.picframe.helper.alarm.AlarmScheduler;
import picframe.at.picframe.helper.alarm.TimeConverter;
import picframe.at.picframe.settings.AppData;

public class StatusActivity extends ActionBarActivity {

    private Button aboutButton;
    private TextView nbFiles;
    private TextView currentFolder;
    private TextView nextDownload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        nbFiles = (TextView) findViewById(R.id.status_nbFiles);
        currentFolder = (TextView) findViewById(R.id.status_currentFolder);
        nextDownload = (TextView) findViewById(R.id.statusOC_nextAlarm);
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
        if(! (AppData.getSourceType() == AppData.sourceTypes.OwnCloud) )
            return;
        String remoteOCFolder = "";
        String remoteOCFileCount;
        String nextDownload;
        String loginCheckResult;


        Long nextAlarm = AppData.getNextAlarmTime();
        if(nextAlarm == -1 || nextAlarm < new GregorianCalendar().getTimeInMillis()){
            nextDownload = "No download scheduled";
        } else {
            TimeConverter tc = new TimeConverter();
            nextDownload = tc.millisecondsToDate(nextAlarm);
        }
        this.nextDownload.setText(nextDownload);

    }

}
