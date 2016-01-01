package picframe.at.picframe.downloader;

import picframe.at.picframe.service_broadcast.ServiceCallbacks;

public abstract class Downloader extends Thread {
    protected ServiceCallbacks serviceCallbacks;
    public enum Failure {
        LOGIN, WIFI, INTERRUPT, TIMEOUT, STORAGESPACE;
        private static Failure[] allValues = values();
        public static Failure getStatesForInt(int num) {
            try{
                return allValues[num];
            } catch(ArrayIndexOutOfBoundsException e) {
                return LOGIN;
            }
        }
    }
}
