package sybyla.classifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import sybyla.io.FastFileReader;

public class Blacklist {
	private static final Logger LOGGER = Logger.getLogger(Blacklist.class);
	
	private static String blacklistFile="/classificationBlacklistedTerms.txt";

	public static final Set<String> BLACKLIST  = load(blacklistFile);
	
	public static Set<String> load(String blacklistFile) {
		
		Set<String> blacklist = new HashSet<String>();
		
		try{
			InputStream is = Blacklist.class.getResourceAsStream(blacklistFile);

			BufferedReader reader = new BufferedReader(new InputStreamReader(is,"UTF-8"));
			String line="";
			
			while((line = reader.readLine())!=null){
				
				String term = line.trim().toLowerCase();
				blacklist.add(term);
				LOGGER.debug("Loaded blacklisted term: "+term);
			}
		} catch(IOException e){
			LOGGER.error("Error loading blacklist file",e );
		}
		return blacklist;
		
	}
}
