package sybyla.controller.impl;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import sybyla.http.HTTPUtils;
import sybyla.jaxb.ApiResponse;
import sybyla.jaxb.ObjectFactory;
import sybyla.jaxb.SentimentResult;

import sybyla.sentiment.Analyzer;
import sybyla.sentiment.Language;
import sybyla.sentiment.Type;

import sybyla.api.Constants;
import sybyla.api.Controller;

public class SentimentController extends Controller{
	
	public static final String TEXT="text";
	public static final String URL="url";
	public static final String LANGUAGE="lang";
	public static final String PORTUGUESE="pt";
	public static final String ENGLISH="en";
	public static final String TYPE="type";
	private ObjectFactory factory = new ObjectFactory();
	
	@Override
	public void checkParams(Map<String, String[]> params)
			throws IllegalArgumentException {
		
		String text = HTTPUtils.getParam(TEXT, params);
		String language = HTTPUtils.getParam(LANGUAGE, params);
		
		if ( text== null ){
			throw new IllegalArgumentException("The "+TEXT+" parameter must be specified in a call");
		}
		
		if (language!=null && !language.equals(PORTUGUESE) && !language.equals(ENGLISH)){
			throw new IllegalArgumentException("The "+LANGUAGE+" parameter only accepts the values "+ENGLISH+" or "+ PORTUGUESE);
		}
	}	
	
	@Override
	public void process(Map<String, String[]> params, ApiResponse response)  {
		
		String text = HTTPUtils.getParam(TEXT, params);
		String language = HTTPUtils.getParam(LANGUAGE, params);
		Language l = Language.ENGLISH;
		if (language!=null && language.equals(PORTUGUESE)){
			l = Language.PORTUGUESE;
		}
		
		Analyzer analyzer;
		try {
			analyzer = new Analyzer(l,Type.PRODUCT);
			double score = analyzer.analyze(text);
			SentimentResult result = factory.createSentimentResult();
			
			result.setSentiment(score);
			result.setText(text);
			response.setSentiment(result);
		} catch (Exception e) {

		}
		
		
	}
	
	@Override
	public JSONObject getResultJSON(Map<String, String[]> params) throws JSONException {
		return null;
		
	}

	@Override
	public String getAppName() {
		return Constants.SENTIMENT_APP;
	}



}
