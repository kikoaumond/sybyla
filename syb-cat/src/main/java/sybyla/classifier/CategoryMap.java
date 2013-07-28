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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import sybyla.io.FastFileReader;

public class CategoryMap {
	private static final Logger LOGGER = Logger.getLogger(CategoryMap.class);
	
	public static final String CATEGORY="category";
	public static final String DISPLAY_CATEGORY="displayCategory";
	public static final String DISPLAY_CATEGORY_DETAIL="displayCategoryDetail";
	public static final String GEO="geo";
	public static final String GEO_DETAIL="geoDetail";
	public static final String CHRONO="chrono";
	public static final String CHRONO_DETAIL="chronoDetail";
	public static final String IAB="iab";
	public static final String IAB_DETAIL="iabDetail";
	public static final String SYBYLA="sybyla";
	
	private static String mapFile="/categoryMap.txt";
    private static final Pattern PARENTHESIS_REGEX = Pattern.compile( "\\(.*?\\)");


	protected static final HashMap<String, Set<CategoryMapEntry>> MAP = load(mapFile);
	
	
	public static HashMap<String, Set<CategoryMapEntry>> load(String mapFile) {
		
		HashMap<String, Set<CategoryMapEntry>> map 
							= new HashMap<String, Set<CategoryMapEntry>>();
		String line="";
		int nLines=0;

		try{

			InputStream is = CategoryMap.class.getResourceAsStream(mapFile);
			BufferedReader reader = new BufferedReader(new InputStreamReader(is,"UTF-8"));
			
			while((line = reader.readLine())!=null){
				nLines++;
				String[] tokens = line.split("\\t", -1);
				String category = tokens[0];
				String displayCategory = tokens[1];
				String displayCategoryDetail = tokens[2];
				String geo = tokens[3];
				String geoDetail= tokens[4];
				String chrono=tokens[5];
				String chronoDetail=tokens[6];
				String iab=tokens[7];
				String iabDetail=tokens[8];
				
				CategoryMapEntry catEntry = new CategoryMapEntry( displayCategory,  displayCategoryDetail,
								  						    geo,  geoDetail, 
								  						    chrono,  chronoDetail,
								  						    iab,  iabDetail);
				
				Set<CategoryMapEntry> entries = map.get(category);
				
				if (entries == null){
					entries = new HashSet<CategoryMapEntry>();
					map.put(category, entries);
				}
				entries.add(catEntry);
			}
		} catch(Throwable t){
			LOGGER.error("Error loading category map: line"+nLines+": "+ line,t );
		}
		return map;
		
	}
	
	public static String removeParentheses(String text) {
	    Matcher matcher = PARENTHESIS_REGEX.matcher(text);
	    String t = matcher.replaceAll("");
	    return t.trim();
	}
	
	public static Set<CategoryMapEntry> getCategoryEntry(String category){
		return  MAP.get(category);
	}
	
	public static class CategoryMapEntry implements Comparable<CategoryMapEntry>{
		private String displayCategory;
		private String displayCategoryDetail;
		private String geo;
		private String geoDetail;
		private String chrono;
		private String chronoDetail;
		private String iab;
		private String iabDetail;
		public double score=0;
		
		public CategoryMapEntry(String displayCategory, String displayCategoryDetail,
							 String geo, String geoDetail, 
							 String chrono, String chronoDetail,
							 String iab, String iabDetail){
			
			this.displayCategory = displayCategory;
			this.displayCategoryDetail = displayCategoryDetail;
			this.geo = geo;
			this.geoDetail = geoDetail;
			this.chrono = chrono;
			this.chronoDetail = chronoDetail;
			this.iab = iab;
			this.iabDetail = iabDetail;
			
		}
		
		@Override
		public boolean equals(Object o) {
			if (!(o instanceof CategoryMapEntry)){
				return false;
			}
			
			CategoryMapEntry ce = (CategoryMapEntry) o;
			
			if (   !(this.displayCategory == null && ce.displayCategory == null)
				|| !(this.displayCategory != null && ce.displayCategory != null) 
				|| !this.displayCategory.equals(ce.displayCategory)){
				return false;
			}
			
			if (   !(this.displayCategoryDetail == null && ce.displayCategoryDetail == null)
					|| !(this.displayCategoryDetail != null && ce.displayCategoryDetail != null) 
					|| !this.displayCategoryDetail.equals(ce.displayCategoryDetail)){
					return false;
			}
			
			if (   !(this.geo == null && ce.geo == null)
					|| !(this.geo != null && ce.geo != null) 
					|| !this.geo.equals(ce.geo)){
					return false;
			}
			
			if (   !(this.geoDetail == null && ce.geoDetail == null)
					|| !(this.geoDetail != null && ce.geoDetail != null) 
					|| !this.geoDetail.equals(ce.geoDetail)){
					return false;
			}
			
			if (   !(this.chrono == null && ce.chrono == null)
					|| !(this.chrono != null && ce.chrono != null) 
					|| !this.chrono.equals(ce.chrono)){
					return false;
			}
			
			if (   !(this.chronoDetail == null && ce.chronoDetail == null)
					|| !(this.chronoDetail != null && ce.chronoDetail != null) 
					|| !this.chronoDetail.equals(ce.chronoDetail)){
					return false;
			}
			
			if (   !(this.iab == null && ce.iab == null)
					|| !(this.iab != null && ce.iab != null) 
					|| !this.iab.equals(ce.iab)){
					return false;
			}
			
			if (   !(this.iabDetail == null && ce.iabDetail == null)
					|| !(this.iabDetail != null && ce.iabDetail != null) 
					|| !this.iabDetail.equals(ce.iabDetail)){
					return false;
			}
			
			return true;
			
		}
		
		public String getDisplayCategory() {
			return displayCategory;
		}

		public String getDisplayCategoryDetail() {
			return displayCategoryDetail;
		}

		public String getGeo() {
			return geo;
		}

		public String getGeoDetail() {
			return geoDetail;
		}

		public String getChrono() {
			return chrono;
		}

		public String getChronoDetail() {
			return chronoDetail;
		}

		public String getIab() {
			return iab;
		}

		public String getIabDetail() {
			return iabDetail;
		}

		public JSONObject toJSON() throws JSONException{
			
			JSONObject o =  new JSONObject();
			
			if (displayCategory != null){
				o.put(CATEGORY, displayCategory);
			} else {
				if (iab != null){
					o.put(CATEGORY, iab);
				}
			}
			
			if (displayCategoryDetail != null){
				o.put(DISPLAY_CATEGORY_DETAIL, displayCategoryDetail);
			}
			
			if (geo != null){
				o.put(GEO, chrono);
			}
			if (geoDetail != null){
				o.put(GEO_DETAIL, geoDetail);
			}
			
			if (chrono != null){
				o.put(CHRONO, chrono);
			}
			if (chronoDetail != null){
				o.put(CHRONO_DETAIL, chronoDetail);
			}
			
			if (iab != null){
				o.put(IAB, iab);
			}
			if (iabDetail != null){
				o.put(IAB_DETAIL, iabDetail);
			}
			
			return o;
		}

		@Override
		public int compareTo(CategoryMapEntry e) {
			if (this.score>e.score){
				return 1;
			} else if (this.score<e.score){
				return -1;
			}
			return 0;
		}
	}
}
