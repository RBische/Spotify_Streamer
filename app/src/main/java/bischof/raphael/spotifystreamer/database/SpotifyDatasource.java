package bischof.raphael.spotifystreamer.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.Calendar;
import java.util.List;

import bischof.raphael.spotifystreamer.adapter.TopTracksAdapter;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Makes CRUD operations on SpotifyDB
 */
public class SpotifyDatasource {
    private static final int ITEMS_COUNT_STORED = 5;
    private SQLiteDatabase database;
    private SpotifyDB dbHelper;

    public SpotifyDatasource(Context context) {
        dbHelper = new SpotifyDB(context);
    }

    public void open() throws SQLException {
        database= dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public boolean isTopTracksInDB(String id){
        boolean exists = false;

        Cursor cursor = database.query(SpotifyDB.TOP_TRACKS_TABLE_NAME,new String[]{SpotifyDB.KEY_TOP_TRACKS_ARTIST}, SpotifyDB.KEY_TOP_TRACKS_ARTIST+"=?", new String[]{id}, null, null, null, "1");
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            exists = true;
        }
        cursor.close();
        return exists;
    }

    public TopTracksAdapter createTopTracksCursorAdapter(Context context, String artistID, List<Track> tracks) {
        long currentTime = Calendar.getInstance().getTimeInMillis();
        for(int i=0;i<tracks.size();i++){
            insertTopTrack(tracks.get(i),artistID,currentTime);
        }
        makeDataRotation();
        return createTopTracksCursorAdapter(context,artistID);
    }

    private void makeDataRotation() {
        long dateMaxToKeep = 0;
        Cursor cursor = database.query(SpotifyDB.TOP_TRACKS_TABLE_NAME,
                new String[]{SpotifyDB.KEY_TOP_TRACKS_LOAD_DATE},
                null, null, SpotifyDB.KEY_TOP_TRACKS_LOAD_DATE, null, SpotifyDB.KEY_TOP_TRACKS_LOAD_DATE+" DESC",""+ITEMS_COUNT_STORED+",1");
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            dateMaxToKeep = cursor.getLong(0);
            cursor.moveToNext();
        }
        cursor.close();
        database.delete(SpotifyDB.TOP_TRACKS_TABLE_NAME,SpotifyDB.KEY_TOP_TRACKS_LOAD_DATE+"<?",new String[]{""+dateMaxToKeep});
    }

    private void insertTopTrack(Track track, String artistID,long timeCreation) {
        ContentValues values = new ContentValues();
        values.put(SpotifyDB.KEY_TOP_TRACKS_ID, track.id);
        values.put(SpotifyDB.KEY_TOP_TRACKS_NAME, track.name);
        values.put(SpotifyDB.KEY_TOP_TRACKS_ALBUM, track.album.name);
        values.put(SpotifyDB.KEY_TOP_TRACKS_ARTIST, artistID);
        values.put(SpotifyDB.KEY_TOP_TRACKS_LOAD_DATE, timeCreation);
        values.put(SpotifyDB.KEY_TOP_TRACKS_PREVIEW_URL, track.preview_url);
        if(track.album.images!=null){
            if(track.album.images.size()>0){
                int indexKeepedLarge = -1;
                int indexKeepedSmall = -1;
                for(int i=0;i<track.album.images.size();i++){
                    if(track.album.images.get(i).width==640||track.album.images.get(i).height==640){
                        indexKeepedLarge = i;
                    } else if(track.album.images.get(i).width==200||track.album.images.get(i).height==200){
                        indexKeepedSmall = i;
                    }
                }
                if (indexKeepedLarge!=-1){
                    values.put(SpotifyDB.KEY_TOP_TRACKS_ALBUM_LARGE_IMAGE_URL,track.album.images.get(indexKeepedLarge).url);
                }else {
                    values.put(SpotifyDB.KEY_TOP_TRACKS_ALBUM_LARGE_IMAGE_URL,track.album.images.get(0).url);
                }
                if (indexKeepedSmall!=-1){
                    values.put(SpotifyDB.KEY_TOP_TRACKS_ALBUM_IMAGE_URL,track.album.images.get(indexKeepedSmall).url);
                }else {
                    values.put(SpotifyDB.KEY_TOP_TRACKS_ALBUM_IMAGE_URL,track.album.images.get(0).url);
                }
            }
        }
        database.insert(SpotifyDB.TOP_TRACKS_TABLE_NAME, null, values);
    }

    public TopTracksAdapter createTopTracksCursorAdapter(Context context, String artistID) {
        //Cursor is used after in TopTracksAdapter, we can't free it, that explains the suppressLint
        @SuppressLint("Recycle") Cursor cursor = database.query(SpotifyDB.TOP_TRACKS_TABLE_NAME,
                new String[]{SpotifyDB.KEY_TOP_TRACKS_ID,
                        SpotifyDB.KEY_TOP_TRACKS_NAME,
                        SpotifyDB.KEY_TOP_TRACKS_ALBUM,
                        SpotifyDB.KEY_TOP_TRACKS_ALBUM_IMAGE_URL,
                        SpotifyDB.KEY_TOP_TRACKS_ALBUM_LARGE_IMAGE_URL,
                        SpotifyDB.KEY_TOP_TRACKS_PREVIEW_URL},
                SpotifyDB.KEY_TOP_TRACKS_ARTIST+"=?", new String[]{artistID}, null, null, null);
        return new TopTracksAdapter(context,cursor);
    }
}
