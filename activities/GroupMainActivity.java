package com.brandon.apps.groupstudio.activities;

/**
 * Created by Brandon on 4/19/2015.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.brandon.apps.groupstudio.assets.BaseActivity;
import com.brandon.apps.groupstudio.assets.Calculation;
import com.brandon.apps.groupstudio.assets.DatabaseAdapter;
import com.brandon.apps.groupstudio.assets.DefaultAttribute;
import com.brandon.apps.groupstudio.assets.DefaultGroup;
import com.brandon.apps.groupstudio.assets.DefaultMember;
import com.brandon.apps.groupstudio.assets.DefaultType;
import com.brandon.apps.groupstudio.assets.FileHandler;
import com.brandon.apps.groupstudio.assets.GetAllAsyncTask;
import com.brandon.apps.groupstudio.assets.ModelAdapter;
import com.brandon.apps.groupstudio.assets.PullAsyncTask;
import com.brandon.apps.groupstudio.assets.PushAsyncTask;
import com.brandon.apps.groupstudio.assets.IntentCode;
import com.brandon.apps.groupstudio.assets.UpdateAsyncTask;
import com.brandon.apps.groupstudio.inflaters.MemberListInflater;
import com.brandon.apps.groupstudio.R;
import com.brandon.apps.groupstudio.assets.ResultCode;
import com.brandon.apps.groupstudio.models.AttributeModel;
import com.brandon.apps.groupstudio.models.CalculationModel;
import com.brandon.apps.groupstudio.models.DataModel;
import com.brandon.apps.groupstudio.models.GroupModel;
import com.brandon.apps.groupstudio.models.MemberModel;
import com.brandon.apps.groupstudio.models.TargetModel;
import com.brandon.apps.groupstudio.models.TypeModel;
import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

public class GroupMainActivity extends BaseActivity {

    private ListView memberListView;
    private MemberListInflater memberListInflater;
    private DefaultGroup parentGroup;
    private Intent intent;
    private int selection;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.button_overlay_activity);
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        intent = getIntent();
        String name = intent.getStringExtra("name");
        setTitle(name);

        database.open();
        parentGroup = database.selectGroupById(intent.getIntExtra("id", 1));
        database.close();

        memberListView = (ListView) findViewById(R.id.object_list);
        memberListInflater = new MemberListInflater(GroupMainActivity.this, memberListView, parentGroup);

        final ImageButton newMember = (ImageButton) findViewById(R.id.new_button);
        newMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupMainActivity.this, CreateMemberActivity.class);
                startActivityForResult(intent, ResultCode.CREATE_CODE);
            }
        });

        memberListInflater.populateList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        database.open();
        switch (database.selectUserType()) {
            case 0:
                getMenuInflater().inflate(R.menu.menu_statistics_basic, menu);
                break;
            case 1:
                getMenuInflater().inflate(R.menu.menu_statistics, menu);
                break;
            default:
                getMenuInflater().inflate(R.menu.menu_statistics, menu);
                break;
        }
        database.close();
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_CANCELED){
            memberListInflater.populateList();
            return;
        }


        if (requestCode == ResultCode.DELETE_CODE) {
            // delete the group with the returned ID from list and database
            database.open();
            database.deleteGroupById(data.getIntExtra("id", 1));
            database.close();
            Toast.makeText(getBaseContext(), "Attempting to delete!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        }
        else if (requestCode == ResultCode.UPDATE_CODE) {
            setTitle(data.getStringExtra("name"));
            Toast.makeText(getBaseContext(), parentGroup.getName() + " has been updated!", Toast.LENGTH_SHORT).show();
        }
        else if (requestCode == ResultCode.CREATE_CODE) {
            DefaultMember member = new DefaultMember(0, data.getStringExtra("name"), data.getIntExtra("type", 0), parentGroup.getId());
            database.open();
            long id = database.insertMember(parentGroup, member);
            database.close();
            member.setId((int) id);
            if (id < 0)
                Toast.makeText(getBaseContext(), "An error has occurred!", Toast.LENGTH_SHORT).show();
            memberListInflater.populateList();
            Toast.makeText(getBaseContext(), member.getName() + " has been added to " + parentGroup.getName() + "!", Toast.LENGTH_SHORT).show();
        }
        else if (requestCode == ResultCode.READ_CODE) {
            memberListInflater.populateList();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parentGroup activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_edit) {
            Intent intent = new Intent(GroupMainActivity.this, EditGroupActivity.class);
            intent.putExtra("name", parentGroup.getName());
            intent.putExtra("desc", parentGroup.getDesc());
            intent.putExtra("id", parentGroup.getId());
            startActivityForResult(intent, ResultCode.UPDATE_CODE);
            return true;
        } else if (id == R.id.action_delete) {
            Intent intent = new Intent(GroupMainActivity.this, DeleteActivity.class);
            intent.putExtra("name",  parentGroup.getName());
            intent.putExtra("id", parentGroup.getId());
            startActivityForResult(intent, ResultCode.DELETE_CODE);
            return true;
        } else if (id == R.id.action_back) {
            Intent intent = new Intent();
            setResult(RESULT_CANCELED, intent);
            finish();
            return true;
        } else if (id == R.id.action_stats) {
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.type_dialog, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(GroupMainActivity.this);
            builder.setView(dialogView);

            final TextView dialogTitle = (TextView) dialogView.findViewById(R.id.dialog_title_text);
            dialogTitle.setText("Select Type");

            final Spinner typeSpinner = (Spinner) dialogView.findViewById(R.id.type_spinner);

            String[] typeList = DefaultMember.getTypeList(getApplicationContext());

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(GroupMainActivity.this, R.layout.spinner_layout, typeList);
            adapter.setDropDownViewResource(R.layout.spinner_item_layout);
            typeSpinner.setAdapter(adapter);

            typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position != 0) {
                        database.open();
                        selection = database.getAllTypes().get(position - 1).getId();
                        database.close();
                    } else {
                        selection = -1;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    if (selection != -1) {
                        Intent intent = new Intent(GroupMainActivity.this, StatisticsMainActivity.class);
                        intent.putExtra("name", parentGroup.getName());
                        intent.putExtra("groupId", parentGroup.getId());
                        intent.putExtra("typeId", selection);
                        startActivityForResult(intent, ResultCode.READ_CODE);
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                }
            });
            builder.create().show();
        } else if (id == R.id.action_rank) {
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.type_dialog, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(GroupMainActivity.this);
            builder.setView(dialogView);

            final TextView dialogTitle = (TextView) dialogView.findViewById(R.id.dialog_title_text);
            dialogTitle.setText("Select Type");

            final Spinner typeSpinner = (Spinner) dialogView.findViewById(R.id.type_spinner);

            String[] typeList = DefaultMember.getTypeList(getApplicationContext());

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(GroupMainActivity.this, R.layout.spinner_layout, typeList);
            adapter.setDropDownViewResource(R.layout.spinner_item_layout);
            typeSpinner.setAdapter(adapter);

            typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position != 0) {
                        database.open();
                        selection = database.getAllTypes().get(position - 1).getId();
                        database.close();
                    } else {
                        selection = -1;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    if (selection != -1) {
                        Intent intent = new Intent(GroupMainActivity.this, StatisticsRankActivity.class);
                        intent.putExtra("name", parentGroup.getName());
                        intent.putExtra("groupId", parentGroup.getId());
                        intent.putExtra("typeId", selection);
                        startActivityForResult(intent, ResultCode.READ_CODE);
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                }
            });
            builder.create().show();
        } else if (id == R.id.action_push_to_cloud) {
            new GetAllAsyncTask(getApplicationContext()) {
                @Override
                public void OnListUpdate() {
                    super.OnListUpdate();
                    final List<GroupModel> modelList = super.modelList;
                    String[] nameList = new String[modelList.size() + 1];
                    nameList[0] = "New Group";
                    for (int i = 0; i < modelList.size(); i++) {
                        nameList[i + 1] = modelList.get(i).GroupName;
                    }

                    LayoutInflater inflater = GroupMainActivity.this.getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.type_dialog, null);
                    System.out.println("Push To");

                    AlertDialog.Builder builder = new AlertDialog.Builder(GroupMainActivity.this, R.style.AlertDialogStyle);
                    builder.setView(dialogView);

                    final TextView dialogTitle = (TextView) dialogView.findViewById(R.id.dialog_title_text);
                    dialogTitle.setText("Push To");

                    final Spinner spinner = (Spinner) dialogView.findViewById(R.id.type_spinner);

                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selection = position;
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(GroupMainActivity.this, R.layout.spinner_layout, nameList);
                    adapter.setDropDownViewResource(R.layout.spinner_item_layout);
                    spinner.setAdapter(adapter);

                    builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final String targetName;
                            if (selection > 0) {
                                targetName = modelList.get(selection - 1).GroupName;
                            } else {
                                targetName = "";
                            }
                            new AsyncTask<Void, Void, DataModel>() {
                                @Override
                                public DataModel doInBackground(Void... params) {
                                    database.open();
                                    String password = database.selectServerPassword();
                                    database.close();

                                    DataModel model = new DataModel(password, targetName, new ModelAdapter().toModel(GroupMainActivity.this, parentGroup));
                                    return model;
                                }

                                @Override
                                public void onPostExecute(DataModel result) {
                                    new PushAsyncTask(getApplicationContext()).execute(result);
                                }
                            }.execute();
                        }
                    });

                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                    builder.show();
                }
            }.execute();

        } else if (id == R.id.action_pull_from_cloud) {
            database.open();
            new GetAllAsyncTask(getApplicationContext()) {
                @Override
                public void OnListUpdate() {
                    super.OnListUpdate();
                    final List<GroupModel> modelList = super.modelList;
                    String[] nameList = new String[modelList.size() + 1];
                    nameList[0] = "Select a Group";
                    for (int i = 0; i < modelList.size(); i++) {
                        nameList[i + 1] = modelList.get(i).GroupName;
                    }

                    LayoutInflater inflater = GroupMainActivity.this.getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.type_dialog, null);
                    System.out.println("Pull a Group");

                    AlertDialog.Builder builder = new AlertDialog.Builder(GroupMainActivity.this, R.style.AlertDialogStyle);
                    builder.setView(dialogView);

                    final TextView dialogTitle = (TextView) dialogView.findViewById(R.id.dialog_title_text);
                    dialogTitle.setText("Pull From");

                    final Spinner spinner = (Spinner) dialogView.findViewById(R.id.type_spinner);

                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selection = position;
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(GroupMainActivity.this, R.layout.spinner_layout, nameList);
                    adapter.setDropDownViewResource(R.layout.spinner_item_layout);
                    spinner.setAdapter(adapter);

                    builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (selection > 0) {
                                final String targetName = modelList.get(selection - 1).GroupName;
                                new PullAsyncTask(GroupMainActivity.this, parentGroup.getId()) {
                                    @Override
                                    public void OnTaskEnd() {
                                        memberListInflater.populateList();
                                    }
                                }.execute(new TargetModel(database.selectServerPassword(), targetName));
                            }
                        }
                    });

                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                    builder.show();
                }
            }.execute();
        } else if (id == R.id.action_merge_with_cloud) {
            database.open();
            new PullAsyncTask(GroupMainActivity.this,  parentGroup.getId()) {
                @Override
                public void OnTaskEnd() {
                    memberListInflater.populateList();
                    new AsyncTask<Void, Void, DataModel>() {
                        @Override
                        public DataModel doInBackground(Void... params) {
                            database.open();
                            String password = database.selectServerPassword();
                            database.close();

                            DataModel model = new DataModel(password, parentGroup.getName(), new ModelAdapter().toModel(GroupMainActivity.this, parentGroup));
                            return model;
                        }
                        @Override
                        public void onPostExecute(DataModel result) {
                            new PushAsyncTask(getApplicationContext()).execute(result);
                        }
                    }.execute();
                }
            }.execute(new TargetModel(database.selectServerPassword(), parentGroup.getName()));
            database.close();
        } else if (id == R.id.action_json) {
            new AsyncTask<Void, Void, GroupModel>() {
                @Override
                public GroupModel doInBackground(Void... params) {
                    return new ModelAdapter().toModel(GroupMainActivity.this, parentGroup);
                }
                @Override
                public void onPostExecute(GroupModel result) {
                    Gson serializer = new Gson();
                    String data = serializer.toJson(result);
                    System.out.println(data);

                    try {
                        FileHandler.write(getApplicationContext(), data, result.GroupName);
                        Toast.makeText(getApplicationContext(), "Saved to Documents!", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.execute();
        }

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }
}
