package com.brandon.apps.groupstudio.assets;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.brandon.apps.groupstudio.R;

/**
 * Created by Brandon on 12/23/2015.
 */
public class BaseActivity extends ActionBarActivity {
    public DatabaseAdapter database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        database = new DatabaseAdapter(getApplicationContext());
        database.open();
        switch (database.selectUserTheme()) {
            case 0:
                setTheme(R.style.AppTheme);
                break;
            case 1:
                setTheme(R.style.AppTheme_Light);
                break;
            case 2:
                setTheme(R.style.AppTheme_Skye);
                break;
        }
        database.close();
        super.onCreate(savedInstanceState);
    }
}
