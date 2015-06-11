/*
 * Copyright (C) 2015 Raphaël Bischof
 */

package bischof.raphael.spotifystreamer.async;

/**
 * Interface definition for a callback to be invoked when an AsyncTask loading datas sends data back.
 * Created by biche on 09/06/2015.
 */
public interface OnContentLoadedListener<T> {

    /**
     * Callback method to be invoked when data fetching is successful
     *
     * @param content The result of the data fetching
     */
    void onContentLoaded(T content);

    /**
     * Callback method to be invoked when data fetching failed
     *
     * @param errorMessage Explanation of the fetch fail
     */
    void onContentError(String errorMessage);
}
