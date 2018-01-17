package com.marvl.imt_lille_douai.marvl.comparison.tools;

import com.marvl.imt_lille_douai.marvl.comparison.image.ComparedImage;

import static org.bytedeco.javacpp.opencv_highgui.WINDOW_AUTOSIZE;
//import static org.bytedeco.javacpp.opencv_highgui.imread;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_highgui.imshow;
import static org.bytedeco.javacpp.opencv_highgui.namedWindow;
import static org.bytedeco.javacpp.opencv_highgui.waitKey;
//import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgproc.THRESH_BINARY;
import static org.bytedeco.javacpp.opencv_imgproc.threshold;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_imgcodecs;

public class GlobalTools {

    public static Mat loadImgLena() {
        String	imgName	=	"lena.jpg";
        Mat	image	=	imread(imgName);

        if	(image.empty())	{
            throw	new	RuntimeException("cannot	find	img	"	+	imgName	+	"	in	classpath");
        }

        return image;
    }

    public static Mat loadImg(String imageName) {
        Mat	image	=	imread(imageName);

        if	(image.empty())	{
            throw	new	RuntimeException("cannot	find	img	"	+	imageName	+	"	in	classpath");
        }

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

    public static ArrayList<ComparedImage> convertHashMapToArrayListOfComparedImage(HashMap<String, Float> imgValueMap, String pathToDataBank) {
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
    }
}
