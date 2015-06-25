package bischof.raphael.spotifystreamer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import java.util.ArrayList;

import bischof.raphael.spotifystreamer.R;
import bischof.raphael.spotifystreamer.fragment.StreamingFragment;
import bischof.raphael.spotifystreamer.model.ParcelableTrack;

public class StreamingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streaming);
        if (savedInstanceState == null) {
            // The detail Activity called via intent.  Inspect the intent for forecast data.
            Intent intent = getIntent();
            if (intent != null) {
                Bundle args = new Bundle();
                ArrayList<ParcelableTrack> tracks = intent.getParcelableArrayListExtra(StreamingFragment.EXTRA_TOP_TRACKS);
                args.putParcelableArrayList(StreamingFragment.EXTRA_TOP_TRACKS, tracks);
                args.putInt(StreamingFragment.EXTRA_TOP_TRACK_SELECTED, intent.getIntExtra(StreamingFragment.EXTRA_TOP_TRACK_SELECTED,0));
                StreamingFragment fragment = new StreamingFragment();
                fragment.setArguments(args);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, fragment).commit();
            }

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Intent intent = NavUtils.getParentActivityIntent(this);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_SINGLE_TOP);
                NavUtils.navigateUpTo(this, intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
