/*
 * http://code.tutsplus.com/tutorials/create-a-music-player-on-android-project-setup--mobile-22764
 * http://code.tutsplus.com/tutorials/create-a-music-player-on-android-song-playback--mobile-22778
 * http://code.tutsplus.com/tutorials/create-a-music-player-on-android-user-controls--mobile-22787
 */

package us.reimus.moaplayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import us.reimus.moaplayer.MusicService.MusicBinder;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.MediaController.MediaPlayerControl;
//import android.view.MenuItem;

public class MainActivity extends ActionBarActivity implements MediaPlayerControl {
	private ArrayList<Song> songList;	// Used to store songs.
	private ListView songView;			// Used to display song.
	private MusicService musicSrv;
	private Intent playIntent;
	private boolean musicBound = false;
	private MusicController controller;
	private boolean paused = false;
	private boolean playbackPaused = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		songView = (ListView)findViewById(R.id.song_list);
		songList = new ArrayList<Song>();
		getSongList();
		
		Collections.sort(songList, new Comparator<Song>() {
			public int compare(Song a, Song b){
			    return a.getTitle().compareTo(b.getTitle());
			  }
		});
		SongAdapter songAdt = new SongAdapter(this, songList);
		songView.setAdapter(songAdt);
		
		setController();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if(playIntent==null){
			playIntent = new Intent(this, MusicService.class);
			bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
			startService(playIntent);
		}
	}
	
	// Connect to the MusicService.
	private ServiceConnection musicConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			MusicBinder binder = (MusicBinder)service;
			// Get service
			musicSrv = binder.getService();
			// Pass the list.
			musicSrv.setList(songList);
			musicBound = true;
		}
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			musicBound = false;
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_activity_actions, menu);
	    return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
		case R.id.action_shuffle:
			musicSrv.setShuffle();
			break;
		case R.id.action_end:
			stopService(playIntent);
			musicSrv=null;
			System.exit(0);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onDestroy() {
		stopService(playIntent);
		musicSrv=null;
		super.onDestroy();
	}
	
	public void getSongList() {
		// Retrieve song info.
		
		// Queries the Music Files.
		ContentResolver musicResolver = getContentResolver();
		Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
		
		// Iterate through music.
		if (musicCursor!=null && musicCursor.moveToFirst()) {
			// Get columns
			int titleColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
			int idColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
			int artistColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST);
			// Add songs to list.
			do {
				long thisId = musicCursor.getLong(idColumn);
				String thisTitle = musicCursor.getString(titleColumn);
				String thisArtist = musicCursor.getString(artistColumn);
				songList.add(new Song(thisId, thisTitle, thisArtist));
			} while (musicCursor.moveToNext());
		}
	}
	
	public void songPicked(View view) {
		musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
		musicSrv.playSong();
		if (playbackPaused) {
			setController();
			playbackPaused = false;
		}
		controller.show(0);
	}

	@Override
	public void start() {
		musicSrv.go();		
	}

	@Override
	public void pause() {
		playbackPaused = true;
		musicSrv.pausePlayer();
	}

	@Override
	public int getDuration() {
		// The conditional tests are to avoid various exceptions 
		// that may occur when using the MediaPlayer and MediaController 
		// classes.
		if (musicSrv != null && musicBound && musicSrv.isPng())
			return musicSrv.getDur();
		else return 0;
	}

	@Override
	public int getCurrentPosition() {
		// The conditional tests are to avoid various exceptions 
		// that may occur when using the MediaPlayer and MediaController 
		// classes.
		if (musicSrv != null && musicBound && musicSrv.isPng())
			return musicSrv.getPosn();
		else return 0;
	}

	@Override
	public void seekTo(int pos) {
		musicSrv.seek(pos);
	}

	@Override
	public boolean isPlaying() {
		if (musicSrv != null && musicBound)
			return musicSrv.isPng();
		return false;
	}

	@Override
	public int getBufferPercentage() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean canPause() {
		return true;
	}

	@Override
	public boolean canSeekBackward() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean canSeekForward() {
		// TODO Auto-generated method stub
		return true;
	}
	
	private void setController(){
		// Sets the controller up.
		controller = new MusicController(this);
		
		// Add listeners for the click events on the next and prev buttons.
		controller.setPrevNextListeners(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				playNext();
			}
		}, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
			    playPrev();
			}
		});
		
		// Setup controller to work on media playback.
		controller.setMediaPlayer(this);
		controller.setAnchorView(findViewById(R.id.song_list));
		controller.setEnabled(true);
	}
	
	private void playNext() {
		musicSrv.playNext();
		if (playbackPaused) {
			setController();
			playbackPaused = false;
		}
		controller.show(0);
	}
	 
	private void playPrev() {
		musicSrv.playPrev();
		if (playbackPaused) {
			setController();
			playbackPaused = false;
		}
		controller.show(0);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		paused = true;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (paused) {
			setController();
			paused = false;
		}
	}
	
	@Override
	protected void onStop() {
		controller.hide();
		super.onStop();
	}
}