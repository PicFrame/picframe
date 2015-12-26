/*
   Copyright (C) 2015 Martin Bayerl, Christoph Krasa, Linda Spindler, Clemens Hlawacek

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

import picframe.at.picframe.activities.MainActivity;
import picframe.at.picframe.helper.TimeConverter;
import picframe.at.picframe.helper.settings.AppData;
import picframe.at.picframe.service_broadcast.DownloadService;
import picframe.at.picframe.Keys;

/**
 * Created by Martin on 05.12.2015.
 */

public class AlarmReceiver extends BroadcastReceiver {

    TimeConverter tc;

    @Override
    public void onReceive(Context context, Intent intent) {

        tc = new TimeConverter();

        System.out.println(" ALARMRECEIVER ");

        // load settings
        AppData settingsObj = AppData.getINSTANCE();

        Intent startDownloadIntent = new Intent(context, DownloadService.class);
        startDownloadIntent.setAction(Keys.ACTION_STARTDOWNLOAD);
        context.startService(startDownloadIntent);

        // Set time for next alarm
        Long alarmTime = settingsObj.getLastAlarmTime();
        System.out.println(tc.millisecondsToDate(alarmTime));

        // loading settings
        Long currentTime = new GregorianCalendar().getTimeInMillis();

        if (alarmTime + tc.hoursToMilliseconds(settingsObj.getUpdateIntervalInHours()) < currentTime) {
            // Time is small or negativ download after 1 minute
            alarmTime = currentTime + tc.minutesToMilliseconds(1);
        } else {
            alarmTime = alarmTime + tc.hoursToMilliseconds(settingsObj.getUpdateIntervalInHours());
        }

        System.out.println(tc.millisecondsToDate(currentTime));
        System.out.println(tc.millisecondsToDate(alarmTime));

        //testing
        //time = calendar + 1*60*1000;

        // Save starttime of the alarm so we can compare after the app shutsdown if the interval gets switched
        settingsObj.setLastAlarmTime(currentTime);

        // Create an Intent and set the class that will execute when the Alarm triggers. Here we have
        // specified AlarmReceiver in the Intent. The onReceive() method of this class will execute when the broadcast from your alarm is received.
        Intent intentAlarm = new Intent(context, AlarmReceiver.class);

        // Get the Alarm Service.
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Set the alarm for a particular time.
        alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, PendingIntent.getBroadcast(context, 1, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));

        System.out.println("Start time: " + tc.millisecondsToDate(currentTime) + " " + (currentTime));
        System.out.println("Go OFF time: " + tc.millisecondsToDate(alarmTime) + " " + alarmTime);
    }
}