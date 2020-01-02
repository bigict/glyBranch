package org.ict.big.GlycanTools.annotation;

import org.eurocarbdb.application.glycanbuilder.*;
import org.ict.big.GlycanTools.debug.Print;
import org.ict.big.GlycanTools.spectrumModel.Peak;
import org.ict.big.GlycanTools.spectrumModel.PeakList;
import org.ict.big.GlycanTools.spectrumModel.PeakWithAnnotation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class Annotator {

    GlycanFragmenter fragmenter;

    FragmentCollection ms2Fragments;

    double resolution;

    double annotaionTolenrence = 0.3;

    public Annotator(GlycanFragmenter _fragmenter, FragmentCollection _ms2Fragments, double _resolution){
        fragmenter = _fragmenter;
        ms2Fragments = _ms2Fragments;
        resolution = _resolution;
    }

    public Annotator(GlycanFragmenter _fragmenter, Glycan g, double _resolution){
        fragmenter = _fragmenter;
        ms2Fragments = fragmenter.computeAllFragments(g);
        resolution = _resolution;
    }

    public void annotatePeakList(PeakList peakList){

        FragmentCollection parentIon = getParentFragments(peakList.getPrecursorMasses());

        FragmentCollection fragments = ms2Fragments;
        if(parentIon != null){
//            Print.pl(peakList.toTitleString());
//            for(FragmentEntry f: parentIon.getFragments()){
//                Print.pl("\t"+ f.structure + "\n");
//            }
            fragments = fragmenter.computeAllFragments(parentIon);
        }
//        Print.pl("All fragments: " + fragments.size());
//        for(FragmentEntry f: fragments.getFragments()){
//             Print.pl("\t"+f.mz_ratio + "\t" + f.structure);
//             Glycan g = f.getFragment();
//             Molecule m = null;
//             try {
//                 m = g.computeMolecule();
//             } catch (Exception e) {
//                 e.printStackTrace();
//             }
//            Collection<Map.Entry<Atom,Integer>> atoms = m.getAtoms();
//            for(Map.Entry<Atom,Integer> atom: atoms){
//                Print.pl("\t\t"+atom.getKey().getName() + ": "+atom.getValue());
//            }
//        }
        for(int i = 0; i < peakList.size(); i++){
            Peak p = peakList.getPeak(i);
            double window = getWindowWidth(p.getMz());
            // find matched peaks
            FragmentCollection matchedFragments = fragmenter.filterFragmentsOutOfAGivenMzRange(fragments,
                    p.getMz() - annotaionTolenrence, p.getMz() + annotaionTolenrence);
            if(matchedFragments.size() > 0){
                // annotate this peak
                peakList.annotatePeak(i, matchedFragments);
            }
        }
    }

    protected FragmentCollection getParentFragments(ArrayList<Double> precurorMasses){
        FragmentCollection retC = null;
        for(int i = 1;i < precurorMasses.size(); i++){
            double pMass = precurorMasses.get(i);
            double window = getWindowWidth(pMass);
            if(i == 1){
                retC = fragmenter.filterFragmentsOutOfAGivenMzRange(ms2Fragments,
                        pMass - window / 2, pMass + window / 2);
            } else {
                retC = fragmenter.computeFragmentsOfAGivenMzRange(retC,
                        pMass - window / 2, pMass + window / 2);
            }
        }
        return retC;
    }

    protected double getWindowWidth(double mz){
        return mz / resolution;
    }
}
