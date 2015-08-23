package picframe.at.picframe.service_broadcast;


public abstract interface ServiceCallbacks {
    void publishProgress(float f, boolean b);
    void interruptedDownload();
    void finishedDownload(int count);
}
