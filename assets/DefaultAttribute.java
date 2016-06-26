package com.brandon.apps.groupstudio.assets;

/**
 * Created by Brandon on 7/1/2015.
 */
public class DefaultAttribute {
    private int id, typeId, statId = 0, universalId = 0, rankId = 0;
    private static String[] TypeList = new String[] {
            // available types for values
            "Text",             // statId == 0
            "Number",           // statId == 1
            "Numeric List",     // statId == 2
            "Boolean",          // statId == 3
    };
    public static String[] getTypeList() { return TypeList; }
    private String title;
    private String value;
    public DefaultAttribute(int _id, int _type, String _title, String _value) {
        super();
        id = _id;
        typeId = _type;
        title = _title;
        value = _value;
    }
    public DefaultAttribute(int _id, int _type, int _rankId, String _title, String _value) {
        super();
        id = _id;
        typeId = _type;
        rankId = _rankId;
        title = _title;
        value = _value;
    }
    public int getId() { return id; }
    public void setId(int _id) { id = _id; }
    public int getTypeId() {
        return typeId;
    }
    public void setTypeId(int t) {
        typeId = t;
    }
    public int getUniversalId() {
        return universalId;
    }
    public void setUniversalId(int u) {
        universalId = u;
    }
    public int getStatId() {
        return statId;
    }
    public void setStatId(int s) {
        statId = s;
    }
    public int getRankId() { return rankId; }
    public void setRankId(int _rankId) { rankId = _rankId; }
    public String getTitle() {
        return title;
    }
    public void setTitle(String t) {
        title = t;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String v) { value = v; }
    public double[] getCalculation() {
        if (statId == 1) {
            // is a single number or equation
            return new double[]{StatisticMath.eval(value)};
        } else if (statId == 2) {
            // is a list of doubles
            return StatisticMath.parseString(value);
        } else if (statId == 3) {
            // is a yes / no boolean (1 or zero)
            double buffer;
            if (value.toLowerCase().trim().equals("yes") ||
                    value.toLowerCase().trim().equals("true") ||
                    value.toLowerCase().trim().equals("on") ||
                    value.toLowerCase().trim().equals("1"))
                buffer = 1;
            else if (value.toLowerCase().trim().equals("no") ||
                    value.toLowerCase().trim().equals("false") ||
                    value.toLowerCase().trim().equals("off") ||
                    value.toLowerCase().trim().equals("0"))
                buffer = 0;
            else
                buffer = 0;
            return new double[] { buffer };
        } else {
            return new double[] { 0 };
        }
    }
}
