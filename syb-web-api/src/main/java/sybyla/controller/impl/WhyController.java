package sybyla.controller.impl;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import sybyla.jaxb.ApiResponse;
import sybyla.jaxb.ObjectFactory;
import sybyla.jaxb.WhyResult;
import sybyla.api.Constants;
import sybyla.api.Controller;
import sybyla.http.HTTPUtils;

public class WhyController extends Controller{
	
	private static final Logger LOGGER = Logger.getLogger(WhyController.class);
	
	private ObjectFactory objectFactory= new ObjectFactory();
	
	public static final String TERM1="term1";
	public static final String TERM2="term2";
	
	@Override
	public void process(Map<String, String[]> params, ApiResponse response) {

		String term1 = HTTPUtils.getParam(TERM1, params);
		String term2 = HTTPUtils.getParam(TERM2, params);
		
		String url=null;
		try {
			url =  buildURL(term1, term2);
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("Error generating Why URL",e);
		}
		
		List<WhyLink> links = new ArrayList<WhyLink>();
		
		try {
			
			JSONObject json = run(url);
			JSONObject d= json.getJSONObject("d");
			JSONArray results = d.getJSONArray("results");
			
			for(int i=0; i<results.length();i++){
				if (i>10) break;
				int rank = 0;
				JSONObject result = results.getJSONObject(i);
				
				String title = result.getString("Title");
				String description = result.getString("Description");
				String displayUrl = result.getString("DisplayUrl");
				
				if (title.contains(term1)){
					rank =  rank+2;
				}
				
				if (title.contains(term2)){
					rank = rank+2;
				}
				
				if (description.contains(term1)){
					rank =  rank+1;
				}
				
				if (description.contains(term2)){
					rank = rank+1;
				}
				
				WhyLink whyLink = new WhyLink(displayUrl,description, rank);
				links.add(whyLink);
			}
			
			Collections.sort(links,Collections.reverseOrder());
			WhyLink whyLink = links.get(0);
			
			WhyResult why = objectFactory.createWhyResult();
			String t = whyLink.text;
			
			String link="<a href=\"http://"+whyLink.url+"\"target=\"_blank\">" + t + "</a>";
			why.setLink(link);
			response.setWhy(why);
			
		} catch (JSONException e) {
			LOGGER.error("Error generating Why URL",e);
		}
		
	}

	@Override
	public JSONObject getResultJSON(Map<String, String[]> params)
			throws JSONException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void checkParams(Map<String, String[]> params)
			throws IllegalArgumentException {
		
		String term1 = HTTPUtils.getParam(TERM1, params);
		String term2 = HTTPUtils.getParam(TERM1, params);
		if (term1 == null || term2== null){
			throw new IllegalArgumentException("Both "+TERM1+" and "+TERM2  +
					" parameters must be specified in a call");
		}
		
	}

	@Override
	public String getAppName() {
		return Constants.WHY_APP;
	}
	
	private String buildURL(String term1, String term2) throws UnsupportedEncodingException{
		String query = URLEncoder.encode("'\""+term1+"\" & \""+term2+"\"'","UTF-8");
		String urlStr="https://api.datamarket.azure.com/Bing/Search/v1/Web?$format=json&Query="+query;
		return urlStr;
	}
	

	private JSONObject run(String urlStr) throws JSONException {
	      String accountKey="8uFglROxxOLfMvypepsQNRF3+zpvORgyKu1ttf/UTDw=";
	      byte[] accountKeyBytes = Base64.encodeBase64((accountKey + ":" + accountKey).getBytes());
	      String accountKeyEnc = new String(accountKeyBytes);
	
		  HttpURLConnection connection = null;
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
	          connection.setDoOutput(false);
	          connection.setRequestProperty("Authorization", "Basic " + accountKeyEnc);
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
	          JSONObject json= new JSONObject(response);
	          return json;
	                    
	      } catch (MalformedURLException e) {
	          LOGGER.error("error running Why query",e);
	          return null;
	      } catch (ProtocolException e) {
	          LOGGER.error("error running Why query",e);
	          return null;
	      } catch (IOException e) {
	          LOGGER.error("error running Why query",e);
	          return null;
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
	
	private static class WhyLink implements Comparable<WhyLink>{
		private String url;
		private String text;
		private int rank;
		
		public WhyLink(String url, String text, int rank){
			this.url =  url;
			this.text = text;
			this.rank = rank;
		}
		
		@Override
		public int compareTo(WhyLink l) {
			
			if (this.rank > l.rank){
				return 1;
			} else if (this.rank < l.rank){
				return -1;
			}
			return 0;
		}
		
	}
}
