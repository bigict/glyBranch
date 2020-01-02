package org.ict.big.GlycanTools.spectrumModel;

import org.eurocarbdb.application.glycanbuilder.FragmentCollection;
import org.eurocarbdb.application.glycanbuilder.FragmentEntry;

import java.util.Collection;

public class PeakWithAnnotation extends Peak {

    protected FragmentCollection annotation;

    public PeakWithAnnotation(){
        super();
        annotation = new FragmentCollection();
    }

    public PeakWithAnnotation(double _mz, double _int){
        super(_mz, _int);
        annotation = new FragmentCollection();
    }

    public PeakWithAnnotation(double _mz, double _int, double _rint){
        super(_mz, _int, _rint);
        annotation = new FragmentCollection();
    }

    public PeakWithAnnotation(Peak p){
        super(p);
        annotation = new FragmentCollection();
    }

    public PeakWithAnnotation(PeakWithAnnotation peakWithAnnotation){
        super(peakWithAnnotation);
        this.annotation = peakWithAnnotation.annotation.clone();
    }

    public PeakWithAnnotation(Peak p, FragmentCollection fc){
        super(p);
        annotation = fc;
    }

    public PeakWithAnnotation clone(){
        PeakWithAnnotation clone = new PeakWithAnnotation(this);
        return clone;
    }

    public FragmentCollection getAnnotation() {
        return annotation;
    }

    public void setAnnotation(FragmentCollection fragments) {
        this.annotation = fragments;
    }

    public Collection<FragmentEntry> getAnnotatedFragments() {
        return annotation.getFragments();
    }


    @Override
    public String toString() {
        return super.toString() + "\tannotation: " + annotation.size();
    }
    public String toStringWithDetailedAnnotation(){
        String ret = toString() + "\n";
        IsotopicPeakDistributionCaculator iso = new IsotopicPeakDistributionCaculator(0.01);
        for(FragmentEntry f: annotation.getFragments()){
            ret += "\t\t" + f.structure + "\n";
            ret += "\t\t" + iso.computeIsotopicDistributionCarbonOnly(f.fragment) + "\n";
        }
        return ret.substring(0, ret.length()-1);
    }
}
