package com.marvl.imt_lille_douai.marvl.comparison.tools;

import org.bytedeco.javacpp.opencv_core;

import static org.bytedeco.javacpp.opencv_highgui.WINDOW_AUTOSIZE;
import static org.bytedeco.javacpp.opencv_highgui.imread;
import static org.bytedeco.javacpp.opencv_highgui.imshow;
import static org.bytedeco.javacpp.opencv_highgui.namedWindow;
import static org.bytedeco.javacpp.opencv_highgui.waitKey;
import static org.bytedeco.javacpp.opencv_imgproc.THRESH_BINARY;
import static org.bytedeco.javacpp.opencv_imgproc.threshold;

public class ImageTools {

    public static opencv_core.Mat loadImgPlain(String imageName) {
        opencv_core.Mat image	=	imread(imageName,-1);

        if(image.empty()){ throw new RuntimeException("cannot fin img " + imageName + " in classpath");  }

        return image;
    }

    public static opencv_core.Mat loadImgGrayscale(String imageName) {
        opencv_core.Mat image = imread(imageName,0);

        if(image.empty()){ throw new RuntimeException("cannot fin img " + imageName + " in classpath");  }

        return image;
    }

    public static opencv_core.Mat loadImg3ChannelColor(String imageName) {
        opencv_core.Mat image = imread(imageName,1);

        if(image.empty()){ throw new RuntimeException("cannot fin img " + imageName + " in classpath");  }

        return image;
    }

    public static opencv_core.Mat loadThresh(opencv_core.Mat image) {
        opencv_core.Mat thresh = new opencv_core.Mat(image.size());
        threshold(image,thresh,120,255,THRESH_BINARY);

        return thresh;
    }

    public static void displayImg(String windowsName, opencv_core.Mat displayImg) {
        namedWindow(windowsName, WINDOW_AUTOSIZE);	// Create a window for display
        imshow(windowsName, displayImg);	// Show our image inside it
        waitKey(0);	// Wait for a keys in the windows
    }

}
