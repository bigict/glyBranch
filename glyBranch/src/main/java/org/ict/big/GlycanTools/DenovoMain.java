package org.ict.big.GlycanTools;

import org.eurocarbdb.MolecularFramework.io.CarbohydrateSequenceEncoding;
import org.eurocarbdb.application.glycanbuilder.BuilderWorkspace;
import org.eurocarbdb.application.glycanbuilder.Glycan;
import org.eurocarbdb.application.glycanbuilder.massutil.MassOptions;
import org.eurocarbdb.application.glycanbuilder.renderutil.GlycanRenderer;
import org.eurocarbdb.application.glycanbuilder.renderutil.GlycanRendererAWT;
import org.ict.big.GlycanTools.annotation.denovo.DenovoEmuSetPrinter;
import org.ict.big.GlycanTools.annotation.denovo.EnumrationalAnnotator;
import org.ict.big.GlycanTools.debug.Print;
import org.ict.big.GlycanTools.io.GlycanImporter;
import org.ict.big.GlycanTools.io.ReaderUtil;
import org.ict.big.GlycanTools.options.DefaultMassOptions;
import org.ict.big.GlycanTools.spectrumModel.IsotopicDistribution;
import org.ict.big.GlycanTools.spectrumModel.IsotopicPeakDistributionCaculator;
import org.ict.big.GlycanTools.spectrumModel.PeakList;
import org.ict.big.GlycanTools.spectrumModel.oldTree.PeakListTree;
import org.ict.big.GlycanTools.spectrumModel.compositionDenovo.FragmentComposition;
import org.ict.big.GlycanTools.spectrumProcesses.PeakFilter;
import org.ict.big.GlycanTools.spectrumProcesses.SpectrumPreprocessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class DenovoMain {

//    static String[] allGlycanNames = {"2-FL","3-FL","A2","L735","L736","Man5","Man6",
//            "Man7d1","Man8","Man9","NA2","NA3","NA4","NGA2","NGA3","NGA4"};

//    static String[] allGlycanNames = {"2-FL", "A2", "L602", "L603", "L735", "L736", "Man5",
//        "Man6", "Man7D1", "NA2", "NA3", "NA4", "NGA2", "NGA3", "NGA4", // standard
//        "RNaseB_1579", "RNaseB_1783", "RNaseB_1988", "RNaseB_2192", "RNaseB_2396", // rnaseb
//        "IgG_1835", "IgG_2040", "IgG_2070", "IgG_2081", "IgG_2244", "IgG_2285",
//        "IgG_2401", "IgG_2605", "IgG_2850", "IgG_2966", "IgG_3213"}; // IgG

    static String[] allGlycanNames = {"IgG_3213"}; //"Man5_test"

    private static GlycanRenderer glycanRenderer = new GlycanRendererAWT();
    private static BuilderWorkspace builderWorkspace = new BuilderWorkspace(glycanRenderer);

    public static void main(String[] args) {
//        MonosaccharideLibrary.test();
        for(String glycanName: allGlycanNames){
            Print.pl("GlycanName:"+glycanName);
            String spectraFolderPath = "./input-spectra/Top5_jfs/0.1mv/"+glycanName;
            String structureFilePath = "./structure-files/"+glycanName+".glycoct_condensed";
            if(!Files.exists(Paths.get(spectraFolderPath))){
                continue;
            }

            labelSpectraByEnum(spectraFolderPath, structureFilePath, glycanName);
        }
//        String glycanStr = "freeEnd--??1D-GlcNAc,p(--??1D-GlcNAc,p--??1D-Man,p((--??1D-Man,p--??1D-GlcNAc,p--??1D-Gal,p/#zcleavage)--??1D-GlcNAc,p)--??1D-Man,p--??1D-GlcNAc,p--??1D-Gal,p--??2D-NeuAc,p)--??1L-Fuc,p$MONO,perMe,Na,0,freeEnd";
//        GlycanImporter gi = new GlycanImporter(false);
//        MassOptions massOptions = DefaultMassOptions.getDefaultMassOptions();
//        Glycan g = null;
//        try {
//            g = gi.parse(glycanStr, CarbohydrateSequenceEncoding.GWS, massOptions);
//        } catch (Exception e) {
//            Print.pl("Conversion from text encoding to Glycan object encounters an error!");
//            e.printStackTrace();
//        }
//        IsotopicPeakDistributionCaculator idc = new IsotopicPeakDistributionCaculator();
//        IsotopicDistribution d = idc.computeIsotopicDistributionCarbonOnly(g);
//        Print.pl(""+d);
    }

    public static void labelSpectraByEnum(String spectraFolder, String glycanPath, String glycanName){
        ArrayList<PeakList> originalSpectra = ReaderUtil.readAllPeakList(spectraFolder, ".mzXML");
        ArrayList<PeakList> filteredSpectra = new ArrayList<PeakList>();
        PeakFilter peakFilter = new PeakFilter(0.01);
        for(PeakList peakList: originalSpectra){
            if(peakList.size() > 1){
                filteredSpectra.add( peakFilter.filter(peakList) );
            }
        }
        SpectrumPreprocessor spp = new SpectrumPreprocessor();
        for(PeakList pl: filteredSpectra){
            spp.prePorcess(pl);
        }
        // annotate with true glycan
//        Glycan g = ReaderUtil.getGlycan(glycanPath);
//        ArrayList<PeakList> filteredSpectraClone = new ArrayList<>();
//        for(PeakList pl: filteredSpectra){
//            filteredSpectraClone.add(pl.clone());
//        }
//        Annotator trueAnnotator = new Annotator(new GlycanFragmenter(), g, 250.);
//        for(PeakList pl: filteredSpectraClone){
//            trueAnnotator.annotatePeakList(pl);
//        }
//        Print.pl("True annotation:\n"+ filteredSpectraClone.get(0));

//        Print.pl(filteredSpectra.get(0).toString());
        PeakListTree peakListTree = new PeakListTree(filteredSpectra);
        Print.pl(""+peakListTree);

        EnumrationalAnnotator annotator = new EnumrationalAnnotator(peakListTree, 250.);
        FragmentComposition precursorComposition = annotator.annotate();
        DenovoEmuSetPrinter desp = new DenovoEmuSetPrinter(peakListTree, precursorComposition);
        try {
            desp.writeToFile("./jfs_denovo/0.1mv/denovo_compositon_"+glycanName+".txt");
        } catch (IOException e) {
            e.printStackTrace();
            Print.pl("Write file error!");
        }

    }



}
