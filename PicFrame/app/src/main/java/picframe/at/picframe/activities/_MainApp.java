package picframe.at.picframe.activities;

import android.app.Application;

/**
 * Created by MrAdmin on 24.05.2015.
 * Crash-report per mail to teampicframe@gmail.com
 * ACRA
 */
/*@ReportsCrashes(
        mailTo = "teampicframe@gmail.com",
        customReportContent = {
                ReportField.BRAND,
                ReportField.PHONE_MODEL,
                ReportField.ANDROID_VERSION,
                ReportField.APP_VERSION_CODE,
                ReportField.CUSTOM_DATA,
                ReportField.STACK_TRACE
        },
        //logcatArguments = { "-t", "200", "-v", "long","test:I" ,"*:D","*:S"}
//        ,mode = ReportingInteractionMode.SILENT,
        reportType= HttpSender.Type.JSON
)*/
public class _MainApp extends Application {
    private static _MainApp INSTANCE = null;

    @Override
    public void onCreate() {
        super.onCreate();
        if (INSTANCE == null) {
            INSTANCE = this;
        }
    //    ACRA.init(this);
    }

    public static _MainApp getINSTANCE() {
        return INSTANCE;
    }
}