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
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class CustomNavigationViewSetting {

    public static final int PICK_PROFILE_IMAGE = 777;

    private AppCompatActivity activity;
    private ImageButton toggleDrawerButton;

    private ImageView drawerProfileImage;

    public CustomNavigationViewSetting(AppCompatActivity activity, ImageButton toggleDrawerButton) {
        this.activity = activity;
        this.toggleDrawerButton = toggleDrawerButton;

        init();
    }

    public void setProfileImage(Uri uri) {
        drawerProfileImage.setImageURI(uri);
    }

    private void init() {
        NavigationView navigationView = activity.findViewById(R.id.navigation_view);
        View headerView = navigationView.getHeaderView(0);
        LinearLayout drawerLogoutLayout = headerView.findViewById(R.id.drawer_logout_layout);
        TextView idText = headerView.findViewById(R.id.drawer_id_text);
        final DrawerLayout drawerLayout = activity.findViewById(R.id.drawer_layout);

        drawerProfileImage = headerView.findViewById(R.id.drawer_profile_image);

        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        idText.setText(CurrentUserInfo.getInstance().getId());

        drawerProfileImage.setClipToOutline(true);
        drawerProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);

                activity.startActivityForResult(gallery, PICK_PROFILE_IMAGE);
            }
        });

        drawerLogoutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
                SharedPreferences.Editor editor = preferences.edit();
                Intent intent = new Intent(activity, LoginActivity.class);

                editor.remove("id");
                editor.remove("password");
                editor.apply();

                CurrentUserInfo.getInstance().setId(null);
                CurrentUserInfo.getInstance().setProfileImage(null);

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                activity.startActivity(intent);
            }
        });

        if (CurrentUserInfo.getInstance().getProfileImage() != null) {
            drawerProfileImage.setImageBitmap(CurrentUserInfo.getInstance().getProfileImage());
        }

        toggleDrawerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.END);
            }
        });
    }

}
