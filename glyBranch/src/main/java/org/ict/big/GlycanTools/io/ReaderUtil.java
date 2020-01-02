package org.ict.big.GlycanTools.io;

import org.eurocarbdb.MolecularFramework.io.CarbohydrateSequenceEncoding;
import org.eurocarbdb.application.glycanbuilder.Glycan;
import org.ict.big.GlycanTools.train.CorrectGlycan;
import org.ict.big.GlycanTools.debug.Print;
import org.ict.big.GlycanTools.spectrumModel.PeakList;
import org.ict.big.GlycanTools.spectrumModel.PeakListPrecursorMassesComparator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ReaderUtil {
    /**
     * Construct a Glycan object from a structure file.
     * @param glycanPath path of the structure file of a glycan
     * @return a Glycan object or null if an error occurred
     */
    public static Glycan getGlycan(String glycanPath){
        StructureReader sr = null;
        try {
            sr = new StructureReader(CarbohydrateSequenceEncoding.glycoct_condensed);
        } catch (Exception e) {
            e.printStackTrace();
            Print.pl("Conversion error occurred!");
        }
        if(sr == null){
            return null;
        }
        Glycan glycan = null;
        try {
            glycan = sr.getGlycan(glycanPath);
        } catch (Exception e) {
            e.printStackTrace();
            Print.pl("Conversion error occurred!");
        }
        if(glycan == null){
            return null;
        }
        double mz = glycan.computeMZ();
        Print.pl("glycan m/z = " + mz);
        return glycan;
    }

    public static ArrayList<PeakList> readAllPeakList(String spectraFolderPath, String extension){
        ArrayList<PeakList> allPeakLists = new ArrayList<PeakList>();
        ArrayList<String> allPathes = FileUtil.getFilePathsByPathAndSuffix(spectraFolderPath, extension);
        MzXMLReader mzXMLReader = new MzXMLReader();
        for(String path: allPathes){
            PeakList peakList = null;
            try {
                peakList = mzXMLReader.getPeakListFromFile(path);
            } catch (Exception e) {
                e.printStackTrace();
                Print.pl("An error occurred when reads an spectrum file.");
            }
            if(peakList != null){
                allPeakLists.add(peakList);
            }
        }
        allPeakLists.sort(new PeakListPrecursorMassesComparator());
        return allPeakLists;
    }

    public static ArrayList<PeakList> readAllPeakList(String spectraFolderPath, SpectrumFileType fileType){
        ArrayList<PeakList> allPeakLists = new ArrayList<PeakList>();
        ArrayList<String> allPathes = FileUtil.getFilePathsByPathAndSuffix(spectraFolderPath,
                "." + fileType.getExtension());
        if(allPathes == null){
            return null;
        }
        AbstractSpectrumReader spectrumReader = null;
        switch (fileType){
            case TXT:
                spectrumReader = new TextSpectrumReader();
                break;
            case mzXML:
                spectrumReader = new MzXMLReader();
                break;
        }
        if(spectrumReader == null){
            return  allPeakLists;
        }
        for(String path: allPathes){
            PeakList peakList = null;
            try {
                peakList = spectrumReader.getPeakListFromFile(path);
            } catch (Exception e) {
                e.printStackTrace();
                Print.pl("An IO error occurred when reads an spectrum file.");
            }
            if(peakList != null){
                allPeakLists.add(peakList);
            }
        }
        allPeakLists.sort(new PeakListPrecursorMassesComparator());
        return allPeakLists;
    }


    public static ArrayList<CorrectGlycan> readCorrectGlycan(String filePath){
        List<String> allLines = null;
        try {
            allLines = Files.readAllLines(Paths.get(filePath));
        } catch (IOException e) {
            e.printStackTrace();
            Print.pl("read correct Glcyan error!");
        }
        if(allLines == null){
            return null;
        }
        ArrayList<CorrectGlycan> correctGlycans = new ArrayList<>();
        for(String line: allLines){
            String trimedLine = line.trim();
            if(trimedLine.startsWith("#")){
                continue;
            }
            String[] split = trimedLine.split("[\t| ]+");
            if(split.length >= 2){
                CorrectGlycan correctG = new CorrectGlycan(split[0], split[1]);
                correctGlycans.add(correctG);
            }
        }
        return correctGlycans;
    }
}
