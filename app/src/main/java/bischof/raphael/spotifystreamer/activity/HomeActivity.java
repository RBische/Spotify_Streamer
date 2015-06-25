/*
 * Copyright (C) 2015 Raphaël Bischof
 */
package bischof.raphael.spotifystreamer.activity;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

import bischof.raphael.spotifystreamer.R;
import bischof.raphael.spotifystreamer.fragment.ArtistSearchFragment;
import bischof.raphael.spotifystreamer.fragment.StreamingFragment;
import bischof.raphael.spotifystreamer.fragment.TopTracksFragment;
import bischof.raphael.spotifystreamer.model.ParcelableTrack;
import bischof.raphael.spotifystreamer.service.ServiceStateReceiver;
import bischof.raphael.spotifystreamer.service.ServiceTools;
import bischof.raphael.spotifystreamer.service.StreamerService;

/*
 * Starting activty of the app
 * Created by biche on 10/06/2015.
 */
public class HomeActivity extends AppCompatActivity implements ArtistSearchFragment.Callbacks,TopTracksFragment.Callbacks,ServiceStateReceiver.OnServiceStateChangeListener {

    private static final String TAG_STREAMING_FRAGMENT = "StreamingFragment";
    //Is set to true if it's TabletUI
    private boolean mTwoPane = false;
    private ServiceStateReceiver mStateReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        if (findViewById(R.id.container)!=null){
            mTwoPane = true;
        }
        IntentFilter filter = new IntentFilter(StreamerService.ACTION_NOTIFY_SERVICE_STATE);
        mStateReceiver = new ServiceStateReceiver();
        mStateReceiver.setListener(this);
        registerReceiver(mStateReceiver,filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mStateReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        MenuItem item = menu.findItem(R.id.action_now_playing);
        item.setVisible(ServiceTools.isServiceRunning(this,StreamerService.class.getName()));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, PreferenceActivity.class));
                return true;
            case R.id.action_now_playing:
                if(mTwoPane){
                    onAskToShowPlayer(null,0);
                }else{
                    Intent i = new Intent(this,StreamingActivity.class);
                    startActivity(i);
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAskToShowDetailFragment(String name, String id, ArrayList<ParcelableTrack> tracks, int topTrackSelected) {
        if (!mTwoPane) {
            if (tracks != null) {
                Intent i = new Intent(this, TopTracksActivity.class);
                i.setAction(StreamerService.ACTION_SHOW_UI_FROM_SONG);
                i.putExtra(Intent.EXTRA_TEXT, id);
                i.putExtra(Intent.EXTRA_TITLE, name);
                i.putExtra(StreamerService.EXTRA_TOP_TRACK_SELECTED, topTrackSelected);
                i.putExtra(StreamerService.EXTRA_TOP_TRACKS, tracks);
                startActivity(i);
            } else {
                Intent i = new Intent(this, TopTracksActivity.class);
                i.putExtra(Intent.EXTRA_TEXT, id);
                i.putExtra(Intent.EXTRA_TITLE, name);
                startActivity(i);
            }
        }else{
            Bundle args = new Bundle();
            args.putString(TopTracksFragment.ARG_TITLE, name);
            args.putString(TopTracksFragment.ARG_ARTIST_ID, id);
            args.putBoolean(TopTracksFragment.ARG_MUST_FILL_UI_WITH_DATAS, tracks!=null);
            if (tracks!=null){
                args.putParcelableArrayList(StreamingFragment.EXTRA_TOP_TRACKS, tracks);
                args.putInt(StreamingFragment.EXTRA_TOP_TRACK_SELECTED, topTrackSelected);
            }
            TopTracksFragment fragment = new TopTracksFragment();
            fragment.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment).commit();
        }
    }

    @Override
    public void onAskToShowPlayer(ArrayList<ParcelableTrack> tracks, int topTrackSelected) {
        Bundle args = new Bundle();
        args.putParcelableArrayList(StreamingFragment.EXTRA_TOP_TRACKS, tracks);
        args.putInt(StreamingFragment.EXTRA_TOP_TRACK_SELECTED, topTrackSelected);
        StreamingFragment fragment = new StreamingFragment();
        fragment.setArguments(args);
        fragment.show(getSupportFragmentManager(),TAG_STREAMING_FRAGMENT);
    }

    @Override
    public void onServiceStateChange(boolean started) {
        invalidateOptionsMenu();
    }
}
