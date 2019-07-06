package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.models.Tweet;

import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class TweetAdapter extends RecyclerView.Adapter<TweetAdapter.ViewHolder> {

    private List<Tweet> mTweets;
    private Context context;
    TimelineActivity activity;
    private final int hasImage = 1, hasNoImage = 0;

    //pass in the Tweets array in constructor
    public TweetAdapter(List<Tweet> tweets, TimelineActivity activity){
        mTweets = tweets;
        this.activity = activity;
    }

    //For each row, inflate the layout and cache references into viewholder
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        ViewHolder viewHolder;
        context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        switch (viewType) {
            case hasImage:
                View tweetViewWithImage = inflater.inflate(R.layout.item_tweet_embed_img, viewGroup, false);
                viewHolder = new ViewHolder(tweetViewWithImage);
                break;
            case hasNoImage:
                View tweetViewNoImage = inflater.inflate(R.layout.item_tweet, viewGroup, false);
                viewHolder = new ViewHolder(tweetViewNoImage);
                break;
            default:
                View defaultView = inflater.inflate(R.layout.item_tweet, viewGroup, false);
                viewHolder = new ViewHolder(defaultView);
                break;
        }

        return viewHolder;
    }

    //bind values based on the position of the element
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {

        //get the data according to position
        Tweet tweet = mTweets.get(position);
        //populate the views according to this data
        viewHolder.tvUsername.setText(tweet.user.name);
        viewHolder.tvBody.setText(tweet.body);
        viewHolder.tvScreenName.setText(tweet.user.screenName);
        viewHolder.tvTimestamp.setText(tweet.createdAt);
        viewHolder.tvRetweetCount.setText(tweet.retweetCount + "");
        viewHolder.tvFavCount.setText(tweet.favoriteCount + "");

        viewHolder.ibReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.showComposeDialog();
            }
        });

        Glide.with(context).load(tweet.user.profileImageUrl).bitmapTransform(new RoundedCornersTransformation(context, 25, 0)).into(viewHolder.ivProfileImage);

        switch (viewHolder.getItemViewType()) {
            case hasImage:
                Glide.with(context).load(tweet.embeddedImageUrl).bitmapTransform(new RoundedCornersTransformation(context, 25, 0)).into(viewHolder.ivEmbedImage);
                break;
            default:
                break;
        }

    }

    @Override
    public int getItemCount() {
        return mTweets.size();
    }

    @Override
    public int getItemViewType(int position) {
        //More to come
        if (mTweets.get(position).embeddedImageUrl != null) {
            return hasImage;
        } else if (mTweets.get(position).embeddedImageUrl == null ) {
            return hasNoImage;
        }
        return -1;
    }

    //create viewholder class
    public static class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView ivProfileImage;
        public TextView tvBody;
        public TextView tvUsername;
        public TextView tvTimestamp;
        public ImageView ivEmbedImage;
        public TextView tvScreenName;
        public TextView tvFavCount;
        public TextView tvRetweetCount;
        public ImageView ibReply;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            ivEmbedImage = itemView.findViewById(R.id.ivEmbedImage);
            tvFavCount = itemView.findViewById(R.id.tvFavCount);
            tvRetweetCount = itemView.findViewById(R.id.tvRetweetCt);
            ibReply = itemView.findViewById(R.id.ibReply);
        }
    }


    // Clean all elements of the recycler
    public void clear() {
        mTweets.clear();
        notifyDataSetChanged();
    }

}
