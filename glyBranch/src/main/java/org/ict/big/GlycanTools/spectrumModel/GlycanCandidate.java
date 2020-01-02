package org.ict.big.GlycanTools.spectrumModel;

import org.ict.big.GlycanTools.spectrumModel.structureLibrary.CarbohydrateRecord;

import java.util.Map;

public class GlycanCandidate implements Comparable<GlycanCandidate> {

    public final CarbohydrateRecord record;

    public double score;


//    Map<String, Double> allPossibleMz;

    /**
     *
     */
    public double tempScore;

    public GlycanCandidate(CarbohydrateRecord record){
        this.record = record;
        this.score = 0;
        this.tempScore = 0;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

//    public void initTempScore(){
//        this.tempScore = 1.0;
//    }
    public double getTempScore() {
        return tempScore;
    }

    public void setTempScore(double tempScore) {
        this.tempScore = tempScore;
    }

//    public Map<String, Double> getAllPossibleMz() {
//        return allPossibleMz;
//    }
//
//    public void setAllPossibleMz(Map<String, Double> allPossibleMz) {
//        this.allPossibleMz = allPossibleMz;
//    }


    @Override
    public int compareTo(GlycanCandidate o) {
        return this.record.compareTo(o.record);
    }

    public boolean equals(Object other) {
        if(other == null || !(other instanceof GlycanCandidate)){
            return false;
        } else {
            return this.record.equals(((GlycanCandidate) other).record);
        }
    }

    @Override
    public String toString() {
        return record.toString() + "\t" + String.format("%.4f", score);
//        return record.toStringOnlyIdAndMass() + "\t" + String.format("%.4f", score); //Double.toString(score)
    }
}
