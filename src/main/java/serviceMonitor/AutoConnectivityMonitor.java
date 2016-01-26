package serviceMonitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.cisco.onep.core.exception.OnepConnectionException;
import com.cisco.onep.core.exception.OnepException;
import com.cisco.onep.core.exception.OnepIllegalArgumentException;
import com.cisco.onep.core.exception.OnepRemoteProcedureException;
import com.cisco.onep.routing.L3UnicastRoute;
import com.cisco.onep.routing.L3UnicastScope;
import com.cisco.onep.routing.RIB;
import com.cisco.onep.routing.RIBRouteStateEvent;
import com.cisco.onep.routing.RIBRouteStateListener;
import com.cisco.onep.routing.L3UnicastRoute.OwnerType;
import com.cisco.onep.routing.L3UnicastScope.AFIType;
import gui.Logger;
import model.Extractor;
import model.Router;

public class AutoConnectivityMonitor implements Runnable, RIBRouteStateListener {

	Router router;
	Extractor extractor4 = new Extractor();
	Extractor extractor6 = new Extractor();
	ArrayList<Integer> ribListeners = new ArrayList<Integer>();
	public static boolean STARTED = false;

	public AutoConnectivityMonitor(Router router) {
		this.router = router;
	}

	@Override
	public void run() {
		try {

		//	ribListeners.add(router.addRIBRouteStateListener(AFIType.IPV4, this, OwnerType.RIP));
			ribListeners.add(router.addRIBRouteStateListener(AFIType.IPV6, this, OwnerType.RIP));
		} catch (OnepException e1) {
			e1.printStackTrace();
		}

		while (true) {
			Logger.info("Checking if there are new Autconn neighbors");
			try {
				Thread.sleep(10*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				Logger.info("INTERRUPT IN AUTCONNECT");
//				for (Integer ribListener : ribListeners) {
//					try {
//						router.removeRIBRouteListener(ribListener);
//					} catch (OnepIllegalArgumentException | OnepRemoteProcedureException | OnepConnectionException e1) {
//						// TODO Auto-generated catch block
//						e1.printStackTrace();
//					}
//				}
				return;
			}
			if(AutoConnectivityMonitor.STARTED){
				lookForGREPeers();
			}
		}
	}
	
	public void lookForGREPeers(){
		HashMap<String, String> ripPeers = router.getRipPeers();
		List<String> tunnelIds = router.getConfiguredTunnel();
		//Check the announced peers against the tunnelIDs
//		
//		Down
//			router.removeNeighbor(octett);
//		
		
		for(String peerId : ripPeers.keySet()){
		//System.out.println(peerId);
			if(!tunnelIds.contains(peerId)){
				Logger.info("Discovered a new GRE peer " + peerId);
				if (Integer.parseInt(peerId) > 47) {
					router.addSlaveNode(ripPeers.get(peerId).split("\\."), ripPeers.get(peerId));
				} else if (Integer.parseInt(peerId) < 47) {
					router.addMasterNode(ripPeers.get(peerId).split("\\."),ripPeers.get(peerId));
				}
			}
		}
		loop:
		for(String tunnelId : tunnelIds){
		//	System.out.println("checking tunnel " +tunnelId);
			Integer id = Integer.parseInt(tunnelId);
			if(id<=10 || id>100 ){
				continue loop;
			}
			if(!ripPeers.containsKey(tunnelId)){
				Logger.info("Discovered a tunnel ID that has no RIP entry - remove " + tunnelId);
				router.removeNeighbor(tunnelId );
			}
//			else if(!router.isTunnelConfigured(ripPeers.get(tunnelId))){
//				Logger.info("The tunnel is not configured properly - remove " + tunnelId);
//				router.removeNeighbor(ripPeers.get(tunnelId).split("\\."),ripPeers.get(tunnelId) );
//			}
		}
		
		
	}

	public void lookForNewPeer(){
		HashMap<String, String> bgpPeers = router.getBgpPeers();
		HashMap<String, String> ripPeers = router.getRIPngPeers();
		for(String ripPeer : ripPeers.keySet()){
		}
	}
	
	
	public void handleEvent(RIBRouteStateEvent event, Object clientData) {

		L3UnicastRoute l3uRoute = (L3UnicastRoute) event.getRoute();
		OwnerType rType = l3uRoute.getOwnerType();

		RIB.RouteState state = event.getState();
		Logger.info("RIBRouteStateEvent received...");
		Logger.info("This is a " + rType + " route.");
		Logger.info("Belongs to: " + l3uRoute.getPrefix().getAddress());
		Logger.info("State: " + event.getState());
		Logger.info("Scope: " + ((L3UnicastScope) event.getScope()).getAfi());

		// String rType = l3uRoute.getOwnerType().toString();
		
	////I AM CONFIGURING OWNER TYPE IN THE SET UP OF THE 	
	//	if (rType.equals(OwnerType.RIP)) {
			if (((L3UnicastScope) event.getScope()).getAfi() == AFIType.IPV4) {
				Logger.info("----------------------START Configuring Tunnel---------------------------");
				String[] octett = extractor4.octettExtractor(l3uRoute.getPrefix().getAddress().toString());
//				if (state == RIB.RouteState.UP) {
//
//				} else if (state == RIB.RouteState.DOWN) {
//					router.removeNeighbor(octett);
//				}
				Logger.info("----------------------DONE Configuring Tunnel---------------------------");
			}else{
				String[] data = extractor6.hexExtractor(l3uRoute.getPrefix().getAddress());
				Logger.info("inside RIP configure wih " + Integer.parseInt(data[0]));
				switch (Integer.parseInt(data[0])) {
				case 202:
					if (state.equals(RIB.RouteState.UP)) {
						Logger.info("Identified 202 UP");
						router.addGRETunnel(data);
					}
					if (state.equals(RIB.RouteState.DOWN)) {
						Logger.info("Identified 202 DOWN");
						router.removeGRETunnel(data);
					}
					break;
				case 510:
					if (state.equals(RIB.RouteState.UP)) {
						router.addBGPv4Neighbor(data);
					}
					else if (state.equals(RIB.RouteState.DOWN)) {
						router.removeBGPv4Neighbor(data);
					}
					break;
				case 511:
					if (state.equals(RIB.RouteState.UP)) {
						router.addMulicastNeighbor(data);
					}
					else if (state.equals(RIB.RouteState.DOWN)) {
						router.removeMulicastNeighbor(data);
					}
					break;
				}
			}
		//}
	}

}
