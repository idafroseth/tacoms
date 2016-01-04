package autoconnectivity;

import com.cisco.onep.element.NetworkElement;
import com.cisco.onep.vty.VtyService;
import org.slf4j.Logger;

import connect.*;

public class CLBGP {
	VtyService vtyService6;
	Logger logger; 
	CiscoCLI cli = new CiscoCLI();
	public CLBGP(Logger l){
		this.logger = l;
	}
	public void configure(String[] hexArray, NetworkElement ne){
		try {
			vtyService6 = new VtyService(ne);
			vtyService6.open();
			
			String routeType = hexArray[0];
			String ipv4Peer = hexArray[3];
			String ipv4TuAdress = hexArray[4];
		   	/*
		  	 * If this is a route type 202, it is a GRE prefix
		  	 */
			if (routeType.equals("202")){
				if((Integer.parseInt(hexArray[1])>cli.MAINAS)){

					 
					 String setIPadr = 
			
					 vtyService6.write(cli.CONFT);
					 vtyService6.write("interface tunnel " + hexArray[1]);
					 vtyService6.write("ip address " + ipv4TuAdress + " 255.255.255.252");
					 vtyService6.write(cli.END);
			  }
		 /*
		  * If this is a route type 510, it is a BGP prefix
		  */
			} else if (routeType.equals("510")){
			    getLogger().info("It is a BGP prefix so configure the BGP table to peer " + ipv4Peer + " with ASN " + hexArray[1] + "." + hexArray[2]);
			    
				String neighASN = hexArray[1] + "." + hexArray[2];					 
			  	String staticIPRoute = "ip route " + ipv4Peer + " 255.255.255.255 tu " + hexArray[1];
			  	String setBGPRemoteAs = "neighbor " + ipv4Peer + " remote-as " + neighASN;
			  	String setBGPMultihop = "neighbor " + ipv4Peer + " ebgp-multihop 4";
			  	String activatePeer = "neighbor " + ipv4Peer + " activate";
			  	vtyService6.write(cli.END);
			  	vtyService6.write(cli.CONFT);
			  	getLogger().info("******IS THIS CORRECT*******: " + staticIPRoute);
			  	vtyService6.write(staticIPRoute);
			  	vtyService6.write("neighbor " + ipv4Peer + " remote-as " + neighASN + " update-so 47");
	  	        vtyService6.write(cli.GOCLBGP);
	  	        vtyService6.write(setBGPRemoteAs);
	  	        vtyService6.write(cli.GOBGPV4);
	  	        vtyService6.write(activatePeer);
			    vtyService6.write(setBGPMultihop);
		
			  	vtyService6.write(cli.END);
		 
		 /*
		  * If this is a route type 511, it is a msdp prefix
		  */
			} else if (routeType.equals("511")){
				getLogger().info("It is a msdp prefix so configure multicast to peer " + ipv4Peer);
				String setMSDPpeer = "ip msdp peer " + ipv4Peer + " connect-source loop 472";
				vtyService6.write(cli.CONFT);
				vtyService6.write(setMSDPpeer);
				vtyService6.write(cli.END);
			   	
			}	
		 vtyService6.cancel();
		 vtyService6.close();
		} catch (Exception e) {
	         getLogger().error(e.getLocalizedMessage(), e);
		}
	}
	public void remove(String[] hexArray, NetworkElement ne){
	 try {
		 
		 vtyService6 = new VtyService(ne);
		 vtyService6.open();

//		 String neighID = Integer.toString(mainASN)+Integer.toString(subASN);
		 String routeType = hexArray[0];
	     String neighASN = hexArray[1] + "." + hexArray[2];
   		 String ipv4Peer = hexArray[3];
   		 //String ipv4TuAdress = hexArray[1] + "." + hexArray[2] + "." + octett[2] + "." + (octett[3]-1);
   		 
		 if (routeType.equals("202")){
			 getLogger().info("Do nothing, the tunnel would be removed if the tunnel goes down.");
		 /*
		  * Check if it is a BGP type packet
		  */
		 } else if (routeType.equals("510")){	 	
			 String removeBGPPeer = "no neighbor " + ipv4Peer + " remote-as " + neighASN; 
			 String removeStaticIPRoute = "no ip route " + ipv4Peer + " 255.255.255.255 Tunnel " + hexArray[1];
			 
			 vtyService6.write(cli.CONFT);
			 vtyService6.write(cli.GOCLBGP);
			 vtyService6.write(removeBGPPeer);
			 vtyService6.write(cli.EXIT);
			 vtyService6.write(removeStaticIPRoute);
			 vtyService6.write(cli.END);
			 
		 } else if (routeType.equals("511")){
			 /*
			  * If this is a route type 510, it is a BGP prefix
			  */
			 getLogger().info("we are about to remove msdp");
			 String removeMSDPpeer =  vtyService6.write(cli.CONFT);
			 vtyService6.write("no ip msdp peer " + ipv4Peer + " connect-source tu " + hexArray[1] );
			 vtyService6.write(cli.END);
			 getLogger().info("MSDP is removed");
		 }
		 vtyService6.cancel();
		 vtyService6.close();
	  } catch (Exception e) {
	         getLogger().error(e.getLocalizedMessage(), e);
	  }
	}
	public Logger getLogger(){
		return this.logger;
	}
}
