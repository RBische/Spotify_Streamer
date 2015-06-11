/*
 * Copyright (C) 2015 Raphaël Bischof
 */

package bischof.raphael.spotifystreamer.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * ArtistDB let the app store informations about top tracks of artists
 * Created by biche on 10/06/2015.
 */
public class SpotifyDB extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Artist.db";

    //Key of Table Name
    public static final String TOP_TRACKS_TABLE_NAME = "TopTracks";

    //Key of TopTracksTable's column
    public static final String KEY_TOP_TRACKS_ID = "_id";
    //Key of TopTracksTable's column
    public static final String KEY_TOP_TRACKS_NAME = "name";
    //Key of TopTracksTable's column
    public static final String KEY_TOP_TRACKS_ARTIST = "artistID";
    //Key of TopTracksTable's column
    public static final String KEY_TOP_TRACKS_ALBUM = "album";
    //Key of TopTracksTable's column
    public static final String KEY_TOP_TRACKS_ALBUM_IMAGE_URL = "albumImageUrl";
    //Key of TopTracksTable's column
    public static final String KEY_TOP_TRACKS_ALBUM_LARGE_IMAGE_URL = "albumLargeImageUrl";
    //Key of TopTracksTable's column
    public static final String KEY_TOP_TRACKS_PREVIEW_URL = "previewUrl";
    //Key of TopTracksTable's column
    //To know top tracks load date, useful to do top tracks data rotation
    public static final String KEY_TOP_TRACKS_LOAD_DATE = "dateTopTracksLoaded";

    private static final String TOP_TRACKS_TABLE_CREATE = "CREATE TABLE " + TOP_TRACKS_TABLE_NAME +
            " (" + KEY_TOP_TRACKS_ID + " TEXT, "
            +KEY_TOP_TRACKS_NAME + " TEXT, "
            +KEY_TOP_TRACKS_ARTIST + " TEXT, "
            +KEY_TOP_TRACKS_ALBUM + " TEXT, "
            +KEY_TOP_TRACKS_ALBUM_IMAGE_URL + " TEXT, "
            +KEY_TOP_TRACKS_ALBUM_LARGE_IMAGE_URL + " TEXT, "
            + KEY_TOP_TRACKS_LOAD_DATE + " INTEGER, "
            +KEY_TOP_TRACKS_PREVIEW_URL + " TEXT);";


    /**
     * Constructor
     *
     * @param context The current context.
     */
    public SpotifyDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Creates the DB
     * @param db Current db instance
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TOP_TRACKS_TABLE_CREATE);
    }

    /**
     * Updates the db by dropping tables and then recreating them
     * @param db Current db instance
     * @param oldVersion The current version of the DB that needs to be updated
     * @param newVersion The version in DATABASE_VERSION
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TOP_TRACKS_TABLE_NAME);
        onCreate(db);
    }
}