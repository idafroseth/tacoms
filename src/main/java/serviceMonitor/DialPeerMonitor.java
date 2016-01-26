package serviceMonitor;

import java.util.HashMap;

import gui.Logger;
import model.Router;

public class DialPeerMonitor implements Runnable {
	private Router router;
	public static boolean STARTED = false;

	public DialPeerMonitor(Router router) {
		this.router = router;
	}

	/**
	 * Check for updates in the dialpeers every 10 seconds
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true) {
			Logger.info("Cecking PEER: " + STARTED);
			if(DialPeerMonitor.STARTED){
				Logger.info("Checking dialPeers against BGP table");
				lookForChangePeers();
			}
			try {
				Thread.sleep(40 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				//Interrupted when the user click disable
				return;
			}
		}
	}

	/**
	 * Looks for new peers and updates add og delete dialpeers.
	 */
	public void lookForChangePeers() {
		HashMap<String, String> newDialPeers = router.getBgpDialPeers();
		HashMap<String, String[]> existingDialPeers = router.getDialPeers();


		//********Compares new Dial-peers against existing Dial-peers:
		for (String key : newDialPeers.keySet()){
			String[] result = existingDialPeers.get(key);
			if(existingDialPeers.get(key)==null){
				router.addDialPeer(key, newDialPeers.get(key));
				Logger.info("Dial-Peers do not exist");
			}
			else if(existingDialPeers.get(key)[0].equals(newDialPeers.get(key))){
//						System.out.println("Exsist with correct IP, do nothing");
			}else{
//						System.out.println("New ip " + newDialPeers.get(key));
				int dialpeerNR = Integer.parseInt(existingDialPeers.get(key)[1]);
				if(dialpeerNR >= 30000){ //sjekker kun linjer som starter med tall over 30000
					String nexthopAddr = newDialPeers.get(key);
					router.updateDialPeer(key, nexthopAddr, Integer.parseInt(existingDialPeers.get(key)[1])); //Updates dial-peers list
				}else{
					Logger.info("Skipping: " + key + " NextHop " + newDialPeers.get(key) + " static nextHop is "+existingDialPeers.get(key)[0]);
				}
			}
		}
		
//*******Compares existing Dial-peers against new Dial-peers (in case of interface down):
		if(newDialPeers.keySet().equals(existingDialPeers.keySet())){
			Logger.info("Dial-Peers are correct. Do nothing");
		}
		else{
			for(String key:existingDialPeers.keySet()){
				if(newDialPeers.get(key) == null){
					router.removeDialPeer(existingDialPeers.get(key)[1]);
					Logger.info("Cleaning ERROR !");
				}else{
					
				}
			}
		}
	}
}
