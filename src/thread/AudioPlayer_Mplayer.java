package thread;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ResourceBundle;
import java.util.Vector;

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
public class AudioPlayer_Mplayer extends Thread {
	
	private ResourceBundle trans = ResourceBundle.getBundle("translations.StreamRipStar");
	private Stream stream;
	private Gui_StreamRipStar mainGui;
	private SRSOutput lg = SRSOutput.getInstance();
	
	private String mplayerPath = "/usr/bin/mplayer";
	private int MPLAYER_CACHE = 128;
	private Vector<String> options = new Vector<String>(0,1);
	
	private BufferedReader inStream = null;
	private BufferedReader errStream = null;
	private BufferedWriter outStream = null;
	
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
				options.add("http://127.0.0.1:"+stream.relayServerPortTF);
	    	} else {
	    		options.add(stream.address);		
	    	}
			
			//collect the options
			options.add(0,mplayerPath);
			options.add("-slave");
			options.add("-quiet");
			options.add("-cache");
			options.add(String.valueOf(MPLAYER_CACHE));

			//say, we are loading the stream
			if (mainGui != null)
			{
				mainGui.showMessageInTray(trans.getString("audioplayer.loadingStream"));
			}
			
			//start the process itself
			lg.log("AudioPlayer: Start music with mplayer command: "+mplayerPath+" "+options.toString());
//			mplayerProcess = Runtime.getRuntime().exec(mplayerPath+" "+mplayerOptions);
//			
			mplayerProcess = new ProcessBuilder(options).start();
			
			//create the streams we need to interact
			inStream = new BufferedReader(new InputStreamReader(mplayerProcess.getInputStream()));
			errStream = new BufferedReader(new InputStreamReader(mplayerProcess.getErrorStream()));
			outStream = new BufferedWriter(new OutputStreamWriter(mplayerProcess.getOutputStream()));
			
			String messages = "";
			
			//get the messages from mplayer
			while((messages = inStream.readLine()) != null)
			{
				//if we have a new interpret and title update the gui
				if(messages.startsWith("ICY Info: StreamTitle="))
				{
					mainGui.setTitleForAudioPlayer(stream.name ,messages.substring(messages.indexOf("StreamTitle=\'")+13,messages.lastIndexOf("';")),false);
//					mainGui.setTitleForAudioPlayer(stream.name ,,false);
					
				}
			}
		 
		} catch (IOException e) {
			lg.logE("Error while executing mplayer: "+options.toString()+e.getMessage());
		} catch (Exception e) {
			lg.logE("Error while executing mplayer: "+options.toString()+e.getMessage());
		}  
	}
	
	/**
	 * Stop the playback of the thread with the player. It set the new 
	 * status message
	 */
	public void stopPlaying()
	{
		
		try {
			if(stream != null)
			{
				lg.logD("AudioPlayer: Try to stop the audio player with the stream: "+stream.name);
			}
			
			mainGui.setTitleForAudioPlayer(stream.name ,"",false);
			
			outStream.write("stop\n");
			outStream.flush();
			
			if(stream != null)
			{
				lg.logD("AudioPlayer: Audio Player with stream: "+stream.name + "has been stopped");
			}
		} catch (IOException e) {
			lg.logE("Error while stopping playback of mplayer: "+e.getMessage());
		}
		
	}
	
	/**
	 * Set a now volume for the audio player
	 * @param volumePercent The new volume in percent
	 */
	public void setAudioVolum(int volumePercent)
	{
		try {
			outStream.write("volume "+volumePercent + " 100\n");
			outStream.flush();
			lg.logD("AudioPlayer: set new volume to "+volumePercent);
		} catch (IOException e) {
			lg.logE("Error while set a new playback of mplayer: "+e.getMessage());
		}
		
	}
	
	/**
	 * Kills this thread
	 */
	public void killThread()
	{
		mplayerProcess.destroy();
	}
	
	/**+
	 * Looks if the process of the mplayer intstance has correct end
	 * @return true, if the end was correct, else false
	 */
	public boolean isThreadDead()
	{
		
		try {
			//if there was an exit value, the thread is dead ;)
			lg.logD("Mplayer exit value is: "+mplayerProcess.exitValue());
				return true;
		} catch (IllegalThreadStateException e) {
			lg.logD("AudioPLayer_Mplayer: thread is still running and has no exit value");
		}
		
		return false;
	}
}
