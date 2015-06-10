package bischof.raphael.spotifystreamer.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import bischof.raphael.spotifystreamer.R;
import bischof.raphael.spotifystreamer.database.SpotifyDB;
import bischof.raphael.spotifystreamer.picasso.CircleTransform;

/**
 * Displays a list of top tracks.
 * Created by biche on 10/06/2015.
 */
public class TopTracksAdapter extends CursorAdapter {
    public TopTracksAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.adapter_thumb_two_line_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tvTrack = (TextView) view.findViewById(R.id.tvText1);
        tvTrack.setText(cursor.getString(cursor.getColumnIndexOrThrow(SpotifyDB.KEY_TOP_TRACKS_NAME)));
        TextView tvAlbum = (TextView) view.findViewById(R.id.tvText2);
        tvAlbum.setText(cursor.getString(cursor.getColumnIndexOrThrow(SpotifyDB.KEY_TOP_TRACKS_ALBUM)));
        ImageView imageView = (ImageView)view.findViewById(R.id.ivThumb);
        String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(SpotifyDB.KEY_TOP_TRACKS_ALBUM_IMAGE_URL));
        Picasso.with(context).load(imageUrl)
                .transform(new CircleTransform())
                .placeholder(R.drawable.ic_cd_placeholder).into(imageView);
    }
}
