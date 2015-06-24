package bischof.raphael.spotifystreamer.fragment;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import bischof.raphael.spotifystreamer.R;

/**
 * This fragment shows general preferences.
 * Created by biche on 24/06/2015.
 */

public class GeneralPreferenceFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
    }
}
