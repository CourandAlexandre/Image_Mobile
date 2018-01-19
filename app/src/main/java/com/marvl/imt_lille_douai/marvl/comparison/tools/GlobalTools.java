package com.marvl.imt_lille_douai.marvl.comparison.tools;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import com.marvl.imt_lille_douai.marvl.comparison.image.ComparedImage;

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

    public static Mat loadImgPlain(String imageName) {
        Mat	image	=	imread(imageName,-1);

        if(image.empty()){ throw new RuntimeException("cannot fin img " + imageName + " in classpath");  }

        return image;
    }

    public static Mat loadImgGrayscale(String imageName) {
        Mat image = imread(imageName,0);

        if(image.empty()){ throw new RuntimeException("cannot fin img " + imageName + " in classpath");  }

        return image;
    }

    public static Mat loadImg3ChannelColor(String imageName) {
        Mat image = imread(imageName,1);

        if(image.empty()){ throw new RuntimeException("cannot fin img " + imageName + " in classpath");  }

        return image;
    }

    public static Mat loadThresh(Mat image) {
        Mat thresh = new Mat(image.size());
        threshold(image,thresh,120,255,THRESH_BINARY);

        return thresh;
    }

    public static void displayImg(String windowsName, Mat displayImg) {
        namedWindow(windowsName, WINDOW_AUTOSIZE);	// Create a window for display
        imshow(windowsName, displayImg);	// Show our image inside it
        waitKey(0);	// Wait for a keys in the windows
    }

    /*=public static ArrayList<ComparedImage> convertHashMapToArrayListOfComparedImage(HashMap<String, Float> imgValueMap, String pathToDataBank) {
        ArrayList<ComparedImage> comparedImgArray = new ArrayList<>();

        Set<String> imgSet = imgValueMap.keySet();
        Iterator<String> i = imgSet.iterator();

        String imgName = (String) i.next();
        Float imgDistance = (Float) imgValueMap.get(imgName);

        comparedImgArray.add(new ComparedImage(imgName, imgDistance,pathToDataBank));

        while(i.hasNext()) {
            imgName = (String) i.next();
            imgDistance = (Float) imgValueMap.get(imgName);

            comparedImgArray.add(new ComparedImage(imgName, imgDistance,pathToDataBank));
        }

        return comparedImgArray;
    }*/

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
}
