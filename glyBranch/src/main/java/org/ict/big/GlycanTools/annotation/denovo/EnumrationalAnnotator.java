package org.ict.big.GlycanTools.annotation.denovo;

import org.ict.big.GlycanTools.debug.Print;
import org.ict.big.GlycanTools.spectrumModel.Peak;
import org.ict.big.GlycanTools.spectrumModel.oldTree.PeakListTree;
import org.ict.big.GlycanTools.spectrumModel.oldTree.PeakListTreeNode;
import org.ict.big.GlycanTools.spectrumModel.compositionDenovo.FragmentComposition;
import org.ict.big.GlycanTools.spectrumModel.compositionDenovo.FragmentCompositionMzComparator;
import org.ict.big.GlycanTools.spectrumModel.compositionDenovo.MonoCompositionEntry;
import org.ict.big.GlycanTools.spectrumModel.compositionDenovo.PeakWithCompositionAnnotation;

import java.util.ArrayList;
import java.util.Collections;

public class EnumrationalAnnotator {

    /**
     * the peak list tree to be annotated
     */
    PeakListTree peakListTree;

    // some masses of atoms or groups used for computing m/z
    double Na = 22.9897697;
    double O = 15.9949146;
    double H = 1.00782500;
    double C = 12.0;
    double CH3 = C + H * 3;
    double OCH3 = CH3 + O;
    double tolerenceOfParrentIon = 1.0;
//    double removeMassWithO = 14.0156500;
//    double removeMassWithoutO = 32.0262146;
    double addMassToWhole = CH3 + OCH3 + Na;

    /** The maximum number of fragmentation times for every spectra. **/
    int maxCutAllowed = 5;
    /** The number of fragmentation times added every stage. **/
    int maxCutEveryStage = 3;
    /** The tolerance of m/z about the matching of fragments and peaks. **/
    double peakTolerance = 0.2;
    /** The resolution of the MS. **/
    double resolution;


    public EnumrationalAnnotator(PeakListTree peakListTree, double resolution){
        this.peakListTree = peakListTree;
        this.resolution = resolution;
    }

    /**
     * This method annotates spectra according to the following steps:
     * <ol>
     *     <li>Enumerate all possible compositions(may be multiple) according to the precursor mass.</li>
     *     <li>Score every possible compositions by virtually(the fragments are not stored to the peak) annotate
     *     spectra using every composition.</li>
     *     <li>Select the best composition according to their scores as the actual composition.</li>
     * </ol>
     * @return the composition of the parent ion(the whole molecule).
     */
    public FragmentComposition annotate(){
        double ms2Mass = peakListTree.getParentMass();
        ArrayList<FragmentComposition> compositions = enumerateParentComposition(ms2Mass);
        for(FragmentComposition fc: compositions){
            Print.pl("ParrentComposition: "+fc);
        }
        FragmentComposition bestComposition = selectBestComposition(compositions, peakListTree.getMS2());
        double score = annotateAllPeakList(peakListTree.getMS2(), bestComposition, maxCutEveryStage, true);
        Print.pl("Matching score: "+score);
        Print.pl("Annotated Tree: \n"+peakListTree.getMS2().getPeakList());
        return bestComposition;
    }

    /**
     * This method numerates all possible compositions(may be multiple) according to the precursor mass.
     * @param mz the mass of the whole molecule.
     * @return a ArrayList containing all possible compositions.
     */
    public ArrayList<FragmentComposition> enumerateParentComposition(double mz){
        double adduct = addMassToWhole;
        ArrayList<FragmentComposition> compositions = new ArrayList<>();
        double[] monoMasses = MonosaccharideLibrary.getAllMass();
        String[] monoId = MonosaccharideLibrary.getAllId();

        int[] numOfMono = new int[monoMasses.length];
        ArrayList<int[]> validComposition = new ArrayList<>();
        double enumMass = mz - adduct;
        enumerateParentCompositionWalk(enumMass, numOfMono, 0, monoMasses, validComposition);
        for(int[] num: validComposition){
            FragmentComposition composition = new FragmentComposition(monoId, num);
            composition.trimInPlace();
            compositions.add(composition);
        }
        return compositions;
    }

    /**
     * This method is a recursive method of enumerating all possible compositions of the whole molecule.
     * This method enumerates the number of i+1th monosaccharide.
     * @see EnumrationalAnnotator#enumerateParentComposition
     * @param massLeft the mass of undetermined part.
     * @param curComposition composition[0] to composition[i-1] are the determined composition.
     *                       composition[i:] may not be 0.
     * @param i i means that this method will enumerate the i-th of composition (starting form 0).
     * @param masses masses the list containing masses of monosaccharides.
     * @param validCompositions the list containing the answers(possible compositions).
     */
    private void enumerateParentCompositionWalk(double massLeft, int[] curComposition, int i,
                                                double[] masses, ArrayList<int[]> validCompositions){
        if(i >= masses.length){
            return;
        }
        int upper = (int)Math.ceil((massLeft + tolerenceOfParrentIon) / masses[i]);
        // enumerate the number of i-th monosaccharide is j
        for(int j = 0; j <= upper; j++){
            curComposition[i] = j;
            double mass_now = massLeft - masses[i] * j;
            if(Math.abs(mass_now) < tolerenceOfParrentIon){
                // a valid composition
                int[] validC = new int[curComposition.length];
                for(int k = 0;k < validC.length; k++){
                    // make all numbers in composition after i-th be zero
                    validC[k] = k <= i? curComposition[k]:0;
                }
                validCompositions.add(validC);
//                Print.pl("mass diff:"+mass_now);
            } else {
                enumerateParentCompositionWalk(mass_now, curComposition, i+1, masses, validCompositions);
            }
        }
    }

    /**
     * This method chooses the best composition from compositions enumerated according to scores of compositions.
     * @param compositions
     * @param node
     * @return
     */
    public FragmentComposition selectBestComposition(ArrayList<FragmentComposition> compositions, PeakListTreeNode node){
        FragmentComposition bestComposition = null;
        double bestScore = 0;
        for(FragmentComposition c: compositions){
            double score = annotateAllPeakList(node, c, maxCutEveryStage, false);
            //double score = annotateAPeakList(node, c, maxCutEveryStage, false);
            Print.pl("Composition: " + c + "\tmatchedNum: "+ score);
            if(score > bestScore){
                bestComposition = c;
                bestScore = score;
            }
        }
        return bestComposition;
//        return compositions.get(2);
    }

    public double annotateAPeakList(PeakListTreeNode ms2Node, FragmentComposition parentComposition,
                                   int maxCut, boolean annotate){
        double scoreOfThisPeakList = 0;
        ArrayList<FragmentComposition> precursorCompositions = new ArrayList<>();
        precursorCompositions.add(parentComposition);
        scoreOfThisPeakList = annotateAPeakListWalk(ms2Node, precursorCompositions, maxCut, annotate, false);
        return scoreOfThisPeakList;
    }

    public double annotateAllPeakList(PeakListTreeNode ms2Node, FragmentComposition parentComposition,
                                   int maxCut, boolean annotate){
        double totalScore = 0;
        ArrayList<FragmentComposition> precursorCompositions = new ArrayList<>();
        precursorCompositions.add(parentComposition);
        totalScore = annotateAPeakListWalk(ms2Node, precursorCompositions, maxCut, annotate, true);
        return totalScore;
    }


    public double annotateAPeakListWalk(PeakListTreeNode node, ArrayList<FragmentComposition> precursorCompositions,
                                       int maxCut, boolean annotate, boolean walk){
        double scoreOfThisPeakList = 0;
        ArrayList<FragmentComposition> allFragemts = fragmentation(precursorCompositions, maxCut);
//        Print.pl("\tallFragments.size: "+allFragemts.size());
        Collections.sort(allFragemts, new FragmentCompositionMzComparator());
        ArrayList< ArrayList<FragmentComposition> > nextStageComposition = new ArrayList<>();
        for(Peak p: node.getPeakList()){
            ArrayList<FragmentComposition> fragments = findFragmentsInRange(allFragemts,
                    p.getMz() - p.getIsotopicMz() - peakTolerance,
                    p.getMz() - p.getIsotopicMz() + peakTolerance);
            if(p.getIsotopicMz() < 0.1 && p.getIsotopicMz() > -0.1){
                if(fragments.size()> 0){
                    FragmentComposition f0 = fragments.get(0);
                    double theoreticalMz = f0.getMz();
                    scoreOfThisPeakList += (1 - Math.abs(theoreticalMz - p.getMz()) / peakTolerance); // add score
                }
            }
            if(fragments.size() > 0 && annotate){
                labelAPeak(node, p, fragments);
            }
            if(p.hasNextLevelPeakList()){
                nextStageComposition.add(fragments);
            }
        }
//        Print.pl("matcheed size: "+numOfAnnotatedPeaks);
        //for(PeakListTreeNode nextNode: node.getNextStagePeakListTreeNodes()){
        if(walk){
            for(int i = 0;i < node.getNextStagePeakListTreeNodes().size();i++){
//                Print.pl("debug:"+i+"\t"+ node.getNextStagePeakListTreeNodes().size());
                PeakListTreeNode nextNode = node.getNextStagePeakListTreeNodes().get(i);
                scoreOfThisPeakList += this.annotateAPeakListWalk(nextNode, nextStageComposition.get(i),
                        maxCut + maxCutEveryStage, annotate, walk);
            }
        }
        return scoreOfThisPeakList;
    }


    public ArrayList<FragmentComposition> findFragmentsInRange(ArrayList<FragmentComposition> allFragments,
                                                               double lower, double upper){
        ArrayList<FragmentComposition> ret = new ArrayList<>();
        for(FragmentComposition f: allFragments){
            if(f.getMz() >= lower && f.getMz() <= upper){
                ret.add(f);
            }
        }
        return ret;
    }

    public void labelAPeak(PeakListTreeNode node, Peak p, ArrayList<FragmentComposition> fragments){
        PeakWithCompositionAnnotation peakWithCompositionAnnotation = new PeakWithCompositionAnnotation(p, fragments);
        int index = node.getPeakList().getPeaks().indexOf(p);
        node.getPeakList().getPeaks().set(index, peakWithCompositionAnnotation);
    }

    public ArrayList<FragmentComposition> fragmentation(ArrayList<FragmentComposition> composition, int maxCut) {
        ArrayList<FragmentComposition> uniqueCompositions = getUniqueCompositions(composition);
        ArrayList<FragmentComposition> allFragments = new ArrayList<>();
        for(FragmentComposition uf: uniqueCompositions){
            ArrayList<FragmentComposition> fs = fragmentation(uf, maxCut);
            allFragments.addAll(fs);
        }
        return allFragments;
    }

    public ArrayList<FragmentComposition> getUniqueCompositions(ArrayList<FragmentComposition> compositions){
        ArrayList<FragmentComposition> uniqueCompositions = new ArrayList<>();
        for(FragmentComposition f: compositions){
            boolean flagEqual = false;
            for(FragmentComposition uf: uniqueCompositions){
                if(f.equalInComposition(uf)){
                    flagEqual = true;
                    break;
                }
            }
            if(!flagEqual){
                uniqueCompositions.add(f);
            }
        }
        return uniqueCompositions;
    }

    public ArrayList<FragmentComposition> fragmentation(FragmentComposition composition, int maxCut){
//        int numOfParentMono = composition.numOfMono();
//        maxCut = Math.min(maxCut, numOfParentMono);
        maxCut = Math.min(maxCut, maxCutAllowed);
        ArrayList<FragmentComposition> allFragemts = new ArrayList<>();
        if(composition == null){
            return allFragemts;
        }
        FragmentComposition curC = composition.deepClone();
        double[] masses = new double[composition.getNumOfType()];
        for(int i = 0;i < masses.length;i++){
            double mass = MonosaccharideLibrary.getMassFromId(composition.getPureComposition()[i].id);
            masses[i] = mass;
        }
//        Print.pl("In fragmentation: composition: "+composition);
        fragmentationWalk( 0, curC, maxCut, composition, masses, allFragemts);
//        for(FragmentComposition f: allFragemts){
//            Print.pl(f.toFragmentString()+",");
//        }Print.pl("\n");
        return allFragemts;
    }

    private void fragmentationWalk(int i, FragmentComposition curComposition, int maxCut,
                                   FragmentComposition parentComposition, double[] masses,
                                   ArrayList<FragmentComposition> fragments){
        if(i >= parentComposition.getPureComposition().length){
            int curL = curComposition.numOfMono();
            if(curL > 0 && curL < parentComposition.numOfMono()){
                double totalMass = 0;
                for(int k = 0;k< masses.length;k++){
                    totalMass += curComposition.getPureComposition()[k].num * masses[k];
                }
                totalMass += addMassToWhole;
                ArrayList<FragmentComposition> addMasses = BYCZTable.getPossibleMasses(maxCut);

                for(FragmentComposition f: addMasses){
                    double mz = totalMass + f.getMz();
                    FragmentComposition frag = new FragmentComposition(f);
                    frag.setComposition(curComposition.copyPureComposition());
                    frag.setMz(mz);
                    fragments.add(frag);
//                    Print.pl("add");
                }
            }
            return;
        }
        int limit = parentComposition.getPureComposition()[i].num;
        MonoCompositionEntry[] curC = curComposition.getPureComposition();
        for(int j = 0; j <= limit; j++){
            curC[i].num = j;
            fragmentationWalk(i+1, curComposition, maxCut, parentComposition, masses, fragments);
        }

    }

}

class BYCZTable{
    double removeMassWithO = 14.0156500;
    double removeMassWithoutO = 32.0262146;
    ArrayList< ArrayList<FragmentComposition> > table;
    static BYCZTable instance;

    BYCZTable(int maxCut){
        table = new ArrayList<>();
        for(int i = 0; i<= maxCut; i++){// number of section
            ArrayList<FragmentComposition> t = new ArrayList<>();
            for(int j = 0; j <= i; j++){// number of o-section edges
                double mass = j * removeMassWithoutO + (i - j) * removeMassWithO;
//                Print.pl("i: "+i+"\tj:"+j);
                t.add(new FragmentComposition(null, i, j, -mass));
            }
            table.add(t);
        }
//        for(ArrayList<FragmentComposition> l: table){
//            if(l == null) continue;
//            for(FragmentComposition f: l){
//                Print.pl(f.toFragmentString()+",");
//            }
//            Print.pl("\n");
//        }
    }

    public static ArrayList<FragmentComposition> getPossibleMasses(int maxCut){
        instance = getInstance();
        ArrayList<FragmentComposition> ret = new ArrayList<>();
        for(int i = 1;i <= maxCut; i++){
            ret.addAll(instance.table.get(i));
        }
        return ret;
    }

    public static BYCZTable getInstance(){
        if(instance == null){
            instance = new BYCZTable(5);
        }
        return instance;
    }
}