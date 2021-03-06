/*
 * Copyright (C) 2015 Raphaël Bischof
 */

package bischof.raphael.spotifystreamer.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import bischof.raphael.spotifystreamer.R;
import bischof.raphael.spotifystreamer.adapter.TopTracksAdapter;
import bischof.raphael.spotifystreamer.async.OnContentLoadedListener;
import bischof.raphael.spotifystreamer.async.TopTracksLoader;
import bischof.raphael.spotifystreamer.model.ParcelableTrack;
import bischof.raphael.spotifystreamer.service.StreamerService;
import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Fragment containing a {@link ListView} to show top tracks.
 * ListView uses an {@link TopTracksAdapter} to display items.
 * Created by biche on 11/06/2015.
 */
public class TopTracksFragment extends Fragment implements AdapterView.OnItemClickListener {

    private static final String LOG_TAG = TopTracksFragment.class.getSimpleName();
    private static final String LV_SAVED = "LvItemsToSave";
    public static final String ARG_TITLE = "Title";
    public static final String ARG_MUST_FILL_UI_WITH_DATAS = "FillUIXWithDatas";
    public static final String ARG_ARTIST_ID = "ArtistName";
    private TopTracksAdapter mLvTopTracksAdapter;
    private Toast mToast;
    private Callbacks mCallbacks;
    @InjectView(R.id.lvTopTracks) ListView mLvTopTracks;

    public TopTracksFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatActivity activity = ((AppCompatActivity)getActivity());
        ActionBar actionBar = activity.getSupportActionBar();
        String title = getArguments().getString(ARG_TITLE);
        if(actionBar!=null)
        actionBar.setSubtitle(title);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_top_tracks, container, false);
        ButterKnife.inject(this, v);

        if(savedInstanceState==null){
            mLvTopTracksAdapter = new TopTracksAdapter(getActivity(),new ArrayList<ParcelableTrack>());
            mLvTopTracks.setAdapter(mLvTopTracksAdapter);
            searchTopTracks();
        }
        mLvTopTracks.setOnItemClickListener(this);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getArguments().getBoolean(ARG_MUST_FILL_UI_WITH_DATAS)&&savedInstanceState==null){
            ArrayList<ParcelableTrack> tracks = getArguments().getParcelableArrayList(StreamerService.EXTRA_TOP_TRACKS);
            mLvTopTracksAdapter = new TopTracksAdapter(getActivity(),tracks);
            mLvTopTracks.setAdapter(mLvTopTracksAdapter);
            showPlayer(getArguments().getInt(StreamerService.EXTRA_TOP_TRACK_SELECTED, 0));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity instanceof Callbacks){
            mCallbacks = (Callbacks) activity;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(LV_SAVED,mLvTopTracksAdapter.getTracks());
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState!=null&&savedInstanceState.containsKey(LV_SAVED)){
            ArrayList<ParcelableTrack> tracks = savedInstanceState.getParcelableArrayList(LV_SAVED);
            mLvTopTracksAdapter = new TopTracksAdapter(getActivity(),tracks);
            mLvTopTracks.setAdapter(mLvTopTracksAdapter);
        }
    }

    /**
     * Search and load top tracks for an artist specified in ARG_ARTIST_ID
     */
    private void searchTopTracks() {
        //Get the size of desired bitmap to know which image to load in ImageView later
        //The goal is to avoid big bitmaps download and to load image at the best quality that can be displayed
        int sizeOfImageToLoad = (int) getResources().getDimension(R.dimen.tile_height_avatar_with_one_line_text);

        TopTracksLoader loader = new TopTracksLoader(sizeOfImageToLoad,getActivity());
        loader.setOnContentLoadedListener(new OnContentLoadedListener<ArrayList<ParcelableTrack>>() {
            @Override
            public void onContentLoaded(ArrayList<ParcelableTrack> content) {
                try{
                    if (content.size()==0){
                        if (mToast!=null){
                            mToast.cancel();
                            mToast = null;
                        }
                        mToast = Toast.makeText(getActivity(), getString(R.string.no_top_tracks), Toast.LENGTH_SHORT);
                        mToast.show();
                    }else{
                        mLvTopTracksAdapter.clear();
                        mLvTopTracksAdapter.addAll(content);
                    }
                }catch (IllegalStateException e){
                    Log.d(LOG_TAG, "Activity (context) seems to have been recycled. " + e.getMessage());
                }
            }

            @Override
            public void onContentError(String errorMessage) {
                try {
                    if (mToast!=null){
                        mToast.cancel();
                        mToast = null;
                    }
                    mToast = Toast.makeText(getActivity(), getString(R.string.connection_needed), Toast.LENGTH_SHORT);
                    mToast.show();
                }catch (IllegalStateException e){
                    Log.d(LOG_TAG, "Activity (context) seems to have been recycled. " + e.getMessage());
                }
            }
        });
        loader.execute(getArguments().getString(ARG_ARTIST_ID));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        showPlayer(position);
    }

    private void showPlayer(int position) {
        if(mCallbacks!=null){
            mCallbacks.onAskToShowPlayer(mLvTopTracksAdapter.getTracks(),position);
        }
    }

    public interface Callbacks {
        void onAskToShowPlayer(ArrayList<ParcelableTrack> tracks, int topTrackSelected);
    }
}
