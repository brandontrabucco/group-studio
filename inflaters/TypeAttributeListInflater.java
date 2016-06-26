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

import com.brandon.apps.groupstudio.assets.DatabaseAdapter;
import com.brandon.apps.groupstudio.assets.DefaultAttribute;
import com.brandon.apps.groupstudio.R;
import com.brandon.apps.groupstudio.assets.DefaultMember;
import com.brandon.apps.groupstudio.assets.DefaultType;
import com.brandon.apps.groupstudio.assets.UpdateAsyncTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Brandon on 4/26/2015.
 */
public class TypeAttributeListInflater {
    private ActionBarActivity activity;
    private DatabaseAdapter database;
    private List<DefaultAttribute> list;
    private ListView listView;
    private int typeId;

    public TypeAttributeListInflater(ActionBarActivity _activity, ListView _listView, int _typeId) {
        activity = _activity;
        database = new DatabaseAdapter(activity.getApplicationContext());
        listView = _listView;
        list = new ArrayList<DefaultAttribute>();
        typeId = _typeId;
    }

    public void populateList(){
        database.open();
        new UpdateAsyncTask<DefaultAttribute>(activity.getApplicationContext()) {
            @Override
            public List<DefaultAttribute> UpdateTask() {
                list = this.database.getAllTypeAttributes(typeId);
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
            TextView value = (TextView) view.findViewById(R.id.object_length);
            value.setText(currentAttribute.getValue());
            TextView type = (TextView) view.findViewById(R.id.object_desc);
            type.setText(DefaultAttribute.getTypeList()[currentAttribute.getStatId()]);

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
                    dialogEdit.setText(currentAttribute.getValue());

                    builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            database.updateTypeAttributeById(typeId, currentAttribute.getId(), currentAttribute.getStatId(), currentAttribute.getTitle(), dialogEdit.getText().toString());
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

            if (currentAttribute.getStatId() == 1) {
                value.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        LayoutInflater inflater = activity.getLayoutInflater();
                        View dialogView = inflater.inflate(R.layout.rank_dialog_number, null);

                        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AlertDialogStyle);
                        builder.setView(dialogView);

                        final TextView dialogTitle = (TextView) dialogView.findViewById(R.id.dialog_title_text);
                        dialogTitle.setText("Set Target");
                        final RadioGroup radioGroup = (RadioGroup) dialogView.findViewById(R.id.radio_group);

                        class ReadType {
                            public int type = 0;
                        }

                        final ReadType readType = new ReadType();

                        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(RadioGroup group, int checkedId) {
                                if (checkedId == R.id.none_select) {
                                    readType.type = 0;
                                } else if (checkedId == R.id.high_select) {
                                    readType.type = 1;
                                } else if (checkedId == R.id.low_select) {
                                    readType.type = 2;
                                }
                            }
                        });

                        if (currentAttribute.getRankId() == 0) {
                            radioGroup.check(R.id.none_select);
                        } else if (currentAttribute.getRankId() == 1) {
                            radioGroup.check(R.id.high_select);
                        } else if (currentAttribute.getRankId() == 2) {
                            radioGroup.check(R.id.low_select);
                        }

                        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                database.updateTypeAttributeById(
                                        currentAttribute.getTypeId(),
                                        currentAttribute.getId(),
                                        readType.type);
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
            } else if (currentAttribute.getStatId() == 3) {
                value.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        LayoutInflater inflater = activity.getLayoutInflater();
                        View dialogView = inflater.inflate(R.layout.rank_dialog_bool, null);

                        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AlertDialogStyle);
                        builder.setView(dialogView);

                        final TextView dialogTitle = (TextView) dialogView.findViewById(R.id.dialog_title_text);
                        dialogTitle.setText("Set Target");
                        final RadioGroup radioGroup = (RadioGroup) dialogView.findViewById(R.id.radio_group);

                        class ReadType {
                            public int type = 0;
                        }

                        final ReadType readType = new ReadType();

                        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(RadioGroup group, int checkedId) {
                                if (checkedId == R.id.none_select) {
                                    readType.type = 0;
                                } else if (checkedId == R.id.high_select) {
                                    readType.type = 1;
                                } else if (checkedId == R.id.low_select) {
                                    readType.type = 2;
                                }
                            }
                        });

                        if (currentAttribute.getRankId() == 0) {
                            radioGroup.check(R.id.none_select);
                        } else if (currentAttribute.getRankId() == 1) {
                            radioGroup.check(R.id.high_select);
                        } else if (currentAttribute.getRankId() == 2) {
                            radioGroup.check(R.id.low_select);
                        }

                        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                database.updateTypeAttributeById(
                                        currentAttribute.getTypeId(),
                                        currentAttribute.getId(),
                                        readType.type);
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
                            database.updateTypeAttributeById(typeId, currentAttribute.getId(), currentAttribute.getStatId(), dialogEdit.getText().toString(), currentAttribute.getValue());
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
                            database.deleteTypeAttributeById(typeId, currentAttribute.getId());
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
                                database.updateTypeAttributeById(
                                        currentAttribute.getTypeId(),
                                        currentAttribute.getId(),
                                        readType.type,
                                        currentAttribute.getTitle(),
                                        currentAttribute.getValue());
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
                            database.deleteTypeAttributeById(typeId, currentAttribute.getId());
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