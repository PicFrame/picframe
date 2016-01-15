package picframe.at.picframe.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import picframe.at.picframe.R;

public class StatusActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
