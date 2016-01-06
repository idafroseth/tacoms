package manager;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import com.cisco.onep.core.exception.OnepException;

import gui.Logger;
import gui.MainWindow;
import gui.ServiceID;
import gui.ServicesPanel;
import model.Router;

public class TacomsMng {
	
	MainWindow gui;
	ServicesPanel overview;
	String t6ServiceView = "Service SoW6";
	Router router;
	
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
		overview.addContainer(t6ServiceView);
		overview.addService(t6ServiceView,"Autoconnect", ServiceID.AUTOCONNECT, false);
		overview.addService(t6ServiceView, "Sa_bgp", ServiceID.SA_BGP, false);
		overview.addService(t6ServiceView, "NTP", ServiceID.NTP, false);
		
		
		overview.addContainer("TACOMS LOG");
		overview.getCanvas("TACOMS LOG");
		overview.getCanvas("TACOMS LOG").addLogger();
	//	Logger.error("Must go HOME");
		gui.addContentWindow(overview, "Overview");
		//Logger.status("Hello");
	}
	
	
	public void connect(String ip, String userName, String pwd){
		try {
			router = new Router(ip, userName, pwd, "TACOMS");
			router.connect("TACOMS");
			
		} catch (OnepException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public boolean isRouterConnected(){
		if(router == null){
			return false;
		}
		return router.getNetworkElement().isConnected();
	}
	
	public void serviceButtonClicked(String id){
		String en = "_ENABLE";
		String dis = "_DISABLE";
		if(id.equals(ServiceID.AUTOCONNECT + en)){
			if(!isRouterConnected()){
				
			}
			else{
				
			}
			System.out.println("AUTOCONF_ENABLE");
			
		}
		else if(id.equals(ServiceID.AUTOCONNECT+dis)){
			System.out.println("DISABLE_AUTOCONF");
		}
		else if(id.equals(ServiceID.SA_BGP+en)){
			System.out.println("ENBALE_SA");
		}
		else if(id.equals(ServiceID.SA_BGP+dis)){
			System.out.println("DISABLE_SA");
		}
		else if(id.equals(ServiceID.NTP+en)){
			System.out.println("ENABLE NTP");
		}
		
	}
}
