package org.ict.big.GlycanTools.spectrumModel;

import org.eurocarbdb.application.glycanbuilder.FragmentCollection;
import org.eurocarbdb.application.glycanbuilder.FragmentEntry;

import java.util.ArrayList;
import java.util.Vector;

/**
 * This class represents the annotation of one peak with only one glycan candidate.
 */
public class MultiAnnotationsEntry {

    GlycanCandidate candidate;

//    double currentScore;
//    ArrayList<Peak>

    FragmentCollection fragments;
    ArrayList<IsotopicDistribution> isotopicDistributions;
    ArrayList<Double> maxIntensedIsoMz;

    private MultiAnnotationsEntry(){}

    public MultiAnnotationsEntry(GlycanCandidate candidate){
        this.candidate = candidate;
        isotopicDistributions = new ArrayList<>();
        maxIntensedIsoMz = new ArrayList<>();
        fragments = new FragmentCollection();
    }

    public GlycanCandidate getCandidate() {
        return candidate;
    }

    public void setCandidate(GlycanCandidate candidate) {
        this.candidate = candidate;
    }

    public FragmentCollection getFragments() {
        return fragments;
    }

    public void setFragments(FragmentCollection fragments) {
        this.fragments = fragments;
    }

    public int size(){
        return fragments.size();
    }

    public ArrayList<IsotopicDistribution> getIsotopicDistributions() {
        return isotopicDistributions;
    }

    public void setIsotopicDistributions(ArrayList<IsotopicDistribution> isotopicDistributions) {
        this.isotopicDistributions = isotopicDistributions;
    }

    public ArrayList<Double> getMaxIntensedIsoMz() {
        return maxIntensedIsoMz;
    }

    public void setMaxIntensedIsoMz(ArrayList<Double> maxIntensedIsoMz) {
        this.maxIntensedIsoMz = maxIntensedIsoMz;
    }

    public void initialParentMoleculeAnnotation(){
        this.fragments.addFragment(candidate.record.glycan.clone(), "ParentMolecule");
        generateIsotopicDistribution();
    }

    public void generateIsotopicDistribution(){
        IsotopicPeakDistributionCaculator isoCalulator = new IsotopicPeakDistributionCaculator(0.1);
        isotopicDistributions.clear();
        maxIntensedIsoMz.clear();
        for(FragmentEntry fragment: this.fragments.getFragments()){
            IsotopicDistribution iso = isoCalulator.computeIsotopicDistributionCarbonOnly(fragment.fragment);
            isotopicDistributions.add(iso);
            maxIntensedIsoMz.add(iso.maxIntensedIndex + fragment.getMZ());
        }
    }

    public boolean addFragment(FragmentEntry fragment,
                               IsotopicDistribution isotopicDistribution, double maxIntMz){
        boolean success = this.fragments.addFragment(fragment);
        if(success){
            isotopicDistributions.add(isotopicDistribution);
            maxIntensedIsoMz.add(maxIntMz);
        }
        return success;
    }

    public MultiAnnotationsEntry generateAnnotationsInARange(double mzLeft, double mzRight){
        MultiAnnotationsEntry newMae = new MultiAnnotationsEntry(this.candidate);
        ArrayList<FragmentEntry> allFragments = (ArrayList<FragmentEntry>) this.fragments.getFragments();
        for(int i = 0; i < maxIntensedIsoMz.size(); i++){
            double mz = maxIntensedIsoMz.get(i);
            if(mz >= mzLeft && mz <= mzRight){
                newMae.fragments.addFragment(allFragments.get(i));
                newMae.isotopicDistributions.add(this.isotopicDistributions.get(i));
                newMae.maxIntensedIsoMz.add(mz);
            }
        }
        return newMae;
    }

    public void merge(MultiAnnotationsEntry entryToBeMerged){
        if(!this.candidate.equals(entryToBeMerged.candidate)){
            return;
        }
        Vector<FragmentEntry> allFragmentsToBeMerged = (Vector<FragmentEntry>) entryToBeMerged.fragments.getFragments();
        for(int i = 0; i < allFragmentsToBeMerged.size();i++){
            this.addFragment(allFragmentsToBeMerged.get(i), entryToBeMerged.isotopicDistributions.get(i),
                    entryToBeMerged.maxIntensedIsoMz.get(i));
        }
    }

    public Object clone(){
        MultiAnnotationsEntry mae = new MultiAnnotationsEntry();
        mae.candidate = this.candidate;
        mae.fragments = this.fragments.clone();
        mae.isotopicDistributions = new ArrayList<>();
        for(IsotopicDistribution iso: this.isotopicDistributions){
            mae.isotopicDistributions.add((IsotopicDistribution) iso.clone());
        }
        mae.maxIntensedIsoMz = new ArrayList<>();
        for(Double d: this.maxIntensedIsoMz){
            mae.maxIntensedIsoMz.add(d.doubleValue());
        }
        return mae;
    }

    @Override
    public String toString() {
        String ret = candidate.record.ID + "\t" + fragments;
        return ret;
    }
}
