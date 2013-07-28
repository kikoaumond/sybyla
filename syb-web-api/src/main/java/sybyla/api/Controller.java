package sybyla.api;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import sybyla.jaxb.ApiResponse;

public abstract class Controller {
			
	public abstract void process(Map<String, String[]> params, ApiResponse response);
	
	public abstract JSONObject getResultJSON(Map<String, String[]> params) throws JSONException;

	public abstract void checkParams(Map<String, String[]> params) throws IllegalArgumentException;
	
	public abstract String getAppName();
	
}
