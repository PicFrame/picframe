package picframe.at.picframe.helper.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.GregorianCalendar;

import picframe.at.picframe.activities._MainApp;
import picframe.at.picframe.activities.MainActivity;
import picframe.at.picframe.settings.AppData;

/**
 * Created by linda on 28.12.2015.
 */
public class AlarmScheduler {
    private static final String TAG = AlarmScheduler.class.getSimpleName();
    private static AlarmManager alarmManager;
    private String nextAlarmAsDate;
    Long nextAlarmTime;

    public AlarmScheduler(){
        if(alarmManager == null)
            alarmManager = (AlarmManager) _MainApp.getINSTANCE().getApplicationContext().getSystemService(Context.ALARM_SERVICE);
    }

    public Long scheduleAlarm(){

        deleteAlarm();
        if(AppData.getSourceType() != AppData.sourceTypes.OwnCloud
                || AppData.getUpdateIntervalInHours() == -1)
        {
            Log.d(TAG,"NExt alarm: -1");
            AppData.setNextAlarmTime(-1L);
            return -1L;
        } else if (!AppData.getLoginSuccessful()){
            return -1L;
        }


        TimeConverter tc = new TimeConverter();

        Long nextAlarmTime;
        Long currentTime = new GregorianCalendar().getTimeInMillis();
        nextAlarmTime = AppData.getLastAlarmTime() + AppData.getUpdateIntervalInHours() * 1000 * 60 * 60;

        Log.d(TAG, "currentTime    : "+tc.millisecondsToDate(currentTime));
        Log.d(TAG, "previousAlarm  : "+tc.millisecondsToDate(AppData.getLastAlarmTime()));
        Log.d(TAG, "Update Interval: "+String.valueOf(AppData.getUpdateIntervalInHours()));
        Log.d(TAG, "nextAlarm      : " + tc.millisecondsToDate(nextAlarmTime));

        // If no alarm is currently scheduled, or if the time for the next scheduled alarm is passed,
        // download immediately
        if(AppData.getNextAlarmTime() == -1 || nextAlarmTime < currentTime){
            Log.d(TAG, tc.millisecondsToDate(nextAlarmTime)+" < "+tc.millisecondsToDate(currentTime));
            Log.d(TAG, "previously scheduled alarm is in the past; start new alarm in 1 minute");
            nextAlarmTime = currentTime + 2 * 1000 * 60;
        } else {
            Log.d(TAG, tc.millisecondsToDate(nextAlarmTime)+" >= "+tc.millisecondsToDate(currentTime));
        }

        setAlarm(nextAlarmTime);
        AppData.setNextAlarmTime(nextAlarmTime);
        nextAlarmAsDate = tc.millisecondsToDate(nextAlarmTime);
        System.out.println("Start time: " + tc.millisecondsToDate(currentTime) + " " + (currentTime));
        System.out.println("Go OFF time: " + tc.millisecondsToDate(nextAlarmTime) + " " + nextAlarmTime);
        return nextAlarmTime;
    }

    private void setAlarm (Long nextAlarmTime){
        Intent intent = new Intent(MainActivity.getContext(),AlarmReceiver.class);
//        intent.setAction("ACTION_UPDATE_ALARM");
        alarmManager.set(AlarmManager.RTC_WAKEUP, nextAlarmTime, PendingIntent.getBroadcast(MainActivity.getContext(), 1, intent, PendingIntent.FLAG_UPDATE_CURRENT));
    }

    private void deleteAlarm(){
        Log.d(TAG, " DELETE ALARMS ");
        Intent i = new Intent(MainActivity.getContext(),AlarmReceiver.class);
        PendingIntent p = PendingIntent.getBroadcast(MainActivity.getContext(), 1, i, 0);
        alarmManager.cancel(p);
        p.cancel();
    }

    public String getNextAlarmAsDate(){
        TimeConverter tc = new TimeConverter();
        return tc.millisecondsToDate(AppData.getNextAlarmTime());
    }

}
