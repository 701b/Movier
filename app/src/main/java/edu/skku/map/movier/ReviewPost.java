package edu.skku.map.movier;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReviewPost {

    // review_list/{movie_title}/{user_name}
    public static final String REVIEW_TABLE_NAME = "review_list";

    private String idArrayWhoPressThumb = "";
    private String id;
    private boolean isMan;
    private int score;
    private String content;

    private List<String> idListWhoPressThumb;
    private Bitmap profileImage;


    public ReviewPost() {}

    public ReviewPost(String id, boolean isMan, int score, String content, Bitmap profileImage) {
        this.id = id;
        this.isMan = isMan;
        this.score = score;
        this.content = content;
        this.profileImage = profileImage;

        init();
    }


    public void init() {
        idListWhoPressThumb = new ArrayList<>();
        String[] idArray = idArrayWhoPressThumb.split("\\|");

        idListWhoPressThumb.addAll(Arrays.asList(idArray));

        idListWhoPressThumb.remove("");
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();

        result.put("id", id);
        result.put("isMan", isMan);
        result.put("score", score);
        result.put("content", content);
        result.put("idArrayWhoPressThumb", idArrayWhoPressThumb);

        return result;
    }

    public void postFirebaseDatabase(String movieTitle) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Map<String, Object> childUpdates = new HashMap<>();
        Map<String, Object> postValues = null;

        idArrayWhoPressThumb = "";

        for (String id : idListWhoPressThumb) {
            if (idArrayWhoPressThumb.equals("")) {
                idArrayWhoPressThumb = id;
            } else {
                idArrayWhoPressThumb = idArrayWhoPressThumb + "|" + id;
            }
        }

        postValues = toMap();

        childUpdates.put("/" + REVIEW_TABLE_NAME + "/" + movieTitle + "/" + id, postValues);
        databaseReference.updateChildren(childUpdates);
    }

    public boolean getIsPressThumb(String id) {
        return idListWhoPressThumb.contains(id);
    }

    public void addIdWhoPressThumb(String id) {
        if (!idListWhoPressThumb.contains(id)) {
            idListWhoPressThumb.add(id);
        } else {
            Log.d("TEST", "좋아요 누른 멤버 잘 못 추가함");
        }
    }

    public void removeIdWhoPressThumb(String id) {
        idListWhoPressThumb.remove(id);
    }

    public int getNumberOfThumb() {
        return idListWhoPressThumb.size();
    }

    public String getId() {
        return id;
    }

    public boolean getIsMan() {
        return isMan;
    }

    public int getScore() {
        return score;
    }

    public String getContent() {
        return content;
    }

    public String getIdArrayWhoPressThumb() {
        return idArrayWhoPressThumb;
    }

    public Bitmap getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(Bitmap profileImage) {
        this.profileImage = profileImage;
    }
}
