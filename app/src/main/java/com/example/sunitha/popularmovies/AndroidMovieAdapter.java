package com.example.sunitha.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.sunitha.popularmovies.data.MovieContract;
import com.squareup.picasso.Picasso;

/**
 * Created by Sunitha on 11/12/2015.
 */
public class AndroidMovieAdapter extends CursorAdapter {

    private static final String LOG_TAG = AndroidMovieAdapter.class.getSimpleName();

    private Context mContext;
    private static int sLoaderID;

    public static class ViewHolder {
        public final ImageView imageView;

        public ViewHolder(View view){

            imageView = (ImageView) view.findViewById(R.id.image_item_movie);

        }
    }

    public AndroidMovieAdapter(Context context, Cursor c, int flags, int loaderID ) {

        super(context, c, flags);
        Log.d(LOG_TAG, "AndroidMovieAdapter");
        mContext = context;
        sLoaderID = loaderID;
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        int layoutId = R.layout.movie_details;

        Log.d(LOG_TAG, "In new View");

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        Log.d(LOG_TAG, "In bind View");

        int posterIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER);
        String poster = cursor.getString(posterIndex);
        Picasso.with(mContext).load("http://image.tmdb.org/t/p/w185/" + poster).into(viewHolder.imageView);
        viewHolder.imageView.setAdjustViewBounds(true);

    }
}
