package com.marvl.imt_lille_douai.marvl.comparison.tools;

import android.content.Context;

import com.marvl.imt_lille_douai.marvl.comparison.image.ComparedImage;
import com.marvl.imt_lille_douai.marvl.comparison.variables.GlobalVariables;
import com.marvl.imt_lille_douai.marvl.comparison.variables.SiftVariables;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_features2d;
import org.bytedeco.javacpp.opencv_features2d.BOWImgDescriptorExtractor;
import org.bytedeco.javacpp.opencv_features2d.FlannBasedMatcher;
import org.bytedeco.javacpp.opencv_features2d.KeyPoint;
import org.bytedeco.javacpp.opencv_ml.CvSVM;
import org.bytedeco.javacpp.opencv_nonfree.SIFT;

public class SiftTools {

    public static ComparedImage doComparison(Context context, ArrayList<File> classifierArray, CvSVM[] classifiers, String testedImagePath){
        Loader.load(opencv_core.class);

        Mat vocabulary = loadVocabulary(context);
        SIFT sift = new SIFT(SiftVariables.nFeatures, SiftVariables.nOctaveLayers, SiftVariables.contrastThreshold, SiftVariables.edgeThresold, SiftVariables.sigma);   // Create SIFT feature point extractor

        FlannBasedMatcher FBMatcher = new FlannBasedMatcher();  // Create a Matcher with FlannBase Euclidien distance. Used to find the nearest word of the trained vocabulary for each keypoint descriptor of the image
        opencv_features2d.DescriptorExtractor DExtractor = sift.asDescriptorExtractor(); // Descriptor extractor that is used to compute descriptors for an input image and its keypoints.
        BOWImgDescriptorExtractor BOWDescriptor = new BOWImgDescriptorExtractor(DExtractor, FBMatcher); // Minimal constructor

        BOWDescriptor.setVocabulary(vocabulary);    // Set vocabulary for the descriptor for calculations compared to what is in vocab. Visual vocabulary whitin each row is a visual word (cluster center)

        Mat imageDescriptor = new Mat();
        KeyPoint keyPoints = new KeyPoint();
        Mat inputDescriptors = new Mat();

        String photoTestName = GlobalTools.getFileNameFromPath(testedImagePath);

        System.out.println(GlobalVariables.debugTag + " photoTestName :" + photoTestName );

        //String photoTest = SystemTools.toCache(context, testedImagePath , photoTestName).getAbsolutePath();
        //String photoTest = SystemTools.getCachePhotoPath(context);

        Mat imageTest = ImageTools.loadImg3ChannelColor(testedImagePath); // RGB image matrix

        sift.detectAndCompute(imageTest, Mat.EMPTY, keyPoints, inputDescriptors); // Detect interesting point in image and convert to matrice | Find keypoints and descriptors in a single step
        BOWDescriptor.compute(imageTest, keyPoints, imageDescriptor);  // Compare imageTest detected keyPoints and store in responseHist | Computes an image descriptor using the set visual vocabulary. Image Descriptor = computed output image descriptor

        return SimilitudeTools.getbestMatch(classifierArray, classifiers, imageDescriptor, testedImagePath);
    }

    // TODO : Revoir la fonction / récup les images depuis le serveur
    public static Mat loadVocabulary(Context context){
        Mat vocabulary;
        //String[] listURL = null;

        /*try { //a suppr quand yml est good
            listURL = context.getAssets().list("yml"); //recup list image
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        String photoPath = SystemTools.getCacheVocabularyPath(context);
        System.out.println(GlobalVariables.debugTag + " photo : " + photoPath);

        opencv_core.CvFileStorage storage = opencv_core.cvOpenFileStorage(photoPath, null, opencv_core.CV_STORAGE_READ); // change et met url cache du fichier
        System.out.println(GlobalVariables.debugTag + " storage" + storage);

        Pointer p = opencv_core.cvReadByName(storage, null, "vocabulary", opencv_core.cvAttrList()); // Find an object by name and decodes it | Null = Function seaches a top level node for the parent Map | vocabulary = node name | cvAttrList = Unused parameter ^^

        opencv_core.CvMat cvMat = new opencv_core.CvMat(p);
        vocabulary = new opencv_core.Mat(cvMat);

        System.out.println(GlobalVariables.debugTag + " vocabulary loaded " + vocabulary.rows() + " x " + vocabulary.cols());
        opencv_core.cvReleaseFileStorage(storage);  // Flush storage before exit

        return vocabulary;
    }

    public static CvSVM[] initClassifiersAndCacheThem(Context context, ArrayList<File> classifierArray) {
        CvSVM[] classifiers = new CvSVM[classifierArray.size()]; // SupportVectorMachine array initialize to nb of xml size

        System.out.println(GlobalVariables.debugTag + " Ok. Creating class name from " + classifierArray.size());

        for (int i = 0; i < classifierArray.size(); i++) {
            System.out.println(GlobalVariables.debugTag + " Ok. Creating class name from " + classifierArray.get(i).getAbsolutePath());

            //open the file to write the resultant descriptor
            classifiers[i] = new CvSVM(); // Default and training constructor
            System.out.println(GlobalVariables.debugTag + " class " + classifiers[i].get_support_vector_count());

            System.out.println(GlobalVariables.debugTag + " class " + classifierArray.get(i).getTotalSpace());

            System.out.println(GlobalVariables.debugTag + " class " + classifierArray.get(i));

            classifiers[i].load( classifierArray.get(i).getAbsolutePath()); // load xml dans classifier en cache url | Load the model from a file (CvStatModel inherit) | Clear the previous XML or YAML to load the complete model state with the specified name from the XML or YAML file.
        }

        return classifiers;
    }

}