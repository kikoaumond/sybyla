package sybyla.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import sybyla.json.JSONUtils;


public class Client {
	
	public static final String GET="GET";
	public static final String POST="POST";
	public static final String URL="url";
	public static final String JSON="JSON";
	public static final String XML="XML";
	
	private static final Logger LOGGER = Logger.getLogger(Client.class);
	
	private String url;
	private String method=POST;
	private String urlParams="";
	private UrlEncodedFormEntity formParams;
	private String responseContent;
	private int responseStatusCode;
	private String responseStatusLine;
	private String responseType;
	private Header[] headers;
	private String encoding="UTF-8";
	
	public Client(String url){
		this.url =  url;
		this.method = GET;
	}
	
	public Client(String url, Map<String,String> params, String method)  {
		this.url = url;
		this.method = method;
		if (method.equals(GET)){
			urlParams = buildGETParams(params);
		}
		if (method.equals(POST)){
			formParams = buildPOSTParams(params);
		}
	}
	
	public void setParams(Map<String,String> params){
		if (method.equals(GET)){
			urlParams = buildGETParams(params);
		}
		if (method.equals(POST)){
			formParams = buildPOSTParams(params);
		}
	}
	
	public String buildGETParams(Map<String, String> params)  {
		if (params ==  null || params.size()==0) {
			return "";
		}
		List<NameValuePair> paramList  = new ArrayList<NameValuePair>();
		StringBuffer sb = new StringBuffer("?");
		String a="";
		
		for(String paramName: params.keySet()) {
			String paramValue =  params.get(paramName);
			paramList.add(new BasicNameValuePair(paramName, paramValue));
			sb.append(a);
			a="&";
			
			try {
				sb.append(URLEncoder.encode(paramName, encoding));
				sb.append("=");
				sb.append(URLEncoder.encode(paramValue, encoding));	
			} catch (UnsupportedEncodingException e) {
				LOGGER.error("Error URL eencoding string "+paramName+"="+paramValue,e);
			}
			
		}
		return sb.toString();
	}
	
	public static String buildURL(String url, Map<String, String> params) throws UnsupportedEncodingException {
		if (params ==  null || params.size()==0) {
			return "";
		}
		StringBuffer sb = new StringBuffer(url+"?");
		String a="";
		
		for(String paramName: params.keySet()) {
			String paramValue =  params.get(paramName);
			sb.append(a);
			a="&";
			
			sb.append(URLEncoder.encode(paramName, "UTF-8"));
			
			sb.append("=");
			sb.append(URLEncoder.encode(paramValue,"UTF-8"));				
		}
		String u = sb.toString();
		return u;
	}
	
	public UrlEncodedFormEntity buildPOSTParams(Map<String, String> params)   {
		
		List<NameValuePair> paramList  = new ArrayList<NameValuePair>();
		for(String paramName: params.keySet()) {
			String paramValue =  params.get(paramName);
			paramList.add(new BasicNameValuePair(paramName, paramValue));
			
		}
		
		try {
			formParams = new UrlEncodedFormEntity(paramList,encoding);
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("error building POST parameters",e);
		}
		return formParams;
	}
	
	
	public void run() {
				
		HttpClient client =  new DefaultHttpClient();
		HttpGet get = null;
		HttpPost post = null;
		HttpResponse response = null;
		if (method.equalsIgnoreCase(POST)) {
			post = new HttpPost(url);
			if(formParams != null) {
				post.setEntity(formParams);
			}
			try {
				response = client.execute(post);
					
			} catch(Throwable t) {
				LOGGER.error("error executing HTTP call",t);
				return;
			}
		}
		if (method.toUpperCase().equals(GET)){
			get = new HttpGet(url+urlParams);
			try {
				response = client.execute(get);
					
			} catch(Throwable t) {
				LOGGER.error("error executing HTTP call",t);
				return;
			}
		}

		responseStatusLine = response.getStatusLine().getReasonPhrase();
		responseStatusCode = response.getStatusLine().getStatusCode();
		headers = response.getAllHeaders();
		
		HttpEntity entity = response.getEntity();
		Header responseTypeHeader = entity.getContentType();
		if (responseTypeHeader != null){
			responseType = entity.getContentType().getValue();
		}
		
		InputStream instream = null;
		if (entity != null) {
		     try {
		    	 instream = entity.getContent();
		         BufferedReader reader = new BufferedReader(
		                 new InputStreamReader(instream));
		         String line=null;
		         StringBuilder sb = new StringBuilder();
		         do{
		        	 line =reader.readLine();
		        	 if (line==null){
		        		 break;
		        	 }
		        	 sb.append(line);
		        	 sb.append('\n');
		         } while(line!=null);
		         
		         responseContent=sb.toString().trim();
		         
		     } catch (IOException ioe) {

		         LOGGER.error("Error issuing HTTP call", ioe);
		         return;

		     } catch (RuntimeException rte) {

		         // In case of an unexpected exception you may want to abort
		         // the HTTP request in order to shut down the underlying
		         // connection and release it back to the connection manager.
		    	 if (get !=  null) {
		    		 get.abort();
		    	 }
		    	 
		    	 if (post !=null) {
		    		 post.abort();
		    	 }
		    	 LOGGER.error("Error issuing HTTP call", rte);
		    	 return;

		     } finally {

		         // Closing the input stream will trigger connection release
		         try {
					instream.close();
				} catch (IOException ioe) {
					LOGGER.error("Error closing HTTP connection", ioe);
				}

		     }
		}
	}	
	
	protected String processJSONResponse(String responseElement) throws JSONException {
		
		JSONObject o = new JSONObject(responseContent);
		String result = JSONUtils.findInPath(o, responseElement);
		return result;
		
	}
	
	public String getResponseContent() {
		return responseContent;
	}

	public int getResponseStatusCode() {
		return responseStatusCode;
	}
	
	public String getResponseStatusLine() {
		return responseStatusLine;
	}

	public String getResponseType() {
		return responseType;
	}

	public Header[] getHeaders() {
		return headers;
	}
}
