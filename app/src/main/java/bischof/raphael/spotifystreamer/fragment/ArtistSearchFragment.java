/*
 * Copyright (C) 2015 Raphaël Bischof
 */

package bischof.raphael.spotifystreamer.fragment;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import bischof.raphael.spotifystreamer.R;
import bischof.raphael.spotifystreamer.activity.TopTracksActivity;
import bischof.raphael.spotifystreamer.adapter.ArtistAdapter;
import bischof.raphael.spotifystreamer.async.ArtistLoader;
import bischof.raphael.spotifystreamer.async.OnContentLoadedListener;
import kaaes.spotify.webapi.android.models.Artist;

/**
 * Fragment containing the {@link EditText} used to search an artist and a {@link ListView} to show results.
 * ListView uses an {@link ArtistAdapter} to display items.
 * Created by biche on 10/06/2015.
 */
public class ArtistSearchFragment extends Fragment {

    private static final String ET_SAVED = "EditTextSavedInstanceState";
    private ArtistAdapter mLvArtistAdapter;
    private EditText mEtArtist;
    private ArtistLoader mLoader;
    private Toast mToast;

    public ArtistSearchFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_artist_search, container, false);
        //Once the view is inflated, store UI components in fields to use them later
        ListView lvArtist = (ListView) v.findViewById(R.id.lvArtist);
        mEtArtist = (EditText)v.findViewById(R.id.etArtist);

        //Get the size of desired bitmap to know which image to load in ImageView later
        //The goal is to avoid big bitmaps download and to load image at the best quality that can be displayed
        int sizeOfImageToLoad = (int) getResources().getDimension(R.dimen.tile_height_avatar_with_one_line_text);

        //Fill UI with empty adapter
        mLvArtistAdapter = new ArtistAdapter(getActivity(),new ArrayList<Artist>(),sizeOfImageToLoad);
        lvArtist.setAdapter(mLvArtistAdapter);

        //Set the listeners on UI components
        mEtArtist.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //intentionally does nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchArtist(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                //intentionally does nothing
            }
        });
        lvArtist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getActivity(), TopTracksActivity.class);
                i.putExtra(Intent.EXTRA_TEXT, mLvArtistAdapter.getItem(position).id);
                i.putExtra(Intent.EXTRA_TITLE, mLvArtistAdapter.getItem(position).name);
                startActivity(i);
            }
        });
        return v;
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState!=null){
            mEtArtist.setText(savedInstanceState.getString(ET_SAVED));
        }
        //TODO: Back button restart completely the activity
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ET_SAVED,mEtArtist.getText().toString());
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
            mLoader = new ArtistLoader();
            mLoader.setOnContentLoadedListener(new OnContentLoadedListener<List<Artist>>() {
                @Override
                public void onContentLoaded(List<Artist> content) {
                    if (content.size()==0){
                        if (mToast!=null){
                            mToast.cancel();
                            mToast = null;
                        }
                        mToast = Toast.makeText(getActivity(), getString(R.string.refine_search), Toast.LENGTH_SHORT);
                        mToast.show();
                    }
                    mLvArtistAdapter.clear();
                    mLvArtistAdapter.addAll(content);
                }

                @Override
                public void onContentError(String errorMessage) {
                    if (mToast!=null){
                        mToast.cancel();
                        mToast = null;
                    }
                    mToast = Toast.makeText(getActivity(), getString(R.string.connection_needed), Toast.LENGTH_SHORT);
                    mToast.show();
                }
            });
            mLoader.execute(artist);
        }
    }

}
