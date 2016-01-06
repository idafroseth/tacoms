package co_routing;

import java.util.HashMap;

import gui.Logger;

public class CoMonitor implements Runnable{

	private HashMap<String, String[]> existingDialPeers;
	private HashMap<String, String> newDialPeers;
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
        while(true){
			Logger.info("Checking dialPeers and BGP table");
		//	compareHashMaps(); //Compares the two tables and returns values upon different outcome
		//	connect.getLogger().info("-------------------------SLEEP---------------------------");
        //	Thread.sleep(1000*5);
		}
	}
	
	public void lookForNewPeers(){
		
	}

	
}
