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
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import java.io.IOException;
import java.util.ArrayList;

import bischof.raphael.spotifystreamer.R;
import bischof.raphael.spotifystreamer.activity.HomeActivity;
import bischof.raphael.spotifystreamer.model.ParcelableTrack;

/**
 * Service that stream the music from a specified track list
 * Multiple actions are available to control the service
 * Created by biche on 22/06/2015.
 */
public class StreamerService extends Service implements MediaPlayer.OnPreparedListener,MediaPlayer.OnCompletionListener {
    public static final String ACTION_TOGGLE_PLAY_PAUSE = "TogglePlayPause";
    public static final String ACTION_TOGGLE_NOTIFICATION = "ToggleNotification";
    public static final String ACTION_NEXT_SONG = "NextSong";
    public static final String ACTION_PREVIOUS_SONG = "NextSong";
    public static final String ACTION_LOAD_SONG = "LoadSong";
    public static final String EXTRA_TOP_TRACKS = "ExtraTopTracks";
    public static final String EXTRA_TOP_TRACK_SELECTED = "ExtraTopTrackSelected";
    private static final int NOTIFICATION_ID = 668;
    private MediaPlayer mMediaPlayer;
    private boolean mPlayerPrepared = false;
    private boolean mNotificationShown = true;
    private ArrayList<ParcelableTrack> mTopTracks;
    private int mTopTrackSelected;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(ACTION_LOAD_SONG)){
            //Getting the extras
            this.mTopTracks = intent.getParcelableArrayListExtra(EXTRA_TOP_TRACKS);
            this.mTopTrackSelected = intent.getIntExtra(EXTRA_TOP_TRACK_SELECTED,0);
            loadSong();
        }else if (intent.getAction().equals(ACTION_TOGGLE_PLAY_PAUSE)){
            if (mPlayerPrepared){
                if(mMediaPlayer.isPlaying()){
                    mMediaPlayer.pause();
                }else{
                    mMediaPlayer.start();
                }
            }
        }else if (intent.getAction().equals(ACTION_TOGGLE_NOTIFICATION)){
            if(mNotificationShown){
                hideNotification();
            }else {
                if(mMediaPlayer.isPlaying()){
                    showNotification();
                }
            }
        }else if (intent.getAction().equals(ACTION_NEXT_SONG)){
            this.mTopTrackSelected+=1;
            loadSong();
        }else if (intent.getAction().equals(ACTION_PREVIOUS_SONG)) {
            this.mTopTrackSelected-=1;
            loadSong();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void loadSong() {
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
            //TODO: fill exception
        }
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.prepareAsync();
    }

    private void showNotification() {
        ParcelableTrack currentTrack = mTopTracks.get(mTopTrackSelected);

        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
                new Intent(getApplicationContext(), HomeActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notification = new Notification.Builder(getApplicationContext())
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                            // Add media control buttons that invoke intents in your media service
                    .addAction(android.R.drawable.ic_media_previous, "Previous", previousPendingIntent) // #0
                    .addAction(android.R.drawable.ic_media_pause, "Pause", pausePendingIntent)  // #1
                    .addAction(android.R.drawable.ic_media_next, "Next", nextPendingIntent)
                    .setStyle(new Notification.MediaStyle()
                            // Show our playback controls in the compat view
                            .setShowActionsInCompactView(0, 1, 2))
                    .setContentText(currentTrack.getName() + " - " + currentTrack.getAlbumName())
                    .setContentTitle(getApplicationContext().getString(R.string.now_playing))
                    .setContentIntent(pi).build();     // #2
        }else{
            notification = new NotificationCompat.Builder(getApplicationContext())
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                            // Add media control buttons that invoke intents in your media service
                    .addAction(android.R.drawable.ic_media_previous, "Previous", previousPendingIntent) // #0
                    .addAction(android.R.drawable.ic_media_pause, "Pause", pausePendingIntent)  // #1
                    .addAction(android.R.drawable.ic_media_next, "Next", nextPendingIntent)
                    .setContentText(currentTrack.getName() + " - " + currentTrack.getAlbumName())
                    .setContentTitle(getApplicationContext().getString(R.string.now_playing))
                    .setContentIntent(pi).build();     // #2
        }
        notification.tickerText = getApplicationContext().getString(R.string.now_playing);
        notification.icon = R.drawable.ic_cd_placeholder;
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        startForeground(NOTIFICATION_ID, notification);
        mNotificationShown = true;
    }

    private void hideNotification() {
        NotificationManager manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(NOTIFICATION_ID);
        stopForeground(true);
        mNotificationShown = false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mPlayerPrepared = true;
        mp.start();
        mp.setOnCompletionListener(this);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(mNotificationShown){
            hideNotification();
        }
    }
}
