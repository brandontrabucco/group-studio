package com.brandon.apps.groupstudio.models;

/**
 * Created by Brandon on 11/27/2015.
 */
public class AttributeModel {
    public int AttributeId;
    public int AttributeUniversalId;
    public int AttributeType;
    public int AttributeStat;
    public int AttributeRank;
    public String AttributeTitle;
    public String AttributeValue;

    public AttributeModel(int id, int uId, int type, int stat, int rank, String title, String value) {
        AttributeId = id;
        AttributeUniversalId = uId;
        AttributeType = type;
        AttributeStat = stat;
        AttributeRank = rank;
        AttributeTitle = title;
        AttributeValue = value;
    }

    public AttributeModel(int id, int uId, int type, int stat, String title, String value) {
        AttributeId = id;
        AttributeUniversalId = uId;
        AttributeType = type;
        AttributeStat = stat;
        AttributeRank = 0;
        AttributeTitle = title;
        AttributeValue = value;
    }
}
