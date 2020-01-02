package org.ict.big.GlycanTools.spectrumModel;

import org.eurocarbdb.application.glycanbuilder.Glycan;
import org.eurocarbdb.application.glycanbuilder.massutil.Atom;
import org.eurocarbdb.application.glycanbuilder.massutil.MassUtils;
import org.eurocarbdb.application.glycanbuilder.massutil.Molecule;
import org.ict.big.GlycanTools.debug.Print;
import sun.reflect.generics.tree.Tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class IsotopicPeakDistributionCaculator {

    ArrayList<Atom> atomsComputed;
    double minim_abundance = 0.01;

    public IsotopicPeakDistributionCaculator(){
        atomsComputed = new ArrayList<>();
    }

    public IsotopicPeakDistributionCaculator(double minim_abundance){
        atomsComputed = new ArrayList<>();
        this.minim_abundance = minim_abundance;
    }

    public IsotopicPeakDistributionCaculator(ArrayList<Atom> _atomsComputed, double minim_abundance){
        this.atomsComputed = _atomsComputed;
        this.minim_abundance = minim_abundance;
    }

    public IsotopicPeakDistributionCaculator(String atomsString, double minim_abundance){
        ArrayList<Atom> atoms = new ArrayList<>();
        String[] atomStrs = atomsString.split(",");
        for(String atomStr: atomStrs){
            Atom atom = null;
            try {
                atom = MassUtils.getAtom(atomStr);
            } catch (Exception e) {
                Print.pl("Invalid atom representation!");
                e.printStackTrace();
            }
            if(atom != null){
                atoms.add(atom);
            }
        }
        this.atomsComputed = atoms;
        this.minim_abundance = minim_abundance;
    }

//    public ArrayList<Double> computeIsotopicDistribution(Glycan glycan){
//        ArrayList<Double> ret = null;
//        Molecule m = null;
//        try {
//            m = glycan.computeMolecule();
//        } catch (Exception e) {
//            Print.pl("Computing molecule error!");
//            e.printStackTrace();
//        }
//        if(m != null){
//            ret = computeIsotopicDistribution(m);
//        } else {
//
//        }
//        return ret;
//    }

//    public ArrayList<Double> computeIsotopicDistribution(Molecule molecule){
//        TreeMap<Atom, Integer> atoms = (TreeMap<Atom, Integer>) molecule.getAtoms();
//        for(Atom atom: atomsComputed){
//            if(atoms.containsKey(atom)){
//
//            }
//        }
//        //TODO
//
//    }

    public IsotopicDistribution computeIsotopicDistributionCarbonOnly(Glycan glycan){
        IsotopicDistribution ret = null;
        Molecule m = null;
        try {
            m = glycan.computeMolecule();
        } catch (Exception e) {
            Print.pl("Computing molecule error!");
            e.printStackTrace();
        }
        if(m != null){
            ret = computeIsotopicDistributionCarbonOnly(m);
        } else {

        }
        return ret;
    }

    public IsotopicDistribution computeIsotopicDistributionCarbonOnly(Molecule molecule){
        ArrayList<Double> dis = null;
        Collection<Map.Entry<Atom, Integer>> entries = molecule.getAtoms();
        TreeMap<Atom, Integer> atoms = new TreeMap<Atom, Integer>();
        for(Map.Entry<Atom, Integer> e: entries){
            atoms.put(e.getKey(), e.getValue());
        }
        Atom atom = null;
        try {
            atom = MassUtils.getAtom("C");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(atom != null){
            int num = atoms.get(atom);
            dis = computeIsotopicDistributionOfCarbon(atom, num);
        }
        IsotopicDistribution iso = new IsotopicDistribution(dis);
        return iso;
    }

    public ArrayList<Double> computeIsotopicDistributionOfCarbon(Atom atom, int number){
        double C12_abundance = 0.98930000;
        ArrayList<Double> distribution = new ArrayList<>();
        boolean increase = true;
        double abundance = 1.; // abundance of first peak(0 isotopic)
        for(int i = 0; increase || abundance > minim_abundance; i++){
            distribution.add(abundance);
            double newAbundance = abundance * (number - i) / (i + 1) * (1 - C12_abundance) / C12_abundance;
            if(newAbundance < abundance){
                increase = false;
            }
            abundance = newAbundance;
        }
        normalizeDistribution(distribution);
        return distribution;
    }


    private void normalizeDistribution(ArrayList<Double> distribution){
        double sum = 0;
        for(Double d: distribution){
            sum += d;
        }
        for(int i = 0;i < distribution.size();i++){
            distribution.set(i, distribution.get(i) / sum);
        }
    }

    public static IsotopicDistribution trimAccordingToIntensity(IsotopicDistribution iso,
                                                                double intensity, double minIntensity){
        int maxI = iso.getMaxIntensedIndex();
        double minProb = (iso.get(maxI) / intensity) * minIntensity;
        for(int i = maxI + 1; i < iso.size(); i++){
            if(iso.get(i) < minProb){
                int numTobeRemoved = iso.size() - i;
                for(int c = 0; c < numTobeRemoved; c++){
                    iso.remove(iso.size() - 1);
                }
            }
        }
        iso.normalize();
        return iso;
    }

//    public static void main(String[] args){
//        IsotopicPeakDistributionCaculator iso = new IsotopicPeakDistributionCaculator(0.00001);
//        ArrayList<Double> dis = iso.computeIsotopicDistributionOfCarbon(null, 10);
//        for(Double d: dis){
//            Print.pl(""+d);
//        }
//    }
}
