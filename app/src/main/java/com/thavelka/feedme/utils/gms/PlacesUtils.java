package com.thavelka.feedme.utils.gms;

import android.graphics.Bitmap;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.Places;
import com.thavelka.feedme.utils.ImageUtils;

import io.reactivex.Maybe;

public class PlacesUtils {

    public static Maybe<Image> getPhoto(GoogleApiClient client, String placeId, int w, int h) {
        return Maybe.create(emitter -> {
            try {
                PlacePhotoMetadataResult result = Places.GeoDataApi
                        .getPlacePhotos(client, placeId).await();

                if (result.getStatus().isSuccess()) {
                    PlacePhotoMetadataBuffer photoMetadataBuffer = result.getPhotoMetadata();

                    if (photoMetadataBuffer.getCount() <= 0) {
                        photoMetadataBuffer.release();
                        emitter.onComplete();
                        return;
                    }

                    PlacePhotoMetadata photo = photoMetadataBuffer.get(0);
                    Bitmap fullImage = photo.getScaledPhoto(client, w, h).await().getBitmap();
                    Bitmap thumbnail = ImageUtils.getCroppedBitmap
                            (photo.getScaledPhoto(client, 200, 200).await().getBitmap());
                    CharSequence attribution = photo.getAttributions() != null
                            ? photo.getAttributions() : "";
                    photoMetadataBuffer.release();
                    Image image = new Image(fullImage, thumbnail, attribution.toString());
                    emitter.onSuccess(image);
                } else {
                    emitter.onComplete();
                }
            } catch (Throwable t) {
                emitter.onError(t);
            }
        });
    }
}
