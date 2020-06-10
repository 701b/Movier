package edu.skku.map.movier;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.net.URI;

public class CustomNavigationViewSetting {

    public static final int PICK_PROFILE_IMAGE = 777;

    private AppCompatActivity activity;
    private UserAccountPost userAccountPost;
    private ImageButton toggleDrawerButton;

    private ImageView drawerProfileImage;

    public CustomNavigationViewSetting(AppCompatActivity activity, UserAccountPost userAccountPost, ImageButton toggleDrawerButton) {
        this.activity = activity;
        this.userAccountPost = userAccountPost;
        this.toggleDrawerButton = toggleDrawerButton;

        init();
    }

    public void setProfileImage(Uri uri) {
        drawerProfileImage.setImageURI(uri);
    }

    private void init() {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        NavigationView navigationView = activity.findViewById(R.id.main_navigation_view);
        View headerView = navigationView.getHeaderView(0);
        Button drawerLogoutButton = headerView.findViewById(R.id.drawer_logout_button);
        final DrawerLayout drawerLayout = activity.findViewById(R.id.main_drawer_layout);

        drawerProfileImage = headerView.findViewById(R.id.drawer_profile_image);

        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        drawerProfileImage.setClipToOutline(true);
        drawerProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);

                activity.startActivityForResult(gallery, PICK_PROFILE_IMAGE);
            }
        });

        drawerLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
                SharedPreferences.Editor editor = preferences.edit();
                Intent intent = new Intent(activity, LoginActivity.class);

                editor.remove("id");
                editor.remove("password");
                editor.apply();

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                activity.startActivity(intent);
            }
        });

        storageReference.child(userAccountPost.PROFILE_IMAGE_ADDRESS).child(userAccountPost.getId())
                .getBytes(4 * 1024 * 1024).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                drawerProfileImage.setImageBitmap(bitmap);
            }
        });

        toggleDrawerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.END);
            }
        });
    }

}
