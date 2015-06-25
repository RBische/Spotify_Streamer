/*
 * Copyright (C) 2015 Raphaël Bischof
 */

package bischof.raphael.spotifystreamer.async;

import android.content.Context;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import bischof.raphael.spotifystreamer.R;
import bischof.raphael.spotifystreamer.model.ParcelableTrack;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;

/**
 * Search/loads top tracks of a specified artist.
 * Created by biche on 11/06/2015.
 */
public class TopTracksLoader extends AsyncTask<String, Void, TopTracksLoader.Response> {
    private int mSizeOfImageToLoad;
    private Context mContext;

    /**
     * Constructor
     * @param sizeOfImageToLoad size of the ImageView in px where the thumbnail will be displayed
     * @param context Context is needed to retrieve country code preferences
     */
    public TopTracksLoader(int sizeOfImageToLoad, Context context) {
        this.mSizeOfImageToLoad = sizeOfImageToLoad;
        this.mContext = context;
    }

    /**
     * The listener that receives notifications the asynctask finished its work
     */
    private OnContentLoadedListener<ArrayList<ParcelableTrack>> listener;

    /**
     * Search/loads top tracks of an artist asynchronously
     * @param params String array in which item at index 0 is considered as the artist's id searched
     * @return Returns a {@link Response} that contains either an adapter to display the results or a string explaning the error
     */
    @Override
    protected Response doInBackground(String... params) {
        try {
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();
            Map<String, Object> options = new HashMap<>();
            String countryCode = PreferenceManager.getDefaultSharedPreferences(mContext).getString(mContext.getString(R.string.pref_country_code_key), Locale.getDefault().getCountry());
            options.put("country",countryCode);
            Tracks tracks = spotify.getArtistTopTrack(params[0],options);
            return new Response(null,tracks.tracks);
        }catch (RetrofitError e){
            return new Response(e,null);
        }
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    @Override
    protected void onPostExecute(Response response) {
        super.onPostExecute(response);
        if(listener!=null){
            if (response!=null&&response.getTracks()!=null){
                listener.onContentLoaded(ParcelableTrack.convertFromTrackList(response.getTracks(),mSizeOfImageToLoad));
            }else{
                if (response!=null&&response.getError()!=null) {
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
    public void setOnContentLoadedListener(OnContentLoadedListener<ArrayList<ParcelableTrack>> listener) {
        this.listener = listener;
    }


    /**
     * Class formatting the AsyncTask result.
     */
    protected class Response {
        private RetrofitError error;
        private List<Track> tracks;

        public Response(RetrofitError error, List<Track> tracks) {
            this.error = error;
            this.tracks = tracks;
        }

        public RetrofitError getError() {
            return error;
        }

        public List<Track> getTracks() {
            return tracks;
        }
    }
}
