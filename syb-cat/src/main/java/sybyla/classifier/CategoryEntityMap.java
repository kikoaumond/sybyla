package sybyla.classifier;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import sybyla.bytes.ByteArray;

public class CategoryEntityMap {
	
	private static final Logger LOGGER = Logger.getLogger(CategoryEntityMap.class);
	
	private static final String DEFAULT_CATEGORY_ENTITY_MAP_PATH="/mnt/data/current/categoryEntityMap.txt";
	private static final String CATEGORY_ENTITY_MAP_KEY="sybyla.category.entity.map";
	
	private static String categoryEntityMapFile;
	static {
		categoryEntityMapFile = System.getProperty(CATEGORY_ENTITY_MAP_KEY, DEFAULT_CATEGORY_ENTITY_MAP_PATH);
	}
	
	private static final String DEFAULT_SYNONYMS_FILE_PATH="/mnt/data/current/synonyms.txt";
	private static final String SYNONYMS_KEY="sybyla.synonyms";
	
	private static String synonymsFile;
	static {
		//synonymsFile = System.getProperty(SYNONYMS_KEY, DEFAULT_SYNONYMS_FILE_PATH);
	}
	private static  HashMap<Integer, ByteArray> categoryIndex = new HashMap<Integer,ByteArray>();

	private static final HashMap<ByteArray, Set<Integer>> CATEGORY_ENTITY_MAP = loadCategoryEntityMap(categoryEntityMapFile);
	//private static final HashMap<ByteArray, Set<ByteArray>> SYNONYMS_MAP = loadSynonyms(synonymsFile);


	private static HashMap<ByteArray, Set<Integer>> loadCategoryEntityMap(String mapFile) {
		
		LOGGER.info("Loading category entity map in "+mapFile);
		HashMap<ByteArray, Set<Integer>> map = new HashMap<ByteArray, Set<Integer>>();
		int nDiscardedLines=0;
		String line="";
		BufferedReader reader =  null;
		int nBytesTotal=0;
		int nBytes=0;
		int nLines=0;
		
		try{

			InputStream is = new FileInputStream(mapFile);
			reader = new BufferedReader(new InputStreamReader(is,"UTF-8"));
			
			
			while((line = reader.readLine())!=null){
				nLines++;
				String[] tokens = line.split("\\t", -1);
				if (tokens == null || tokens.length<2){
					nDiscardedLines++;
					continue;
				}
				String entity = tokens[0];
				String category = tokens[1];
				
				if (category== null || category.contains("|")){
					continue;
				}
				
				if (!CategoryMap.MAP.containsKey(category)){
					continue;
				}
				entity = CategoryMap.removeParentheses(entity);
				ByteArray e = new ByteArray(entity.toLowerCase().trim());
				ByteArray c = new ByteArray(category);
				int index = c.hashCode();
				
				categoryIndex.put(index, c);
				nBytesTotal+=c.getByteArray().length;
				nBytesTotal+=e.getByteArray().length;
				nBytesTotal+=32;
				nBytes+=c.getByteArray().length;
				nBytes+=e.getByteArray().length;
				nBytes+=32;
				if (nLines%100000==0){
					DecimalFormat byteFormatter = new DecimalFormat("#,###,###,###");
					String b = byteFormatter.format(nBytesTotal);
					DecimalFormat lineFormatter = new DecimalFormat("##,###,###");
					String l = lineFormatter.format(nLines);
					LOGGER.info("Lines: "+l+"   Bytes: "+b);
					nBytes=0;
				}

				Set<Integer> categories  = map.get(e);
				if (categories == null){
					categories = new HashSet<Integer>();
					map.put(e, categories);
				}
				categories.add(index);
			}
			LOGGER.info("Loaded "+map.size()+" category entity entries");
			LOGGER.info(nDiscardedLines +" malforemed lines in file ");

		} catch(Throwable t){
			LOGGER.error("Error loading category map",t );
			LOGGER.error("Line: " +line);
		}finally{
			if (reader!=null){
				try {
					reader.close();
				} catch (IOException e) {
					LOGGER.error("Error closing categoryEntityMap file",e);
				}
			}
		}
		
		return map;
		
	}
	
	private static HashMap<ByteArray, Set<ByteArray>> loadSynonyms(String mapFile) {
		
		HashMap<ByteArray, Set<ByteArray>> map = new HashMap<ByteArray, Set<ByteArray>>();
		
		try{

			InputStream is = new FileInputStream(mapFile);
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String line="";
			
			while((line = reader.readLine())!=null){
				
				String[] tokens = line.split("\\t", -1);
				String entity = tokens[0];
				String synonym = tokens[1];
				
				if (entity!= null && entity.contains("/")){
					continue;
				}
				
				if (synonym!= null && synonym.contains("/")){
					continue;
				}
				ByteArray e = new ByteArray(entity.toLowerCase().trim());
				ByteArray s = new ByteArray(synonym.toLowerCase().trim());
				
				Set<ByteArray> synonyms  = map.get(synonym);
				if (synonyms == null){
					synonyms = new HashSet<ByteArray>();
					map.put(e, synonyms);
				}
				synonyms.add(s);
			}
			LOGGER.info("Loaded "+map.size()+" synonym entries");
		} catch(IOException e){
			LOGGER.error("Error loading category map",e );
		}
		return map;
		
	}
	
	public static Set<Category> getCategories(Collection<String> terms) {
		
		Set<Category> categories = new HashSet<Category>();
		Set<String> categoryNames =  new HashSet<String>();
		
 		for(String term: terms){
 			
			ByteArray t;
			try {
				t = new ByteArray(term.toLowerCase().trim());
			} catch (UnsupportedEncodingException e) {
				LOGGER.error("error looking up categories from entities",e);
				continue;
			}
			Set<Integer> mappedCategories = CATEGORY_ENTITY_MAP.get(t);
			if (mappedCategories != null){
				for(Integer catIndex: mappedCategories){
					ByteArray cat= categoryIndex.get(catIndex);
					if (cat!=null){
						String categoryName =  cat.toString();
						if (!categoryNames.contains(categoryName)){
							categoryNames.add(categoryName);
							Category category = new Category(categoryName, 1);
							categories.add(category);

						}
					} else{
					}
				}
			}
		}
		
		return categories;
		
	}
	
	public static Set<Category> getCategories(Map<String, Integer> termCounts) {
				
		Set<Category> categories = new HashSet<Category>();
		Map<String, Category> categoryMap = new HashMap<String, Category>();
		
		for(String term: termCounts.keySet()){
			
			ByteArray t;
			try {
				t = new ByteArray(term.toLowerCase().trim());
			} catch (UnsupportedEncodingException e) {
				LOGGER.error("error looking up categories from entities",e);
				continue;
			}
			Set<Integer> mappedCategories = CATEGORY_ENTITY_MAP.get(t);
			if (mappedCategories != null){
				for(Integer catIndex: mappedCategories){
					ByteArray cat= categoryIndex.get(catIndex);
					double count = (double)termCounts.get(term);
					Category c = categoryMap.get(cat);
					if (c ==null){
						String category =  cat.toString();
						c = new Category(category, count);
						categoryMap.put(category, c);
					} else{
						double score = c.getScore();
						score += count;
						c.setScore(score);
					}
				}
			}
		}
		
		categories.addAll(categoryMap.values());
		return categories;
		
	}
}
