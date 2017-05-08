package com.thavelka.feedme.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Listing extends RealmObject {

    public static final String TYPE_FOOD = "food";
    public static final String TYPE_DRINK = "drink";

    @PrimaryKey public String id;
    public Place place;
    public int type;
    public String description;
    public String days;
}
