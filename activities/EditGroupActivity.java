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

import com.brandon.apps.groupstudio.assets.BaseActivity;
import com.brandon.apps.groupstudio.assets.DatabaseAdapter;
import com.brandon.apps.groupstudio.R;

public class EditGroupActivity extends BaseActivity {
    EditText name, desc;
    Intent intent;
    int focusId;
    String oldName, oldDesc;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_params);
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        intent = getIntent();
        focusId = intent.getIntExtra("id", 1);
        oldName = intent.getStringExtra("name");
        oldDesc = intent.getStringExtra("desc");

        final Button confirm = (Button) findViewById(R.id.confirm_new_group);
        final Button cancel = (Button) findViewById(R.id.cancel_new_group);

        database.open();

        name = (EditText) findViewById(R.id.enter_group_name);
        name.setText(database.selectGroupById(focusId).getName());

        desc = (EditText) findViewById(R.id.enter_group_desc);
        desc.setText(database.selectGroupById(focusId).getDesc());

        database.close();

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                database.open();
                if (name.getText().toString().trim().isEmpty() && desc.getText().toString().trim().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Enter a Name or Description!", Toast.LENGTH_SHORT).show();
                } else if (name.getText().toString().trim().isEmpty() && !desc.getText().toString().trim().isEmpty()){
                    Intent intent = new Intent();
                    database.updateGroupById(focusId, oldName, desc.getText().toString());
                    intent.putExtra("name", database.selectGroupById(focusId).getName());
                    setResult(Activity.RESULT_OK, intent);
                    database.close();
                    finish();
                } else if (!name.getText().toString().trim().isEmpty() && desc.getText().toString().trim().isEmpty()){
                    Intent intent = new Intent();
                    database.updateGroupById(focusId, name.getText().toString(), oldDesc);
                    intent.putExtra("name", database.selectGroupById(focusId).getName());
                    setResult(Activity.RESULT_OK, intent);
                    database.close();
                    finish();
                } else {
                    Intent intent = new Intent();
                    database.updateGroupById(focusId, name.getText().toString(), desc.getText().toString());
                    intent.putExtra("name", database.selectGroupById(focusId).getName());
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
