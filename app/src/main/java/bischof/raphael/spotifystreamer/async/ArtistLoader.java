/*
 * Copyright (C) 2015 Raphaël Bischof
 */

package bischof.raphael.spotifystreamer.async;

import android.os.AsyncTask;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.RetrofitError;

/**
 * Search and loads the artists from a string
 * Created by biche on 10/06/2015.
 */
public class ArtistLoader extends AsyncTask<String, Void, ArtistLoader.Response> {
    /**
     * The listener that receives notifications the asynctask finished its work
     */
    private OnContentLoadedListener<List<Artist>> listener;

    /**
     * Search and loads the artist asynchronously and store it in result
     * @param params String array in which item at index 0 is considered as the artist's name searched
     * @return Returns a {@link Response} that contains either the artists or a string explaning the error
     */
    @Override
    protected ArtistLoader.Response doInBackground(String... params) {
        try {
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            ArtistsPager pager = spotify.searchArtists(params[0]);
            return new Response(null,pager.artists.items);
        }catch (RetrofitError e){
            return new Response(e,null);
        }
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    @Override
    protected void onPostExecute(ArtistLoader.Response response) {
        super.onPostExecute(response);
        if(listener!=null){
            if (response!=null&&response.getArtists()!=null){
                listener.onContentLoaded(response.artists);
            }else{
                if (response!=null&&response.getError()!=null){
                    listener.onContentError(response.getError().getMessage());
                }
            }
        }
    }

    /**
     * Register a callback to be invoked when the asynctask finished its work
     *
     * @param listener The callback that will be invoked.
     */
    public void setOnContentLoadedListener(OnContentLoadedListener<List<Artist>> listener) {
        this.listener = listener;
    }

    /**
     * Class formatting the AsyncTask result.
     */
    protected class Response{
        private RetrofitError error;
        private List<Artist> artists;

        public Response(RetrofitError error, List<Artist> artists) {
            this.error = error;
            this.artists = artists;
        }

        public RetrofitError getError() {
            return error;
        }

        public List<Artist> getArtists() {
            return artists;
        }
    }
}
