/*
 * Copyright (C) 2015 Raphaël Bischof
 */

package bischof.raphael.spotifystreamer.fragment;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import bischof.raphael.spotifystreamer.R;
import bischof.raphael.spotifystreamer.activity.TopTracksActivity;
import bischof.raphael.spotifystreamer.adapter.ArtistAdapter;
import bischof.raphael.spotifystreamer.async.ArtistLoader;
import bischof.raphael.spotifystreamer.async.OnContentLoadedListener;
import bischof.raphael.spotifystreamer.model.ParcelableArtist;
import bischof.raphael.spotifystreamer.model.ParcelableTrack;
import bischof.raphael.spotifystreamer.service.StreamerService;
import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Fragment containing the {@link EditText} used to search an artist and a {@link ListView} to show results.
 * ListView uses an {@link ArtistAdapter} to display items.
 * Created by biche on 10/06/2015.
 */
public class ArtistSearchFragment extends Fragment {

    private static final String ET_SAVED = "EditTextSavedInstanceState";
    private static final String LV_SAVED = "LvItemsToSave";
    private static final String SAVE_SCROLL_POSITION = "SaveScrollPosition";

    private static final String LOG_TAG = ArtistSearchFragment.class.getSimpleName();

    @InjectView(R.id.etArtist) EditText mEtArtist;
    @InjectView(R.id.lvArtist) ListView mLvArtist;

    private ArtistAdapter mLvArtistAdapter;
    private ArtistLoader mLoader;
    private Toast mToast;
    private boolean mTriggerTextChange = true;
    private Callbacks mCallbacks;
    private int mPosition = ListView.INVALID_POSITION;

    public ArtistSearchFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity instanceof Callbacks){
            mCallbacks = (Callbacks) activity;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_artist_search, container, false);
        ButterKnife.inject(this, v);

        //Fill UI with empty adapter
        mLvArtistAdapter = new ArtistAdapter(getActivity(),new ArrayList<ParcelableArtist>());
        mLvArtist.setAdapter(mLvArtistAdapter);

        //Set the listeners on UI components
        mEtArtist.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //intentionally does nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(mTriggerTextChange){
                    searchArtist(s.toString());
                }else{
                    mTriggerTextChange = true;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                //intentionally does nothing
            }
        });
        mLvArtist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showTopTracks(mLvArtistAdapter.getItem(position).getName(), mLvArtistAdapter.getItem(position).getId());
            }
        });
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity().getIntent().getAction().equals(StreamerService.ACTION_SHOW_UI_FROM_SONG)&&savedInstanceState==null){
            ArrayList<ParcelableTrack> tracks = getActivity().getIntent().getParcelableArrayListExtra(StreamerService.EXTRA_TOP_TRACKS);
            int topTrackSelected = getActivity().getIntent().getIntExtra(StreamerService.EXTRA_TOP_TRACK_SELECTED,0);
            String artist = null;
            String artistID = null;
            if (tracks.size()>topTrackSelected){
                artist = tracks.get(0).getArtist();
                artistID = tracks.get(0).getArtistId();
            }
            mEtArtist.setText(artist);
            showTopTracksEverLoaded(artist, artistID, tracks, topTrackSelected);
        }
    }

    private void showTopTracks(String name, String id) {
        if(mCallbacks!=null){
            mCallbacks.onAskToShowDetailFragment(name,id,null,0);
        }
    }

    private void showTopTracksEverLoaded(String name, String id, ArrayList<ParcelableTrack> tracks, int topTrackSelected){
        if(mCallbacks!=null){
            mCallbacks.onAskToShowDetailFragment(name,id,tracks,topTrackSelected);
        }
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState!=null){
            mTriggerTextChange = false;
            mEtArtist.setText(savedInstanceState.getString(ET_SAVED));
            if(savedInstanceState.containsKey(LV_SAVED)){
                mPosition = savedInstanceState.getInt(SAVE_SCROLL_POSITION);
                ArrayList<ParcelableArtist> artists = savedInstanceState.getParcelableArrayList(LV_SAVED);
                mLvArtistAdapter.clear();
                mLvArtistAdapter.addAll(artists);
                if (mPosition != ListView.INVALID_POSITION) {
                    // If we don't need to restart the loader, and there's a desired position to restore
                    // to, do so now.
                    mLvArtist.smoothScrollToPosition(mPosition);
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ET_SAVED,mEtArtist.getText().toString());
        outState.putParcelableArrayList(LV_SAVED,mLvArtistAdapter.getArtists());
        outState.putInt(SAVE_SCROLL_POSITION,mPosition);
    }

    /**
     * Search and load for a specified String artists corresponding
     * @param artist The specified artist
     */
    private void searchArtist(final String artist){
        if (artist!=null&&!artist.equals("")){
            if (mLoader != null) {
                mLoader.cancel(true);
            }

            //Get the size of desired bitmap to know which image to load in ImageView later
            //The goal is to avoid big bitmaps download and to load image at the best quality that can be displayed
            int sizeOfImageToLoad = (int) getResources().getDimension(R.dimen.tile_height_avatar_with_one_line_text);

            mLoader = new ArtistLoader(sizeOfImageToLoad);
            mLoader.setOnContentLoadedListener(new OnContentLoadedListener<ArrayList<ParcelableArtist>>() {
                @Override
                public void onContentLoaded(ArrayList<ParcelableArtist> content) {
                    try {
                        if (content.size() == 0) {
                            if (mToast != null) {
                                mToast.cancel();
                                mToast = null;
                            }
                            mToast = Toast.makeText(getActivity(), getString(R.string.refine_search), Toast.LENGTH_SHORT);
                            mToast.show();
                        }
                        mLvArtistAdapter.clear();
                        mLvArtistAdapter.addAll(content);
                    } catch (IllegalStateException e) {
                        Log.d(LOG_TAG, "Activity (context) seems to have been recycled. " + e.getMessage());
                    }
                }

                @Override
                public void onContentError(String errorMessage) {
                    try {
                        if (mToast != null) {
                            mToast.cancel();
                            mToast = null;
                        }
                        mToast = Toast.makeText(getActivity(), getString(R.string.connection_needed), Toast.LENGTH_SHORT);
                        mToast.show();
                    }
                    catch(IllegalStateException e)
                    {
                        Log.d(LOG_TAG, "Activity (context) seems to have been recycled. " + e.getMessage());
                    }
                }
            });
            mLoader.execute(artist);
        }
    }

    public interface Callbacks {
        void onAskToShowDetailFragment(String name, String id, ArrayList<ParcelableTrack> tracks, int topTrackSelected);
    }
}
