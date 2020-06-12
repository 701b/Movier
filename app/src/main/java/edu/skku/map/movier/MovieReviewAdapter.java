package edu.skku.map.movier;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MovieReviewAdapter extends RecyclerView.Adapter<MovieReviewAdapter.ViewHolder> {

    private Context context;
    private List<ReviewPost> reviewPostList;


    public MovieReviewAdapter(Context context, List<ReviewPost> reviewPostList) {
        this.context = context;
        this.reviewPostList = reviewPostList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.review_item, parent, false);
        MovieReviewAdapter.ViewHolder viewHolder = new MovieReviewAdapter.ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final ReviewPost reviewPost = reviewPostList.get(position);
        final CurrentUserInfo currentUserInfo = CurrentUserInfo.getInstance();

        holder.idText.setText(reviewPost.getId());
        holder.contentText.setText(reviewPost.getContent());
        holder.numberOfThumbText.setText(String.valueOf(reviewPost.getNumberOfThumb()));

        switch (reviewPost.getScore()) {
            // break없는 것은 의도적인 것임.
            case 1:
                holder.scoreStarImage2.setVisibility(View.INVISIBLE);

            case 2:
                holder.scoreStarImage3.setVisibility(View.INVISIBLE);

            case 3:
                holder.scoreStarImage4.setVisibility(View.INVISIBLE);

            case 4:
                holder.scoreStarImage5.setVisibility(View.INVISIBLE);
        }

        UserAccountPost.addOnDownloadProfileImage(reviewPost.getId(), new OnDownloadProfileImageListener() {
            @Override
            public void onDownloadProfileImage(Bitmap profileImage) {
                holder.profileImage.setImageBitmap(profileImage);
            }
        });

        if (reviewPost.getIsPressThumb(currentUserInfo.getId())) {
            holder.thumbImage.setImageDrawable(context.getDrawable(R.drawable.ic_thumb_up_red));
            holder.numberOfThumbText.setTextColor(Color.RED);
        }

        holder.thumbImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (reviewPost.getIsPressThumb(currentUserInfo.getId())) {
                    holder.thumbImage.setImageDrawable(context.getDrawable(R.drawable.ic_thumb_up));
                    holder.numberOfThumbText.setTextColor(Color.parseColor("#444444"));
                    reviewPost.removeIdWhoPressThumb(currentUserInfo.getId());
                } else {
                    holder.thumbImage.setImageDrawable(context.getDrawable(R.drawable.ic_thumb_up_red));
                    holder.numberOfThumbText.setTextColor(Color.RED);
                    reviewPost.addIdWhoPressThumb(currentUserInfo.getId());
                }

                holder.numberOfThumbText.setText(String.valueOf(reviewPost.getNumberOfThumb()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return reviewPostList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView idText;
        ImageView profileImage;
        ImageView scoreStarImage1;
        ImageView scoreStarImage2;
        ImageView scoreStarImage3;
        ImageView scoreStarImage4;
        ImageView scoreStarImage5;
        TextView contentText;
        ImageView thumbImage;
        TextView numberOfThumbText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            idText = itemView.findViewById(R.id.review_item_id_text);
            profileImage = itemView.findViewById(R.id.review_item_profile_image);
            scoreStarImage1 = itemView.findViewById(R.id.review_item_star1);
            scoreStarImage2 = itemView.findViewById(R.id.review_item_star2);
            scoreStarImage3 = itemView.findViewById(R.id.review_item_star3);
            scoreStarImage4 = itemView.findViewById(R.id.review_item_star4);
            scoreStarImage5 = itemView.findViewById(R.id.review_item_star5);
            contentText = itemView.findViewById(R.id.review_item_content_text);
            thumbImage = itemView.findViewById(R.id.review_item_thumb_image);
            numberOfThumbText = itemView.findViewById(R.id.review_item_thumb_number_text);

            profileImage.setClipToOutline(true);
        }
    }

}
