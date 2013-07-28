package sybyla.regex;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class LinkProcessor {
	private Pattern pattern;
	private Matcher matcher;
	private static final Logger LOGGER = Logger.getLogger(URLProcessor.class);
	public static String LINK_PATTERN = "\\s*(?i)href\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))";

	
	public LinkProcessor(){
		  pattern = Pattern.compile(LINK_PATTERN);
	}
	
	public Set<String> getLinks(String s){
		Set<String> links = new HashSet<String>();
		matcher = pattern.matcher(s);
		while (matcher.find()){
			//int g = matcher.groupCount();
			String url =  matcher.group(1);
			links.add(url);
		}
		  
		return links;
	  }
	  
	public String addParam(String url, Map<String,String> params)   {
		  return addParam(url, params, true);
	 }
	  
	 public String addParam(String url, Map<String, String> params, boolean urlEncode) {
		 //remove double quotes
		 StringBuffer sb =  new StringBuffer(url.substring(1,url.length()-1));
		 char and='\0';
		 if (url.indexOf('?')==-1){
			 and='?';
		 } else {
			 and='&';
		 }
		  
		 List<String> keys =  new ArrayList<String>();
		 keys.addAll(params.keySet());
		 Collections.sort(keys);
		  
		 for(String name:keys){
			 
			 String value = params.get(name);
				 
			 String n =  name;
			 String v =  value;
				 
			 if (urlEncode){
				 try {
					n = URLEncoder.encode(name,"UTF-8");
					v = URLEncoder.encode(value, "UTF-8");	 

				} catch (UnsupportedEncodingException e) {
					LOGGER.error("Error url-encoding", e);
				}
			 }
				 
			 sb.append(and).append(n).append("=").append(v);
			 if(and=='?'){
				 and='&';
			 }
			 
		 }
		 return "\""+sb.toString()+"\"";
	 }
	 
	 public String rewriteLinks(String html, Map<String,String> params){
		 return rewriteLinks( html, params, true);
	 }
	 
	 public String rewriteLinks(String html, Map<String,String> params, boolean urlEncode)  {
		  
		 String s =  new String (html);
		  
		 Set<String> links = getLinks(html);
		   
		 for(String link: links){
			 String rewrittenLink =  addParam(link, params, urlEncode);
			 s = s.replace(link, rewrittenLink);
		 }
		  
		 return s;	  
	 }
	  
	 
	

}
