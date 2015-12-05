package picframe.at.picframe.downloader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import picframe.at.picframe.service_broadcast.DownloadService;
import picframe.at.picframe.service_broadcast.Keys;

/**
 * Created by Martin on 05.12.2015.
 */

public class AlarmReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {

        Intent startDownloadIntent = new Intent(context, DownloadService.class);
        startDownloadIntent.setAction(Keys.ACTION_STARTDOWNLOAD);
        context.startService(startDownloadIntent);


    }
}