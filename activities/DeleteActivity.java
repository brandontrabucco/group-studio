package com.brandon.apps.groupstudio.activities;

/**
 * Created by Brandon on 4/19/2015.
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.brandon.apps.groupstudio.R;
import com.brandon.apps.groupstudio.assets.BaseActivity;

public class DeleteActivity extends BaseActivity {

    private TextView display;
    private Intent data;
    private int focusId;
    private String name;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete);
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        data = getIntent();
        focusId = data.getIntExtra("id", 0);
        name = data.getStringExtra("name");

        display = (TextView) findViewById(R.id.delete_text);
        display.setText(name);
        final Button confirm = (Button) findViewById(R.id.confirm_delete);
        final Button cancel = (Button) findViewById(R.id.cancel_delete);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("id", focusId);
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
        getMenuInflater().inflate(R.menu.menu_blank, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sync) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
