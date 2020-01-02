package org.ict.big.GlycanTools.annotation;

import org.apache.commons.io.FileUtils;
import org.eurocarbdb.application.glycanbuilder.FragmentCollection;
import org.eurocarbdb.application.glycanbuilder.FragmentEntry;
import org.eurocarbdb.application.glycanbuilder.Glycan;
import org.eurocarbdb.application.glycanbuilder.renderutil.GlycanRendererAWT;
import org.eurocarbdb.application.glycanbuilder.renderutil.SVGUtils;
import org.ict.big.GlycanTools.Main;
import org.ict.big.GlycanTools.debug.Print;
import org.ict.big.GlycanTools.options.ScoringConfig;
import org.ict.big.GlycanTools.spectrumModel.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * This class is used to
 */
public class AnnotationScorer {

    PeakListMultiAnnTree annotatedTree;
    GlycanCandidates candidates;
    ScoringConfig config;

    public AnnotationScorer(PeakListMultiAnnTree annotatedTree,
                            GlycanCandidates candidates, ScoringConfig config){
        this.annotatedTree = annotatedTree;
        this.candidates = candidates;
        this.config = config;
    }

    protected void initialScores(){
        for(GlycanCandidate candidate: candidates){
            candidate.setScore(0);
        }
    }

    protected void initialTempScores(){
        for(GlycanCandidate candidate: candidates){
            candidate.setTempScore(0);
        }
    }

    protected void applyTempSocres(){
        for(GlycanCandidate candidate: candidates){
            candidate.setScore(candidate.getScore() + candidate.getTempScore());
        }
    }

    protected void sigmoidNormalize(){
        double maxScore=0;
        if(candidates.size()>0) {
            maxScore = candidates.get(0).getScore();
        }
        for(GlycanCandidate candidate: candidates){
            if (candidate.getScore()>maxScore){
                maxScore=candidate.getScore();
            }
        }
        int divider=1;
        while (Double.isNaN(Math.exp(maxScore)*20)||Double.isInfinite(Math.exp(maxScore)*20)){
            maxScore=Math.round(maxScore/2);
            divider*=2;
        }
        for(GlycanCandidate candidate: candidates){
                candidate.setScore(Math.exp(Math.round(candidate.getScore()/divider)));
        }
        candidates.normalizeScores();
    }

    public void score(){
        initialScores();
        PeakListMultiAnnNode startNode = annotatedTree.getMS2();
        if(startNode == null){
            return;
        }
        boolean recursive = !config.scoreOnlyMs2;
        scoreANode(startNode,recursive);
//        candidates.normalizeScores();
        if(config.sigmoidNormalize){
            sigmoidNormalize();
        }
    }

    public void scoreANode(PeakListMultiAnnNode node, boolean recursive){
        // compute score for every annotated peaks in this node
//        Print.pl("Scoring peak list: " + node.toTitleString());
        initialTempScores();
        for(Peak p: node){
            if(p instanceof PeakWithMultipleAnnotations){
                PeakWithMultipleAnnotations pa = (PeakWithMultipleAnnotations)p;
                scoreAPeak(node, pa);
            }
        }
        applyTempSocres();
//        candidates.normalizeScores();
//        Print.pl("candidates: " + candidates);
//        Print.pl("actual candidate: " + candidates.findCandidateById("125"));
        // for next level peak list nodes
        if(recursive){
            for(PeakListMultiAnnNode nextNode: node.getNextStagePeakListTreeNodes()){
                scoreANode(nextNode, recursive);
            }
        }
    }

    private void scoreAPeak(PeakListMultiAnnNode node, PeakWithMultipleAnnotations p){
        double spectrumWeight = config.weightForEachStage[node.getLevel()];

        MultiAnnotations annotations = p.getAnnotations();
        for(MultiAnnotationsEntry annForACandidate: annotations){
            GlycanCandidate candidate = annForACandidate.getCandidate();
            // compute m/z similarity
            double mzSimilarity = Math.abs(annForACandidate.getMaxIntensedIsoMz().get(0) - p.getMz())
                    / config.annotationTolerance;

            // compute isotopic similarity
            IsotopicDistribution theoreticalIsotopicDis = annForACandidate.getIsotopicDistributions().get(0);
            theoreticalIsotopicDis = IsotopicPeakDistributionCaculator.trimAccordingToIntensity(theoreticalIsotopicDis,
                    p.getRelativeIntensity(), config.minIntsAllowedOfIsoDistribution);
            int maxIntensedIndex = theoreticalIsotopicDis.getMaxIntensedIndex();
            IsotopicDistribution experimentalIsotopicDis = getIsotopicDistributionOfAPeak(node, p, maxIntensedIndex);

            double isoSimilariry = computeSimilarityBetweenIsotopicDistributions(
                    theoreticalIsotopicDis, experimentalIsotopicDis);
//            isoSimilariry = 1;
            // scoring function
            double score=0;
            if(config.ScoringFunction==0){
                score = isoSimilariry * Math.tanh(config.alphaForTanh * p.getRelativeIntensity());
            }
            else if(config.ScoringFunction==1){
                score=isoSimilariry*1;
            }
            else if(config.ScoringFunction==2){
                score=isoSimilariry * Math.log(p.getRelativeIntensity()+1);
            }
            else{
                Print.pl("Please input the vaild scoring function!");
            }
//            double score = spectrumWeight * isoSimilariry * Math.log(p.getRelativeIntensity()) + 1;
//            double score =  isoSimilariry * Math.log(p.getRelativeIntensity()+1);
//            double score = isoSimilariry * Math.tanh(config.alphaForTanh * p.getRelativeIntensity());
//            double score = p.getRelativeIntensity() / 100;
//            double score = isoSimilariry *1;
//            double score = Math.log(p.getRelativeIntensity()+1);
//            double score = Math.tanh(config.alphaForTanh * p.getRelativeIntensity());

            if(candidate.record.ID.equals("179")
                    && annotations.findAnnotationById("125") == null
                    && annotations.findAnnotationById("179") != null){
//                Print.pl("\t\t" + theoreticalIsotopicDis + "\t" + experimentalIsotopicDis +
//                        "\tsimilarity:" + isoSimilariry + "\tinitScore: "+ score);
                Print.pl("\tMS->" + node.toPrecursorString(0) + "\t" + p.getMz() +"\t"+ p.getRelativeIntensity()
                        +"\t" + isoSimilariry +"\t" + score);

                //fragments svg
//                FragmentCollection annotatedFragments = annotations.findAnnotationById("132").getFragments();
//                int i = 0;
//                for(FragmentEntry fragment: annotatedFragments.getFragments()){
//                    i++;
//                    LinkedList<Glycan> fragmentArray = new LinkedList<>();
//                    fragmentArray.add(fragment.fragment);
//                    String svgStr = SVGUtils.getVectorGraphics((GlycanRendererAWT)Main.glycanRenderer, fragmentArray,
//                            false, true);
//                    String filename = generateSVGFileName(node, p, i);
//                    try {
//                        FileUtils.writeByteArrayToFile(new File("D:\\Work-temp\\Paper_scoring\\figure\\svg\\Man-7D3_GIPS\\"+filename), svgStr.getBytes());
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
            }
            candidate.setTempScore(candidate.getTempScore() + score);
//            Print.pl("\ttotal score:" + candidate.getScore());
        }
//        if(annotations.findAnnotationById("126") == null
//                && annotations.findAnnotationById("467") != null){
//            Print.pl("\tMatched peaks:\t"+p.toString());
//        }
    }

    private double computeSimilarityBetweenIsotopicDistributions(IsotopicDistribution theoreticalIsotopicDis,
                                                                 IsotopicDistribution experimentalIsotopicDis){
        double ans = 0;
        int minLen = Math.min(theoreticalIsotopicDis.size(), experimentalIsotopicDis.size());
        double lenThe = 0;
        double lenExp = 0;
        for(int i = 0; i < minLen;i++){
            ans += theoreticalIsotopicDis.get(i) * experimentalIsotopicDis.get(i);
        }
        for(double d: theoreticalIsotopicDis){
            lenThe += d * d;
        }
        for(double d: experimentalIsotopicDis){
            lenExp += d * d;
        }
        lenThe = Math.sqrt(lenThe);
        lenExp = Math.sqrt(lenExp);
        ans = ans / (lenExp * lenThe);
        return ans;
    }

    private IsotopicDistribution getIsotopicDistributionOfAPeak(PeakList peakList,
                                                                Peak p, int isoPos){
        double isotopicTolerence = config.isotopicTolerence;
        IsotopicDistribution distribution = new IsotopicDistribution();
        for(int i = 0; i < isoPos; i++){
            distribution.add(0.0);
        }
        ArrayList<Peak> peaks = peakList.getPeaks();
        int index = peaks.indexOf(p);
        // find isotopic peak before p(should not exceed isoPos)
        if(isoPos > 0){
            int diff = 1;
            for(int i = index - 1; i >= 0; i--){
                Peak curP = peaks.get(i);
                if(curP.getMz() < p.getMz() - isoPos - 0.5 || diff > isoPos){
                    break;
                }
                double shouldMz = p.getMz() - diff;
                if(curP.getMz() > shouldMz - isotopicTolerence &&
                        curP.getMz() < shouldMz + isotopicTolerence){
                    distribution.set(isoPos - diff, curP.getRelativeIntensity());
                    diff++;
                }
            }
        }
        distribution.add(p.getRelativeIntensity());
        int diff = 1;
        for(int i = index + 1; i < peaks.size();i++){
            Peak curP = peaks.get(i);
            if(curP.getMz() - p.getMz() > diff + 2.0){
                break;
            }
            double shouldMz = p.getMz() + diff;
            if(curP.getMz() > shouldMz - isotopicTolerence &&
                    curP.getMz() < shouldMz + isotopicTolerence){
                diff++;
                distribution.add(curP.getRelativeIntensity());
            }
        }
        distribution.normalize();
        return distribution;
    }

    private String generateSVGFileName(PeakListMultiAnnNode node, PeakWithMultipleAnnotations p,int number){
        String dd = Integer.toString(0);;
        String ret = "";
        for(int i = 0;i<node.getPrecursorMasses().size();i++){
            if(i == 0) {
                ret += String.format("%."+dd+"f", node.getPrecursorMasses().get(i));
            } else {
                ret += String.format("-%."+dd+"f", node.getPrecursorMasses().get(i));
            }
        }
        ret += String.format("_%.2f", p.getMz());
        ret += "-" + Integer.toString(number);
        ret += ".svg";
        return ret;
    }
}
