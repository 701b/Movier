package edu.skku.map.movier;

import java.util.ArrayList;
import java.util.List;

public class MovieData {

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
        return director;
    }

    public String getActors() {
        String result = "";
        String[] actors = actor.split("\\|");

        for (String actor : actors) {
            result += actor + " ";
        }

        return result;
    }
}
