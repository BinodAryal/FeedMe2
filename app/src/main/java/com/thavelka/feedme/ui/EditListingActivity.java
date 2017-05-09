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
import android.widget.ToggleButton;

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
import com.thavelka.feedme.utils.ImageUtils;

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
    private Image image;

    @BindView(R.id.edit_listing_img_place) ImageView placeImage;
    @BindView(R.id.edit_listing_text_place_name) TextView placeName;
    @BindView(R.id.edit_listing_text_place_address) TextView placeAddress;
    @BindView(R.id.edit_listing_editText_description) EditText description;
    @BindView(R.id.edit_listing_btn_sun) ToggleButton sundayButton;
    @BindView(R.id.edit_listing_btn_mon) ToggleButton mondayButton;
    @BindView(R.id.edit_listing_btn_tue) ToggleButton tuesdayButton;
    @BindView(R.id.edit_listing_btn_wed) ToggleButton wednesdayButton;
    @BindView(R.id.edit_listing_btn_thu) ToggleButton thursdayButton;
    @BindView(R.id.edit_listing_btn_fri) ToggleButton fridayButton;
    @BindView(R.id.edit_listing_btn_sat) ToggleButton saturdayButton;
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
            if (image != null) {
                realmPlace.thumbnail = image.getThumbnailBytes();
            }

            String days = "";
            if (sundayButton.isChecked()) days += "1";
            if (mondayButton.isChecked()) days += "2";
            if (tuesdayButton.isChecked()) days += "3";
            if (wednesdayButton.isChecked()) days += "4";
            if (thursdayButton.isChecked()) days += "5";
            if (fridayButton.isChecked()) days += "6";
            if (saturdayButton.isChecked()) days += "7";

            Listing listing = realm1.createObject(Listing.class, UUID.randomUUID().toString());
            listing.place = realmPlace;
            listing.description = description.getText().toString();
            listing.type = type;
            listing.days = days;
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
        Maybe<Image> getPhoto = Maybe.create(emitter -> {
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
                    Bitmap fullImage = photo.getScaledPhoto(mGoogleApiClient, placeImage.getWidth(),
                            1600).await().getBitmap();
                    Bitmap thumbnail = ImageUtils.getCroppedBitmap
                            (photo.getScaledPhoto(mGoogleApiClient, 200, 200).await().getBitmap());
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

        photoDisposable = getPhoto
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(image -> {
                    this.image = image;
                    placeImage.setImageBitmap(image.getImage());
                });

    }

    public static class Image {
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
}
