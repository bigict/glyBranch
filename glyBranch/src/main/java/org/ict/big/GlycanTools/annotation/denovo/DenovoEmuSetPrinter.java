package org.ict.big.GlycanTools.annotation.denovo;

import org.ict.big.GlycanTools.debug.Print;
import org.ict.big.GlycanTools.spectrumModel.Peak;
import org.ict.big.GlycanTools.spectrumModel.PeakList;
import org.ict.big.GlycanTools.spectrumModel.oldTree.PeakListTree;
import org.ict.big.GlycanTools.spectrumModel.compositionDenovo.FragmentComposition;
import org.ict.big.GlycanTools.spectrumModel.compositionDenovo.PeakWithCompositionAnnotation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class DenovoEmuSetPrinter {

    protected PeakListTree peakListTree;
    protected FragmentComposition precursorComposition;

    public DenovoEmuSetPrinter(PeakListTree peakListTree, FragmentComposition fc){
        this.peakListTree = peakListTree;
        precursorComposition = fc;
    }

    public boolean writeToFile(String filePath) throws IOException {
        boolean success = false;
        String all = precursorComposition.toString() + "\n{\n";
        all += DFSNode(peakListTree.getMS2().getPeakList(), "\t");
        all += "}\n";
        Path path = Paths.get(filePath);
        Files.write(path, all.getBytes(), StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE);
        Print.pl(all);
        return success;
    }

    protected String DFSNode(PeakList peakList, String prefixString){
        String ret = "";
//        Set<String> compositionSet = new HashSet<>();
        for(Peak peak: peakList){
            if(peak instanceof PeakWithCompositionAnnotation){
                if((peak.getIsotopicMz() > 0.1 || peak.getIsotopicMz() < -0.1)
                        && !peak.hasNextLevelPeakList()) {
                    // if this is a isotopic peak(+1,+2...) and does not have a next staged peaklist
                    continue;
                }

                String fragmentsComposition = "";
                for(FragmentComposition fc: ((PeakWithCompositionAnnotation) peak).getAnnotation()){
                    String compositionStr = fc.toString();
                    fragmentsComposition += compositionStr + ",";
                }
                fragmentsComposition = prefixString + String.format("%1$-7.2f\t%2$6.2f\t",
                        peak.getMz(), peak.getRelativeIntensity()) +
                        fragmentsComposition.substring(0,fragmentsComposition.length() - 1 ) +
                        "\n";
                ret += fragmentsComposition;
                if(peak.hasNextLevelPeakList()){
                    ret += prefixString + "{\n";
                    ret += DFSNode(peak.getNextLevelPeakList(), prefixString+"\t");
                    ret += prefixString + "}\n";
                }
            }
        }
        return ret;
    }
}
