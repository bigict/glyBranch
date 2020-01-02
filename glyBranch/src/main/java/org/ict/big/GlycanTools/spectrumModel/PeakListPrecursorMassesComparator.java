package org.ict.big.GlycanTools.spectrumModel;

import java.util.Comparator;

/**
 * This class is a comparator of PeakList.
 * The spectrum with the smaller SP level is smaller.
 * If the SP levels of the two specta are the same. The spectrum with the 
 * smaller fileID is smaller. 
 */
public class PeakListPrecursorMassesComparator implements Comparator<PeakList> {

    @Override
    public int compare(PeakList oA, PeakList oB) {
        if (oA.getLevel() > oB.getLevel()) {
            return 1;
        } else if (oA.getLevel() < oB.getLevel()) {
            return -1;
        } else { // oA.getLevel() == oB.getLevel()
            int l = oA.getPrecursorMasses().size();
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
            //return oA.getSpFileID().compareToIgnoreCase(oB.getSpFileID());
            return 0;
        }

    }
}