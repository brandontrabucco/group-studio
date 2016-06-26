package com.brandon.apps.groupstudio.assets;

/**
 * Created by Brandon on 9/16/2015.
 */
public class Calculation {
    private String name;
    private int id, targetId, statId;
    public Calculation(int _id, int _targetId, int _statId, String _name) {
        id = _id;
        targetId = _targetId;
        statId = _statId;
        name = _name;
    }
    public int getId() {
        return id;
    }
    public int getTargetId() {
        return targetId;
    }
    public int getStatId() {
        return statId;
    }
    public String getName() {
        return name;
    }
    public void setId(int _id) {
        id = _id;
    }
    public void setTargetId(int _targetId) {
        targetId = _targetId;
    }
    public void setStatId(int _statId) {
        statId = _statId;
    }
    public void setName(String _name) {
        name = _name;
    }
    public double getCalculation(double[] data) {
        return StatisticMath.calculate(statId, data);
    }
}
