package com.thavelka.feedme.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class User extends RealmObject {

    @PrimaryKey public String id;
    public String name;
    public String email;
}