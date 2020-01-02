package org.ict.big.GlycanTools.annotation;

import org.eurocarbdb.application.glycanbuilder.*;
import org.eurocarbdb.application.glycanbuilder.massutil.MassOptions;
import org.ict.big.GlycanTools.debug.Print;
import org.ict.big.GlycanTools.options.GlycanFragmentOptions;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This utility class is used to generate all possible fragments of a glycan
 * molecule.
 * @see org.eurocarbdb.application.glycanbuilder.Fragmenter
 * <ol>Difference comparing to Fragmenter in glycanbuilder: </ol>
 *  <li>Default value of max number of cleavages is set to 3.</li>
 * @author Jingwei Zhang
 */
public class GlycanFragmenter extends Fragmenter {

    public int no_cleavages_added_at_each_stage;

    public int max_no_cleavages_for_very_stage;

    /**
     * Initialize the fragmenter using the default options.
     */
    public GlycanFragmenter(){
        super.afragments = false;
        super.bfragments = true;
        super.cfragments = true;
        super.xfragments = false;
        super.yfragments = true;
        super.zfragments = true;
        super.internal_fragments = false;
        super.max_no_cleavages = 6;
        super.max_no_crossrings = 1;
        super.small_ring_fragments = true;
        no_cleavages_added_at_each_stage = 3;
        max_no_cleavages_for_very_stage = 6;
    }

    /**
     * Initialize the fragmenter using the given options.
     * @param opt the fragment options
     */
    public GlycanFragmenter(GlycanFragmentOptions opt){
        if (opt != null) {
            super.afragments = opt.ADD_AFRAGMENTS;
            super.bfragments = opt.ADD_BFRAGMENTS;
            super.cfragments = opt.ADD_CFRAGMENTS;
            super.xfragments = opt.ADD_XFRAGMENTS;
            super.yfragments = opt.ADD_YFRAGMENTS;
            super.zfragments = opt.ADD_ZFRAGMENTS;
            super.internal_fragments = opt.INTERNAL_FRAGMENTS;
            super.max_no_cleavages = opt.MAX_NO_CLEAVAGES;
            super.max_no_crossrings = opt.MAX_NO_CROSSRINGS;
            this.no_cleavages_added_at_each_stage = opt.no_cleavages_added_at_each_stage;
            this.max_no_cleavages_for_very_stage = opt.max_no_cleavages_for_very_stage;
        }
    }

    public void setMaxNoCleavagesAccordingToLevel(int level){
        int should = (level - 1) * no_cleavages_added_at_each_stage;
        int no = should < max_no_cleavages_for_very_stage ? should : max_no_cleavages_for_very_stage;
        super.setMaxNoCleavages(no);
    }

    /**
     * Compute all fragments of a given structure.
     * The returned collection of fragments does not contain the structure itself.
     */
    public FragmentCollection computeAllFragments(Glycan structure) {
        FragmentCollection fragments = new FragmentCollection();
        computeAllFragments(fragments, structure);
        return fragments;
    }

    /**
     * Compute all fragments of all fragments in a collection.
     * The returned collection of fragments does not contain the parent fragments themselves.
     */
    public FragmentCollection computeAllFragments(FragmentCollection fragmentsToBeFragmented) {
        FragmentCollection fragments = new FragmentCollection();
        for(FragmentEntry fe: fragmentsToBeFragmented.getFragments()){
            computeAllFragments(fragments, fe.fragment);
        }
        return fragments;
    }

    /**
     * Compute all fragments of a given structure. Return the results in
     * <code>fragments</code>.
     * The collection of fragments do not contain the structure itself.
     */
    public void computeAllFragments(FragmentCollection fragments,
                                    Glycan structure) {
        if (structure != null && !structure.isFuzzy(true)
                && !structure.hasRepetition()) {
            // remove exchanges from parent mass options
            MassOptions mass_opt = structure.getMassOptions().removeExchanges();
            Glycan parent = structure.clone();
            parent.setMassOptions(mass_opt);

            // compute fragments
            if (parent.hasLabileResidues()) {
//                Print.pl("flag");
                computeAllFragmentsWithLabiles(fragments, parent,
                        max_no_cleavages, Math.min(max_no_cleavages,
                                max_no_crossrings), mass_opt);
//                for (Glycan conf : parent.getAllLabilesConfigurations())
//                    fragments.addFragment(conf, getFragmentType(conf));
            } else {
                computeAllFragments(fragments, parent.getRoot(),
                        max_no_cleavages, Math.min(max_no_cleavages,
                                max_no_crossrings), mass_opt);
//                fragments.addFragment(parent, getFragmentType(parent));
            }
        }
    }

    /**
     * Compute all fragments of a given structure.
     * The returned new  collection of fragments does not contain the structure itself.
     */
    public FragmentCollection computeFragmentsOfAGivenMzRange(Glycan structure,
                                                              double mzLower, double mzUpper) {
        FragmentCollection fragments = new FragmentCollection();
        computeAllFragments(fragments, structure);
        ArrayList<FragmentEntry> fragsToBeRemoved = new ArrayList<FragmentEntry>();
        for(FragmentEntry f: fragments.getFragments()){
            if(f.mz_ratio > mzUpper || f.mz_ratio < mzLower){
                fragsToBeRemoved.add(f);
            }
        }
        fragments.removeFragments(fragsToBeRemoved);
        return fragments;
    }

    /**
     * Compute all fragments of given fragments.
     * The returned new  collection of fragments does not contain the structure itself.
     */
    public FragmentCollection computeFragmentsOfAGivenMzRange(FragmentCollection fs,
                                                              double mzLower, double mzUpper) {
        FragmentCollection allFragments = computeAllFragments(fs);
        FragmentCollection fgsRet = new FragmentCollection();
        for(FragmentEntry f: allFragments.getFragments()){
            if(f.mz_ratio > mzLower && f.mz_ratio < mzUpper){
                fgsRet.addFragment(f);
            }
        }
        return fgsRet;
    }

    /**
     * Filter out fragments in a collection that has a mz ration out of a given range.
     * This method returns a new FragmentCollection.
     */
    public FragmentCollection filterFragmentsOutOfAGivenMzRange(FragmentCollection fragments,
                                                              double mzLower, double mzUpper) {

        FragmentCollection fragmentsRet = new FragmentCollection();
        for(FragmentEntry f:fragments.getFragments()){
            if(f.mz_ratio <= mzUpper && f.mz_ratio >=  mzLower){
                fragmentsRet.addFragment(f);
            }
        }
        return fragmentsRet;
    }

//    public Map<String, Double> getAllPossibleMz(Glycan structure){
//        this.max_no_cleavages = 5;
//        FragmentCollection allFragments = this.computeAllFragments(structure);
//        Map<String, Double> allMz = new HashMap<>();
//        DecimalFormat decimalFormat = new DecimalFormat("0.00");
//        decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
//        for(FragmentEntry fragment: allFragments.getFragments()){
//            double mz = fragment.getMZ();
//            String mzStr = decimalFormat.format(mz);
//            if(!allMz.containsKey(mzStr)){
//                allMz.put(mzStr, mz);
//            }
//        }
//        this.max_no_cleavages = 3;
//        return allMz;
//    }

}
