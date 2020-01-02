package org.ict.big.GlycanTools.spectrumModel.structureLibrary;

import org.eurocarbdb.application.glycanbuilder.Glycan;

import java.util.Comparator;

public class CarbohydrateRecord implements Comparable<CarbohydrateRecord> {
    public Glycan glycan;
    public String ID;
    public double mass;

    public CarbohydrateRecord(Glycan _glycan, String _ID, double _mass){
        this.glycan = _glycan;
        this.ID = _ID;
        this.mass = _mass;
    }

    public Glycan getGlycan() {
        return glycan;
    }

    public void setGlycan(Glycan glycan) {
        this.glycan = glycan;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public double getMass() {
        return mass;
    }

    public void setMass(double mass) {
        this.mass = mass;
    }

    @Override
    public String toString() {
        return "Record: " + ID + "\t" + Double.toString(mass) + "\t" + glycan.toString();
    }

    public String toStringOnlyIdAndMass(){
        return "Record: " + ID + "\t" + Double.toString(mass);
    }
    /**
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(CarbohydrateRecord o) {
        if(this.ID.length() == o.ID.length()){
            return this.ID.length() - o.ID.length();
        } else {
            return this.ID.compareTo(o.ID);
        }
    }

    public boolean equals(Object other) {
        if(other == null || !(other instanceof CarbohydrateRecord)){
            return false;
        } else {
            return this.ID.equals(((CarbohydrateRecord)other).ID);
        }
    }
}
