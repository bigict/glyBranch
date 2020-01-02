package org.ict.big.GlycanTools.spectrumModel.structureLibrary;

import org.eurocarbdb.MolecularFramework.io.CarbohydrateSequenceEncoding;
import org.eurocarbdb.application.glycanbuilder.*;
import org.eurocarbdb.application.glycanbuilder.massutil.MassOptions;
import org.eurocarbdb.application.glycanbuilder.renderutil.GlycanRenderer;
import org.eurocarbdb.application.glycanbuilder.renderutil.GlycanRendererAWT;
import org.ict.big.GlycanTools.debug.Print;
import org.ict.big.GlycanTools.io.GlycanImporter;
import org.ict.big.GlycanTools.options.DefaultMassOptions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>This class represents a structure library initialed from a text file.</p>
 * <p>Every record should be structured like below:</p>
 *
 * <p>A line starting with ";" is regarded as comments.
 *    If a line at top of this file starts with ";CarbohydrateSequenceEncoding: XXX",
 *    it indicates that the coding format of carbohydrate Sequences is XXX.</p>
 */
public class TextFileStructureLibrary extends AbstractStructureLibrary {

    private String sequenceEncodingIndicator = ";CarbohydrateSequenceEncoding:";
    private String IDIndicator = "ID:";
    private String structureIndicator = "structure:";
    private String massIndicator = "mass:";
    private String endOfRecordIndicator = "end";
    private String beginOfRecordIndicator = "start";
    private String textFilePath;

    /**
     * The type of encoding of carbohydrate sequences in the text file.
     */
    public CarbohydrateSequenceEncoding carbohydrateSequenceEncoding
            = CarbohydrateSequenceEncoding.glycoct_condensed;

    /**
     * All carbohydrate records stored in this library.
     */
    ArrayList<CarbohydrateRecord> allStructures;


    public TextFileStructureLibrary(String libraryFilePath){
        this.textFilePath = libraryFilePath;
        this.allStructures = new ArrayList<>();
    }

    public String getTextFilePath() {
        return textFilePath;
    }

    public void setTextFilePath(String textFilePath) {
        this.textFilePath = textFilePath;
    }

    public CarbohydrateSequenceEncoding getCarbohydrateSequenceEncoding() {
        return carbohydrateSequenceEncoding;
    }

    public void setCarbohydrateSequenceEncoding(CarbohydrateSequenceEncoding carbohydrateSequenceEncoding) {
        this.carbohydrateSequenceEncoding = carbohydrateSequenceEncoding;
    }


    @Override
    public void init() {
        this.readStructuresFromFile();
    }

    /**
     * Get structures the m/z of which are between massLeft and massRight.
     *
     * @param massLeft  the lowest m/z allowed
     * @param massRight the higest m/z allowed
     * @return a list of CarbohydrateRecord objects
     */
    @Override
    public ArrayList<CarbohydrateRecord> searchStructureRecord(double massLeft, double massRight) {
        ArrayList<CarbohydrateRecord> ret = new ArrayList<>();
        for(CarbohydrateRecord r: allStructures){
            if(r.getMass() >= massLeft && r.getMass() <= massRight){
                ret.add(r);
            }
        }
        return ret;
    }

    /**
     * Get structures the m/z of which are between massLeft and massRight.
     *
     * @param massLeft  the lowest m/z allowed
     * @param massRight the higest m/z allowed
     * @return a list of Glycan objects
     */
    @Override
    public ArrayList<Glycan> searchStructure(double massLeft, double massRight) {
        return null;
    }

    /**
     * Get the number of structures in this library.
     *
     * @return An integer indicating the number
     */
    @Override
    public int size() {
        return allStructures.size();
    }

    /**
     * Read all structures from a text file.
     */
    private void readStructuresFromFile(){
        List<String> allLines = null;
        try{
            allLines = Files.readAllLines(Paths.get(this.textFilePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(allLines != null){
            GlycanImporter gi = new GlycanImporter(false);
            MassOptions massOptions = DefaultMassOptions.getDefaultMassOptions();
            String ID = "";
            String glycanStr = "";
            String mass = "";
            for(String line: allLines){
                if(line.startsWith(beginOfRecordIndicator)){
                    ID = "";
                    glycanStr = "";
                    mass = "";
                } else if(line.startsWith(sequenceEncodingIndicator)){
                    String encodingStr = line.substring(sequenceEncodingIndicator.length());
                    encodingStr = encodingStr.trim();
                    CarbohydrateSequenceEncoding encoding = null;
                    try {
                        encoding = CarbohydrateSequenceEncoding.forId(encodingStr);
                    } catch (Exception e) {
                        Print.pl("Unsupported Encoding!");
                        e.printStackTrace();
                    }
                    if(encoding != null){
                        setCarbohydrateSequenceEncoding(encoding);
                    }
                } else if(line.startsWith(IDIndicator)){
                    ID = line.substring(IDIndicator.length()).trim();

                } else if(line.startsWith(massIndicator)){
                    mass = line.substring(massIndicator.length()).trim();

                } else if(line.startsWith(structureIndicator)){
                    glycanStr = line.substring(structureIndicator.length()).trim();

                } else if(line.startsWith(endOfRecordIndicator)){
                    double m = Double.parseDouble(mass);
                    Glycan g = null;
                    try {
                        g = gi.parse(glycanStr, this.carbohydrateSequenceEncoding, massOptions);
                    } catch (Exception e) {
                        Print.pl("Conversion from text encoding to Glycan object encounters an error!");
                        e.printStackTrace();
                    }
                    if(g != null){
                        CarbohydrateRecord record = new CarbohydrateRecord(g, ID, m);
                        this.allStructures.add(record);
                    }
                }
            }
        }
    }

//    private parse


}

class TextFileStructureLibraryTester{
    private static GlycanRenderer glycanRenderer = new GlycanRendererAWT();
    private static BuilderWorkspace builderWorkspace = new BuilderWorkspace(glycanRenderer);
    public static void main(String[] paras) throws Exception {
//        String test = "freeEnd--??1D-Glc--6b1D-Glc,p(--3b1D-Glc,p)--6b1D-Glc,p--6b1D-Glc,p(--3b1D-Glc,p)--6b1D-Glc,p$MONO,perMe,Na,0,freeEnd";
//        Glycan g = GWSParser.fromString(test, DefaultMassOptions.getDefaultMassOptions());
//        Print.pl(""+g.computeMZ());
        TextFileStructureLibrary library = new TextFileStructureLibrary("./structure-library/carbbank_dict.gwd");
        library.init();
        Print.pl("library size: "+library.allStructures.size());
        for(CarbohydrateRecord record: library.allStructures){
            Print.pl(""+record);
        }

        ArrayList<CarbohydrateRecord> candiates = library.searchStructureRecord(1579-1, 1579 + 1);
        Print.pl("Searched result:");
        for(CarbohydrateRecord record: candiates){
            Print.pl(""+record);
        }
    }
}