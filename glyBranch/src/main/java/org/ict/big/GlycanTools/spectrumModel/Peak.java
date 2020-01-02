package org.ict.big.GlycanTools.spectrumModel;

public class Peak implements Comparable<Peak> {

    /**
     * This constant is used to judge zero of double.
     */
    protected static double EPS = 1E-9;

    protected double mz_ratio;
    protected double absoluteIntensity;
    protected double relativeIntensity;

    protected PeakList nextLevelPeakList = null;

    /**
     * Supporting peaks...
     */
    protected String supportingInformation = "";


    /**
     * =0, +1, +2,...
     * Currently, for mz < 1600, the most intensed isotopic peak is 0,
     * while for mz > 1600, the most intensed isotopic peak is +1.
     */
    protected double isotopicMz = 0;

    /**
     * Create a new peak object with 0 values for mass/charge and intensity
     */
    public Peak() {
        mz_ratio = 0.;
        absoluteIntensity = 0.;
        relativeIntensity = -1;
    }

    /**
     * Create a new peak object with specific values for mass/charge and
     * intensity
     * @param _mz the mass/charge value
     * @param _int the absolute intensity value
     */
    public Peak(double _mz, double _int) {
        mz_ratio = _mz;
        absoluteIntensity = _int;
    }

    /**
     * Create a new peak object with specific values for mass/charge,
     * absolute intensity and relative intensity.
     * @param _mz the mass/charge value
     * @param _int the absolute intensity value
     * @param _rint the relative intensity value
     */
    public Peak(double _mz, double _int, double _rint) {
        mz_ratio = _mz;
        absoluteIntensity = _int;
        relativeIntensity = _rint;
    }

    public Peak(Peak _peak){
        this.mz_ratio = _peak.mz_ratio;
        this.absoluteIntensity = _peak.absoluteIntensity;
        this.relativeIntensity = _peak.relativeIntensity;
        this.nextLevelPeakList = _peak.nextLevelPeakList;
        this.supportingInformation = _peak.supportingInformation;
        this.isotopicMz = _peak.isotopicMz;
    }

    /**
     * Create a copy of this object
     */
    public Peak clone() {
        Peak clone = new Peak(this);
        return clone;
    }

    public double getMz() {
        return mz_ratio;
    }

    public void setMz(double mz_ratio) {
        this.mz_ratio = mz_ratio;
    }

    public double getAbsoluteIntensity() {
        return absoluteIntensity;
    }

    public void setAbsoluteIntensity(double absoluteIntensity) {
        this.absoluteIntensity = absoluteIntensity;
    }

    public double getRelativeIntensity() {
        return relativeIntensity;
    }

    public void setRelativeIntensity(double relativeIntensity) {
        this.relativeIntensity = relativeIntensity;
    }

    public void setIntensity(double absoluteIntensity, double maxIntensity){
        this.absoluteIntensity = absoluteIntensity;
        this.relativeIntensity = absoluteIntensity / maxIntensity;
    }

    public PeakList getNextLevelPeakList() { return nextLevelPeakList; }

    public void setNextLevelPeakList(PeakList nextLevelPeakList) {
        this.nextLevelPeakList = nextLevelPeakList;
    }

    public boolean hasNextLevelPeakList(){
        return nextLevelPeakList == null? false:true;
    }

    public String getSupportingInformation() {
        return supportingInformation;
    }

    public void setSupportingInformation(String supportingInformation) {
        this.supportingInformation = supportingInformation;
    }

    public void addSuppordtingInformation(String info){
         this.supportingInformation += info;
    }

    public double getIsotopicMz() {
        return isotopicMz;
    }

    public void setIsotopicMz(double isotopicMz) {
        this.isotopicMz = isotopicMz;
    }

    /**
     * The default mz_ration comparator of Peak class.
     */
    @Override
    public int compareTo(Peak p) {
        if (this.mz_ratio < p.mz_ratio){
            return -1;
        }
        if (this.mz_ratio > p.mz_ratio)
            return +1;
        return 0;
    }

    public boolean equals(Object other) {
        if (!(other instanceof Peak))
            return false;

        Peak p = (Peak) other;
        return (Math.abs(this.mz_ratio - p.mz_ratio) < EPS && Math
                .abs(this.absoluteIntensity - p.absoluteIntensity) < EPS &&
                Math.abs(this.relativeIntensity - p.relativeIntensity) < EPS);
    }

    public boolean mzEquals(Peak it){
        return (Math.abs(this.mz_ratio - it.mz_ratio) < EPS);
    }

    public boolean mzEquals(Peak it, MassUnit unit, double accuracy){
        if(unit==MassUnit.PPM){
            return (Math.abs(1. - it.getMz() / mz_ratio) < (0.000001 * accuracy));
        }
        return (Math.abs(mz_ratio - it.getMz()) < accuracy);
    }

    public String toString(){
        String str = String.format("%1$7.2f\t%2$12.2f\t%3$6.2f\tIso:%4$2.0f",
                mz_ratio, absoluteIntensity, relativeIntensity, isotopicMz);
        if(supportingInformation.length() > 0){
            str += "\t" + supportingInformation;
        }
        return str;
    }
}
