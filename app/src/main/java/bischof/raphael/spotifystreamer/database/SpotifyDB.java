package bischof.raphael.spotifystreamer.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * ArtistDB let the app store informations about top tracks of artists
 */
public class SpotifyDB extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Artist.db";

    public static final String TOP_TRACKS_TABLE_NAME = "TopTracks";

    public static final String KEY_TOP_TRACKS_ID = "_id";
    public static final String KEY_TOP_TRACKS_NAME = "name";
    public static final String KEY_TOP_TRACKS_ARTIST = "artistID";
    public static final String KEY_TOP_TRACKS_ALBUM = "album";
    public static final String KEY_TOP_TRACKS_ALBUM_IMAGE_URL = "albumImageUrl";
    public static final String KEY_TOP_TRACKS_ALBUM_LARGE_IMAGE_URL = "albumLargeImageUrl";
    public static final String KEY_TOP_TRACKS_PREVIEW_URL = "previewUrl";
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

    public SpotifyDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TOP_TRACKS_TABLE_CREATE);
    }

    @Override

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TOP_TRACKS_TABLE_NAME);
        onCreate(db);
    }
}