package com.brandon.apps.groupstudio.assets;

/**
 * Created by Brandon on 4/19/2015.
 */
public class DefaultGroup {
    private String name, desc;
    private int id;
    public DefaultGroup(int _id, String _name, String _desc){
        id = _id;
        name = _name;
        desc = _desc;
    }
    public int getId() { return id; }
    public void setId(int _id) { id = _id; }
    public String getName(){
        return name;
    }
    public String getDesc(){
        return desc;
    }
    public DefaultGroup setName(String _name){
        name = _name;
        return this;
    }
}
