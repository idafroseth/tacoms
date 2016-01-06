package serviceMonitor;

import java.util.HashMap;

import gui.Logger;
import model.Router;

public class DialPeerMonitor extends Thread {
	private Router router;

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
			Logger.info("Checking dialPeers against BGP table");
			lookForChangePeers();
			try {
				Thread.sleep(10 * 1000);
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
		HashMap<String, String> bgpDialpeers = router.getBgpDialPeers();
		HashMap<String, String[]> dialPeers = router.getDialPeers();

		for (String telPrefix : bgpDialpeers.keySet()) {
			if (dialPeers.get(telPrefix) == null) {
				router.addDialPeer(telPrefix, bgpDialpeers.get(telPrefix));
				System.out.println("Dial-Peers do not exist: " + telPrefix + ", " + bgpDialpeers.get(telPrefix));
			} else if (!dialPeers.get(telPrefix)[0].equals(bgpDialpeers.get(telPrefix))) {
				System.out.println("New ip " + bgpDialpeers.get(telPrefix));
				int dialpeerNR = Integer.parseInt(dialPeers.get(telPrefix)[1]);
				if (dialpeerNR >= 30000) { // sjekker kun linjer som starter med
											// tall over 30000
					String nexthopAddr = bgpDialpeers.get(telPrefix);
					router.updateDialPeer(telPrefix, nexthopAddr, Integer.parseInt(dialPeers.get(telPrefix)[1])); // Updates
																													// dial-peers
																													// list
				} else {
					System.out.println("Skipping: " + telPrefix + " NextHop " + bgpDialpeers.get(telPrefix)
							+ " static nextHop is " + dialPeers.get(telPrefix)[0]);
				}
			}
		}
		// *******Compares existing Dial-peers against new Dial-peers (in case
		// of interface down):
		if (bgpDialpeers.keySet().equals(dialPeers.keySet())) {
			System.out.println("Dial-Peers are correct. Do nothing");
		} else {
			for (String telPrefix : dialPeers.keySet()) {
				if (bgpDialpeers.get(telPrefix) == null) {
					router.removeDialPeer(dialPeers.get(telPrefix)[1]);
					System.out.println("Cleaning ERROR !");
				} else {

				}
			}
		}
	}
}
