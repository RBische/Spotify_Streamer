/*
 * Copyright (C) 2015 Raphaël Bischof
 */
package bischof.raphael.spotifystreamer.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

import bischof.raphael.spotifystreamer.R;

/*
 * Activity containing TopTracksFragment
 * Created by biche on 11/06/2015.
 */
public class TopTracksActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_top_tracks, menu);
        return true;
    }
}
