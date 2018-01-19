package com.marvl.imt_lille_douai.marvl.comparison.image;

import com.marvl.imt_lille_douai.marvl.comparison.tools.GlobalVariables;

public class ComparedImage {

    private String imageName;
    private Float imageDistance;
    private String imageClass;

    private String bestMatchImage;
    private Long timePrediction;

    /* Old contrstuctor, might be reused
    public ComparedImage(String imageName, Float imageDistance, String pathToDataBank) {
        this.imageName = imageName;
        this.imageDistance = imageDistance;

        //String clearImgClass = imageName.replaceAll(pathToDataBank, "");
        //this.imageClass = clearImgClass.substring(0,clearImgClass.indexOf("_"));
    } */

    public ComparedImage(String imageName, String bestMatchImage, Long timePrediction){
        this.imageName = imageName;
        this.bestMatchImage = bestMatchImage;
        this.timePrediction = timePrediction;
    }


    public String getImageName() {
        return imageName;
    }

    public Float getImageDistance() {
        return imageDistance;
    }

    public String getImageClass() {
        return imageClass;
    }

    public String getBestMatchImage() { return bestMatchImage; }

    public Long getTimePrediction() { return timePrediction; }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public void setImageDistance(Float imageDistance) {
        this.imageDistance = imageDistance;
    }

    public void setBestMatchImage(String bestMatchImage) { this.bestMatchImage = bestMatchImage; }

    public void setTimePrediction(Long timePrediction) { this.timePrediction = timePrediction; }
}

