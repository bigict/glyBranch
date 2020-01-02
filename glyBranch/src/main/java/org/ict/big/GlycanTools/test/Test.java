package org.ict.big.GlycanTools.test;

import org.eurocarbdb.application.glycanbuilder.*;
import org.eurocarbdb.application.glycanbuilder.linkage.Linkage;
import org.eurocarbdb.application.glycanbuilder.massutil.MassUtils;
import org.eurocarbdb.application.glycanbuilder.renderutil.GlycanRenderer;
import org.eurocarbdb.application.glycanbuilder.renderutil.GlycanRendererAWT;
import org.ict.big.GlycanTools.annotation.Annotator;
import org.ict.big.GlycanTools.debug.Print;
import org.ict.big.GlycanTools.annotation.GlycanFragmenter;
import org.ict.big.GlycanTools.io.ReaderUtil;
import org.ict.big.GlycanTools.spectrumModel.Peak;
import org.ict.big.GlycanTools.spectrumModel.PeakList;
import org.ict.big.GlycanTools.spectrumModel.oldTree.PeakListTree;
import org.ict.big.GlycanTools.spectrumProcesses.PeakFilter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Vector;


/**
 * This class is used to doing something temporarily.
 * @author Jingwei Zhang
 */
public class Test {

    static String glycanName = "2-FL_test";
//    static String[] allGlycanNames = {"2-FL","3-FL","A2","L735","L736","Man5","Man6",
//            "Man7d1","Man8","Man9","NA2","NA3","NA4","NGA2","NGA3","NGA4"};
    static String[] allGlycanNames = {"Man5_test"};
    /**
     * This variable is the path of the structure file to be loaded.
     */
    static String structureFilePath = "./structure-files/"+glycanName+".glycoct_condensed";
    static String spectraFolderPath = "D:\\Codes\\IntelliJ_Workspace\\GlycanTools\\input-spectra\\"+glycanName;

    private static GlycanRenderer glycanRenderer = new GlycanRendererAWT();
    private static BuilderWorkspace builderWorkspace = new BuilderWorkspace(glycanRenderer);

    /**
     * @param args currently not used
     */
    public static void main(String[] args) {
        for(String glycanStr: allGlycanNames){
            glycanName = glycanStr;
            structureFilePath = "./structure-files/"+glycanName+".glycoct_condensed";
            spectraFolderPath = "D:\\Codes\\IntelliJ_Workspace\\GlycanTools\\input-spectra\\"+glycanName;
            labelSpectra(structureFilePath, spectraFolderPath);
//            printEntropyOfSpectra(spectraFolderPath);
        }
//        labelSpectra(structureFilePath, spectraFolderPath);
    }

    /**
     * This function is used to
     * @param glycanPath path of the structure file of a glycan
     * @param spectraFolder path of the folder of spectra
     */
    public static void labelSpectra(String glycanPath, String spectraFolder){
        // get glycan
        Glycan g = ReaderUtil.getGlycan(glycanPath);
        double mass = g.computeMass();
        Residue r = g.getRoot();
        walk(r, "");
//        Print.pl("resmass: "+ r.getResidueName()+ "\t" + r.getType().getMass());
//        Vector<Linkage> allLink = r.getChildrenLinkages();
//        for(Linkage ln: allLink){
//            Residue cr = ln.getChildResidue();
//            Print.pl("\tlink: "+ cr.getResidueName()+ "\t" +cr.getType().getMass());
//        }

//        // read spectra and filter out noise
//        ArrayList<PeakList> originalSpectra = ReaderUtil.readAllPeakList(spectraFolder);
//        ArrayList<PeakList> filteredSpectra = new ArrayList<PeakList>();
//        PeakFilter peakFilter = new PeakFilter(0.01);
//        for(PeakList peakList: originalSpectra){
//            if(peakList.size() > 1){
//                filteredSpectra.add( peakFilter.filter(peakList) );
//            }
//        }
//        // annotation
//        Annotator annotator = new Annotator(new GlycanFragmenter(), g, 250.);
//        for(PeakList pl: filteredSpectra){
//            annotator.annotatePeakList(pl);
//            Print.pl(pl.toStringOnlyAnnotation());
//        }
//        PeakListTree peakListTree = new PeakListTree(filteredSpectra);
//        Print.pl(""+peakListTree);
    }

//    public static double computeMass(Glycan g,Residue node) {
//        if (node == null) {
//            return 0.0D;
//        } else {
//            ResidueType type = node.getType();
//            int no_bonds = node.getNoBonds();
//            double mass = type.getMass();
//            if (node.isReducingEnd() && node.getType().makesAlditol()) {
//                mass += 2.0D * MassUtils.hydrogen.getMass();
//            }
//
//            if (node.isBracket()) {
//                int no_linked_labiles = Math.min(g.countLabilePositions(), g.countDetachedLabiles());
//                mass -= (double)(no_bonds - no_linked_labiles) * g.substitutionMass();
//            } else if (node.isCleavage() && !node.isRingFragment()) {
//                if (node.isReducingEnd() && !node.hasChildren()) {
//                    mass += this.substitutionMass();
//                }
//            } else if (this.isDropped(type)) {
//                mass -= type.getMass() - MassUtils.water.getMass() - g.substitutionMass();
//            } else {
//                mass += (double)(this.noSubstitutions(type) - no_bonds) * g.substitutionMass();
//            }
//
//            return mass;
//        }
//    }

    public static void walk(Residue r, String prefix){
        double mass = r.getType().getMass() + (r.getType().getNoMethyls()-2) * (MassUtils.methyl.getMass() - MassUtils.hydrogen.getMass());
        Print.pl(prefix + r.getResidueName()+ "\t" + r.getType().getMass() + "\t" + mass);
        LinkedList<Linkage> allLink = r.getChildrenLinkages();
        for(Linkage ln: allLink){
            Residue cr = ln.getChildResidue();
            walk(cr, prefix+"\t");
        }
    }

    /**
     * Print the cross entropy of a spectrum and a uniform spectrum with the
     * same number of peaks.
     * @param spectraFolderPath
     */
    public static void printEntropyOfSpectra(String spectraFolderPath){
        ArrayList<PeakList> originalSpectra = ReaderUtil.readAllPeakList(spectraFolderPath, ".mzXML");
        ArrayList<PeakList> filteredSpectra = new ArrayList<PeakList>();
        PeakFilter peakFilter = new PeakFilter(0.01);
        for(PeakList peakList: originalSpectra){
            if(peakList.size() > 1){
                filteredSpectra.add( peakFilter.filter(peakList) );
            }
        }
        for(PeakList peakList: filteredSpectra){
            double totalIntensity = 0;
            for(Peak p: peakList){
                totalIntensity += p.getRelativeIntensity();
            }
            // compute cross entropy
            double crossEntropy = 0;
            for (Peak p: peakList){
                crossEntropy += - Math.log(p.getRelativeIntensity() / totalIntensity) / Math.log(2.);
            }
            crossEntropy /= - Math.log(1. / peakList.size()) / Math.log(2.);
            Print.pl(peakList.toTitleString()+"\n\t" + crossEntropy);
        }

    }

}
