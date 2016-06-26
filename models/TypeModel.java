package com.brandon.apps.groupstudio.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Brandon on 11/27/2015.
 */
public class TypeModel {
    public int TypeId;
    public String TypeName;
    public List<AttributeModel> AttributeList;
    public List<CalculationModel> CalculationList;

    public TypeModel(int id, String name) {
        TypeId = id;
        TypeName = name;
        AttributeList = new ArrayList<AttributeModel>();
        CalculationList = new ArrayList<CalculationModel>();
    }
}
