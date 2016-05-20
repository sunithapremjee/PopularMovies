package com.example.sunitha.popularmovies.data;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class MovieContract{

    public static final String CONTENT_AUTHORITY = "com.example.sunitha.popularmovies.app";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);


    public static final class MovieEntry implements BaseColumns {
        // table name
        public static final String TABLE_MOVIES = "movies";
        // columns
        public static final String _ID = "_id";
        public static final String COLUMN_SORTORDER = "sort_order";
        public static final String COLUMN_FAVORITE = "Favorite";
        public static final String COLUMN_NAME = "moviename";
        public static final String COLUMN_POSTER = "poster";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_USERRATING = "user_rating";
        public static final String COLUMN_RELEASEDATE = "release_date";
        public static final String COLUMN_MOVIEID = "movie_id";
        public static final String COLUMN_TRAILORS = "trailors";
        public static final String COLUMN_REVIEWS = "reviews";

        // create content uri
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(TABLE_MOVIES).build();

        // create cursor of base type directory for multiple entries
        public static final String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TABLE_MOVIES;

        // create cursor of base type item for single entry
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE +"/" + CONTENT_AUTHORITY + "/" + TABLE_MOVIES;

        // for building URIs on insertion
        public static Uri buildMoviesUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
