package org.ict.big.GlycanTools.io;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZhangJikai on 2016/11/17.
 */
public class MzXmlData implements Serializable {

    private static final long serialVersionUID = 5983101148337937106L;

    private int maxLevel;
    private List<ScanData> scanDataList;

    public void addPeakData(ScanData scanData) {
        if (scanDataList == null) {
            scanDataList = new ArrayList<>();

        }
        if (scanData.getMsLevel() > maxLevel) {
            maxLevel = scanData.getMsLevel();
        }
        scanDataList.add(scanData);
    }

    public ScanData getMaxLevelPeakData() {
        if (scanDataList.size() == 0)
            return null;
        return scanDataList.get(scanDataList.size() - 1);
    }

    public List<ScanData> getScanDataList() {
        return scanDataList;
    }

    public void setScanDataList(List<ScanData> scanDataList) {
        this.scanDataList = scanDataList;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    public static class ScanData implements Serializable {

        private static final long serialVersionUID = -7968802335963403067L;

        private int msLevel;
        private int peakCount;
        private String peakContent="";
        private double mass;

        public ScanData() {
        }

        public ScanData(int msLevel, int peakCount, String peakContent, double mass) {
            this.msLevel = msLevel;
            this.peakCount = peakCount;
            this.peakContent = peakContent;
            this.mass = mass;
        }

        public int getMsLevel() {
            return msLevel;
        }

        public void setMsLevel(int msLevel) {
            this.msLevel = msLevel;
        }

        public int getPeakCount() {
            return peakCount;
        }

        public void setPeakCount(int peakCount) {
            this.peakCount = peakCount;
        }

        public String getPeakContent() {
            return peakContent;
        }

        public void setPeakContent(String peakContent) {
            this.peakContent = peakContent;
        }

        public double getMass() {
            return mass;
        }

        public void setMass(double mass) {
            this.mass = mass;
        }

        @Override
        public String toString() {
            return "ScanData{" +
                    "msLevel=" + msLevel +
                    ", peakCount=" + peakCount +
                    ", peakContent='" + peakContent + '\'' +
                    ", mass=" + mass +
                    '}';
        }
    }
}
