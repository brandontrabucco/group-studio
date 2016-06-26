package com.brandon.apps.groupstudio.assets;

import android.content.Context;

import java.util.List;

/**
 * Created by Brandon on 4/12/2015.
 */
public class DefaultType {
    private int id;
    private String name;
    public DefaultType(int _id, String _name){
        super();
        id = _id;
        name = _name;
    }
    public void setId(int _id) {
        id = _id;
    }
    public int getId(){ return id; }
    public void setName(String _name) {
        name = _name;
    }
    public String getName() {
        return name;
    }
    public static String[] getTargetList(Context c, int _id, boolean addType) {
        DatabaseAdapter database = new DatabaseAdapter(c);
        database.open();
        List<DefaultAttribute> targetList = database.getAllNumericTypeAttributes(_id);
        database.close();

        String[] targetNameList = new String[targetList.size() + 1];
        targetNameList[0] = "Empty Attribute";
        for(int i = 0; i < targetList.size(); i++) {
            targetNameList[i + 1] = targetList.get(i).getTitle();
            if (addType){
                targetNameList[i + 1] += " (" + DefaultAttribute.getTypeList()[targetList.get(i).getStatId()] + ")";
            }
        }
        return targetNameList;
    }
}
