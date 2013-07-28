package sybyla.controller.impl;

import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import sybyla.http.HTTPUtils;
import sybyla.jaxb.ApiResponse;
import sybyla.jaxb.ObjectFactory;
import sybyla.jaxb.TagResult;
import sybyla.nlp.Language;
import sybyla.tag.Tagger;
import sybyla.tag.Tagger.Tag;
import sybyla.api.Constants;
import sybyla.api.Controller;

public class TagController extends Controller{
	
	public static final String TEXT="text";
	public static final String URL="url";
	public static final String LANGUAGE="lang";
	public static final String PORTUGUESE="pt";
	public static final String ENGLISH="en";
	private ObjectFactory factory = new ObjectFactory();
	
	@Override
	public void checkParams(Map<String, String[]> params)
			throws IllegalArgumentException {
		
		String text = HTTPUtils.getParam(TEXT, params);
		String url = HTTPUtils.getParam(URL, params);
		String language = HTTPUtils.getParam(LANGUAGE, params);
		
		if ( text== null && url == null){
			throw new IllegalArgumentException("The "+TEXT+" parameter must be specified in a call");
		}
		
		if (language!=null && !language.equals(PORTUGUESE) && !language.equals(ENGLISH)){
			throw new IllegalArgumentException("The "+LANGUAGE+" parameter only accepts the values "+ENGLISH+" or "+ PORTUGUESE);
		}
	}	
	
	@Override
	public void process(Map<String, String[]> params, ApiResponse response) {
		
		String text = HTTPUtils.getParam(TEXT, params);
		Tagger tagger = new Tagger();
		String language = HTTPUtils.getParam(LANGUAGE, params);
		Language l = Language.ENGLISH;
		if (language!=null && language.equals(PORTUGUESE)){
			l = Language.PORTUGUESE;
		}
		
		Map<String, List<Tag>> tagMap = tagger.getTagsByType(text,l);
		String type ="NNP";
		if (language != null){
			if (language.equals(PORTUGUESE)){
				type="prop";
			}
		}
		
		List<Tag> tags = tagMap.get(type);
		for (Tag tag: tags){
			double relevance  = tag.getRelevance();
			if (relevance == 0) continue;
			TagResult tagResult =  factory.createTagResult();
			tagResult.setTerm(tag.getTerm());
			tagResult.setRelevance(relevance);
			response.getTags().add(tagResult);
		}		
	}
	
	@Override
	public JSONObject getResultJSON(Map<String, String[]> params) throws JSONException {
		return null;
		
	}

	@Override
	public String getAppName() {
		return Constants.TAG_APP;
	}



}
