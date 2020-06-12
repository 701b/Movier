package edu.skku.map.movier;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MovieDetailActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;

    private CustomNavigationViewSetting customNavigationViewSetting;

    private MovieData movieData;

    private MovieReviewAdapter movieReviewAdapter;
    private List<ReviewPost> reviewDataList;

    private boolean isDeleteMoreReviewLayout = false;

    private ImageView starImage1;
    private ImageView starImage2;
    private ImageView starImage3;
    private ImageView starImage4;
    private ImageView starImage5;

    private int reviewScore;

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

        databaseReference = FirebaseDatabase.getInstance().getReference();
        reviewDataList = new ArrayList<>();
        customNavigationViewSetting = new CustomNavigationViewSetting(MovieDetailActivity.this, toggleDrawerButton);

        titleText.setText(movieData.getTitle());
        subtitleText.setText(movieData.getPubDate());
        directorText.setText(movieData.getDirector());
        pubDateText.setText(movieData.getPubDate());
        actorText.setText(movieData.getActors());

        new DownloadImageTask(movieData, posterImage).execute();

        initRelativeLayout();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(android.R.anim.fade_in, R.anim.slide_to_bottom);
            }
        });
    }

    private void initRelativeLayout() {
        final int INITIAL_NUMBER_OF_REVIEW_POST_SHOWN = 3;

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        final RecyclerView recyclerView = findViewById(R.id.movie_detail_recycler_view);
        final RelativeLayout moreReviewLayout = findViewById(R.id.movie_detail_more_review_layout);
        final LinearLayout writeReviewLayout = findViewById(R.id.movie_detail_write_review_layout);
        final TextView reviewNumberText = findViewById(R.id.movie_detail_review_number_text);
        final LinearLayout innerLayout = findViewById(R.id.movie_detail_more_review_inner_layout);
        final ImageView loadingImage = findViewById(R.id.movie_detail_loading_image);

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
                List<ReviewPost> reviewList = new ArrayList<>();
                boolean isAlreadyReviewed = false;

                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    ReviewPost reviewPost = data.getValue(ReviewPost.class);

                    reviewPost.init();
                    reviewList.add(reviewPost);

                    if (reviewPost.getId().equals(CurrentUserInfo.getInstance().getId())) {
                        isAlreadyReviewed = true;
                    }
                }

                reviewList.sort(new Comparator<ReviewPost>() {
                    @Override
                    public int compare(ReviewPost o1, ReviewPost o2) {
                        return o2.getNumberOfThumb() - o1.getNumberOfThumb();
                    }
                });

                for (ReviewPost reviewPost : reviewList) {
                    reviewDataList.add(reviewPost);

                    if (reviewDataList.size() == INITIAL_NUMBER_OF_REVIEW_POST_SHOWN) {
                        break;
                    }
                }

                reviewNumberText.setText(String.valueOf(dataSnapshot.getChildrenCount()));

                // 리뷰 수가 INITIAL_NUMBER_OF_REVIEW_POST_SHOWN보다 작거나 같으면 더보기 버튼 삭제
                if (dataSnapshot.getChildrenCount() <= INITIAL_NUMBER_OF_REVIEW_POST_SHOWN) {
                    ((ViewGroup) moreReviewLayout.getParent()).removeView(moreReviewLayout);
                    ((RelativeLayout.LayoutParams) writeReviewLayout.getLayoutParams()).addRule(RelativeLayout.BELOW, R.id.movie_detail_recycler_view);
                    isDeleteMoreReviewLayout = true;
                }

                // 현재 로그인한 유저가 이미 리뷰를 작성했다면 리뷰 작성 버튼 삭제
                if (isAlreadyReviewed) {
                    ((ViewGroup) writeReviewLayout.getParent()).removeView(writeReviewLayout);
                }

                movieReviewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        moreReviewLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                innerLayout.setVisibility(View.INVISIBLE);
                loadingImage.setVisibility(View.VISIBLE);

                loadingImage.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_loading));

                databaseReference.child(ReviewPost.REVIEW_TABLE_NAME).child(movieData.getTitle()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<ReviewPost> reviewList = new ArrayList<>();
                        int firstIndex = reviewDataList.size();

                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            ReviewPost reviewPost = data.getValue(ReviewPost.class);

                            reviewPost.init();
                            reviewList.add(reviewPost);
                        }

                        reviewList.sort(new Comparator<ReviewPost>() {
                            @Override
                            public int compare(ReviewPost o1, ReviewPost o2) {
                                return o2.getNumberOfThumb() - o1.getNumberOfThumb();
                            }
                        });

                        try {
                            for (int i = 0; i < 5; i++) {
                                reviewDataList.add(reviewList.get(firstIndex + i));
                            }
                        } catch (IndexOutOfBoundsException e) {
                            // 리뷰를 다 읽으면 더보기 버튼 삭제
                            ((ViewGroup) moreReviewLayout.getParent()).removeView(moreReviewLayout);
                            ((RelativeLayout.LayoutParams) writeReviewLayout.getLayoutParams()).addRule(RelativeLayout.BELOW, R.id.movie_detail_recycler_view);
                            isDeleteMoreReviewLayout = true;
                        }

                        movieReviewAdapter.notifyDataSetChanged();

                        innerLayout.setVisibility(View.VISIBLE);
                        loadingImage.setVisibility(View.INVISIBLE);
                        loadingImage.clearAnimation();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
            }
        });

        writeReviewLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final NestedScrollView scrollView = findViewById(R.id.movie_detail_scroll_view);
                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                final ViewGroup viewGroup = (ViewGroup) writeReviewLayout.getParent();
                final View view = inflater.inflate(R.layout.write_review, viewGroup, false);
                LinearLayout writeRevewLayout = view.findViewById(R.id.write_review_write_review_layout);
                final EditText contentInput = view.findViewById(R.id.write_review_content_input);

                starImage1 = view.findViewById(R.id.write_review_star_image1);
                starImage2 = view.findViewById(R.id.write_review_star_image2);
                starImage3 = view.findViewById(R.id.write_review_star_image3);
                starImage4 = view.findViewById(R.id.write_review_star_image4);
                starImage5 = view.findViewById(R.id.write_review_star_image5);

                viewGroup.removeView(writeReviewLayout);
                viewGroup.addView(view);

                if (isDeleteMoreReviewLayout) {
                    ((RelativeLayout.LayoutParams) view.getLayoutParams()).addRule(RelativeLayout.BELOW, R.id.movie_detail_recycler_view);
                } else {
                    ((RelativeLayout.LayoutParams) view.getLayoutParams()).addRule(RelativeLayout.BELOW, R.id.movie_detail_more_review_layout);
                }

                view.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_from_top_for_writing_review));

                recyclerView.bringToFront();

                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {}

                        scrollView.smoothScrollTo(0, 2000);
                    }
                });

                starImage1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        reviewScore = 1;
                        editReviewScore();
                    }
                });

                starImage2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        reviewScore = 2;
                        editReviewScore();
                    }
                });

                starImage3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        reviewScore = 3;
                        editReviewScore();
                    }
                });

                starImage4.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        reviewScore = 4;
                        editReviewScore();
                    }
                });

                starImage5.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        reviewScore = 5;
                        editReviewScore();
                    }
                });

                writeRevewLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!contentInput.getText().equals("")) {
                            ReviewPost reviewPost = new ReviewPost(CurrentUserInfo.getInstance().getId(), reviewScore, contentInput.getText().toString());

                            reviewDataList.add(reviewPost);
                            reviewPost.postFirebaseDatabase(movieData.getTitle());

                            movieReviewAdapter.notifyDataSetChanged();

                            viewGroup.removeView(view);

                            reviewNumberText.setText(String.valueOf(Integer.parseInt(reviewNumberText.getText().toString()) + 1));
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        for (ReviewPost reviewPost : reviewDataList) {
            reviewPost.postFirebaseDatabase(movieData.getTitle());
        }
    }

    private void editReviewScore() {
        starImage1.setImageDrawable(getDrawable(R.drawable.ic_big_empry_star_grey));
        starImage2.setImageDrawable(getDrawable(R.drawable.ic_big_empry_star_grey));
        starImage3.setImageDrawable(getDrawable(R.drawable.ic_big_empry_star_grey));
        starImage4.setImageDrawable(getDrawable(R.drawable.ic_big_empry_star_grey));
        starImage5.setImageDrawable(getDrawable(R.drawable.ic_big_empry_star_grey));

        switch (reviewScore) {
            // break문이 없는 것은 의도적
            case 5:
                starImage5.setImageDrawable(getDrawable(R.drawable.ic_big_star_red));

            case 4:
                starImage4.setImageDrawable(getDrawable(R.drawable.ic_big_star_red));

            case 3:
                starImage3.setImageDrawable(getDrawable(R.drawable.ic_big_star_red));

            case 2:
                starImage2.setImageDrawable(getDrawable(R.drawable.ic_big_star_red));

            case 1:
                starImage1.setImageDrawable(getDrawable(R.drawable.ic_big_star_red));
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
