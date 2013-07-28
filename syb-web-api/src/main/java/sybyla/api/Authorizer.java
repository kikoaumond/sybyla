package sybyla.api;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.eclipse.jetty.util.ConcurrentHashSet;

import sybyla.http.HTTPUtils;
import sybyla.ip.IPRange;
import sybyla.ip.IPRangeChecker;

public class Authorizer {
	
	private static final Logger LOGGER = Logger.getLogger(Authorizer.class);

	protected static ConcurrentHashMap<String,List<IPRange>> ipRanges = new ConcurrentHashMap<String,List<IPRange>>();
	protected static ConcurrentHashMap<String, Quota> quotas = new ConcurrentHashMap<String, Quota>();
	protected static ConcurrentHashSet<String> authorizedKeys = new ConcurrentHashSet<String>();
	
	public static boolean authorize(Map<String, String[]> params) throws UnknownHostException{
		String apiKey = HTTPUtils.getParam(Constants.API_KEY_PARAM, params);
		String ipAddress =  HTTPUtils.getParam(Constants.REQUEST_IP, params);
		return authorize(apiKey,ipAddress);
	}
	
	public static boolean authorize(String apiKey, String ipAddress) throws UnknownHostException{
		
		if (IPRangeChecker.isLocalhost(ipAddress)){
			return true;
		}
		
		if (apiKey ==  null || ipAddress == null){
			return false;
		}
		
		boolean ok =  checkIPRange(apiKey, ipAddress) && checkQuota(apiKey) && checkStatus(apiKey);
		if(ok){
			updateQuota(apiKey);
		}
		return ok;
	}
	
	private static boolean checkIPRange(String apiKey, String ipAddress){
		
		List<IPRange> ranges = ipRanges.get(apiKey);
		
		if ((ranges == null) || ranges.size()==0){
			return true;
		}
		
		for (IPRange range: ranges){
			try {
				if (range.isInRange(ipAddress)){
					return true;
				}
			} catch (UnknownHostException e) {
				LOGGER.error("Error checking IP " + ipAddress + " against range: "+ range);
				return false;
			}
		}
		
		return true;
	}
	
	
	private static boolean checkQuota(String apiKey){
		
		if (apiKey ==  null){
			return false;
		}
		
		Quota quota = quotas.get(apiKey);
		if (quota ==  null){
			return false;
		}
		
		return quota.isWithinQuota();
		
	}
	private static void updateQuota(String apiKey){
		Quota q =  quotas.get(apiKey);
		q.increment();
	}
	
	private static boolean checkStatus(String apiKey){
		return (authorizedKeys.contains(apiKey));
	}
	
	//TODO need to load all this stuff
	private void loadQuotas(){
		
	}
	
	private void loadIPRanges(){
		
	}
	
	private void loadStatus(){
		
	}
	
	private static class Quota{
		private AtomicInteger calls = new AtomicInteger(0);
		private final int limit;
		
		public Quota(int calls, int limit){
			this.calls = new AtomicInteger(calls);
			this.limit = limit;
		}
		
		public int increment(){
			return calls.incrementAndGet();
		}
		
		public boolean isWithinQuota(){
			return calls.get() <= limit;
		}
		
	}
}
