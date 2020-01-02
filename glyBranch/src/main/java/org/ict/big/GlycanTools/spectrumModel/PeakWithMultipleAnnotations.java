package org.ict.big.GlycanTools.spectrumModel;

import org.eurocarbdb.application.glycanbuilder.FragmentCollection;

import java.util.ArrayList;

public class PeakWithMultipleAnnotations extends Peak {

    MultiAnnotations annotations;

    public PeakWithMultipleAnnotations(Peak p, MultiAnnotations annotations){
        super(p);
        this.annotations = annotations;
    }

    public MultiAnnotations getAnnotations() {
        return annotations;
    }

    public void setAnnotations(MultiAnnotations annotations) {
        this.annotations = annotations;
    }

    @Override
    public String toString() {
        return super.toString() + "\tMultiAnn_size: "+ annotations.size();
    }
}
