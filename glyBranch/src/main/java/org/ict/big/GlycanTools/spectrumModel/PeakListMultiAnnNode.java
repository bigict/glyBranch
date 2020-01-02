package org.ict.big.GlycanTools.spectrumModel;

import org.eurocarbdb.application.glycanbuilder.FragmentCollection;
import org.ict.big.GlycanTools.debug.Print;

import java.util.ArrayList;

public class PeakListMultiAnnNode extends PeakList {

    /**
     * The precursor peak list node of this node.
     */
    protected PeakListMultiAnnNode precursorPeakListTreeNode;

    /**
     * The peak list nodes of the nexted staged peak list of this node.
     */
    protected ArrayList<PeakListMultiAnnNode> nextStagePeakListTreeNodes;

    protected MultiAnnotations annotations;

    /**
     * Initialize a PeakListMultiAnnNode according to a PeakList and its precursor node.
     * @param _peakList
     * @param _precursorPeakListNode
     */
    public PeakListMultiAnnNode(PeakList _peakList, PeakListMultiAnnNode _precursorPeakListNode){
        super(_peakList);
        precursorPeakListTreeNode = _precursorPeakListNode;
        nextStagePeakListTreeNodes = new ArrayList<PeakListMultiAnnNode>(0);
    }

    /**
     * Get the precursor node of this peak list node.
     * @return the precursor node
     */
    public PeakListMultiAnnNode getPrecursorPeakListTreeNode() {
        return precursorPeakListTreeNode;
    }

    /**
     * Get the precursor node of this peak list node.
     * @param precursorPeakListTreeNode the precursor node of this peak list node to be set.
     */
    public void setPrecursorPeakListTreeNode(PeakListMultiAnnNode precursorPeakListTreeNode) {
        this.precursorPeakListTreeNode = precursorPeakListTreeNode;
    }

    /**
     * Get nodes of the next staged spectrum of this peak list node.
     * @return
     */
    public ArrayList<PeakListMultiAnnNode> getNextStagePeakListTreeNodes() {
        return nextStagePeakListTreeNodes;
    }


    public MultiAnnotations getAnnotations() {
        return annotations;
    }

    public void setAnnotations(MultiAnnotations annotations) {
        this.annotations = annotations;
    }

    /**
     * Add a peaklist of next stage to both this node and and the peak in the peak list.
     * @param nextPL The peak list object to be added
     * @param toleranceMs2 Mass tolerance of the precursor mass for MS2.
     * @param toleranceMsn Mass tolerance of the precursor mass for MSn.
     * @return the added peak list node, or null if failed.
     */
    public PeakListMultiAnnNode addNextStagePeakList(PeakList nextPL, double toleranceMs2, double toleranceMsn){
        PeakListMultiAnnNode nextPLNode = new PeakListMultiAnnNode(nextPL, this);
        boolean success = addNextStagePeakListNode(nextPLNode,toleranceMs2, toleranceMsn);
        return success?nextPLNode:null;
    }

    /**
     * Add a peak list node of next stage to both this node and the peak in the peak list.
     * @param nextPL The peak list node object to be added
     * @param toleranceMs2 Mass tolerance of the precursor mass for MS2.
     * @param toleranceMsn Mass tolerance of the precursor mass for MSn.
     * @return true if succeed, or false if failed.
     */
    public boolean addNextStagePeakListNode(PeakListMultiAnnNode nextPL,double toleranceMs2, double toleranceMsn){
        double tolerance = this.getLevel() == 1?toleranceMs2:toleranceMsn;
        double precursorMass = nextPL.getPrecursorMass();
        Peak p = findMaxPeak(precursorMass, tolerance);
        if(p != null){
            p.setNextLevelPeakList(nextPL);
        }
        this.nextStagePeakListTreeNodes.add(nextPL);
        nextPL.setPrecursorPeakListTreeNode(this);
        return true;
    }

    @Override
    public String toString() {
        return toString("");
    }

    public String toString(String prefixString){
        String ret = prefixString + this.toTitleString() + "\n";
        for(Peak p: this){
            ret += prefixString + p.toString() + "\n";
        }
        for(PeakListMultiAnnNode peakListTreeNode: nextStagePeakListTreeNodes){
            ret += peakListTreeNode.toString(prefixString+"\t");
        }
        return ret;
    }
}
