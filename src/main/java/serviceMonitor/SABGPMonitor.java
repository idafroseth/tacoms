package serviceMonitor;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import com.cisco.onep.core.exception.OnepException;
import com.cisco.onep.idl.ExceptionIDL;

import gui.Logger;
import model.Router;

public class SABGPMonitor extends Thread {
	private Router router;

	public SABGPMonitor(Router router) {
		this.router = router;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true) {
			Logger.info("Checking SA BGP peers against neighbors table");
			lookForChangeInPeers();
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
	 * Compare exisiting bgpPeers with new SA peers.
	 * 
	 * @return
	 */
	public boolean lookForChangeInPeers() {

		ArrayList<String[]> ripListData = router.getRipV6List();
		HashMap<String, String> configuredBgpPeers = router.getConfiguredBgpPeers();

		for (String[] data : ripListData) {
			if (!configuredBgpPeers.containsKey(data[3])) {
				// There is a new DialPeer so add it
				System.out.println("adding bgp peer: " + data[3]);
				router.addSABGPPeer(data);
			}
		}
		/**
		 * Comparing if the ipv4Peer configure still exists in the network,
		 * remove if not
		 */
		for (String ipv4Peer : configuredBgpPeers.keySet()) {
			boolean exists = false;
			for (String[] data : ripListData) {
				if (data[3].equals(ipv4Peer)) {
					exists = true;
					break;
				}
			}
			if (!exists) {
				System.out.println("Trying to remove bgp peer: " + ipv4Peer);
				router.saBGPRemove(ipv4Peer);
			}
		}
		return true;
	}
}
