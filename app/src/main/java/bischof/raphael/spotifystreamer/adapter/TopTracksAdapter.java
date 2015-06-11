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
import butterknife.ButterKnife;
import butterknife.InjectView;

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
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = mInflater.inflate(R.layout.adapter_thumb_two_line_item, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }

        ParcelableTrack track = getItem(position);

        holder.tvText1.setText(track.getName());
        holder.tvText2.setText(track.getAlbumName());
        if (track.getImageUrlSmall()!=null){
            Picasso.with(mContext).load(track.getImageUrlSmall())
                    .transform(new CircleTransform())
                    .placeholder(R.drawable.ic_cd_placeholder).into(holder.ivThumb);
        }
        return view;
    }

    static class ViewHolder {
        @InjectView(R.id.tvText1) TextView tvText1;
        @InjectView(R.id.tvText2) TextView tvText2;
        @InjectView(R.id.ivThumb) ImageView ivThumb;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
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
