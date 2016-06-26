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
import android.widget.Toast;

import com.brandon.apps.groupstudio.assets.BaseActivity;
import com.brandon.apps.groupstudio.assets.DatabaseAdapter;
import com.brandon.apps.groupstudio.assets.DefaultType;
import com.brandon.apps.groupstudio.R;
import com.brandon.apps.groupstudio.assets.StatisticMath;

public class EditCalculationActivity extends BaseActivity {
    EditText name;
    Spinner targetSpinner, statSpinner;
    Intent intent;
    int typeId, id, oldTarget, oldStat, targetSelection, statSelection;
    String oldName;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculation_params);
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        intent = getIntent();
        typeId = intent.getIntExtra("typeId", 0);
        id = intent.getIntExtra("id", 0);
        oldName = intent.getStringExtra("name");
        oldTarget = intent.getIntExtra("target", 0);
        oldStat = intent.getIntExtra("stat", 0);

        final Button confirm = (Button) findViewById(R.id.confirm_new_calculation);
        final Button cancel = (Button) findViewById(R.id.cancel_new_calculation);

        name = (EditText) findViewById(R.id.enter_calculation_name);
        name.setText(oldName);

        targetSpinner = (Spinner) findViewById(R.id.target_spinner);
        statSpinner = (Spinner) findViewById(R.id.stat_spinner);

        String[] statList = StatisticMath.getStatList();
        String[] targetList = DefaultType.getTargetList(getApplicationContext(), typeId, true);

        ArrayAdapter<String> targetAdapter = new ArrayAdapter<String>(EditCalculationActivity.this, R.layout.spinner_layout, targetList);
        targetAdapter.setDropDownViewResource(R.layout.spinner_item_layout);
        targetSpinner.setAdapter(targetAdapter);

        ArrayAdapter<String> statAdapter = new ArrayAdapter<String>(EditCalculationActivity.this, R.layout.spinner_layout, statList);
        statAdapter.setDropDownViewResource(R.layout.spinner_item_layout);
        statSpinner.setAdapter(statAdapter);

        database.open();
        targetSpinner.setSelection(1 + database.getTypeAttributePositionByTarget(typeId, oldTarget));
        database.close();

        statSpinner.setSelection(oldStat);

        targetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                targetSelection = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        statSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                statSelection = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (targetSelection == 0 || statSelection == 0) {
                    Toast.makeText(getApplicationContext(), "Please Choose an Attribute and Calculation!", Toast.LENGTH_SHORT).show();
                } else if (name.getText().toString().trim().isEmpty()){
                    Intent intent = new Intent();
                    database.open();
                    database.updateCalculationById(typeId, id, database.getAllNumericTypeAttributes(typeId).get(targetSelection - 1).getId(), statSelection, oldName);
                    database.close();
                    intent.putExtra("name", oldName);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                } else {
                    Intent intent = new Intent();
                    database.open();
                    database.updateCalculationById(typeId, id, database.getAllNumericTypeAttributes(typeId).get(targetSelection - 1).getId(), statSelection, name.getText().toString());
                    database.close();
                    intent.putExtra("name", name.getText().toString());
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
