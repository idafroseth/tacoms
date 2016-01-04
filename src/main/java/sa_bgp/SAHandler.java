package sa_bgp;
import connect.*;
import extractor.*;

import com.cisco.onep.core.exception.OnepException;
import com.cisco.onep.element.NetworkElement;
import com.cisco.onep.idl.ExceptionIDL;
import com.cisco.onep.vty.*;

import java.net.InetAddress;
import java.util.*;
import java.net.*;

//import org.slf4j.Logger;

public class SAHandler extends ConnectRouter {
	private Extractor extractor6 = new Extractor();
	private HashMap<String,String> bgpPeers = new HashMap();
	private ArrayList<String> ripList = new ArrayList();
	private ArrayList<String[]> ripListData = new ArrayList();
	private NetworkElement ne;
	private CiscoCLI cli = new CiscoCLI();
	private VtyService vty;
	
	public SAHandler(String[] args) {
		super(args);
		this.ne = getNetworkElement();
	}
	
	public void vtyOpen(){
		try{
			vty = new VtyService(getNetworkElement());
			vty.open();
		}catch (OnepException | InterruptedException  e) {
			System.out.println("Could not open VTY SERVICE!!");
		}
	}

	//	private NTPhandling ntp;
	public HashMap<String,String> getBgpPeers(){   
        try {
        	clearBgpPeers();
			if(!vty.isOpen()){
				vtyOpen();
			}
	        String ipv6Summary = vty.write("show bgp ipv6 summary");
	        Scanner scin = new Scanner(ipv6Summary);
	        String line = scin.nextLine();
	       
	        while(scin.hasNextLine()){
	        	line = scin.nextLine();
	        	if(line.contains("Neighbor")){
	        		line = scin.nextLine();
	        		break;
	        	}
	        }
	        while(scin.hasNextLine() && !line.contains("%")){
	        	String[] words = line.split("\\s+");
	        	//FIX: Denne sjekken er kanskje litt usikker!?!?
	        	if(words.length>9){
	    //    		getLogger().info("Neighbor: " + words[0] + " is " + words[9]);
	        		this.bgpPeers.put(words[0], words[9]);
	        		line = scin.nextLine();
	        	}
	        	else{
	        		break;
	        	}
	        }
	 
	        
		} catch (InterruptedException | OnepException | ExceptionIDL e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return this.bgpPeers;
	}
	public ArrayList<String[]> getRipList(){   
		String nextHop = "";
        try {
        	if(!vty.isOpen()){
				vtyOpen();
			}
        	clearRipList();
        	//FIX: kan eventuelt endre "include" til "section" for å finne next-hop for statisk rute
	        String ripSummary = vty.write("show ipv6 route rip | section "+ cli.SAPREFIX);
	        Scanner scin = new Scanner(ripSummary);
	       
	        while(scin.hasNextLine()){
	        	 String line = scin.nextLine();
	        //	System.out.println(line);
	        	String[] words = line.split("\\s+");
	        	if(!ripList.contains(words[1])){
	        		ripList.add(words[1]);
	       // 		getLogger().info(words[1]);
	        	}
	        	line = scin.nextLine();
	        	String[] words2 = line.split("/");
	        	nextHop = words2[0];
	        	
	        }
	        for (String s: ripList){
	        	String[] ipAdr = s.split("/");
	        	InetAddress ipv6adr = InetAddress.getByName(ipAdr[0]);
	        	String[] data = extractor6.hexExtractor(ipv6adr, getLogger());
	        	//i metoden benyttes ikke routeType til noe for denne SABGP og vi setter denne som placeholder for nextHop.
	        	data[0] = nextHop;
	        	ripListData.add(data);
	        }
		} catch (InterruptedException | OnepException | ExceptionIDL | UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return this.ripListData;
	}
	public boolean compareLists(){
		for(String[] data : ripListData){
			if(bgpPeers.containsKey(data[3])){
				//Do noting
			}else{
				System.out.println("adding bgp peer: " + data[3]);
				saBGPConfigure(data);
			}
		}
		/**
		 * Comparing if the ipv4Peer configure still exists in the network, remove if not
		 */
		for(String ipv4Peer : bgpPeers.keySet()){
			boolean exists = false;
			for(String[] data : ripListData){
				if(data[3].equals(ipv4Peer)){
					exists = true;
					break;
				}
			}
			if(!exists){
				getLogger().info("Trying to remove bgp peer: " + ipv4Peer);
				saBGPRemove(ipv4Peer);
			}
		}
		return true;
	}
	public void vtyClose(){
		try{
			vty.cancel();
			vty.close();
			vty.destroy();
		}catch(OnepException e){
			
		}

	}
	public boolean saBGPConfigure(String[] data){
		try {
			if(!vty.isOpen()){
				vtyOpen();
			} 
			vty.write(cli.CONFT);
			vty.write("ip route " + data[3] + " 255.255.255.255 " + data[0]);	
			vty.write(cli.GOSABGP);
			vty.write("neighbor " + data[3] + " remote-as " + data[1] +"." + data[2]);
			vty.write("neighbor " + data[3] + " ebgp-multihop " + Integer.parseInt(data[5])+3);
			vty.write("neighbor " + data[3] + " update-source loopback 47");
			vty.write(cli.GOBGPV4);
			vty.write("no neighbor " + data[3] + " activate");
			vty.write(cli.GOBGPV6);
			vty.write("neighbor " + data[3] + " activate");
			vty.write("neighbor " + data[3] + " route-map setNextHop out");
			vty.write("neighbor " + data[3] + " prefix-list bgp_out out");
			vty.write("neighbor " + data[3] + " route-map setNextHopIn out");
			vty.write("neighbor " + data[3] + " soft-reconfiguration inbound");
		    vty.write(cli.END);
			getLogger().info("Peering to: " + data[3] + " with ASN " + data[1] + "." + data[2]);
			
		} catch (InterruptedException e){
			e.printStackTrace();
		} catch( OnepException | ExceptionIDL e) {
			e.printStackTrace();

		}
		return true;
	}
	public boolean saBGPRemove(String neighbor){
		try {
			if(!vty.isOpen()){
				vtyOpen();
			}
			String cliTest = vty.write("sh bgp ipv4 unicast neighbors " + neighbor);
			String findIfStatic = vty.write("show run | include " + neighbor +" description staticSA");
			String[] data = neighbor.split(".");
			if(findIfStatic.contains("static")){
				getLogger().info("Neighbor " + neighbor + " is configured static skip this peer");
				return false;
			}
			if(cliTest.contains("% No such neighbor")){
				getLogger().info("Denne eksisterer ikke som IPV4 - fjerner hele peeren");
				String ipRoute = vty.write("show run | include ip route " + neighbor);
				vty.write(cli.CONFT);
				vty.write(cli.GOSABGP);
				vty.write("no neighbor " + neighbor);
				getLogger().info("no neighbor " + neighbor);
				vty.write("no " + ipRoute);
				getLogger().info("Fjerner statisk rute: no " + ipRoute );
			}else{
				vty.write(cli.CONFT);
				vty.write(cli.GOSABGP);
				vty.write(cli.GOBGPV6);
				vty.write("no neighbor " + neighbor);
			}
			
			vty.write("cli.END");
		} catch (InterruptedException | OnepException | ExceptionIDL e) {
			getLogger().info("SA-BGP config -- CRASH");
			e.printStackTrace();
		}
		return true;
	}
	public static void main(String[] args){
		
		SAHandler auto = new SAHandler(args);
        try {
            if (!auto.connect("saBGP")) {
            	auto.getLogger().info("Could not connect to the network element");
                System.exit(1);
            }
            System.out.println("****************************************************************************");
    		System.out.println("*****************   WELCOME TO SA-BGP AUTOCONFIGURATION   ******************");
    		System.out.println("****************************************************************************");
    		auto.vtyOpen();
        	while (true) {
        		auto.getBgpPeers();
        		auto.getRipList();
        		auto.compareLists();
        		auto.getLogger().info("-------------------------SLEEP---------------------------");
        		Thread.sleep(15*1000);
        	}
        }catch (OnepException e) {
        	auto.getLogger().error(e.getLocalizedMessage(), e);
        }catch (InterruptedException ex) {
        	Thread.currentThread().interrupt();
        }catch (Exception e) {
        	auto.getLogger().error(e.getLocalizedMessage(), e);
        }finally {
        	auto.disconnect();
        }
    }
	public void clearRipList(){
		this.ripList = new ArrayList();
	}
	public void clearBgpPeers(){
		this.bgpPeers = new HashMap();
	}
}