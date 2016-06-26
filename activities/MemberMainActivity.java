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
import com.brandon.apps.groupstudio.assets.DefaultMember;
import com.brandon.apps.groupstudio.inflaters.MemberAttributeListInflater;
import com.brandon.apps.groupstudio.R;
import com.brandon.apps.groupstudio.assets.ResultCode;

public class MemberMainActivity extends BaseActivity {
    private DefaultMember selection;
    private Intent intent;
    private int parentId;
    private int focusId;
    private ListView attributeList;
    private MemberAttributeListInflater memberAttributeListInflater;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.button_overlay_activity);
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        intent = getIntent();
        parentId = intent.getIntExtra("parentId", 1);
        focusId = intent.getIntExtra("id", 1);
        String name = intent.getStringExtra("name");
        setTitle(name);

        database.open();
        selection = database.selectMemberById(parentId, focusId);
        database.close();

        attributeList = (ListView)findViewById(R.id.object_list);
        memberAttributeListInflater = new MemberAttributeListInflater(MemberMainActivity.this, attributeList, parentId, focusId);

        final ImageButton newAttributeButton = (ImageButton)findViewById(R.id.new_button);
        newAttributeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                database.open();
                long id = database.insertMemberAttribute(parentId, selection.getId(), new DefaultAttribute(0, -1, "Add a description", "..."));
                database.close();
                memberAttributeListInflater.populateList();
            }
        });

        memberAttributeListInflater.populateList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_options, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_CANCELED){
            memberAttributeListInflater.populateList();
            return;
        }

        if (requestCode == ResultCode.DELETE_CODE) {
            // delete the group with the returned ID from list and database
            database.open();
            database.deleteMemberById(parentId, focusId);
            Toast.makeText(getBaseContext(), "Attempting to delete!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            database.close();
            finish();
        }
        else if (requestCode == ResultCode.UPDATE_CODE) {
            setTitle(data.getStringExtra("name"));
            database.open();
            selection = database.selectMemberById(parentId, focusId);
            database.close();
            Toast.makeText(getBaseContext(), selection.getName() + " has been updated!", Toast.LENGTH_SHORT).show();
            memberAttributeListInflater.populateList();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_edit) {
            Intent intent = new Intent(MemberMainActivity.this, EditMemberActivity.class);
            intent.putExtra("name", selection.getName());
            intent.putExtra("type", selection.getTypeId());
            intent.putExtra("id", focusId);
            intent.putExtra("parent", parentId);
            startActivityForResult(intent, ResultCode.UPDATE_CODE);
            return true;
        } else if (id == R.id.action_delete) {
            Intent intent = new Intent(MemberMainActivity.this, DeleteActivity.class);
            intent.putExtra("name", selection.getName());
            intent.putExtra("id", focusId);
            startActivityForResult(intent, ResultCode.DELETE_CODE);
            return true;
        } else if (id == R.id.action_back) {
            Intent intent = new Intent();
            setResult(RESULT_CANCELED, intent);
            finish();
            return true;
        }

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }
}
