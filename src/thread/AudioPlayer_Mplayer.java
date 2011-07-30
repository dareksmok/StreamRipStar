package thread;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ResourceBundle;
import misc.Stream;
import control.SRSOutput;
import gui.Gui_StreamRipStar;

/**
 * Starts an instance to play with mplayer. From here you can control the volume, the stream
 * and fetch some other information about the stream.
 * 
 * @author Johannes Putzke
 *
 */
public class AudioPlayer_Mplayer extends Thread{
	
	private ResourceBundle trans = ResourceBundle.getBundle("translations.StreamRipStar");
	private Stream stream;
	private Gui_StreamRipStar mainGui;
	private SRSOutput lg = SRSOutput.getInstance();
	
	private String mplayerPath = "/usr/bin/mplayer";
	private String mplayerOptions = "";
	private int MPLAYER_CACHE = 128;
	
	private Process mplayerProcess = null;
	
	/**
	 * Use this constructor if you want to play a stream
	 * @param stream The stream you want to hear
	 * @param mainGui Mainwindow
	 */
	public AudioPlayer_Mplayer(Stream stream, Gui_StreamRipStar mainGui)
	{
		this.stream = stream;
		this.mainGui = mainGui;

		lg.logD("AudioPlayer: Backend for mplayer created");
	}
	
	/**
	 * Starts the mplayer process on the OS and fetches the output messages of it
	 * to update the title information
	 */
	public void run()
	{
		try
		{
			//do we need the local or the stream from the net?
			if(stream.getStatus() && stream.connectToRelayCB) {
				mplayerOptions += "http://127.0.0.1:"+stream.relayServerPortTF;
	    	} else {
	    		mplayerOptions += stream.address;		
	    	}
			
			//say, we are loading the stream
			if (mainGui != null)
			{
				mainGui.showMessageInTray(trans.getString("audioplayer.loadingStream"));
			}
			
			//start the process itself
			lg.log("AudioPlayer: Start music with mplayer command: "+mplayerPath+mplayerOptions);
			mplayerProcess = Runtime.getRuntime().exec(mplayerPath+mplayerOptions);
			
		} catch (Exception e) {
		    lg.logE("Fehler beim Ausführen von "+mplayerPath+mplayerOptions+e.getMessage());
		}
	}
	
	/**
	 * Stop the playback of the thread with the player. It set the new 
	 * status message
	 */
	public void stopPlaying()
	{
		if(stream != null)
		{
			lg.logD("AudioPlayer: Try to stop the audio player with the stream: "+stream.name);
		}
		
		mainGui.showMessageInTray("");
		
		if(stream != null)
		{
			lg.logD("AudioPlayer: Audio Player with stream: "+stream.name + "has been stopped");
		}
	}
	
	/**
	 * Set a now volume for the audio player
	 * @param volumePercent The new volume in percent
	 */
	public void setAudioVolum(int volumePercent)
	{

		lg.logD("AudioPlayer: set new volume to"+volumePercent);
	}
}
