package org.ict.big.GlycanTools.spectrumModel;

import java.util.Comparator;

public class GlycanCandidateScoreComparator implements Comparator<GlycanCandidate> {

    @Override
    public int compare(GlycanCandidate o1, GlycanCandidate o2) {
        return Double.compare(o1.score, o2.score);
    }
}
