/**
 * 
 */
package extractor;

import java.net.InetAddress;
import org.slf4j.*;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * @author Cisco
 * 
 */
public class Extractor {
	private String routeType;
	private int mainAsn;
	private int subAsn;
	private String IPv4Address;
	private String IPv4AddressMinusEn;
	private String[] hextettArray;
	private String ebgpHop;
	private int[] octett4 = new int[4];
	private int[] octett6 = new int[4];
	private String phoneNumber;
	Logger logger;
	

	public int[] getOctett4() {
		return octett4;
	}
	public int[] getOctett6() {
		return octett6;
	}

	public String[] octettExtractor(String ipv4Adr) {
		String[] oktett = null;
		if (ipv4Adr.contains(".")) {
			oktett = ipv4Adr.split("\\.");
//			for (int i = 0; i< oktett.length; i++){
//				octett4[i] = Integer.parseInt(oktett[i]);
//			}
//
//			this.mainAsn = octett4[1];
//			this.subAsn = octett4[2];
//			this.IPv4Address = octett4[0] + "." + octett4[1] + "." + octett4[2] + "." + octett4[3];
//			this.IPv4AddressMinusEn = octett4[0] + "." + octett4[1] + "."	+ octett4[2] + "." + (octett4[3] - 1);
		} else {
			System.out.println("This was not an IPv4 address, since it did not contain .");
		}
		return oktett;
	}

	//Henter ut ndvendig info fra en ipv6 addr og logger dette i en Array[]: 
	public String[] hexExtractor(InetAddress ipv6Adr, Logger l ){
		String[] hexArray = ipv6Adr.toString().split(":");
		if(hexArray.length>0){
			this.routeType = hexArray[1]+"";
		}
		    
		byte[] negBytes = ipv6Adr.getAddress();//ipv6Adr.getAddress();
		Integer[] bytes = new Integer[negBytes.length];
		for(int i = 0; i<negBytes.length; i++){
			bytes[i] = negBytes[i]&0xff;
		}
		int lengde = bytes.length;
		if(bytes.length>8){
			this.IPv4Address = bytes[bytes.length-4] + "." + bytes[bytes.length-3] + "." + bytes[bytes.length-2] + "." + bytes[bytes.length-1];
			this.IPv4AddressMinusEn = bytes[bytes.length-4] + "." + bytes[bytes.length-3] + "." + bytes[bytes.length-2] + "." + Integer.toString(bytes[bytes.length-1]-1);
			this.mainAsn = bytes[bytes.length-7];
			this.subAsn = bytes[bytes.length-5] + bytes[bytes.length-6];
			this.ebgpHop = ""+bytes[bytes.length-9];
		}else{
			System.out.println("Extractor kunne ikke kjenne igjen dette formatet");
		}
		String[]sum = {this.routeType,""+this.mainAsn,""+this.subAsn,this.IPv4Address,this.IPv4AddressMinusEn, this.ebgpHop}; 
		return sum;
		
	}
	
	public String[] phoneExtractor(String phone, InetAddress nH, int mask){
		String[] pNr = phone.split(":");
		//System.out.println(nH);
		String phoneNr ="";
		String nextHop = "";
		Integer[] nhBytes;
		try{
			byte[] nhBytesNeg = nH.getAddress();
			nhBytes = new Integer[nhBytesNeg.length];
			for(int i = 0; i<nhBytesNeg.length; i++){
				nhBytes[i] = (nhBytesNeg[i])&0xff;
			}
			
		}catch(NullPointerException e){
			return null;
		}
		
		for(int i = 2; i<mask/16; i++){
			phoneNr += pNr[i];
		}
	//	System.out.println(pNr);
		nextHop = nhBytes[nhBytes.length-4] +"."+ nhBytes[nhBytes.length-3] +"."+ nhBytes[nhBytes.length-2] +"."+ nhBytes[nhBytes.length-1];

		String[] retur = {phoneNr, nextHop};
		return retur;
		
	}
		
	public static void main(String[] args) throws UnknownHostException {
		System.out.println("Welcome to extractor");
	//	Extractor ipv6 = new Extractor();
		//System.out.println("The phone number is: " + ipv6.phoneExtractor("FD00:4:1:941:321:3:2:3211"));
		
	//	System.out.println(Arrays.toString(ipv6.hexExtractor("::FFFF:aaaa:bbbb")));
		//System.out.println(Arrays.toString(ipv6.extractor("D00:510::2E:1:2F01:101")));
		//ipv6.IPv4Extractor("D00:510::2E:1:2F01:101");
		
		//String ipv6Adr = "D00:510::2E:1:2F01:101";
	
	}
	public Logger getLogger(){
		return this.logger;
	}

}