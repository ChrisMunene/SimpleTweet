package com.codepath.apps.restclienttemplate;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import cz.msebera.android.httpclient.Header;
// ...

public class ComposeDialogFragment extends DialogFragment {

    private EditText tweetInput;
    private Button tweetBtn;
    private TwitterClient client;

    public ComposeDialogFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    // 1. Defines the listener interface with a method passing back data result.
    public interface ComposeDialogListener {
        void onFinishComposeDialog(Parcelable parcel);
    }

    public static ComposeDialogFragment newInstance(String title) {
        ComposeDialogFragment frag = new ComposeDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.compose_tweet_modal, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get input field and button
        tweetInput = view.findViewById(R.id.tweetInput);
        tweetBtn = view.findViewById(R.id.tweetBtn);

        // Handle button click
        tweetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = tweetInput.getText().toString();
                makePost(message);
            }
        });

        // Init twitter client
        client = TwitterApp.getRestClient(getContext());

        // Fetch arguments from bundle and set title
        String title = getArguments().getString("title", "Compose Tweet");
        getDialog().setTitle(title);

        // Show soft keyboard automatically and request focus to field
        tweetInput.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

    }

    // Handle submission to twitter API
    public void makePost(String message){
        client.sendTweet(message, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    // Create new tweet
                    ComposeDialogListener listener = (ComposeDialogListener) getTargetFragment();
                    Tweet tweet = Tweet.fromJson(response);
                    Log.d("ComposeDialogFragment", "Tweet Fetched Successfully");

                    //hide progress modal
                    listener.onFinishComposeDialog(Parcels.wrap(tweet));
                    dismiss();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
        });
    }
}
