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

import com.brandon.apps.groupstudio.assets.BaseActivity;
import com.brandon.apps.groupstudio.assets.DatabaseAdapter;
import com.brandon.apps.groupstudio.R;
import com.brandon.apps.groupstudio.inflaters.RankListInflater;


public class StatisticsRankActivity extends BaseActivity {

    private ListView rankListView;
    private ImageButton newButton;
    private RankListInflater rankListInflater;
    private int typeId, groupId;
    private Intent intent;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.refresh_overlay_activity);
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        intent = getIntent();
        String name = intent.getStringExtra("name");
        setTitle(name);
        typeId = intent.getIntExtra("typeId",0);
        groupId = intent.getIntExtra("groupId",0);

        rankListView = (ListView) findViewById(R.id.object_list);
        newButton = (ImageButton) findViewById(R.id.new_button);
        rankListInflater = new RankListInflater(StatisticsRankActivity.this, rankListView, typeId, groupId);

        newButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rankListInflater.populateList();
            }
        });

        rankListInflater.populateList();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_CANCELED){
            rankListInflater.populateList();
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
            Intent intent = new Intent();
            setResult(RESULT_CANCELED, intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
