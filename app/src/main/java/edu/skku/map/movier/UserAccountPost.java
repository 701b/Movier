package edu.skku.map.movier;

import android.app.ProgressDialog;

import com.google.firebase.database.DatabaseReference;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class UserAccountPost implements Serializable {

    public static final String ACCOUNT_TABLE_NAME = "account_table";
    public static final String PROFILE_IMAGE_ADDRESS = "profile_image";

    private String id;
    private String password;
    private boolean isMan;
    private String profileImageAddress;


    public UserAccountPost() {}

    public UserAccountPost(String id, String password, boolean isMan) {
        this.id = id;
        this.password = password;
        this.isMan = isMan;
    }


    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();

        result.put("id", id);
        result.put("password", password);
        result.put("is_man", isMan);
        result.put("profile_image_address", profileImageAddress);

        return result;
    }

    public void postFirebaseDatabase(DatabaseReference databaseReference) {
        Map<String, Object> childUpdates = new HashMap<>();
        Map<String, Object> postValues = null;

        postValues = toMap();

        childUpdates.put("/" + ACCOUNT_TABLE_NAME + "/" + id, postValues);
        databaseReference.updateChildren(childUpdates);
    }

    public String getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public String getProfileImageAddress() {
        return profileImageAddress;
    }

    public void setProfileImageAddress(String profileImageAddress) {
        this.profileImageAddress = profileImageAddress;
    }
}
