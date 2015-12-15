package picframe.at.picframe.downloader;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import picframe.at.picframe.helper.settings.AppData;
import picframe.at.picframe.service_broadcast.DownloadService;
import picframe.at.picframe.Keys;

/**
 * Created by Martin on 05.12.2015.
 */

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {


        System.out.println(" ALARMRECEIVER ");

        if(intent.getAction().equals("ACTION_UPDATE_ALARM")){
            System.out.println(" UPDATE RECEIVED ");
        }

        Intent startDownloadIntent = new Intent(context, DownloadService.class);
        startDownloadIntent.setAction(Keys.ACTION_STARTDOWNLOAD);
        context.startService(startDownloadIntent);

        // SET NEW ALARM
        SharedPreferences prefs = context.getSharedPreferences("PicFrameSettings", Context.MODE_PRIVATE);
        Long time = prefs.getLong("alarmtime", -1);

        // loading settings
        AppData settingsObj = AppData.getINSTANCE();
        Long calendar = new GregorianCalendar().getTimeInMillis();

        if (time + settingsObj.getUpdateIntervalInHours() * 60 * 60 * 1000 < calendar) {
            // Time is small or negativ download after 1 minute
            time = calendar + 1 * 60 * 1000;
        } else {
            time = time + settingsObj.getUpdateIntervalInHours() * 60 * 60 * 1000;
        }

        //testing
        //time = calendar + 1*60*1000;

        // Save starttime of the alarm so we can compare after the app shutsdown if the interval gets switched
        Long startTime = calendar;

        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("alarmtime", startTime);
        editor.commit();


        // Create an Intent and set the class that will execute when the Alarm triggers. Here we have
        // specified AlarmReceiver in the Intent. The onReceive() method of this class will execute when the broadcast from your alarm is received.
        Intent intentAlarm = new Intent(context, AlarmReceiver.class);

        // TODO: Remove (only for testing purposes)
        time = time / 60;

        // Get the Alarm Service.
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Set the alarm for a particular time.
        alarmManager.set(AlarmManager.RTC_WAKEUP, time, PendingIntent.getBroadcast(context, 1, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));

        Date date = new Date(time);
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        String dateFormatted = formatter.format(date);
        System.out.println("Start time: " + formatter.format(calendar) + " " + (calendar));
        System.out.println("Go OFF time: " + formatter.format(time) + " " + time);
    }
}