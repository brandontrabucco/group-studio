package com.brandon.apps.groupstudio.inflaters;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.brandon.apps.groupstudio.R;
import com.brandon.apps.groupstudio.activities.GroupMainActivity;
import com.brandon.apps.groupstudio.assets.DatabaseAdapter;
import com.brandon.apps.groupstudio.assets.DefaultGroup;
import com.brandon.apps.groupstudio.assets.DeleteAsyncTask;
import com.brandon.apps.groupstudio.assets.GetAllAsyncTask;
import com.brandon.apps.groupstudio.assets.PullAsyncTask;
import com.brandon.apps.groupstudio.assets.ResultCode;
import com.brandon.apps.groupstudio.assets.UpdateAsyncTask;
import com.brandon.apps.groupstudio.models.GroupModel;
import com.brandon.apps.groupstudio.models.TargetModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Brandon on 4/26/2015.
 */

public class CloudGroupListInflater {
    private ActionBarActivity activity;
    private List<GroupModel> list;
    private ListView listView;
    DatabaseAdapter database;
    public CloudGroupListInflater(ActionBarActivity _activity, ListView _listView) {
        activity = _activity;
        listView = _listView;
        list = new ArrayList<GroupModel>();
        database = new DatabaseAdapter(activity.getApplicationContext());
    }

    public void populateList(){
        database.open();
        new UpdateAsyncTask<GroupModel>(activity.getApplicationContext()) {
            @Override
            public List<GroupModel> UpdateTask() {
                // get groups from server
                new GetAllAsyncTask(activity.getApplicationContext()) {
                    @Override
                    public void OnListUpdate() {
                        super.OnListUpdate();
                        list = super.modelList;
                        ArrayAdapter<GroupModel> adapter = new ListAdapter();
                        listView.setAdapter(adapter);
                        database.close();
                    }
                }.execute();
                return list;
            }

            @Override
            public void UpdateView(List result) {
                super.UpdateView(result);
            }
        }.execute();
    }

    private class ListAdapter extends ArrayAdapter<GroupModel>{
        public ListAdapter(){
            super(activity, R.layout.list_object_data, list);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent){
            if(view == null)
                view = activity.getLayoutInflater().inflate(R.layout.type_layout, parent, false);

            final GroupModel currentGroup = list.get(position);

            TextView name = (TextView) view.findViewById(R.id.type_name);
            name.setText(currentGroup.GroupName);
            TextView amount = (TextView) view.findViewById(R.id.attribute_length);
            int length = (currentGroup.MemberList != null) ? currentGroup.MemberList.size() : 0;

            if (length == 1) {
                amount.setText(length + " Member");
            } else {
                amount.setText(length + " Members");
            }

            view.setTag(currentGroup.GroupId);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // download group prompt

                    LayoutInflater inflater = activity.getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.delete_dialog, null);

                    AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AlertDialogStyle);
                    builder.setView(dialogView);

                    final TextView dialogTitle = (TextView) dialogView.findViewById(R.id.dialog_title_text);
                    dialogTitle.setText("Pull Group");
                    final TextView dialogBody = (TextView) dialogView.findViewById(R.id.dialog_body_text);
                    dialogBody.setText(currentGroup.GroupName);

                    builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // send server download request
                            new PullAsyncTask(activity) {
                                @Override
                                public void OnTaskEnd() {
                                    populateList();
                                }
                            }.execute(new TargetModel(database.selectServerPassword(), currentGroup.GroupName));
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
                    builder.create().show();
                }
            });

            if (database.selectUserType() != 0) {
                view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        // delete from cloud prompt

                        LayoutInflater inflater = activity.getLayoutInflater();
                        View dialogView = inflater.inflate(R.layout.delete_dialog, null);

                        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AlertDialogStyle);
                        builder.setView(dialogView);

                        final TextView dialogTitle = (TextView) dialogView.findViewById(R.id.dialog_title_text);
                        dialogTitle.setText("Delete Group");
                        final TextView dialogBody = (TextView) dialogView.findViewById(R.id.dialog_body_text);
                        dialogBody.setText(currentGroup.GroupName);

                        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                new DeleteAsyncTask(activity.getApplicationContext()) {
                                    @Override
                                    public void OnUpdate() {
                                        populateList();
                                    }
                                }.execute(new TargetModel(database.selectServerPassword(), currentGroup.GroupName));
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        });
                        builder.create().show();

                        return false;
                    }
                });
            }

            return view;
        }
    }
}
