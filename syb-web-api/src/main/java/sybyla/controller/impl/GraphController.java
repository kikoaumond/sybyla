package sybyla.controller.impl;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import sybyla.jaxb.ApiResponse;
import sybyla.api.Constants;
import sybyla.api.Controller;
import sybyla.jaxb.TreeResult;
import sybyla.graph.Tree;
import sybyla.graph.neo4j.Neo4jGraphEngine;
import sybyla.http.HTTPUtils;

public class GraphController extends Controller{
	
	static {
		Neo4jGraphEngine.start();
	}
	
	public static final String TERM="term";
	public static final String TEXT="text";
	public static final String URL="url";
	
	private int firstDegreeOrder=10;
	private int order=4;
	private int depth=2;
	
	@Override
	public void checkParams(Map<String, String[]> params)
			throws IllegalArgumentException {
		
		String term = HTTPUtils.getParam(TERM, params);
		String text = HTTPUtils.getParam(TEXT, params);
		String url = HTTPUtils.getParam(URL, params);
		if (term == null && text== null && url == null){
			throw new IllegalArgumentException("At least one of the "+TERM+", "+TEXT+", or "+URL+ 
												" parameters must be specified in a call");
		}
		if(term!=null){
			if (text==null&url==null){
				return;
			}
		} else if(text!=null){
			if (term==null & url==null){
				return;
			}
		} else if (url!=null){
			if (term == null && text==null) {
				return;
			}
		}
		
		throw new IllegalArgumentException("Only one of the "+TERM+", "+TEXT+", or "+URL+ " parameters may be specified in a call");
	}
	
	
	@Override
	public void process(Map<String, String[]> params, ApiResponse response) {
		Tree tree = getTree(params);
		TreeResult treeResult  = tree.toTreeResult();
		response.setTree(treeResult);
	}
	
	private Tree getTree(Map<String, String[]> params){
		String term  =  HTTPUtils.getParam(TERM, params);
		Tree tree = Neo4jGraphEngine.getRelatedTree(term, order, depth, firstDegreeOrder);
		return tree;
	}
	
	public JSONObject getResultJSON(Map<String, String[]> params) throws JSONException {
		
		Tree tree = getTree(params);
		JSONObject treeJSON = tree.toJSON();
		return treeJSON;
	}

	@Override
	public String getAppName() {
		return Constants.GRAPH_APP;
	}



}
