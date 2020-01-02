package org.ict.big.GlycanTools.spectrumModel.compositionDenovo;

import org.ict.big.GlycanTools.spectrumModel.Peak;
import org.ict.big.GlycanTools.spectrumModel.compositionDenovo.FragmentComposition;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class PeakWithCompositionAnnotation extends Peak {

    protected ArrayList<FragmentComposition> annotation;

    public PeakWithCompositionAnnotation(Peak p){
        super(p);
        annotation = new ArrayList<>();
    }
    public PeakWithCompositionAnnotation(Peak p, ArrayList<FragmentComposition> annotation){
        super(p);
        this.annotation = annotation;
    }

    public ArrayList<FragmentComposition> getAnnotation() {
        return annotation;
    }

    public void setAnnotation(ArrayList<FragmentComposition> annotation) {
        this.annotation = annotation;
    }

    public void addFragmentComposition(FragmentComposition f){
        annotation.add(f);
    }

    public String toString(){
        String str = super.toString() + "\t";
        if(isotopicMz < 0.1 && isotopicMz > -0.1){
            for(FragmentComposition f: annotation){
                str += f.toFragmentString() + ",";
            }
        }
        return  str;
    }
}
