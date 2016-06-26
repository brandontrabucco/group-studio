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

import com.brandon.apps.groupstudio.assets.Calculation;
import com.brandon.apps.groupstudio.assets.DatabaseAdapter;
import com.brandon.apps.groupstudio.assets.DefaultAttribute;
import com.brandon.apps.groupstudio.assets.DefaultGroup;
import com.brandon.apps.groupstudio.assets.DefaultMember;
import com.brandon.apps.groupstudio.R;
import com.brandon.apps.groupstudio.assets.ResultCode;
import com.brandon.apps.groupstudio.activities.MemberMainActivity;
import com.brandon.apps.groupstudio.assets.UpdateAsyncTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Brandon on 4/26/2015.
 */
public class MemberListInflater {
    private ActionBarActivity activity;
    private List<DefaultMember> list;
    private ListView listView;
    private final DefaultGroup parentGroup;
    DatabaseAdapter database;

    public MemberListInflater(ActionBarActivity _activity, ListView _listView, DefaultGroup _parent) {
        activity = _activity;
        listView = _listView;
        list = new ArrayList<DefaultMember>();
        parentGroup = _parent;
        database = new DatabaseAdapter(activity.getApplicationContext());
    }

    public void populateList(){
        database.open();
        new UpdateAsyncTask<DefaultMember>(activity.getApplicationContext()) {
            @Override
            public List<DefaultMember> UpdateTask() {
                list = database.getAllMembers(parentGroup.getId());
                return list;
            }

            @Override
            public void UpdateView(List result) {
                super.UpdateView(result);
                ArrayAdapter<DefaultMember> adapter = new ListAdapter();
                listView.setAdapter(adapter);
                database.close();
            }
        }.execute();
    }

    private class ListAdapter extends ArrayAdapter<DefaultMember>{
        public ListAdapter(){
            super(activity, R.layout.list_object_data, list);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent){
            if(view == null)
                view = activity.getLayoutInflater().inflate(R.layout.list_object_data, parent, false);

            final DefaultMember currentMember = list.get(position);

            List<DefaultAttribute> typeAttributeList = database.getAllTypeAttributes(currentMember.getTypeId());
            List<DefaultAttribute> memberAttributeList = database.getAllMemberAttributes(parentGroup.getId(), currentMember.getId());

            for (int i = 0; i < typeAttributeList.size(); i++) {
                boolean exists = false;
                DefaultAttribute selected = typeAttributeList.get(i);
                for (int j = 0; j < memberAttributeList.size(); j++) {
                    if (memberAttributeList.get(j).getUniversalId() == typeAttributeList.get(i).getId()) {
                        database.updateMemberAttributeById(parentGroup.getId(), currentMember.getId(), memberAttributeList.get(j).getId(), memberAttributeList.get(j).getUniversalId(), selected.getStatId(), selected.getTitle(), memberAttributeList.get(j).getValue());
                        exists = true;
                    }
                }
                if(!exists) {
                    database.insertMemberAttribute(parentGroup.getId(), currentMember.getId(), typeAttributeList.get(i));
                }
            }

            TextView name = (TextView) view.findViewById(R.id.object_name);
            name.setText(currentMember.getName());
            TextView type = (TextView) view.findViewById(R.id.object_desc);
            if (currentMember.getTypeId() != 0) {
                type.setText(database.selectTypeById(currentMember.getTypeId()).getName());
            } else {
                type.setText("Empty Type");
            }
            TextView amount = (TextView) view.findViewById(R.id.object_length);
            int length = database.getMemberAttributeLength(parentGroup.getId(), currentMember.getId());

            if (length == 1) {
                amount.setText(length + " Attribute");
            } else {
                amount.setText(length + " Attributes");
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity, MemberMainActivity.class);
                    intent.putExtra("name", currentMember.getName());
                    intent.putExtra("id", currentMember.getId());
                    intent.putExtra("parentId", parentGroup.getId());
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
                    dialogTitle.setText("Delete Member");
                    final TextView dialogBody = (TextView) dialogView.findViewById(R.id.dialog_body_text);
                    dialogBody.setText(currentMember.getName());

                    builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            database.deleteMemberById(parentGroup.getId(), currentMember.getId());
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