package edu.skku.map.movier;

import android.app.ProgressDialog;

import com.google.firebase.database.DatabaseReference;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class UserAccountPost implements Serializable {

    public static final String ACCOUNT_TABLE_NAME = "account_table";

    public String id;
    public String password;
    public boolean isMan;


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
        result.put("isMan", isMan);

        return result;
    }

    public void postFirebaseDatabase(DatabaseReference databaseReference) {
        Map<String, Object> childUpdates = new HashMap<>();
        Map<String, Object> postValues = null;

        postValues = toMap();

        childUpdates.put("/" + ACCOUNT_TABLE_NAME + "/" + id, postValues);
        databaseReference.updateChildren(childUpdates);
    }
}
