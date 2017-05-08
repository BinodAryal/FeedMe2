package com.thavelka.feedme.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.thavelka.feedme.R;
import com.thavelka.feedme.models.Listing;

import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Maybe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;

public class EditListingActivity extends AppCompatActivity implements PlaceSelectionListener,
        GoogleApiClient.OnConnectionFailedListener {

    public static final String ARG_LISTING_TYPE = "listingType";

    private int type;
    private GoogleApiClient mGoogleApiClient;
    private Place place;
    private Disposable photoDisposable;

    @BindView(R.id.edit_listing_img_place) ImageView placeImage;
    @BindView(R.id.edit_listing_text_place_name) TextView placeName;
    @BindView(R.id.edit_listing_text_place_address) TextView placeAddress;
    @BindView(R.id.edit_listing_editText_description) EditText description;
    @BindView(R.id.edit_listing_btn_submit) Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_listing);
        ButterKnife.bind(this);
        type = getIntent().getIntExtra(ARG_LISTING_TYPE, -1);
        if (type < 0) throw new IllegalArgumentException("Type is required argument");

        // Set up API client
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        // Retrieve the PlaceAutocompleteFragment.
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setOnPlaceSelectedListener(this);
    }

    @Override
    protected void onDestroy() {
        if (photoDisposable != null) photoDisposable.dispose();
        super.onDestroy();
    }

    @OnClick(R.id.edit_listing_btn_submit) void submit() {
        if (place == null) return;
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            com.thavelka.feedme.models.Place realmPlace = realm1.where(com.thavelka.feedme.models.Place.class)
                    .equalTo("googlePlacesId", place.getId()).findFirst();
            if (realmPlace == null) realmPlace =
                    realm1.createObject(com.thavelka.feedme.models.Place.class, UUID.randomUUID().toString());
            realmPlace.googlePlacesId = place.getId();
            if (!TextUtils.isEmpty(place.getName())) {
                realmPlace.name = place.getName().toString();
            }
            if (!TextUtils.isEmpty(place.getAddress())) {
                realmPlace.address = place.getAddress().toString();
            }
            if (place.getLatLng() != null) {
                LatLng location = place.getLatLng();
                realmPlace.latitude = location.latitude;
                realmPlace.longitude = location.longitude;
            }

            Listing listing = realm1.createObject(Listing.class, UUID.randomUUID().toString());
            listing.place = realmPlace;
            listing.description = description.getText().toString();
            listing.type = type;
            listing.days = "1234567";
        });
        realm.close();
        finish();
    }

    @Override
    public void onPlaceSelected(Place place) {
        this.place = place;

        placeName.setText(!TextUtils.isEmpty(place.getName()) ? place.getName() : "No Name");
        placeAddress.setText(!TextUtils.isEmpty(place.getAddress()) ? place.getAddress() : "No Address");
        getPhoto();
    }

    @Override
    public void onError(Status status) {
        String errorMessage = "Error: ";
        if (status.getStatusMessage() != null) errorMessage += status.getStatusMessage();
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "API client could not connect", Toast.LENGTH_SHORT).show();
    }

    private void getPhoto() {
        if (!mGoogleApiClient.isConnected() || place == null) return;
        Maybe<Bitmap> getPhoto = Maybe.create(emitter -> {
            try {
                PlacePhotoMetadataResult result = Places.GeoDataApi
                        .getPlacePhotos(mGoogleApiClient, place.getId()).await();

                if (result != null && result.getStatus().isSuccess()) {
                    PlacePhotoMetadataBuffer photoMetadataBuffer = result.getPhotoMetadata();

                    if (photoMetadataBuffer.getCount() <= 0) {
                        photoMetadataBuffer.release();
                        emitter.onComplete();
                        return;
                    }

                    PlacePhotoMetadata photo = photoMetadataBuffer.get(0);
                    Bitmap image = photo.getScaledPhoto(mGoogleApiClient, placeImage.getWidth(),
                            placeImage.getHeight()).await().getBitmap();
                    CharSequence attribution = photo.getAttributions();
                    photoMetadataBuffer.release();
                    emitter.onSuccess(image);
                } else {
                    emitter.onComplete();
                }
            } catch (Throwable t) {
                emitter.onError(t);
            }
        });

        photoDisposable = getPhoto
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(placeImage::setImageBitmap);

    }
}
