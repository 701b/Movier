package edu.skku.map.movier;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

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

    private StorageReference storageReference;

    private NavigationView navigationView;
    private EditText movieTitleInput;
    private RecyclerView recyclerView;
    private ImageButton toggleDrawerButton;

    private List<MovieData> movieDataList;
    private MovieItemAdapter movieItemAdapter;

    private ProgressDialog progressDialog;

    private CustomNavigationViewSetting customNavigationViewSetting;

    private boolean isSearchFinished;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toggleDrawerButton = findViewById(R.id.main_toggle_drawer_button);
        navigationView = findViewById(R.id.navigation_view);
        recyclerView = findViewById(R.id.main_recycler_view);
        movieTitleInput = findViewById(R.id.main_movie_title_input);

        storageReference = FirebaseStorage.getInstance().getReference();

        progressDialog = new ProgressDialog(MainActivity.this);

        autoLogin();
        initRecyclerView();
    }

    private void initRecyclerView() {
        movieDataList = new ArrayList<>();
        movieItemAdapter = new MovieItemAdapter(movieDataList, new OnItemClickMovieItemListener() {
            @Override
            public void onItemClickMovieItem(MovieData movieData) {
                Intent intent = new Intent(MainActivity.this, MovieDetailActivity.class);

                intent.putExtra("movie_data", movieData);

                startActivity(intent);
                overridePendingTransition(R.anim.slide_from_bottom, R.anim.do_nothing);
            }
        });
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
        if (CurrentUserInfo.getInstance().getId() == null) {
            // CurrentUserInfo instance에 계정이 저장되어 있지 않다면 자동 로그인 실행
            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

            if (preferences.contains("id") && preferences.contains("password")) {
                // preference에 아이디와 비밀번호가 저장된 경우 자동 로그인
                // 아닌 경우 회원가입 페이지로 간다.
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                final String id = preferences.getString("id", null);
                final String password = preferences.getString("password", null);

                // 데이터베이스와 연결하는데 지연되는 시간동안 dialog를 표시
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
                                        CurrentUserInfo currentUserInfo = CurrentUserInfo.getInstance();

                                        currentUserInfo.setId(post.getId());

                                        customNavigationViewSetting = new CustomNavigationViewSetting(MainActivity.this, toggleDrawerButton);

                                        progressDialog.dismiss();
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
    }

    private void goToSignUpPage() {
        Intent intent = new Intent(MainActivity.this, SignUpActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
        overridePendingTransition(R.anim.slide_from_bottom, R.anim.do_nothing);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CustomNavigationViewSetting.PICK_PROFILE_IMAGE) {
            if (resultCode == RESULT_OK) {
                storageReference.child(UserAccountPost.PROFILE_IMAGE_ADDRESS).child(CurrentUserInfo.getInstance().getId()).putFile(data.getData());
                customNavigationViewSetting.setProfileImage(data.getData());
            }
        }
    }
}
