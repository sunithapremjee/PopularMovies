package com.example.sunitha.popularmovies;

/**
 * Created by Sunitha on 11/24/2015.
 */

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.sunitha.popularmovies.data.MovieContract;
import com.squareup.picasso.Picasso;

/**
 * A DetailFragment  containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    public static final String DETAILFRAGMENT_TAG = "DFTAG";
    static final String DETAIL_URI = "URI";
    private ShareActionProvider mShareActionProvider;

    private Cursor mDetailCursor;
    ContentValues movieValues = null;

    private ImageView mImageView;
    private TextView movieNameView;
    private TextView overviewText;
    private TextView voteAvarageText;
    private TextView releasedateText;
    private RelativeLayout layout;
    private Button buttonFavorite;

    private TextView trailorListLabel;
    private TextView reviewListViewLabel;

    private ListView trailorListView;
    private ListView reviewListView;

    private Uri mUri = null;
    private int mPosition;

    String mSelection = null;
    String [] mSelectionArgs = null;

    private static final int CURSOR_LOADER_ID = 0;

    static final int COL_MOVIE_ID = 0;
    static final int COL_MOVIE_SORTORDER = 1;
    static final int COL_MOVIE_FAVORITE = 2;
    static final int COL_MOVIE_NAME = 3;
    static final int COL_MOVIE_POSTER = 4;
    static final int COL_MOVIE_OVERVIEW = 5;
    static final int COL_MOVIE_USERRATING = 6;
    static final int COL_MOVIE_RELEASEDATE = 7;
    static final int COL_MOVIE_MOVIEID = 8;
    static final int COL_MOVIE_TRAILORS = 9;
    static final int COL_MOVIE_REVIEWS = 10;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    public static DetailFragment newInstance(int position, Uri uri) {
        Log.d(LOG_TAG, "newInstance");
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        fragment.mPosition = position;
        fragment.mUri = uri;
        args.putInt("id", position);

        args.putParcelable(DETAIL_URI, uri);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(LOG_TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.content_detail, container, false);

        layout = (RelativeLayout)rootView.findViewById(R.id.DetailFragmentLayout);

        mImageView = (ImageView) rootView.findViewById(R.id.list_item_movie_poster);
        movieNameView = (TextView) rootView.findViewById(R.id.list_item_originaltitle_textview);
        overviewText = (TextView) rootView.findViewById(R.id.list_item_overview_textview);
        voteAvarageText = (TextView) rootView.findViewById(R.id.list_item_vote_average_textview);
        releasedateText = (TextView) rootView.findViewById(R.id.list_item_releasedate_textview);
        buttonFavorite = (Button)rootView.findViewById(R.id.ButtonFavorite);

        String sortOrder = Utility.getPreferedSortOrder(getContext());

        if (sortOrder.equals( getString(R.string.pref_sortorder_Favorites)))
        {
            buttonFavorite.setClickable(false);
           // buttonFavorite.setVisibility(View.INVISIBLE);
            buttonFavorite.setText("Favorite Movie");
        }

        trailorListView = (ListView) rootView.findViewById(R.id.trailorListView);
        trailorListLabel = (TextView) rootView.findViewById(R.id.list_item_trailer_label);
        reviewListView = (ListView)rootView.findViewById(R.id.reviewListView);
        reviewListViewLabel =(TextView) rootView.findViewById( R.id.list_item_reviews_textview);

        Bundle args = this.getArguments();
        getLoaderManager().initLoader(CURSOR_LOADER_ID, args, this);
        buttonFavorite.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToFavorites();
            }
        });

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        Log.d(LOG_TAG, "onCreateOptionsMenu");
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detail, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if( mDetailCursor != null )
            mShareActionProvider.setShareIntent(createShareTrailorIntent());

    }

    private Intent createShareTrailorIntent() {
        Log.d(LOG_TAG, "createShareTrailorIntent");
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mDetailCursor.getString(COL_MOVIE_TRAILORS));
        return shareIntent;
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }


    public void addToFavorites()
    {
        Log.d(LOG_TAG, "addToFavorites");

        movieValues.put(MovieContract.MovieEntry.COLUMN_FAVORITE, "Yes");
        getContext().getContentResolver().update(mUri,
                movieValues,
                mSelection,
                mSelectionArgs);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(LOG_TAG, "onCreateLoader");

        if( args != null )
        {
            mPosition = args.getInt("id");
            mUri = args.getParcelable(DETAIL_URI);
        }

        if( mUri == null ) {
            Log.d(LOG_TAG, "onCreateLoader::mUri == null");
            return null;

        }

        mSelection = MovieContract.MovieEntry._ID;
        mSelectionArgs = new String[]{String.valueOf(mPosition)};

        return new CursorLoader(getActivity(),
                mUri,
                null,
                mSelection,
                mSelectionArgs,
                null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(LOG_TAG, "onLoadFinished");

        if( data == null )
            return;
        mDetailCursor = data;
        mDetailCursor.moveToFirst();
        DatabaseUtils.dumpCursor(data);

        movieValues = new ContentValues();

        movieValues.put(MovieContract.MovieEntry.COLUMN_NAME, mDetailCursor.getString(COL_MOVIE_NAME));
        movieValues.put(MovieContract.MovieEntry.COLUMN_SORTORDER, mDetailCursor.getString(COL_MOVIE_SORTORDER));
        movieValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, mDetailCursor.getString(COL_MOVIE_OVERVIEW));
        movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASEDATE, mDetailCursor.getString(COL_MOVIE_RELEASEDATE));
        movieValues.put(MovieContract.MovieEntry.COLUMN_USERRATING, mDetailCursor.getString(COL_MOVIE_USERRATING));
        movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER, mDetailCursor.getString(COL_MOVIE_POSTER));
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIEID, mDetailCursor.getString(COL_MOVIE_ID));

        Picasso.with(getContext()).load("http://image.tmdb.org/t/p/w185/" + mDetailCursor.getString(COL_MOVIE_POSTER)).into(mImageView);

        movieNameView.setText(mDetailCursor.getString(COL_MOVIE_NAME));
        overviewText.setText(mDetailCursor.getString(COL_MOVIE_OVERVIEW));
        voteAvarageText.setText(mDetailCursor.getString(COL_MOVIE_USERRATING));
        releasedateText.setText(mDetailCursor.getString(COL_MOVIE_RELEASEDATE));

        updateTtrailerList();

        updateReviewList();


    }

    void updateTtrailerList() {

        Log.d(LOG_TAG, "updateTtrailerList");
        String trailors = mDetailCursor.getString(COL_MOVIE_TRAILORS);

        if( trailors != null ) {

            trailorListLabel.setText("Trailors");
            movieValues.put(MovieContract.MovieEntry.COLUMN_TRAILORS, mDetailCursor.getString(COL_MOVIE_TRAILORS));

            String[] trailorArr = trailors.split(",");

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                   R.layout.trailor_details, R.id.movie_trailer_textview, trailorArr);

            trailorListView.setAdapter(adapter);
            trailorListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {

                    String itemValue = (String) trailorListView.getItemAtPosition(position);
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(itemValue)));


                }
            });
            if( mShareActionProvider != null )
                mShareActionProvider.setShareIntent(createShareTrailorIntent());
        }
    }

    void updateReviewList() {

        Log.d(LOG_TAG, "updateReviewList");

        String reviews = mDetailCursor.getString(COL_MOVIE_REVIEWS);
        if( reviews != null ) {
            reviewListViewLabel.setText( "Reviews" );

            movieValues.put(MovieContract.MovieEntry.COLUMN_REVIEWS, mDetailCursor.getString(COL_MOVIE_REVIEWS));

            String[] reviewArr = reviews.split("--ReviewEnd--");

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                    R.layout.review_details, R.id.movie_review_textview, reviewArr);

            reviewListView.setAdapter(adapter);
        }

    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mDetailCursor = null;

    }
}