package com.brandon.apps.groupstudio.inflaters;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.brandon.apps.groupstudio.assets.DatabaseAdapter;
import com.brandon.apps.groupstudio.assets.DefaultAttribute;
import com.brandon.apps.groupstudio.assets.DefaultMember;
import com.brandon.apps.groupstudio.R;
import com.brandon.apps.groupstudio.assets.DefaultType;
import com.brandon.apps.groupstudio.assets.StatisticMath;
import com.brandon.apps.groupstudio.assets.UpdateAsyncTask;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Timer;

/**
 * Created by Brandon on 4/26/2015.
 */

public class RankListInflater {
    private ActionBarActivity activity;
    private List<DefaultMember> list;
    private ListView listView;
    private int typeId, groupId;
    DatabaseAdapter database;
    public RankListInflater(ActionBarActivity _activity, ListView _listView, int _typeId, int _groupId) {
        activity = _activity;
        listView = _listView;
        list = new ArrayList<DefaultMember>();
        database = new DatabaseAdapter(activity.getApplicationContext());
        typeId = _typeId;
        groupId = _groupId;
    }

    public void populateList(){
        database.open();
        new UpdateAsyncTask<DefaultMember>(activity.getApplicationContext()) {
            @Override
            public List<DefaultMember> UpdateTask() {
                List<DefaultMember> temp = this.database.getAllMembers(groupId);
                list.clear();
                for (int i = 0; i < temp.size(); i++) {
                    if (temp.get(i).getTypeId() == typeId) {
                        list.add(temp.get(i));
                    }
                }
                System.out.println(list.size() + " members found, " + typeId);
                return list;
            }

            @Override
            public void UpdateView(List result) {
                super.UpdateView(result);
                new RankTask().execute(result);
                database.close();
            }
        }.execute();
    }
    
    public List<DefaultMember> updateWeights(List<DefaultMember> _list) {
        System.out.println("Searching for attributes");
        List<DefaultAttribute> attributes = database.getAllTargetTypeAttributes(typeId);
        System.out.println(attributes.size() + " attributes collected");
        List<MemberData> valueList = new ArrayList<MemberData>();
        double[] values;

        for (int j = 0; j < attributes.size(); j++) {
            DefaultAttribute attribute = attributes.get(j);

            System.out.println("Loop " + j);

            // iterate for each attribute with a target in the type
            values = new double[_list.size()];
            valueList.clear();
            for (int i = 0; i < _list.size(); i++) {
                if (attribute.getStatId() == 1) {
                    // number value is parsed into value array, insert parser here

                    try {
                        String attributeValue = database.selectAttributeValueByUId(groupId, _list.get(i).getId(), attribute.getId()).trim();

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
                                for (DefaultAttribute currentAttribute:
                                        database.getAllMemberAttributes(groupId, _list.get(i).getId())) {
                                    System.out.println(currentAttribute.getTitle() + " == " + rowName);
                                    if (currentAttribute.getTitle().trim().toLowerCase().replace(' ', '_').equals(rowName.trim().toLowerCase())) {
                                        rowValue = currentAttribute.getValue();
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

                        values[i] = StatisticMath.eval(attributeValue);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        values[i] = 0;
                    }

                } else if (attribute.getStatId() == 3) {
                    if ((database.selectAttributeValueById(groupId, _list.get(i).getId(), attribute.getId()).toLowerCase().trim().equals("yes") ||
                            database.selectAttributeValueById(groupId, _list.get(i).getId(), attribute.getId()).toLowerCase().trim().equals("true")) ||
                            (database.selectAttributeValueById(groupId, _list.get(i).getId(), attribute.getId()).toLowerCase().trim().equals("on") ||
                                    database.selectAttributeValueById(groupId, _list.get(i).getId(), attribute.getId()).toLowerCase().trim().equals("1"))) {
                        values[i] = 1;
                    } else if ((database.selectAttributeValueById(groupId, _list.get(i).getId(), attribute.getId()).toLowerCase().trim().equals("no") ||
                            database.selectAttributeValueById(groupId, _list.get(i).getId(), attribute.getId()).toLowerCase().trim().equals("false")) ||
                            (database.selectAttributeValueById(groupId, _list.get(i).getId(), attribute.getId()).toLowerCase().trim().equals("off") ||
                                    database.selectAttributeValueById(groupId, _list.get(i).getId(), attribute.getId()).toLowerCase().trim().equals("1"))) {
                        values[i] = 0;
                    } else {
                        values[i] = 0;
                    }
                } else {
                    values[i] = 0;
                }


                MemberData data = new MemberData(_list.get(i).getId());
                data.setData(values[i]);
                valueList.add(data);
            }

            // get range of values

            double max = StatisticMath.max(values);
            double min = StatisticMath.min(values);
            double range = StatisticMath.range(values);

            // use the target to iterate per attribute and add a weighted number tp each member based on position in range of values

            int rankTarget = attribute.getRankId(); // the target id

            for (int i = 0; i < _list.size(); i++) {
                double score = 0;
                if (rankTarget == 1) {
                    score = (valueList.get(i).getData() - min) / range;
                } else if (rankTarget == 2) {
                    score = (max - valueList.get(i).getData()) / range;
                }
                _list.get(i).addWeight(score);
                System.out.println("Adding " + score + " to " + _list.get(i).getName() + ", " + _list.get(i).getWeight());
            }


        }
        Collections.sort(_list);
        System.out.println();
        return _list;
    }

    private class MemberData {
        private int memberId;
        private double data;
        public MemberData(int _memberId) {
            memberId = _memberId;
            data = 0;
        }
        public void setMemberId(int memberId) {
            this.memberId = memberId;
        }
        public void setData(double data) {
            this.data = data;
        }
        public int getMemberId() {
            return memberId;
        }
        public double getData() {
            return data;
        }
    }

    public class RankTask extends AsyncTask<List<DefaultMember>, Void, List<DefaultMember>> {
        protected List<DefaultMember> doInBackground(List<DefaultMember>... params) {
            System.out.println("Starting new task. " + params.length);
            return updateWeights(params[0]);
        }
        protected void onPostExecute(List<DefaultMember> params) {
            System.out.println("Ending task.");
            ArrayAdapter<DefaultMember> adapter = new ListAdapter(params);
            listView.setAdapter(adapter);
        }
    }

    private class ListAdapter extends ArrayAdapter<DefaultMember>{
        private List<DefaultMember> memberList;
        public ListAdapter(List<DefaultMember> _list){
            super(activity, R.layout.type_layout, _list);
            memberList = _list;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent){
            if(view == null)
                view = activity.getLayoutInflater().inflate(R.layout.type_layout, parent, false);

            final DefaultMember currentMember = memberList.get(position);

            TextView name = (TextView) view.findViewById(R.id.type_name);
            name.setText((position + 1) + ".  " + currentMember.getName());
            TextView detail = (TextView) view.findViewById(R.id.attribute_length);
            detail.setText(StatisticMath.round(currentMember.getWeight()) + "");

            view.setTag(currentMember);

            return view;
        }
    }
}
