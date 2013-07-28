package sybyla.json;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONUtils {

	public static String findFirstBreadthFirst(JSONObject o, String s) throws JSONException {
		if (o.has(s)){
			return o.getString(s);
		}
		
		JSONArray a = o.names();
		
		for(int i=0; i<a.length(); i++){
			String k = a.getString(i);
			String v = o.getString(k);
			if(isJSONObject(v)){
				JSONObject oo = new JSONObject(v);
				String ss =  findFirstBreadthFirst(oo,s);
				if (ss!=null){
					return ss;
				}
			}
		}
		return null;
	}
		
	public static List<String> findAllBreadthFirst(JSONObject o, String s) throws JSONException {
		List<String> list = null;
		
		
		JSONArray a = o.names();
		boolean found = false;
		for(int i=0; i<a.length(); i++){
			if (!found && o.has(s)){
				String v = o.getString(s);
				list = new ArrayList<String>();
				list.add(v);
				found = true;
			}
			String k = a.getString(i);
			String v = o.getString(k);
			
			if(isJSONObject(v)){
				List<String> ll =  findAllBreadthFirst(new JSONObject(v),s);
				if (ll!=null){
					list.addAll(ll);
				}
			}
		}
		return list;
		
	}

	public static String findInPath(JSONObject o, String path) throws JSONException{
		if (path ==  null || path.trim().equals("")){
			return null;
		}
		
		String[] elements = path.split("\\.");
		JSONObject oo = o;
		String s=null;
		
		for(int i=0; i<elements.length; i++){
			if (elements[i].trim().equals("")){
				break;
			}
			s = findFirstBreadthFirst(oo,elements[i]);
			if(s!=null && isJSONObject(s)) {
				oo = new JSONObject(s);
			} else {
				break;
			}
		}
		return s;
	}
	public static boolean isJSONObject(String s){
		return(s.trim().startsWith("{"));
	}
	
	public static boolean isJSONArray(String s){
		return(s.trim().startsWith("["));
	}
	
	public static boolean isElement(String s){
		 
		return(!isJSONObject(s) && !isJSONArray(s));
	}
}
