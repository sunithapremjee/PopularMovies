package com.example.sunitha.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.CursorLoader;
import android.util.Log;

import com.example.sunitha.popularmovies.data.MovieContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class FetchMovieDetailsTask extends CursorLoader {


    private final String LOG_TAG = FetchMovieDetailsTask.class.getSimpleName();

    String mSortOrder = null;

    String mSelection = "";
    String[] mSelectionArgs = null;

    Context mContext;

    public FetchMovieDetailsTask(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        super(context, uri, null, null, null, null);
        mContext = context;

        mSelection = selection;
        mSelectionArgs = selectionArgs;

        setSelection(selection);
        setSelectionArgs(selectionArgs);
        setUri(uri);
        mSortOrder = Utility.getPreferedSortOrder(mContext);
    }


    @Override
    public Cursor loadInBackground() {
        mSortOrder = Utility.getPreferedSortOrder(mContext);

        Log.d(LOG_TAG, "loadInBackground");
        Cursor cIDCount = mContext.getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI,
                new String[]{MovieContract.MovieEntry._ID},
                null,
                null,
                null);
        if( cIDCount.getCount() != 0 )
        {
            Log.d(LOG_TAG, "loadInBackground"+mSelection+mSelectionArgs);
           Cursor c =  mContext.getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI,
                    null,
                   mSelection,
                   mSelectionArgs,
                    null);
            if(( c != null && c.getCount() != 0)|| mSortOrder.equals("Favorites" ))
                return c;
        }

        final String MOVIES_BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
            final String QUERY_PARAM = "sort_by";
            final String API_KEY = "api_key";
            String movieJsonStr = null;

            Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, mSortOrder + ".desc")
                    .appendQueryParameter(API_KEY, "Please add api key")
                    .build();

            movieJsonStr = getJSONStringFromURL(builtUri);
            try {
                insertMovieDataToDBFromJson(movieJsonStr);


            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

        return super.loadInBackground();

    }

    private String getJSONStringFromURL( Uri builtUri  ) {

        Log.d(LOG_TAG, "getJSONStringFromURL");
        String movieJsonStr = null;

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        try {

            Log.e(LOG_TAG, builtUri.toString() );

            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();

            if (urlConnection == null)
                return null;

            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            movieJsonStr = buffer.toString();
            Log.v(LOG_TAG, "moviedata" + movieJsonStr);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return  movieJsonStr;
    }



    private void insertMovieDataToDBFromJson(String movieJsonStr)
            throws JSONException {

        Log.d(LOG_TAG, "insertMovieDataToDBFromJson");

        final String MOVIE_RESULT = "results";
        if(movieJsonStr == null )
        {
            return  ;
        }

        JSONObject movieJson = new JSONObject(movieJsonStr);
        JSONArray movieArrayJson = movieJson.getJSONArray(MOVIE_RESULT);


        ContentValues[] movieValuesArr = new ContentValues[movieArrayJson.length()];

        Log.v(LOG_TAG, "movieDetails" + movieArrayJson.length());
        for (int i = 0; i < movieArrayJson.length(); i++) {

            // Get the JSON object representing the movie
            JSONObject movieDetailsJson = movieArrayJson.getJSONObject(i);

            movieValuesArr[i] = new ContentValues();

            int movieId = movieDetailsJson.getInt("id");

            movieValuesArr[i].put(MovieContract.MovieEntry.COLUMN_NAME, movieDetailsJson.getString("original_title"));
            movieValuesArr[i].put(MovieContract.MovieEntry.COLUMN_SORTORDER, mSortOrder);
            movieValuesArr[i].put(MovieContract.MovieEntry.COLUMN_FAVORITE, "No");
            movieValuesArr[i].put(MovieContract.MovieEntry.COLUMN_OVERVIEW ,movieDetailsJson.getString("overview"));
            movieValuesArr[i].put(MovieContract.MovieEntry.COLUMN_RELEASEDATE, movieDetailsJson.getString("release_date"));
            movieValuesArr[i].put(MovieContract.MovieEntry.COLUMN_USERRATING, movieDetailsJson.getString("vote_average"));
            movieValuesArr[i].put(MovieContract.MovieEntry.COLUMN_POSTER, movieDetailsJson.getString("poster_path"));
            movieValuesArr[i].put(MovieContract.MovieEntry.COLUMN_MOVIEID, movieId);

            final String MOVIES_BASE_URL = "http://api.themoviedb.org/3/movie/" + movieId + "?";
            final String QUERY_PARAM = "append_to_response";
            final String API_KEY = "api_key";
            String movieJsonStringData = null;

            Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                    .appendQueryParameter(API_KEY, "Please add API key")
                    .appendQueryParameter(QUERY_PARAM, "trailers,reviews")
                    .build();

            movieJsonStringData = getJSONStringFromURL( builtUri );
            if( movieJsonStringData == null )
            {
                continue;
            }

            JSONObject movieJsonTrailorAndReview = new JSONObject(movieJsonStringData);

            JSONObject movieJsonTrailor = movieJsonTrailorAndReview.getJSONObject("trailers");
            JSONArray movieArrayJsonYoutubeTrailors = movieJsonTrailor.getJSONArray("youtube");

            JSONObject movieJsonReview = movieJsonTrailorAndReview.getJSONObject("reviews");
            JSONArray movieArrayJsonReview = movieJsonReview.getJSONArray("results");

            String trailors = "";
            for (int nTrailerCount = 0; nTrailerCount < movieArrayJsonYoutubeTrailors.length(); nTrailerCount++) {

                JSONObject trailorJson = movieArrayJsonYoutubeTrailors.getJSONObject(nTrailerCount);
                trailors  += "http://www.youtube.com/watch?v=" + trailorJson.getString("source")+",";

            }

            String reviews = "";

            for (int nReviewCount = 0; nReviewCount < movieArrayJsonReview.length(); nReviewCount++) {

                JSONObject reviewJson = movieArrayJsonReview.getJSONObject(nReviewCount);
                reviews +=  reviewJson.getString("content") +"--ReviewEnd--";

            }

            movieValuesArr[i].put(MovieContract.MovieEntry.COLUMN_TRAILORS, trailors);
            movieValuesArr[i].put(MovieContract.MovieEntry.COLUMN_REVIEWS, reviews);
        }

        mContext.getContentResolver().bulkInsert(MovieContract.MovieEntry.CONTENT_URI,
                movieValuesArr);


        Log.d(LOG_TAG, "insertData End");

    }

}