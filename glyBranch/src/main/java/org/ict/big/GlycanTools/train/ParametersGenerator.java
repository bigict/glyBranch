package org.ict.big.GlycanTools.train;

import java.util.ArrayList;

/**
 * This class generates all possible values of a parameter in the scoring function.
 */
public class ParametersGenerator {

    public static ArrayList<Double> generateAlphaInTanh(double lower, double upper, double step){
        ArrayList<Double> allAlpha = new ArrayList<>();
        for(double d = lower; d < upper; d += step){
            allAlpha.add(d);
        }
        return allAlpha;
    }
}
