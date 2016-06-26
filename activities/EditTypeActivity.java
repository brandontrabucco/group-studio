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

import com.brandon.apps.groupstudio.assets.BaseActivity;
import com.brandon.apps.groupstudio.assets.DatabaseAdapter;
import com.brandon.apps.groupstudio.R;

public class EditTypeActivity extends BaseActivity {
    EditText name;
    Intent intent;
    int typeId;
    String oldName;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type_params);
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        intent = getIntent();
        typeId = intent.getIntExtra("id", 1);
        oldName = intent.getStringExtra("name");

        final Button confirm = (Button) findViewById(R.id.confirm_new_type);
        final Button cancel = (Button) findViewById(R.id.cancel_new_type);

        name = (EditText) findViewById(R.id.enter_type_name);
        name.setText(oldName);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (name.getText().toString().trim().isEmpty()) {
                    Intent intent = new Intent();
                    intent.putExtra("name", oldName);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                } else {
                    Intent intent = new Intent();
                    database.open();
                    database.updateTypeById(typeId, name.getText().toString());
                    intent.putExtra("name", name.getText().toString());
                    setResult(Activity.RESULT_OK, intent);
                    database.close();
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
