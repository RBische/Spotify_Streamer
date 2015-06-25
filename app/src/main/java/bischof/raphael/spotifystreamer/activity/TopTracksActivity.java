/*
 * Copyright (C) 2015 Raphaël Bischof
 */
package bischof.raphael.spotifystreamer.activity;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

import bischof.raphael.spotifystreamer.R;
import bischof.raphael.spotifystreamer.fragment.StreamingFragment;
import bischof.raphael.spotifystreamer.fragment.TopTracksFragment;
import bischof.raphael.spotifystreamer.model.ParcelableTrack;
import bischof.raphael.spotifystreamer.service.ServiceStateReceiver;
import bischof.raphael.spotifystreamer.service.ServiceTools;
import bischof.raphael.spotifystreamer.service.StreamerService;

/*
 * Activity containing TopTracksFragment
 * Created by biche on 11/06/2015.
 */
public class TopTracksActivity extends AppCompatActivity implements TopTracksFragment.Callbacks,ServiceStateReceiver.OnServiceStateChangeListener {

    private ServiceStateReceiver mStateReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);
        IntentFilter filter = new IntentFilter(StreamerService.ACTION_NOTIFY_SERVICE_STATE);
        mStateReceiver = new ServiceStateReceiver();
        mStateReceiver.setListener(this);
        registerReceiver(mStateReceiver,filter);
        // The detail Activity called via intent.  Inspect the intent for forecast data.
        Intent intent = getIntent();
        if (intent != null) {
            Bundle args = new Bundle();
            args.putString(TopTracksFragment.ARG_TITLE,getIntent().getStringExtra(Intent.EXTRA_TITLE));
            args.putString(TopTracksFragment.ARG_ARTIST_ID,getIntent().getStringExtra(Intent.EXTRA_TEXT));
            boolean mustFillUI = getIntent().getAction()!=null&&getIntent().getAction().equals(StreamerService.ACTION_SHOW_UI_FROM_SONG);
            args.putBoolean(TopTracksFragment.ARG_MUST_FILL_UI_WITH_DATAS, mustFillUI);
            if (mustFillUI){
                ArrayList<ParcelableTrack> tracks = intent.getParcelableArrayListExtra(StreamingFragment.EXTRA_TOP_TRACKS);
                args.putParcelableArrayList(StreamingFragment.EXTRA_TOP_TRACKS, tracks);
                args.putInt(StreamingFragment.EXTRA_TOP_TRACK_SELECTED, intent.getIntExtra(StreamingFragment.EXTRA_TOP_TRACK_SELECTED,0));
            }
            TopTracksFragment fragment = new TopTracksFragment();
            fragment.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragment).commit();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mStateReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_top_tracks, menu);
        MenuItem item = menu.findItem(R.id.action_now_playing);
        item.setVisible(ServiceTools.isServiceRunning(this, StreamerService.class.getName()));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Intent intent = NavUtils.getParentActivityIntent(this);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP);
                NavUtils.navigateUpTo(this, intent);
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, PreferenceActivity.class));
                return true;
            case R.id.action_now_playing:
                onAskToShowPlayer(null,0);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAskToShowPlayer(ArrayList<ParcelableTrack> tracks, int topTrackSelected) {
        Intent intent = new Intent(this, StreamingActivity.class);
        intent.putParcelableArrayListExtra(StreamingFragment.EXTRA_TOP_TRACKS,tracks);
        intent.putExtra(StreamingFragment.EXTRA_TOP_TRACK_SELECTED,topTrackSelected);
        startActivity(intent);
    }

    @Override
    public void onServiceStateChange(boolean started) {
        invalidateOptionsMenu();
    }
}
