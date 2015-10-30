package com.example.mediaplayerpreferences;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Author: Jason Wong
 */
public class MyMediaPlayerActivity extends Activity {

	/**
	 * Other view elements
	 */
	private TextView songTitleLabel;

	/**
	 *  media player:
	 *  http://developer.android.com/reference/android/media/MediaPlayer.html 
	 */
	private MediaPlayer mp;

	/**
	 * Index of the current song being played
	 */
	private int currentSongIndex = 0;

	private int requestNumber = 99;

	/**
	 * List of Sounds that can be played in the form of SongObjects
	 */
	private static ArrayList<SongObject> songsList = new ArrayList<SongObject>();

	Button playPauseButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.media_player_main);

		songTitleLabel = (TextView) findViewById(R.id.songTitle);

		// Initialize the media player
		mp = new MediaPlayer();

		// Getting all songs in a list
		populateSongsList();

		// Default to first song if it exists
		playSong(0);

		playPauseButton = (Button) findViewById(R.id.playpausebutton);
		playPauseButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				playPauseSong();
			}
		});

		Button forwardButton = (Button) findViewById(R.id.forwardbutton);
		forwardButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (currentSongIndex >= songsList.size() - 1) {
					currentSongIndex = 0;
					playSong(currentSongIndex);
				} else {
					currentSongIndex = currentSongIndex + 1;
					playSong(currentSongIndex);
				}
			}
		});

		Button backButton = (Button) findViewById(R.id.backbutton);
		backButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (currentSongIndex == 0) {
					currentSongIndex = songsList.size() - 1;
					playSong(currentSongIndex);
				} else {
					currentSongIndex = currentSongIndex - 1;
					playSong(currentSongIndex);
				}
			}
		});

	}

	@Override
	public void onResume(){
		super.onResume();
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		Resources res = getResources();
		boolean shuffle = preferences.getBoolean(res.getString(R.string.mp_shuffle_pref), false);

		//Shuffle songs
		if(shuffle){
			long seed = System.nanoTime();
			Collections.shuffle(songsList, new Random(seed));
			Log.v("pref_shuffle","TRUE");

		}else{
			Log.v("pref_shuffle","FALSE");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.media_player_menu, menu);
		return true;
	} 

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_choose_song:
			Intent intent = new Intent(getApplicationContext(), SongList.class);
			startActivityForResult(intent, requestNumber);

			return true;
		case R.id.menu_preferences:
			Intent intent2 = new Intent(getApplicationContext(), MediaPreferences.class);
			startActivity(intent2);

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	

	/**
	 * Helper function to play a song at a specific index of songsList
	 * @param songIndex - index of song to be played
	 */
	public void  playSong(int songIndex){
		// Play song if index is within the songsList
		if (songIndex < songsList.size() && songIndex >= 0) {
			try {
				mp.stop();
				mp.reset();
				mp.setDataSource(songsList.get(songIndex).getFilePath());
				mp.prepare();
				mp.start();
				// Displaying Song title
				String songTitle = songsList.get(songIndex).getTitle();
				songTitleLabel.setText(songTitle);

				// Changing Button Image to pause image
				((Button)findViewById(R.id.playpausebutton)).setBackgroundResource(R.drawable.btn_pause);

				// Update song index
				currentSongIndex = songIndex;

			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} 
		} else if (songsList.size() > 0) {
			playSong(0);
		}
	}


	/**
	 * Toggle song playing and update button image
	 */
	public void  playPauseSong(){
		try {
			if(mp.isPlaying()){
				mp.pause();
				playPauseButton.setBackgroundResource(R.drawable.btn_play);
			}else{
				mp.start();
				playPauseButton.setBackgroundResource(R.drawable.btn_pause);
			}

		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}


	/** 
	 * Get list of info for all sounds to be played
	 */
	public void populateSongsList(){
		String[] mProjection = {MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA};

		String[] mSelectionArgs = {""};

		String mSelectionClause = MediaStore.Audio.Media.IS_MUSIC + " = 1";

		Cursor mCursor = getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,   // The content URI of the words table
				mProjection,                        // The columns to return for each row
				mSelectionClause,                    // Selection criteria
				null,                     // Selection criteria
				null);

		while (mCursor.moveToNext()) {
			int titleIndex = mCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
			int songPathIndex = mCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
			String title = mCursor.getString(titleIndex);
			String songPath = mCursor.getString(songPathIndex);
			Log.v("song_title", title);
			SongObject song = new SongObject(title, songPath);
			songsList.add(song);
		}
	
	}

	/**
	 * Get song list for display in ListView
	 * @return list of Songs 
	 */
	public static ArrayList<SongObject> getSongsList() {
		return songsList;
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){

		super.onActivityResult(requestCode, resultCode,data);
		// Check which request it is that we're responding to
		if (requestCode == requestNumber) {
			// Make sure the request was successful
			if (resultCode == RESULT_OK) {
				Bundle extras = data.getExtras();
				int songIndex = extras.getInt("songIndex");
				currentSongIndex = songIndex;
				playSong(currentSongIndex);

				// Do something with the phone number...
			}
		}
	}

}
