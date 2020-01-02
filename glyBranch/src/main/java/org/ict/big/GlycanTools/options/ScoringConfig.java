package org.ict.big.GlycanTools.options;

import org.eurocarbdb.application.glycanbuilder.FragmentOptions;
import org.ict.big.GlycanTools.io.SpectrumFileType;

public class ScoringConfig {

    /**
     * The type of the input spectrum file.
     */
    public SpectrumFileType spectrumFileType = SpectrumFileType.mzXML;

    /**
     * The resolution of this experiment.
     */
    public double resolution = 500.0;
    /**
     * The options of fragmenter.
     */
    public GlycanFragmentOptions fragmentOptions = new GlycanFragmentOptions();

    /**
     * The m/z tolerance of the matched theoretical peak and experimental peak.
     */
    public double annotationTolerance = 0.5;

    public double moleculeMassTolerance = 2.0;
    public double precursorMassTolerance = 1.0;

    /**
     * starting from MS0.
     */
    public double[] weightForEachStage = {0, 0, 0.25, 0.25, 0.25, 0.25};

    public double averageWeight = 0.25;

    public double isotopicTolerence = 0.1;

    public double minMz = 200.0;

    public double defaultFilterRatio = 0.01;

//    public double minFilterRatio = 0.02;
    public double minFilterRatio = 0.00;

    public double minIntsAllowedOfIsoDistribution = 1;

    public int minNumberOfIsotopicPeakGroupsMoreThan1500 = 2;

    public int minNumberOfIsotopicPeakGroupsLessThan1500 = 0;

    public double baselineMin = 2.0 + 0.001;

    public int minNumberOfPeaks = 1;

    public double alphaForTanh = 0.1;

    public boolean scoreOnlyMs2 = false;

    public boolean sigmoidNormalize = true;
//scoringFunction 用于控制使用哪个打分函数，分别对应tanhx,1,ln(x+1)
    public int ScoringFunction=0;

    public ScoringConfig(){
        fragmentOptions.MAX_NO_CLEAVAGES = 3;
        fragmentOptions.max_no_cleavages_for_very_stage = 6;
        fragmentOptions.no_cleavages_added_at_each_stage = 3;
    }
}
