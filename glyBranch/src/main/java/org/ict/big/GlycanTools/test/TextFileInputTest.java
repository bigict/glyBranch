package org.ict.big.GlycanTools.test;

import org.eurocarbdb.application.glycanbuilder.BuilderWorkspace;
import org.eurocarbdb.application.glycanbuilder.renderutil.GlycanRenderer;
import org.eurocarbdb.application.glycanbuilder.renderutil.GlycanRendererAWT;
import org.ict.big.GlycanTools.annotation.AnnotationScorer;
import org.ict.big.GlycanTools.annotation.MultiAnnotator;
import org.ict.big.GlycanTools.debug.Print;
import org.ict.big.GlycanTools.io.FileUtil;
import org.ict.big.GlycanTools.io.ReaderUtil;
import org.ict.big.GlycanTools.io.SpectrumFileType;
import org.ict.big.GlycanTools.options.ScoringConfig;
import org.ict.big.GlycanTools.spectrumModel.GlycanCandidate;
import org.ict.big.GlycanTools.spectrumModel.GlycanCandidates;
import org.ict.big.GlycanTools.spectrumModel.PeakList;
import org.ict.big.GlycanTools.spectrumModel.PeakListMultiAnnTree;
import org.ict.big.GlycanTools.spectrumModel.structureLibrary.AbstractStructureLibrary;
import org.ict.big.GlycanTools.spectrumModel.structureLibrary.CarbohydrateRecord;
import org.ict.big.GlycanTools.spectrumModel.structureLibrary.TextFileStructureLibrary;
import org.ict.big.GlycanTools.spectrumProcesses.PeakFilter;
import org.ict.big.GlycanTools.spectrumProcesses.PeakListFilter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

public class TextFileInputTest {
    static String structureLibraryFilePath = "./structure-library/carbbank_dict.gwd";
    static String spectraFileRootPath = "D:\\Data\\Glycan_Data\\GIPS_experiment_dataset_TXT\\2018_03_05_doc_spectra\\Egg";


    private static GlycanRenderer glycanRenderer = new GlycanRendererAWT();
    private static BuilderWorkspace builderWorkspace = new BuilderWorkspace(glycanRenderer);

    public static void main(String[] args){
        ScoringConfig scoringConfig = new ScoringConfig();
        scoringConfig.spectrumFileType = SpectrumFileType.TXT;
        TextFileStructureLibrary library = new TextFileStructureLibrary(structureLibraryFilePath);
        library.init();
        Print.pl("library size: "+library.size());
//        for(CarbohydrateRecord record: library.allStructures){
//            Print.pl(""+record);
//        }
        for(File spectraFolder: FileUtil.listDirs(new File(spectraFileRootPath))){
            Print.pl("dealing with folder: "+spectraFolder.getName());
            score(spectraFolder, library, scoringConfig);
        }
    }

    public static void score(File spectraFolder, AbstractStructureLibrary library, ScoringConfig config){
        ScoringConfig scoringConfig = new ScoringConfig();

        // load spectra, preprocess spectra and build spectra tree.
        Print.pl(spectraFolder.getName());
        ArrayList<PeakList> originalSpectra = ReaderUtil.readAllPeakList(spectraFolder.getPath(),
                config.spectrumFileType);
        ArrayList<PeakList> filteredPeakList = new ArrayList<>();
        ArrayList<PeakList> tempPeakList = null;
        PeakListFilter peakListFilter = new PeakListFilter(scoringConfig);
        //originalSpectra = peakListFilter.filterNoisedPeakListBeforePeakFiltering(originalSpectra);

        PeakFilter peakFilter = new PeakFilter(scoringConfig);
        for(PeakList peakList: originalSpectra){
            if(peakList.size() > 0){
//                Print.pl("\tFilter spectrum: "+peakList.toTitleString());
//                double ratio = peakFilter.autoSetFilterRatio(peakList);
//                Print.pl("\tratio = "+ratio);
                PeakList fpl = peakFilter.filter(peakList);
                filteredPeakList.add( fpl );
            }
        }


        //filteredPeakList = peakListFilter.filterNoisedPeakList(filteredPeakList);

        PeakListMultiAnnTree peakListTree = new PeakListMultiAnnTree(filteredPeakList,
                2.0,
                scoringConfig.precursorMassTolerance);
        Print.pl(""+peakListTree);

        // get candidates
        double searchMz = peakListTree.getParentMass();
        double window = searchMz / config.resolution;
        ArrayList<CarbohydrateRecord> candidates = library.searchStructureRecord(
                searchMz - window / 2, searchMz + window / 2);
//        Print.pl("Candidates: "+ candidates.size());
//        for(CarbohydrateRecord record: candidates){
//            Print.pl("\t" + record);
//        }
        GlycanCandidates glycanCandidates = new GlycanCandidates(candidates);
        // annotate peak list tree
        MultiAnnotator annotator = new MultiAnnotator(peakListTree, glycanCandidates, scoringConfig);
        annotator.annotate();
//        Print.pl("" + peakListTree);

        // score candidates
        AnnotationScorer scorer = new AnnotationScorer(peakListTree, glycanCandidates, scoringConfig);
        scorer.score();
        Print.pl("candidates:\n"+glycanCandidates);
        writeToFile(glycanCandidates.toString(), "./scoring-result/"+spectraFolder.getName()+".txt");

    }

    public static void writeToFile(String s, String pathStr){
        Path path = Paths.get(pathStr);
        try {
            Files.write(path, s.getBytes(), StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);
        } catch (IOException e) {
            Print.pl("Write to file error!");
            e.printStackTrace();
        }
    }

}
