package com.brandon.apps.groupstudio.activities;

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

import com.brandon.apps.groupstudio.R;
import com.brandon.apps.groupstudio.assets.BaseActivity;
import com.brandon.apps.groupstudio.assets.DatabaseAdapter;
import com.brandon.apps.groupstudio.assets.DefaultGroup;
import com.brandon.apps.groupstudio.assets.ResultCode;
import com.brandon.apps.groupstudio.inflaters.CloudGroupListInflater;
import com.brandon.apps.groupstudio.inflaters.GroupListInflater;


public class CloudMainActivity extends BaseActivity {

    private ListView groupListView;
    private CloudGroupListInflater groupListInflater;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.refresh_overlay_activity);

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        groupListView = (ListView) findViewById(R.id.object_list);
        groupListInflater = new CloudGroupListInflater(CloudMainActivity.this, groupListView);

        final ImageButton refreshButton = (ImageButton) findViewById(R.id.new_button);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groupListInflater.populateList();
            }
        });

        groupListInflater.populateList();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_CANCELED){
            groupListInflater.populateList();
            return;
        }
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_back) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public android.support.v4.app.FragmentManager getSupportFragmentManager() {
        return null;
    }
}
