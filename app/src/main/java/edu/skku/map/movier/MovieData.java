package edu.skku.map.movier;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class MovieData implements Serializable {

    private String title;
    private String image;
    private String pubDate;
    private String director;
    private String actor;


    public String getTitle() {
        String filteredTitle = title.replaceAll("<b>|</b>", "");

        return filteredTitle;
    }

    public String getImage() {
        return image;
    }

    public String getPubDate() {
        return pubDate;
    }

    public String getDirector() {
        String result = "";
        String[] directors = director.split("\\|");

        for (String director : directors) {
            if (result.equals("")) {
                result += director;
            } else {
                result += "\n" + director;
            }
        }

        return result;
    }

    public String getActors() {
        String result = "";
        String[] actors = actor.split("\\|");

        for (String actor : actors) {
            if (result.equals("")) {
                result += actor;
            } else {
                result += "\n" + actor;
            }
        }

        return result;
    }
}
