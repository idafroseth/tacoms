package serviceMonitor;

import java.util.ArrayList;

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

public class AutoConnectivityMonitor extends Thread implements RIBRouteStateListener {

	Router router;
	Extractor extractor4 = new Extractor();
	Extractor extractor6 = new Extractor();
	ArrayList<Integer> ribListeners = new ArrayList<Integer>();

	public AutoConnectivityMonitor(Router router) {
		this.router = router;
	}

	@Override
	public void run() {
		try {

			ribListeners.add(router.addRIBRouteStateListener(AFIType.IPV4, this, OwnerType.RIP));
			ribListeners.add(router.addRIBRouteStateListener(AFIType.IPV6, this, OwnerType.RIP));
		} catch (OnepException e1) {
			e1.printStackTrace();
		}

		while (true) {
			Logger.info("Checking if there are new Autconn neighbors");
			try {
				Thread.sleep(10 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				for (Integer ribListener : ribListeners) {
					try {
						router.removeRIBRouteListener(ribListener);
					} catch (OnepIllegalArgumentException | OnepRemoteProcedureException | OnepConnectionException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				return;
			}
		}
	}

	public void handleEvent(RIBRouteStateEvent event, Object clientData) {

		L3UnicastRoute l3uRoute = (L3UnicastRoute) event.getRoute();
		OwnerType rType = l3uRoute.getOwnerType();

		RIB.RouteState state = event.getState();
		System.out.println("RIBRouteStateEvent received...");
		System.out.println("This is a " + rType + " route.");
		System.out.println("Belongs to: " + l3uRoute.getPrefix().getAddress());
		System.out.println("State: " + event.getState());
		System.out.println("Scope: " + ((L3UnicastScope) event.getScope()).getAfi());

		// String rType = l3uRoute.getOwnerType().toString();
		
	////I AM CONFIGURING OWNER TYPE IN THE SET UP OF THE 	
	//	if (rType.equals(OwnerType.RIP)) {
			if (((L3UnicastScope) event.getScope()).getAfi() == AFIType.IPV4) {
				System.out.println("----------------------START Configuring Tunnel---------------------------");
				String[] octett = extractor4.octettExtractor(l3uRoute.getPrefix().getAddress().toString());
				if (state == RIB.RouteState.UP) {
					if (Integer.parseInt(octett[1]) > 47) {
						router.addSlaveNode(octett);
					} else if (Integer.parseInt(octett[1]) < 47) {
						router.addMasterNode(octett);
					}
				} else if (state == RIB.RouteState.DOWN) {
					router.removeNeighbor(octett);
				}
				System.out.println("----------------------DONE Configuring Tunnel---------------------------");
			}else{
				String[] data = extractor6.hexExtractor(l3uRoute.getPrefix().getAddress());
				System.out.println("inside RIP configure wih " + data[0]);
				switch (Integer.parseInt(data[0])) {
				case 202:
					if (state.equals(RIB.RouteState.UP)) {
						router.addGRETunnel(data);
					}
					if (state.equals(RIB.RouteState.DOWN)) {
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
