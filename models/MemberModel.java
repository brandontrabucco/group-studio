package com.brandon.apps.groupstudio.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Brandon on 11/27/2015.
 */
public class MemberModel {
    public int MemberId;
    public int MemberType;
    public String MemberName;
    public List<AttributeModel> AttributeList;

    public MemberModel(int id, int type, String name) {
        MemberId = id;
        MemberType = type;
        MemberName = name;
        AttributeList = new ArrayList<AttributeModel>();
    }
}
