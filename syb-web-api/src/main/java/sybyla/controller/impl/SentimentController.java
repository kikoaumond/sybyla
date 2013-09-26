package sybyla.controller.impl;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import sybyla.http.HTTPUtils;
import sybyla.jaxb.ApiResponse;
import sybyla.jaxb.ObjectFactory;
import sybyla.jaxb.Ok;
import sybyla.jaxb.SentimentResult;

import sybyla.sentiment.Analyzer;
import sybyla.sentiment.Language;
import sybyla.sentiment.Result;
import sybyla.sentiment.Type;

import sybyla.api.Constants;
import sybyla.api.Controller;

public class SentimentController extends Controller{
	private static final Logger LOGGER = Logger.getLogger(SentimentController.class);
	public static final String TEXT="text";
	public static final String URL="url";
	public static final String LANGUAGE="lang";
	public static final String PORTUGUESE="pt";
	public static final String ENGLISH="en";
	public static final String TYPE="type";
	public static final String CUSTOMER_KEY="customerKey";
	public static final String CONTEXT="context";
	public static final String SENTIMENT="sentiment";
	public static final String ADD="add";
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
		
		String add = HTTPUtils.getParam(ADD, params);
		if (add != null){
			
			String sentiment =  HTTPUtils.getParam(SENTIMENT, params);

			if (sentiment == null){
				throw new IllegalArgumentException("If the "+ ADD + " parameter is specified, the "+
									SENTIMENT + " parameter and the "+ TEXT + " parameter must be specified as well\n" +
											TEXT+": "+ text+"  \n"+SENTIMENT+ ": "+ sentiment);
			} else {
				try{
					int s =  Integer.parseInt(sentiment);
				} catch(Exception e){
					throw new IllegalArgumentException("The parameter "+ SENTIMENT+" must be -1, 0 or 1");
				}
			}
		}
	}	
	
	@Override
	public void process(Map<String, String[]> params, ApiResponse response)  {
		
		String add =  HTTPUtils.getParam(ADD,params);
		if(add != null){
			add(params,response);
			return;
		}
		
		String text = HTTPUtils.getParam(TEXT, params);
		String language = HTTPUtils.getParam(LANGUAGE, params);
		Language l = Language.ENGLISH;
		if (language!=null && language.equals(PORTUGUESE)){
			l = Language.PORTUGUESE;
		}
		
		Analyzer analyzer;
		try {
			
			analyzer = new Analyzer(l,Type.PRODUCT);
			
			Result result = analyzer.analyze(text);
			DecimalFormat df = new DecimalFormat( "#.##" );
			double score = Double.parseDouble(df.format(result.getResult()));
			double certainty = Double.parseDouble(df.format(result.getCertainty()));
			
			SentimentResult sr = factory.createSentimentResult();
			
			sr.setCertainty(certainty);
			sr.setScore(score);
			sr.setText(text);
			
			if (score < 0) {
				sr.setSentiment("negative");
			} else if (score == 0) {
				sr.setSentiment("neutral");
			} else if (score > 0){
				sr.setSentiment("positive");
			}
			
			response.setSentimentResult(sr);
		} catch (Exception e) {
			LOGGER.error("Sentiment controller error",e);
		}
	}
	
	
	public void add(Map<String, String[]> params, ApiResponse response)  {
		
		String text = HTTPUtils.getParam(TEXT, params);
		String sentiment = HTTPUtils.getParam(SENTIMENT, params);
		int s =  Integer.parseInt(sentiment);
		String context = HTTPUtils.getParam(CONTEXT, params);
		String customerKey = HTTPUtils.getParam(CUSTOMER_KEY, params);
		
		
		Analyzer analyzer;
		try {
			
			analyzer = new Analyzer(Language.PORTUGUESE,Type.PRODUCT);
			analyzer.insert(customerKey, text, s, context);
			Ok ok = factory.createOk();
			ok.setMessage("sentiment example added to the database");
			response.setOk(ok);
			
		} catch (Exception e) {
			LOGGER.error("Sentiment controller error",e);
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
