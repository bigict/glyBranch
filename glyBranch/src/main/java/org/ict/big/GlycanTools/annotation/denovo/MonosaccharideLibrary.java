package org.ict.big.GlycanTools.annotation.denovo;

import org.ict.big.GlycanTools.debug.Print;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonosaccharideLibrary {
    String[] allId;
    double[] allMass;
    Map<String, Double> monoMass;
    Map<String, String> idToFullname;
    static MonosaccharideLibrary instance;
    static String monoFilePath = "./config/monosaccharide.txt";


    private MonosaccharideLibrary() throws IOException {
        monoMass = new HashMap<>();
        idToFullname = new HashMap<>();
        ArrayList<String> allIdL = new ArrayList<>();
        ArrayList<Double> allMassL = new ArrayList<>();
        Path path = Paths.get(monoFilePath);
        List<String> allLines = Files.readAllLines(path);
        for(String line: allLines){
            String trimedLine = line.trim();
            if(trimedLine.startsWith("#")){
                continue;
            } else {
                String[] splitedStr = trimedLine.split("\t");
                if(splitedStr.length >= 3) {
                    String fullname = splitedStr[0];
                    double mass = Double.parseDouble(splitedStr[1]);
                    String id = splitedStr[2];
                    monoMass.put(id, mass);
                    idToFullname.put(id, fullname);
                    allIdL.add(id);
                    allMassL.add(mass);
                }
            }
        }
        allId = new String[allIdL.size()];
        allMass = new double[allMassL.size()];
        for(int i = 0;i< allId.length;i++){
            allId[i] = allIdL.get(i);
            allMass[i] = allMassL.get(i).doubleValue();
        }
    }

    public static MonosaccharideLibrary getInstance(){
        if(instance == null){
            try {
                instance = new MonosaccharideLibrary();
            } catch (IOException e) {
                Print.pl("Initial Mono library error!");
                e.printStackTrace();
            }
        }
        return instance;
    }

    public static Map<String, Double> getAllMassMap(){
        return getInstance().monoMass;
    }
    public static double getMassFromId(String id){
        return getInstance().monoMass.get(id);
    }
    public static String[] getAllId(){
        return getInstance().allId;
    }
    public static double[] getAllMass(){
        return getInstance().allMass;
    }
    public static void test(){
        Map<String, Double> all = MonosaccharideLibrary.getAllMassMap();
        for(Map.Entry<String, Double> e: all.entrySet()){
            Print.pl(e.getKey()+": "+e.getValue());
        }

    }
}
