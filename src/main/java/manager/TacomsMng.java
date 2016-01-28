package manager;

import java.awt.Dimension;

import javax.swing.JOptionPane;

import com.cisco.onep.core.exception.OnepException;

import gui.Logger;
import gui.LoggingPanel;
import gui.MainWindow;
import gui.ServicesPanel;
import model.Router;
import model.ServiceID;
import serviceMonitor.AutoConnectivityMonitor;
import serviceMonitor.DialPeerMonitor;
import serviceMonitor.SABGPMonitor;

public class TacomsMng {
	
	MainWindow gui;
	ServicesPanel overview;
	String t6ServiceView = "Service SoW6";
	Router router;
	Thread autoconnThread; 
	Thread saBgpThread; 
	Thread dialPeerThread;
	AutoConnectivityMonitor autoconnectMonitor;
	SABGPMonitor saBGPMonitor;
	DialPeerMonitor dialPeerMonitor;
	
	public TacomsMng(){
		Logger.init();
		initMainWindow();
		configureServiceWindow();

		
	
	}

	public static void main(String[] args){
		TacomsMng tacoms = new TacomsMng();
		
	}
	
	public void initMainWindow(){
		this.gui = new MainWindow(this, "Overview");
	}
	
	public void configureServiceWindow(){
		this.overview = new ServicesPanel(this, "Overview");
		LoggingPanel log = new LoggingPanel(this, "LoggingPanel");
		
		overview.addContainer(t6ServiceView);
		overview.addService(t6ServiceView,"Autoconnect", ServiceID.AUTOCONNECT, false);
		overview.addService(t6ServiceView, "SA_BGP", ServiceID.SA_BGP, false);
		overview.addService(t6ServiceView, "DIALPEER", ServiceID.DIALPEER, false);
		
		log.addContainer("TACOMS LOG");
		log.getCanvas("TACOMS LOG");
		log.getCanvas("TACOMS LOG").addLogger();
		
		gui.addContentWindow(overview, "Overview");
		gui.addContentWindow(log, "Log");
	}
	
	
	public void connect(String ip, String userName, String pwd){
		try {
			router = new Router(ip, userName, pwd, "TACOMS");
			router.connect("TACOMS");
			
			AutoConnectivityMonitor autoconnectMonitor = new AutoConnectivityMonitor(router);
			SABGPMonitor saBGPMonitor = new SABGPMonitor(router);
			DialPeerMonitor dialPeerMonitor = new DialPeerMonitor(router);
			
			Thread autoThread = new Thread(autoconnectMonitor);
			Thread saBgpThread = new Thread(saBGPMonitor);
			Thread dialPeerThread = new Thread(dialPeerMonitor);
			
			autoThread.start();
			saBgpThread.start(); 
			dialPeerThread.start();
			
		} catch (OnepException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public boolean isRouterConnected(){
		if(router == null){
			return false;
		}
		if(router.getNetworkElement() == null){
			return false;
		}
		return router.getNetworkElement().isConnected();
	}
	
	public boolean enableService(String id){
		if(!isRouterConnected()){
			Logger.error("Tried to start a service, but you are not connected");
			return false;
		}
		else if(id.equals(ServiceID.AUTOCONNECT.toString())){
			AutoConnectivityMonitor.STARTED = true;
			Logger.info("Started autoconnectMonitor");
		}
		else if(id.equals(ServiceID.SA_BGP.toString())){
			SABGPMonitor.STARTED = true;
			Logger.info("Started saBGPMonitor");
		}
		else if(id.equals(ServiceID.DIALPEER.toString())){
			if(!SABGPMonitor.STARTED){
				JOptionPane.showMessageDialog(gui, "You have to start SA_BGP before DialPeer.");
			}else{
				DialPeerMonitor.STARTED = true;
				Logger.info("Started DialpeerMonitor");
			}
		}		
		return true;
	}
	
	public boolean disableService(String id){
		if(router == null){
			Logger.error("Tried to stop a service, but you are not connected");
			return false;
		}
		else if(id.equals(ServiceID.AUTOCONNECT.toString())){
			Logger.info("Trying to disable AUTOCONN");
			AutoConnectivityMonitor.STARTED = false;
			Logger.info("AUTOCONN disabled");
		}
		else if(id.equals(ServiceID.SA_BGP.toString())){
			Logger.info("Trying to disable SA_BGP");
			SABGPMonitor.STARTED = false;
			Logger.info("SA_BGP disbled");
		}
		else if(id.equals(ServiceID.DIALPEER.toString())){
			DialPeerMonitor.STARTED = false;
		}		
		return true;
	}
}
