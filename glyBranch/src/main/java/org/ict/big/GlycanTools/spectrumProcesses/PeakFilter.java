package org.ict.big.GlycanTools.spectrumProcesses;

import org.ict.big.GlycanTools.debug.Print;
import org.ict.big.GlycanTools.options.ScoringConfig;
import org.ict.big.GlycanTools.spectrumModel.Peak;
import org.ict.big.GlycanTools.spectrumModel.PeakList;

import java.util.ArrayList;

/**
 * This class is used to filter peaks.
 * @author Jingwei Zhang
 */
public class PeakFilter {

    double filterRatio = 0.01;

    ScoringConfig config;

    public PeakFilter(){

    }

    public PeakFilter(ScoringConfig config){
        this.config = config;
        filterRatio = config.defaultFilterRatio;
    }

    public PeakFilter(double _filterRatio){
        this.filterRatio = _filterRatio;
    }

    public double getFilterRatio() {
        return filterRatio;
    }

    public void setFilterRatio(double filterRatio) {
        this.filterRatio = filterRatio;
    }

    public double autoSetFilterRatio(PeakList peakList){
        double ratio = 0;
        double mu = 0;
        double sigma = 0;
        if(peakList.getLevel() >= 2){
            ArrayList<Peak> noisedPeaks = new ArrayList<>();
            for(Peak p: peakList){
                if(p.getMz() < config.minMz || p.getMz() > peakList.getPrecursorMass() + 5.0){
                    noisedPeaks.add(p);
                    mu += p.getRelativeIntensity();
                }
            }
            mu /= noisedPeaks.size();
            for(Peak p: noisedPeaks){
//                ratio = Math.max(ratio, p.getRelativeIntensity());
                sigma += (p.getRelativeIntensity() - mu) * (p.getRelativeIntensity() - mu);
            }
            sigma /= noisedPeaks.size();
            sigma = Math.sqrt(sigma);
            ratio = mu + 3 * sigma;
            ratio /= 10;
            ratio += 0.001;
            if(noisedPeaks.size() == 0){
                ratio = 0;
            }
        }
        ratio = Math.max(ratio, config.minFilterRatio);
        filterRatio = ratio;
        return  ratio;
    }

    public PeakList filter(PeakList original){
        PeakList pl = original.cloneWithoutPeaks();
        for(Peak p: original.getPeaks()){
            if(p.getRelativeIntensity() < 0){
                p.setRelativeIntensity(p.getRelativeIntensity() / original.getMaxIntensity() * 10.);
            }
            if(p.getRelativeIntensity() >= filterRatio * 10.){
                pl.addPeak(p.clone());
            }
        }
        return pl;
    }

    public void filterInPlace(PeakList peakList){
        ArrayList<Peak> peaks = new ArrayList<Peak>();
        for(Peak p: peakList.getPeaks()){
            if(p.getRelativeIntensity() < 0){
                p.setRelativeIntensity(p.getAbsoluteIntensity() / peakList.getMaxIntensity() * 10.);
            }
            if(p.getRelativeIntensity() >= filterRatio * 10.){
                peaks.add(p.clone());
            }
        }
        peakList.setPeaks(peaks);
    }
}
