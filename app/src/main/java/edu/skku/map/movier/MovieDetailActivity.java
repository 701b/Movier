package edu.skku.map.movier;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MovieDetailActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        Intent receivedIntent = getIntent();
        MovieData movieData = (MovieData) receivedIntent.getSerializableExtra("movie_data");

        ImageButton backButton = findViewById(R.id.movie_detail_back_button);
        ImageButton toggleDrawerButton = findViewById(R.id.movie_detail_toggle_drawer_button);
        ImageView posterImage = findViewById(R.id.movie_item_poster_image);
        TextView titleText = findViewById(R.id.movie_detail_title_text);
        TextView subtitleText = findViewById(R.id.movie_detail_subtitle_text);
        TextView scoreText = findViewById(R.id.movie_detail_score_text);
        TextView directorText = findViewById(R.id.movie_detail_director_text);
        TextView pubDateText = findViewById(R.id.movie_detail_pub_date_text);
        TextView actorText = findViewById(R.id.movie_detail_actor_text);
        TextView reviewNumberText = findViewById(R.id.movie_detail_review_number_text);
        LinearLayout writeReviewLayout = findViewById(R.id.movie_detail_write_review_layout);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        posterImage.setImageBitmap(movieData.getPosterBitmap());
        titleText.setText(movieData.getTitle());
        subtitleText.setText(movieData.getPubDate());
        directorText.setText(movieData.getDirector());
        pubDateText.setText(movieData.getPubDate());
        actorText.setText(movieData.getActors());

        initRecyclerView();
    }

    private void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.movie_detail_recycler_view);

    }
}
