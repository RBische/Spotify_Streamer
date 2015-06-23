package bischof.raphael.spotifystreamer.activity;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_streaming, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
