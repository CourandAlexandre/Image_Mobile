package com.marvl.imt_lille_douai.marvl.comparison.tools;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class SystemTools {

    // TODO : change to OpenFileOutput -> getFileCacheDir
    public static File toCache(Context context, String Path, String fileName) {
        InputStream input;
        FileOutputStream output;
        byte[] buffer;

        String filePath = context.getFilesDir() + "/" + fileName;
        File file = new File(filePath);
        AssetManager assetManager = context.getAssets();

        try {
            input = assetManager.open(Path);
            buffer = new byte[input.available()];
            input.read(buffer);
            input.close();

            output = new FileOutputStream(filePath);
            output.write(buffer);
            output.close();
            return file;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static File toCacheServ(Context context, File file) {
        InputStream input;
        FileOutputStream output;
        byte[] buffer;

        String filePath = context.getApplicationContext().getFilesDir().getName() + "/" + file.getName();
        AssetManager assetManager = context.getAssets();

        try {
            input = assetManager.open(file.getAbsolutePath());
            buffer = new byte[input.available()];
            input.read(buffer);
            input.close();

            output = new FileOutputStream(filePath);
            output.write(buffer);
            output.close();
            return file;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<File> convertCacheToClassifierArray(Context context){
        ArrayList<File> classifierArray = new ArrayList<>();

        File cacheDir = new File(context.getFilesDir().getPath());

        System.out.println("TAG : " + cacheDir);

        File[] listOfFiles = cacheDir.listFiles();

        for(int i=0 ; i < listOfFiles.length ; i++) {
            if( !listOfFiles[i].getName().contains("volley") ){
                classifierArray.add(listOfFiles[i]);
            }
        }

        return classifierArray;
    }
}
