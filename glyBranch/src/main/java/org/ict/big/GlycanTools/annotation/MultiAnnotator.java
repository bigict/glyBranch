package org.ict.big.GlycanTools.annotation;

import org.eurocarbdb.application.glycanbuilder.FragmentCollection;
import org.ict.big.GlycanTools.debug.Print;
import org.ict.big.GlycanTools.options.ScoringConfig;
import org.ict.big.GlycanTools.spectrumModel.*;
import org.ict.big.GlycanTools.spectrumModel.structureLibrary.CarbohydrateRecord;

import java.util.ArrayList;
import java.util.Map;

public class MultiAnnotator {

    GlycanFragmenter fragmenter;

    ScoringConfig scoringConfig;

    GlycanCandidates candidates;

    PeakListMultiAnnTree peakListTree;

    public MultiAnnotator(PeakListMultiAnnTree peakListTree,
                          GlycanCandidates candidates,
                          ScoringConfig config){
        this.peakListTree = peakListTree;
        this.scoringConfig = config;
        this.candidates = candidates;
        fragmenter = new GlycanFragmenter(config.fragmentOptions);
    }

    public void annotate(){
        int level = peakListTree.topPeakListLevel();
        PeakListMultiAnnNode startNode = peakListTree.getMS2();
        MultiAnnotations parentIonAnnotation = new MultiAnnotations(candidates);
        parentIonAnnotation.initialParentMoleculeAnnotation();
        AnnotateANode(startNode, parentIonAnnotation, !scoringConfig.scoreOnlyMs2);

//        generateAllPossibleMz();
    }

    public void AnnotateANode(PeakListMultiAnnNode node, MultiAnnotations precursorAnnotations, boolean recursive){
        // Annotate this node
        if(precursorAnnotations == null || node == null){
            return;
        }
        fragmenter.setMaxNoCleavagesAccordingToLevel(node.getLevel());
        MultiAnnotations annotationsOnThisNode = new MultiAnnotations(candidates);
//        Print.pl(""+precursorAnnotations);
        for(MultiAnnotationsEntry mae: precursorAnnotations){
            FragmentCollection fragmentsToBeFragmented = mae.getFragments();
            FragmentCollection fragments = fragmenter.computeAllFragments(fragmentsToBeFragmented);
            MultiAnnotationsEntry maeOnThisNode = new MultiAnnotationsEntry(mae.getCandidate());
            maeOnThisNode.setFragments(fragments);
            maeOnThisNode.generateIsotopicDistribution();
            annotationsOnThisNode.add(maeOnThisNode);
        }
        node.setAnnotations(annotationsOnThisNode);

        // Annotate peaks in this node
        for(Peak p: node){
            double tolerance = scoringConfig.annotationTolerance;

            MultiAnnotations ann = node.getAnnotations().generateAnnotationsInARange(
                    p.getMz() - tolerance, p.getMz() + tolerance);
            if(ann != null){
                PeakWithMultipleAnnotations newP = new PeakWithMultipleAnnotations(p, ann);
                node.set(p, newP);
            }
        }

        // Annotate next staged peak list nodes
        if(recursive){
            for(PeakListMultiAnnNode nextNode: node.getNextStagePeakListTreeNodes()){
                double preMass = nextNode.getPrecursorMass();
                double window = preMass / scoringConfig.resolution;
                MultiAnnotations annotationsWithinIonTrap = node.getAnnotations().generateAnnotationsInARange(
                        preMass - window / 2, preMass + window / 2);
                this.AnnotateANode(nextNode, annotationsWithinIonTrap, recursive);
            }
        }
    }


//    public void generateAllPossibleMz(){
//        for(GlycanCandidate candidate: candidates){
//            Map<String, Double> allMz = fragmenter.getAllPossibleMz(candidate.record.glycan);
//            candidate.setAllPossibleMz(allMz);
//        }
//    }
}
