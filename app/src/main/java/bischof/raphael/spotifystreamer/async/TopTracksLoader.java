/*
 * Copyright (C) 2015 Raphaël Bischof
 */

package bischof.raphael.spotifystreamer.async;

import android.content.Context;
import android.os.AsyncTask;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import bischof.raphael.spotifystreamer.adapter.TopTracksAdapter;
import bischof.raphael.spotifystreamer.database.SpotifyDatasource;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;

/**
 * Search/loads/store top tracks of a specified artist.
 * Needs a context to achieve the maximum of work in the background task to avoid UI freeze (like storing top tracks in DB,clean old datas, etc...)
 * Created by biche on 11/06/2015.
 */
public class TopTracksLoader extends AsyncTask<String, Void, TopTracksLoader.Response> {
    private Context mContext;
    private OnContentLoadedListener<TopTracksAdapter> listener;

    public TopTracksLoader(Context context) {
        this.mContext = context;
    }

    public void setOnContentLoadedListener(OnContentLoadedListener<TopTracksAdapter> listener) {
        this.listener = listener;
    }

    @Override
    protected Response doInBackground(String... params) {
        SpotifyDatasource datasource = new SpotifyDatasource(mContext);
        datasource.open();
        boolean isEverLoaded = datasource.isTopTracksInDB(params[0]);
        if (!isEverLoaded){
            try {
                SpotifyApi api = new SpotifyApi();
                SpotifyService spotify = api.getService();
                Map<String, Object> options = new HashMap<>();
                options.put("country", Locale.getDefault().getCountry());
                Tracks tracks = spotify.getArtistTopTrack(params[0],options);
                TopTracksAdapter adapter = datasource.createTopTracksCursorAdapter(mContext, params[0], tracks.tracks);
                return new Response(null,adapter);
            }catch (RetrofitError e){
                datasource.close();
                return new Response(e,null);
            }
        }else{
            TopTracksAdapter adapter = datasource.createTopTracksCursorAdapter(mContext, params[0]);
            return new Response(null,adapter);
        }
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    @Override
    protected void onPostExecute(Response response) {
        super.onPostExecute(response);
        if(listener!=null){
            if (response!=null&&response.getAdapter()!=null){
                listener.onContentLoaded(response.getAdapter());
            }else{
                if (response!=null&&response.getError()!=null) {
                    listener.onContentError(response.getError().getMessage());
                }
            }
        }
    }

    protected class Response {
        private RetrofitError error;
        private TopTracksAdapter adapter;

        public Response(RetrofitError error, TopTracksAdapter adapter) {
            this.error = error;
            this.adapter = adapter;
        }

        public RetrofitError getError() {
            return error;
        }

        public TopTracksAdapter getAdapter() {
            return adapter;
        }
    }
}
