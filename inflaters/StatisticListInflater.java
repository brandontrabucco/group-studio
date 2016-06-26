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

import com.brandon.apps.groupstudio.activities.EditCalculationActivity;
import com.brandon.apps.groupstudio.assets.Calculation;
import com.brandon.apps.groupstudio.assets.DatabaseAdapter;
import com.brandon.apps.groupstudio.assets.DefaultAttribute;
import com.brandon.apps.groupstudio.assets.DefaultMember;
import com.brandon.apps.groupstudio.assets.DefaultType;
import com.brandon.apps.groupstudio.R;
import com.brandon.apps.groupstudio.assets.ResultCode;
import com.brandon.apps.groupstudio.assets.StatisticMath;
import com.brandon.apps.groupstudio.assets.UpdateAsyncTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Brandon on 4/26/2015.
 */

public class StatisticListInflater {
    private ActionBarActivity activity;
    private List<Calculation> list;
    private ListView listView;
    private int typeId, groupId;
    DatabaseAdapter database;
    public StatisticListInflater(ActionBarActivity _activity, ListView _listView, int _typeId, int _groupId) {
        activity = _activity;
        listView = _listView;
        list = new ArrayList<Calculation>();
        database = new DatabaseAdapter(activity.getApplicationContext());
        typeId = _typeId;
        groupId = _groupId;
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

    public double getCalculation(Calculation calculation) {
        List<String> values = new ArrayList<String>();
        List<DefaultMember>  members = database.getAllMembers(groupId);

        for (int i = 0; i < members.size(); i++) {
            if (members.get(i).getTypeId() == typeId) {
                String attributeValue = database.selectAttributeValueById(groupId, members.get(i).getId(), calculation.getTargetId()).trim();

                while (attributeValue.contains("@")) {
                    int charPosition = attributeValue.indexOf("@");
                    int start = charPosition;
                    System.out.println(attributeValue + ", " + charPosition);
                    String rowName = "", rowValue = "";
                    charPosition++;
                    while (charPosition < attributeValue.length() &&
                            ((Character.toLowerCase(attributeValue.charAt(charPosition)) >= 'a' &&
                                    Character.toLowerCase(attributeValue.charAt(charPosition)) <= 'z') ||
                                    attributeValue.charAt(charPosition) == '_')) {
                        rowName += Character.toString(attributeValue.charAt(charPosition));
                        charPosition++;
                    }
                    System.out.println(rowName);
                    if (!rowName.trim().isEmpty()) {
                        for (DefaultAttribute attribute:
                                database.getAllMemberAttributes(groupId, members.get(i).getId())) {
                            System.out.println(attribute.getTitle() + " == " + rowName);
                            if (attribute.getTitle().trim().toLowerCase().replace(' ', '_').equals(rowName.trim().toLowerCase())) {
                                rowValue = attribute.getValue();
                                System.out.println("Row match found");
                                break;
                            }
                        }
                    } else {
                        rowValue = "";
                    }

                    if (attributeValue.contains(rowValue)) {
                        attributeValue = "";
                        break;
                    }

                    System.out.println("Value: " + rowValue);

                    attributeValue = attributeValue.substring(0, start) + rowValue + attributeValue.substring(charPosition, attributeValue.length());
                }

                values.add(attributeValue);
            }
        }

        double[] data = new double[values.size()];
        for (int i = 0; i < values.size(); i++) {
            // number value is parsed into value array, insert parser here

            try {
                data[i] = StatisticMath.eval(values.get(i).replace(" ", ""));
                System.out.println(data[i]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                data[i] = 0;
            }

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
            amount.setText(getCalculation(currentCalculation) + "");

            view.setTag(currentCalculation);

            if (database.selectUserType() != 0) {
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
            }

            return view;
        }
    }
}
