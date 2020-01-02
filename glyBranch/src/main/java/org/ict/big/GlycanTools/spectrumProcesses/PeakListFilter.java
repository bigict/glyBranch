package org.ict.big.GlycanTools.spectrumProcesses;

import org.ict.big.GlycanTools.debug.Print;
import org.ict.big.GlycanTools.options.ScoringConfig;
import org.ict.big.GlycanTools.spectrumModel.Peak;
import org.ict.big.GlycanTools.spectrumModel.PeakList;

import java.util.ArrayList;
import java.util.Collections;

public class PeakListFilter {
    ScoringConfig config;
//    public static ArrayList<String> tempPrint = new ArrayList<>();
    public PeakListFilter(ScoringConfig scoringConfig){
        this.config = scoringConfig;
    }

    public ArrayList<PeakList> filterNoisedPeakList(ArrayList<PeakList> originalPeakLists){
        ArrayList<PeakList> ret = new ArrayList<>();
        for(PeakList pl: originalPeakLists){
            if(isValidPeakList(pl)){
                ret.add(pl);
            }
        }
        return ret;
    }

    public ArrayList<PeakList> filterNoisedPeakListBeforePeakFiltering(ArrayList<PeakList> originalPeakLists){
        ArrayList<PeakList> ret = new ArrayList<>();
        for(PeakList pl: originalPeakLists){
            if(isValidPeakListBeforePeakFiltering(pl)){
                ret.add(pl);
            }
        }
        return ret;
    }

    public boolean isValidPeakListBeforePeakFiltering(PeakList peakList) {
        boolean ret = true;
        if(ret){
            ret = ret && isNumberOfPeaksValidPeakList(peakList);
        }
//        if (ret) {
//            ret = ret && isBaselineValidPeakList(peakList);
//        }
//        Print.pl(peakList.toTitleString()+"\tvalid:"+ret);
        return ret;
    }

    public boolean isValidPeakList(PeakList peakList){
        boolean ret = true;
        if(ret){
            ret = ret && isNumberOfPeaksValidPeakList(peakList);
        }
//        if(ret){
//            ret = ret && isBaselineValidPeakList(peakList);
//        }
        if(ret){
            ret = ret && isIsotopicValidPeakList(peakList);
        }
        return ret;
    }

    public boolean isNumberOfPeaksValidPeakList(PeakList peaks){
        return peaks.size() >= config.minNumberOfPeaks;
    }

    public boolean isBaselineValidPeakList(PeakList peakList) {
        double sumTop3 = 0;
        ArrayList<Double> allIntensities = new ArrayList<>();
        for(Peak p: peakList){
            allIntensities.add(p.getRelativeIntensity());
        }
        Collections.sort(allIntensities);
        for(int i = 1,n = allIntensities.size(); i <= 3 && n - i >= 0; i++){
            sumTop3 += allIntensities.get(n - i);
        }
        double avgOfTop3 = sumTop3 / 3;
        double median = allIntensities.get(allIntensities.size() / 2);
        Print.pl(peakList.toTitleString());
        Print.pl("\tbaseline: " + avgOfTop3 / median);
        return avgOfTop3 / median >= config.baselineMin;
    }
    public boolean isIsotopicValidPeakList(PeakList peakList){
        if(peakList.getLevel() == 1){
            return true;
        }
        labelIsotopicPeaks(peakList);
        int cntIsotopicGroup = 0;
        for(Peak p: peakList){
            if(p.getIsotopicMz() == 1){
                cntIsotopicGroup++;
            }
        }
//        Print.pl("cntIsotopicGroup: "+cntIsotopicGroup + " in "+ peakList.toTitleString());
        int min = peakList.getPrecursorMass() > 1500 ?
                config.minNumberOfIsotopicPeakGroupsMoreThan1500:
                config.minNumberOfIsotopicPeakGroupsLessThan1500;

//        String s = peakList.getPrecursorMass() + "\t" + cntIsotopicGroup + "\t" + peakList.getPrecursorMasses();
//        if(peakList.getLevel() <= 2) tempPrint.add(s);
        return (cntIsotopicGroup >= min);
    }

    public void labelIsotopicPeaks(PeakList peakList){
        for(int i = 0;i< peakList.size();i++){
            Peak p = peakList.getPeak(i);
            if(p.getIsotopicMz() == 0){
                ArrayList<Peak> isoPeaks = findContinousIsotopicPeaksForward(peakList, i);
                for(Peak ip: isoPeaks){
                    ip.setIsotopicMz(Math.round(ip.getMz() - p.getMz()));
                }
            }
        }
    }
    public ArrayList<Peak> findContinousIsotopicPeaksForward(PeakList peakList, int i){
        ArrayList<Peak> isoPeaks = new ArrayList<>();
        double peakMz = peakList.getPeak(i).getMz();
        int k = 1;
        int k_upper = 5;
        for(int j = i+1;j < peakList.size() && k <= k_upper;j++){
            double mz = peakList.getPeak(j).getMz();
            double diff = mz - peakMz;
            if(diff > k - config.isotopicTolerence
                    && diff < k + config.isotopicTolerence){
                isoPeaks.add(peakList.getPeak(j));
                k++;
            }
            if(Math.abs(mz - peakMz) > k_upper + 1){
                break;
            }
        }
        return isoPeaks;
    }
}
