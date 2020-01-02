package org.ict.big.GlycanTools.spectrumModel.structureLibrary;

import org.eurocarbdb.MolecularFramework.io.CarbohydrateSequenceEncoding;
import org.eurocarbdb.application.glycanbuilder.Glycan;

import java.util.ArrayList;

public abstract class AbstractStructureLibrary {
    /**
     * The name of this library.(e.g. carbbank, glytocan)
      */
    public String name;

    /**
     * Get the name of this library.
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of this library.
     * @param name the name to be set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Initial this library.
     */
    public abstract void init();

    /**
     * Get structures the m/z of which are between massLeft and massRight.
     * @param massLeft the lowest m/z allowed
     * @param massRight the higest m/z allowed
     * @return a list of CarbohydrateRecord objects
     */
    public abstract ArrayList<CarbohydrateRecord> searchStructureRecord(double massLeft, double massRight);

    /**
     * Get structures the m/z of which are between massLeft and massRight.
     * @param massLeft the lowest m/z allowed
     * @param massRight the higest m/z allowed
     * @return a list of Glycan objects
     */
    public abstract ArrayList<Glycan> searchStructure(double massLeft, double massRight);

    /**
     * Get the number of structures in this library.
     * @return An integer indicating the number
     */
    public abstract int size();
}
