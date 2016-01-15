package picframe.at.picframe.service;


import android.app.IntentService;
import android.content.Intent;

public class ConnectionCheckService extends IntentService {

    public ConnectionCheckService() {
        super(ConnectionCheckService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
