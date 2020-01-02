package org.ict.big.GlycanTools.spectrumModel.compositionDenovo;

import java.util.Comparator;

public class FragmentCompositionMzComparator implements Comparator<FragmentComposition> {

    @Override
    public int compare(FragmentComposition o1, FragmentComposition o2) {
        if(o1.mz == o2.mz){
            return 0;
        }else if(o1.mz < o2.mz){
            return -1;
        } else return 1;
    }
}
