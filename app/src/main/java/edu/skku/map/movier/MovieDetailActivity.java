package edu.skku.map.movier;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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

    private static final int INITIAL_NUMBER_OF_REVIEW_POST_SHOWN = 3;
    private static final int NUMBER_OF_MORE_REVIEW_POST = 5;

    private CustomNavigationViewSetting customNavigationViewSetting;

    private MovieData movieData;

    private MovieReviewAdapter movieReviewAdapter;
    private List<ReviewPost> reviewDataList;

    private boolean isDeleteMoreReviewLayout = false;
    private boolean isOpenWritingReviewLayout = false;

    private ImageView starImage1;
    private ImageView starImage2;
    private ImageView starImage3;
    private ImageView starImage4;
    private ImageView starImage5;

    private LinearLayout openedWritingReviewLayout;

    private int reviewScore = 0;
    private float averageScore = 0.0f;
    private int[] numberOfManScore = new int[5];
    private int[] numberOfWomanScore = new int[5];

    private int numberOfLoadedImage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        Intent receivedIntent = getIntent();
        movieData = (MovieData) receivedIntent.getSerializableExtra("movie_data");

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        final RecyclerView recyclerView = findViewById(R.id.movie_detail_recycler_view);
        final RelativeLayout moreReviewLayout = findViewById(R.id.movie_detail_more_review_layout);
        final LinearLayout writeReviewLayout = findViewById(R.id.movie_detail_write_review_layout);
        final TextView reviewNumberText = findViewById(R.id.movie_detail_review_number_text);
        final LinearLayout innerLayout = findViewById(R.id.movie_detail_more_review_inner_layout);
        final ProgressBar moreReviewProgressBar = findViewById(R.id.movie_detail_more_review_loading_image);
        final TextView scoreText = findViewById(R.id.movie_detail_score_text);
        final ProgressBar mainProgressBar = findViewById(R.id.movie_detail_main_loading_image);
        final LinearLayout contentLayout = findViewById(R.id.movie_detail_content_layout);
        final NestedScrollView scrollView = findViewById(R.id.movie_detail_scroll_view);
        final LinearLayout reviewTitleLayout = findViewById(R.id.movie_detail_review_title_layout);

        ImageButton backButton = findViewById(R.id.movie_detail_back_button);
        ImageButton toggleDrawerButton = findViewById(R.id.movie_detail_toggle_drawer_button);
        ImageView posterImage = findViewById(R.id.movie_detail_poster_image);
        TextView titleText = findViewById(R.id.movie_detail_title_text);
        TextView subtitleText = findViewById(R.id.movie_detail_subtitle_text);
        TextView directorText = findViewById(R.id.movie_detail_director_text);
        TextView pubDateText = findViewById(R.id.movie_detail_pub_date_text);
        TextView actorText = findViewById(R.id.movie_detail_actor_text);

        contentLayout.setVisibility(View.INVISIBLE);

        // progressbar 색상 변경
        mainProgressBar.setIndeterminate(true);
        moreReviewProgressBar.setIndeterminate(true);
        mainProgressBar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#FF0000"), PorterDuff.Mode.MULTIPLY);
        moreReviewProgressBar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#FF0000"), PorterDuff.Mode.MULTIPLY);

        reviewDataList = new ArrayList<>();
        customNavigationViewSetting = new CustomNavigationViewSetting(MovieDetailActivity.this, toggleDrawerButton);

        titleText.setText(movieData.getTitle());
        subtitleText.setText(movieData.getPubDate());
        directorText.setText(movieData.getDirector());
        pubDateText.setText(movieData.getPubDate());
        actorText.setText(movieData.getActors());

        new DownloadImageTask(movieData, posterImage).execute();

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
                final List<ReviewPost> reviewList = new ArrayList<>();
                ReviewPost myReviewPost = null;
                int sumOfScore = 0;

                numberOfLoadedImage = 0;

                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    ReviewPost reviewPost = data.getValue(ReviewPost.class);

                    reviewPost.init();
                    reviewList.add(reviewPost);

                    if (reviewPost.getId().equals(CurrentUserInfo.getInstance().getId())) {
                        myReviewPost = reviewPost;
                    }

                    sumOfScore += reviewPost.getScore();

                    if (reviewPost.getIsMan()) {
                        switch (reviewPost.getScore()) {
                            case 1:
                                numberOfManScore[0]++;
                                break;

                            case 2:
                                numberOfManScore[1]++;
                                break;

                            case 3:
                                numberOfManScore[2]++;
                                break;

                            case 4:
                                numberOfManScore[3]++;
                                break;

                            case 5:
                                numberOfManScore[4]++;
                                break;
                        }
                    } else {
                        switch (reviewPost.getScore()) {
                            case 1:
                                numberOfWomanScore[0]++;
                                break;

                            case 2:
                                numberOfWomanScore[1]++;
                                break;

                            case 3:
                                numberOfWomanScore[2]++;
                                break;

                            case 4:
                                numberOfWomanScore[3]++;
                                break;

                            case 5:
                                numberOfWomanScore[4]++;
                                break;
                        }
                    }
                }

                renewScoreGraph();

                reviewList.sort(new Comparator<ReviewPost>() {
                    @Override
                    public int compare(ReviewPost o1, ReviewPost o2) {
                        return o2.getNumberOfThumb() - o1.getNumberOfThumb();
                    }
                });

                if (myReviewPost != null) {
                    // 현재 로그인한 계정으로 작성한 리뷰는 가장 위에 표시
                    reviewList.remove(myReviewPost);
                    reviewList.add(0, myReviewPost);
                }

                for (final ReviewPost reviewPost : reviewList) {
                    reviewDataList.add(reviewPost);

                    UserAccountPost.addOnDownloadProfileImage(reviewPost.getId(), new OnDownloadProfileImageListener() {
                        @Override
                        public void onDownloadProfileImage(Bitmap profileImage) {
                            int maxSize;

                            if (reviewList.size() > INITIAL_NUMBER_OF_REVIEW_POST_SHOWN) {
                                maxSize = INITIAL_NUMBER_OF_REVIEW_POST_SHOWN;
                            } else {
                                maxSize = reviewList.size();
                            }

                            reviewPost.setProfileImage(profileImage);
                            numberOfLoadedImage++;

                            if (numberOfLoadedImage == maxSize) {
                                contentLayout.setVisibility(View.VISIBLE);
                                movieReviewAdapter.notifyDataSetChanged();
                                contentLayout.startAnimation(AnimationUtils.loadAnimation(MovieDetailActivity.this, android.R.anim.fade_in));
                                mainProgressBar.setVisibility(View.INVISIBLE);
                                scrollView.scrollTo(0, 0);
                            }
                        }
                    }, new OnFailToDownloadProfileImageListener() {
                        @Override
                        public void onFailToDownloadProfileImage(Exception e) {
                            int maxSize;

                            if (reviewList.size() > INITIAL_NUMBER_OF_REVIEW_POST_SHOWN) {
                                maxSize = INITIAL_NUMBER_OF_REVIEW_POST_SHOWN;
                            } else {
                                maxSize = reviewList.size();
                            }

                            numberOfLoadedImage++;

                            if (numberOfLoadedImage == maxSize) {
                                contentLayout.setVisibility(View.VISIBLE);
                                movieReviewAdapter.notifyDataSetChanged();
                                contentLayout.startAnimation(AnimationUtils.loadAnimation(MovieDetailActivity.this, android.R.anim.fade_in));
                                mainProgressBar.setVisibility(View.INVISIBLE);
                                scrollView.scrollTo(0, 0);
                            }
                        }
                    });

                    if (reviewDataList.size() == INITIAL_NUMBER_OF_REVIEW_POST_SHOWN) {
                        break;
                    }
                }

                if (reviewList.size() == 0) {
                    // 검색된 리뷰가 없을 때
                    contentLayout.setVisibility(View.VISIBLE);
                    movieReviewAdapter.notifyDataSetChanged();
                    contentLayout.startAnimation(AnimationUtils.loadAnimation(MovieDetailActivity.this, android.R.anim.fade_in));
                    mainProgressBar.setVisibility(View.INVISIBLE);
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1);
                            } catch (InterruptedException e) {}

                            scrollView.scrollTo(0, 0);
                        }
                    });
                }

                reviewNumberText.setText(String.valueOf(dataSnapshot.getChildrenCount()));

                if (dataSnapshot.getChildrenCount() != 0) {
                    averageScore = ((float) sumOfScore) / dataSnapshot.getChildrenCount();
                    scoreText.setText(String.format("%.1f", averageScore));
                } else {
                    scoreText.setText("0.0");
                }

                // 리뷰 수가 INITIAL_NUMBER_OF_REVIEW_POST_SHOWN보다 작거나 같으면 더보기 버튼 삭제
                if (dataSnapshot.getChildrenCount() <= INITIAL_NUMBER_OF_REVIEW_POST_SHOWN) {
                    ((ViewGroup) moreReviewLayout.getParent()).removeView(moreReviewLayout);
                    ((RelativeLayout.LayoutParams) writeReviewLayout.getLayoutParams()).addRule(RelativeLayout.BELOW, R.id.movie_detail_recycler_view);
                    isDeleteMoreReviewLayout = true;
                }

                // 현재 로그인한 유저가 이미 리뷰를 작성했다면 리뷰 작성 버튼 삭제
                if (myReviewPost != null) {
                    ((ViewGroup) writeReviewLayout.getParent()).removeView(writeReviewLayout);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        moreReviewLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                innerLayout.setVisibility(View.INVISIBLE);
                moreReviewProgressBar.setVisibility(View.VISIBLE);

                databaseReference.child(ReviewPost.REVIEW_TABLE_NAME).child(movieData.getTitle()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final List<ReviewPost> reviewList = new ArrayList<>();
                        ReviewPost myReviewPost = null;
                        final int firstIndex = reviewDataList.size();
                        int lastIndex = firstIndex;

                        numberOfLoadedImage = 0;

                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            ReviewPost reviewPost = data.getValue(ReviewPost.class);

                            reviewPost.init();
                            reviewList.add(reviewPost);

                            if (reviewPost.getId().equals(CurrentUserInfo.getInstance().getId())) {
                                myReviewPost = reviewPost;
                            }
                        }

                        if (lastIndex + NUMBER_OF_MORE_REVIEW_POST - 1 >= reviewList.size()) {
                            lastIndex = reviewList.size() - 1;
                        } else {
                            lastIndex += NUMBER_OF_MORE_REVIEW_POST - 1;
                        }

                        reviewList.sort(new Comparator<ReviewPost>() {
                            @Override
                            public int compare(ReviewPost o1, ReviewPost o2) {
                                return o2.getNumberOfThumb() - o1.getNumberOfThumb();
                            }
                        });

                        if (myReviewPost != null) {
                            // 현재 로그인한 계정으로 작성한 리뷰는 가장 위에 표시
                            reviewList.remove(myReviewPost);
                            reviewList.add(0, myReviewPost);
                        }

                        for (int i = firstIndex; i <= lastIndex; i++) {
                            final ReviewPost reviewPostToAdd = reviewList.get(i);
                            final int finalLastIndex = lastIndex;

                            reviewDataList.add(reviewPostToAdd);

                            UserAccountPost.addOnDownloadProfileImage(reviewPostToAdd.getId(), new OnDownloadProfileImageListener() {
                                @Override
                                public void onDownloadProfileImage(Bitmap profileImage) {
                                    reviewPostToAdd.setProfileImage(profileImage);
                                    numberOfLoadedImage++;

                                    if (numberOfLoadedImage == finalLastIndex - firstIndex + 1) {
                                        movieReviewAdapter.notifyDataSetChanged();
                                        innerLayout.setVisibility(View.VISIBLE);
                                        moreReviewProgressBar.setVisibility(View.INVISIBLE);

                                        if (finalLastIndex == reviewList.size() - 1) {
                                            // 더 이상의 리뷰가 없을 때 더보기 버튼 삭제
                                            if (isOpenWritingReviewLayout) {
                                                ((RelativeLayout.LayoutParams) openedWritingReviewLayout.getLayoutParams()).addRule(RelativeLayout.BELOW, R.id.movie_detail_recycler_view);
                                            }

                                            ((ViewGroup) moreReviewLayout.getParent()).removeView(moreReviewLayout);
                                            ((RelativeLayout.LayoutParams) writeReviewLayout.getLayoutParams()).addRule(RelativeLayout.BELOW, R.id.movie_detail_recycler_view);
                                            isDeleteMoreReviewLayout = true;
                                        }

                                        AsyncTask.execute(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    Thread.sleep(50);
                                                } catch (InterruptedException e) {}

                                                scrollView.smoothScrollTo(0, 999999);
                                            }
                                        });
                                    }
                                }
                            }, new OnFailToDownloadProfileImageListener() {
                                @Override
                                public void onFailToDownloadProfileImage(Exception e) {
                                    numberOfLoadedImage++;

                                    if (numberOfLoadedImage == finalLastIndex - firstIndex + 1) {
                                        movieReviewAdapter.notifyDataSetChanged();
                                        innerLayout.setVisibility(View.VISIBLE);
                                        moreReviewProgressBar.setVisibility(View.INVISIBLE);

                                        if (finalLastIndex == reviewList.size() - 1) {
                                            // 더 이상의 리뷰가 없을 때 더보기 버튼 삭제
                                            if (isOpenWritingReviewLayout) {
                                                ((RelativeLayout.LayoutParams) openedWritingReviewLayout.getLayoutParams()).addRule(RelativeLayout.BELOW, R.id.movie_detail_recycler_view);
                                            }

                                            ((ViewGroup) moreReviewLayout.getParent()).removeView(moreReviewLayout);
                                            ((RelativeLayout.LayoutParams) writeReviewLayout.getLayoutParams()).addRule(RelativeLayout.BELOW, R.id.movie_detail_recycler_view);
                                            isDeleteMoreReviewLayout = true;
                                        }

                                        AsyncTask.execute(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    Thread.sleep(50);
                                                } catch (InterruptedException e) {}

                                                scrollView.smoothScrollTo(0, 999999);
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
            }
        });

        writeReviewLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                final ViewGroup viewGroup = (ViewGroup) writeReviewLayout.getParent();

                openedWritingReviewLayout = (LinearLayout) inflater.inflate(R.layout.write_review, viewGroup, false);

                LinearLayout postWritingReviewLayout = openedWritingReviewLayout.findViewById(R.id.write_review_write_review_layout);
                final EditText contentInput = openedWritingReviewLayout.findViewById(R.id.write_review_content_input);

                isOpenWritingReviewLayout = true;

                starImage1 = openedWritingReviewLayout.findViewById(R.id.write_review_star_image1);
                starImage2 = openedWritingReviewLayout.findViewById(R.id.write_review_star_image2);
                starImage3 = openedWritingReviewLayout.findViewById(R.id.write_review_star_image3);
                starImage4 = openedWritingReviewLayout.findViewById(R.id.write_review_star_image4);
                starImage5 = openedWritingReviewLayout.findViewById(R.id.write_review_star_image5);

                viewGroup.removeView(writeReviewLayout);
                viewGroup.addView(openedWritingReviewLayout);

                if (isDeleteMoreReviewLayout) {
                    ((RelativeLayout.LayoutParams) openedWritingReviewLayout.getLayoutParams()).addRule(RelativeLayout.BELOW, R.id.movie_detail_recycler_view);
                } else {
                    ((RelativeLayout.LayoutParams) openedWritingReviewLayout.getLayoutParams()).addRule(RelativeLayout.BELOW, R.id.movie_detail_more_review_layout);
                }

                openedWritingReviewLayout.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_from_top_for_writing_review));

                recyclerView.bringToFront();
                moreReviewLayout.bringToFront();

                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {}

                        scrollView.smoothScrollTo(0, 99999999);
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

                postWritingReviewLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (reviewScore != 0) {
                            if (!contentInput.getText().toString().equals("")) {
                                ReviewPost reviewPost = new ReviewPost(CurrentUserInfo.getInstance().getId(), CurrentUserInfo.getInstance().isMan(), reviewScore, contentInput.getText().toString(), CurrentUserInfo.getInstance().getProfileImage());
                                float sumOfScore;
                                int reviewCount;

                                reviewDataList.add(0, reviewPost);
                                reviewPost.postFirebaseDatabase(movieData.getTitle());

                                movieReviewAdapter.notifyDataSetChanged();

                                viewGroup.removeView(openedWritingReviewLayout);

                                reviewCount = Integer.parseInt(reviewNumberText.getText().toString()) + 1;
                                sumOfScore = (reviewCount - 1) * averageScore + reviewPost.getScore();
                                reviewNumberText.setText(String.valueOf(reviewCount));
                                scoreText.setText(String.format("%.1f", sumOfScore / reviewCount));

                                if (reviewPost.getIsMan()) {
                                    switch (reviewPost.getScore()) {
                                        case 1:
                                            numberOfManScore[0]++;
                                            break;

                                        case 2:
                                            numberOfManScore[1]++;
                                            break;

                                        case 3:
                                            numberOfManScore[2]++;
                                            break;

                                        case 4:
                                            numberOfManScore[3]++;
                                            break;

                                        case 5:
                                            numberOfManScore[4]++;
                                            break;
                                    }
                                } else {
                                    switch (reviewPost.getScore()) {
                                        case 1:
                                            numberOfWomanScore[0]++;
                                            break;

                                        case 2:
                                            numberOfWomanScore[1]++;
                                            break;

                                        case 3:
                                            numberOfWomanScore[2]++;
                                            break;

                                        case 4:
                                            numberOfWomanScore[3]++;
                                            break;

                                        case 5:
                                            numberOfWomanScore[4]++;
                                            break;
                                    }
                                }

                                renewScoreGraph();

                                scrollView.smoothScrollTo(0, reviewTitleLayout.getTop());
                            } else {
                                new AlertDialog.Builder(MovieDetailActivity.this)
                                        .setMessage("리뷰 내용을 작성하세요")
                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        }).show();
                            }
                        } else {
                            new AlertDialog.Builder(MovieDetailActivity.this)
                                    .setMessage("별을 눌러 평점을 매겨주세요")
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    }).show();
                        }
                    }
                });
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

    @Override
    protected void onPause() {
        super.onPause();

        for (ReviewPost reviewPost : reviewDataList) {
            reviewPost.postFirebaseDatabase(movieData.getTitle());
        }
    }

    private void renewScoreGraph() {
        final LinearLayout[] manScoreGraphLayout = new LinearLayout[5];
        final LinearLayout[] womanScoreGraphLayout = new LinearLayout[5];

        manScoreGraphLayout[0] = findViewById(R.id.movie_detail_man_score_graph1);
        manScoreGraphLayout[1] = findViewById(R.id.movie_detail_man_score_graph2);
        manScoreGraphLayout[2] = findViewById(R.id.movie_detail_man_score_graph3);
        manScoreGraphLayout[3] = findViewById(R.id.movie_detail_man_score_graph4);
        manScoreGraphLayout[4] = findViewById(R.id.movie_detail_man_score_graph5);
        womanScoreGraphLayout[0] = findViewById(R.id.movie_detail_woman_score_graph1);
        womanScoreGraphLayout[1] = findViewById(R.id.movie_detail_woman_score_graph2);
        womanScoreGraphLayout[2] = findViewById(R.id.movie_detail_woman_score_graph3);
        womanScoreGraphLayout[3] = findViewById(R.id.movie_detail_woman_score_graph4);
        womanScoreGraphLayout[4] = findViewById(R.id.movie_detail_woman_score_graph5);

        final float maxHeight = getResources().getDisplayMetrics().density * 100;
        int maxNumber = 0;

        for (int number : numberOfManScore) {
            if (number > maxNumber) {
                maxNumber = number;
            }
        }

        for (int number : numberOfWomanScore) {
            if (number > maxNumber) {
                maxNumber = number;
            }
        }

        if (maxNumber != 0) {
            for (int i = 0; i < 5; i++) {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) manScoreGraphLayout[i].getLayoutParams();

                layoutParams.height = (int) maxHeight * numberOfManScore[i] / maxNumber;
                manScoreGraphLayout[i].setLayoutParams(layoutParams);
            }

            for (int i = 0; i < 5; i++) {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) womanScoreGraphLayout[i].getLayoutParams();

                layoutParams.height = (int) maxHeight * numberOfWomanScore[i] / maxNumber;
                womanScoreGraphLayout[i].setLayoutParams(layoutParams);
            }
        } else {
            for (int i = 0; i < 5; i++) {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) manScoreGraphLayout[i].getLayoutParams();

                layoutParams.height = 0;
                manScoreGraphLayout[i].setLayoutParams(layoutParams);
            }

            for (int i = 0; i < 5; i++) {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) womanScoreGraphLayout[i].getLayoutParams();

                layoutParams.height = 0;
                womanScoreGraphLayout[i].setLayoutParams(layoutParams);
            }
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

                try {
                    CurrentUserInfo.getInstance().setProfileImage(MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData()));
                } catch (IOException e) {
                    Log.e("TEST", "load bitmap from uri ERROR", e);
                }
            }
        }
    }

}
