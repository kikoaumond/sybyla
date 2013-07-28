package sybyla.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;


public class GraphIntegrationTest {
	
	private static String server="ec2-23-20-235-232.compute-1.amazonaws.com";
	private static String port="8984";
	
	public static void main(String[] args){
		GraphIntegrationTest git =  new GraphIntegrationTest();
		git.testTerm();
		git.testURL();
	}
	
	public void testTerm() {
	    
		String urlStr=null;
		String callback=null;
		
		try {
			callback = URLEncoder.encode("myCallbackFunction","UTF-8");

			urlStr = "http://"+server+":"+port+"/graph/?callback=" + callback +
			  		"&t="+URLEncoder.encode("Renato Aragão","UTF-8");
			run(urlStr, callback);
			urlStr = "http://"+server+":"+port+"/graph/?callback=" + callback +
			  		"&t="+URLEncoder.encode("Bill Clinton","UTF-8");
			run(urlStr, callback);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	  }
	
	public void testURL() {
	    
		String urlStr=null;
		
		try {
			String url = URLEncoder.encode("http://en.wikipedia.org/wiki/Bill_Clinton","UTF-8");
			urlStr = "http://"+server+":"+port+"/graph/?url="+url;
			run(urlStr, null);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	public void run(String urlStr, String callback) {
	      
		  HttpURLConnection connection = null;
	     // OutputStreamWriter wr = null;
	      BufferedReader rd  = null;
	      StringBuilder sb = null;
	      String line = null;
	    
	      URL serverAddress = null;
	    
	      try {
	          serverAddress = new URL(urlStr);
	          //set up out communications stuff
	          connection = null;
	        
	          //Set up the initial connection
	          connection = (HttpURLConnection)serverAddress.openConnection();
	          connection.setRequestMethod("GET");
	          connection.setDoOutput(true);
	          connection.setReadTimeout(300000);
	          connection.setConnectTimeout(300000);
	                    
	          connection.connect();
	        
	          //get the output stream writer and write the output to the server
	          //not needed in this example
	          //wr = new OutputStreamWriter(connection.getOutputStream());
	          //wr.write("");
	          //wr.flush();
	        
	          //read the result from the server
	          rd  = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	          sb = new StringBuilder();
	        
	          while ((line = rd.readLine()) != null)
	          {
	              sb.append(line + '\n');
	          }
	        
	          String response = sb.toString().trim();
	          System.out.println(sb.toString());
	          
	          String json= response;
	          if (callback!=null){
		          int begin =  response.indexOf("{");
		          int end = response.indexOf(");");
		          json = response.substring(begin,end);
		          System.out.println(json);
	          }
	          	                    
	      } catch (MalformedURLException e) {
	          e.printStackTrace();
	      } catch (ProtocolException e) {
	          e.printStackTrace();
	      } catch (IOException e) {
	          e.printStackTrace();
	      }
	      finally
	      {
	          //close the connection, set all objects to null
	          connection.disconnect();
	          rd = null;
	          sb = null;
	          //wr = null;
	          connection = null;
	      }
	  }
}
