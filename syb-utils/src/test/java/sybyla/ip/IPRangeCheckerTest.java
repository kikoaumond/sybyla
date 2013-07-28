package sybyla.ip;

import static org.junit.Assert.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Test;

public class IPRangeCheckerTest {

	@Test
	public void testLongInRange() throws UnknownHostException {
	    
	        long ipLow = IPRangeChecker.ipToLong(InetAddress.getByName("192.200.0.0"));
	        long ipHigh = IPRangeChecker.ipToLong(InetAddress.getByName("192.255.0.0"));

	        boolean inRange = IPRangeChecker.isInRange("192.200.3.0", ipLow, ipHigh);
	        assertTrue(inRange);
	}
	
	@Test
	public void testLongOutOfRange() throws UnknownHostException {
	    
	        long ipLow = IPRangeChecker.ipToLong(InetAddress.getByName("192.200.0.0"));
	        long ipHigh = IPRangeChecker.ipToLong(InetAddress.getByName("192.255.0.0"));

	        boolean inRange = IPRangeChecker.isInRange("192.255.3.0", ipLow, ipHigh);
	        assertFalse(inRange);
	}
	
	@Test
	public void testStringInRange() throws UnknownHostException {
	    
	        boolean inRange = IPRangeChecker.isInRange("192.200.3.0", "192.200.0.0", "192.255.0.0");
	        assertTrue(inRange);
	}
	
	@Test
	public void testStringOutOfRange() throws UnknownHostException {


	        boolean inRange = IPRangeChecker.isInRange("192.255.3.0", "192.200.0.0", "192.255.0.0");
	        assertFalse(inRange);
	}

}
