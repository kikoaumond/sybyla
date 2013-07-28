package sybyla.http;

import java.io.BufferedReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

public class HTTPUtils {
	
    private static final String CONTENT_DISPOSITION="Content-Disposition:";
    private static final String NAME="name";
    private static final String NO_NAME="xxx";
    private static final String BOUNDARY="boundary";
    private static final String BOUNDARY_DELIMITER="--";
    private static final String MULTIPART_FORM_DATA="multipart/form-data";
    
    public static final String REQUEST_URI="requestURI";
    public static final String REQUEST_IP="requestIP";
    public static final String REQUEST_HOST="requestHost";
    public static final String ORIGINATING_IP="originatingIP";
    public static final String REFERRER="referrer";
    public static final String UTF8="UTF8";
    
    public static final short JSON_TYPE=0;
    public static final short XML_TYPE=1;
	
	public static Map<String, String[]> getParameters(HttpServletRequest request) {
	    Map<String, String[]> result = new HashMap<String, String[]>();
	
	    Enumeration<String> paramNames = request.getParameterNames();
	    while (paramNames.hasMoreElements()) {
	        String paramName = paramNames.nextElement();
	        String[] paramValues = request.getParameterValues(paramName);
	        result.put(paramName, paramValues);
	    }
	    
	    return result;
	}
	
	public static Map<String,String[]> getParamsAndHeaders(HttpServletRequest request) throws Exception {
		
		Map<String, String[]> params = HTTPUtils.getParameters(request);
		
		String requestURI = request.getRequestURI();
		if (requestURI !=  null){
			String[] s = {requestURI};
			params.put(REQUEST_URI, s);
		}
		
		String requestIP = getRequestIP(request);
		if (requestIP !=null){
			String[] s = {requestIP};
			params.put(REQUEST_IP, s);
		}
		
		String requestHost = getRequestHost(request);
		if (requestHost != null){
			String[] s= {requestHost};
			params.put(REQUEST_HOST, s);
			
		}
		
		String originatingIP =  getOriginatingIP(request);
		if (originatingIP != null){
			String[] s = {originatingIP};
			params.put(ORIGINATING_IP, s);
		}
		
		String referrer = getReferrer(request);
		if (referrer!=null){
			String[] s= {referrer};
			params.put(REFERRER, s);
		}
		
		if (isMultiPart(request)){
			Map<String,String> multiPartParams =  getMultiPartContent(request);
			for(String name: multiPartParams.keySet()){
				String value = multiPartParams.get(name);
				String[] s = {value};
				params.put(name, s);
			}
		}
		

		
		return params;
	}

	public static String getReferrer(HttpServletRequest request){
		String referrer = request.getHeader("referer"); 
		return referrer;
	}
	
	
	public static String getOriginatingIP(HttpServletRequest request){
		
		String ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");

		if (ipAddress  == null) {
			ipAddress = request.getRemoteAddr();
		}
		
		return ipAddress;
	}
	
	public static String getRequestIP(HttpServletRequest request){
		
		String ipAddress = request.getRemoteAddr();
		return ipAddress;
	}
	
	public static String getRequestHost(HttpServletRequest request){
		
		String host = request.getRemoteHost();
		return host;
	}
	
	public static Map<String, String> extractQueryParamsFromUrl(String queryStr) throws UnsupportedEncodingException {
	    Map<String, String> result = new HashMap<String, String>();
	    
	    if (queryStr ==  null) return result;
	    
	    String[] params = queryStr.split("&");
	    for (String param : params) {
	        String[] keyValue = param.split("=");
	        String key = URLDecoder.decode(keyValue[0], "UTF-8");
	
	        String value = null;
	        if (keyValue.length == 2) {
	            value = URLDecoder.decode(keyValue[1], "UTF-8");
	        }
	        
	        result.put(key, value);
	    }
	    
	    return result;
	}

	public static String getFullUrl(HttpServletRequest req) {
	    String reqUrl = req.getRequestURL().toString();
	    String queryString = req.getQueryString();   
	    if (queryString != null) {
	        reqUrl += "?"+queryString;
	    }
	    
	    return reqUrl;
	}

	public static void setContentType(HttpServletResponse response, short type) {
	    if (type==XML_TYPE) {
	        response.setContentType("text/xml; charset=UTF-8");
	    } else {
	        response.setContentType("application/json; charset=UTF-8");
	    }
	    
	    response.setCharacterEncoding(UTF8);
	}

	public static boolean isMultiPart(HttpServletRequest request){
		String contentType = request.getContentType();
	    
	    if (contentType==null || contentType.trim().equals("") || !contentType.contains(MULTIPART_FORM_DATA)) {
	        return false;
	    }
	    
	    return true;
	}
	
	public static  Map<String,String> getMultiPartContent(HttpServletRequest request) throws Exception {
	    
		Map<String,String> content = new HashMap<String,String>();
	    String contentType = request.getContentType();
	    
		if (isMultiPart(request)){
	    	return content;
	    }
	    
	    
	    String boundary=null;
	    String boundaryStop=null;
	    
	    int boundaryIdx = contentType.indexOf(BOUNDARY);
	    if(boundaryIdx > 0) {
	        boundaryIdx += BOUNDARY.length()+1;
	        boundary = BOUNDARY_DELIMITER + contentType.substring(boundaryIdx);
	        boundaryStop = boundary + BOUNDARY_DELIMITER;
	    }
	    
	    if (boundary == null) return content;
	    
	    BufferedReader reader = request.getReader();
	    
	    boolean end = false;
	    boolean read = false;
	    String name="";
	    StringBuffer value = new StringBuffer("");
	    StringBuffer multiPart =  new StringBuffer("");
	    String line;
	    do{
	        line = reader.readLine();
	        if (line == null) break;
	        multiPart.append(line).append("\n");
	        if (line.equals(boundaryStop)) {
	            end = true;
	        }
	        if (line.contains(boundary)) {
	            if (read) {
	                content.put(name, value.toString());
	                read =  false;
	            } 
	            if (end) break;
	            continue;
	        } 
	        
	        if (line.startsWith(CONTENT_DISPOSITION)) {                
	            
	            name = NO_NAME;
	            value.delete(0, value.length());
	            read = true;
	            
	            int nameIndex = line.indexOf(NAME);
	            if (nameIndex > 0) {
	                nameIndex += NAME.length()+1;
	                name = line.substring(nameIndex);
	                name =  name.replace('\"',' ').trim();
	            }
	            
	            continue;
	        }
	        if (read) {
	            value.append(line); 
	            value.append(" ");
	        }
	    }while (line!=null); 
	    
	    if (!end) {
	        throw new Exception("Multipart request has no end delimiter.  Expected "+ boundaryStop + "\nRequest:\n"+multiPart.toString());
	    }
	    reader.close();
	    
	    return content;
	}
	
    public static String getParam(String name, Map<String,String[]> params){
    	String[] s =  params.get(name);
    	String param =  null;
    	if (s!=null && s.length>0){
    		param = s[0];
    	}
    	return param;
    }
    
	public static String wrapCallback(String callback, JSONObject json){
		
		String output = json.toString();
		if (callback!=null){
        	output = callback + "(" + output + ");";
        }
		return output;
	}
	
	public static String wrapCallback(String callback, String json){
		
		if (callback!=null){
        	String output = callback + "(" + json + ");";
        	return output;
        }
		return json;
	}
	
}
