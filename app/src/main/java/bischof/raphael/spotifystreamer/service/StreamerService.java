/*
 * Copyright (C) 2015 Raphaël Bischof
 */

package bischof.raphael.spotifystreamer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import bischof.raphael.spotifystreamer.R;
import bischof.raphael.spotifystreamer.activity.HomeActivity;
import bischof.raphael.spotifystreamer.model.ParcelableTrack;

/**
 * Service that stream the music from a specified track list
 * Multiple actions are available to control the service
 * Created by biche on 22/06/2015.
 */
public class StreamerService extends Service implements MediaPlayer.OnPreparedListener,MediaPlayer.OnCompletionListener {
    private static final String LOG_TAG = StreamerService.class.getSimpleName();
    // Binder given to clients
    private final IBinder mBinder = new StreamerBinder();

    private OnStreamerStateChangeListener mListener;

    //Actions available of the service
    public static final String ACTION_SHOW_UI_FROM_SONG = "ShowUIFromSong";
    public static final String ACTION_TOGGLE_PLAY_PAUSE = "TogglePlayPause";
    public static final String ACTION_NEXT_SONG = "NextSong";
    public static final String ACTION_PREVIOUS_SONG = "PreviousSong";
    public static final String ACTION_LOAD_SONG = "LoadSong";
    public static final String ACTION_STAY_AWAKE = "ActionToForceServiceToStayAwake";

    public static final String ACTION_NOTIFY_SERVICE_STATE = "bischof.raphael.spotifystreamer.service.ServiceStateUpdate";
    public static final String EXTRA_TOP_TRACKS = "ExtraTopTracks";
    public static final String EXTRA_TOP_TRACK_SELECTED = "ExtraTopTrackSelected";
    public static final String EXTRA_SERVICE_STARTED = "ExtraServiceStarted";

    private static final int NOTIFICATION_ID = 668;

    private MediaPlayer mMediaPlayer;
    private boolean mPlayerPrepared = false;
    private boolean mPlayerPreparing = false;
    private boolean mNotificationShown = true;
    private boolean mPlayerEmpty = true;
    private ArrayList<ParcelableTrack> mTopTracks;
    private int mTopTrackSelected;
    private int mNotificationCount = 0;

    public boolean isPlayerPrepared() {
        return mPlayerPrepared;
    }

    public int getDuration() {
        return mMediaPlayer.getDuration();
    }

    public boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    public void seekTo(int progress) {
        if(!mPlayerEmpty){
            mMediaPlayer.seekTo(progress);
        }
    }

    public boolean isPlayerEmpty() {
        return mPlayerEmpty;
    }

    public ArrayList<ParcelableTrack> getTopTracks() {
        return mTopTracks;
    }

    public int getTopTrackSelected() {
        return mTopTrackSelected;
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class StreamerBinder extends Binder {
        public StreamerService getService() {
            // Return this instance of LocalService so clients can call public methods
            return StreamerService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //Notifies the broadcast receivers that the service started. That broadcast allows activities to know if they have to refresh or not their menus
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.setAction(ACTION_NOTIFY_SERVICE_STATE);
        intent.putExtra(EXTRA_SERVICE_STARTED, true);
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer!=null){
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        //Notifies the broadcast receivers that the service stopped. That broadcast allows activities to know if they have to refresh or not their menus
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.setAction(ACTION_NOTIFY_SERVICE_STATE);
        intent.putExtra(EXTRA_SERVICE_STARTED, false);
        sendBroadcast(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        //When the fragment binding to the service stops, we also stops the service except if the mediaplayer is playing
        if(mMediaPlayer!=null&&mMediaPlayer.isPlaying()){
            showNotification();
        }else{
            stopSelf();
        }
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent!=null){
            if (intent.getAction()!=null){
                if (intent.getAction().equals(ACTION_LOAD_SONG)){
                    //Getting the extras
                    ArrayList<ParcelableTrack> tracks = intent.getParcelableArrayListExtra(EXTRA_TOP_TRACKS);
                    int currentPosition = intent.getIntExtra(EXTRA_TOP_TRACK_SELECTED,0);
                    loadSong(tracks,currentPosition);
                }else if (intent.getAction().equals(ACTION_TOGGLE_PLAY_PAUSE)){
                    togglePlayPause();
                }else if (intent.getAction().equals(ACTION_NEXT_SONG)){
                    playNextSong();
                }else if (intent.getAction().equals(ACTION_PREVIOUS_SONG)) {
                    playPreviousSong();
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Switch the player to previous song. If there is no previous song, switch to last song.
     */
    public void playPreviousSong() {
        this.mTopTrackSelected-=1;
        if(mTopTrackSelected<0){
            mTopTrackSelected = mTopTracks.size()-1;
        }
        loadSong();
        if(mNotificationShown){
            showNotification(true);
        }
    }

    /**
     * Switch the player to next song. If there is no next song, switch to the first song.
     */
    public void playNextSong() {
        this.mTopTrackSelected+=1;
        if(mTopTrackSelected==mTopTracks.size()){
            mTopTrackSelected = 0;
        }
        loadSong();
        if(mNotificationShown){
            showNotification(true);
        }
    }

    /**
     * Loads/prepare and plays the song at the specified position
     * @param tracks A list of tracks that can be explored via Next/Previous
     * @param currentPosition The position in the last of tracks that we want to play
     */
    public void loadSong(ArrayList<ParcelableTrack> tracks, int currentPosition) {
        if (tracks!=null){
            boolean needsToLoadSong = false;
            if (mTopTracks==null||!mTopTracks.get(mTopTrackSelected).getPreviewUrl().equals(tracks.get(currentPosition).getPreviewUrl())){
                needsToLoadSong = true;
            }
            this.mTopTracks = tracks;
            this.mTopTrackSelected = currentPosition;
            mPlayerEmpty = false;
            if(needsToLoadSong){
                loadSong();
            }
        }
    }

    /**
     * Toggle the mediaplayer to pause state or to play state depending the base state.
     */
    public void togglePlayPause() {
        if (mPlayerPrepared){
            if(mMediaPlayer.isPlaying()){
                mMediaPlayer.pause();
            }else{
                mMediaPlayer.start();
            }
            if(mNotificationShown){
                showNotification();
            }
        }else{
            if (!mPlayerPreparing){
                loadSong();
            }
        }
    }

    private void loadSong() {
        mPlayerPrepared = false;
        //Initializing the MediaPlayer
        String url = mTopTracks.get(mTopTrackSelected).getPreviewUrl();
        if (mMediaPlayer!=null){
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mMediaPlayer.setDataSource(url);
        } catch (IOException | IllegalArgumentException e) {
            //Should never be raised
            Log.d(LOG_TAG,e.getMessage());
        }
        mMediaPlayer.setOnPreparedListener(this);
        this.mPlayerPreparing = true;
        mMediaPlayer.prepareAsync();
    }

    /**
     * Show a notification to control the player.
     */
    public void showNotification() {
        showNotification(false);
    }

    private void showNotification(boolean forceShowIsPlaying) {
        ParcelableTrack currentTrack = mTopTracks.get(mTopTrackSelected);
        Intent intentStreamingUI = new Intent(getBaseContext(), HomeActivity.class);
        intentStreamingUI.setAction(ACTION_SHOW_UI_FROM_SONG);
        intentStreamingUI.putExtra(EXTRA_TOP_TRACK_SELECTED,mTopTrackSelected);
        intentStreamingUI.putExtra(EXTRA_TOP_TRACKS,mTopTracks);
        intentStreamingUI.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pi = PendingIntent.getActivity(getBaseContext(), 0,
                intentStreamingUI,
                PendingIntent.FLAG_CANCEL_CURRENT);
        Intent intentPrevious = new Intent(getApplicationContext(), StreamerService.class);
        intentPrevious.setAction(ACTION_PREVIOUS_SONG);
        PendingIntent previousPendingIntent = PendingIntent.getService(getApplicationContext(), 0,
                intentPrevious,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Intent intentNext = new Intent(getApplicationContext(), StreamerService.class);
        intentNext.setAction(ACTION_NEXT_SONG);
        PendingIntent nextPendingIntent = PendingIntent.getService(getApplicationContext(), 0,
                intentNext,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Intent intentPause = new Intent(getApplicationContext(), StreamerService.class);
        intentPause.setAction(ACTION_TOGGLE_PLAY_PAUSE);
        PendingIntent pausePendingIntent = PendingIntent.getService(getApplicationContext(), 0,
                intentPause,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification;
        int btnPlayPause;
        if (forceShowIsPlaying){
            btnPlayPause = android.R.drawable.ic_media_pause;
        }else if (mMediaPlayer.isPlaying()){
            btnPlayPause = android.R.drawable.ic_media_pause;
        }else{
            btnPlayPause = android.R.drawable.ic_media_play;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Notification.Builder builder = new Notification.Builder(getApplicationContext())
                            // Add media control buttons that invoke intents in your media service
                    .addAction(android.R.drawable.ic_media_previous, "Previous", previousPendingIntent)
                    .addAction(btnPlayPause, "Pause", pausePendingIntent)
                    .addAction(android.R.drawable.ic_media_next, "Next", nextPendingIntent)
                    .setStyle(new Notification.MediaStyle()
                            // Show our playback controls in the compat view
                            .setShowActionsInCompactView(0, 1, 2))
                    .setContentText(currentTrack.getName() + " - " + currentTrack.getAlbumName())
                    .setContentTitle(getApplicationContext().getString(R.string.now_playing))
                    .setContentIntent(pi);
            if(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(getString(R.string.pref_enable_notifications_key),true)){
                builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            }else{
                builder.setVisibility(NotificationCompat.VISIBILITY_PRIVATE);
            }
            buildLargeIcon(currentTrack.getImageUrlLarge(),null,builder);
            notification = builder.build();
        }else{
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                            // Add media control buttons that invoke intents in your media service
                    .addAction(android.R.drawable.ic_media_previous, "Previous", previousPendingIntent)
                    .addAction(btnPlayPause, "Pause", pausePendingIntent)
                    .addAction(android.R.drawable.ic_media_next, "Next", nextPendingIntent)
                    .setContentText(currentTrack.getName() + " - " + currentTrack.getAlbumName())
                    .setContentTitle(getApplicationContext().getString(R.string.now_playing))
                    .setContentIntent(pi);
            if(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(getString(R.string.pref_enable_notifications_key),true)){
                builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            }else{
                builder.setVisibility(NotificationCompat.VISIBILITY_PRIVATE);
            }
            buildLargeIcon(currentTrack.getImageUrlLarge(),builder,null);
            notification = builder.build();
        }
        notification.tickerText = getApplicationContext().getString(R.string.now_playing);
        notification.icon = R.drawable.ic_notification_cd;
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        startForeground(NOTIFICATION_ID, notification);
        mNotificationCount+=1;
        mNotificationShown = true;
    }

    private void buildLargeIcon(final String url, NotificationCompat.Builder builderCompat, Notification.Builder builder){
        if (url!=null){
            Bitmap contactPic = null;

            try {
                contactPic = new AsyncTask<Void, Void, Bitmap>() {
                    @Override
                    protected Bitmap doInBackground(Void... params) {
                        try {
                            return Picasso.with(getApplicationContext()).load(url)
                                    .resize(200, 200)
                                    .placeholder(R.drawable.ic_cd_placeholder)
                                    .error(R.drawable.ic_cd_placeholder)
                                    .get();
                        } catch (IOException e) {
                            //Can be raised only if Spotify's wrapper provides a wrong URL
                            Log.d(LOG_TAG,e.getMessage());
                        }
                        return null;
                    }
                }.execute().get();
            } catch (InterruptedException | ExecutionException e) {
                Log.d(LOG_TAG,e.getMessage());
            }
            if(builder!=null){
                if (contactPic != null) {
                    builder.setLargeIcon(contactPic);
                } else {
                    builder.setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.ic_cd_placeholder));
                }
            }else{
                if (contactPic != null) {
                    builderCompat.setLargeIcon(contactPic);
                } else {
                    builderCompat.setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.ic_cd_placeholder));
                }
            }
        }
    }

    /**
     * Hide notifications that controls the player. If more than one notification has been shown, the stack is cleared.
     */
    public void hideNotification() {
        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        while (mNotificationCount>0){
            manager.cancel(NOTIFICATION_ID);
            mNotificationCount--;
        }
        stopForeground(true);
        mNotificationShown = false;
    }

    /**
     * Gives the current playing time of the song
     * @return Playing time in milliseconds
     */
    public int getCurrentTime(){
        if (mMediaPlayer.isPlaying()){
            return mMediaPlayer.getCurrentPosition();
        }else{
            return 0;
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mPlayerPreparing = false;
        mPlayerPrepared = true;
        mp.start();
        mp.setOnCompletionListener(this);
        if (mListener!=null){
            mListener.onPlaying(mp.getDuration());
        }else{
            showNotification();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(mNotificationShown){
            hideNotification();
            stopSelf();
        }else{
            if (mListener!=null){
                mListener.onPlayEnding();
            }
        }
    }

    /**
     * Register a callback to be invoked when this service is playing a track or ended the stream.
     *
     * @param listener The callback that will run
     */
    public void setOnStreamerStateChangeListener(OnStreamerStateChangeListener listener) {
        this.mListener = listener;
    }

    public interface OnStreamerStateChangeListener{
        void onPlaying(int duration);
        void onPlayEnding();
    }
}
