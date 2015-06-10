package bischof.raphael.spotifystreamer.async;

/**
 * Interface definition for a callback to be invoked when an AsyncTask loading datas sends data back.
 * Created by biche on 09/06/2015.
 */
public interface OnContentLoadedListener<T> {
    void onContentLoaded(T content);
    void onContentError(String errorMessage);
}
