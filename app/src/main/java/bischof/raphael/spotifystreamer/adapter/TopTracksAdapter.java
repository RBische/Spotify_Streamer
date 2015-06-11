/*
 * Copyright (C) 2015 Raphaël Bischof
 */

package bischof.raphael.spotifystreamer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import bischof.raphael.spotifystreamer.R;
import bischof.raphael.spotifystreamer.model.ParcelableTrack;
import bischof.raphael.spotifystreamer.picasso.CircleTransform;

/**
 * Displays a list of top tracks.
 * Created by biche on 10/06/2015.
 */
public class TopTracksAdapter extends BaseAdapter {

    /**
     * Containing all the displayed tracks
     */
    private final ArrayList<ParcelableTrack> mTracks;
    private Context mContext;
    private LayoutInflater mInflater;

    /**
     * Constructor
     *
     * @param context The current context.
     * @param tracks The objects to represent in the ListView.
     */
    public TopTracksAdapter(Context context, ArrayList<ParcelableTrack> tracks) {
        this.mTracks = tracks;
        this.mContext = context;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mTracks.size();
    }

    @Override
    public ParcelableTrack getItem(int position) {
        return mTracks.get(position);
    }

    @Override
    public long getItemId(int position) {
        //Like in ArrayAdapter, position is used to differentiate items
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        TextView tvText1;
        TextView tvText2;
        ImageView ivThumb;
        ParcelableTrack track = getItem(position);

        if (convertView == null) {
            view = mInflater.inflate(R.layout.adapter_thumb_two_line_item, parent, false);
        } else {
            view = convertView;
        }

        tvText1 = (TextView) view.findViewById(R.id.tvText1);
        tvText2 = (TextView) view.findViewById(R.id.tvText2);
        ivThumb = (ImageView) view.findViewById(R.id.ivThumb);

        tvText1.setText(track.getName());
        tvText2.setText(track.getAlbumName());
        if (track.getImageUrlSmall()!=null){
            Picasso.with(mContext).load(track.getImageUrlSmall())
                    .transform(new CircleTransform())
                    .placeholder(R.drawable.ic_cd_placeholder).into(ivThumb);
        }
        return view;
    }

    /**
     * Remove all elements from the list.
     */
    public void clear(){
        mTracks.clear();
        notifyDataSetChanged();
    }

    /**
     * Adds the specified Collection at the end of the array.
     *
     * @param tracks The Collection to add at the end of the array.
     */
    public void addAll(ArrayList<ParcelableTrack> tracks){
        mTracks.addAll(tracks);
        notifyDataSetChanged();
    }

    public ArrayList<ParcelableTrack> getTracks() {
        return mTracks;
    }
}
