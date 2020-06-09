package edu.skku.map.movier;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
/*
        new NaverMovieSearch("어벤져스", new OnReceiveMovieDataListener() {
            @Override
            public void onReceiveMovieData(List<MovieData> movieDataList) {
                for (MovieData data : movieDataList) {
                    Log.d("TEST", data.getTitle());
                }
            }
        });
*/
        ImageButton toggleDrawerButton = findViewById(R.id.main_toggle_drawer_button);

        drawerLayout = findViewById(R.id.main_drawer_layout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        toggleDrawerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.END);
            }
        });
    }
}
