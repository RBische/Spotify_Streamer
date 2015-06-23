package bischof.raphael.spotifystreamer.fragment;

import android.content.Intent;
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

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

import bischof.raphael.spotifystreamer.R;
import bischof.raphael.spotifystreamer.activity.StreamingActivity;
import bischof.raphael.spotifystreamer.model.ParcelableTrack;
import bischof.raphael.spotifystreamer.service.StreamerService;
import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * A placeholder fragment containing a simple view.
 */
public class StreamingFragment extends Fragment implements View.OnClickListener {

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

    private ArrayList<ParcelableTrack> mTopTracks;
    private int mTopTrackSelected;

    public StreamingFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_streaming, container, false);
        ButterKnife.inject(this, view);
        //Creating handlers of UI buttons
        mIbPrevious.setOnClickListener(this);
        mIbPlayPause.setOnClickListener(this);
        mIbNext.setOnClickListener(this);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState==null){
            this.mTopTracks = getArguments().getParcelableArrayList(EXTRA_TOP_TRACKS);
            this.mTopTrackSelected = getArguments().getInt(EXTRA_TOP_TRACK_SELECTED);
            Intent intent = new Intent(getActivity(), StreamerService.class);
            intent.setAction(StreamerService.ACTION_LOAD_SONG);
            intent.putExtra(StreamerService.EXTRA_TOP_TRACKS,this.mTopTracks);
            intent.putExtra(StreamerService.EXTRA_TOP_TRACK_SELECTED,this.mTopTrackSelected);
            getActivity().startService(intent);
        }else{
            this.mTopTracks = savedInstanceState.getParcelableArrayList(EXTRA_TOP_TRACKS);
            this.mTopTrackSelected = savedInstanceState.getInt(EXTRA_TOP_TRACK_SELECTED);
        }
        fillUI();
    }

    private void fillUI() {
        ParcelableTrack track = mTopTracks.get(mTopTrackSelected);
        mTvArtist.setText(track.getArtist());
        Picasso.with(getActivity())
            .load(track.getImageUrlLarge())
            .into(mIvThumb);
        mTvAlbum.setText(track.getAlbumName());
        mTvTrackName.setText(track.getName());
        int formatId = R.string.format_time;
        String duration = getActivity().getString(
                formatId,
                track.getDuration()/60000,
                (track.getDuration()/1000)%60);
        mTvDuration.setText(duration);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(EXTRA_TOP_TRACKS,this.mTopTracks);
        outState.putInt(EXTRA_TOP_TRACK_SELECTED,this.mTopTrackSelected);
    }

    @Override
    public void onPause() {
        super.onPause();
        Intent intent = new Intent(getActivity(), StreamerService.class);
        intent.setAction(StreamerService.ACTION_TOGGLE_NOTIFICATION);
        getActivity().startService(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent intent = new Intent(getActivity(), StreamerService.class);
        intent.setAction(StreamerService.ACTION_TOGGLE_NOTIFICATION);
        getActivity().startService(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ibPrevious:
                Intent intentPrevious = new Intent(getActivity(), StreamerService.class);
                intentPrevious.setAction(StreamerService.ACTION_PREVIOUS_SONG);
                getActivity().startService(intentPrevious);
                break;
            case R.id.ibNext:
                Intent intentNext = new Intent(getActivity(), StreamerService.class);
                intentNext.setAction(StreamerService.ACTION_NEXT_SONG);
                getActivity().startService(intentNext);
                break;
            case R.id.ibPlayPause:
                Intent intent = new Intent(getActivity(), StreamerService.class);
                intent.setAction(StreamerService.ACTION_TOGGLE_PLAY_PAUSE);
                getActivity().startService(intent);
                break;
        }
    }
}
