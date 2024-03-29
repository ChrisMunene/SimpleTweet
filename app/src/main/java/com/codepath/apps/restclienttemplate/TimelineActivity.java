package com.codepath.apps.restclienttemplate;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class TimelineActivity extends AppCompatActivity implements ComposeDialogFragment.ComposeDialogListener {

    private TwitterClient client;
    private TweetAdapter tweetAdapter;
    private ArrayList<Tweet> tweets;
    private RecyclerView rvTweets;
    private SwipeRefreshLayout swipeContainer;
    private MenuItem miActionProgressItem;
    private EndlessRecyclerViewScrollListener scrollListener;
    private Boolean isRefreshing = false;
    private Long lowestTweetId = null;
    private FloatingActionButton fabCompose;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);



        // Find the toolbar view inside the activity layout
        Toolbar toolbar = findViewById(R.id.toolbar);

        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);

        client = TwitterApp.getRestClient(TimelineActivity.this);

        // Find swipe container
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);

        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                isRefreshing = true;
                populateTimeline();
            }
        });

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        //Init arraylist of tweets
        tweets = new ArrayList<>();

        //construct the adapter from the tweets
        tweetAdapter = new TweetAdapter(tweets, this);


        //Find recyclerView
        rvTweets = findViewById(R.id.rvTweet);

        //Setup layout manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(TimelineActivity.this);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvTweets.getContext(),
                linearLayoutManager.getOrientation());
        rvTweets.addItemDecoration(dividerItemDecoration);
        rvTweets.setLayoutManager(linearLayoutManager);

        // Retain an instance so that you can call `resetState()` for fresh searches
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                Log.d("TimelineActivity", "Loading more....");
                populateTimeline();
            }
        };

        // Adds the scroll listener to RecyclerView
        rvTweets.addOnScrollListener(scrollListener);

        // Connect RecyclerView to adapter
        rvTweets.setAdapter(tweetAdapter);

        fabCompose = findViewById(R.id.fabCompose);
        fabCompose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showComposeDialog();
            }
        });


    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_timeline, menu);
        return true;
    }

    // Handles action bar item clicks.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Get clicked item
        int id = item.getItemId();

        switch(id) {
            case R.id.action_settings:
                return true;
            case R.id.composeBtn:
                showComposeDialog();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Store instance of the menu item containing progress
        miActionProgressItem = menu.findItem(R.id.miActionProgress);
        // Extract the action-view from the menu item
        ProgressBar v =  (ProgressBar) MenuItemCompat.getActionView(miActionProgressItem);

        //Populate timeline after toolbar is fully configured
        populateTimeline();

        // Return to finish
        return super.onPrepareOptionsMenu(menu);
    }

    // Populates the user's timeline
    private void populateTimeline() {

        // If not refreshing and progressbar loaded, show progress bar.
        // Done to avoid showing two progressbars when refreshing.
        if(miActionProgressItem != null && !isRefreshing) showProgressBar();

        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("TwitterClient", response.toString());
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {

                // Convert fetched data to tweets and add to our data model
                for(int i = 0; i < response.length(); i++){
                    try {

                        Tweet tweet = Tweet.fromJson(response.getJSONObject(i));

                        // Set the lowest tweet id we have encountered
                        if(lowestTweetId == null ||tweet.uid < lowestTweetId) {
                            lowestTweetId = tweet.uid;
                        };
                        tweets.add(tweet);
                        tweetAdapter.notifyItemInserted(tweets.size() - 1);

                    } catch (JSONException e) {
                        Log.e("TwitterClient", e.getMessage());
                        e.printStackTrace();
                    }

                }

                Log.d("TimelineActivity",String.format("Max_id: %s", lowestTweetId));

                // Set refresh to false
                swipeContainer.setRefreshing(false);
                isRefreshing = false;

                hideProgressBar();

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("TwitterClient", responseString);
                throwable.printStackTrace();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("TwitterClient", errorResponse.toString());
                throwable.printStackTrace();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                Log.d("TwitterClient", errorResponse.toString());
                throwable.printStackTrace();
            }
        }, (isRefreshing) ? null : lowestTweetId);
    }

    public void showComposeDialog() {
        FragmentManager fm = getSupportFragmentManager();
        ComposeDialogFragment composeDialogFragment= ComposeDialogFragment.newInstance("Some Title");
        composeDialogFragment.show(fm, "compose_tweet_modal");
    }


    @Override
    public void onFinishComposeDialog(Parcelable parcel) {
        //Unwrap the tweet
        Tweet tweet = Parcels.unwrap(parcel);
        // Add the tweet to the top of the list
        tweets.add(0, tweet);
        tweetAdapter.notifyDataSetChanged();

        //Scroll to the most recent tweet
        rvTweets.scrollToPosition(0);

        //Notify user that operation was successful
        Toast.makeText(this, "Tweet posted successfully", Toast.LENGTH_SHORT).show();
    }

    public void showProgressBar() {
        // Show progress item
        miActionProgressItem.setVisible(true);
    }

    public void hideProgressBar() {
        // Hide progress item
        miActionProgressItem.setVisible(false);
    }
}
