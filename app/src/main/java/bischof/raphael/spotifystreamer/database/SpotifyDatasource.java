/*
 * Copyright (C) 2015 Raphaël Bischof
 */

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
 * Created by biche on 10/06/2015.
 */
public class SpotifyDatasource {
    private static final int ITEMS_COUNT_STORED = 5;
    private SQLiteDatabase database;
    private SpotifyDB dbHelper;


    /**
     * Constructor
     *
     * @param context The current context.
     */
    public SpotifyDatasource(Context context) {
        dbHelper = new SpotifyDB(context);
    }

    /**
     * Prepares the Datasource (get an instance of the DB)
     * @throws SQLException
     */
    public void open() throws SQLException {
        database= dbHelper.getWritableDatabase();
    }

    /**
     *  Free the resources of the datasource. Use this method after using your datasource to avoid memory leaks.
     */
    public void close() {
        database.close();
        dbHelper.close();
    }

    /**
     * Returns a boolean that specifies if the top tracks are ever stored in DB for a specific artist ID
     * @param id The artist ID
     * @return Boolean that specifies if the top tracks are ever stored in DB for a specific artist ID
     */
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

    /**
     * Store the top tracks for a specific artist ID. After cleaning up old obsolete data, it returns a TopTracksAdapter to display results
     * @param context The current context
     * @param artistId Artist ID that depends the datas
     * @param tracks The tracks to store
     * @return TopTracksAdapter that contains rows corresponding to the tracks
     */
    public TopTracksAdapter createTopTracksCursorAdapter(Context context, String artistId, List<Track> tracks) {
        long currentTime = Calendar.getInstance().getTimeInMillis();
        for(int i=0;i<tracks.size();i++){
            insertTopTrack(tracks.get(i),artistId,currentTime);
        }
        makeDataRotation();
        return createTopTracksCursorAdapter(context,artistId);
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

    /**
     * Get a TopTracksAdapter to display top tracks that has ever been stored
     * @param context The current context
     * @return TopTracksAdapter that contains rows corresponding to the tracks
     */
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
