package misc;

import gui.Gui_StreamRipStar;

/**
 * If the VM crashes this code will executed
 * 
 * @author Johannes Putzke
 */
public class ShutDownHuck extends Thread {

	private Gui_StreamRipStar mainWin;
	
	/**
	 * We just need the mainwindow. Over this we can get all threads
	 * @param mainWin
	 */
	public ShutDownHuck(Gui_StreamRipStar mainWin)
	{
		this.mainWin = mainWin;
	}
	
	public void run()
	{
		//kill all audio Threads
		mainWin.getTabel().stopInternalAudioPlayer();
	}
}
