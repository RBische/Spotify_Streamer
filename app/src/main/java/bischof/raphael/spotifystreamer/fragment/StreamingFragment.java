package bischof.raphael.spotifystreamer.fragment;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;

import bischof.raphael.spotifystreamer.R;
import bischof.raphael.spotifystreamer.activity.StreamingActivity;
import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * A placeholder fragment containing a simple view.
 */
public class StreamingFragment extends Fragment implements View.OnClickListener {

    private MediaPlayer mMediaPlayer;
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

    public StreamingFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_streaming, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Creating handlers of UI buttons
        mIbPrevious.setOnClickListener(this);
        mIbPlayPause.setOnClickListener(this);
        mIbNext.setOnClickListener(this);

        //Initializing the MediaPlayer
        String url = getActivity().getIntent().getStringExtra(StreamingActivity.EXTRA_PREVIEW_URL);
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mMediaPlayer.setDataSource(url);
            mMediaPlayer.prepare(); // might take long! (for buffering, etc)
            mMediaPlayer.start();
        } catch (IOException | IllegalArgumentException e) {
            //TODO: fill exception
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //According to android's activity lifecycle, the mediaplyer is stopped directly if activity pauses, because in some cases onStop or onDestroy are never called in an activity
        mMediaPlayer.stop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ibPrevious:
                break;
            case R.id.ibNext:
                break;
            case R.id.ibPlayPause:
                if (mMediaPlayer.isPlaying()){
                    mMediaPlayer.pause();
                }else{
                    mMediaPlayer.start();
                }
                break;
        }
    }
}
