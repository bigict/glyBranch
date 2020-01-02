package org.ict.big.GlycanTools.spectrumModel;

import org.eurocarbdb.application.glycanbuilder.FragmentCollection;
import org.ict.big.GlycanTools.debug.Print;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

public class PeakList implements Comparator<PeakList>, Iterable<Peak> {

    protected ArrayList<Peak> peakList;

    protected double maxIntensity = -1;

    protected ArrayList<Double> precursorMassList;

    public PeakList(){
        peakList = new ArrayList<Peak>();
        precursorMassList = new ArrayList<Double>(0);
    }

    public PeakList(PeakList pl){
        peakList = new ArrayList<Peak>();
        precursorMassList = new ArrayList<Double>(0);
        for(Peak p: pl){
            this.peakList.add(p.clone());
        }
        this.maxIntensity = pl.maxIntensity;
        for(Double d: pl.precursorMassList){
            this.precursorMassList.add(Double.valueOf(d.doubleValue()));
        }
    }

    public PeakList(Collection<Peak> peaks, Collection<Double> preMasses){
        peakList = new ArrayList<Peak>(peaks);
        precursorMassList = new ArrayList<Double>(preMasses);
        generateMaxAndRelativeIntensity();
    }

    public PeakList clone(){
        PeakList clone = new PeakList();
        for(Peak p: peakList){
            clone.peakList.add(p.clone());
        }
        clone.maxIntensity = this.maxIntensity;
        for(Double d: precursorMassList){
            clone.precursorMassList.add(Double.valueOf(d.doubleValue()));
        }
        return clone;
    }

    public PeakList cloneWithoutPeaks(){
        PeakList clone = new PeakList();
        clone.maxIntensity = this.maxIntensity;
        for(Double d: precursorMassList){
            clone.precursorMassList.add(Double.valueOf(d.doubleValue()));
        }
        return clone;
    }

    public ArrayList<Peak> getPeaks() {
        return peakList;
    }

    public double getMaxIntensity() {
        return maxIntensity;
    }

    public ArrayList<Double> getPrecursorMasses() {
        return precursorMassList;
    }

    public double getPrecursorMass(){
        return (precursorMassList == null || precursorMassList.size() == 0) ?
                -1. :precursorMassList.get(precursorMassList.size() - 1);
    }

    public int getLevel(){
        return precursorMassList.size() + 1;
    }

    public void setPeaks(ArrayList<Peak> peakList) {
        this.peakList = peakList;
    }

    public void setMaxIntensity(double maxIntensity) {
        this.maxIntensity = maxIntensity;
    }

    public void setPrecursorMasses(ArrayList<Double> precursorMassList) {
        this.precursorMassList = precursorMassList;
    }

    public int size(){ return peakList.size(); }

    public void addPeak(Peak p){
        peakList.add(p);
        maxIntensity = Math.max(maxIntensity, p.absoluteIntensity);
    }

    public void addPeak(double _mz, double _int){
        Peak p = new Peak(_mz, _int);
        peakList.add(p);
        maxIntensity = Math.max(maxIntensity, _int);
    }

    public Peak getPeak(int i){ return peakList.get(i); }

    public void generateMaxAndRelativeIntensity(){
        for(Peak p: peakList){
            maxIntensity = Math.max(maxIntensity, p.absoluteIntensity);
        }
        generateRelativeIntensity();
    }

    public void generateRelativeIntensity(){
        for(Peak p: peakList){
            p.setRelativeIntensity(p.getAbsoluteIntensity() / maxIntensity * 10.);
        }
    }

    public boolean remove(Peak p){ return peakList.remove(p); }
    public Peak remove(int index) { return peakList.remove(index); }

    public boolean annotatePeak(int peakIndex, FragmentCollection fragments){
        if(peakIndex < 0 || peakIndex > peakList.size()){
            return false;
        }
        if(peakList.get(peakIndex) instanceof PeakWithAnnotation){
            ((PeakWithAnnotation) peakList.get(peakIndex)).setAnnotation(fragments);
        } else { // The element is not an instance of PeakWithAnnotation
            Peak p = peakList.get(peakIndex);
            peakList.set(peakIndex, new PeakWithAnnotation(p, fragments));
        }
        return true;
    }

    public boolean annotatePeak(Peak p, FragmentCollection fragments){
        int peakIndex = peakList.indexOf(p);
        return annotatePeak(peakIndex, fragments);
    }

    public int indexOf(Peak p){
        return peakList.indexOf(p);
    }

    public Peak set(int index, Peak p){
        return peakList.set(index, p);
    }

    public Peak set(Peak oldPeak, Peak newPeak){
        int index = peakList.indexOf(oldPeak);
        return peakList.set(index, newPeak);
    }

    /**
     * This method returns all occurrences of a peak of which the mz
     * ratio is between mz-tolerance and mz+tolerance
     * @param mz the mz ration of the peak to be found
     * @param tolerance the tolerance of the searching process
     * @return the peak collection that satisfies the requirement.
     *         If no peak satisfies, a empty collection will return.
     */
    public ArrayList<Peak> findAllPeaks(double mz, double tolerance){
        ArrayList<Peak> ps = new ArrayList<Peak>();
        for(Peak p: peakList){
            if(p.getMz() < mz + tolerance && p.getMz() > mz - tolerance){
                ps.add(p);
            }
        }
        return ps;
    }

    /**
     * This method returns the first occurrence of a peak of which the mz
     * ratio is between mz-tolerance and mz+tolerance
     * @param mz the mz ration of the peak to be found
     * @param tolerance the tolerance of the searching process
     * @return the first peak that satisfies the requirement or null if no peak satisfies.
     */
    public Peak findPeak(double mz, double tolerance){
        for(Peak p: peakList){
            if(p.getMz() < mz + tolerance && p.getMz() > mz - tolerance){
                return p;
            }
        }
        return null;
    }

    /**
     * This method returns the max intensed peak in peaks of which the mz
     * ratio is between mz-tolerance and mz+tolerance.
     * @param mz the mz ration of the peak to be found
     * @param tolerance the tolerance of the searching process
     * @return the max intensed peak that satisfies the requirement or null if no peak satisfies.
     */
    public Peak findMaxPeak(double mz, double tolerance){
        ArrayList<Peak> satisfiedPeaks = findAllPeaks(mz, tolerance);
        Peak maxIntensedPeak = null;
        double maxIntense = 0.;
        for(Peak p: satisfiedPeaks){
            if(p.getAbsoluteIntensity() > maxIntense){
               maxIntense = p.getAbsoluteIntensity();
               maxIntensedPeak = p;
            }
        }
        return maxIntensedPeak;
    }

    /**
     * This function compares two PeakList objects.
     * Firstly, the one with lower MS level is smaller, eg. MS2_1579 < MS3_1579_1084
     * Secondly, if they have the same MS level, the one with lower precursor mass
     *           is smaller, eg, MS3_1579_839 < MS3_1579_1084,
     *                           MS4_1579_839_667 < MS4_1579_1084_4XX
     * @param oA
     * @param oB
     * @return
     */
    @Override
    public int compare(PeakList oA, PeakList oB) {
        if (oA.getLevel() > oB.getLevel()) {
            return 1;
        } else if (oA.getLevel() < oB.getLevel()) {
            return -1;
        } else { // oA.getLevel() == oB.getLevel()
            int l = oA.getLevel();
            for(int i = 0; i < l;i++){
                if(Math.abs(oA.getPrecursorMasses().get(i) - oB.getPrecursorMasses().get(i))
                        > 0.3){ // these two
                    if(oA.getPrecursorMasses().get(i) > oB.getPrecursorMasses().get(i)){
                        return 1;
                    } else {
                        return -1;
                    }
                }
            }
        }
        return 0;
    }

    /**
     * This mathod returns a String that fully presents this PeakList object.
     * @return
     */
    public String toString(){
        String ret = toTitleString() + "\n";
        for(Peak p: peakList){
            ret += "\t" + p + "\n";
        }
        return ret;
    }
    public String toStringOnlyAnnotation(){
        String ret = toTitleString() + "\n";
        for(Peak p: peakList){
            if(p instanceof PeakWithAnnotation){
                ret += "\t" + ((PeakWithAnnotation) p).toStringWithDetailedAnnotation() + "\n";
            }
        }
        return ret;
    }

    public String toPrecursorString(int decimalDigits){
        String dd = Integer.toString(decimalDigits);;
        String ret = "";
        for(int i = 0;i<precursorMassList.size();i++){
            if(i == 0) {
                ret += String.format("%."+dd+"f", precursorMassList.get(i));
            } else {
                ret += String.format("->%."+dd+"f", precursorMassList.get(i));
            }
        }
        return ret;
    }

    public String toTitleString(){
        String ret = "PeakList: " + size() + ", MS" + getLevel() + ": " + toPrecursorString(2);
//        for(int i = 0;i<precursorMassList.size();i++){
//            if(i == 0) {
//                ret += String.format("%.2f", precursorMassList.get(i));
//            } else {
//                ret += String.format("->%.2f", precursorMassList.get(i));
//            }
//        }
        ret += String.format(" maxIntensiy: %.2f", maxIntensity);
        return ret;
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @Override
    public Iterator<Peak> iterator() {
        return peakList.iterator();
    }

    public Iterator<Peak> getIterator(){
        return peakList.iterator();
    }
}
