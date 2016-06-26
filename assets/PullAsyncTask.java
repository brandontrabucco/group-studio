package com.brandon.apps.groupstudio.assets;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Xml;
import com.brandon.apps.groupstudio.R;
import com.brandon.apps.groupstudio.models.AttributeModel;
import com.brandon.apps.groupstudio.models.CalculationModel;
import com.brandon.apps.groupstudio.models.DataModel;
import com.brandon.apps.groupstudio.models.GroupModel;
import com.brandon.apps.groupstudio.models.MemberModel;
import com.brandon.apps.groupstudio.models.TargetModel;
import com.brandon.apps.groupstudio.models.TypeModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;;

/**
 * Created by Brandon on 11/26/2015.
 */
public class PullAsyncTask extends AsyncTask<TargetModel, Void, String> {
    private Context context;
    private ActionBarActivity activity;
    private DatabaseAdapter database;
    private int selection;
    public GroupModel model;
    private int targetId;
    public PullAsyncTask(ActionBarActivity a) {
        activity = a;
        context = a.getApplicationContext();
        database = new DatabaseAdapter(context);
        targetId = -1;
    }
    public PullAsyncTask(ActionBarActivity a, int id) {
        activity = a;
        context = a.getApplicationContext();
        database = new DatabaseAdapter(context);
        targetId = id;
    }
    public void OnTaskEnd() {}
    @Override
    protected String doInBackground(TargetModel... data) {
        database.open();
        String ip = database.selectServerIp();
        String url;
        if (ip.equals("")) {
            url = "";
        } else {
            url = (ip.trim().substring(ip.length() - 1).equals("/")) ? ip + "PullGroupData" : ip + "/PullGroupData";
        }
        database.close();

        Gson gson = new Gson();
        String json = gson.toJson(data[0]);
        System.out.println(json);
        return WebConnectionHandler.post(url, json);
    }
    // onPostExecute displays the results of the AsyncTask.
    @Override
    protected void onPostExecute(String result) {
        if(!result.equals("")) {
            try {
                switch (Integer.parseInt(result)) {
                    case (ResponseCode.Success):
                        // Should not ever output this
                        break;
                    case (ResponseCode.NotAuthorized):
                        Toast.makeText(context, "Not authorized, check password!", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(context, "Server error!", Toast.LENGTH_SHORT).show();
                        break;
                }
            } catch (NumberFormatException e) {
                Gson gson = new Gson();
                model = gson.fromJson(result, GroupModel.class);

                if (targetId == -1) {
                    LayoutInflater inflater = activity.getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.type_dialog, null);
                    database.open();

                    AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AlertDialogStyle);
                    builder.setView(dialogView);

                    final TextView dialogTitle = (TextView) dialogView.findViewById(R.id.dialog_title_text);
                    dialogTitle.setText("Write To");

                    final Spinner spinner = (Spinner) dialogView.findViewById(R.id.type_spinner);
                    final List<DefaultGroup> groupList = database.getAllGroups();
                    String[] groupSelectList = new String[groupList.size() + 2];
                    groupSelectList[0] = "JSON File";
                    groupSelectList[1] = "New Group";

                    for (int i = 0; i < groupList.size(); i++) {
                        groupSelectList[i + 2] = groupList.get(i).getName();
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

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity, R.layout.spinner_layout, groupSelectList);
                    adapter.setDropDownViewResource(R.layout.spinner_item_layout);
                    spinner.setAdapter(adapter);

                    builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            int overwriteId;
                            if (selection == 0) {
                                overwriteId = -2;
                            } else if (selection == 1) {
                                overwriteId = -1;
                            } else {
                                overwriteId = groupList.get(selection - 2).getId();
                            }
                            new ModelAdapter(){
                                @Override
                                public void OnUpdate() {
                                    OnTaskEnd();
                                    // Success
                                }
                            }.fromModel(activity, model, overwriteId);
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
                    builder.create().show();
                } else {
                    new ModelAdapter(){
                        @Override
                        public void OnUpdate() {
                            OnTaskEnd();
                            // Success
                        }
                    }.fromModel(activity, model, targetId);
                }
            }
        } else {
            Toast.makeText(context, "Error, check network or server settings!", Toast.LENGTH_SHORT).show();
        }
    }
}
