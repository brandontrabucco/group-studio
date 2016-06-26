package com.brandon.apps.groupstudio.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.brandon.apps.groupstudio.assets.BaseActivity;
import com.brandon.apps.groupstudio.assets.DatabaseAdapter;
import com.brandon.apps.groupstudio.assets.DefaultGroup;
import com.brandon.apps.groupstudio.assets.FileHandler;
import com.brandon.apps.groupstudio.assets.ModelAdapter;
import com.brandon.apps.groupstudio.inflaters.GroupListInflater;
import com.brandon.apps.groupstudio.R;
import com.brandon.apps.groupstudio.assets.ResultCode;
import com.brandon.apps.groupstudio.models.GroupModel;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.List;


public class MainActivity extends BaseActivity {

    private ListView groupListView;
    private GroupListInflater groupListInflater;
    private Toolbar toolbar;
    private int selection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.button_overlay_activity);

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        groupListView = (ListView) findViewById(R.id.object_list);
        groupListInflater = new GroupListInflater(MainActivity.this, groupListView);

        final ImageButton newButton = (ImageButton) findViewById(R.id.new_button);
        newButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CreateGroupActivity.class);
                startActivityForResult(intent, ResultCode.CREATE_CODE);
            }
        });

        groupListInflater.populateList();
        FileHandler.init();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_CANCELED){
            groupListInflater.populateList();
            return;
        }

        switch (requestCode){
            case ResultCode.CREATE_CODE:
                DefaultGroup group = new DefaultGroup(0, data.getStringExtra("name"), data.getStringExtra("desc"));
                database.open();
                long id = database.insertGroup(group);
                database.close();
                group.setId((int)id);
                if (id < 0)
                    Toast.makeText(getBaseContext(), "An error has occurred!", Toast.LENGTH_SHORT).show();
                groupListInflater.populateList();
                Toast.makeText(getBaseContext(), group.getName() + " has been added to your Groups!", Toast.LENGTH_SHORT).show();
                break;
            case ResultCode.DELETE_CODE:
                // delete the group with the returned ID from list and database
                database.open();
                database.deleteGroupById(data.getIntExtra("id", 1));
                database.close();
                groupListInflater.populateList();
                Toast.makeText(getBaseContext(), "Attempting to delete!", Toast.LENGTH_SHORT).show();
                break;
            case ResultCode.READ_CODE:
                groupListInflater.populateList();
                break;
            case ResultCode.UPDATE_CODE:
                MainActivity.this.recreate();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        database.open();
        switch (database.selectUserType()) {
            case 0:
                getMenuInflater().inflate(R.menu.menu_main_basic, menu);
                break;
            case 1:
                getMenuInflater().inflate(R.menu.menu_main, menu);
                break;
            default:
                getMenuInflater().inflate(R.menu.menu_main, menu);
                break;
        }
        database.close();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sync) {
            Intent intent = new Intent(MainActivity.this, CloudMainActivity.class);
            startActivityForResult(intent, ResultCode.READ_CODE);
            return true;
        } else if (id == R.id.action_create_type) {
            Intent intent = new Intent(MainActivity.this, ManageTypeActivity.class);
            startActivityForResult(intent, ResultCode.MANAGE_CODE);
            return true;
        } else if (id == R.id.action_net) {
            Intent intent = new Intent(MainActivity.this, ServerConfigActivity.class);
            startActivityForResult(intent, ResultCode.UPDATE_CODE);
        /*} else if (id == R.id.action_bluetooth) {
                Intent intent = new Intent(MainActivity.this, BluetoothActivity.class);
                startActivityForResult(intent, ResultCode.READ_CODE);*/
        } else if (id == R.id.action_app_settings) {
            Intent intent = new Intent(MainActivity.this, AppConfigActivity.class);
            startActivityForResult(intent, ResultCode.UPDATE_CODE);
        } else if (id == R.id.action_import) {
            LayoutInflater inflater = MainActivity.this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.type_dialog, null);
            System.out.println("File import");

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialogStyle);
            builder.setView(dialogView);

            final TextView dialogTitle = (TextView) dialogView.findViewById(R.id.dialog_title_text);
            dialogTitle.setText("Import JSON");

            final Spinner spinner = (Spinner) dialogView.findViewById(R.id.type_spinner);
            final List<File> fileList = FileHandler.getAll();
            String[] fileSelectList = new String[fileList.size() + 1];
            fileSelectList[0] = "Select a File";

            for (int i = 0; i < fileList.size(); i++) {
                fileSelectList[i + 1] = fileList.get(i).getName().substring(0 ,fileList.get(i).getName().lastIndexOf('.'));
            }

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selection = position;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, R.layout.spinner_layout, fileSelectList);
            adapter.setDropDownViewResource(R.layout.spinner_item_layout);
            spinner.setAdapter(adapter);

            builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    if (selection != 0) {
                        new AsyncTask<Void, Void, GroupModel>() {
                            @Override
                            protected GroupModel doInBackground(Void... params) {
                                try {
                                    String data = FileHandler.read(getApplicationContext(),
                                            fileList.get(selection - 1).getName());
                                    System.out.println(data);
                                    Gson gson = new Gson();
                                    GroupModel model = gson.fromJson(data, GroupModel.class);
                                    return model;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    return null;
                                }
                            }

                            @Override
                            protected void onPostExecute(final GroupModel result) {
                                System.out.println(result.GroupName);
                                LayoutInflater inflater = getLayoutInflater();
                                View dialogView = inflater.inflate(R.layout.type_dialog, null);
                                database.open();

                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialogStyle);
                                builder.setView(dialogView);

                                final TextView dialogTitle = (TextView) dialogView.findViewById(R.id.dialog_title_text);
                                dialogTitle.setText("Write To");

                                final Spinner spinner2 = (Spinner) dialogView.findViewById(R.id.type_spinner);
                                final List<DefaultGroup> groupList = database.getAllGroups();
                                String[] groupSelectList = new String[groupList.size() + 1];
                                groupSelectList[0] = "New Group";

                                for (int i = 0; i < groupList.size(); i++) {
                                    groupSelectList[i + 1] = groupList.get(i).getName();
                                    System.out.println(groupList.get(i).getName());
                                }

                                spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                        selection = position;
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> parent) {

                                    }
                                });

                                ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(MainActivity.this, R.layout.spinner_layout, groupSelectList);
                                adapter2.setDropDownViewResource(R.layout.spinner_item_layout);
                                spinner2.setAdapter(adapter2);

                                System.out.println("Adapter Set");

                                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        int overwriteId;
                                        if (selection == 0) {
                                            overwriteId = -1;
                                        } else {
                                            overwriteId = groupList.get(selection - 1).getId();
                                        }
                                        new ModelAdapter() {
                                            @Override
                                            public void OnUpdate() {
                                                groupListInflater.populateList();
                                            }
                                        }.fromModel(MainActivity.this, result, overwriteId);
                                    }
                                });
                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                    }
                                });
                                builder.create().show();
                                System.out.println("Displaying View");
                            }
                        }.execute();
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                }
            });
            builder.show();
        } else if (id == R.id.action_contact) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("plain/text");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "groupstudiodev@gmail.com" });
            intent.putExtra(Intent.EXTRA_SUBJECT, "Group Studio Support");
            intent.putExtra(Intent.EXTRA_TEXT, "Report a bug or request support.");
            startActivity(Intent.createChooser(intent, "Choose an Email App"));
        }

        return super.onOptionsItemSelected(item);
    }
}
