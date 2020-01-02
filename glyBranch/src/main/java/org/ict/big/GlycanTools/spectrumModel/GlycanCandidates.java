package org.ict.big.GlycanTools.spectrumModel;

import org.ict.big.GlycanTools.spectrumModel.structureLibrary.CarbohydrateRecord;

import java.util.ArrayList;
import java.util.Collections;

/**
 * This class represents all glycan candidates
 */
public class GlycanCandidates extends ArrayList<GlycanCandidate> {

    public GlycanCandidates(){
        super();
    }

    public GlycanCandidates(ArrayList<CarbohydrateRecord> records){
        super();
        for(CarbohydrateRecord record: records){
            GlycanCandidate candidate = new GlycanCandidate(record);
            this.add(candidate);
        }
    }

    public void normalizeScores(){
        double sum = 0;
        for(GlycanCandidate candidate: this){
            sum += candidate.getScore();
        }
        for(GlycanCandidate candidate: this){
            candidate.setScore(candidate.getScore() / sum);
        }
    }



    /**
     * Find candidate by its ID.
     * @param id
     * @return Glycan candidate object with the same id, or null if this does not
     */
    public GlycanCandidate findCandidateById(String id){
        for(GlycanCandidate candidate: this){
            if(id.equals(candidate.record.ID)){
                return candidate;
            }
        }
        return null;
    }

    /**
     * get the rank of score of the candidate by its ID.
     * @param id
     * @return the rank of the candidate wanted, or -1 if the candidate do not exist
     */
    public int getRankOfACandidateById(String id){
        ArrayList<GlycanCandidate> newArray = new ArrayList<>(this);
        Collections.sort(newArray, new GlycanCandidateScoreComparator());
        int index = -1;
        double score = 0;
        for(int i = 0;i < newArray.size();i++){
            if(id.equals(newArray.get(i).record.ID)){
                index = i;
                score = newArray.get(i).score;
                break;
            }
        }
        if(index < 0){
            return -1;
        }
        int upperIndex = newArray.size();
        for(int i = index + 1; i< newArray.size(); i++){
            if(newArray.get(i).score - score >= 1E-4){
                upperIndex = i;
            }
        }
        upperIndex--;
        return newArray.size() - upperIndex;
    }

    /**
     * get the secondly largest score in all candidates
     * @return the score of the candidate, or -1 is there is only 1 candidate
     */
    public double getSecondlyLargestScore(){
        ArrayList<GlycanCandidate> newArray = new ArrayList<>(this);
        Collections.sort(newArray, new GlycanCandidateScoreComparator());
        double score = -1;
        if(newArray.size() >= 2){
            score = newArray.get(newArray.size() - 2).score;
        }
        return score;
    }

    @Override
    public String toString() {
        return  toString("");
    }

    public String toString(String prefix){
        String ret = "";
        for(GlycanCandidate candidate: this){
            ret += prefix + candidate.toString() + "\n";
        }
        return ret;
    }


}
