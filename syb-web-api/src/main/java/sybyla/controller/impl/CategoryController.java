package sybyla.controller.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import sybyla.api.Constants;
import sybyla.api.Controller;
import sybyla.classifier.Category;
import sybyla.classifier.CategoryMap;
import sybyla.classifier.CategoryMap.CategoryMapEntry;
import sybyla.classifier.Classifier;
import sybyla.http.HTTPUtils;
import sybyla.jaxb.ApiResponse;
import sybyla.jaxb.CategoryResult;
import sybyla.jaxb.ObjectFactory;

public class CategoryController extends Controller{
	public static final Logger LOGGER =  Logger.getLogger(CategoryController.class);	
	public static final String TEXT="text";
	public static final String URL="url";
	static{
		Classifier.init();
	}

	private ObjectFactory factory = new ObjectFactory();
	
	@Override
	public void checkParams(Map<String, String[]> params)
			throws IllegalArgumentException {
		
		String text = HTTPUtils.getParam(TEXT, params);
		String url = HTTPUtils.getParam(URL, params);
		
		if ( text== null && url == null){
			throw new IllegalArgumentException("The "+TEXT+" parameter must be specified in a call");
		}
	}	
	
	@Override
	public void process(Map<String, String[]> params, ApiResponse response) {
		
		String text = HTTPUtils.getParam(TEXT, params);
		Classifier classifier = new Classifier();
		List<Category> categories = classifier.classify(text);
		Set<String> unique = new HashSet<String>();

		int nCats=0;
		int maxCats=2;
		
		for (int i=0; i< categories.size();i++){
			//if (nCats >= maxCats){
			//	break;
			//}
			Category category = categories.get(i);
			double score = category.getScore();
			String categoryName = category.getName();
			Set<CategoryMapEntry>  entries= CategoryMap.getCategoryEntry(categoryName);
			if (entries != null && entries.size()>0){
				nCats++;
			}
			for (CategoryMapEntry entry: entries){
				
				CategoryResult sybylaCat =  null;
				CategoryResult iabCat =  null;

				String name  = entry.getDisplayCategory();
				String iab =  entry.getIab();
				
				if ((name == null && iab == null) || (name.equals("") && iab.equals(""))){
					nCats--;
					continue;
				}
				
				if (name!=null && !name.equals("") && !name.equals(iab) && !unique.contains(name)){
					unique.add(name);
					sybylaCat = factory.createCategoryResult();
					sybylaCat.setCategory(name);
					sybylaCat.setType(CategoryMap.SYBYLA);
					sybylaCat.setRelevance(score);
					response.getCategories().add(sybylaCat);
					
				}
				
				if (iab!=null && !iab.equals("") && !iab.equals(name) && !unique.contains(iab)){
					unique.add(iab);
					iabCat = factory.createCategoryResult();
					iabCat.setCategory(iab);
					iabCat.setType(CategoryMap.IAB);
					iabCat.setRelevance(score);
					response.getCategories().add(iabCat);
					
				} 
				
			}
		}
		Comparator<CategoryResult> reverseScoreComparator = new Comparator<CategoryResult>(){

			@Override
			public int compare(CategoryResult c1, CategoryResult c2) {
				if (c1.getRelevance() < c2.getRelevance()){
					return 1;
				} else if (c1.getRelevance() > c2.getRelevance()){
					return -1;
				}
				return 0;
			}
		};
		Collections.sort(response.getCategories(), reverseScoreComparator);
	}
	
	@Override
	public JSONObject getResultJSON(Map<String, String[]> params) throws JSONException {
		
		return null;
		
	}

	@Override
	public String getAppName() {
		return Constants.CATEGORY_APP;
	}

	
	public void processOld(Map<String, String[]> params, ApiResponse response) {
		
		String text = HTTPUtils.getParam(TEXT, params);
		Classifier classifier = new Classifier();
		List<Category> categories = classifier.classify(text);
		Map<String, CategoryResult> scoreMap = new HashMap<String, CategoryResult>();
		for (Category category: categories){
			
			String categoryName = category.getName();
			Set<CategoryMapEntry>  entries= CategoryMap.getCategoryEntry(categoryName);
		
			for (CategoryMapEntry entry: entries){
				
				CategoryResult sybylaCat =  null;
				CategoryResult iabCat =  null;
	
				String name  = entry.getDisplayCategory();
				String iab =  entry.getIab();
				
				if ((name == null && iab == null) || (name.equals("") && iab.equals(""))){
					continue;
				}
				
				if (name!=null && !name.equals("") && !name.equals(iab)){
					sybylaCat = factory.createCategoryResult();
					sybylaCat.setCategory(name);
					sybylaCat.setType(CategoryMap.SYBYLA);
					
				}
				
				if (iab!=null && !iab.equals("") && !iab.equals(name)){
					iabCat = factory.createCategoryResult();
					iabCat.setCategory(iab);
					iabCat.setType(CategoryMap.IAB);
					
				} 
				
				if (sybylaCat != null){
					String nameDetail = entry.getDisplayCategoryDetail();
					sybylaCat.setCategoryDetail(nameDetail);
				}
				
				if (iabCat != null){
					String iabDetail = entry.getIabDetail();
					iabCat.setCategoryDetail(iabDetail);
				}
				
				String geo =  entry.getGeo();
				if (geo!=null){
					if (sybylaCat != null ) {
						sybylaCat.setGeo(geo);
					}
			
					if (iabCat !=  null){
						iabCat.setGeo(geo);
					}
				}
				
				String geoDetail =  entry.getGeoDetail();
				if (geoDetail !=null){
					if (sybylaCat != null ) {
						sybylaCat.setGeoDetail(geoDetail);
					}
			
					if (iabCat != null){
						iabCat.setGeoDetail(geoDetail);
					}				
				}
			
				String chrono = entry.getChrono();
				if (chrono !=null){
					if (sybylaCat != null ) {
						sybylaCat.setChrono(chrono);
					}
			
					if (iabCat != null){
						iabCat.setChrono(chrono);
					}				
				}
				
				String chronoDetail = entry.getChronoDetail();
				if (chronoDetail !=null){
					if (sybylaCat != null ) {
						sybylaCat.setChronoDetail(chronoDetail);
					}
			
					if (iabCat != null){
						iabCat.setChronoDetail(chronoDetail);
					}				
				}
	
				double score = category.getScore();
				if (sybylaCat != null){
					sybylaCat.setRelevance(score);
					String cat = sybylaCat.getCategory();
					String key =  cat;
					String detail = sybylaCat.getCategoryDetail();
					if (detail != null) {
						key = key+"-"+detail;
					} 
					CategoryResult r = scoreMap.get(key);
					if (r == null){
						scoreMap.put(key,  sybylaCat);
						response.getCategories().add(sybylaCat);
					} else{
						double s = r.getRelevance();
						s += sybylaCat.getRelevance();
						r.setRelevance(s);
					}
					
				}
				if (iabCat != null){
					iabCat.setRelevance(score);
					String cat = iabCat.getCategory();
					String key =  cat;
					String detail = iabCat.getCategoryDetail();
					if (detail != null) {
						key = key+"-"+detail;
					} 
					CategoryResult r = scoreMap.get(key);
					if (r == null){
						scoreMap.put(key,  iabCat);
						response.getCategories().add(iabCat);
					} else{
						double s = r.getRelevance();
						s += iabCat.getRelevance();
						r.setRelevance(s);
					}
				}
			}		
		}
		
		Comparator<CategoryResult> reverseScoreComparator = new Comparator<CategoryResult>(){
	
			@Override
			public int compare(CategoryResult c1, CategoryResult c2) {
				if (c1.getRelevance() < c2.getRelevance()){
					return 1;
				} else if (c1.getRelevance() > c2.getRelevance()){
					return -1;
				}
				return 0;
			}
		};
		Collections.sort(response.getCategories(), reverseScoreComparator);
	}
}
