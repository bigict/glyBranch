package org.ict.big.GlycanTools.spectrumModel;

/**
 * This class is modified from org.eurocarbdb.application.glycoworkbench
 * A extra R(Resolution) value is added.
 */
public enum MassUnit {
	PPM(),
	Da(),
    R();
	
	public static MassUnit valueOfCompat(String value){
		if(value.equals("ppm")){
			return MassUnit.PPM;
		} else if(value.equals("R") || value.equals("Res")){
            return MassUnit.R;
        } else{
			return MassUnit.valueOf(value);
		}
	}
	
}
