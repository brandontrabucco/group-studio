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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.brandon.apps.groupstudio.assets.BaseActivity;
import com.brandon.apps.groupstudio.assets.DatabaseAdapter;
import com.brandon.apps.groupstudio.assets.DefaultMember;
import com.brandon.apps.groupstudio.R;

public class EditMemberActivity extends BaseActivity {
    EditText name;
    Spinner spinner;
    Intent intent;
    int parentId, focusId, oldType, selection;
    String oldName;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_params);
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        intent = getIntent();
        parentId = intent.getIntExtra("parent", 1);
        focusId = intent.getIntExtra("id", 1);
        oldName = intent.getStringExtra("name");
        oldType = intent.getIntExtra("type", 0);

        final Button confirm = (Button) findViewById(R.id.confirm_new_member);
        final Button cancel = (Button) findViewById(R.id.cancel_new_member);

        name = (EditText) findViewById(R.id.enter_member_name);
        name.setText(oldName);

        spinner = (Spinner) findViewById(R.id.type_spinner);

        String[] typeList = DefaultMember.getTypeList(getApplicationContext());

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(EditMemberActivity.this, R.layout.spinner_layout, typeList);
        adapter.setDropDownViewResource(R.layout.spinner_item_layout);
        spinner.setAdapter(adapter);

        // problem line
        database.open();
        spinner.setSelection(database.getTypePositionById(oldType) + 1);
        database.close();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    database.open();
                    selection = database.getAllTypes().get(position - 1).getId();
                    database.close();
                } else {
                    selection = 0;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                database.open();
                if (name.getText().toString().trim().isEmpty()){
                    Intent intent = new Intent();
                    database.updateMemberById(parentId, focusId, oldName, selection);
                    intent.putExtra("name", oldName);
                    setResult(Activity.RESULT_OK, intent);
                    database.close();
                    finish();
                } else {
                    Intent intent = new Intent();
                    database.updateMemberById(parentId, focusId, name.getText().toString(), selection);
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
