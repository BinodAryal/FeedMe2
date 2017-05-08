package com.thavelka.feedme.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Place extends RealmObject {

    @PrimaryKey public String id;
    public String googlePlacesId;
    public String name;
    public String address;
    public Double latitude;
    public Double longitude;
    public byte[] thumbnail;
}
