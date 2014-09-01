package us.reimus.moaplayer;

import java.util.ArrayList;

import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import java.util.Random;
import android.app.Notification;
import android.app.PendingIntent;

public class MusicService extends Service implements
MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
MediaPlayer.OnCompletionListener {
	private MediaPlayer player; 	// Media player
	private ArrayList<Song> songs; 	// Song list
	private int songPosn;			// Current position
	private final IBinder musicBind = new MusicBinder();
	private String songTitle = "";
	private static final int NOTIFY_ID=1;
	private boolean shuffle = false;
	private Random rand;
	
	public void onCreate() {
		// Create the service.
		super.onCreate();
		songPosn = 0;
		player = new MediaPlayer();
		rand = new Random();
		
		initMusicPlayer();
	}
	
	@Override
	public void onDestroy() {
		stopForeground(true);
	}
	
	public void initMusicPlayer() {
		// Set player properties.
		player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		
		// Set class listeners.
		player.setOnPreparedListener(this);
		player.setOnCompletionListener(this);
		player.setOnErrorListener(this);
	}
	
	public void setList(ArrayList<Song> theSongs) {
		songs=theSongs;
	}
	
	public class MusicBinder extends Binder {
		MusicService getService() {
		    return MusicService.this;
		}
	}
	 
	@Override
	public IBinder onBind(Intent arg0) {
	    return musicBind;
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		player.stop();
		player.release();
		return false;
	}

	// TODO: To ensure that your app does not interfere with other 
	//		 audio services on the user's device, you should enhance 
	//		 it to handle audio focus gracefully. Make the Service 
	//		 class implement the AudioManager.OnAudioFocusChangeListener 
	//		 interface. In the onCreate method, create an instance of 
	//		 the AudioManager class and call requestAudioFocus on it. 
	//		 Finally, implement the onAudioFocusChange method in your 
	//		 class to control what should happen when the application 
	//		 gains or loses audio focus. See the Audio Focus section in 
	//		 the Developer Guide for more details. 
	@Override
	public void onCompletion(MediaPlayer mp) {
		if (player.getCurrentPosition() > 0) {
			mp.reset();
			playNext();
		}
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		mp.reset();
		return false;
	}

	// Copied from: http://code.tutsplus.com/tutorials/create-a-music-player-on-android-user-controls--mobile-22787
	@Override
	public void onPrepared(MediaPlayer mp) {
		// Start playback.
		mp.start();
		
		// The PendingIntent class will take the user back to the 
		// main Activity class when they select the notification.
		Intent notIntent = new Intent(this, MainActivity.class);
		notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pendInt = PendingIntent.getActivity(this, 0,
				notIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		 
		Notification.Builder builder = new Notification.Builder(this);
		 
		builder.setContentIntent(pendInt)
			.setSmallIcon(R.drawable.play)
			.setTicker(songTitle)
			.setOngoing(true)
			.setContentTitle("Playing")
			.setContentText(songTitle);
		@SuppressWarnings("deprecation")
		Notification not = builder.getNotification(); // For API min used builder.build();
		 
		startForeground(NOTIFY_ID, not);
	}
	
	public void playSong() {
		// Play a song.
		player.reset();
		Song playSong = songs.get(songPosn); // Get song
		songTitle = playSong.getTitle();
		long currSong = playSong.getID();	//Get song Id
		Uri trackUri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSong);	// Set URI		
		
		try {
			player.setDataSource(getApplicationContext(), trackUri);
		}
		catch (Exception e) {
			Log.e("MUSIC SERVICE", "Error setting data source", e);
		}
		
		player.prepareAsync();
	}
	
	public void setSong(int songIndex) {
		songPosn = songIndex;
	}
	
	public int getPosn() {
		return player.getCurrentPosition();
	}
		 
	public int getDur() {
		return player.getDuration();
	}
		 
	public boolean isPng() {
		return player.isPlaying();
	}
		 
	public void pausePlayer() {
		player.pause();
	}
		 
	public void seek(int posn) {
		player.seekTo(posn);
	}
		 
	public void go() {
		player.start();
	}
	
	public void playPrev() {
		songPosn--;
		if (songPosn < 0) songPosn=songs.size()-1;
		playSong();
	}
	
	public void playNext() {
		if (shuffle) {
			int newSong = songPosn;
			while (newSong == songPosn) {
				newSong = rand.nextInt(songs.size());
			}
			songPosn = newSong;
		}
		else {
			songPosn++;
			if (songPosn >= songs.size()) songPosn = 0;
		}
		playSong();
	}
	
	// Toggles shuffle setting.
	public void setShuffle() {
		if (shuffle) shuffle = false;
		else shuffle = true;
	}
}
