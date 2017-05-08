package com.thavelka.feedme.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.thavelka.feedme.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditListingActivity extends AppCompatActivity {

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
    }
}
