package edu.skku.map.movier;

import android.content.Context;
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

    private List<MovieData> movieDataList;
    private OnItemClickMovieItemListener onItemClickMovieItemListener;


    public MovieItemAdapter(List<MovieData> movieDataList, OnItemClickMovieItemListener onItemClickMovieItemListener) {
        this.movieDataList = movieDataList;
        this.onItemClickMovieItemListener = onItemClickMovieItemListener;
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
        DownloadImageTask connectionWithNaverTask = new DownloadImageTask(holder, movieData, onItemClickMovieItemListener);

        connectionWithNaverTask.execute();

        holder.titleText.setText(movieData.getTitle());
        holder.pubDateText.setText(movieData.getPubDate());
    }

    @Override
    public int getItemCount() {
        return movieDataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private LinearLayout movieItem;
        ImageView posterImage;
        TextView titleText;
        TextView scoreText;
        TextView pubDateText;


        private ViewHolder(@NonNull View itemView) {
            super(itemView);

            movieItem = itemView.findViewById(R.id.movie_item);
            posterImage = itemView.findViewById(R.id.movie_item_poster_image);
            titleText = itemView.findViewById(R.id.movie_item_title_text);
            scoreText = itemView.findViewById(R.id.movie_item_score_text);
            pubDateText = itemView.findViewById(R.id.movie_item_pub_date_text);

            posterImage.setClipToOutline(true);
        }
    }

    private static class DownloadImageTask extends AsyncTask<Void, Void, Bitmap> {

        private ViewHolder viewHolder;
        private MovieData movieData;
        private OnItemClickMovieItemListener onItemClickMovieItemListener;


        public DownloadImageTask(ViewHolder viewHolder, MovieData movieData, OnItemClickMovieItemListener onItemClickMovieItemListener) {
            this.viewHolder = viewHolder;
            this.movieData = movieData;
            this.onItemClickMovieItemListener = onItemClickMovieItemListener;
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

            viewHolder.posterImage.setImageBitmap(bitmap);

            viewHolder.movieItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickMovieItemListener.onItemClickMovieItem(movieData);
                }
            });
        }
    }
}
