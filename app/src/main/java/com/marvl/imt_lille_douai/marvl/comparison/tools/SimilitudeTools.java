package com.marvl.imt_lille_douai.marvl.comparison.tools;

import android.content.Context;

import com.marvl.imt_lille_douai.marvl.comparison.image.ComparedImage;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_ml.CvSVM;

import java.io.File;
import java.util.ArrayList;

public class SimilitudeTools {

    public static ComparedImage getbestMatch(ArrayList<File> classifierArray, CvSVM[] classifiers, Mat responseHist, String photoTest){
        float minf = Float.MAX_VALUE;
        String bestMatch = null;

        // loop for all classes | xml
        for (int i = 0; i < classifierArray.size(); i++) {
            float res = classifiers[i].predict(responseHist, true); // classifier prediction based on reconstructed histogram

            //System.out.println(class_names[i] + " is " + res);

            if (res < minf) {
                minf = res;
                bestMatch = classifierArray.get(i).getAbsolutePath();
            }
        }

        return new ComparedImage(photoTest,bestMatch);
    }

}
