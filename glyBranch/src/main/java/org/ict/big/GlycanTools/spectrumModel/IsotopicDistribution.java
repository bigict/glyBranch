package org.ict.big.GlycanTools.spectrumModel;

import java.util.ArrayList;

public class IsotopicDistribution extends ArrayList<Double> {

    int maxIntensedIndex = 0;

    public IsotopicDistribution(){
        super();
    }

    public IsotopicDistribution(ArrayList<Double> dis){
        super(dis);
        double max = -1;
        int maxI = -1;

        for(int i = 0;i < dis.size(); i++){
            if(dis.get(i) > max){
                max = dis.get(i);
                maxI = i;
            }
        }
        this.maxIntensedIndex = maxI;
    }

    public int getMaxIntensedIndex() {
        return maxIntensedIndex;
    }

//    public void setMaxIntensedIndex(int maxIntensedIndex) {
//        this.maxIntensedIndex = maxIntensedIndex;
//    }

    public void normalize(){
        double sum = 0;
        for(Double d: this){
            sum += d;
        }
        for(int i = 0;i < this.size();i++){
            this.set(i, this.get(i) / sum);
        }
    }

    @Override
    public Object clone() {
        IsotopicDistribution iso = new IsotopicDistribution();
        for(Double d: this){
            iso.add(d.doubleValue());
            iso.maxIntensedIndex = this.maxIntensedIndex;
        }
        return iso;
    }
}
