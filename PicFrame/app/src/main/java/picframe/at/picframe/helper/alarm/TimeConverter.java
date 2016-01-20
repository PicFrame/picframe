package picframe.at.picframe.helper.alarm;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by linda on 26.12.2015.
 */
public class TimeConverter {
    public TimeConverter(){

    }
    public String millisecondsToDate(long dateInMilliseconds){
        DateFormat formatter = new SimpleDateFormat("DD.MM, HH:mm:ss");
        return formatter.format(new Date(dateInMilliseconds));
    }
}
