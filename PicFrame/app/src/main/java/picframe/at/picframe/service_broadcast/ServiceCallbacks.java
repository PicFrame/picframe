package picframe.at.picframe.service_broadcast;


import picframe.at.picframe.downloader.Downloader;

public abstract interface ServiceCallbacks {
    void publishProgress(float progressPercent, boolean progressIndeterminate); // TODO add filecount
    void finishedDownload(int count);
    void downloadFailed(Downloader.Failure failureMsg);
}
