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
import android.widget.EditText;
import android.widget.Toast;

import com.brandon.apps.groupstudio.R;
import com.brandon.apps.groupstudio.assets.BaseActivity;

public class CreateGroupActivity extends BaseActivity {
    EditText name, desc;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_params);
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        final Button confirm = (Button) findViewById(R.id.confirm_new_group);
        final Button cancel = (Button) findViewById(R.id.cancel_new_group);

        name = (EditText) findViewById(R.id.enter_group_name);
        desc = (EditText) findViewById(R.id.enter_group_desc);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (name.getText().toString().trim().isEmpty() || desc.getText().toString().trim().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Enter a Valid Name and Description!", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent();
                    intent.putExtra("name", name.getText().toString());
                    intent.putExtra("desc", desc.getText().toString());
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
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

        return super.onOptionsItemSelected(item);
    }
}
