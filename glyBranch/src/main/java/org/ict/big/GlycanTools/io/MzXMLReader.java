package org.ict.big.GlycanTools.io;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.ict.big.GlycanTools.spectrumModel.Peak;
import org.ict.big.GlycanTools.spectrumModel.PeakList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


/**
 * Created by ZhangJikai on 2016/11/17.
 */
public class MzXMLReader extends AbstractSpectrumReader {


    private static final String SCAN_TAG = "scan";
    private static final String PEAKS_TAG = "peaks";
    private static final String PRECURSOR_TAG = "precursorMz";
    private static final String MSRUN_TAG = "msRun";

    public MzXMLReader(){}

    public PeakList getPeakListFromFile(String filePath) throws Exception {
        MzXmlData mzXmlData = readMzXML(filePath);
        MzXmlData.ScanData scanData = mzXmlData.getMaxLevelPeakData();
        ArrayList<Peak> peakArray = decodePeakContent(scanData.getPeakContent(), scanData.getPeakCount());
        ArrayList<Double> preMzList = new ArrayList<Double>();
        boolean isFirst = true;
        for (MzXmlData.ScanData data : mzXmlData.getScanDataList()) {
            // 过滤掉MS1
            if(isFirst) {
                isFirst = false;
                continue;
            }
            preMzList.add(data.getMass());
        }

        return new PeakList(peakArray, preMzList);
    }

    private MzXmlData readMzXML(String xmlName) throws ParserConfigurationException, SAXException, IOException {
        File xmlFile = new File(xmlName);
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxParserFactory.newSAXParser();
        MzXmlData data = new MzXmlData();
        saxParser.parse(xmlFile, new MzXMLHandler(data));
        return data;
    }



    /**
     * 解析mzXML内容，内容使用Base64编码
     * @param peakContent
     * @param peakCount
     * @return
     */
    public ArrayList<Peak> decodePeakContent(String peakContent, int peakCount) throws IOException {
        if(peakContent == null) {
            return null;
        }

        byte[] bytes = Base64.decode(peakContent);
        ArrayList<Peak> peakList = new ArrayList<>();
        Peak peak;
        try(DataInputStream peakStream = new DataInputStream(new ByteArrayInputStream(bytes))) {
            for(int i = 0; i < peakCount; i++) {
                double mzValue = peakStream.readFloat();
                double intensValue = peakStream.readFloat();
                //System.out.println("mzValue is " + mzValue + " intensValue is " + intensValue);
                peak = new Peak(mzValue, intensValue);
                peakList.add(peak);
            }
        }
        return peakList;
    }

    class MzXMLHandler extends DefaultHandler {

        private MzXmlData mzXmlData;
        private MzXmlData.ScanData scanData;
        private String preTag;

        public MzXMLHandler() {
            preTag = "";
        }

        public MzXMLHandler(MzXmlData mzXmlData) {
            this();
            this.mzXmlData = mzXmlData;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

            switch (qName) {
                case SCAN_TAG:
                    if (scanData != null) {
                        mzXmlData.addPeakData(scanData);
                    }
                    scanData = new MzXmlData.ScanData();
                    scanData.setMsLevel(Integer.parseInt(attributes.getValue("msLevel")));
                    scanData.setPeakCount(Integer.parseInt(attributes.getValue("peaksCount")));
                    break;
                case PEAKS_TAG:
                    preTag = PEAKS_TAG;
                    break;
                case PRECURSOR_TAG:
                    preTag = PRECURSOR_TAG;
                    break;
                default:
                    break;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch (qName) {
                case PEAKS_TAG:
                case PRECURSOR_TAG:
                    preTag = "";
                    break;
                case MSRUN_TAG:
                    mzXmlData.addPeakData(scanData);
                    break;
                default:
                    break;
            }

        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {

            switch (preTag) {
                case PEAKS_TAG:
                    scanData.setPeakContent(scanData.getPeakContent() + new String(ch, start, length));
                    break;
                case PRECURSOR_TAG:
                    scanData.setMass(Double.parseDouble(new String(ch, start, length)));
                    break;
                default:
                    break;
            }

        }
    }
}
