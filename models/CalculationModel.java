package com.brandon.apps.groupstudio.models;

/**
 * Created by Brandon on 11/27/2015.
 */
public class CalculationModel {
    public int CalculationId;
    public int CalculationTarget;
    public int CalculationStat;
    public String CalculationName;

    public CalculationModel(int id, int target, int stat, String name) {
        CalculationId = id;
        CalculationTarget = target;
        CalculationStat = stat;
        CalculationName = name;
    }
}
