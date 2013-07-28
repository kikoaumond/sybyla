package sybyla.ip;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

public class IPRangeChecker {
	
	    public static long ipToLong(InetAddress ip) {
	        byte[] octets = ip.getAddress();
	        long result = 0;
	        for (byte octet : octets) {
	            result <<= 8;
	            result |= octet & 0xff;
	        }
	        return result;
	    }
	    
	    public static boolean isInRange(String ip, String ipLow, String ipHigh) throws UnknownHostException{
	    	
	    	long l = ipToLong(InetAddress.getByName(ipLow));
	    	long h = ipToLong(InetAddress.getByName(ipHigh));
	    	long t =  ipToLong(InetAddress.getByName(ip));
	    	boolean isInRange = t >= l && t <= h;
	    	return isInRange;
	    }
	    
	    public static boolean isInRange(String ip, long ipLow, long ipHigh) throws UnknownHostException{
	    	
	    	long t =  ipToLong(InetAddress.getByName(ip));
	    	boolean isInRange = t >= ipLow && t <= ipHigh;
	    	return isInRange;
	    }
	    
	    public static boolean isLocalhost(String ip) throws UnknownHostException {
	    	
	    	InetAddress addr = InetAddress.getByName(ip);
	        // Check if the address is a valid special local or loop back
	        if (addr.isAnyLocalAddress() || addr.isLoopbackAddress())
	            return true;

	        // Check if the address is defined on any interface
	        try {
	            return NetworkInterface.getByInetAddress(addr) != null;
	        } catch (SocketException e) {
	            return false;
	        }
	    }
}
