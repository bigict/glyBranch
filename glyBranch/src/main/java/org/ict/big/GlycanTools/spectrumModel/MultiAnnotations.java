package org.ict.big.GlycanTools.spectrumModel;

import org.eurocarbdb.application.glycanbuilder.FragmentCollection;

import java.util.ArrayList;

/**
 * This class represents the annotation of one peak with multiple glycans separately.
 */
public class MultiAnnotations extends ArrayList<MultiAnnotationsEntry> {
    //ArrayList<MultiAnnotationsEntry> annotations;

    public MultiAnnotations(){
        super();
    }

    public MultiAnnotations(GlycanCandidates candidates){
        super();
        for(GlycanCandidate candidate: candidates){
            MultiAnnotationsEntry mae = new MultiAnnotationsEntry(candidate);
            this.add(mae);
        }
    }

    public void initialParentMoleculeAnnotation(){
        for(MultiAnnotationsEntry mae: this){
            mae.initialParentMoleculeAnnotation();
        }
    }

    public void addAnnotation(GlycanCandidate candidate, FragmentCollection fragments){
        MultiAnnotationsEntry entry = findAnnotation(candidate);
        entry.setFragments(fragments);
    }

    /**
     * Find the annotation entry by the candidate object
     * @param candidate
     * @return MultiAnnotationsEntry object or null if it does not exists.
     */
    public MultiAnnotationsEntry findAnnotation(GlycanCandidate candidate){
        for(MultiAnnotationsEntry mae: this){
            if(mae.candidate.equals(candidate)){
                return mae;
            }
        }
        return null;
    }

    /**
     * Find the annotation entry by the ID of candidate object
     * @param id
     * @return MultiAnnotationsEntry object or null if it does not exists.
     */
    public MultiAnnotationsEntry findAnnotationById(String id){
        for(MultiAnnotationsEntry mae: this){
            if(mae.candidate.record.ID.equals(id)){
                return mae;
            }
        }
        return null;
    }

    /**
     *
     * @param mzLeft
     * @param mzRight
     * @return A new MultiAnnotations object or null if no fragments of any
     * candidate can be annotated in this range.
     */
    public MultiAnnotations generateAnnotationsInARange(double mzLeft, double mzRight){
        MultiAnnotations annotations = new MultiAnnotations();
        for(MultiAnnotationsEntry entry: this){
            MultiAnnotationsEntry newEntry = entry.generateAnnotationsInARange(mzLeft, mzRight);
            if(newEntry.size() > 0){
                annotations.add(newEntry);
            }
        }
        return annotations.size() == 0?null:annotations;
    }

    public void merge(MultiAnnotations annotationsToBeMerged){
        for(MultiAnnotationsEntry entry: annotationsToBeMerged){
            MultiAnnotationsEntry entrySearched = this.findAnnotation(entry.candidate);
            if(entrySearched == null){

            } else {
                entrySearched.merge(entry);
            }
        }
    }

    @Override
    public String toString() {
        return  toString("");
    }
    public String toString(String prefix) {
        String ret = "";
        ret += prefix + "Annotations:\n";
        for(MultiAnnotationsEntry mae: this){
            ret += prefix + "\t" + mae.toString() + "\n";
        }
        return ret;
    }
}
