package picframe.at.picframe.service_broadcast;

public class Keys {

    /* Keys for the download-parameters */
    public static final String PICFRAMEPATH = "picframeRootPath";
    public static final String CALLBACK = "callback";
    public static final String CONTEXT = "context";

    public static final int NOTIFICATION_ID = 654321;
    public enum NotificationStates {
        START, PROGRESS, FINISHED, STOP, INTERRUPT, FAILURE ;
        private static NotificationStates[] allValues = values();
        public static NotificationStates getStatesForInt(int num){
            try{
                return allValues[num];
            }catch(ArrayIndexOutOfBoundsException e){
                return STOP;
            }
        }
    }

    /* Keys for the actions and messages */
    // FROM ACTIVITY TO SERVICE(started with startService(intent) )
    public static final String ACTION_STARTDOWNLOAD = "picframe.at.picframe.service.STARTDOWNLOAD";
            // FROM SERVICE TO RECEIVER (started with .sendBroadcast(intent) )
            //public static final String ACTION_PROGRESSUPDATE = "picframe.at.picframe.service.PROGRESSUPDATE";
            // FROM SERVICE TO RECEIVER
            //public static final String ACTION_FAILURE = "picframe.at.picframe.service.ERROR";
    // FROM NOTIFICATION TO SERVICE (to stop download and service)
    public static final String ACTION_STOPDOWNLOAD = "picframe.at.picframe.service.STOPDOWNLOAD";
    // FROM SERVICE TO RECEIVER
    public static final String ACTION_DOWNLOAD_FINISHED = "picframe.at.picframe.service.DOWNLOAD_FINISHED";
    public static final String MSG_PROGRESSUPDATE_PERCENT = "progressUpdatePercent";
    public static final String MSG_PROGRESSUPDATE_INDITERMINATE = "progressUpdateInditerminate";
    public static final String MSG_FAILURE = "failure";
}
