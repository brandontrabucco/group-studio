package com.brandon.apps.groupstudio.assets;

import org.nfunk.jep.JEP;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Brandon on 9/13/2015.
 */
public class StatisticMath {

    public static double eval(String expression) {
        JEP expressionParser = new JEP();
        expressionParser.addStandardFunctions();
        expressionParser.addStandardConstants();
        expressionParser.addFunction("average", new AverageFunction());
        expressionParser.addFunction("median", new MedianFunction());
        expressionParser.addFunction("range", new RangeFunction());
        expressionParser.addFunction("max", new MaxFunction());
        expressionParser.addFunction("min", new MinFunction());

        expressionParser.parseExpression(expression);
        return expressionParser.getValue();
    }

    public static double[] parseString(String s) {
        String[] items = s.trim().replaceAll("\\s","").split(",");
        double[] result = new double[items.length];
        for (int i = 0; i < items.length; i++) {
            try {
                result[i] = StatisticMath.eval(items[i]);
            } catch (NumberFormatException nfe) {
                // not a number
                result[i] = 0;
            }
        }
        return result;
    }

    public static double average(double[] n) {
        double sum = 0;
        for (int i = 0; i < n.length; i++) {
            sum += n[i];
        }
        return sum/n.length;
    }

    public static double sum(double[] n) {
        double sum = 0;
        for (int i = 0; i < n.length; i++) {
            sum += n[i];
        }
        return sum;
    }

    public static double median(double[] n) {
        Arrays.sort(n);
        if ((n.length & 1) == 0) {
            return (n[n.length / 2 - 1] + n[n.length / 2])/2;
        } else {
            return n[(int)Math.floor(n.length / 2)];
        }
    }

    public static double range(double[] n) {
        if (n.length != 0) {
            Arrays.sort(n);
            double max = n[n.length - 1], min = n[0];
            return max - min;
        } else {
            return 0;
        }
    }

    public static double max(double[] n) {
        if (n.length != 0) {
            Arrays.sort(n);
            return n[n.length - 1];
        } else {
            return 0;
        }
    }

    public static double min(double[] n) {
        if (n.length != 0) {
            Arrays.sort(n);
            return n[0];
        } else {
            return 0;
        }
    }

    public static double calculate(int statId, double[] n) {
        if (n.length == 0) {
            return 0;
        }
        if(statId == 1) {
            return StatisticMath.round(average(n));
        } else if (statId == 2) {
            return StatisticMath.round(median(n));
        } else if (statId == 3) {
            return StatisticMath.round(range(n));
        } else if (statId == 4) {
            return StatisticMath.round(sum(n));
        } else if (statId == 5) {
            return StatisticMath.round(max(n));
        } else if (statId == 6) {
            return StatisticMath.round(min(n));
        } else {
            return 0;
        }
    }

    public static double round(double d) {
        double result = (double)Math.round(d * 100) / 100;
        return result;
    }

    public static String[] getStatList() {

        String[] typeNameList = new String[7];
        typeNameList[0] = "Empty Calculation";
        typeNameList[1] = "Average";
        typeNameList[2] = "Median";
        typeNameList[3] = "Range";
        typeNameList[4] = "Sum";
        typeNameList[5] = "Maximum";
        typeNameList[6] = "Minimum";
        return typeNameList;
    }
}
