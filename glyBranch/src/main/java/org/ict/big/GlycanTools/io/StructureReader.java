package org.ict.big.GlycanTools.io;

import org.eurocarbdb.MolecularFramework.io.*;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.application.glycanbuilder.Glycan;
import org.eurocarbdb.application.glycanbuilder.converterGlycoCT.GlycoCTParser;
import org.eurocarbdb.application.glycanbuilder.massutil.MassOptions;
import org.ict.big.GlycanTools.options.DefaultMassOptions;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * This class is used to read a glycan structure from file.
 * @author Jingwei Zhang
 */
public class StructureReader extends GlycoCTParser {

    /**
     * The encoding format of files to be readed.
     */
    private CarbohydrateSequenceEncoding seqEnconding;

    /**
     * Importer of a type of sequence encoding.
     */
    private SugarImporter sugarImporter;

    private MassOptions massOptions;

    private static boolean tolerate_unknown_residues = false;

    /**
     * Instantiates a new structure reader using the default enconding.
     * @throws Exception Throws a exception when the enconding is not supported.
     */
    public StructureReader() throws Exception {
        super(tolerate_unknown_residues);
        seqEnconding = CarbohydrateSequenceEncoding.glycoct_xml;
        sugarImporter = SugarImporterFactory.getImporter(seqEnconding);
        massOptions = DefaultMassOptions.getDefaultMassOptions();
    }

    /**
     * Instantiates a new structure reader by specifying the
     * encoding of input file.
     * @param encoding the sequence encoding of input file
     */
    public StructureReader(CarbohydrateSequenceEncoding encoding) throws Exception {
        super(tolerate_unknown_residues);
        seqEnconding = encoding;
        sugarImporter = SugarImporterFactory.getImporter(seqEnconding);
        massOptions = DefaultMassOptions.getDefaultMassOptions();
    }


    /**
     * Constructures a Glycan object from a specific text file
     * @param path the path of the structure file
     * @return  a Glycan object
     * @throws Exception when error occurs in conversion
     */
    public Glycan getGlycan(String path) throws Exception {
        String t_carbseq = readSequenceFromFile(path);
        Sugar t_sugar;
        t_sugar = sugarImporter.parse(t_carbseq);
        return fromSugar(t_sugar, massOptions);
    }



    /**
     * This function reads the sequence from a structure file.
     * @param path The path of the structure file.
     * @return The sequence string.
     */
    private String readSequenceFromFile(String path) throws IOException {
        String seq = "";
        seq = new String(Files.readAllBytes(Paths.get(path)));
        seq = seq.trim();
        return seq;
    }

//    public static void main(String[] args) {
//        StructureReader structureReader = new
//    }
}