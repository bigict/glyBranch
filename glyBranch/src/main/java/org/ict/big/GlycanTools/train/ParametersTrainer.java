package org.ict.big.GlycanTools.train;

import org.eurocarbdb.application.glycanbuilder.BuilderWorkspace;
import org.eurocarbdb.application.glycanbuilder.renderutil.GlycanRenderer;
import org.eurocarbdb.application.glycanbuilder.renderutil.GlycanRendererAWT;
import org.ict.big.GlycanTools.annotation.AnnotationScorer;
import org.ict.big.GlycanTools.annotation.MultiAnnotator;
import org.ict.big.GlycanTools.debug.Print;
import org.ict.big.GlycanTools.io.ReaderUtil;
import org.ict.big.GlycanTools.options.ScoringConfig;
import org.ict.big.GlycanTools.spectrumModel.GlycanCandidate;
import org.ict.big.GlycanTools.spectrumModel.GlycanCandidates;
import org.ict.big.GlycanTools.spectrumModel.PeakList;
import org.ict.big.GlycanTools.spectrumModel.PeakListMultiAnnTree;
import org.ict.big.GlycanTools.spectrumModel.structureLibrary.CarbohydrateRecord;
import org.ict.big.GlycanTools.spectrumModel.structureLibrary.TextFileStructureLibrary;
import org.ict.big.GlycanTools.spectrumProcesses.PeakFilter;
import org.ict.big.GlycanTools.spectrumProcesses.PeakListFilter;

import java.io.*;
import java.util.ArrayList;
import java.text.DecimalFormat;

public class ParametersTrainer {

    private static GlycanRenderer glycanRenderer = new GlycanRendererAWT();
    private static BuilderWorkspace builderWorkspace = new BuilderWorkspace(glycanRenderer);
    static String structureLibraryFilePath = "./structure-library/carbbank_dict.gwd";
    static String correctIdFilePath = "./structure-library/correct_structure.txt";
    static String spectraBaseFolder = "./input-spectra/Top5_0.1mv/";

    public static void main(String[] args){
        train();
    }

    public static void train(){

        // initial structure library
        TextFileStructureLibrary library = new TextFileStructureLibrary(structureLibraryFilePath);
        library.init();
        // read all training samples
        ArrayList<CorrectGlycan> correctGlycans = ReaderUtil.readCorrectGlycan(correctIdFilePath);
        // initial config
        ScoringConfig config = new ScoringConfig();
        // get all alpha
        ArrayList<Double> allAlphas = ParametersGenerator.generateAlphaInTanh(0.01, 1.009, 0.1);
        // Initial the array containing all scores trained
        ArrayList<ArrayList<ScoreAndRank>> trainingScores = new ArrayList<>();
        ArrayList<String> trainedGlycanNames = new ArrayList<>();

        for(CorrectGlycan correctGlycan: correctGlycans){
            // load spectra
            String spectraFolderPath = spectraBaseFolder + correctGlycan.glycanName;
            ArrayList<PeakList> originalSpectra = ReaderUtil.readAllPeakList(spectraFolderPath,
                    config.spectrumFileType);
            if(originalSpectra == null){
                continue;
            }
            // preprocess spectra
            ArrayList<PeakList> filteredSpectra = spectraPreprocess(originalSpectra, config);
            // build spectra tree
            PeakListMultiAnnTree peakListTree = new PeakListMultiAnnTree(filteredSpectra,
                    config.moleculeMassTolerance, config.precursorMassTolerance);
            // search candidates from database
            double searchMz = peakListTree.getParentMass();
            double window = searchMz / config.resolution;
            ArrayList<CarbohydrateRecord> candidates = library.searchStructureRecord(
                    searchMz - window / 2, searchMz + window / 2);
            GlycanCandidates glycanCandidates = new GlycanCandidates(candidates);
            // annotate peak list tree
            MultiAnnotator annotator = new MultiAnnotator(peakListTree, glycanCandidates, config);
            annotator.annotate();
            // A array storing all scores of this glycan
            ArrayList<ScoreAndRank> allScores = new ArrayList<>();
            // loop all alphas
            for(double alpha: allAlphas){
                config.alphaForTanh = alpha;
                // score candidates
                AnnotationScorer scorer = new AnnotationScorer(peakListTree, glycanCandidates, config);
                scorer.score();
                // get the score of the actual glycan
                GlycanCandidate correctCandidate = glycanCandidates.findCandidateById(correctGlycan.correctId);
                if(correctCandidate == null){
                    continue;
                }
                int rank = glycanCandidates.getRankOfACandidateById(correctGlycan.correctId);
                double score = correctCandidate.score;

                allScores.add(new ScoreAndRank(score, rank));
            }
            // Add a row to the trainingScores
            trainingScores.add(allScores);
            trainedGlycanNames.add(correctGlycan.glycanName);
        } // for glycans ends
        writeTrainingResultToFile(trainingScores, allAlphas, trainedGlycanNames);
    }

    public static ArrayList<PeakList> spectraPreprocess(ArrayList<PeakList> originalSpectra,
                                                        ScoringConfig scoringConfig){
        ArrayList<PeakList> filteredPeakList = new ArrayList<>();
//        PeakListFilter peakListFilter = new PeakListFilter(scoringConfig);
//        originalSpectra = peakListFilter.filterNoisedPeakListBeforePeakFiltering(originalSpectra);

        PeakFilter peakFilter = new PeakFilter(scoringConfig);
        for(PeakList peakList: originalSpectra){
            if(peakList.size() > 0){
//                Print.pl("\tFilter spectrum: "+peakList.toTitleString());
                double ratio = peakFilter.autoSetFilterRatio(peakList);
                Print.pl("\tratio = "+ratio);
                PeakList fpl = peakFilter.filter(peakList);
                filteredPeakList.add( fpl );
            }
        }

//        filteredPeakList = peakListFilter.filterNoisedPeakList(filteredPeakList);
        return filteredPeakList;
    }

    public static void writeTrainingResultToFile(ArrayList<ArrayList<ScoreAndRank>> trainingScores,
                                                 ArrayList<Double> allAlpha, ArrayList<String> glycanNames){
        DecimalFormat df = new DecimalFormat("#0.0000");
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream("./Top5_0.1mv_Score_noise.txt"), "utf-8"))) {
            // write the first line containing all alpha
            writer.write("#glycan\\alpha");
            for(Double d: allAlpha){
                writer.write("\t"+df.format(d));
            }
            writer.write("\n");
            // write all glycans
            for(int i = 0; i < glycanNames.size(); i++){
                writer.write(glycanNames.get(i));
                ArrayList<ScoreAndRank> scores = trainingScores.get(i);
                for(ScoreAndRank d: scores){
                    writer.write("\t"+d.toString());
                }
                writer.write("\n");
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ScoreAndRank{
    public double score;
    public int rank;
    public ScoreAndRank(double s, int r){
        score = s;
        rank = r;
    }

    @Override
    public String toString() {
        DecimalFormat df = new DecimalFormat("#0.0000");
        return df.format(score)+"/"+rank;
    }
}
