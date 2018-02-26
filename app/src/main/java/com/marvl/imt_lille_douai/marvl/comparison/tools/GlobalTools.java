package com.marvl.imt_lille_douai.marvl.comparison.tools;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.android.volley.Cache;
import com.marvl.imt_lille_douai.marvl.comparison.image.ComparedImage;
import com.marvl.imt_lille_douai.marvl.controller.MainActivity;

import static org.bytedeco.javacpp.opencv_highgui.WINDOW_AUTOSIZE;
import static org.bytedeco.javacpp.opencv_highgui.imread; //import static org.bytedeco.javacpp.opencv_imgcodecs.imread -> JavaCV 1.3;
import static org.bytedeco.javacpp.opencv_highgui.imshow;
import static org.bytedeco.javacpp.opencv_highgui.namedWindow;
import static org.bytedeco.javacpp.opencv_highgui.waitKey;
import static org.bytedeco.javacpp.opencv_imgproc.THRESH_BINARY;
import static org.bytedeco.javacpp.opencv_imgproc.threshold;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.bytedeco.javacpp.opencv_core.Mat;
//import org.bytedeco.javacpp.opencv_imgcodecs;

public class GlobalTools {

    public static String getRealPath(Context context, Uri uri){
        Cursor cursor;

        String[] projection = {MediaStore.Images.Media.DATA};
        cursor = context.getContentResolver().query(
                uri,
                projection,
                null,
                null,
                null
        );

        int dataIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        return cursor.getString(dataIndex);
    }

    public static Bitmap decodeFile(File file){
        Bitmap bitmap = null;

        try {
            FileInputStream inputStream = new FileInputStream(file); // System.err

            BitmapFactory.Options options = new BitmapFactory.Options();
            bitmap = BitmapFactory.decodeStream(inputStream, null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    public static int indexOfStr(String completeStr, String seekingStr, int occurrence){
        int pos = completeStr.indexOf(seekingStr);

        while( --occurrence > 0 && pos != -1 ){
            pos = completeStr.indexOf(seekingStr, pos+1);
        }

        return pos;
    }

    public static String getFileNameFromPath(String pathName){
        int nbOfSeparator = 0;

        for(int i=0 ; i < pathName.length(); i++){
            if(pathName.charAt(i) == '/' ){
                nbOfSeparator++;
            }
        }

        return pathName.substring(indexOfStr(pathName,"/",nbOfSeparator)+1);
    }
}
