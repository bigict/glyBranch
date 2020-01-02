package org.ict.big.GlycanTools.io;

import org.ict.big.GlycanTools.spectrumModel.PeakList;

public abstract class AbstractSpectrumReader {

    /**
     * Get a peak list file from a file
     * @param path the path of a file.
     * @return
     */
    public abstract PeakList getPeakListFromFile(String path) throws Exception;

}
