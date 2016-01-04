package co_routing;

import connect.ConnectRouter;
import connect.CiscoCLI;

import com.cisco.onep.routing.RIB;
import com.cisco.onep.vty.VtyService;
import com.cisco.onep.core.exception.OnepConnectionException;
import com.cisco.onep.core.exception.OnepException;
import com.cisco.onep.core.exception.OnepIllegalArgumentException;
import com.cisco.onep.core.exception.OnepRemoteProcedureException;
import com.cisco.onep.element.NetworkElement;
import com.cisco.onep.idl.ExceptionIDL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;

import org.slf4j.Logger;

public class CORouting extends ConnectRouter{
	private String currentDialPeers;
	private VtyService vtyCO;
	private CiscoCLI cli = new CiscoCLI();
	static HashSet<Integer> tagsTaken = new HashSet<Integer>();
	private HashMap<String, String[]> existingDialPeers = new HashMap();
	private HashMap<String, String> newDialPeers = new HashMap();
	private int newTag;
	private Scanner scin;
	private NetworkElement ne;
	private VtyService vty;
	
	public CORouting(String[] args){
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
	
	public static void main(String[] args){
		
		CORouting connect = new CORouting(args);
        try {
            if (!connect.connect("corouting")) {
            	connect.getLogger().info("Could not connect to the network element");
                System.exit(1);
            }
            connect.vtyOpen();
            System.out.println("****************************************************************************");
    		System.out.println("****************   WELCOME TO CORouting AUTOCONFIGURATION   ****************");
    		System.out.println("****************************************************************************");
            while(true){
    			connect.getLogger().info("Checking dialPeers and BGP table");
    			connect.compareHashMaps(); //Compares the two tables and returns values upon different outcome
    			connect.getLogger().info("-------------------------SLEEP---------------------------");
            	Thread.sleep(1000*15);
    		}
    		
        }catch (OnepException e) {
        	connect.getLogger().error(e.getLocalizedMessage(), e);
        }catch (Exception e) {
        	connect.getLogger().error(e.getLocalizedMessage(), e);
        }finally {
        	connect.disconnect();
        }
	}
	
	public void compareHashMaps() {
		listDialPeers(); //Get list of current dial-peers and SIP-server from router. Put in existingDialPeers
		this.newDialPeers = listBGPpeers3();
//		this.newDialPeers = listBGPpeers();  //Get list of current BGP IPv6 routes. Put in newDialPeers 
	
//********Compares new Dial-peers against existing Dial-peers:
		for (String key : newDialPeers.keySet()){
			String[] result = existingDialPeers.get(key);
			if(existingDialPeers.get(key)==null){
				addPeer(key, newDialPeers.get(key));
				getLogger().info("Dial-Peers do not exist");
			}
			else if(existingDialPeers.get(key)[0].equals(newDialPeers.get(key))){
//				System.out.println("Exsist with correct IP, do nothing");
			}else{
//				System.out.println("New ip " + newDialPeers.get(key));
				int dialpeerNR = Integer.parseInt(existingDialPeers.get(key)[1]);
				if(dialpeerNR >= 30000){ //sjekker kun linjer som starter med tall over 30000
					String nexthopAddr = newDialPeers.get(key);
					updatePeer(key, nexthopAddr, Integer.parseInt(existingDialPeers.get(key)[1])); //Updates dial-peers list
				}else{
					getLogger().info("Skipping: " + key + " NextHop " + newDialPeers.get(key) + " static nextHop is "+existingDialPeers.get(key)[0]);
				}
			}
		}
		
//*******Compares existing Dial-peers against new Dial-peers (in case of interface down):
		if(newDialPeers.keySet().equals(existingDialPeers.keySet())){
			getLogger().info("Dial-Peers are correct. Do nothing");
		}
		else{
			for(String key:existingDialPeers.keySet()){
				if(newDialPeers.get(key) == null){
					removePeer(existingDialPeers.get(key)[1]);
					getLogger().info("Cleaning ERROR !");
				}else{
					
				}
			}
		}
	}
		
	public void listDialPeers(){
		existingDialPeers = new HashMap();
		try{
			if(!vty.isOpen()){
				vtyOpen();
			}
			currentDialPeers = vty.write("show dial-peer voice summary");
			Scanner scin = new Scanner(currentDialPeers);
			//System.out.println(currentDialPeers);
			while(scin.hasNextLine()){
				String line = scin.nextLine();
				if(line.contains("syst")){
					String[] word = line.split("\\s+");	//Deretter deler vi opp innholdet av linjen		
					String dstPattern = word[4].replaceAll("\\D+",""); //Keep only digits of the dest-pattern
					String nextHop = word[7].substring(5);
					String data[] = new String[2];
					data[0] = nextHop;
					data[1] = word[0];
					//System.out.println("putting into existing dialpeers: "+dstPattern + " with data " + data[0]+ " and " +data[1]);
					existingDialPeers.put(dstPattern, data);
				}
			}
		}
		catch (InterruptedException | OnepException | ExceptionIDL e) {
			e.printStackTrace();
		}
	}
	
	public HashMap<String,String> listBGPpeers3(){
		//<tlf-prefix>,<best-next-hop-IPadr>
		HashMap<String, String> bgpList = new HashMap();
		//<tlf-prefix>,<nexthop, type>
		HashMap<String, String[]> midlDP = new HashMap();
		//String bgpLines;

		try {
			if(!vty.isOpen()){
				vtyOpen();
			}
			String result = vty.write("show bgp ipv6 unicast");
			
		//for  slippe  splitte ta med tomme linjer kan vi skrive [\\r\\n]+
			String[] bgplines = result.split("Network")[1].split("\\r?\\n");
			for(int i = 0; i<(bgplines.length)-1; i++){
				String[] line = bgplines[i].split("/");
				if(bgplines[i].contains(cli.NOCOBGP)){
					continue;
				}
				else if(bgplines[i].contains("FD00")){
					int mask = Integer.parseInt(line[1]);
					String[] telprefix = (line[0].split(":"));
					
					Integer startHex = 0;
					if(bgplines[i].contains("FD00:520")){
						startHex = 2;
					}else if(bgplines[i].contains("FD00:4:1")){
						startHex = 3;
					}
					String dialPeerNr = "";
					for(int s=startHex; s<=(mask/16)-1 ;s++){
						if(telprefix[s].equals("")){
							dialPeerNr += "000";
						}else{
							dialPeerNr += telprefix[s];
						}
					}
					int oldmetric = 255;
					String bestNextHop = "";
					while(i+1<bgplines.length){
						if(bgplines[i+1].contains("FD00")){
							break;
						}
						if(bgplines[i].contains("FFFF")){
							String nexthop = bgplines[i].split(":")[3];
							int metric = bgplines[i+1].length();
							if(metric<oldmetric){
								oldmetric = metric;
								bestNextHop = nexthop;
							}
						}
						i++;
					}
					//sjekk på om det er en end-to-end rute fra før
					String[] array = {bestNextHop,startHex.toString()}; 
					if(midlDP.containsKey(dialPeerNr)){
						if(startHex > Integer.parseInt(midlDP.get(dialPeerNr)[1])){
							System.out.println("Dialpeer nummer: " +dialPeerNr + "er bedre enn "+ midlDP.get(dialPeerNr)[0]);
							midlDP.put(dialPeerNr, array);
							bgpList.put(dialPeerNr, bestNextHop);
						}
					}else{
						midlDP.put(dialPeerNr, array);
						bgpList.put(dialPeerNr, bestNextHop);
					}
				}// ferdig if-løkke mot fd00
				
			} //ferdig for-løkke
		}// ferdig try
		catch (InterruptedException | OnepException | ExceptionIDL e) {
			// TODO Auto-generated catch block
			 e.printStackTrace();
		}
		return bgpList;

	} // ferdig metode

	public HashMap<String,String> listBGPpeers(){
		HashMap<String, String> bgpList = new HashMap();
		HashMap<String, ArrayList<String>> midlDP = new HashMap();
		
		CiscoCLI cli = new CiscoCLI();
		String result = null;
		try {
			if(!vty.isOpen()){
				vtyOpen();
			}
			result = vty.write("show bgp ipv6");
		//	vtyCOr.write(cli.END);
	
		
			if(result.contains("Network")){
				//System.out.println(result);
				scin = new Scanner(result);
				String line = scin.nextLine();
				//System.out.println(scin.hasNextLine());
	
				while(scin.hasNextLine()){	
					
					if(line.contains("FD00")){		//finds where the dial-peers starts
						break;
					}
					line = scin.nextLine();
				}
				//line is first peer
				
				String dialPeer = "10.10.10.10";
				ArrayList<String> nextHopList = new ArrayList();
				line = scin.nextLine();
				String nextHop = line;
				
				while(scin.hasNextLine()){
					if(line.contains(cli.NOCOBGP)){
//						System.out.println("Continuing: " +line);
						line = scin.nextLine();
						continue;
					}
					else if(line.contains("FD00:520")){
						String[] lineWords = line.split("\\s+");
						String[] dpIDs = lineWords[2].split(":");
						dialPeer = dpIDs[2] + dpIDs[3];
					}else if(line.contains("FD00:4:1")){
						String[] lineWords = line.split("\\s+");
						String[] dpIDs = lineWords[2].split(":");
						dialPeer = dpIDs[3] + dpIDs[4];
					}else{
//						System.out.println("Continuing: " +line);
						line = scin.nextLine();
						continue;
					}
					line = scin.nextLine();
					//System.out.println(line);
					while(!line.contains("FD00")){
						
						if(line.contains("::FFFF:")){
							String[] lineWords = line.split("\\s+");
//							System.out.println("Discovering nextHOP: "+ line);
//							System.out.println("Discovering lineword: "+ lineWords[1]);
							if(line.contains("*")){
								nextHop = lineWords[2].substring(7);
							}
							else{
								nextHop = lineWords[1].substring(7);
							}
							//System.out.println("Discovering nextHOP: "+ line);
							//System.out.println("Extracting: " + line.split("\\s+")[1].substring(7));
							nextHopList.add(nextHop);
						}
						if(!scin.hasNextLine()){
							break;
						}
						
						line = scin.nextLine();
//						System.out.println("!line.contains(FD00:) "+ !line.contains("FD00:"));
						
					}
					midlDP.put(dialPeer, nextHopList);
//					getLogger().info("adding: "+dialPeer +" with best hop = "+ nextHopList);
					//System.out.println("Adding dialPeer With Number: " +dialPeer);
					//System.out.println("with nextHopList: " +nextHopList);
				}
			
			String bestNextHop = null;
			for(String key : midlDP.keySet()){
				ArrayList<String> nextHops = midlDP.get(key);
				for(String nh : nextHops){
					bestNextHop = nh;
					//System.out.println("Comparing: " + key.substring(3,5) + " with " + nh.substring(0,2));
					if(key.substring(3,5).equals(nh.substring(0,2))){
						bestNextHop = nh;
						break;
					}	
				}
				//System.out.println(bestNextHop);
//				 getLogger().info("adding: "+key +" with best hop = "+ bestNextHop);
				if(bestNextHop != null){
					bgpList.put(key,bestNextHop);
				}
		
			}
		}
			
		}catch (InterruptedException | OnepException | ExceptionIDL e) {
			// TODO Auto-generated catch block
			 e.printStackTrace();
		}
//		getLogger().info("**********DONE listBGPpeers**********");
		for(String key : bgpList.keySet()){
			getLogger().info("NewDialPeers has : "+ key + " with nextHop: "+ bgpList.get(key));
		}
			
		return bgpList;
	}
	
	public boolean removePeer(String dialPeer){
	      try { 
	    	  	//System.out.println(getNetworkElement());
	    	  if(!vty.isOpen()){
					vtyOpen();
				}
	            vty.write(cli.CONFT);
	            vty.write("no dial-peer voice " + dialPeer + " voip");
	            vty.write(cli.END);
	            tagsTaken.remove(dialPeer);
	            return true;
	        } catch (InterruptedException | OnepException | ExceptionIDL e) {
	            // TODO Auto-generated catch block
	             e.printStackTrace();
	             return false;
	        }
	}     
	public void updatePeer(String destPattern, String sipServer, int tag){
		try {
			
			if(!vty.isOpen()){
				vtyOpen();
			}
			vty.write(cli.CONFT);
			vty.write("dial-peer voice " + tag + " voip");
			vty.write("session target ipv4:" + sipServer);
			vty.write("voice-class codec 1");
			vty.write("session protocol sipv2");
			vty.write(cli.END);
			
		} catch (InterruptedException | OnepException | ExceptionIDL e) {
			// TODO Auto-generated catch block
			 e.printStackTrace();
			 getLogger().info("Error with updatePeer");
		}
	}
	public boolean addPeer(String destPattern, String sipServer){
		int newTag = 30000;
		for(int i = 30000; i<30500; i++){
//			//System.out.println(!tagsTaken.contains(i));
			if(tagsTaken.add(i)){		//if its successfully added (true)
				newTag = i;
				break;
			}
		}
		int antallPrikker = 13 - destPattern.length();
		for(int i =0; i<antallPrikker; i++){
			destPattern += ".";
		}
		//System.out.println("DESTINATION OPAATER:" + destPattern);
		try {
			if(!vty.isOpen()){
				vtyOpen();
			}
			vty.write(cli.CONFT);
			vty.write("dial-peer voice " + newTag  + " voip");
			vty.write("destination-pattern " + destPattern);
			vty.write("session target ipv4:" + sipServer);
			vty.write("voice-class codec 1");
			vty.write("session protocol sipv2");
			vty.write(cli.END);
			
			return true;
		} catch (InterruptedException | OnepException | ExceptionIDL e) {
			// TODO Auto-generated catch block
			 e.printStackTrace();
			 return false;
		}
	}
}
