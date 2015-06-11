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

import java.util.List;

import bischof.raphael.spotifystreamer.R;
import bischof.raphael.spotifystreamer.picasso.CircleTransform;
import kaaes.spotify.webapi.android.models.Artist;

/**
 * Displays a list of artists
 * Created by biche on 10/06/2015.
 */
public class ArtistAdapter extends BaseAdapter {
    private List<Artist> mArtists;
    private Context mContext;
    private LayoutInflater mInflater;
    private int mSizeOfImageToLoad;

    public ArtistAdapter(Context context, List<Artist> artists, int sizeOfImageToLoad) {
        this.mArtists = artists;
        this.mContext = context;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mSizeOfImageToLoad = sizeOfImageToLoad;
    }

    @Override
    public int getCount() {
        return mArtists.size();
    }

    @Override
    public Artist getItem(int position) {
        return mArtists.get(position);
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
        ImageView ivThumb;
        Artist artist = getItem(position);

        if (convertView == null) {
            view = mInflater.inflate(R.layout.adapter_thumb_item, parent, false);
        } else {
            view = convertView;
        }

        tvText1 = (TextView) view.findViewById(R.id.tvText1);
        ivThumb = (ImageView) view.findViewById(R.id.ivThumb);

        tvText1.setText(artist.name);
        String imageUrl = null;
        if(artist.images!=null){
            if(artist.images.size()>0){
                //Take the smallest size of bitmap but > sizeImageToLoad or the first one if no other are fitting better
                int indexKeeped = -1;
                for(int i=0;i<artist.images.size();i++){
                    if (indexKeeped==-1){
                        indexKeeped=i;
                    }else if (artist.images.get(i).height*artist.images.get(i).width> mSizeOfImageToLoad * mSizeOfImageToLoad){
                        if(artist.images.get(i).height*artist.images.get(i).width<artist.images.get(indexKeeped).height*artist.images.get(indexKeeped).width){
                            indexKeeped=i;
                        }
                    }
                }
                imageUrl = artist.images.get(indexKeeped).url;
            }
        }
        Picasso.with(mContext)
                .load(imageUrl)
                .transform(new CircleTransform())
                .placeholder(R.drawable.ic_cd_placeholder)
                .into(ivThumb);
        return view;
    }

    public void clear(){
        mArtists.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Artist> artists){
        mArtists.addAll(artists);
        notifyDataSetChanged();
    }
}
