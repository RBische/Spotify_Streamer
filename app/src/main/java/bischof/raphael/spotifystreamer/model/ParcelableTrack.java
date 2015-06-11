/*
 * Copyright (C) 2015 Raphaël Bischof
 */

package bischof.raphael.spotifystreamer.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Simplified track from Spotify that can be saved in a bundle
 * Created by biche on 11/06/2015.
 */
public class ParcelableTrack implements Parcelable {
    private String name;
    private String albumName;
    private String imageUrlSmall;
    private String imageUrlLarge;
    private String previewUrl;

    public ParcelableTrack(String name, String albumName, String imageUrlSmall, String imageUrlLarge, String previewUrl) {
        this.name = name;
        this.albumName = albumName;
        this.previewUrl = previewUrl;
        this.imageUrlSmall = imageUrlSmall;
        this.imageUrlLarge = imageUrlLarge;
    }

    private ParcelableTrack(Parcel in) {
        this.name = in.readString();
        this.albumName = in.readString();
        this.previewUrl = in.readString();
        this.imageUrlSmall = in.readString();
        this.imageUrlLarge = in.readString();
    }

    public static final Creator<ParcelableTrack> CREATOR = new Creator<ParcelableTrack>() {
        @Override
        public ParcelableTrack createFromParcel(Parcel in) {
            return new ParcelableTrack(in);
        }

        @Override
        public ParcelableTrack[] newArray(int size) {
            return new ParcelableTrack[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(albumName);
        dest.writeString(previewUrl);
        dest.writeString(imageUrlSmall);
        dest.writeString(imageUrlLarge);
    }

    public static ArrayList<ParcelableTrack> convertFromTrackList(List<Track> tracks, int sizeOfImageToLoad){
        ArrayList<ParcelableTrack> parcelableTracks = new ArrayList<>();
        for(Track track:tracks){

            String imageUrlSmall = null;
            String imageUrlLarge = null;
            if(track.album.images!=null){
                if(track.album.images.size()>0){
                    int indexKeepedSmall = -1;
                    int indexKeepedLarge = -1;
                    for(int i=0;i<track.album.images.size();i++){
                        if(track.album.images.get(i).width==640||track.album.images.get(i).height==640){
                            indexKeepedLarge = i;
                        } else if(track.album.images.get(i).width==200||track.album.images.get(i).height==200){
                            indexKeepedSmall = i;
                        }
                    }
                    if (indexKeepedLarge!=-1){
                        imageUrlLarge = track.album.images.get(indexKeepedLarge).url;
                    }else {
                        imageUrlLarge = track.album.images.get(0).url;
                    }
                    if (indexKeepedSmall!=-1){
                        imageUrlSmall = track.album.images.get(indexKeepedSmall).url;
                    }else {
                        //Take the smallest size of bitmap but > sizeImageToLoad or the first one if no other are fitting better
                        int indexKeeped = -1;
                        for(int i=0;i<track.album.images.size();i++){
                            if (indexKeeped==-1){
                                indexKeeped=i;
                            }else if (track.album.images.get(i).height*track.album.images.get(i).width> sizeOfImageToLoad * sizeOfImageToLoad){
                                if(track.album.images.get(i).height*track.album.images.get(i).width<track.album.images.get(indexKeeped).height*track.album.images.get(indexKeeped).width){
                                    indexKeeped=i;
                                }
                            }
                        }
                        imageUrlSmall = track.album.images.get(indexKeeped).url;
                    }
                }
            }
            ParcelableTrack parcelableTrack = new ParcelableTrack(track.name,track.album.name,imageUrlSmall,imageUrlLarge,track.preview_url);
            parcelableTracks.add(parcelableTrack);
        }
        return parcelableTracks;
    }

    public String getName() {
        return name;
    }

    public String getAlbumName() {
        return albumName;
    }

    public String getImageUrlSmall() {
        return imageUrlSmall;
    }
}
