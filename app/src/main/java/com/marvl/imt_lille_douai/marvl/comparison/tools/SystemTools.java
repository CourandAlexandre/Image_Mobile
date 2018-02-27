package com.marvl.imt_lille_douai.marvl.comparison.tools;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.marvl.imt_lille_douai.marvl.comparison.variables.GlobalVariables;

import org.bytedeco.javacpp.opencv_core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;

public class SystemTools {

    // TODO : change to OpenFileOutput -> getFileCacheDir
    public static File toCache(Context context, String Path, String fileName) {
        InputStream input;
        FileOutputStream output;
        byte[] buffer;

        String filePath = context.getCacheDir() + "/" + fileName;
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

        String filePath = context.getApplicationContext().getCacheDir().getAbsolutePath() + "/" + file.getName();
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

        File cacheDir = new File(context.getCacheDir().getAbsolutePath());

        System.out.println(GlobalVariables.debugTag + " cacheDir : " + cacheDir);

        File[] listOfFiles = cacheDir.listFiles();

        for(int i=0 ; i < listOfFiles.length ; i++) {
            if( !listOfFiles[i].getName().contains("volley") && !listOfFiles[i].getName().contains("yml") && !listOfFiles[i].getName().contains("jpg") ){
                classifierArray.add(listOfFiles[i]);
            }
        }

        return classifierArray;
    }

    public static String getCacheVocabularyPath(Context context) {
        File cacheDir = new File(context.getCacheDir().getAbsolutePath());

        System.out.println(GlobalVariables.debugTag + " cacheDir : " + cacheDir);

        File[] listOfFiles = cacheDir.listFiles();

        for(int i=0 ; i < listOfFiles.length ; i++) {
            System.out.println(GlobalVariables.debugTag + "ahah"+ listOfFiles[i].getAbsolutePath());
            if( listOfFiles[i].getName().contains("yml") ){
               return listOfFiles[i].getAbsolutePath();
            }
        }

        return null;
    }

    public static File putFileIntoLocal(Context context, String fileName, String response) {
        Writer writer;
        File outputFile = null;
        File outDir = new File(context.getCacheDir().getAbsolutePath());

        if (!outDir.isDirectory()) {
            outDir.mkdir();
        }
        try {
            if (!outDir.isDirectory()) {
                throw new IOException(
                        "Unable to create directory EZ_time_tracker. Maybe the SD card is mounted?");
            }
            outputFile = new File(outDir, fileName);
            writer = new BufferedWriter(new FileWriter(outputFile));
            writer.write(response);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputFile;
    }

    public static String getCachePhotoPath(Context context) {
        File cacheDir = new File(context.getCacheDir().getAbsolutePath());

        System.out.println(GlobalVariables.debugTag + " cacheDir : " + cacheDir);

        File[] listOfFiles = cacheDir.listFiles();

        for(int i=0 ; i < listOfFiles.length ; i++) {
            System.out.println(GlobalVariables.debugTag + "ahah"+ listOfFiles[i].getAbsolutePath());
            if( listOfFiles[i].getName().contains("jpg") ){
                return listOfFiles[i].getAbsolutePath();
            }
        }

        return null;
    }

    public static void clearCache(Context context){
        File cacheDir = new File(context.getCacheDir().getAbsolutePath());
        File[] listOfFiles = cacheDir.listFiles();

        for(int i=0 ; i < listOfFiles.length ; i++) {
            listOfFiles[i].delete();
        }
    }

    public static void clearFileFromCache(Context context, String fileName){
        File cacheDir = new File(context.getCacheDir().getAbsolutePath());
        File[] listOfFiles = cacheDir.listFiles();

        for(int i=0 ; i < listOfFiles.length ; i++) {
            if( listOfFiles[i].getName().contains(fileName) ){
                listOfFiles[i].delete();
            }
        }
    }

    public static File convertBitmapToFile(Context context, Bitmap bitmap, String name){
        String filePath = context.getCacheDir().getAbsolutePath() ;
        File file = new File (filePath, name);

        OutputStream output;

        try {
            output = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, GlobalVariables.bitmapCompression, output);

            output.flush();
            output.close();

            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
