package org.ict.big.GlycanTools.spectrumModel.compositionDenovo;

public class MonoCompositionEntry {
    public String id;
    public int num;
    public MonoCompositionEntry(String id, int num){
        this.id = id;
        this.num = num;
    }
    public MonoCompositionEntry clone(){
        return new MonoCompositionEntry(this.id, this.num);
    }

    public boolean equalTo(MonoCompositionEntry m){
        return this.id == m.id && this.num == m.num;
    }

    @Override
    public String toString() {
        String ret = "";
        for(int i = 0;i< num;i++){
            ret += id;
        }
        return ret;
    }

}
