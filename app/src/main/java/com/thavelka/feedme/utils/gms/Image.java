package com.thavelka.feedme.utils.gms;

import android.graphics.Bitmap;

import com.thavelka.feedme.utils.ImageUtils;

public class Image {
    private Bitmap image;
    private Bitmap thumbnail;
    private String attribution;

    public Image(Bitmap image, Bitmap thumbnail, String attribution) {
        this.image = image;
        this.thumbnail = thumbnail;
        this.attribution = attribution;
    }

    public Bitmap getImage() {
        return image;
    }

    public Bitmap getThumbnail() {
        return thumbnail;
    }

    public byte[] getThumbnailBytes() {
        return ImageUtils.getImageBytes(thumbnail, Bitmap.CompressFormat.PNG, 50);
    }

    public String getAttribution() {
        return attribution;
    }
}