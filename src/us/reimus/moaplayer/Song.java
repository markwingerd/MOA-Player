package us.reimus.moaplayer;

public class Song {
	private long id;
	private String title;
	private String artist;
	private String file;
	
	public Song(long songID, String songTitle, String songArtist, String fileLocation) {
		id=songID;
		title=songTitle;
		artist=songArtist;
		file=fileLocation;
	}
	
	public long getID(){return id;}
	
	public String getTitle(){return title;}
	
	public String getArtist(){return artist;}
	
	public String getFileLocation(){return file;}

}