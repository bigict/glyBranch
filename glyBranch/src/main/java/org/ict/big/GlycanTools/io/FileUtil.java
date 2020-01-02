package org.ict.big.GlycanTools.io;

import org.ict.big.GlycanTools.debug.Print;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;

public class FileUtil {
    public static File[] getFilesByPathAndSuffix(File path,  
            final String sufixStr) {  
        File[] fileArr = path.listFiles(new FilenameFilter() {  
            @Override  
            public boolean accept(File dir, String name) {  
                // System.out.println("prefixStr:"+prefixStr);  
                if ( dir.isDirectory() && name.endsWith(sufixStr) ) {  
                    return true;  
                } else {  
                    return false;  
                }  
            }  
        });  
        return fileArr;  
  
    }  
    
    public static File[] getFilesByPathAndSuffix(String pathStr,  
            final String sufixStr) {  
        File path = new File(pathStr);  
        return getFilesByPathAndSuffix(path, sufixStr);  
    }  
    
    public static ArrayList<String> getFileNamesByPathAndSuffix(String pathStr,  
            final String sufixStr) {  
        File path = new File(pathStr);
        File[] files = getFilesByPathAndSuffix(path, sufixStr);
        ArrayList<String> fileNames = new ArrayList<String>();
        for(File f:files){
            fileNames.add(f.getName());
        }
        return fileNames;
    }
    
    public static ArrayList<String> getFilePathsByPathAndSuffix(String pathStr,  
            final String sufixStr) {  
        File path = new File(pathStr);
        if(path == null){
            return null;
        }
        File[] files = getFilesByPathAndSuffix(path, sufixStr);
        ArrayList<String> filePaths = new ArrayList<String>();
        System.out.println(path);
        for(File f:files){
            filePaths.add(f.getPath());
        }
        return filePaths;
    }
    
    
    /**
     * Creates the directory, including any necessary but nonexistent 
     * parent directories.
     * The same as "mkdir -p".
     * @param path the path of its parent folder
     * @param dirName the name of this folder
     * @return true, if successful
     */
    public static boolean mkDir(String path, String dirName){
        File dir = new File(path, dirName);
        System.out.println("mkdir:"+dir.getPath());
        return dir.mkdirs();
    }
    
    public static File[] listDirs(File dirFile){
        File[] files = dirFile.listFiles(new FileFilter(){

            @Override
            public boolean accept(File f) {
                return f.isDirectory();
            }

        });
        return files;
    }
    public static ArrayList<String> listDirNames(String path){
        File file = new File(path);
        File[] files = listDirs(file);
        ArrayList<String> dirNames = new ArrayList<String>();
        for(File f:files){
            dirNames.add(f.getName());
        }
        return dirNames;
    }
    public static ArrayList<String> listDirPaths(String path){
        File file = new File(path);
        File[] files = listDirs(file);
        ArrayList<String> dirPaths = new ArrayList<String>();
        for(File f:files){
            dirPaths.add(f.getPath());
        }
        return dirPaths;
    }
}
