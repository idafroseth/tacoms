package serviceMonitor;

import com.jhlabs.image.FieldWarpFilter.Line;

public class CRLMonitor implements Runnable{

	//Poll the local CRL to check if the crl is revocated, if that is the case it will start with R
	
	@Override
	public void run() {
		//HTTP get to the server to get the crl
	}
	
	public void readFile(){
		String line = "";
		if(line.startsWith("R")){
			//The certificated is revocated - remove the tunnel(?)
		}
	}
	
}
