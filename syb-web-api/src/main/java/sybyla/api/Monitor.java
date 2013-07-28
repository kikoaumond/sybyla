package sybyla.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import sybyla.http.HTTPUtils;

public class Monitor {
	
	private static final Logger LOGGER = Logger.getLogger(Monitor.class);
	
	private static final String HOST_NAME = getHostIdentification();
	private static final String PROCESS_ID=getProcessId();
	
	private static final String[] DISK_USAGE={"df","-h"};
	private static final String[] CPU_USAGE={"ps","-ao", "pcpu,args"};
	
	private static long errorEmailInterval = 900000l; //15 min
	private static long lastEmailSent=0;
	
	private static final Map<String,Integer> requests = new ConcurrentHashMap<String,Integer>();
	private static final Map<String, Long> requestTime = new ConcurrentHashMap<String,Long>();
	private static final ConcurrentLinkedQueue<Request> requestQueue =  new ConcurrentLinkedQueue<Request>();
	private static final ConcurrentLinkedQueue<Request> deniedRequestQueue =  new ConcurrentLinkedQueue<Request>();

	
	public static void monitor(Map<String, String[]> params){
		
		String apiKey = HTTPUtils.getParam(Constants.API_KEY_PARAM, params);
		String requestIP =  HTTPUtils.getParam(Constants.REQUEST_IP, params);
		String requestHost = HTTPUtils.getParam(Constants.REQUEST_HOST, params);
		String originatingIP = HTTPUtils.getParam(Constants.ORIGINATING_IP, params);
		String referrer = HTTPUtils.getParam(Constants.REFERRER, params);

		Request req =new Request(apiKey, requestHost, requestIP, originatingIP, referrer);
		requestQueue.add(req);
	}
	
	public static void monitorDenied(Map<String, String[]> params){
		
		String apiKey = HTTPUtils.getParam(Constants.API_KEY_PARAM, params);
		String requestIP =  HTTPUtils.getParam(Constants.REQUEST_IP, params);
		String requestHost = HTTPUtils.getParam(Constants.REQUEST_HOST, params);
		String originatingIP = HTTPUtils.getParam(Constants.ORIGINATING_IP, params);
		String referrer = HTTPUtils.getParam(Constants.REFERRER, params);

		Request req =new Request(apiKey, requestHost, requestIP, originatingIP, referrer);
		deniedRequestQueue.add(req);
	}
	
	public static synchronized void addRequestTime(String app, long requestInterval){
		
		Integer nRequests = requests.get(app);
		if (nRequests ==  null){
			nRequests = new Integer(0);
		}
		nRequests++;
		requests.put(app, nRequests);
		
		Long avgRequestTime = requestTime.get(app);
		if (avgRequestTime == null){
			avgRequestTime = 0l;
		}
		avgRequestTime = (requestInterval*(nRequests-1) + requestInterval)/nRequests;
		requestTime.put(app, avgRequestTime);
		
	}

	
	public static String checkDiskUsage() throws IOException, InterruptedException{
		String[] command = {"df","-h"};
		return runCommand(command);
	}
	
	
	public static String getCPUUsage() throws IOException, InterruptedException{
		String[] command = {"ps","-ao", "pcpu,args"};
		return runCommand(command);
	}

	
	public static String getProcessId(){
		return ManagementFactory.getRuntimeMXBean().getName();
	}
	
	public static String getHostIdentification() {
		InetAddress address;
		try {
			address = java.net.InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			LOGGER.error("error getting local host name",e);
			return "";
		}

		StringBuilder sb = new StringBuilder();
		
		sb.append("Canonical Host Name: ").append(address.getCanonicalHostName()).append("\n");
		sb.append("Host Name:" + address.getHostName()).append("\n");
		sb.append("Host Address:" + address.getHostAddress()).append("\n");
		
		return sb.toString();
	}
	
	public static String runCommand(String[] command) throws IOException, InterruptedException{
		
		Process process = Runtime.getRuntime().exec(command);
        //Read output
        StringBuilder out = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = null, previous = null;
        while ((line = br.readLine()) != null) {
            if (!line.equals(previous)) {
                previous = line;
                out.append(line).append('\n');
            }
        }
        
        StringBuilder errOut = new StringBuilder();
        BufferedReader errBr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String errLine = null, errPrevious = null;
        while ((errLine = errBr.readLine()) != null) {
            if (!errLine.equals(errPrevious)) {
                errPrevious = errLine;
                errOut.append(errLine).append('\n');
            }
        }
           
		process.waitFor();

        return out.toString();
	}
	
	public static String getHost(){
		return HOST_NAME;
	}
	
	private static class MonitorThread extends Thread{
		String[] command;
		String result;
		
		public MonitorThread(String[] command) {
			this.command=command;
		}
		
		public String toString(){
			StringBuilder sb =  new StringBuilder();
			sb.append("Thread ").append(Thread.currentThread().getId()).append("\n");
			String space="";
			for(String token: command){
				sb.append(space).append(token);
				space=" ";
			}
			return sb.toString();
		}
		
		@Override
		public void run(){
			try {
				runCommand(command);
			} catch (IOException e) {
				LOGGER.error("Error running MonitorThread "+toString(),e);
			} catch (InterruptedException e) {
				LOGGER.error("Error running MonitorThread "+toString(),e);
			}
		}
	}
	
	private static class Request{
		long time;
		String apiKey;
		String remoteHost;
		String remoteIP;
		String originatingIP;
		String referrer;
		
		public Request(String apiKey, String remoteHost, String remoteIP, String originatingIP, String referrer){
			time = new Date().getTime();
			this.apiKey=apiKey;
			this.remoteHost=remoteHost;
			this.remoteIP=remoteIP;
			this.originatingIP=originatingIP;
			this.referrer=referrer;
		}
	}
}
