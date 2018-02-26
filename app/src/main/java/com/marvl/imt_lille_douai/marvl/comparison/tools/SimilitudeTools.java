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

        long timePrediction = System.currentTimeMillis();

        // loop for all classes | xml
        for (int i = 0; i < classifierArray.size(); i++) {

            float res = classifiers[i].predict(responseHist, true); // classifier prediction based on reconstructed histogram

            //System.out.println(class_names[i] + " is " + res);

            if (res < minf) {
                minf = res;
                bestMatch = classifierArray.get(i).getAbsolutePath();
            }
        }

        timePrediction = System.currentTimeMillis() - timePrediction;

        return new ComparedImage(photoTest,bestMatch,timePrediction);
    }

    // TODO : Remove SYSO
    public void findBestTruc(ArrayList<File> classifierArray, Context context){
        /*final opencv_ml.CvSVM[] classifiers = SiftTools.initClassifiersAndCacheThem(context, classifierArray) ;

        System.out.println(GlobalVariables.debugTag + " class0 " + classifiers[0].get_support_vector_count());
        System.out.println(GlobalVariables.debugTag + " class1 " + classifiers[1].sizeof());
        System.out.println(GlobalVariables.debugTag + " class2 " + classifiers[2].sizeof());

        // ComparedImage comparedImage = SiftTools.doComparison(context,classifierArray,classifiers);

        // System.out.println(GlobalVariables.debugTag + comparedImage.getImageName() + "  predicted as " + comparedImage.getBestMatchImage() + " in " + comparedImage.getTimePrediction() + " ms");
        */
    }

}
