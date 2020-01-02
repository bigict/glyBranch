package org.ict.big.GlycanTools.spectrumModel.oldTree;

import org.eurocarbdb.application.glycanbuilder.FragmentCollection;
import org.ict.big.GlycanTools.spectrumModel.Peak;
import org.ict.big.GlycanTools.spectrumModel.PeakList;

import java.util.ArrayList;

public class PeakListTreeNode {

    protected PeakList peakList;

    protected PeakListTreeNode precursorPeakListTreeNode;

    protected FragmentCollection simulatedFragments;

    protected ArrayList<PeakListTreeNode> nextStagePeakListTreeNodes;

    public PeakListTreeNode(PeakList _peakList, PeakListTreeNode _precursorPeakListNode){
         peakList = _peakList;
         precursorPeakListTreeNode = _precursorPeakListNode;
         nextStagePeakListTreeNodes = new ArrayList<PeakListTreeNode>(0);
    }

    public PeakList getPeakList() {
        return peakList;
    }

    public void setPeakList(PeakList peakList) {
        this.peakList = peakList;
    }

    public PeakListTreeNode getPrecursorPeakListTreeNode() {
        return precursorPeakListTreeNode;
    }

    public void setPrecursorPeakListTreeNode(PeakListTreeNode precursorPeakListTreeNode) {
        this.precursorPeakListTreeNode = precursorPeakListTreeNode;
    }

    public FragmentCollection getSimulatedFragments() {
        return simulatedFragments;
    }

    public void setSimulatedFragments(FragmentCollection simulatedFragments) {
        this.simulatedFragments = simulatedFragments;
    }

    public ArrayList<PeakListTreeNode> getNextStagePeakListTreeNodes() {
        return nextStagePeakListTreeNodes;
    }

    public double getPrecursorMass(){ return peakList.getPrecursorMass(); }


    /**
     * This method returns the max intensed peak in peaks of which the mz
     * ratio is between mz-tolerance and mz+tolerance.
     * @param mz the mz ration of the peak to be found
     * @param tolerance the tolerance of the searching process
     * @return the max intensed peak that satisfies the requirement or null if no peak satisfies.
     */
    public Peak findMaxPeak(double mz, double tolerance){
        return peakList.findMaxPeak(mz, tolerance);
    }

    /**
     * Add a peaklist of next stage to both this node and the peaklist in this node.
     * @param nextPL
     * @param tolerance
     * @return the newly added PeakListTreeNode object, or null if the parent peak does not exist.
     */
    public PeakListTreeNode addNextStagePeakList(PeakList nextPL, double tolerance){
        PeakListTreeNode nextPLNode = new PeakListTreeNode(nextPL, this);
        boolean success = addNextStagePeakList(nextPLNode, tolerance);
        return success?nextPLNode:null;
    }

    /**
     * Add a peaklist of next stage to both this node and the peaklist in this node.
     * @param nextPL
     * @param tolerance
     * @return true if succeeded, or false if the parent peak does not exist.
     */
    public boolean addNextStagePeakList(PeakListTreeNode nextPL, double tolerance){
        double precursorMass = nextPL.getPeakList().getPrecursorMass();
        Peak p = findMaxPeak(precursorMass, tolerance);
        if(p == null){
            return false;
        }
        p.setNextLevelPeakList(nextPL.getPeakList());
        this.nextStagePeakListTreeNodes.add(nextPL);
        nextPL.setPrecursorPeakListTreeNode(nextPL);
        return true;
    }

    @Override
    public String toString() {
        return toString("");
    }

    public String toString(String prefixString){
        String ret = prefixString + peakList.toTitleString() + "\n";
        for(PeakListTreeNode peakListTreeNode: nextStagePeakListTreeNodes){
            ret += peakListTreeNode.toString(prefixString+"\t");
        }
        return ret;
    }
}
