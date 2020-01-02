package org.ict.big.GlycanTools.io;

import org.eurocarbdb.MolecularFramework.io.CarbohydrateSequenceEncoding;
import org.eurocarbdb.MolecularFramework.io.SugarImporterFactory;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.application.glycanbuilder.*;
import org.eurocarbdb.application.glycanbuilder.converterGlycoCT.GlycoCTParser;
import org.eurocarbdb.application.glycanbuilder.dataset.GWSParser;
import org.eurocarbdb.application.glycanbuilder.massutil.MassOptions;

public class GlycanImporter extends GlycoCTParser {

    public GlycanImporter(boolean tolerate) {
        super(tolerate);
    }

    public Glycan parse(String sequence, CarbohydrateSequenceEncoding encoding, MassOptions massOptions) throws Exception {
        if(encoding.equals(CarbohydrateSequenceEncoding.GWS)){
            return GWSParser.fromString(sequence, massOptions);
        } else {
            Sugar s = SugarImporterFactory.importSugar(sequence, encoding);
            return this.fromSugar(s, massOptions);
        }
    }
}
