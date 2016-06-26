package com.brandon.apps.groupstudio.models;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Brandon on 11/27/2015.
 */
public class GroupModel {
    public int GroupId;
    public String GroupName;
    public String GroupDesc;
    public List<MemberModel> MemberList;
    public List<TypeModel> TypeList;

    public GroupModel(int id, String name, String desc) {
        GroupId = id;
        GroupName = name;
        GroupDesc = desc;
        MemberList = new ArrayList<MemberModel>();
        TypeList = new ArrayList<TypeModel>();
    }
}
