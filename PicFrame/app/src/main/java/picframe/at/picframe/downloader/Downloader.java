package picframe.at.picframe.downloader;

import picframe.at.picframe.service_broadcast.ServiceCallbacks;

public abstract class Downloader extends Thread {
    protected ServiceCallbacks serviceCallbacks;
}
