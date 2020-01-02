package org.ict.big.GlycanTools.spectrumModel.oldTree;

import org.ict.big.GlycanTools.debug.Print;
import org.ict.big.GlycanTools.spectrumModel.PeakList;

import java.util.ArrayList;
import java.util.Collection;

public class PeakListTree {


    protected PeakListTreeNode topPeakListNode;

    int size = 0;

    public PeakListTree(PeakList _topPeakList){
        topPeakListNode = new PeakListTreeNode(_topPeakList, null);
        size = 0;
    }

    /**
     * Initialize a PeakListTree by a set of peak lists
     * @param plSortedCollection a sorted collection of peak lists
     */
    public PeakListTree(Collection<PeakList> plSortedCollection){
        size = 0;
        for(PeakList peakList: plSortedCollection){
            addPeakList(peakList, 0.3);
        }
    }

    public PeakListTreeNode addPeakList(PeakList pl, double tolerance){
        PeakListTreeNode ret = null;
//        Print.pl("Adding peaklist: "+ pl.toTitleString());
        if(topPeakListNode == null){
            topPeakListNode = new PeakListTreeNode(pl, null);
            ret = topPeakListNode;
        } else {
            PeakListTreeNode precursorPL = findPrecursorPeakList(pl.getPrecursorMasses(), tolerance);
//            Print.pl("\tFinding precursor PL: "+ precursorPL);
            if(precursorPL != null){
                ret = precursorPL.addNextStagePeakList(pl, tolerance);
            } else {
                Print.pl("find precursor peaklist error!");
            }

        }
        if(ret == null){
            Print.pl("Error occurred!");
            Print.pl("\t"+pl.toTitleString()+"\n");
        }
        size++;
        return ret;
    }

    public int topPeakListLevel(){
        return topPeakListNode == null? 0: topPeakListNode.getPeakList().getLevel();
    }

    public PeakListTreeNode getTopPeakListNode() {
        return topPeakListNode;
    }

    public PeakListTreeNode findPrecursorPeakList(final ArrayList<Double> preMasses, double tolerance){
        if(this.topPeakListLevel() == 0){
            return null;
        }
        int statIndex = 1;
        if(this.topPeakListLevel() == 1){
            statIndex = 0;
        }
        PeakListTreeNode currentNode = topPeakListNode;
        boolean found = true;
        for(int i = statIndex;i < preMasses.size() - 1; i++){
            found = false;
            double search = preMasses.get(i);
            for(PeakListTreeNode compareNode: currentNode.getNextStagePeakListTreeNodes()){
                double compareMass = compareNode.getPrecursorMass();
//                Print.pl("searchMass: "+search+"\tcompareMass:"+compareMass);
                if( search > compareMass - tolerance && search < compareMass + tolerance){
                    currentNode = compareNode;
                    found = true;
//                    Print.pl("\tfounded!");
                    break;
                }
            }
            if(!found){
                break;
            }
        }
        return found?currentNode:null;
    }


    public String toString(){
        String ret = "PeakListTree:"+(topPeakListNode == null?"null":"")+"\n";
        if(topPeakListNode != null){
            ret += topPeakListNode.toString();
        }
        return ret;
    }

    public double getParentMass(){
        PeakListTreeNode ms2 = getMS2();
        return (ms2 == null)?0:ms2.getPrecursorMass();
    }

    public PeakListTreeNode getMS2(){
        PeakListTreeNode n = null;
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
}
