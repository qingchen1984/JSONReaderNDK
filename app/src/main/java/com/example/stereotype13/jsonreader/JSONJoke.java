package com.example.stereotype13.jsonreader;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by stereotype13 on 4/18/15.
 */

public class JSONJoke {
    private String type;
    private Joke value;

    public String getType() {return type;}
    public Joke getValue() {return value;}

    static public class Joke {
        private int id;
        private String joke;
        private String[] categories;

        public String getJoke() {return joke;}

    }

}