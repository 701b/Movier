package edu.skku.map.movier;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class MovieDetailActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;

    private CustomNavigationViewSetting customNavigationViewSetting;

    private MovieData movieData;

    private MovieReviewAdapter movieReviewAdapter;
    private List<ReviewPost> reviewDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        Intent receivedIntent = getIntent();
        movieData = (MovieData) receivedIntent.getSerializableExtra("movie_data");

        ImageButton backButton = findViewById(R.id.movie_detail_back_button);
        ImageButton toggleDrawerButton = findViewById(R.id.movie_detail_toggle_drawer_button);
        ImageView posterImage = findViewById(R.id.movie_detail_poster_image);
        TextView titleText = findViewById(R.id.movie_detail_title_text);
        TextView subtitleText = findViewById(R.id.movie_detail_subtitle_text);
        TextView scoreText = findViewById(R.id.movie_detail_score_text);
        TextView directorText = findViewById(R.id.movie_detail_director_text);
        TextView pubDateText = findViewById(R.id.movie_detail_pub_date_text);
        TextView actorText = findViewById(R.id.movie_detail_actor_text);
        LinearLayout contentLayout = findViewById(R.id.movie_detail_content_layout);
        LinearLayout moreReviewLayout = findViewById(R.id.movie_detail_more_review_layout);
        LinearLayout writeReviewLayout = findViewById(R.id.movie_detail_write_review_layout);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        reviewDataList = new ArrayList<>();
        customNavigationViewSetting = new CustomNavigationViewSetting(MovieDetailActivity.this, toggleDrawerButton);

        titleText.setText(movieData.getTitle());
        subtitleText.setText(movieData.getPubDate());
        directorText.setText(movieData.getDirector());
        pubDateText.setText(movieData.getPubDate());
        actorText.setText(movieData.getActors());

        new DownloadImageTask(movieData, posterImage).execute();

        initRecyclerView();

        writeReviewLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(android.R.anim.fade_in, R.anim.slide_to_bottom);
            }
        });
    }

    private void initRecyclerView() {
        final int INITIAL_NUMBER_OF_REVIEW_POST_SHOWN = 3;

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        RecyclerView recyclerView = findViewById(R.id.movie_detail_recycler_view);
        final LinearLayout moreReviewLayout = findViewById(R.id.movie_detail_more_review_layout);
        final TextView reviewNumberText = findViewById(R.id.movie_detail_review_number_text);

        reviewDataList = new ArrayList<>();
        movieReviewAdapter = new MovieReviewAdapter(this, reviewDataList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean canScrollHorizontally() {
                return false;
            }

            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        recyclerView.setAdapter(movieReviewAdapter);

        databaseReference.child(ReviewPost.REVIEW_TABLE_NAME).child(movieData.getTitle()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    ReviewPost reviewPost = data.getValue(ReviewPost.class);

                    reviewPost.init();

                    if (reviewDataList.size() < INITIAL_NUMBER_OF_REVIEW_POST_SHOWN) {
                        reviewDataList.add(reviewPost);
                    }
                }

                reviewNumberText.setText(String.valueOf(dataSnapshot.getChildrenCount()));

                if (dataSnapshot.getChildrenCount() <= INITIAL_NUMBER_OF_REVIEW_POST_SHOWN) {
                    ((ViewGroup) moreReviewLayout.getParent()).removeView(moreReviewLayout);
                }

                movieReviewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        for (ReviewPost reviewPost : reviewDataList) {
            reviewPost.postFirebaseDatabase(movieData.getTitle());
        }
    }

    private static class DownloadImageTask extends AsyncTask<Void, Void, Bitmap> {

        private MovieData movieData;
        private ImageView posterImage;


        public DownloadImageTask(MovieData movieData, ImageView posterImage) {
            this.movieData = movieData;
            this.posterImage = posterImage;
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            Bitmap bitmap = null;

            try {
                URL posterImageUrl = new URL(movieData.getImage());
                URLConnection connection = posterImageUrl.openConnection();
                InputStream inputStream;
                BufferedInputStream bufferedInputStream;

                connection.setDoInput(true);
                connection.connect();
                inputStream = connection.getInputStream();
                bufferedInputStream = new BufferedInputStream(inputStream);
                bitmap = BitmapFactory.decodeStream(bufferedInputStream);

                bufferedInputStream.close();
                inputStream.close();
            } catch (IOException e) {
                Log.e("TEST", "Error getting bitmap", e);
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            posterImage.setImageBitmap(bitmap);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CustomNavigationViewSetting.PICK_PROFILE_IMAGE) {
            if (resultCode == RESULT_OK) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference();

                storageReference.child(UserAccountPost.PROFILE_IMAGE_ADDRESS).child(CurrentUserInfo.getInstance().getId()).putFile(data.getData());
                customNavigationViewSetting.setProfileImage(data.getData());
            }
        }
    }

}
