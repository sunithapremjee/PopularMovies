package com.example.sunitha.popularmovies;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.example.sunitha.popularmovies.data.MovieContract;

public class MainactivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String LOG_TAG = MainactivityFragment.class.getSimpleName();

    private AndroidMovieAdapter movieAdapter = null;
    GridView mGridView = null;
    String mSortOrder = null;

    int mPosition = 0;
    boolean mbTwoPane = false;

    String mSelection = "";
    String[] mSelectionArgs = null;

    Cursor mCurrentCursor = null;

    private static final int CURSOR_LOADER_ID = 0;

    private static final String[] MOVIE_COLUMNS = {

    MovieContract.MovieEntry.TABLE_MOVIES + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_SORTORDER,
            MovieContract.MovieEntry.COLUMN_FAVORITE,
            MovieContract.MovieEntry.COLUMN_NAME,
    MovieContract.MovieEntry.COLUMN_POSTER,
    MovieContract.MovieEntry.COLUMN_OVERVIEW,
    MovieContract.MovieEntry.COLUMN_USERRATING,
    MovieContract.MovieEntry.COLUMN_RELEASEDATE,
    MovieContract.MovieEntry.COLUMN_MOVIEID,
    MovieContract.MovieEntry.COLUMN_TRAILORS,
    MovieContract.MovieEntry.COLUMN_REVIEWS
    };


    public MainactivityFragment() {

    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        Log.d(LOG_TAG, "onActivityCreated");

        if (!Utility.isOnline(getContext()) )
        {
            showToastMessage();

        }
        if (getActivity().findViewById(R.id.container) != null)
            mbTwoPane = true;

        mSortOrder = Utility.getPreferedSortOrder(getContext());
        setSelectionArguments();
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);

        super.onActivityCreated(savedInstanceState);
    }
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

    }

    public boolean onOptionsItemSelected(MenuItem item) {

        Log.d(LOG_TAG, "onOptionsItemSelected");
         int id = item.getItemId();

        if (id == R.id.action_settings ) {

            Intent settingsIntent = new Intent(getContext(), SettingsActivity.class);
            startActivity( settingsIntent );
            return true;
        }

        return onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(LOG_TAG, "onCreateView");

        View rootView = inflater.inflate(R.layout.fragment_mainactivity, container, false);

        movieAdapter = new AndroidMovieAdapter(getActivity(), null, 0,CURSOR_LOADER_ID) ;
        mGridView = (GridView) rootView.findViewById(R.id.gridview);
        mGridView.setAdapter(movieAdapter);


        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                updateDetailFragment( position );
            }
        });
        return rootView;
    }

    private void updateDetailFragment( int position )
    {
        if( mCurrentCursor != null ) {

            mPosition = position;
            mCurrentCursor.moveToFirst();
            mCurrentCursor.move(position);
            int uriId = mCurrentCursor.getInt(mCurrentCursor.getColumnIndex(MovieContract.MovieEntry._ID));

            // append Id to uri
            Uri uri = ContentUris.withAppendedId(MovieContract.MovieEntry.CONTENT_URI,
                    uriId);
            // create fragment
            DetailFragment detailFragment = DetailFragment.newInstance(uriId, uri);

            if (getActivity().findViewById(R.id.container) != null) {
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container,
                        detailFragment ).commit();
            } else
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, detailFragment )
                        .addToBackStack(null).commit();
        }
    }

    public void onResume()
    {
        Log.d(LOG_TAG, "onResume");
        super.onResume();
        String sortOrder = Utility.getPreferedSortOrder(getContext());

        if( sortOrder != null && sortOrder != mSortOrder )
        {
            mPosition = 0;
            mSortOrder = sortOrder;
            setSelectionArguments();
            getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);
        }
        if( mbTwoPane )
            updateDetailFragment( mPosition );
    }
    public void setSelectionArguments()
    {
        Log.d(LOG_TAG, "setSelectionArguments");

        if( mSortOrder.equals(getString(R.string.pref_sortorder_Favorites)))
        {
            Log.d(LOG_TAG, "Favorites");
            mSelection = MovieContract.MovieEntry.COLUMN_FAVORITE+ "=?";
            mSelectionArgs = new String[]{"Yes"};
        }
        else {
            mSelection = MovieContract.MovieEntry.COLUMN_SORTORDER + "=?";
            mSelectionArgs = new String[]{mSortOrder};
        }

    }

    private void showToastMessage()
    {
        CharSequence text = "No Network connection";
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(getContext(), text, duration);
        toast.show();
    }



    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Log.d(LOG_TAG, "onCreateLoader");


        return new FetchMovieDetailsTask(getContext(),
                MovieContract.MovieEntry.CONTENT_URI,
                null,
                mSelection,
                mSelectionArgs,
                null);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){

        Log.d(LOG_TAG, "onViewCreated");
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        Log.d(LOG_TAG, "onLoadFinished");
        mCurrentCursor  = data;
        movieAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset( Loader<Cursor> loader) {
        movieAdapter.swapCursor(null);
    }
}
