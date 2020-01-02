package org.ict.big.GlycanTools.spectrumModel;

import org.ict.big.GlycanTools.debug.Print;

import java.util.ArrayList;
import java.util.Collection;

public class PeakListMultiAnnTree {

    /**
     * The top peak list node in this tree, ms1 or ms2.
     */
    protected PeakListMultiAnnNode topPeakListNode;

    /**
     * The number of nodes in this tree.
     */
    int size = 0;

    /**
     * Initialize a PeakListMultiAnnTree by a set of peak lists
     * @param plSortedCollection a sorted collection of peak lists
     * @param toleranceMs2 Mass tolerance of the precursor mass for MS2.
     * @param toleranceMsn Mass tolerance of the precursor mass for MSn.
     */
    public PeakListMultiAnnTree(Collection<PeakList> plSortedCollection,double toleranceMs2, double toleranceMsn){
        size = 0;
        for(PeakList peakList: plSortedCollection){
            addPeakList(peakList,toleranceMs2, toleranceMsn);
        }
    }

    /**
     * Get the top peak list node in this tree, ms1 or ms2.
     * @return the top peak list node in this tree, ms1 or ms2.
     */
    public PeakListMultiAnnNode getTopPeakListNode() {
        return topPeakListNode;
    }

//    public void setTopPeakListNode(PeakListMultiAnnNode topPeakListNode) {
//        this.topPeakListNode = topPeakListNode;
//    }

    /**
     * Get the number of peak list nodes in this tree.
     * @return the number of peak list nodes in this tree.
     */
    public int size(){
        return size;
    }

    /**
     * Get the MS level of the top peak list.
     * @return
     */
    public int topPeakListLevel(){
        return topPeakListNode == null? 0: topPeakListNode.getLevel();
    }

    /**
     * Add a peak list to this tree.
     * @param pl PeakList object to be added.
     * @param toleranceMs2 Mass tolerance of the precursor mass for MS2.
     * @param toleranceMsn Mass tolerance of the precursor mass for MSn.
     * @return The PeakListMultiAnnNode object generated, or null if failed.
     */
    public PeakListMultiAnnNode addPeakList(PeakList pl, double toleranceMs2, double toleranceMsn){
        PeakListMultiAnnNode ret = null;
//        Print.pl("Adding PeakList: "+ pl.toTitleString());
        if(topPeakListNode == null){
            topPeakListNode = new PeakListMultiAnnNode(pl, null);
            ret = topPeakListNode;
        } else {
            PeakListMultiAnnNode precursorPL = findPrecursorPeakList(pl.getPrecursorMasses(), toleranceMs2, toleranceMsn);
//            if(precursorPL != null) Print.pl("\tFinding pre: "+precursorPL.toTitleString());
//            else Print.pl("\t Finding pre: null");

            if(precursorPL != null){
                ret = precursorPL.addNextStagePeakList(pl, toleranceMs2, toleranceMsn);
            } else {
                Print.pl("In PeakListMultiAnnTree: addPeakList(): find precursor peaklist error!");
            }

        }
        if(ret == null){
            Print.pl("In PeakListMultiAnnTree: addPeakList(): Error occurred!");
            Print.pl("\t"+pl.toTitleString()+"\n");
        }
        size++;
        return ret;
    }

    /**
     * Find the PeakList Node in this tree according to its precursor masses.
     * @param preMasses The precursor masses of the peak list wanted.
     * @param toleranceMs2 Mass tolerance of the precursor mass for MS2.
     * @param toleranceMsn Mass tolerance of the precursor mass for MSn.
     * @return The PeakListMultiAnnNode object wanted, or null if it does not exist.
     */
    public PeakListMultiAnnNode findPrecursorPeakList(final ArrayList<Double> preMasses,
                                                      double toleranceMs2 , double toleranceMsn){
        if(this.topPeakListLevel() == 0){
            return null;
        }
        int statIndex = 1;
        if(this.topPeakListLevel() == 1){
            statIndex = 0;
        }
        PeakListMultiAnnNode currentNode = topPeakListNode;
        boolean found = true;
        if(preMasses.size() == 1 && this.topPeakListLevel() == 1){
            // find precursor of MS2 && MS1 already exists
            currentNode = topPeakListNode;
            found = true;
        } else {
            for(int i = statIndex;i < preMasses.size() - 1; i++){
                found = false;
                double search = preMasses.get(i);
                for(PeakListMultiAnnNode compareNode: currentNode.getNextStagePeakListTreeNodes()){
                    double compareMass = compareNode.getPrecursorMass();
                    double t = i == 0?toleranceMs2:toleranceMsn;
                    if( search > compareMass - t && search < compareMass + t){
                        currentNode = compareNode;
                        found = true;
                        break;
                    }
                }
                if(!found){
                    break;
                }
            }
        }
        return found?currentNode:null;
    }

    /**
     * Get the precursor mass of the MS2, the m/z of the molecule.
     * @return The precursor mass of the MS2, or the m/z of the molecule.
     */
    public double getParentMass(){
        PeakListMultiAnnNode ms2 = getMS2();
        return (ms2 == null)?0:ms2.getPrecursorMass();
    }

    /**
     * Get the MS2 peak list node in this tree.
     * @return The MS2 peak list.
     */
    public PeakListMultiAnnNode getMS2(){
        PeakListMultiAnnNode n = null;
        switch (topPeakListLevel()){
            case 2:
                n = topPeakListNode;
                break;
            case 1:
                n = topPeakListNode;
                if(topPeakListNode.nextStagePeakListTreeNodes!= null
                        && topPeakListNode.nextStagePeakListTreeNodes.size() == 1){
                    n = topPeakListNode.nextStagePeakListTreeNodes.get(0);
                }
                break;
            default:
                n = null;
        }
        return n;
    }

    @Override
    public String toString(){
        String ret = "PeakListTree:"+(topPeakListNode == null?"null":"")+"\n";
        if(topPeakListNode != null){
            ret += topPeakListNode.toString();
        }
        return ret;
    }
}
