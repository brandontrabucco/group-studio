package com.brandon.apps.groupstudio.assets;

import android.content.Context;

import java.util.List;

/**
 * Created by Brandon on 4/12/2015.
 */
public class DefaultMember implements Comparable<DefaultMember> {
    private String name;
    private int id, typeId, groupId;
    private double weight = 0;
    public DefaultMember(int _id, String _name, int _typeId, int _groupId) {
        id = _id;
        name = _name;
        typeId = _typeId;
        groupId = _groupId;

    }
    public String getName() {
        return name;
    }
    public static String[] getTypeList(Context c) {
        DatabaseAdapter database = new DatabaseAdapter(c);
        database.open();
        List<DefaultType> typeList = database.getAllTypes();
        database.close();

        String[] typeNameList = new String[typeList.size() + 1];
        typeNameList[0] = "Empty Type";
        for(int i = 0; i < typeList.size(); i++) {
            typeNameList[i + 1] = typeList.get(i).getName();
        }
        return typeNameList;
    }
    public int getTypeId() {
        return typeId;
    }
    public int getId() {
        return id;
    }
    public int getGroupId() { return groupId; }
    public void setName(String _name) {
        name = _name;
    }
    public void setTypeId(int _typeId) {
        typeId = _typeId;
    }
    public void setGroupId(int _groupId) {
        groupId = _groupId;
    }
    public void setId(int _id) {
        id = _id;
    }
    public double getWeight() {
        return weight;
    }
    public void addWeight(double weight) {
        this.weight += weight;
    }
    public void clearWeight() {
        this.weight = 0;
    }

    @Override
    public int compareTo(DefaultMember comparison) {
        double weight = comparison.getWeight();
        return (int)Math.round((weight - this.weight) * 1000);
    }
}
