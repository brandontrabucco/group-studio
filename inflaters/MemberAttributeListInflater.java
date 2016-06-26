package com.brandon.apps.groupstudio.inflaters;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.brandon.apps.groupstudio.assets.Calculation;
import com.brandon.apps.groupstudio.assets.DatabaseAdapter;
import com.brandon.apps.groupstudio.assets.DefaultAttribute;
import com.brandon.apps.groupstudio.R;
import com.brandon.apps.groupstudio.assets.StatisticMath;
import com.brandon.apps.groupstudio.assets.UpdateAsyncTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Brandon on 4/26/2015.
 */
public class MemberAttributeListInflater {
    private ActionBarActivity activity;
    private DatabaseAdapter database;
    private List<DefaultAttribute> list;
    private ListView listView;
    private int groupId;
    private int memberId;

    public MemberAttributeListInflater(ActionBarActivity _activity, ListView _listView, int _groupId, int _memberId) {
        activity = _activity;
        database = new DatabaseAdapter(activity.getApplicationContext());
        listView = _listView;
        list = new ArrayList<DefaultAttribute>();
        groupId = _groupId;
        memberId = _memberId;
    }

    public void populateList(){
        database.open();
        new UpdateAsyncTask<DefaultAttribute>(activity.getApplicationContext()) {
            @Override
            public List<DefaultAttribute> UpdateTask() {
                List<DefaultAttribute> typeAttributeList = database.getAllTypeAttributes(database.selectMemberById(groupId, memberId).getTypeId());
                List<DefaultAttribute> memberAttributeList = database.getAllMemberAttributes(groupId, memberId);

                for (int i = 0; i < typeAttributeList.size(); i++) {
                    DefaultAttribute selected = typeAttributeList.get(i);
                    boolean exists = false;
                    for (int j = 0; j < memberAttributeList.size(); j++) {
                        if (memberAttributeList.get(j).getUniversalId() == selected.getId()) {
                            database.updateMemberAttributeById(groupId, memberId, memberAttributeList.get(j).getId(), memberAttributeList.get(j).getUniversalId(), selected.getStatId(), selected.getTitle(), memberAttributeList.get(j).getValue());
                            exists = true;
                        }
                    }
                    if (!exists) {
                        database.insertMemberAttribute(groupId, memberId, typeAttributeList.get(i));
                    }
                }

                list = database.getAllMemberAttributes(groupId, memberId);
                return list;
            }

            @Override
            public void UpdateView(List result) {
                super.UpdateView(result);
                ArrayAdapter<DefaultAttribute> adapter = new ListAdapter();
                listView.setAdapter(adapter);
                database.close();
            }
        }.execute();
    }

    private class ListAdapter extends ArrayAdapter<DefaultAttribute>{
        public ListAdapter(){
            super(activity, R.layout.list_object_data, list);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent){
            if(view == null)
                view = activity.getLayoutInflater().inflate(R.layout.list_object_data, parent, false);

            final DefaultAttribute currentAttribute = list.get(position);

            TextView title = (TextView) view.findViewById(R.id.object_name);
            title.setText(currentAttribute.getTitle());
            TextView type = (TextView) view.findViewById(R.id.object_desc);
            type.setText(DefaultAttribute.getTypeList()[currentAttribute.getStatId()]);

            TextView value = (TextView) view.findViewById(R.id.object_length);

            String attributeValue = currentAttribute.getValue();

            if (attributeValue.contains("@")) {
                while (attributeValue.contains("@")) {
                    int charPosition = attributeValue.indexOf("@");
                    int start = charPosition;
                    System.out.println(attributeValue + ", " + charPosition);
                    String rowName = "", rowValue = "";
                    charPosition++;
                    while (charPosition < attributeValue.length() &&
                            (((Character.toLowerCase(attributeValue.charAt(charPosition)) >= 'a' &&
                                    Character.toLowerCase(attributeValue.charAt(charPosition)) <= 'z') ||
                                    (Character.toLowerCase(attributeValue.charAt(charPosition)) >= '0' &&
                                    Character.toLowerCase(attributeValue.charAt(charPosition)) <= '9')) ||
                                    attributeValue.charAt(charPosition) == '_')) {
                        rowName += Character.toString(attributeValue.charAt(charPosition));
                        charPosition++;
                    }
                    System.out.println(rowName);
                    if (!rowName.trim().isEmpty()) {
                        for (DefaultAttribute attribute:
                                database.getAllMemberAttributes(groupId, memberId)) {
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

                    System.out.println("Value: " + rowValue);

                    attributeValue = attributeValue.substring(0, start) + rowValue + attributeValue.substring(charPosition, attributeValue.length());
                }
                if (currentAttribute.getStatId() == 4 || currentAttribute.getStatId() == 1) {
                    currentAttribute.setValue(StatisticMath.eval(attributeValue) + "");
                } else {
                    currentAttribute.setValue(attributeValue);
                }
            }


            System.out.println(currentAttribute.getValue());
            if (currentAttribute.getStatId() == 1) {
                value.setText(StatisticMath.round(StatisticMath.eval(currentAttribute.getValue())) + "");
            } else {
                value.setText(currentAttribute.getValue());
            }


            if (currentAttribute.getStatId() != 4) {
                value.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LayoutInflater inflater = activity.getLayoutInflater();
                        View dialogView = inflater.inflate(R.layout.edit_dialog, null);

                        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AlertDialogStyle);
                        builder.setView(dialogView);

                        final TextView dialogTitle = (TextView) dialogView.findViewById(R.id.dialog_title_text);
                        dialogTitle.setText("Edit Value");
                        final EditText dialogEdit = (EditText) dialogView.findViewById(R.id.dialog_edit_text);
                        dialogEdit.setText(database.selectAttributeValueById(groupId, memberId, currentAttribute.getId()));

                        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                database.updateMemberAttributeById(groupId,
                                        memberId,
                                        currentAttribute.getId(),
                                        currentAttribute.getUniversalId(),
                                        currentAttribute.getStatId(),
                                        currentAttribute.getTitle(),
                                        dialogEdit.getText().toString());

                                populateList();
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        });
                        builder.create().show();
                    }
                });
            }


            title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LayoutInflater inflater = activity.getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.edit_dialog, null);

                    AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AlertDialogStyle);
                    builder.setView(dialogView);

                    final TextView dialogTitle = (TextView) dialogView.findViewById(R.id.dialog_title_text);
                    dialogTitle.setText("Edit Title");
                    final EditText dialogEdit = (EditText) dialogView.findViewById(R.id.dialog_edit_text);
                    dialogEdit.setText(currentAttribute.getTitle());

                    builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            database.updateMemberAttributeById(groupId,
                                    memberId,
                                    currentAttribute.getId(),
                                    -1,
                                    currentAttribute.getStatId(),
                                    dialogEdit.getText().toString(),
                                    currentAttribute.getValue()
                            );
                            populateList();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
                    builder.create().show();
                }
            });

            title.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    LayoutInflater inflater = activity.getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.delete_dialog, null);

                    AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AlertDialogStyle);
                    builder.setView(dialogView);

                    final TextView dialogTitle = (TextView) dialogView.findViewById(R.id.dialog_title_text);
                    dialogTitle.setText("Delete Attribute");
                    final TextView dialogBody = (TextView) dialogView.findViewById(R.id.dialog_body_text);
                    dialogBody.setText(currentAttribute.getTitle());

                    builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            database.deleteMemberAttributeById(groupId, memberId, currentAttribute.getId());
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

            type.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    LayoutInflater inflater = activity.getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.delete_dialog, null);

                    AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AlertDialogStyle);
                    builder.setView(dialogView);

                    final TextView dialogTitle = (TextView) dialogView.findViewById(R.id.dialog_title_text);
                    dialogTitle.setText("Delete Attribute");
                    final TextView dialogBody = (TextView) dialogView.findViewById(R.id.dialog_body_text);
                    dialogBody.setText(currentAttribute.getTitle());

                    builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            database.deleteMemberAttributeById(groupId, memberId, currentAttribute.getId());
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

            type.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LayoutInflater inflater = activity.getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.read_as_dialog, null);

                    AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AlertDialogStyle);
                    builder.setView(dialogView);

                    final TextView dialogTitle = (TextView) dialogView.findViewById(R.id.dialog_title_text);
                    dialogTitle.setText("Read As");

                    class ReadType {
                        public int type = 0;
                    }

                    final ReadType readType = new ReadType();

                    final RadioGroup radioGroup = (RadioGroup) dialogView.findViewById(R.id.radio_group);
                    radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(RadioGroup group, int checkedId) {
                            if (checkedId == R.id.read_text) {
                                readType.type = 0;
                            } else if (checkedId == R.id.read_number) {
                                readType.type = 1;
                            } else if (checkedId == R.id.read_list) {
                                readType.type = 2;
                            } else if (checkedId == R.id.read_boolean) {
                                readType.type = 3;
                            }
                        }
                    });

                    if (currentAttribute.getStatId() == 0) {
                        radioGroup.check(R.id.read_text);
                    } else if (currentAttribute.getStatId() == 1) {
                        radioGroup.check(R.id.read_number);
                    } else if (currentAttribute.getStatId() == 2) {
                        radioGroup.check(R.id.read_list);
                    } else if (currentAttribute.getStatId() == 3) {
                        radioGroup.check(R.id.read_boolean);
                    }

                    builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if (readType.type != -1) {
                                database.updateMemberAttributeById(
                                        groupId,
                                        memberId,
                                        currentAttribute.getId(),
                                        -1,
                                        readType.type,
                                        currentAttribute.getTitle(),
                                        currentAttribute.getValue()
                                );
                                populateList();
                            }
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
                    builder.create().show();
                }
            });

            return view;
        }
    }
}