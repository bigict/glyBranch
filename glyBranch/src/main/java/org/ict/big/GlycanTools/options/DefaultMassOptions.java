package org.ict.big.GlycanTools.options;

import org.eurocarbdb.application.glycanbuilder.massutil.MassOptions;

/**
 * This class stores the default mass option for current MS experiment.
 * @author Jingwei Zhang
 */
public class DefaultMassOptions {
    public static MassOptions getDefaultMassOptions(){
        MassOptions massOption = new MassOptions();
//        massOption.setIsotope(MassOptions.ISOTOPE_MONO);
        massOption.setDerivatization(MassOptions.PERMETHYLATED);
//        massOption.setReducingEndType(ResidueType.createFreeReducingEnd());
//        massOption.ION_CLOUD = new IonCloud("Na");
//        massOption.NEUTRAL_EXCHANGES = new IonCloud();
        return massOption;
    }
}
