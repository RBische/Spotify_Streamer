/*
 * Copyright (C) 2015 Raphaël Bischof
 */
package bischof.raphael.spotifystreamer.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

import bischof.raphael.spotifystreamer.R;
/*
 * Starting activty of the app
 * Created by biche on 10/06/2015.
 */
public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }
}
