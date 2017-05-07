package com.thavelka.feedme.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Listing extends RealmObject {

    public static final int TYPE_FOOD = 0;
    public static final int TYPE_DRINK = 1;

    @PrimaryKey public String id;
    public Place place;
    public int type;
    public String description;
    public String days;
}
