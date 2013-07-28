package sybyla.classifier;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class GeoLocator {
	private static final String COUNTRY_LIST_FILE="/countryList.txt";

	private static final String CITIES=" cities";
	private static final String CITY=" city";
	private static final String METROPOLITAN_AREA="metropolitan area";
	private static final String TOWN=" town";
	private static final String COUNTY=" county";
	private static final String COUNTIES=" counties";
	private static final String PREFECTURE="prefecture";
	private static final String OBLAST="oblast";
	private static final String VOIVODESHIP="voivodeship";
	private static final String REGION="region";
	private static final String PROVINCE="province";
	private static final String STATE="state";
	/*
	 * regexes:
	 * <...>of <name> county...
	 * <...>of county <name>...
	 * ...(cities|towns) of <...>
	 * <...> district
	 * ...regions of <...> (country)
	 *  
	 */
	
	private static final Map<String,Country> COUNTRY_MAP=new HashMap<String, Country>();
	
	private String getDetail(String category){
		
		String c =  category.toLowerCase();
		if (c.contains(TOWN)){
			return TOWN;
		}
		if (c.contains(CITIES)||c.contains(CITY)){
			return CITY;
		}
		if (c.contains(METROPOLITAN_AREA)){
			return METROPOLITAN_AREA;
		}
		if (c.contains(PREFECTURE)){
			return PREFECTURE;
		}
		if (c.contains(COUNTIES) ||c.contains(COUNTY)){
			return COUNTY;
		}
		if (c.contains(REGION)){
			return REGION;
		}
		if (c.contains(PROVINCE)){
			return PROVINCE;
		}
		if (c.contains(STATE)){
			return STATE;
		}
		return null;
	}
	
	private Country getCountry(String category){
		
		for(String countryName: COUNTRY_MAP.keySet()){
			Country country = COUNTRY_MAP.get(countryName);
			if (category.contains(countryName)){
				return country;
			}
			String adjective = country.getAdjective();
			if (adjective != null){
				if (category.contains(adjective)){
					return country;
				}
			}
		}
		return null;
	}

	public static final String[] SUPER_REGIONS={"Africa", 
		 "Asia", 
		 "Australia",
		 "Antarctica", 
		 "Greenland", 
		 "Europe", 
		 "Middle East",
		 "North America", 
		 "Central America", 
		 "South America",
		 "Oceania",
		 "Caribbean"};
	
	private void loadCountries() throws Exception{
		InputStream is = CategoryMap.class.getResourceAsStream(COUNTRY_LIST_FILE);
		BufferedReader reader = new BufferedReader(new InputStreamReader(is,"UTF-8"));
		String line="";
		
		while((line = reader.readLine())!=null){
			String[] tokens =  line.split("\\t");
			if (tokens.length<2) {
				throw new Exception("Malformed line in country list");
			}
			String countryName = tokens[0];
			String regions =  tokens[1];
			String adjective = null;
			if (tokens.length == 3){
				adjective =  tokens[2];
			}
			Country country = new Country(countryName);
			String[] r  = regions.split(",");
			for (int i=0; i < r.length; i++){
				String superRegion = getSuperRegion(r[i]);
				if (superRegion != null){
					country.setSuperRegion(superRegion);
					break;
				}
			}
			if (adjective !=  null){
				country.setAdjective(adjective);
			}
		} 
	}
	
	private String getSuperRegion(String region){
		for(int i=0; i<SUPER_REGIONS.length;i++){
			String superRegion =  SUPER_REGIONS[i];
			if (region.contains(superRegion)){
				return superRegion;
			}
		}
		return null;
	}
	
	private static class Country{
		

		private String name;
		private String superRegion;
		private String region;
		private String adjective;
		
		public Country(String name){
			this.name  =  name;
		}
		
		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public String getContinent() {
			return superRegion;
		}
		
		public void setSuperRegion(String superRegion) {
			this.superRegion = superRegion;
		}
		
		public String getRegion() {
			return region;
		}
		
		public void setRegion(String region) {
			this.region = region;
		}
		
		public String getAdjective() {
			return adjective;
		}
		
		public void setAdjective(String adjective) {
			this.adjective = adjective;
		}
	}
}
