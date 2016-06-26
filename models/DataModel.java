package com.brandon.apps.groupstudio.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Brandon on 11/27/2015.
 */
public class DataModel {
    public String Password;
    public String Target;
    public GroupModel GroupData;

    public DataModel(String _password, String _target, GroupModel model) {
        Password = _password;
        Target = _target;
        GroupData = model;
    }
}
