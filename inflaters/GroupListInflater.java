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

import com.brandon.apps.groupstudio.assets.DatabaseAdapter;
import com.brandon.apps.groupstudio.assets.DefaultGroup;
import com.brandon.apps.groupstudio.R;
import com.brandon.apps.groupstudio.assets.ResultCode;
import com.brandon.apps.groupstudio.activities.GroupMainActivity;
import com.brandon.apps.groupstudio.assets.UpdateAsyncTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Brandon on 4/26/2015.
 */

public class GroupListInflater {
    private ActionBarActivity activity;
    private List<DefaultGroup> list;
    private ListView listView;
    DatabaseAdapter database;
    public GroupListInflater(ActionBarActivity _activity, ListView _listView) {
        activity = _activity;
        listView = _listView;
        list = new ArrayList<DefaultGroup>();
        database = new DatabaseAdapter(activity.getApplicationContext());
    }

    public void populateList(){
        database.open();
        new UpdateAsyncTask<DefaultGroup>(activity.getApplicationContext()) {
            @Override
            public List<DefaultGroup> UpdateTask() {
                list = this.database.getAllGroups();
                return list;
            }

            @Override
            public void UpdateView(List result) {
                super.UpdateView(result);
                ArrayAdapter<DefaultGroup> adapter = new ListAdapter();
                listView.setAdapter(adapter);
                database.close();
            }
        }.execute();
    }

    private class ListAdapter extends ArrayAdapter<DefaultGroup>{
        public ListAdapter(){
            super(activity, R.layout.list_object_data, list);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent){
            if(view == null)
                view = activity.getLayoutInflater().inflate(R.layout.list_object_data, parent, false);

            final DefaultGroup currentGroup = list.get(position);

            TextView name = (TextView) view.findViewById(R.id.object_name);
            name.setText(currentGroup.getName());
            TextView desc = (TextView) view.findViewById(R.id.object_desc);
            desc.setText(currentGroup.getDesc());
            TextView amount = (TextView) view.findViewById(R.id.object_length);
            int length = database.getMemberLength(currentGroup.getId());

            if (length == 1) {
                amount.setText(length + " Member");
            } else {
                amount.setText(length + " Members");
            }

            view.setTag(currentGroup.getId());

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity, GroupMainActivity.class);
                    int id = (int) v.getTag();
                    // get the name of the row with the id of v.getTag();
                    intent.putExtra("name", database.selectGroupById(id).getName());
                    intent.putExtra("id", id);
                    activity.startActivityForResult(intent, ResultCode.READ_CODE);
                }
            });

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    LayoutInflater inflater = activity.getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.delete_dialog, null);

                    AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AlertDialogStyle);
                    builder.setView(dialogView);

                    final TextView dialogTitle = (TextView) dialogView.findViewById(R.id.dialog_title_text);
                    dialogTitle.setText("Delete Group");
                    final TextView dialogBody = (TextView) dialogView.findViewById(R.id.dialog_body_text);
                    dialogBody.setText(currentGroup.getName());

                    builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            database.deleteGroupById(currentGroup.getId());
                            populateList();
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

            return view;
        }
    }
}
