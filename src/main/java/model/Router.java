package model;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import com.cisco.onep.core.exception.OnepConnectionException;
import com.cisco.onep.core.exception.OnepException;
import com.cisco.onep.core.exception.OnepIllegalArgumentException;
import com.cisco.onep.core.exception.OnepRemoteProcedureException;
import com.cisco.onep.element.NetworkElement;
import com.cisco.onep.idl.ExceptionIDL;
import com.cisco.onep.routing.L3UnicastRIBFilter;
import com.cisco.onep.routing.L3UnicastScope;
import com.cisco.onep.routing.RIB;
import com.cisco.onep.routing.L3UnicastScope.AFIType;
import com.cisco.onep.routing.L3UnicastScope.SAFIType;
import com.cisco.onep.routing.RIB.RouteStateListenerFlag;
import com.cisco.onep.routing.RIBRouteStateListener;
import com.cisco.onep.routing.Routing;
import com.cisco.onep.routing.L3UnicastRoute.OwnerType;
import com.cisco.onep.vty.VtyService;

import gui.Logger;
import old.ConnectRouter;

public class Router extends Connection {

	private HashSet<Integer> dialpeerTagsTaken = new HashSet<Integer>();
	private Extractor extractor6 = new Extractor();
	private ArrayList<String[]> ripListData = new ArrayList();

	public Router(String ipaddress, String password, String userName, String applicationName) throws OnepException {
		super(ipaddress, password, userName, applicationName);

		// TODO Auto-generated constructor stub

	}

	public HashMap<String, String[]> getDialPeers() {
		HashMap<String, String[]> dialPeers = new HashMap<String, String[]>();
		try {
			if (!vty.isOpen()) {
				vtyOpen();
			}
			String currentDialPeers = vty.write("show dial-peer voice summary");
			Scanner scin = new Scanner(currentDialPeers);
			// Logger.info(currentDialPeers);
			while (scin.hasNextLine()) {
				String line = scin.nextLine();
				if (line.contains("syst")) {
					String[] word = line.split("\\s+"); // Deretter deler vi opp
														// innholdet av linjen
					String dstPattern = word[4].replaceAll("\\D+", ""); // Keep
																		// only
																		// digits
																		// of
																		// the
																		// dest-pattern
		
					if(word.length>7){
						if(word[7].length()>5){
							String nextHop = word[7].substring(5);
							String data[] = new String[2];
							data[0] = nextHop;
							data[1] = word[0];
							// Logger.info("putting into existing dialpeers:
							// "+dstPattern + " with data " + data[0]+ " and "
							// +data[1]);
							dialPeers.put(dstPattern, data);
						}
					}
				}
			}
		} catch (InterruptedException | OnepException | ExceptionIDL e) {
			e.printStackTrace();
		}
		return dialPeers;
	}

	/**
	 * Adds a RIB Route listener.
	 *
	 * @return Event handler identifier for the listener.
	 * @throws OnepException
	 *             If it fails to add RIBRoute listener error.
	 */
	public int addRIBRouteStateListener(AFIType type, RIBRouteStateListener RIBRouteListener, OwnerType routeType)
			throws OnepException {
		L3UnicastScope aL3UnicastScope = new L3UnicastScope("", type, SAFIType.UNICAST, "");
		// Hvis vi har VRF maa vi komentere ut her:
		// aL3UnicastScope.setVrf("TACOMS");

		L3UnicastRIBFilter filter = new L3UnicastRIBFilter();
		filter.setOwnerType(routeType);

		Logger.info("adding RIB listener...");
		int RIBRouteListenerEventHandle = (Routing.getInstance(getNetworkElement()).getRib()).addRouteStateListener(
				RIBRouteListener, aL3UnicastScope, filter,
				RouteStateListenerFlag.TRIGGER_INITIAL_WALK, null);
		return RIBRouteListenerEventHandle;
	}

	public void removeRIBRouteListener(int eventHandler)
			throws OnepIllegalArgumentException, OnepRemoteProcedureException, OnepConnectionException {
		Logger.info("removing RIB listener...");
		(Routing.getInstance(getNetworkElement()).getRib()).removeRouteStateListener(eventHandler);
	}

	////////////////////////////// BEGIN AUTOCON
	////////////////////////////// METHODS//////////////////////////////
	public synchronized HashMap<String, String> getRipPeers(){
		HashMap<String,String> ripPeers = new HashMap<String, String>();
		try {
			if (!vty.isOpen()) {
				vtyOpen();
			}
			vty.write(CiscoCLI.END);
			String ripTable = vty.write("show ip route rip");
			Scanner scin = new Scanner(ripTable);
			String line = "";
			loop:
			while(scin.hasNextLine()){
				if( scin.nextLine().contains("110.0.0.0/32")){
					break loop;
				}
			}
			while(scin.hasNextLine()){
				line = scin.nextLine();
				String[] words = line.split("\\s+");
				String announcedPeer = words[4];
				ripPeers.put(announcedPeer.split("\\.")[1],announcedPeer);
				Logger.info("Detected neighbor " + announcedPeer + " with id " +announcedPeer.split("\\.")[1]);
			}
			
			
		} catch (InterruptedException | OnepException | ExceptionIDL e1) {
			e1.printStackTrace();
		}
		
		return ripPeers;
	}
	public List<String> getConfiguredTunnel(){
		List<String> configuredTunnels = new ArrayList<String>();
		try {
			if (!vty.isOpen()) {
				vtyOpen();
			}
			vty.write(CiscoCLI.END);
			String tunnelList = vty.write("sh int summary | include Tunnel");
			Scanner scin = new Scanner(tunnelList);
			String line = "";
			
			while(scin.hasNextLine()){
				line = scin.nextLine();
				if(line.contains("Tunnel")){
					String[] words = line.split("\\s+");
					int index = 0;
					if(line.contains("*")){
						index = 1;
					}else{
						index = 0;
					}
					System.out.println("Tunnel + " + words[index]);
					String tunnelId = words[1].replace("Tunnel","");
					
					Logger.info("Discovered a configured tunnel with id: " + tunnelId);
					configuredTunnels.add(tunnelId);	
				}
			}
		} catch (InterruptedException | OnepException | ExceptionIDL e1) {
			e1.printStackTrace();
		}
			
		return configuredTunnels;
	}
	
	public boolean isTunnelConfigured(String ipv4Peer){
		
		try {
			if (!vty.isOpen()) {
				vtyOpen();
			}
			vty.write(CiscoCLI.END);
			String[] id = ipv4Peer.split(".");
			if(vty.write("show run int tun " + id[1]).contains("Invalid")){
				Logger.info("The tunnel to " + ipv4Peer + " Does not exist");
				return false;
			}
			if(vty.write("ping "+ipv4Peer + " repeat 1").contains("Success")){
				Logger.info("The tunnel is configured correctly");
				return true;
			}
			
			
		} catch (InterruptedException | OnepException | ExceptionIDL e1) {
			e1.printStackTrace();
		}
		return false;
		
	}
	
	public synchronized HashMap<String, String> getRIPngPeers(){
		HashMap<String, String> ripPeers = new HashMap<String, String>();
		try {
			if (!vty.isOpen()) {
				vtyOpen();
			}
			String routeTable = vty.write("show ipv6 route rip");
			Scanner scin = new Scanner(routeTable);
			while (!scin.nextLine().contains("LISP")){
				if(!scin.hasNextLine()){
					return ripPeers;
				}
			}
			
			while (scin.hasNextLine()) {
				String line = scin.nextLine();
				if(line.contains("FD00")){
					String[] words = line.split("\\s+");
					String announcedIPv6 = words[0];
					String nextHop = scin.nextLine().split("\\s+")[3];
					Logger.info("Found RIPng: "+ announcedIPv6 + ", "  + nextHop);
					ripPeers.put(announcedIPv6, nextHop);
				}
			}
			
		} catch (InterruptedException | OnepException | ExceptionIDL e1) {
			e1.printStackTrace();
		}
		return ripPeers;
	}
	public synchronized HashMap<String, String> getBgpPeers(){
		HashMap<String, String> bgpPeers = new HashMap<String, String>();
		try {
			if (!vty.isOpen()) {
				vtyOpen();
			}
			String bgpTable = vty.write("sh run | include neighbor");
			Scanner scin = new Scanner(bgpTable);
			String line;
			String[] words;
			
			outer:
			while(scin.hasNextLine()){
				line = scin.nextLine();
				if(line.contains("log-neighbor-changes")){
					continue outer;
				}
				if(line.contains("remote-as")){
					words = line.split("\\s+");
					Logger.info("Discover BGP peer: " + words[1] + ", " + words[3]);
					bgpPeers.put(words[1], words[3]);
				}
			}
		
		} catch (InterruptedException | OnepException | ExceptionIDL e1) {
			e1.printStackTrace();
		}
		return bgpPeers;
	}
	
	
	public synchronized boolean addMasterNode(String[] octett) {
		try {
			if (!vty.isOpen()) {
				vtyOpen();
			}
			
			if(vty.write("show run interface tun " + octett[1] + " | include 111." + octett[1] + "." + octett[2] + "." + octett[3]).length()>2){
				Logger.info("The tunnel already exists do nothing");
				return true;
			}

			Logger.info("We are a masternode");
			String phyDest = "111." + octett[1] + "." + octett[2] + "." + octett[3];
			String iPv6Adr = "FD00:202::2F:1:114.47." + octett[1] + ".1/127"; //+ (Integer.parseInt(octett[2]) * 4+ 1) + "/127";
			Logger.info("The tunnel did not exist so we are about to create the tunnel");

			vty.write(CiscoCLI.CONFT);
			vty.write("crypto isakmp key TAC9MS! address " + phyDest);
			Logger.info("interface tunnel" + octett[1]);
			vty.write("interface tunnel" + octett[1]);
			vty.write("ip address " + "114.47." + octett[1] + ".2" //+ Integer.parseInt(octett[2]) * 4 + 2
					+ " 255.255.255.252");
			vty.write("tunnel destination " + phyDest);
			vty.write("tunnel source 111.47.1.1");
			vty.write("ipv6 address " + iPv6Adr);
			Logger.info("*************************************ipv6 address " + iPv6Adr);
			Logger.info("**************************");
			vty.write(CiscoCLI.ENIPV6);
			vty.write("ipv6 rip TACOMS enable");
			vty.write("tunnel protection ipsec profile ipsec2000");
			vty.write("ip pim bsr-border");
			vty.write("ip pim sparse-mode");

			vty.write(CiscoCLI.END);
			vty.write(CiscoCLI.CONFT);
			vty.write(CiscoCLI.GORIPV6);
			vty.write("distribute-list prefix-list " + octett[1] + " out tunnel " + octett[1]);
			vty.write(CiscoCLI.END);
			vty.write(CiscoCLI.CONFT);
			vty.write("ipv6 prefix-list " + octett[1] + " permit FD00:510:0:2:2F:1:2F01:1/128");
			vty.write("ipv6 prefix-list " + octett[1] + " permit FD00:511::2F:1:2F01:2/128");
			vty.write("ipv6 prefix-list " + octett[1] + " permit " + iPv6Adr);
			vty.write("ipv6 prefix-list " + octett[1] + " permit FD00:500:0:2:2F:1:2F01:1/128");
			vty.write(CiscoCLI.END);
			Logger.info("You have just created a new Tunnel with ID " + octett[1]);

		} catch (InterruptedException | OnepException | ExceptionIDL e1) {
			e1.printStackTrace();
		}
		return true;
	}

	public synchronized boolean addSlaveNode(String[] octett) {
		try {
			if (!vty.isOpen()) {
				vtyOpen();
			}
			if(vty.write("show run interface tun " + octett[1] + " | include 111." + octett[1] + "." + octett[2] + "." + octett[3]).length()>2){
				Logger.info("The tunnel already exists do nothing");
				return true;
			}
			Logger.info("We are a slave node");
			String phyDest = "111." + octett[1] + "." + octett[2] + "." + octett[3];
			vty.write(CiscoCLI.CONFT);
			vty.write("crypto isakmp key TAC9MS! address " + phyDest);
			vty.write("interface tunnel" + octett[1]);
			vty.write("ip unnumbered loopback 47");
			vty.write("tunnel destination " + phyDest);
			vty.write("tunnel source 111.47.1.1");
			vty.write(CiscoCLI.ENIPV6);
			vty.write("ipv6 rip TACOMS enable");
			vty.write("tunnel protection ipsec profile ipsec2000");
			vty.write("ip pim bsr-border");
			vty.write("ip pim sparse-mode");
			vty.write(CiscoCLI.END);
			vty.write(CiscoCLI.CONFT);
			vty.write("ipv6 prefix-list " + octett[1] + " permit FD00:510:0:2:2F:1:2F01:1/128");
			vty.write("ipv6 prefix-list " + octett[1] + " permit FD00:511::2F:1:2F01:2/128");
			vty.write("ipv6 prefix-list " + octett[1] + " permit FD00:500:0:2:2F:1:2F01:1/128");
			
			vty.write(CiscoCLI.END);
			vty.write(CiscoCLI.CONFT);
			vty.write(CiscoCLI.GORIPV6);
			vty.write("distribute-list prefix-list " + octett[1] + " out tunnel " + octett[1]);
			vty.write(CiscoCLI.END);
			Logger.info("You have just created a new Tunnel with ID " + octett[1]);

		} catch (InterruptedException | ExceptionIDL | OnepException e1) {
			e1.printStackTrace();
			Logger.error(e1.getMessage());
			return false;
		}
		return true;
	}

	public synchronized boolean removeNeighbor(String[] octett) {
		try {
			if (!vty.isOpen()) {
				vtyOpen();
			}
			/*
			 * Remove tunnel with the right ID
			 */
			String phyDest = "111." + octett[1] + "." + octett[2] + "." + octett[3];

			Logger.info("The route went down so we are about to remove the tunnel and IPsec config");
			vty.write(CiscoCLI.CONFT);
			Logger.info(vty.write("no interface tunnel " + octett[1]));
			Logger.info(vty.write("no crypto isakmp key TAC9MS! address " + phyDest));
			Logger.info(vty.write("no ipv6 prefix-list " + octett[1]));
			Logger.info(vty.write(CiscoCLI.GORIPV6));
			Logger.info(vty.write("no distribute-list prefix-list " + octett[1] + " out tunnel " + octett[1]));
			Logger.info(vty.write(CiscoCLI.END));

			Logger.info("Is Open after removing tunnel? " + vty.isOpen());

			return true;
		} catch (Exception T) {
			// getLogger().error(T.getLocalizedMessage(), T);
			return false;
		}
	}
	
	public synchronized void addGRETunnel(String[] hexArray){
		try {
			if (!vty.isOpen()) {
				vtyOpen();
			}
			String ipv4TuAdress = hexArray[4];
			Logger.info("--------------Updating Tunnel to "+hexArray[1] + " --------");
			System.out.println(Integer.parseInt(hexArray[1]) + " is larger than " + CiscoCLI.MAINAS + " so update");
			if ((Integer.parseInt(hexArray[1]) > CiscoCLI.MAINAS)) {
				Logger.info("***Conf T ");
				vty.write(CiscoCLI.CONFT);
				vty.write("interface tunnel " + hexArray[1]);
				Logger.info("***interface tunnel" +hexArray[1] );
				Logger.info("***ip address " + ipv4TuAdress + " 255.255.255.252" );
				vty.write("ip address " + ipv4TuAdress + " 255.255.255.252");
				vty.write(CiscoCLI.END);
			}
			Logger.info("--------------Done updating Tunnel to "+hexArray[1] + " --------");
		} catch (Exception e) {
			Logger.error(e.getLocalizedMessage());
		}

	}
	public void removeGRETunnel(String[] hexArray){
		//The tunnel will be removed when the RIP goes down. 
	}
	
	
	public synchronized void addBGPv4Neighbor(String[] hexArray) {
		try {
			if (!vty.isOpen()) {
				vtyOpen();
			}

			String routeType = hexArray[0];
			String ipv4Peer = hexArray[3];
			String ipv4TuAdress = hexArray[4];
			String neighASN = hexArray[1] + "." + hexArray[2];
			if(vty.write("show run | sec neighbor " + ipv4Peer + " remote-as " + neighASN).length()<4){

				Logger.info("It is a BGP prefix so configure the BGP table to peer " + ipv4Peer + " with ASN "
						+ hexArray[1] + "." + hexArray[2]);

		
				String staticIPRoute = "ip route " + ipv4Peer + " 255.255.255.255 tu " + hexArray[1];
				vty.write(CiscoCLI.END);
				vty.write(CiscoCLI.CONFT);
				Logger.info("******IS THIS CORRECT*******: " + staticIPRoute);
				vty.write(staticIPRoute);
				//vty.write();
				vty.write(CiscoCLI.GOCLBGP);
				vty.write("neighbor " + ipv4Peer + " remote-as " + neighASN);
				vty.write("neighbor " + ipv4Peer + " ebgp-multihop 4");
				vty.write("neighbor " + ipv4Peer + " update-source Loopback47" );
				vty.write(CiscoCLI.GOBGPV4);
				vty.write("neighbor " + ipv4Peer + " activate");

				vty.write(CiscoCLI.END);
			}

		} catch (Exception e) {
			Logger.info(e.getLocalizedMessage());
		}
	}
	
	
	public synchronized void addMulicastNeighbor(String[] hexArray) {
		try {
			if (!vty.isOpen()) {
				vtyOpen();
			}
			String ipv4Peer = hexArray[3];
			Logger.info("It is a msdp prefix so configure multicast to peer " + ipv4Peer);
			String setMSDPpeer = "ip msdp peer " + ipv4Peer + " connect-source loop 472";
			vty.write(CiscoCLI.CONFT);
			vty.write(setMSDPpeer);
			vty.write(CiscoCLI.END);
		} catch (Exception e) {
			// getLogger().error(e.getLocalizedMessage(), e);
		}
	}
	
	
	public synchronized void removeMulicastNeighbor(String[] hexArray) {
		try {
			if (!vty.isOpen()) {
				vtyOpen();
			}
			String ipv4Peer = hexArray[3];
			Logger.info("we are about to remove msdp");
			vty.write(CiscoCLI.CONFT);
			vty.write("no ip msdp peer " + ipv4Peer + " connect-source tu " + hexArray[1]);
			vty.write(CiscoCLI.END);
			Logger.info("MSDP is removed");
		} catch (Exception e) {
			Logger.error(e.getLocalizedMessage());
		}
	
	}

	public synchronized void removeBGPv4Neighbor(String[] hexArray) {
		try {
			if (!vty.isOpen()) {
				vtyOpen();
			}
			String neighASN = hexArray[1] + "." + hexArray[2];
			String ipv4Peer = hexArray[3];
			// String ipv4TuAdress = hexArray[1] + "." + hexArray[2] + "." +
			// octett[2] + "." + (octett[3]-1);
			String removeBGPPeer = "no neighbor " + ipv4Peer + " remote-as " + neighASN;
			String removeStaticIPRoute = "no ip route " + ipv4Peer + " 255.255.255.255 Tunnel " + hexArray[1];

			vty.write(CiscoCLI.CONFT);
			vty.write(CiscoCLI.GOCLBGP);
			vty.write(removeBGPPeer);
			vty.write(CiscoCLI.EXIT);
			vty.write(removeStaticIPRoute);
			vty.write(CiscoCLI.END);

		} catch (Exception e) {
			Logger.error(e.getLocalizedMessage());
		}
	}
	/////////////////////////////// END AUTOCONN
	/////////////////////////////// METHODS/////////////////////////////

	//////////////////////////////// SA BGP
	//////////////////////////////// METHODS/////////////////////////////////
	/**
	 * Get the list of all the RIPv6 neigbor discovered by the router
	 * 
	 * @return
	 */
	public synchronized ArrayList<String[]> getRipV6List() {
		String nextHop = "";
		ArrayList<String> ripList = new ArrayList<String>();
		try {
			if (!vty.isOpen()) {
				vtyOpen();
			}
			// FIX: kan eventuelt endre "include" til "section" for å finne
			// next-hop for statisk rute
			String ripSummary = vty.write("show ipv6 route rip | section " + CiscoCLI.SAPREFIX);
			Scanner scin = new Scanner(ripSummary);

			while (scin.hasNextLine()) {
				String line = scin.nextLine();
				String[] words = line.split("\\s+");
				if (!ripList.contains(words[1])) {
					ripList.add(words[1]);
				}
				if(scin.hasNextLine()){
					line = scin.nextLine();
					String[] words2 = line.split("/");
					nextHop = words2[0];
				}

			}
			for (String s : ripList) {
				String[] ipAdr = s.split("/");
				InetAddress ipv6adr = InetAddress.getByName(ipAdr[0]);
				String[] data = extractor6.hexExtractor(ipv6adr);
				// i metoden benyttes ikke routeType til noe for denne SABGP og
				// vi setter denne som placeholder for nextHop.
				data[0] = nextHop;
				ripListData.add(data);
			}
		} catch (InterruptedException | OnepException | ExceptionIDL | UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ripListData;
	}

	/**
	 * Get a list of all the bgp ipv6 neighbor that are configured on the router
	 * 
	 * @return a HashMap containing IPaddress, NodeID
	 */
	public synchronized HashMap<String, String> getConfiguredBgpPeers() {
		HashMap<String, String> bgpPeers = new HashMap<String, String>();
		try {
			if (!vty.isOpen()) {
				vtyOpen();
			}
			vty.write("end");
			String ipv6Summary = vty.write("show bgp ipv6 summary");
			Scanner scin = new Scanner(ipv6Summary);
			if(scin.hasNextLine()){
				String line = scin.nextLine();
	
				while (scin.hasNextLine()) {
					line = scin.nextLine();
					if (line.contains("Neighbor")) {
						line = scin.nextLine();
						break;
					}
				}
				while (scin.hasNextLine() && !line.contains("%")) {
					String[] words = line.split("\\s+");
					// FIX: Denne sjekken er kanskje litt usikker!?!?
					if (words.length > 9) {
						bgpPeers.put(words[0], words[9]);
						line = scin.nextLine();
					} else {
						break;
					}
				}
			}
		} catch (InterruptedException | OnepException | ExceptionIDL e) {
			e.printStackTrace();
		}
		return bgpPeers;
	}

	/**
	 * Add a SA BGP peer
	 * 
	 * @param data
	 * @return
	 */
	public synchronized boolean addSABGPPeer(String[] data) {
		try {
			if (!vty.isOpen()) {
				vtyOpen();
			}
			vty.write(CiscoCLI.CONFT);
			vty.write("ip route " + data[3] + " 255.255.255.255 " + data[0]);
			vty.write(CiscoCLI.GOSABGP);
			vty.write("neighbor " + data[3] + " remote-as " + data[1] + "." + data[2]);
			vty.write("neighbor " + data[3] + " ebgp-multihop " + Integer.parseInt(data[5]) + 3);
			vty.write("neighbor " + data[3] + " update-source loopback 47");
			vty.write(CiscoCLI.GOBGPV4);
			vty.write("no neighbor " + data[3] + " activate");
			vty.write(CiscoCLI.GOBGPV6);
			vty.write("neighbor " + data[3] + " activate");
			vty.write("neighbor " + data[3] + " prefix-list bgp_out out");
			vty.write("neighbor " + data[3] + " route-map setNextHopIn out");
			vty.write("neighbor " + data[3] + " soft-reconfiguration inbound");
			vty.write(CiscoCLI.END);
			Logger.info("Peering to: " + data[3] + " with ASN " + data[1] + "." + data[2]);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (OnepException | ExceptionIDL e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * Removing a neighbor from the BGP v6 list when
	 * 
	 * @param neighbor
	 *            the IP of the neighbor
	 * @return
	 */
	public synchronized boolean saBGPRemove(String neighbor) {
		try {
			if (!vty.isOpen()) {
				vtyOpen();
			}
			String cliTest = vty.write("sh bgp ipv4 unicast neighbors " + neighbor);
			String findIfStatic = vty.write("show run | include " + neighbor + " description staticSA");
			String[] data = neighbor.split(".");
			if (findIfStatic.contains("static")) {
				Logger.info("Neighbor " + neighbor + " is configured static skip this peer");
				return false;
			}
			if (cliTest.contains("% No such neighbor")) {
				Logger.info("Denne eksisterer ikke som IPV4 - fjerner hele peeren");
				String ipRoute = vty.write("show run | include ip route " + neighbor);
				vty.write(CiscoCLI.CONFT);
				vty.write(CiscoCLI.GOSABGP);
				vty.write("no neighbor " + neighbor);
				Logger.info("no neighbor " + neighbor);
				vty.write("no " + ipRoute);
				Logger.info("Fjerner statisk rute: no " + ipRoute);
			} else {
				vty.write(CiscoCLI.CONFT);
				vty.write(CiscoCLI.GOSABGP);
				vty.write(CiscoCLI.GOBGPV6);
				vty.write("no neighbor " + neighbor);
			}

			vty.write("cli.END");
		} catch (InterruptedException | OnepException | ExceptionIDL e) {
			Logger.error("SA-BGP config -- CRASH");
			e.printStackTrace();
		}
		return true;
	}
	//////////////////////////////// END SA BGP
	//////////////////////////////// METHODS/////////////////////////////////

	/////////////////////// DIAL PEER FUNCTIONS START
	/////////////////////// //////////////////////////////
	/**
	 * 
	 * @return a list of bgp dialpeers from the ipv6 BGP table it is
	 *         synchronized so multiple threads doesn´t interfere and change
	 *         each the others paramaters
	 */

	public synchronized HashMap<String, String> getBgpDialPeers() {
		// <tlf-prefix>,<best-next-hop-IPadr>
		HashMap<String, String> bgpList = new HashMap<String, String>();
		// <tlf-prefix>,<nexthop, type>
		HashMap<String, String[]> midlDP = new HashMap<String, String[]>();
		// String bgpLines;
		try {
			if (!vty.isOpen()) {
				vtyOpen();
			}
			String result = vty.write("show bgp ipv6 unicast");
			
			if(!result.contains("Network")){
				Logger.error("show bgp IPv6 Unicast was empty (Router:510)");
				return bgpList;
			}
			
			String[] bgplines = result.split("Network")[1].split("\\r?\\n");

			for (int i = 0; i < (bgplines.length) - 1; i++) {
				String[] line = bgplines[i].split("/");
				if (bgplines[i].contains(CiscoCLI.NOCOBGP) || (!bgplines[i].contains("FD00:520") && !bgplines[i].contains("FD00:4:1"))) {
					continue;
				}
				if (bgplines[i].contains("FD00")) {
					int mask = Integer.parseInt(line[1]);
					String[] telprefix = (line[0].split(":"));

					Integer startHex = 0;
					if (bgplines[i].contains("FD00:520")) {
						startHex = 2;
					} else if (bgplines[i].contains("FD00:4:1")) {
						startHex = 3;
					} else if(bgplines[i].contains("540")){
						startHex = 3;
					}
					String dialPeerNr = "";
					for (int s = startHex; s <= (mask / 16) - 1; s++) {
						Logger.info("********** Trying to parse DP address " + startHex + " with Dialpeer number " + dialPeerNr);
						if(telprefix.length>s){
							if (telprefix[s].equals("")) {
								dialPeerNr += "000";
							} else {
								dialPeerNr += telprefix[s];
							}
						}
					}
					int oldmetric = 255;
					String bestNextHop = "";
					while (i + 1 < bgplines.length) {
						if (bgplines[i + 1].contains("FD00")) {
							break;
						}
						if (bgplines[i].contains("FFFF")) {
							String nexthop = bgplines[i].split(":")[3];
							int metric = bgplines[i + 1].length();
							if (metric < oldmetric) {
								oldmetric = metric;
								bestNextHop = nexthop;
							}
						}
						i++;
					}
					// sjekk på om det er en end-to-end rute fra før
					String[] array = { bestNextHop, startHex.toString() };
					if (midlDP.containsKey(dialPeerNr)) {
						if (startHex > Integer.parseInt(midlDP.get(dialPeerNr)[1])) {
							Logger.info(
									"Dialpeer nummer: " + dialPeerNr + "er bedre enn " + midlDP.get(dialPeerNr)[0]);
							midlDP.put(dialPeerNr, array);
							bgpList.put(dialPeerNr, bestNextHop);
						}
					} else {
						midlDP.put(dialPeerNr, array);
						bgpList.put(dialPeerNr, bestNextHop);
					}
				} // ferdig if-løkke mot fd00

			} // ferdig for-løkke
		} // ferdig try
		catch (InterruptedException | OnepException | ExceptionIDL e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bgpList;
	} // ferdig metode

	/**
	 * Remove a dialpeer defined by
	 * 
	 * @param dialPeer
	 * @return true if it was successful
	 */
	public boolean removeDialPeer(String dialPeer) {
		try {
			// Logger.info(getNetworkElement());
			if (!vty.isOpen()) {
				vtyOpen();
			}
			Logger.info("Removing dialpeer with tag: " + dialPeer);
			vty.write(CiscoCLI.CONFT);
			vty.write("no dial-peer voice " + dialPeer + " voip");
			vty.write(CiscoCLI.END);
			dialpeerTagsTaken.remove(dialPeer);
			return true;
		} catch (InterruptedException | OnepException | ExceptionIDL e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Update a peer that has moved.
	 * 
	 * @param destPattern
	 * @param sipServer
	 * @param tag
	 */
	public void updateDialPeer(String destPattern, String sipServer, int tag) {
		try {

			if (!vty.isOpen()) {
				vtyOpen();
			}
			vty.write(CiscoCLI.CONFT);
			vty.write("dial-peer voice " + tag + " voip");
			vty.write("session target ipv4:" + sipServer);
			vty.write("voice-class codec 1");
			vty.write("session protocol sipv2");
			vty.write(CiscoCLI.END);

		} catch (InterruptedException | OnepException | ExceptionIDL e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Logger.info("Error with updatePeer");
		}
	}

	/**
	 * Add a dialpeer based on the destPattern and sipServer
	 * 
	 * @param destPattern
	 * @param sipServer
	 * @return
	 */
	public boolean addDialPeer(String destPattern, String sipServer) {
		int newTag = 30000;
		for (int i = 30000; i < 30500; i++) {
			// //Logger.info(!tagsTaken.contains(i));
			if (dialpeerTagsTaken.add(i)) { // if its successfully added (true)
				newTag = i;
				break;
			}
		}
		int antallPrikker = 13 - destPattern.length();
		for (int i = 0; i < antallPrikker; i++) {
			destPattern += ".";
		}
		// Logger.info("DESTINATION PATTERN:" + destPattern);
		try {
			if (!vty.isOpen()) {
				vtyOpen();
			}
			vty.write(CiscoCLI.CONFT);
			Logger.info("****adding dial-peer with tag: " + newTag);
			vty.write("dial-peer voice " + newTag + " voip");
			vty.write("destination-pattern " + destPattern);
			Logger.info("****adding dial-peer with destination Pattern: " + destPattern + " and next hop " + sipServer);
			vty.write("session target ipv4:" + sipServer);
			vty.write("voice-class codec 1");
			vty.write("session protocol sipv2");
			vty.write(CiscoCLI.END);

			return true;
		} catch (InterruptedException | OnepException | ExceptionIDL e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	/////////////////////// DIAL PEER FUNCTIONS END
	/////////////////////// //////////////////////////////
}
