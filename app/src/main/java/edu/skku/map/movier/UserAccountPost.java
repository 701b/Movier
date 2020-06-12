package edu.skku.map.movier;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class UserAccountPost implements Serializable {

    // account_table/{id}
    public static final String ACCOUNT_TABLE_NAME = "account_table";
    // profile_image/{id}
    public static final String PROFILE_IMAGE_ADDRESS = "profile_image";

    private String id;
    private String password;
    private boolean isMan;


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

        return result;
    }

    public void postFirebaseDatabase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
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

    public static void addOnDownloadProfileImage(String id, final OnDownloadProfileImageListener onDownloadProfileImageListener) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();

        storageReference.child(PROFILE_IMAGE_ADDRESS).child(id).getBytes(4 * 1024 * 1024).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                onDownloadProfileImageListener.onDownloadProfileImage(bitmap);
            }
        });
    }
}
