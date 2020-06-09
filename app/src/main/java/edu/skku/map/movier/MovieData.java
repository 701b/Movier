package edu.skku.map.movier;

public class MovieData {

    private String title;
    private String imageUrl;
    private String pubDate;
    private String director;
    private String actor;

    public String getTitle() {
        String filteredTitle = title.replaceAll("<b>|</b>", "");

        return filteredTitle;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getPubDate() {
        return pubDate;
    }

    public String getDirector() {
        return director;
    }

    public String getActor() {
        return actor;
    }
}
