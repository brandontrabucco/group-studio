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

import com.brandon.apps.groupstudio.assets.BaseActivity;
import com.brandon.apps.groupstudio.assets.Calculation;
import com.brandon.apps.groupstudio.assets.DatabaseAdapter;
import com.brandon.apps.groupstudio.R;
import com.brandon.apps.groupstudio.assets.ResultCode;
import com.brandon.apps.groupstudio.inflaters.StatisticListInflater;


public class StatisticsMainActivity extends BaseActivity {

    private ListView calculationListView;
    private ImageButton newButton;
    private StatisticListInflater statisticListInflater;
    private int typeId, groupId;
    private String name;
    private Intent intent;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.button_overlay_activity);
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        intent = getIntent();
        name = intent.getStringExtra("name");
        setTitle(name);
        typeId = intent.getIntExtra("typeId",0);
        groupId = intent.getIntExtra("groupId",0);

        calculationListView = (ListView) findViewById(R.id.object_list);
        newButton = (ImageButton) findViewById(R.id.new_button);
        statisticListInflater = new StatisticListInflater(StatisticsMainActivity.this, calculationListView, typeId, groupId);



        database.open();
        if (database.selectUserType() == 0) {
            newButton.setImageResource(R.drawable.ic_action_refresh);
            newButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    statisticListInflater.populateList();
                }
            });
        } else {
            newButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(StatisticsMainActivity.this, CreateCalculationActivity.class);
                    intent.putExtra("id", typeId);
                    startActivityForResult(intent, ResultCode.CREATE_CODE);
                }
            });
        }
        database.close();

        statisticListInflater.populateList();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_CANCELED){
            statisticListInflater.populateList();
            return;
        }

        switch (requestCode){
            case ResultCode.CREATE_CODE:
                Calculation calculation = new Calculation(0, data.getIntExtra("target",0), data.getIntExtra("stat",0), data.getStringExtra("name"));
                database.open();
                long id = database.insertCalculation(typeId, calculation);
                database.close();
                calculation.setId((int) id);
                if (id < 0)
                    Toast.makeText(getBaseContext(), "An error has occurred!", Toast.LENGTH_SHORT).show();
                statisticListInflater.populateList();
                Toast.makeText(getBaseContext(), calculation.getName() + " has been added to your Calculations!", Toast.LENGTH_SHORT).show();
                break;
            case ResultCode.UPDATE_CODE:
                Toast.makeText(getBaseContext(), data.getStringExtra("name") + " has been updated!", Toast.LENGTH_SHORT).show();
                statisticListInflater.populateList();
                break;
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
        } else if (id == R.id.action_rank) {
            Intent intent = new Intent(StatisticsMainActivity.this, StatisticsRankActivity.class);
            intent.putExtra("name", name);
            intent.putExtra("typeId", typeId);
            intent.putExtra("groupId", groupId);
            startActivityForResult(intent, ResultCode.READ_CODE);
        }

        return super.onOptionsItemSelected(item);
    }
}
