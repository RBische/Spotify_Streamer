package bischof.raphael.spotifystreamer.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;

/**
 * Simplified artist from Spotify that can be saved in a bundle
 * Created by biche on 11/06/2015.
 */
public class ParcelableArtist implements Parcelable {
    private String name;
    private String imageUrl;
    private String id;

    public ParcelableArtist(String name, String imageUrl, String id) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.id = id;
    }

    private ParcelableArtist(Parcel in) {
        this.name = in.readString();
        this.imageUrl = in.readString();
        this.id = in.readString();
    }

    public static final Creator<ParcelableArtist> CREATOR = new Creator<ParcelableArtist>() {
        @Override
        public ParcelableArtist createFromParcel(Parcel in) {
            return new ParcelableArtist(in);
        }

        @Override
        public ParcelableArtist[] newArray(int size) {
            return new ParcelableArtist[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(imageUrl);
        dest.writeString(id);
    }

    public static ArrayList<ParcelableArtist> convertFromArtistList(List<Artist> artists, int sizeOfImageToLoad){
        ArrayList<ParcelableArtist> parcelableArtists = new ArrayList<>();
        for (Artist artist:artists){
            String imageUrl = null;
            if(artist.images!=null){
                if(artist.images.size()>0){
                    //Take the smallest size of bitmap but > sizeImageToLoad or the first one if no other are fitting better
                    int indexKeeped = -1;
                    for(int i=0;i<artist.images.size();i++){
                        if (indexKeeped==-1){
                            indexKeeped=i;
                        }else if (artist.images.get(i).height*artist.images.get(i).width> sizeOfImageToLoad * sizeOfImageToLoad){
                            if(artist.images.get(i).height*artist.images.get(i).width<artist.images.get(indexKeeped).height*artist.images.get(indexKeeped).width){
                                indexKeeped=i;
                            }
                        }
                    }
                    imageUrl = artist.images.get(indexKeeped).url;
                }
            }
            ParcelableArtist parcelableArtist = new ParcelableArtist(artist.name,imageUrl,artist.id);
            parcelableArtists.add(parcelableArtist);
        }
        return parcelableArtists;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
