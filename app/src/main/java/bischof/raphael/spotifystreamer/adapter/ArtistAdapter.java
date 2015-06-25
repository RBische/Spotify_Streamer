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
import bischof.raphael.spotifystreamer.model.ParcelableArtist;
import bischof.raphael.spotifystreamer.picasso.CircleTransform;
import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Displays a list of artists
 * Created by biche on 10/06/2015.
 */
public class ArtistAdapter extends BaseAdapter {

    /**
     * Containing all the displayed artists
     */
    private ArrayList<ParcelableArtist> mArtists;
    private Context mContext;
    private LayoutInflater mInflater;

    /**
     * Constructor
     *
     * @param context The current context.
     * @param artists The objects to represent in the ListView.
     */
    public ArtistAdapter(Context context, ArrayList<ParcelableArtist> artists) {
        this.mArtists = artists;
        this.mContext = context;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mArtists.size();
    }

    @Override
    public ParcelableArtist getItem(int position) {
        return mArtists.get(position);
    }

    @Override
    public long getItemId(int position) {
        //Like in ArrayAdapter, position is used to differentiate items
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ParcelableArtist artist = getItem(position);
        ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = mInflater.inflate(R.layout.adapter_thumb_item, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }

        holder.tvText1.setText(artist.getName());
        if (artist.getImageUrl()!=null){
            Picasso.with(mContext)
                    .load(artist.getImageUrl())
                    .transform(new CircleTransform())
                    .placeholder(R.drawable.ic_cd_placeholder)
                    .into(holder.ivThumb);
        }
        return view;
    }

    public ArrayList<ParcelableArtist> getArtists() {
        return mArtists;
    }

    static class ViewHolder {
        @InjectView(R.id.tvText1) TextView tvText1;
        @InjectView(R.id.ivThumb) ImageView ivThumb;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

    /**
     * Remove all elements from the list.
     */
    public void clear(){
        mArtists.clear();
        notifyDataSetChanged();
    }

    /**
     * Adds the specified Collection at the end of the array.
     *
     * @param artists The Collection to add at the end of the array.
     */
    public void addAll(ArrayList<ParcelableArtist> artists){
        mArtists.addAll(artists);
        notifyDataSetChanged();
    }
}
