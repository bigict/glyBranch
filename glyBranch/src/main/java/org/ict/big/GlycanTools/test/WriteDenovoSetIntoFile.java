package org.ict.big.GlycanTools.test;

import org.eurocarbdb.application.glycanbuilder.BuilderWorkspace;
import org.eurocarbdb.application.glycanbuilder.Glycan;
import org.eurocarbdb.application.glycanbuilder.renderutil.GlycanRenderer;
import org.eurocarbdb.application.glycanbuilder.renderutil.GlycanRendererAWT;
import org.ict.big.GlycanTools.annotation.Annotator;
import org.ict.big.GlycanTools.annotation.denovo.DenovoSetPrinter;
import org.ict.big.GlycanTools.annotation.GlycanFragmenter;
import org.ict.big.GlycanTools.debug.Print;
import org.ict.big.GlycanTools.io.ReaderUtil;
import org.ict.big.GlycanTools.spectrumModel.PeakList;
import org.ict.big.GlycanTools.spectrumModel.oldTree.PeakListTree;
import org.ict.big.GlycanTools.spectrumProcesses.PeakFilter;

import java.io.IOException;
import java.util.ArrayList;


/**
 * This class is used to doing something temporarily.
 * @author Jingwei Zhang
 */
public class WriteDenovoSetIntoFile {

    static String glycanName = "2-FL_test";
//s
    static String[] allGlycanNames = {"NA4"};
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
        // read spectra and filter out noise
        ArrayList<PeakList> originalSpectra = ReaderUtil.readAllPeakList(spectraFolder, ".mzXML");
        ArrayList<PeakList> filteredSpectra = new ArrayList<PeakList>();
        PeakFilter peakFilter = new PeakFilter(0.01);
        for(PeakList peakList: originalSpectra){
            if(peakList.size() > 1){
                filteredSpectra.add( peakFilter.filter(peakList) );
            }
        }
        // annotation
        Annotator annotator = new Annotator(new GlycanFragmenter(), g, 250.);
        for(PeakList pl: filteredSpectra){
            annotator.annotatePeakList(pl);
            Print.pl(pl.toStringOnlyAnnotation());
        }
        PeakListTree peakListTree = new PeakListTree(filteredSpectra);
        Print.pl(""+peakListTree);
        DenovoSetPrinter denovoSetPrinter = new DenovoSetPrinter(peakListTree);
        try {
            denovoSetPrinter.writeToFile("./denovo_compositon_"+glycanName+".txt");
        } catch (IOException e) {
            e.printStackTrace();
            Print.pl("Write file error!");
        }
    }

}
