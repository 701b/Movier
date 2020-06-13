package edu.skku.map.movier;

import android.graphics.Bitmap;

public class CurrentUserInfo {

    private static CurrentUserInfo instance;

    private String id;
    private Bitmap profileImage;
    private boolean isMan;


    private CurrentUserInfo() {}


    public static CurrentUserInfo getInstance() {
        if (instance == null) {
            instance = new CurrentUserInfo();
        }

        return instance;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isMan() {
        return isMan;
    }

    public void setMan(boolean man) {
        isMan = man;
    }

    public Bitmap getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(Bitmap profileImage) {
        this.profileImage = profileImage;
    }
}
