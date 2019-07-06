package com.codepath.apps.restclienttemplate.models;

import android.text.format.DateUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;


@Parcel
public class Tweet {

    //list out attributes
    public String body;
    public long uid; //Database id
    public User user;
    public String createdAt;
    public Boolean hasMedia;
    public String embeddedImageUrl;
    public int retweetCount;
    public int favoriteCount;


    // Empty constructor for parceler
    public Tweet(){}

    //Deserialize the json
    public static  Tweet fromJson(JSONObject jsonObject) throws JSONException {
        Tweet tweet = new Tweet();

        //extract values
        tweet.body = jsonObject.getString("text");
        tweet.uid = jsonObject.getLong("id");
        tweet.createdAt = getRelativeTimeAgo(jsonObject.getString("created_at"));
        tweet.user = User.fromJson(jsonObject.getJSONObject("user"));
        tweet.embeddedImageUrl = getEmbeddedImage(jsonObject.getJSONObject("entities"));
        tweet.retweetCount = jsonObject.getInt("retweet_count");
        tweet.favoriteCount = jsonObject.getInt("favorite_count");

        return tweet;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return super.equals(obj);
    }

    public static String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";
        try {
            long dateMillis = sf.parse(rawJsonDate).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE).toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return relativeDate;
    }

    public static String getEmbeddedImage(JSONObject entities){
        String imageUrl;
        try {
            JSONArray media = entities.getJSONArray("media");
            if(media.length() > 0){
                JSONObject mediaItem = media.getJSONObject(0);
                imageUrl = mediaItem.getString("media_url_https");
                Log.d("Tweet", String.format("%s", imageUrl));
            } else {
                imageUrl = null;
            }
        } catch (JSONException e) {
            imageUrl = null;
        }

        return imageUrl;
    }
}
