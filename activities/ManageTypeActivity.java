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
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.brandon.apps.groupstudio.assets.BaseActivity;
import com.brandon.apps.groupstudio.assets.DatabaseAdapter;
import com.brandon.apps.groupstudio.assets.DefaultType;
import com.brandon.apps.groupstudio.R;
import com.brandon.apps.groupstudio.assets.ResultCode;
import com.brandon.apps.groupstudio.inflaters.TypeListInflater;

public class ManageTypeActivity extends BaseActivity {

    private ListView typeListView;
    private TypeListInflater typeListInflater;
    private Intent intent;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.button_overlay_activity);
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        intent = getIntent();
        setTitle("Manage Types");

        typeListView = (ListView) findViewById(R.id.object_list);
        typeListInflater = new TypeListInflater(ManageTypeActivity.this, typeListView);

        final ImageButton newTypeButton = (ImageButton) findViewById(R.id.new_button);
        newTypeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManageTypeActivity.this, CreateTypeActivity.class);
                startActivityForResult(intent, ResultCode.CREATE_CODE);
            }
        });

        typeListInflater.populateList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_back, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_CANCELED){
            typeListInflater.populateList();
            return;
        }


        if (requestCode == ResultCode.DELETE_CODE) {
            // delete the type with the returned ID from list and database
            database.open();
            database.deleteTypeById(data.getIntExtra("id", 0));
            database.close();
            Toast.makeText(getBaseContext(), "Attempting to delete!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        }
        else if (requestCode == ResultCode.CREATE_CODE) {
            DefaultType type = new DefaultType(0, data.getStringExtra("name"));
            database.open();
            long id = database.insertType(type);
            database.close();
            type.setId((int)id);
            if (id < 0)
                Toast.makeText(getBaseContext(), "An error has occurred!", Toast.LENGTH_SHORT).show();
            typeListInflater.populateList();
            Toast.makeText(getBaseContext(), type.getName() + " has been added to your Types!", Toast.LENGTH_SHORT).show();
        }
        else if (requestCode == ResultCode.READ_CODE) {
            typeListInflater.populateList();
        }
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
