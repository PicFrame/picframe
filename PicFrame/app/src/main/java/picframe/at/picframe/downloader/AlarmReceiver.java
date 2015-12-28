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
import picframe.at.picframe.helper.alarm.AlarmScheduler;
import picframe.at.picframe.helper.alarm.TimeConverter;
import picframe.at.picframe.helper.settings.AppData;
import picframe.at.picframe.service_broadcast.DownloadService;
import picframe.at.picframe.Keys;
import android.util.Log;


/**
 * Created by Martin on 05.12.2015.
 */

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = AlarmReceiver.class.getSimpleName();
    private static AppData settingsObj = AppData.getINSTANCE();

    TimeConverter tc;

    @Override
    public void onReceive(Context context, Intent intent) {

        tc = new TimeConverter();

        System.out.println(" ALARMRECEIVER ");

//        Long scheduledAlarm = lastAlarmTime+settingsObj.getUpdateIntervalInHours();
        Long currentTime = new GregorianCalendar().getTimeInMillis();

        Intent startDownloadIntent = new Intent(context, DownloadService.class);
        startDownloadIntent.setAction(Keys.ACTION_STARTDOWNLOAD);
        context.startService(startDownloadIntent);

        settingsObj.setLastAlarmTime(currentTime);

        Log.d(TAG, "Current Time    : " + tc.millisecondsToDate(currentTime));
        AlarmScheduler alarmScheduler = new AlarmScheduler();
        Long nextAlarm = alarmScheduler.scheduleAlarm();
        Log.d(TAG, "Next Alarm      : "+tc.millisecondsToDate(nextAlarm));
        Log.d(TAG,"Start time: " + tc.millisecondsToDate(currentTime) + " " + (currentTime));
        Log.d(TAG, "Go OFF time: " + tc.millisecondsToDate(nextAlarm) + " " + nextAlarm);
    }
}