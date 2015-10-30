package com.example.mediaplayerpreferences;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

/**
 * Created by jasonwong on 10/11/15.
 */
public class MediaPreferences extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MediaPreferencesFragment()).commit();
    }

    /** This fragment shows the preferences for the media player */
    public static class MediaPreferencesFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            PreferenceManager.setDefaultValues(getActivity(), R.xml.media_preferences, false);
            //  Load  the  preferences  from  an  XML  resource
            addPreferencesFromResource(R.xml.media_preferences);
        }
    }
}

