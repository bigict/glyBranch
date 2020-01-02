package org.ict.big.GlycanTools;

import de.vandermeer.asciitable.AsciiTable;
import org.apache.commons.io.FileUtils;
import org.eurocarbdb.application.glycanbuilder.BaseDocument;
import org.eurocarbdb.application.glycanbuilder.BuilderWorkspace;
import org.eurocarbdb.application.glycanbuilder.Glycan;
import org.eurocarbdb.application.glycanbuilder.GlycanDocument;
import org.eurocarbdb.application.glycanbuilder.linkage.LinkageStyle;
import org.eurocarbdb.application.glycanbuilder.linkage.LinkageStyleDictionary;
import org.eurocarbdb.application.glycanbuilder.renderutil.GlycanRenderer;
import org.eurocarbdb.application.glycanbuilder.renderutil.GlycanRendererAWT;
import org.eurocarbdb.application.glycanbuilder.renderutil.SVGUtils;
import org.eurocarbdb.application.glycanbuilder.util.GraphicOptions;
import org.ict.big.GlycanTools.annotation.AnnotationScorer;
import org.ict.big.GlycanTools.annotation.MultiAnnotator;
import org.ict.big.GlycanTools.train.CorrectGlycan;
import org.ict.big.GlycanTools.debug.Print;
import org.ict.big.GlycanTools.io.ReaderUtil;
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
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Vector;

public class Main {
//"D:\\Sync\\Desktop\\all glycan\\mzXML\\0.00mv\\"

    static String structureLibraryFilePath = "./structure-library/carbbank_dict.gwd"; //carbbank_dict
    static String correctIdFilePath = "./structure-library/correct_structure.txt";
    static String spectraBaseFolderPath = "./input_spectra_data/test/";
    static String outputFolder = "./scoring-result/0.1mv-test/";
//    static String outputFolder = "./scoring-result/0.0mv-igg/";
    static String glycanName = "";
    static String correctId = "";
    static boolean PrintMidResult=false;
    static AsciiTable at = new AsciiTable();

    public static GlycanRenderer glycanRenderer = new GlycanRendererAWT();
    private static BuilderWorkspace workspace = new BuilderWorkspace(glycanRenderer);
    private static GlycanDocument glycanDocument = null;


    /**
     * A main function for testing.
     * @param args
     */
    public static void main(String[] args) {
        for(int i = 0; i < 1; i++) {
            for (int j = 0; j < 1; j++) {
                customizeWorkspace();
                TextFileStructureLibrary library = new TextFileStructureLibrary(structureLibraryFilePath);
                library.init();
                ArrayList<CorrectGlycan> correctGlycans = ReaderUtil.readCorrectGlycan(correctIdFilePath);
//        at.addRule();
                at.addRow("Glycan", "size", "ID", "Score", "Rank",
                        //"MNa+", "Tmass",
                        "second");
//        at.addRule();

                for (CorrectGlycan correctG : correctGlycans) {
                    glycanName = correctG.glycanName;
                    correctId = correctG.correctId;
                    String spectraFolderPath = spectraBaseFolderPath + glycanName;
                    if (!Files.exists(Paths.get(spectraFolderPath))) {
                        continue;
                    }
                    ScoringConfig scoringConfig = new ScoringConfig();
                    scoringConfig.alphaForTanh = 1;
                    scoringConfig.minFilterRatio=0.01*i;
                    scoringConfig.ScoringFunction=j;
                    score(spectraFolderPath, library, scoringConfig);
                }
            }
        }
        String rend = at.render();
        Print.pl(rend);


    }

    public static void score(String spectraFolderPath, AbstractStructureLibrary library, ScoringConfig scoringConfig){

        // load spectra, preprocess spectra and build spectra tree.
        if (PrintMidResult) {
            Print.pl(spectraFolderPath);
        }
        ArrayList<PeakList> originalSpectra = ReaderUtil.readAllPeakList(spectraFolderPath,
                scoringConfig.spectrumFileType);
        ArrayList<PeakList> filteredPeakList = new ArrayList<>();
        ArrayList<PeakList> tempPeakList = null;
        PeakListFilter peakListFilter = new PeakListFilter(scoringConfig);
        originalSpectra = peakListFilter.filterNoisedPeakListBeforePeakFiltering(originalSpectra);

        PeakFilter peakFilter = new PeakFilter(scoringConfig);
        for(PeakList peakList: originalSpectra){
            if(peakList.size() >=scoringConfig.minNumberOfPeaks){
                if (scoringConfig.minFilterRatio!=0) {
                    if (PrintMidResult) {
                        Print.pl("\tFilter spectrum: " + peakList.toTitleString());
                        double ratio = peakFilter.autoSetFilterRatio(peakList);
                        Print.pl("\tratio = " + ratio);
                    }
                    PeakList fpl = peakFilter.filter(peakList);
                    filteredPeakList.add(fpl);
                }
                else {
                    filteredPeakList.add(peakList);
                }

            }
        }
        if (PrintMidResult) {
            Print.pl("filteredPeakList.size = " + filteredPeakList.size());
        }
//        filteredPeakList = peakListFilter.filterNoisedPeakList(filteredPeakList);
//            Print.pl("filteredPeakList.size = " + filteredPeakList.size());
        PeakListMultiAnnTree peakListTree = new PeakListMultiAnnTree(filteredPeakList,
                2.0,
                scoringConfig.precursorMassTolerance);
//        Print.pl(""+peakListTree);

        // get candidates
        double searchMz = peakListTree.getParentMass();
        double window = searchMz / scoringConfig.resolution;
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
//        Print.pl("candidates:\n"+glycanCandidates);
        writeToFile(glycanCandidates.toString(), outputFolder+glycanName+".txt");

        // for table
        GlycanCandidate correctCandidate = glycanCandidates.findCandidateById(correctId);
        if(correctCandidate != null){
            at.addRow(glycanName, candidates.size(), correctId,
                    String.format("%.4f", correctCandidate.score), glycanCandidates.getRankOfACandidateById(correctId)
                    //, peakListTree.getParentMass(), String.format("%.3f", correctCandidate.record.mass)
                    , String.format("%.4f", glycanCandidates.getSecondlyLargestScore())
                    );
            //at.addRule();
        }
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

    public static void customizeWorkspace(){
//        GraphicOptions options = new GraphicOptions();
        workspace.getGraphicOptions().NOTATION = GraphicOptions.NOTATION_SNFG;//GraphicOptions.NOTATION_CFG;
        glycanRenderer.setGraphicOptions(workspace.getGraphicOptions());
        workspace.getGraphicOptions().DISPLAY = GraphicOptions.DISPLAY_CUSTOM;//"custom";
        workspace.getGraphicOptions().MARGIN_TOP_CUSTOM = 10;
        workspace.getGraphicOptions().MARGIN_BOTTOM_CUSTOM = 10;
        workspace.getGraphicOptions().MARGIN_LEFT_CUSTOM = 10;
        workspace.getGraphicOptions().MARGIN_RIGHT_CUSTOM = 10;
        try {
            // 使用反射配置 Linkage 显示信息
            LinkageStyleDictionary dictionary = glycanRenderer.getLinkageStyleDictionary();
            Field stylesField = LinkageStyleDictionary.class.getDeclaredField("styles");
            stylesField.setAccessible(true);
            LinkedList<LinkageStyle> linkageStyles = (LinkedList<LinkageStyle>) stylesField.get(dictionary);

            Field showInfoField = LinkageStyle.class.getDeclaredField("show_info");
            showInfoField.setAccessible(true);
            /**
             * Linkage 信息，一个三位长的字符串，第一位表示 [3, 6], 第二位表示 [α, β], 第三位表示左 linkage
             * 0 表示不绘制，其他字符表示绘制
             */
            for (LinkageStyle style : linkageStyles) {
                showInfoField.set(style, "000");
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Print.pl("error in Main!!");
        }
    }
}
