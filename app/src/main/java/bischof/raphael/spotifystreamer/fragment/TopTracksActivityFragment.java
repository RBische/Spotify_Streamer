package bischof.raphael.spotifystreamer.fragment;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import bischof.raphael.spotifystreamer.R;
import bischof.raphael.spotifystreamer.adapter.TopTracksAdapter;
import bischof.raphael.spotifystreamer.async.OnContentLoadedListener;
import bischof.raphael.spotifystreamer.async.TopTracksLoader;

/**
 * Fragment containing a {@link ListView} to show top tracks.
 * ListView uses an {@link TopTracksAdapter} to display items.
 */
public class TopTracksActivityFragment extends Fragment {

    private ListView mLvTopTracks;
    private Toast mToast;

    public TopTracksActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatActivity activity = ((AppCompatActivity)getActivity());
        ActionBar actionBar = activity.getSupportActionBar();
        String title = getActivity().getIntent().getStringExtra(Intent.EXTRA_TITLE);
        if(actionBar!=null)
        actionBar.setSubtitle(title);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_top_tracks, container, false);
        mLvTopTracks = (ListView)v.findViewById(R.id.lvTopTracks);
        TopTracksLoader loader = new TopTracksLoader(getActivity());
        loader.setOnContentLoadedListener(new OnContentLoadedListener<TopTracksAdapter>() {
            @Override
            public void onContentLoaded(TopTracksAdapter content) {
                if (content.getCount()==0){
                    if (mToast!=null){
                        mToast.cancel();
                        mToast = null;
                    }
                    mToast = Toast.makeText(getActivity(), getString(R.string.no_top_tracks), Toast.LENGTH_SHORT);
                    mToast.show();
                }else{
                    mLvTopTracks.setAdapter(content);
                }
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
        loader.execute(getActivity().getIntent().getStringExtra(Intent.EXTRA_TEXT));
        return v;
    }
}
