package com.marvl.imt_lille_douai.marvl.comparison.tools;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.marvl.imt_lille_douai.marvl.BuildConfig;
import com.marvl.imt_lille_douai.marvl.R;
import com.marvl.imt_lille_douai.marvl.comparison.tools.SiftTools;
import com.marvl.imt_lille_douai.marvl.controller.MainActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.bytedeco.javacpp.opencv_features2d.drawMatches;
import static org.bytedeco.javacpp.opencv_highgui.imread;
import static org.bytedeco.javacpp.opencv_highgui.imshow;
import static org.bytedeco.javacpp.opencv_highgui.imwrite;
import static org.bytedeco.javacpp.opencv_highgui.namedWindow;
import static org.bytedeco.javacpp.opencv_highgui.waitKey;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_features2d.BOWImgDescriptorExtractor;
import org.bytedeco.javacpp.opencv_features2d.FlannBasedMatcher;
import org.bytedeco.javacpp.opencv_features2d.KeyPoint;
import org.bytedeco.javacpp.opencv_ml.CvSVM;
import org.bytedeco.javacpp.opencv_nonfree.SIFT;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SiftTools {

    // Le nombre de meilleurs caractéristiques à retenir. Les caractéristiques sont classées par leurs scores (mesuré dans l'algorithme SIFT comme le contraste local).
    public static int nFeatures = 0;

    // Nombre de couche dans chaque octave (3 est la valeur utilisée avec D.Lowe). Le nombre d'octave est calculé automatiquement à partir de la résolution de l'image.
    public static int nOctaveLayers = 3;

    // Seuil de contraste utilisé pour filtrer les caractéristiques des régions à faible contraste. Plus le seuil est important, moins les caractéristiques sont produites par le détecteur.
    public static double contrastThreshold = 0.03;

    // Seuil utilisé pour filtrer les caractéristiques de pointe. Plus la valeur est importante moins les caractéristiques sont filtrées
    public static int edgeThresold = 10;

    // Sigma gaussien appliqué à l'image d'entrée à l'octave \ # 0. Réduire le nombre si image capturée est de faible qualité
    public static double sigma = 1.6;

    public static void doComparison(Context context, ArrayList<String> classifierArray, CvSVM[] classifiers){
        Loader.load(opencv_core.class);

        Mat vocabulary = loadVocabulary(context);
        SIFT sift = new SIFT(SiftTools.nFeatures, SiftTools.nOctaveLayers, SiftTools.contrastThreshold, SiftTools.edgeThresold, SiftTools.sigma);   // Create SIFT feature point extractor
        FlannBasedMatcher FBMatcher = new FlannBasedMatcher();  // Create a Matcher with FlannBase Euclidien distance
        BOWImgDescriptorExtractor BOWDescriptor = new BOWImgDescriptorExtractor(sift.asDescriptorExtractor(), FBMatcher);

        BOWDescriptor.setVocabulary(vocabulary);    // Set vocabulary for the descriptor for calculations compared to what is in vocab

        Mat responseHist = new Mat();
        KeyPoint keyPoints = new KeyPoint();
        Mat inputDescriptors = new Mat();

        String photoTest = GlobalTools.toCache(context, "ImageBank/TestImage/Pepsi_13.jpg", "Pepsi_13.jpg").getAbsolutePath();
        //File im = new File( "ImageBank/TestImage/Coca_12.jpg");

        Mat imageTest = imread(photoTest, 1); // Load img

        sift.detectAndCompute(imageTest, Mat.EMPTY, keyPoints, inputDescriptors); // Detect interesting point in image and convert to matrice
        BOWDescriptor.compute(imageTest, keyPoints, responseHist);  // Compare imageTest detected keyPoints and store in responseHist

        // TODO : Replace code below by a fonction

        // Finding best match
        float minf = Float.MAX_VALUE;
        String bestMatch = null;

        long timePrediction = System.currentTimeMillis();
        // loop for all classes
        for (int i = 0; i < classifierArray.size(); i++) {
            // classifier prediction based on reconstructed histogram
            float res = classifiers[i].predict(responseHist, true);
            //System.out.println(class_names[i] + " is " + res);
            if (res < minf) {
                minf = res;
                bestMatch = classifierArray.get(i);
            }
        }

        timePrediction = System.currentTimeMillis() - timePrediction;
        System.out.println(photoTest + "  predicted as " + bestMatch + " in " + timePrediction + " ms");

    }

    public static Mat loadVocabulary(Context context){
        Mat vocabulary;
        String[] listURL = null;

        try {
            listURL = context.getAssets().list("yml"); //recup list image
        } catch (IOException e) {
            e.printStackTrace();
        }

        String photo = GlobalTools.toCache(context, "yml/" + listURL[0], listURL[0]).getAbsolutePath();
        System.out.println("testtest : " + photo);

        opencv_core.CvFileStorage storage = opencv_core.cvOpenFileStorage(photo, null, opencv_core.CV_STORAGE_READ); // change et met url cache du fichier
        System.out.println("storage" + storage);

        Pointer p = opencv_core.cvReadByName(storage, null, "vocabulary", opencv_core.cvAttrList());
        opencv_core.CvMat cvMat = new opencv_core.CvMat(p);
        vocabulary = new opencv_core.Mat(cvMat);

        System.out.println("vocabulary loaded " + vocabulary.rows() + " x " + vocabulary.cols());
        opencv_core.cvReleaseFileStorage(storage);  // Flush storage before exit

        return vocabulary;

    }

    public static CvSVM[] initClassifiersAndCacheThem(Context context, ArrayList<String> classifierArray) {
        CvSVM[] classifiers = new CvSVM[classifierArray.size()];

        for (int i = 0; i < classifierArray.size(); i++) {
            //System.out.println("Ok. Creating class name from " + className);

            //open the file to write the resultant descriptor
            classifiers[i] = new CvSVM();
            System.out.println("class " + classifiers[i].get_support_vector_count());

            String fileTemp = GlobalTools.toCache(context, "classifier/" + classifierArray.get(i), classifierArray.get(i)).getAbsolutePath();
            System.out.println(fileTemp);

            classifiers[i].load(fileTemp); // load xml dans classifier en cache url

        }

        return classifiers;
    }

}