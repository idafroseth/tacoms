package autoconnectivity;

import org.slf4j.Logger;
import connect.*;

import com.cisco.onep.core.exception.OnepConnectionException;
import com.cisco.onep.core.exception.OnepException;
import com.cisco.onep.core.exception.OnepIllegalArgumentException;
import com.cisco.onep.core.exception.OnepRemoteProcedureException;
import com.cisco.onep.element.NetworkElement;
import com.cisco.onep.idl.ExceptionIDL;
import com.cisco.onep.routing.RIB;
import com.cisco.onep.vty.VtyService;

public class TunnelConfiguration {
	Logger log;
	private CiscoCLI cli = new CiscoCLI();
	private VtyService vtyService;
	private String iPv6Adr;
	
	public void handler(String[] octett, NetworkElement e, Logger log, RIB.RouteState state){
		this.log = log;
		if(state == RIB.RouteState.UP){
			if(Integer.parseInt(octett[1])>47){
				System.out.println(Integer.parseInt(octett[1]));
				addSlaveNode(octett,e);
			}else if(Integer.parseInt(octett[1])<47){
				System.out.println(Integer.parseInt(octett[1]));
				addMasterNode(octett,e);
			}
		}else if(state == RIB.RouteState.DOWN){
			removeNeighbor(octett,e);
		}
	}
	public boolean addMasterNode(String[] octett, NetworkElement e){
		try {
			this.vtyService = new VtyService(e);
			vtyService.open();
		
			getLogger().info("We are a masternode");	
			String phyDest = "111." + octett[1] + "." + octett[2]+ "." + octett[3];	
			String iPv6Adr = "FD00:202::2F:1:114.47." + octett[1] + "." + Integer.parseInt(octett[2]) * 4  + "/127";
			getLogger().info("The tunnel did not exist so we are about to create the tunnel");	
  
			vtyService.write(cli.CONFT);
			vtyService.write("crypto isakmp key TAC9MS! address " + phyDest);
			getLogger().info("interface tunnel" + octett[1] );
			vtyService.write("interface tunnel" + octett[1] );
			vtyService.write("ip address " + "114.47."+ octett[1] + "." + Integer.parseInt(octett[2]) * 4 + 2 + " 255.255.255.252");
			vtyService.write("tunnel destination " + phyDest);
			vtyService.write("tunnel source 111.47.1.1");
			vtyService.write("ipv6 address " + iPv6Adr);
			getLogger().info("*************************************ipv6 address " + iPv6Adr);
			System.out.println("**************************");
			vtyService.write(cli.ENIPV6);
			vtyService.write("ipv6 rip TACOMS enable");
			vtyService.write("tunnel protection ipsec profile TACOMS");
			
			vtyService.write(cli.END);
			vtyService.write(cli.CONFT);
			vtyService.write(cli.GORIPV6);
			vtyService.write("distribute-list prefix-list " + octett[1] +" out tunnel " + octett[1]);
			vtyService.write(cli.END);
			vtyService.write(cli.CONFT);
			vtyService.write("ipv6 prefix-list " + octett[1] + " permit FD00:510:0:2:2F:1:2F01:1/128");
			vtyService.write("ipv6 prefix-list " + octett[1] + " permit FD00:511::2F:1:2F01:2/128");
			vtyService.write("ipv6 prefix-list " + octett[1] + " permit " + iPv6Adr);
			vtyService.write("ipv6 prefix-list " + octett[1] + " permit FD00:500:0:2:2F:1:2F01:1/128");
			vtyService.write(cli.END);
			getLogger().info("You have just created a new Tunnel with ID " + octett[1]);
			
		    vtyService.cancel();
		    vtyService.close();
		} catch (OnepConnectionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (OnepIllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (OnepRemoteProcedureException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (OnepException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ExceptionIDL e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return true;
	}
	
	public boolean addSlaveNode(String[] octett, NetworkElement e){
		try {
			this.vtyService = new VtyService(e);
			vtyService.open();
			getLogger().info("We are a slave node");
			String phyDest = "111." + octett[1] + "." + octett[2]+ "." + octett[3];	
			vtyService.write(cli.CONFT);
			vtyService.write("crypto isakmp key TAC9MS! address " + phyDest);
			vtyService.write("interface tunnel" + octett[1] );
			vtyService.write("ip unnumbered loopback 47");
			vtyService.write("tunnel destination " + phyDest);
			vtyService.write("tunnel source 111.47.1.1");
			vtyService.write(cli.ENIPV6);
			vtyService.write("ipv6 rip TACOMS enable");
			vtyService.write("tunnel protection ipsec profile TACOMS");
			vtyService.write(cli.END);
			vtyService.write(cli.CONFT);
			vtyService.write(cli.GORIPV6);
			vtyService.write("distribute-list prefix-list " + octett[1] +" out tunnel " + octett[1]);
			vtyService.write(cli.END);
			vtyService.write(cli.CONFT);
			vtyService.write("ipv6 prefix-list " + octett[1] + " permit FD00:510:0:2:2F:1:2F01:1/128");
			vtyService.write("ipv6 prefix-list " + octett[1] + " permit FD00:511::2F:1:2F01:2/128");
			vtyService.write("ipv6 prefix-list " + octett[1] + " permit FD00:500:0:2:2F:1:2F01:1/128");
			vtyService.write(cli.END);
			getLogger().info("You have just created a new Tunnel with ID " + octett[1] );
			
		    vtyService.cancel();
		    vtyService.close();
		} catch (OnepConnectionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (OnepIllegalArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (OnepRemoteProcedureException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (OnepException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ExceptionIDL e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return true;
	}
	
	public boolean removeNeighbor(String[] octett, NetworkElement e){
		try {
			vtyService = new VtyService(e);
			vtyService.open(); 
		   	/*
		  	 * Remove tunnel with the right ID
		  	 */
			 String phyDest = "111." + octett[1] + "." + octett[2] + "." + octett[3]; 
		  
			 getLogger().info("The route went down so we are about to remove the tunnel and IPsec config");
			 vtyService.write(cli.CONFT);
			 vtyService.write("no interface tunnel " + octett[1] );
			 vtyService.write("no crypto isakmp key TAC9MS! address " + phyDest);
			 vtyService.write("no ipv6 prefix-list " + octett[1]);
			 vtyService.write(cli.GORIPV6);
			 vtyService.write("no distribute-list prefix-list " + octett[1] +" out tunnel " + octett[1] );
			 vtyService.write(cli.END);
		  	
			 getLogger().info("Is Open after removing tunnel? " + vtyService.isOpen());
			 vtyService.cancel();
			 vtyService.close();
			 return true;
		 } catch (Exception T) {
			 getLogger().error(T.getLocalizedMessage(), T);
			 return false;
		 }
	}

	public Logger getLogger() {
	     return this.log;
	}
}
