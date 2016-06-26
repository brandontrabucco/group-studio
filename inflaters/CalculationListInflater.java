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
import com.brandon.apps.groupstudio.assets.DefaultType;
import com.brandon.apps.groupstudio.R;
import com.brandon.apps.groupstudio.assets.ResultCode;
import com.brandon.apps.groupstudio.assets.StatisticMath;
import com.brandon.apps.groupstudio.activities.EditCalculationActivity;
import com.brandon.apps.groupstudio.assets.UpdateAsyncTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Brandon on 4/26/2015.
 */

public class CalculationListInflater {
    private ActionBarActivity activity;
    private List<Calculation> list;
    private ListView listView;
    private int typeId;
    DatabaseAdapter database;
    public CalculationListInflater(ActionBarActivity _activity, ListView _listView, int _typeId) {
        activity = _activity;
        listView = _listView;
        list = new ArrayList<Calculation>();
        database = new DatabaseAdapter(activity.getApplicationContext());
        typeId = _typeId;
    }

    public void populateList(){
        database.open();
        new UpdateAsyncTask<Calculation>(activity.getApplicationContext()) {
            @Override
            public List<Calculation> UpdateTask() {
                list = this.database.getAllCalculations(typeId);
                return list;
            }

            @Override
            public void UpdateView(List result) {
                super.UpdateView(result);
                ArrayAdapter<Calculation> adapter = new ListAdapter();
                listView.setAdapter(adapter);
                database.close();
            }
        }.execute();
    }

    public double getCalculation(int groupId, int memberId, Calculation calculation) {
        List<DefaultAttribute>  attributes = database.getAllMemberAttributes(groupId, memberId);
        double[] data = new double[attributes.size()];
        for (int i = 0; i < attributes.size(); i++) {
            data[i] = Double.parseDouble(attributes.get(i).getValue());
        }
        return StatisticMath.calculate(calculation.getStatId(), data);
    }

    private class ListAdapter extends ArrayAdapter<Calculation>{
        public ListAdapter(){
            super(activity, R.layout.list_object_data, list);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent){
            if(view == null)
                view = activity.getLayoutInflater().inflate(R.layout.list_object_data, parent, false);

            final Calculation currentCalculation = list.get(position);

            TextView name = (TextView) view.findViewById(R.id.object_name);
            name.setText(currentCalculation.getName());
            TextView desc = (TextView) view.findViewById(R.id.object_desc);
            desc.setText(StatisticMath.getStatList()[currentCalculation.getStatId()] +
                    " of " +
                    database.selectTypeAttributeById(typeId, currentCalculation.getTargetId()).getTitle() + "s");
            TextView amount = (TextView) view.findViewById(R.id.object_length);
            amount.setText("...");

            view.setTag(currentCalculation);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity, EditCalculationActivity.class);
                    Calculation currentCalculation = (Calculation) v.getTag();
                    intent.putExtra("typeId", typeId);
                    intent.putExtra("id", currentCalculation.getId());
                    intent.putExtra("name", currentCalculation.getName());
                    intent.putExtra("target", currentCalculation.getTargetId());
                    intent.putExtra("stat", currentCalculation.getStatId());
                    activity.startActivityForResult(intent, ResultCode.UPDATE_CODE);
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
                    dialogTitle.setText("Delete Calculation");
                    final TextView dialogBody = (TextView) dialogView.findViewById(R.id.dialog_body_text);
                    dialogBody.setText(currentCalculation.getName());

                    builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            database.deleteCalculationById(typeId, currentCalculation.getId());
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
