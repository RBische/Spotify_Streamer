/*
 * Copyright (C) 2015 Raphaël Bischof
 */

package bischof.raphael.spotifystreamer.fragment;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import bischof.raphael.spotifystreamer.R;
import bischof.raphael.spotifystreamer.activity.StreamingActivity;
import bischof.raphael.spotifystreamer.model.ParcelableTrack;
import bischof.raphael.spotifystreamer.service.StreamerService;
import butterknife.ButterKnife;
import butterknife.InjectView;

/*
 * Fragment containing the Player UI. This fragment is bound to a StreamerService that plays the song shown.
 * This fragment contains 4 {@link ImageButton} that makes the StreamerService play, pause, go to next track, to previous track or that share the current track.
 * Created by biche on 10/06/2015.
 */
public class StreamingFragment extends DialogFragment implements View.OnClickListener,StreamerService.OnStreamerStateChangeListener,SeekBar.OnSeekBarChangeListener {

    public static final String EXTRA_TOP_TRACKS = "ExtraTopTracks";
    public static final String EXTRA_TOP_TRACK_SELECTED = "ExtraTopTrackSelected";
    @InjectView(R.id.tvArtistName) TextView mTvArtist;
    @InjectView(R.id.tvAlbumName) TextView mTvAlbum;
    @InjectView(R.id.ivThumb) ImageView mIvThumb;
    @InjectView(R.id.tvTrackName) TextView mTvTrackName;
    @InjectView(R.id.sbTrack) SeekBar mSbTrack;
    @InjectView(R.id.tvCurrentTime) TextView mTvCurrentTime;
    @InjectView(R.id.tvDuration) TextView mTvDuration;
    @InjectView(R.id.ibPrevious) ImageButton mIbPrevious;
    @InjectView(R.id.ibPlayPause) ImageButton mIbPlayPause;
    @InjectView(R.id.ibNext) ImageButton mIbNext;
    @InjectView(R.id.ibShare) ImageButton mIbShare;

    private ArrayList<ParcelableTrack> mTopTracks;
    private int mTopTrackSelected;
    private boolean mIsPlaying = true;

    private StreamerService mService;
    private boolean mBound = false;
    private Handler mHandler = new Handler();
    private boolean mNeedsToRefreshFromService = false;

    public StreamingFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_streaming, container, false);
        ButterKnife.inject(this, view);
        if(!(getActivity() instanceof StreamingActivity)){
            mIbShare.setVisibility(View.VISIBLE);
        }
        //Creating handlers of UI buttons
        mIbPrevious.setOnClickListener(this);
        mIbPlayPause.setOnClickListener(this);
        mIbNext.setOnClickListener(this);
        mIbShare.setOnClickListener(this);
        mSbTrack.setOnSeekBarChangeListener(this);
        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        // request a window without the title
        if (dialog.getWindow()!=null) dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState==null){
            this.mTopTracks = getArguments().getParcelableArrayList(EXTRA_TOP_TRACKS);
            this.mTopTrackSelected = getArguments().getInt(EXTRA_TOP_TRACK_SELECTED);
            if(mTopTracks==null){
                mNeedsToRefreshFromService = true;
            }
        }else{
            this.mTopTracks = savedInstanceState.getParcelableArrayList(EXTRA_TOP_TRACKS);
            this.mTopTrackSelected = savedInstanceState.getInt(EXTRA_TOP_TRACK_SELECTED);
        }
        fillUI();
    }

    private void fillUI() {
        mSbTrack.setProgress(0);
        mTvCurrentTime.setText("");
        mTvDuration.setText("");
        if (mTopTracks!=null){
            ParcelableTrack track = mTopTracks.get(mTopTrackSelected);
            mTvArtist.setText(track.getArtist());
            Picasso.with(getActivity())
                    .load(track.getImageUrlLarge())
                    .into(mIvThumb);
            mTvAlbum.setText(track.getAlbumName());
            mTvTrackName.setText(track.getName());
        }
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to StreamerBinder, cast the IBinder and get StreamerBinder instance
            StreamerService.StreamerBinder binder = (StreamerService.StreamerBinder) service;
            mService = binder.getService();
            //The intent is here only to make the bound service persistent
            Intent intent = new Intent(getActivity(), StreamerService.class);
            intent.setAction(StreamerService.ACTION_STAY_AWAKE);
            getActivity().startService(intent);
            if(mNeedsToRefreshFromService){
                mTopTracks = mService.getTopTracks();
                mTopTrackSelected = mService.getTopTrackSelected();
                fillUI();
            }else {
                mService.loadSong(mTopTracks,mTopTrackSelected);
            }
            if(getActivity() instanceof StreamingCallbacks){
                ((StreamingCallbacks)getActivity()).onContentLoaded(mTopTracks.get(mTopTrackSelected).getExternalSpotifyUrl());
            }
            mService.hideNotification();
            mBound = true;
            listenPlayerState();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    private void listenPlayerState() {
        mService.setOnStreamerStateChangeListener(StreamingFragment.this);
        if (!mService.isPlaying()&&mService.isPlayerPrepared()){
            onPlayEnding();
        }else if(mService.isPlayerPrepared()){
            onPlaying(mService.getDuration());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(EXTRA_TOP_TRACKS,this.mTopTracks);
        outState.putInt(EXTRA_TOP_TRACK_SELECTED,this.mTopTrackSelected);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mBound) {
            if (mService.isPlaying()){
                mService.showNotification();
            }else{
                mService.stopSelf();
            }
            getActivity().unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(getActivity(), StreamerService.class);
        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ibPrevious:
                if(mTopTracks!=null){
                    this.mTopTrackSelected-=1;
                    if(mTopTrackSelected<0){
                        mTopTrackSelected = mTopTracks.size()-1;
                    }
                    fillUI();
                    if(mService!=null&&!mService.isPlayerEmpty()){
                        mService.playPreviousSong();
                    }else{
                        if (mService!=null){
                            mService.loadSong(mTopTracks,mTopTrackSelected);
                        }
                    }
                }
                break;
            case R.id.ibNext:
                if(mTopTracks!=null){
                    this.mTopTrackSelected+=1;
                    if(mTopTrackSelected==mTopTracks.size()){
                        mTopTrackSelected = 0;
                    }
                    fillUI();
                    if(mService!=null&&!mService.isPlayerEmpty()){
                        mService.playNextSong();
                    }else{
                        if (mService!=null){
                            mService.loadSong(mTopTracks,mTopTrackSelected);
                        }
                    }
                }
                break;
            case R.id.ibPlayPause:
                if(mService!=null&&mService.isPlayerPrepared()){
                    mService.togglePlayPause();
                }else{
                    if (mService!=null){
                        mService.loadSong(mTopTracks,mTopTrackSelected);
                    }
                }
                togglePlayPauseButton();
                listenPlayerState();
                break;
            case R.id.ibShare:
                Intent i = createShareIntent(mTopTracks.get(mTopTrackSelected).getExternalSpotifyUrl());
                startActivity(Intent.createChooser(i,null));
                break;
        }
    }

    @SuppressWarnings("deprecation")
    private Intent createShareIntent(String shareString) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        }else{
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        }
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                shareString + StreamingActivity.SPOTIFY_STREAMER_SHARE_HASHTAG);
        return shareIntent;
    }

    private void togglePlayPauseButton() {
        mIsPlaying = !mIsPlaying;
        if(!mIsPlaying){
            mIbPlayPause.setImageResource(android.R.drawable.ic_media_play);
        }else {
            mIbPlayPause.setImageResource(android.R.drawable.ic_media_pause);
        }
    }

    @Override
    public void onPlaying(int duration) {
        mSbTrack.setVisibility(View.VISIBLE);
        mSbTrack.setMax(duration);
        mTvCurrentTime.setText(formatTime(0));
        mTvDuration.setText(formatTime(duration));
        if (getActivity()!=null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mService != null&&getActivity()!=null) {
                        if (mService.isPlaying()) {
                            int mCurrentPosition = mService.getCurrentTime();
                            mTvCurrentTime.setText(formatTime(mCurrentPosition));
                            mSbTrack.setProgress(mCurrentPosition);
                            mHandler.postDelayed(this, 16);
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onPlayEnding() {
        mIsPlaying=false;
        mSbTrack.setProgress(0);
        mTvCurrentTime.setText(formatTime(0));
        mIbPlayPause.setImageResource(android.R.drawable.ic_media_play);
    }

    private String formatTime(int time){
        int formatId = R.string.format_time;
        if (getActivity()!=null){
            return getActivity().getString(
                    formatId,
                    time / 60000,
                    (time / 1000) % 60);
        }else{
            return null;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (mIsPlaying&&fromUser){
            if(mService!=null){
                mService.seekTo(progress);
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public interface StreamingCallbacks{
        void onContentLoaded(String shareString);
    }
}
