package com.marvl.imt_lille_douai.marvl.comparison.image;

import com.marvl.imt_lille_douai.marvl.comparison.tools.GlobalTools;

public class ComparedImage {

    private String imageName;
    private String imagePath;
    private Float imageDistance;
    private String imageClass;

    private String bestMatchImage;
    private Long timePrediction;

    public ComparedImage(String imageName, String bestMatchImage){
        this.imageName = imageName;
        this.bestMatchImage = bestMatchImage;
        this.imageClass = GlobalTools.getFileNameFromPath(bestMatchImage);
    }

    public ComparedImage(String imageName, String bestMatchImage, Long timePrediction){
        this.imageName = imageName;
        this.bestMatchImage = bestMatchImage;
        this.timePrediction = timePrediction;
        this.imageClass = GlobalTools.getFileNameFromPath(bestMatchImage);
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

    public String getImgWithoutExtension() { return this.imageClass.substring(0,this.imageClass.length()-4); }

    public String toString(){
        return " [ComparedImage] ImageName : "+ imageName + " bestMatchImage : " + bestMatchImage + " imageClass : " + imageClass + " timePrediction : " + timePrediction + "(ms)";
    }
}

