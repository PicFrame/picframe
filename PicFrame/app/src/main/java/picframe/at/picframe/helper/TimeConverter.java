package picframe.at.picframe.helper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by linda on 26.12.2015.
 */
public class TimeConverter {
    public TimeConverter(){

    }

    public long hoursToMilliseconds(int hours){
        return hours * 60 * 60 * 1000;
    }

    public long minutesToMilliseconds (int minutes){
        return minutes * 60 * 1000;
    }

    public String millisecondsToDate(long dateInMilliseconds){
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        return formatter.format(new Date(dateInMilliseconds));
    }
}
