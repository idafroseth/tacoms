package model;

import java.net.UnknownHostException;

import org.slf4j.LoggerFactory;

import com.cisco.onep.core.exception.OnepConnectionException;
import com.cisco.onep.core.exception.OnepException;
import com.cisco.onep.core.util.OnepConstants;
import com.cisco.onep.element.NetworkApplication;
import com.cisco.onep.element.NetworkElement;
import com.cisco.onep.element.SessionConfig;
import com.cisco.onep.element.SessionHandle;
import com.cisco.onep.element.SessionConfig.SessionTransportMode;

import connect.TLSPinningHandler;
import gui.Logger;

public class Connection {
	private String routerIP;
    private String username;
    private String password;
    
    protected NetworkElement networkElement;
    private SessionHandle sessionHandle;
    private SessionConfig sessionConfig;
    private String pinningFile;
    private NetworkApplication networkApplication;
    
    public Connection(String ipaddress, String userName, String password, String applicationName) throws OnepException {
    	
    	pinningFile = "TLS_Pinning2";
    	this.routerIP = ipaddress;
    	this.username = userName;
    	this.password = password;
    	networkApplication = NetworkApplication.getInstance();
         networkApplication.setName(applicationName);
    }
    
    /**
     * Trying to connect to the network element 
     * @param applicationName
     * @return True if the connection success without an exception
     * @throws OnepException
     */
	public boolean connect(String applicationName)  {
		System.out.println("PREFORMING CONNECT");
     
        
        try {
        	networkElement = networkApplication.getNetworkElement(routerIP);
        	System.out.println(networkElement);
        } catch (UnknownHostException e){
        
        	Logger.error(e.getLocalizedMessage(), e.getMessage());
        	return false;
        }catch(	OnepException e) {
         	Logger.error(e.getLocalizedMessage(), e.getMessage());
        	return false;
        }
        
        
        if (networkElement == null) {
            return false;
        }

        Logger.info("Connecting " + routerIP + " using transport type TLS");
        //TLS is the default connection supported
        sessionConfig = new SessionConfig(SessionTransportMode.TLS);
        sessionConfig.setPort(OnepConstants.ONEP_TLS_PORT);
        //Enable tls pinning
        sessionConfig.setTLSPinning(pinningFile, new TLSPinningHandler(pinningFile));

        try {
            // START SNIPPET: connect
        	//her kan vi benytte connect(username, password) hvis vi skal koble til nettverkselementet uten å benytte oss av TLS
        	   System.out.println(username + " " + password + " " +sessionConfig);
        	sessionHandle = networkElement.connect(username, password, sessionConfig);
          //  networkElement.connect
         
            // END SNIPPET: connect
        } catch (OnepException e) {
        	 Logger.error(e.getLocalizedMessage(), e.getMessage());
            return false;
        } 
        if (sessionHandle == null) {
        	Logger.error("LINE76 Connection", "Failed to connect to NetworkElement - " + networkElement);
            return false;
        }
        Logger.info("Successful connection to NetworkElement - " + networkElement);
        startConnectionMonitor();
        return true;
    }
	private void startConnectionMonitor(){
		Thread connMon = new ConnectionMonitor();
		connMon.start();
	}
	
	public NetworkElement getNetworkElement(){
		return this.networkElement;
	}
	
	class ConnectionMonitor extends Thread{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(true){
				try {
					if(!networkElement.isConnected()){
						Logger.error("LOST CONNECTION!!", "LOST CONNECTION!");
					}
				
					Thread.sleep(1000*5);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}

}
