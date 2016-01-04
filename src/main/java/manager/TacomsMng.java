package manager;

import javax.swing.JPanel;

import gui.MainWindow;
import gui.ServiceID;
import gui.ServicesPanel;

public class TacomsMng {
	
	MainWindow gui;
	ServicesPanel overview;
	String t6ServiceView = "Service SoW6";
	
	public TacomsMng(){
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
		gui.addContentWindow(overview, "Overview");
	}
	
	
	
	public void serviceButtonClicked(String id){
		String en = "_ENABLE";
		String dis = "_DISABLE";
		if(id.equals(ServiceID.AUTOCONNECT + en)){
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
