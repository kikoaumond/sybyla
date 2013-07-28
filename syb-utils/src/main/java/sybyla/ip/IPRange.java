package sybyla.ip;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IPRange {
	
	private long hi;
	private long lo;
	
	
	public IPRange(String low, String high) throws UnknownHostException {
		
		lo = IPRangeChecker.ipToLong(InetAddress.getByName(low));
		hi = IPRangeChecker.ipToLong(InetAddress.getByName(high));
		
		if (hi < lo){
			long t=hi;
			hi = lo;
			lo = t;
		}
	}
	
	public boolean isInRange(String ip) throws UnknownHostException{
		
		boolean isInRange = IPRangeChecker.isInRange(ip, lo, hi);
		return isInRange;
	}
}
