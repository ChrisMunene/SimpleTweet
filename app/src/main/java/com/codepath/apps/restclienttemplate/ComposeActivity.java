package com.codepath.apps.restclienttemplate;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import cz.msebera.android.httpclient.Header;

public class ComposeActivity extends AppCompatActivity {

    private EditText tweetInput;
    private Button tweetBtn;
    private TwitterClient client;
    private ProgressDialog pd;

    public final int COMPOSE_REQUEST_CODE = 20;
    public final String USER_NAME = "userName";
    public final String IMAGE_URL = "imageUrl";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        // Get input field and button
        tweetInput = (EditText) findViewById(R.id.tweetInput);
        tweetBtn = (Button) findViewById(R.id.tweetBtn);

        // Init twitter client
        client = TwitterApp.getRestClient(ComposeActivity.this);

        // Handle button click
        tweetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ComposeActivity.this, "Button clicked", Toast.LENGTH_SHORT);
                String message = tweetInput.getText().toString();
                makePost(message);
            }
        });

        pd = new ProgressDialog(ComposeActivity.this);
        pd.setTitle("Loading...");
        pd.setMessage("Please wait.");
        pd.setCancelable(false);
    }

    // Handle submission to twitter API
    public void makePost(String message){
        pd.show();
        client.sendTweet(message, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    // Create new tweet
                    Tweet tweet = Tweet.fromJson(response);
                    Log.d("ComposeActivity", "Tweet Fetched Successfully");

                    //hide modal
                    pd.dismiss();

                    // Create new intent
                    Intent intent =  new Intent();

                    //Serialize the movie using parceler
                    intent.putExtra(Tweet.class.getSimpleName(), Parcels.wrap(tweet));

                    // Set result and return to original activity
                    setResult(RESULT_OK, intent);

                    finish();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("TwitterClient", responseString);
                throwable.printStackTrace();
                pd.dismiss();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("TwitterClient", errorResponse.toString());
                throwable.printStackTrace();
                pd.dismiss();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                Log.d("TwitterClient", errorResponse.toString());
                throwable.printStackTrace();
                pd.dismiss();
            }
        });
    }
}
