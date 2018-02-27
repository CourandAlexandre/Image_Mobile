package com.marvl.imt_lille_douai.marvl.comparison.image;

import android.net.Uri;

import com.marvl.imt_lille_douai.marvl.comparison.tools.GlobalTools;

public class Img {

    private String imageName;
    private String imagePath;
    private Uri imageUri;

    public Img(String imagePath){
        this.imageName = GlobalTools.getFileNameFromPath(imagePath);
        this.imagePath = imagePath;
    }

    public Img(Uri imageUri){
        this.imageName = GlobalTools.getFileNameFromPath(imageUri.getPath());
        this.imageUri = imageUri;
    }

    public Img(String imagePath, String imageName){
        this.imageName = imageName;
        this.imagePath = imagePath;
    }

    public String getImageName() { return imageName; }

    public String getImagePath() { return imagePath; }

    public Uri getImageUri() { return imageUri; }

    public void setImagePath(String imagePath) {
        this.imageName = GlobalTools.getFileNameFromPath(imagePath);
        this.imagePath = imagePath;
    }

    public void setImageUri(Uri imageUri) {
        this.imageName = GlobalTools.getFileNameFromPath(imageUri.getPath());
        this.imageUri = imageUri;
    }
}


