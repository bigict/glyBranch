package org.ict.big.GlycanTools.io;

import org.ict.big.GlycanTools.debug.Print;
import org.ict.big.GlycanTools.spectrumModel.Peak;
import org.ict.big.GlycanTools.spectrumModel.PeakList;

import java.io.*;
import java.util.ArrayList;

public class TextSpectrumReader extends AbstractSpectrumReader {


    File spectrumFile = null;

    /**
     * Get a peak list file from a file
     *
     * @param path the path of a file.
     * @return A PeakList instance, or null if error occurs in reading **peaks**.
     *         Errors in reading precursor masses are ignored.
     * @throws Exception when IO error occurs or double conversion error.
     */
    @Override
    public PeakList getPeakListFromFile(String path) throws Exception{
        spectrumFile = new File(path);
        ArrayList<Double> preMasses = null;
        try {
            preMasses = getPrecursorMassesFromFileName(spectrumFile.getName());
        } catch (Exception e){
            Print.pl("File name error!");
        }
        ArrayList<Peak> allPeaks = getPeaks(spectrumFile);
        PeakList peakList = null;
        if(allPeaks != null){
            peakList = new PeakList(allPeaks, preMasses);
        }
        return peakList;
    }

    /**
     * Get all peaks from text file.
     * All lines stating with '#' are regarded as comments.
     *
     * @param file
     * @return
     */
    public ArrayList<Peak> getPeaks(File file) throws Exception {
        ArrayList<Peak> peaks = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
            for(String line = br.readLine();line != null;line = br.readLine()){
                line = line.trim();
                if(line.startsWith("#")){
                    continue;
                }
                String[] sps = line.split("[\t| ]+");
                if(sps.length >= 2){
                    double mz = Double.parseDouble(sps[0]);
                    double ints = Double.parseDouble(sps[1]);
                    peaks.add(new Peak(mz, ints));
                }
            }
        }
//        catch (IOException e) {
//            Print.pl("File IO error in reading spectrum!");
//            e.printStackTrace();
//        } catch (Exception e){
//            Print.pl("The format of spectrum file contains errors!");
//            e.printStackTrace();
//        }
        return peaks;
    }



    /**
     * get precursor masses from the name of the imput file.
     * e.g. 1579.0_1084.0.txt indicates MS3->1579->1084
     * @param fileName the full name(not path) of the input file, including the extension.
     * @return
     */
    public ArrayList<Double> getPrecursorMassesFromFileName(String fileName) throws Exception{
        int lastDotIndex = fileName.lastIndexOf('.');
        String preCursorStr = fileName.substring(0, lastDotIndex);
        String[] preMassStrs = preCursorStr.split("_|-");

        ArrayList<Double> preMasses = new ArrayList<>();
        for(String mass: preMassStrs){
            double d = Double.parseDouble(mass);
            preMasses.add(d);

        }
        return preMasses;
    }

//    public static void main(String[] para){
//        String filePath = "D:\\Data\\Glycan_Data\\GIPS_experiment_dataset_TXT\\2018_03_05_doc_spectra\\Egg\\1784\\1784.00_1507.00_1288.txt";
//
//        TextSpectrumReader textSpectrumReader = new TextSpectrumReader();
//        PeakList peakList = null;
//        try {
//            peakList = textSpectrumReader.getPeakListFromFile(filePath);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        Print.pl(""+peakList.toString());
//    }
}
