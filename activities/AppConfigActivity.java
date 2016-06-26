package com.brandon.apps.groupstudio.activities;

/**
 * Created by Brandon on 4/19/2015.
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.brandon.apps.groupstudio.R;
import com.brandon.apps.groupstudio.assets.BaseActivity;
import com.brandon.apps.groupstudio.assets.DatabaseAdapter;

public class AppConfigActivity extends BaseActivity {
    private int userSelection, themeSelection;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_params);
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        setTitle("App Settings");

        final Button confirm = (Button) findViewById(R.id.confirm_new_group);
        final Button cancel = (Button) findViewById(R.id.cancel_new_group);

        database.open();
        userSelection = database.selectUserType();
        themeSelection = database.selectUserTheme();
        database.close();

        final RadioGroup typeGroup = (RadioGroup) findViewById(R.id.user_type_group);
        final RadioGroup themeGroup = (RadioGroup) findViewById(R.id.app_theme_group);

        typeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.user_type_basic:
                        userSelection = 0;
                        break;
                    case R.id.user_type_expert:
                        userSelection = 1;
                        break;
                }
            }
        });

        if (userSelection == 0) {
            typeGroup.check(R.id.user_type_basic);
        } else if (userSelection == 1) {
            typeGroup.check(R.id.user_type_expert);
        }

        themeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.app_theme_dark:
                        themeSelection = 0;
                        break;
                    case R.id.app_theme_light:
                        themeSelection = 1;
                        break;
                    case R.id.app_theme_skye:
                        themeSelection = 2;
                        break;
                }
            }
        });

        if (themeSelection == 0) {
            themeGroup.check(R.id.app_theme_dark);
        } else if (themeSelection == 1) {
            themeGroup.check(R.id.app_theme_light);
        } else if (themeSelection == 2) {
            themeGroup.check(R.id.app_theme_skye);
        }

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                database.open();
                database.updateUser(userSelection, themeSelection);
                database.close();
                Intent intent = new Intent();
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(Activity.RESULT_CANCELED, intent);
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_back, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_back) {
            Intent intent = new Intent();
            setResult(RESULT_CANCELED, intent);
            finish();
            return true;
        }

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }
}
