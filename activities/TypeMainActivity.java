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
import com.brandon.apps.groupstudio.assets.DefaultAttribute;
import com.brandon.apps.groupstudio.assets.DefaultType;
import com.brandon.apps.groupstudio.R;
import com.brandon.apps.groupstudio.assets.ResultCode;
import com.brandon.apps.groupstudio.inflaters.TypeAttributeListInflater;

public class TypeMainActivity extends BaseActivity {
    private DefaultType selection;
    private Intent intent;
    private int typeId;
    private ListView attributeList;
    private TypeAttributeListInflater attributeListInflater;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.button_overlay_activity);
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        intent = getIntent();
        typeId = intent.getIntExtra("id", 0);
        String name = intent.getStringExtra("name");
        setTitle(name);

        database.open();
        selection = database.selectTypeById(typeId);
        database.close();

        attributeList = (ListView)findViewById(R.id.object_list);
        attributeListInflater = new TypeAttributeListInflater(TypeMainActivity.this, attributeList, typeId);

        final ImageButton newAttributeButton = (ImageButton)findViewById(R.id.new_button);
        newAttributeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                database.open();
                long id = database.insertTypeAttribute(selection.getId(), new DefaultAttribute(0, typeId, "Add a description", "..."));
                database.close();
                attributeListInflater.populateList();
            }
        });

        attributeListInflater.populateList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_calculations, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_CANCELED){
            attributeListInflater.populateList();
            return;
        }

        if (requestCode == ResultCode.DELETE_CODE) {
            // delete the attribute with the returned ID from list and database
            database.open();
            database.deleteTypeById(typeId);
            database.close();
            Toast.makeText(getBaseContext(), "Attempting to delete!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        }
        else if (requestCode == ResultCode.UPDATE_CODE) {
            setTitle(data.getStringExtra("name"));
            database.open();
            selection = database.selectTypeById(typeId);
            database.close();
            Toast.makeText(getBaseContext(), selection.getName() + " has been updated!", Toast.LENGTH_SHORT).show();
            attributeListInflater.populateList();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_edit) {
            Intent intent = new Intent(TypeMainActivity.this, EditTypeActivity.class);
            intent.putExtra("name", selection.getName());
            intent.putExtra("id", typeId);
            startActivityForResult(intent, ResultCode.UPDATE_CODE);
            return true;
        } else if (id == R.id.action_delete) {
            Intent intent = new Intent(TypeMainActivity.this, DeleteActivity.class);
            intent.putExtra("name", selection.getName());
            intent.putExtra("id", typeId);
            startActivityForResult(intent, ResultCode.DELETE_CODE);
            return true;
        } else if (id == R.id.action_back) {
            Intent intent = new Intent();
            setResult(RESULT_CANCELED, intent);
            finish();
            return true;
        } else if (id == R.id.action_calc) {
            Intent intent = new Intent(TypeMainActivity.this, CalculationMainActivity.class);
            intent.putExtra("name", selection.getName());
            intent.putExtra("id", typeId);
            startActivityForResult(intent, ResultCode.DELETE_CODE);
            return true;
        }

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }
}
