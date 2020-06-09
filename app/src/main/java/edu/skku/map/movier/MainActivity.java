package edu.skku.map.movier;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageButton;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private UserAccountPost userAccountPost;

    private DrawerLayout drawerLayout;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        // preference에 아이디와 비밀번호가 저장된 경우 자동 로그인
        // 아닌 경우 회원가입 페이지로 간다.
        if (preferences.contains("id") && preferences.contains("password")) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
            final String id = preferences.getString("id", null);
            final String password = preferences.getString("password", null);

            databaseReference.child(UserAccountPost.ACCOUNT_TABLE_NAME).child(id)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            UserAccountPost post = dataSnapshot.getValue(UserAccountPost.class);

                            if (post != null) {
                                // 데이터베이스에 같은 id가 존재할 때
                                if (post.password.equals(password)) {
                                    // 비밀번호가 데이터베이스의 비밀번호와 같을 때
                                    userAccountPost = post;
                                } else {
                                    // 비밀번호가 데이터베이스의 비밀번호와 다를 때
                                    goToSignUpPage();
                                }
                            } else {
                                // 데이터베이스에 같은 id가 존재하지 않을 때
                                goToSignUpPage();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        } else {
            goToSignUpPage();
        }
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

    private void goToSignUpPage() {
        Intent intent = new Intent(MainActivity.this, SignUpActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
    }
}
