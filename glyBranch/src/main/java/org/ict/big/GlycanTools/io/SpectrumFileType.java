package org.ict.big.GlycanTools.io;

public enum SpectrumFileType {
    mzXML("mzXML", "mzXML"),
    TXT("txt", "txt");
    private String typeId;
    private String extension;

    private SpectrumFileType(String id, String extension) {
        this.typeId = id;
        this.extension = extension;
    }

    public String getExtension(){return extension;}
}
