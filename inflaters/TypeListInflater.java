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
import com.brandon.apps.groupstudio.assets.DefaultType;
import com.brandon.apps.groupstudio.R;
import com.brandon.apps.groupstudio.assets.ResultCode;
import com.brandon.apps.groupstudio.activities.TypeMainActivity;
import com.brandon.apps.groupstudio.assets.UpdateAsyncTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Brandon on 4/26/2015.
 */
public class TypeListInflater {
    private ActionBarActivity activity;
    private List<DefaultType> list;
    private ListView listView;
    DatabaseAdapter database;
    public TypeListInflater(ActionBarActivity _activity, ListView _listView) {
        activity = _activity;
        listView = _listView;
        list = new ArrayList<DefaultType>();
        database = new DatabaseAdapter(activity.getApplicationContext());
    }

    public void populateList(){
        database.open();
        new UpdateAsyncTask<DefaultType>(activity.getApplicationContext()) {
            @Override
            public List<DefaultType> UpdateTask() {
                list = this.database.getAllTypes();
                return list;
            }

            @Override
            public void UpdateView(List result) {
                super.UpdateView(result);
                ArrayAdapter<DefaultType> adapter = new ListAdapter();
                listView.setAdapter(adapter);
                database.close();
            }
        }.execute();
    }

    private class ListAdapter extends ArrayAdapter<DefaultType>{
        public ListAdapter(){
            super(activity, R.layout.type_layout, list);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent){
            if(view == null)
                view = activity.getLayoutInflater().inflate(R.layout.type_layout, parent, false);

            final DefaultType currentType = list.get(position);

            TextView name = (TextView) view.findViewById(R.id.type_name);
            name.setText(currentType.getName());
            TextView amount = (TextView)view.findViewById(R.id.attribute_length);
            int length = database.getTypeAttributeLength(currentType.getId());

            if (length == 1) {
                amount.setText(length + " Attribute");
            } else {
                amount.setText(length + " Attributes");
            }

            view.setTag(currentType);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity, TypeMainActivity.class);
                    DefaultType type = (DefaultType) v.getTag();
                    // get the name of the row with the id of v.getTag();
                    intent.putExtra("name", type.getName());
                    intent.putExtra("id", type.getId());
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
                    dialogTitle.setText("Delete Type");
                    final TextView dialogBody = (TextView) dialogView.findViewById(R.id.dialog_body_text);
                    dialogBody.setText(currentType.getName());

                    builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            database.deleteTypeById(currentType.getId());
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
