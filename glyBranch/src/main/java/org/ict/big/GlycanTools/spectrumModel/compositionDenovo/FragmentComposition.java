package org.ict.big.GlycanTools.spectrumModel.compositionDenovo;

import java.util.Iterator;

public class FragmentComposition {

    MonoCompositionEntry[] composition;

    int numOfSection;
    int numOfOOnSection;
    double mz; // different from mass, this variable includes Na...

    public FragmentComposition(){
        this.composition = new MonoCompositionEntry[0];
        this.numOfSection = 0;
        this.numOfOOnSection = 0;
        this.mz = 0;
    }

    public FragmentComposition(int numOfType){
        this.composition = new MonoCompositionEntry[numOfType];
        this.numOfSection = 0;
        this.numOfOOnSection = 0;
        this.mz = 0;
    }

    public FragmentComposition(MonoCompositionEntry[] composition){
        this.composition = composition;
        this.numOfSection = 0;
        this.numOfOOnSection = 0;
        this.mz = 0;
    }

    public FragmentComposition(String[] ids, int[] num){
        composition = new MonoCompositionEntry[ids.length];
        for(int i = 0; i< ids.length; i++){
            composition[i] = new MonoCompositionEntry(ids[i], num[i]);
        }
        this.numOfSection = 0;
        this.numOfOOnSection = 0;
        this.mz = 0;
    }

    public FragmentComposition(MonoCompositionEntry[] composition, int numOfSection, int numOfOOnSection){
        this.composition = composition;
        this.numOfSection = numOfSection;
        this.numOfOOnSection = numOfOOnSection;
        this.mz = 0;
    }
    public FragmentComposition(MonoCompositionEntry[] composition, int numOfSection, int numOfOOnSection, double mz){
        this.composition = composition;
        this.numOfSection = numOfSection;
        this.numOfOOnSection = numOfOOnSection;
        this.mz = mz;
    }

    public FragmentComposition(FragmentComposition c){
        this.composition = c.composition;
        this.numOfSection = c.numOfSection;
        this.numOfOOnSection = c.numOfOOnSection;
        this.mz = c.mz;
    }

    public void setMz(double mz) {
        this.mz = mz;
    }

    public double getMz() {
        return mz;
    }

    public int numOfMono(){
        int all = 0;
        for(MonoCompositionEntry m: composition){
            all += m.num;
        }
        return all;
    }

    public MonoCompositionEntry[] getPureComposition() {
        return composition;
    }

    public int getNumOfSection() {
        return numOfSection;
    }

    public int getNumOfOOnSection() {
        return numOfOOnSection;
    }

    public int getNumOfType(){
        return composition.length;
    }


    public void trimInPlace(){
        int cnt = 0;
        for(MonoCompositionEntry mc: composition){
            if(mc.num > 0){
                cnt++;
            }
        }
        MonoCompositionEntry[] newComp = new MonoCompositionEntry[cnt];
        cnt = 0;
        for(MonoCompositionEntry mc: composition){
            if(mc.num > 0){
                newComp[cnt] = mc;
                cnt++;
            }
        }
        this.composition = newComp;
    }

    public String toString(){
        String ret = "";
        if(composition != null) {
            for (MonoCompositionEntry mc : composition) {
                ret += mc.toString();
            }
        }
        return ret;
    }
    public String toFragmentString(){
        String ret = toString();
        ret +=  "("+numOfSection+","+numOfOOnSection+")";
        ret += String.format("=%.2f", mz);
        return ret;
    }

    public void setComposition(MonoCompositionEntry[] composition) {
        this.composition = composition;
    }

    public boolean equalInComposition(FragmentComposition f){
        boolean equal = true;
        if(this.composition.length != f.composition.length){
            equal = false;
        } else {
            for(int i = 0;i< this.composition.length;i++){
                if(!this.composition[i].equalTo(f.composition[i])){
                    equal = false;
                    break;
                }
            }
        }
        return equal;
    }

    public MonoCompositionEntry[] copyPureComposition(){
        MonoCompositionEntry[] clone = new MonoCompositionEntry[this.composition.length];
        for(int i = 0;i< clone.length;i++){
            clone[i] = this.composition[i].clone();
        }
        return clone;
    }
    public FragmentComposition deepClone(){
        FragmentComposition c = new FragmentComposition(this);
        c.setComposition(this.copyPureComposition());
        return c;
    }

}
