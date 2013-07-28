package sybyla.controller.impl;

import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import sybyla.api.Constants;
import sybyla.api.Controller;
import sybyla.ey.ContractAnalyzer;
import sybyla.http.HTTPUtils;
import sybyla.jaxb.ApiResponse;
import sybyla.jaxb.ContractResult;
import sybyla.jaxb.ObjectFactory;

public class EYController extends Controller{
	
	public static final Logger LOGGER =  Logger.getLogger(EYController.class);	
	public static final String TEXT="text";
	private ObjectFactory factory = new ObjectFactory();

	@Override
	public void checkParams(Map<String, String[]> params)
			throws IllegalArgumentException {
		
		String text = HTTPUtils.getParam(TEXT, params);
		
		if ( text== null){
			throw new IllegalArgumentException("The "+TEXT+" parameter must be specified in a call");
		}
	}	
	
	@Override
	public void process(Map<String, String[]> params, ApiResponse response) {
		
		String text = HTTPUtils.getParam(TEXT, params);
		ContractAnalyzer analyzer = new ContractAnalyzer();
		
		analyzer.analyze(text);
		ContractResult contractResult = factory.createContractResult();
		
		contractResult.setPart1(analyzer.getPart1());
		contractResult.setPart2(analyzer.getPart2());
		contractResult.setValue(analyzer.getValue());
		contractResult.setContractDate(analyzer.getContractDate());
		contractResult.setStartDate(analyzer.getBeginDate());
		contractResult.setEndDate(analyzer.getEndDate());
		
		response.setEy(contractResult);
	}
	
	@Override
	public JSONObject getResultJSON(Map<String, String[]> params) throws JSONException {
		
		return null;
		
	}

	@Override
	public String getAppName() {
		return Constants.EY_APP;
	}	
}
