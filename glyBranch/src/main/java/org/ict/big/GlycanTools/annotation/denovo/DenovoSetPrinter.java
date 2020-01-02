package org.ict.big.GlycanTools.annotation.denovo;

import org.eurocarbdb.application.glycanbuilder.FragmentEntry;
import org.eurocarbdb.application.glycanbuilder.Glycan;
import org.eurocarbdb.application.glycanbuilder.Residue;
import org.ict.big.GlycanTools.debug.Print;
import org.ict.big.GlycanTools.spectrumModel.*;
import org.ict.big.GlycanTools.spectrumModel.oldTree.PeakListTree;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class DenovoSetPrinter {
    protected PeakListTree peakListTree;

    private static Map<String, Character> typeMap;

    private static boolean initialized = false;

    public DenovoSetPrinter(PeakListTree _peakListTree){
        peakListTree = _peakListTree;
        if(!initialized){
            initTypeMap();
        }
    }

    private static void initTypeMap() {
        typeMap = new HashMap<>();
        String[][] allTypes = new String[][]{
                {"GlcNAc", "GalNAc", "ManNAc"}, // selected
                {"Man", "Glc", "Gal", "All"}, // selected
                {"GalN", "GlcN", "ManN"},
                {"NeuAc"}, // selected
                {"KDN"},
                {"NeuGc"}, // F selected
                {"Neu"},
                {"IdoA", "GlcA", "GalA", "ManA"},
                {"KDO"},
                {"Fuc", "Rha", "Qui"}, // selected
                {"Xyl"},
                {"Ara", "Rib"},
                {"Fru",  "Hex"},
                {"dTal"},
                {"Tal"},
                {"Hept"},
//                {"S"},
//                {"P"},
//                {"NAc"},
//                {"Ac"},
                //{"freeEnd", "redEnd"},
                //{"Me"}
        };

        int index = 0;
        for(String[] types : allTypes) {
            for(String s : types) {
                int c = 'A' + index;
                typeMap.put(s, (char) c);
            }
            index++;
        }
        initialized = true;
    }

    public boolean writeToFile(String filePath) throws IOException {
        boolean success = false;
        String all = DFSNode(peakListTree.getTopPeakListNode().getPeakList(), "");
        Path path = Paths.get(filePath);
        Print.pl(all);
        Files.write(path, all.getBytes(), StandardOpenOption.CREATE);
        return success;
    }

    protected String DFSNode(PeakList peakList, String prefixString){
        String ret = "";
//        Set<String> compositionSet = new HashSet<>();
        for(Peak peak: peakList){
            if(peak instanceof PeakWithAnnotation){
                String fragmentsComposition = "";
                for(FragmentEntry fragment: ((PeakWithAnnotation)peak).getAnnotatedFragments()){
                    String compositionStr = fragmentToMonoSetString(fragment);
                    fragmentsComposition += compositionStr + ",";
                }
                fragmentsComposition = prefixString + String.format("%1$-7.2f\t%2$6.2f\t",
                            peak.getMz(), peak.getRelativeIntensity()) +
                        fragmentsComposition.substring(0,fragmentsComposition.length() - 1 ) +
                        "\n";
                ret += fragmentsComposition;
                if(peak.hasNextLevelPeakList()){
                    Print.pl("debug");
                    ret += prefixString + "{\n";
                    ret += DFSNode(peak.getNextLevelPeakList(), prefixString+"\t");
                    ret += prefixString + "}\n";
                }
            } else if(peak.hasNextLevelPeakList()){

            }
        }
        return ret;
    }

    protected String fragmentToMonoSetString(FragmentEntry fragment){
        String composition = "";
        Glycan glycan = fragment.fragment;
        Residue res = glycan.getRoot();
        Collection<Residue> allResidues = glycan.getAllResidues();
        for(Residue r: allResidues){
            Character c = residueToChar(r);
            if(c != null){
                composition += residueToChar(r);
            }
        }
        return composition;
    }

    protected Character residueToChar(Residue r){
        String key = r.getType().getResidueName();
        return typeMap.get(key);
    }

}
