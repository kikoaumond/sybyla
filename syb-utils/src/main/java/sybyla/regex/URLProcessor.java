package sybyla.regex;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class URLProcessor {

	  private Pattern pattern;
	  private Matcher matcher;
	  private static final Logger LOGGER = Logger.getLogger(URLProcessor.class);

	  private static final String URL_PATTERN = 
			  "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

	  public URLProcessor(){
		  pattern = Pattern.compile(URL_PATTERN);
	  }

	  /**
	   * Validate hex with regular expression
	   * @param hex hex for validation
	   * @return true valid hex, false invalid hex
	   */
	  public boolean validate(final String hex){

		  matcher = pattern.matcher(hex);
		  return matcher.matches();
	  }
	  
	  public Set<String> getURLs(String s){
		  Set<String> urls = new HashSet<String>();
		  matcher = pattern.matcher(s);
		  while (matcher.find()){
			 int g = matcher.groupCount();
			 String url =  matcher.group(g);
			 urls.add(url);
		  }
		  
		  return urls;
	  }
	  
	  public String addParam(String url, Map<String, String> params){
		  
		  StringBuffer sb =  new StringBuffer(url);
		  
		  if (url.indexOf('?')==-1){
			  sb.append("?");
		  }
		  
		  char and='\0';
		  if(!url.endsWith("?")) {
			  and='&';
		  }
		  
		  for(String name:params.keySet()){
			  String value = params.get(name);
			  try {
				String n = URLEncoder.encode(name, "UTF-8");
				String v = URLEncoder.encode(value,"UTF-8");
				sb.append(and).append(n).append("=").append(v);
				if(and=='\0'){
					and='&';
				}
			} catch (UnsupportedEncodingException e) {
				LOGGER.error("Error adding parameter to URL",e);
				return null;
			}
		  }
		  return sb.toString();
	  }
}
