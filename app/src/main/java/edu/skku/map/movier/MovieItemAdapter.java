package edu.skku.map.movier;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class MovieItemAdapter extends RecyclerView.Adapter<MovieItemAdapter.ViewHolder> {

    private AppCompatActivity activity;
    private List<MovieData> movieDataList;


    public MovieItemAdapter(AppCompatActivity activity, List<MovieData> movieDataList) {
        this.activity = activity;
        this.movieDataList = movieDataList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.movie_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final MovieData movieData = movieDataList.get(position);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
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

                final Bitmap temp = bitmap;

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        holder.posterImage.setImageBitmap(temp);
                        holder.titleText.setText(movieData.getTitle());
                        holder.pubDateText.setText(movieData.getPubDate());
                    }
                });

                holder.movieItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(activity, MovieDetailActivity.class);

                        intent.putExtra("movie_data", movieData);

                        activity.startActivity(intent);
                        activity.overridePendingTransition(R.anim.slide_from_bottom, R.anim.do_nothing);
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return movieDataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout movieItem;
        ImageView posterImage;
        TextView titleText;
        TextView scoreText;
        TextView pubDateText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            movieItem = itemView.findViewById(R.id.movie_item);
            posterImage = itemView.findViewById(R.id.movie_item_poster_image);
            titleText = itemView.findViewById(R.id.movie_item_title_text);
            scoreText = itemView.findViewById(R.id.movie_item_score_text);
            pubDateText = itemView.findViewById(R.id.movie_item_pub_date_text);

            posterImage.setClipToOutline(true);
        }
    }
}
