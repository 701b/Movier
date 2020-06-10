package edu.skku.map.movier;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_PROFILE_IMAGE = 777;

    private UserAccountPost userAccountPost;

    private DrawerLayout drawerLayout;
    private TextView drawerIdText;
    private View headerView;
    private ImageView drawerProfileImage;
    private EditText movieTitleInput;
    RecyclerView recyclerView;

    private ProgressDialog progressDialog;

    private StorageReference storageReference;

    private List<MovieData> movieDataList;
    private MovieItemAdapter movieItemAdapter;

    private boolean isSearchFinished;


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
        NavigationView navigationView = findViewById(R.id.main_navigation_view);

        headerView = navigationView.getHeaderView(0);
        drawerIdText = headerView.findViewById(R.id.drawer_id_text);
        drawerLayout = findViewById(R.id.main_drawer_layout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        recyclerView = findViewById(R.id.main_recycler_view);

        storageReference = FirebaseStorage.getInstance().getReference();
        progressDialog = new ProgressDialog(MainActivity.this);

        movieTitleInput = findViewById(R.id.main_movie_title_input);

        autoLogin();
        initDrawerHeader();
        initRecyclerView();

        toggleDrawerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.END);
            }
        });
    }

    private void initRecyclerView() {
        movieDataList = new ArrayList<>();
        movieItemAdapter = new MovieItemAdapter(this, movieDataList);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(movieItemAdapter);

        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (!isSearchFinished) {
                    if (!recyclerView.canScrollVertically(1)) {
                        new NaverMovieSearch(movieTitleInput.getText().toString(), movieDataList.size() + 1, new OnReceiveMovieDataListener() {
                            @Override
                            public void onReceiveMovieData(List<MovieData> movieDataList) {
                                if (movieDataList.size() < 10) {
                                    isSearchFinished = true;
                                } else {
                                    isSearchFinished = false;
                                }

                                MainActivity.this.movieDataList.addAll(movieDataList);
                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        movieItemAdapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        });
                    }
                }
            }
        });

        movieTitleInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    new NaverMovieSearch(movieTitleInput.getText().toString(), 1, new OnReceiveMovieDataListener() {
                        @Override
                        public void onReceiveMovieData(List<MovieData> movieDataList) {
                            if (movieDataList.size() < 10) {
                                isSearchFinished = true;
                            } else {
                                isSearchFinished = false;
                            }

                            MainActivity.this.movieDataList.clear();
                            MainActivity.this.movieDataList.addAll(movieDataList);
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    movieItemAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    });
                }

                return true;
            }
        });
    }

    private void autoLogin() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        // preference에 아이디와 비밀번호가 저장된 경우 자동 로그인
        // 아닌 경우 회원가입 페이지로 간다.
        if (preferences.contains("id") && preferences.contains("password")) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
            final String id = preferences.getString("id", null);
            final String password = preferences.getString("password", null);

            // 데이터베이스와 서버 Storage 연결하는데 지연되는 시간동안 dialog를 표시
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("잠시만 기다려주세요");
            progressDialog.show();

            databaseReference.child(UserAccountPost.ACCOUNT_TABLE_NAME).child(id)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            UserAccountPost post = dataSnapshot.getValue(UserAccountPost.class);

                            if (post != null) {
                                // 데이터베이스에 같은 id가 존재할 때
                                if (post.getPassword().equals(password)) {
                                    // 비밀번호가 데이터베이스의 비밀번호와 같을 때
                                    userAccountPost = post;
                                    drawerIdText.setText(userAccountPost.getId());
                                    loadProfileImage();
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_PROFILE_IMAGE) {
            if (resultCode == RESULT_OK) {
                storageReference.child(UserAccountPost.PROFILE_IMAGE_ADDRESS).child(userAccountPost.getId()).putFile(data.getData());
                drawerProfileImage.setImageURI(data.getData());
            }
        }
    }

    private void initDrawerHeader() {
        Button drawerLogoutButton = headerView.findViewById(R.id.drawer_logout_button);

        drawerProfileImage = headerView.findViewById(R.id.drawer_profile_image);
        drawerProfileImage.setClipToOutline(true);
        drawerProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);

                startActivityForResult(gallery, PICK_PROFILE_IMAGE);
            }
        });

        drawerLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                SharedPreferences.Editor editor = preferences.edit();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);

                editor.remove("id");
                editor.remove("password");
                editor.apply();

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                startActivity(intent);
            }
        });
    }

    private void loadProfileImage() {
        storageReference.child(userAccountPost.PROFILE_IMAGE_ADDRESS).child(userAccountPost.getId())
                .getBytes(4 * 1024 * 1024).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                        drawerProfileImage.setImageBitmap(bitmap);
                        progressDialog.dismiss();
                    }
        });
    }

    private void goToSignUpPage() {
        Intent intent = new Intent(MainActivity.this, SignUpActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_bottom, R.anim.do_nothing);
    }
}
