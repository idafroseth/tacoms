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
import com.cisco.onep.vty.VtyService;

import gui.Logger;

public class Connection {
	//Node connect parameters
	private String routerIP;
    private String username;
    private String password;
    private String pinningFile;
    private NetworkApplication networkApplication;
    
    //Line and connection parameters
    protected NetworkElement networkElement;
	protected VtyService vty;
	
	
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
        try {
        	networkElement = networkApplication.getNetworkElement(routerIP);
        	//Using sessionConfig and certificate based connection to the router - use only username and password if not in the connect.
        	SessionHandle sessionHandle = networkElement.connect(username, password, getSessionConfig());
            vty = new VtyService(networkElement);
        } catch (UnknownHostException | OnepException e){
        	Logger.error( e.getMessage());
        	return false;
        }
        Logger.info("Successful connection to NetworkElement - " + networkElement);
        startConnectionMonitor();
        return true;
    }
	
	/**
	 * This method will return a certificate session config. You have to create a certificate on the router to have this function working.
	 * @return sessionConfig based on TLS (Certificate based connection)
	 */
	private SessionConfig getSessionConfig(){
        Logger.info("Connecting " + routerIP + " using transport type TLS");
        SessionConfig sessionConfig;
        //TLS is the default connection supported
        sessionConfig = new SessionConfig(SessionTransportMode.TLS);
        sessionConfig.setPort(OnepConstants.ONEP_TLS_PORT);
        //Enable tls pinning
        sessionConfig.setTLSPinning(pinningFile, new TLSPinningHandler(pinningFile));
		return sessionConfig;
	}
	
	/**
	 * Open av vty channel to the NE
	 */
	public void vtyOpen(){
		try{
			vty = new VtyService(getNetworkElement());
			vty.open();
		}catch (OnepException | InterruptedException  e) {
			Logger.error("Could not open VTY SERVICE!!");
		}
	}
	/**
	 * Starting a connection monitor that will send a error message to the network manager if the connection is lost. 
	 * This run in a separate thread to not interfere with the other operations. 
	 */
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
					if(!getNetworkElement().isConnected()){
						Logger.error("LOST CONNECTION!");
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
