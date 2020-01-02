package org.ict.big.GlycanTools.spectrumProcesses;

import org.ict.big.GlycanTools.annotation.denovo.ConstantMass;
import org.ict.big.GlycanTools.options.ProgramSettings;
import org.ict.big.GlycanTools.spectrumModel.Peak;
import org.ict.big.GlycanTools.spectrumModel.PeakList;

import java.util.ArrayList;

public class SpectrumPreprocessor {


    public SpectrumPreprocessor(){}

    public void prePorcess(PeakList peakList){
        addSupportingInformation(peakList);
        labelIsotopicPeaks(peakList);
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
            if(diff > k - ProgramSettings.isotopicTolerence
                    && diff < k + ProgramSettings.isotopicTolerence){
                isoPeaks.add(peakList.getPeak(j));
                k++;
            }
            if(Math.abs(mz - peakMz) > k_upper + 1){
                break;
            }
        }
        return isoPeaks;
    }

    public void addSupportingInformation(PeakList peakList){
        //Isotopic
        for(int i = 0;i< peakList.size();i++){
            ArrayList<Peak> isopeaks = findIsotopicPeaks(peakList, i);
            peakList.getPeak(i).addSuppordtingInformation(isopeaks.size()>0?"1":"0");
        }

        // plus 18 peaks
        for(int i = 0;i< peakList.size();i++){
            ArrayList<Peak> plus18 = findPeaks(peakList, i,
                    ConstantMass.removeMassWithoutO - ConstantMass.removeMassWithO);
            peakList.getPeak(i).addSuppordtingInformation(plus18.size()>0?"1":"0");
        }

        // minus 18 peaks
        for(int i = 0;i< peakList.size();i++){
            ArrayList<Peak> plus18 = findPeaks(peakList, i,
                    -(ConstantMass.removeMassWithoutO - ConstantMass.removeMassWithO));
            peakList.getPeak(i).addSuppordtingInformation(plus18.size()>0?"1":"0");
        }

        // complementary peaks

        for(int i = 0;i< peakList.size();i++){
            double parentMz = peakList.getPrecursorMass();
            ArrayList<Peak> plus18 = findPeaks(peakList, i,
                    parentMz - peakList.getPeak(i).getMz() + ConstantMass.Na);
            peakList.getPeak(i).addSuppordtingInformation(plus18.size()>0?"1":"0");
        }

    }

    public ArrayList<Peak> findIsotopicPeaks(PeakList peakList, int i){
        ArrayList<Peak> isoPeaks = new ArrayList<>();
        double peakMz = peakList.getPeak(i).getMz();
        for(int j = i+1;j< peakList.size();j++){
            double mz = peakList.getPeak(j).getMz();
            if(hasIsotopic(mz, peakMz)){
                isoPeaks.add(peakList.getPeak(j));
            }
            if(Math.abs(mz - peakMz) > 5){
                break;
            }
        }
        for(int j = i-1;j>=0;j--){
            double mz = peakList.getPeak(j).getMz();
            if(hasIsotopic(mz, peakMz)){
                isoPeaks.add(peakList.getPeak(j));
            }
            if(Math.abs(mz - peakMz) > 5){
                break;
            }
        }
        return isoPeaks;
    }
    public boolean hasIsotopic(double a, double b){
        double diff = Math.abs(a-b);
        double[] isoAllowed = {1.};
        for(double iso: isoAllowed){
            if(diff > iso - ProgramSettings.isotopicTolerence
                    && diff < iso + ProgramSettings.isotopicTolerence){
                return true;
            }
        }
        return false;
    }

    public ArrayList<Peak> findPeaks(PeakList peakList, int i, double diff) {
        ArrayList<Peak> matchedPeaks = new ArrayList<>();
        double peakMz = peakList.getPeak(i).getMz();
        if(diff > 0) {
            for (int j = i + 1; j < peakList.size(); j++) {
                double mz = peakList.getPeak(j).getMz();
                if (isMatched(mz, peakMz + diff)) {
                    matchedPeaks.add(peakList.getPeak(j));
                }
                if (Math.abs(mz - peakMz) > diff + 3) {
                    break;
                }
            }
        } else {
            for (int j = i - 1; j >=0; j--) {
                double mz = peakList.getPeak(j).getMz();
                if (isMatched(mz, peakMz + diff)) {
                    matchedPeaks.add(peakList.getPeak(j));
                }
                if (Math.abs(mz - peakMz) < diff - 3) {
                    break;
                }
            }
        }
        return matchedPeaks;
    }
    public boolean isMatched(double a, double b){
        double diff = Math.abs(a-b);
        if(diff < ProgramSettings.peakTolerence){
            return true;
        }
        return false;
    }
}
