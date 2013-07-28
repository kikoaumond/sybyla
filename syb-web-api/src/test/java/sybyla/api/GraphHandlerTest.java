package sybyla.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sybyla.controller.impl.GraphController;
import sybyla.http.Client;
import sybyla.jaxb.APIJSONParser;
import sybyla.jaxb.APIXMLParser;
import sybyla.jaxb.ApiResponse;
import sybyla.jaxb.TreeResult;
import sybyla.jaxb.Utils;

public class GraphHandlerTest {
	private static int port =8081;
	
	@Before
	public void setUp() {
		SybylaServer.setPort(port);
		SybylaServer.setGraph(true);
		boolean test = true;
		SybylaServer.start(test);
	}
	
	@After
	public void tearDown() {
		try {
			SybylaServer.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testInexistentTerm() throws JSONException {
	    
		Map<String, String> params =  new HashMap<String, String>();
		params.put(GraphController.TERM, "xavasca");
		Client httpClient = new Client("http://localhost:"+port+"/graph/json", params, sybyla.http.Constants.GET); 
		httpClient.run();
		String content = httpClient.getResponseContent();
		assertNotNull(content);
		String contentType = httpClient.getResponseType();
		JSONObject json = new JSONObject(content);
		JSONTokener tokener = new JSONTokener(json.toString()); //tokenize the ugly JSON string
		JSONObject finalResult = new JSONObject(tokener); // convert it to JSON object
		System.out.println(finalResult.toString(4));
		assertNotNull(json);
		String expectedContentType =sybyla.http.Constants.JSON_MIME_TYPE+";"+sybyla.http.Constants.CHARSET_UTF8;
		assertTrue(expectedContentType.equalsIgnoreCase(contentType));
		
		String callback =  "callback";
		params.put(Constants.CALLBACK_PARAM, callback);
		httpClient.setParams(params);
		httpClient.run();
		content = httpClient.getResponseContent();
		assertNotNull(content);
		contentType = httpClient.getResponseType();
		
		expectedContentType =sybyla.http.Constants.JSONP_MIME_TYPE+";"+sybyla.http.Constants.CHARSET_UTF8;
		assertTrue(expectedContentType.equalsIgnoreCase(contentType));
		
		String jsonContent = content.substring((callback+"(").length(), content.indexOf(");"));
		JSONObject json2 = new JSONObject(jsonContent);
		assertNotNull(json2);
		System.out.println(json);
		System.out.println(json2);
		JSONObject c =  json.getJSONObject("apiResponse").getJSONObject("tree");
		JSONObject c2 = json2.getJSONObject("apiResponse").getJSONObject("tree");
		assertEquals(c.toString(), c2.toString());
		
		String u =  json.getJSONObject("apiResponse").getString("requestURL");
		String u2 = json2.getJSONObject("apiResponse").getString("requestURL");
		assertEquals(u, u2);	
	}
	
	@Test
	public void testTermJSON() throws JSONException {
	    
		Map<String, String> params =  new HashMap<String, String>();
		params.put(GraphController.TERM, "Rolling Stones");
		Client httpClient = new Client("http://localhost:"+port+"/graph/json", params, sybyla.http.Constants.GET); 
		httpClient.run();
		String content = httpClient.getResponseContent();
		assertNotNull(content);
		String contentType = httpClient.getResponseType();
		JSONObject json = new JSONObject(content);
		JSONTokener tokener = new JSONTokener(json.toString()); //tokenize the ugly JSON string
		JSONObject finalResult = new JSONObject(tokener); // convert it to JSON object
		System.out.println(finalResult.toString(4));
		assertNotNull(json);
		String expectedContentType =sybyla.http.Constants.JSON_MIME_TYPE+";"+sybyla.http.Constants.CHARSET_UTF8;
		assertTrue(expectedContentType.equalsIgnoreCase(contentType));
		
		String callback =  "callback";
		params.put(Constants.CALLBACK_PARAM, callback);
		httpClient.setParams(params);
		httpClient.run();
		content = httpClient.getResponseContent();
		assertNotNull(content);
		contentType = httpClient.getResponseType();
		
		expectedContentType =sybyla.http.Constants.JSONP_MIME_TYPE+";"+sybyla.http.Constants.CHARSET_UTF8;
		assertTrue(expectedContentType.equalsIgnoreCase(contentType));
		
		String jsonContent = content.substring((callback+"(").length(), content.indexOf(");"));
		JSONObject json2 = new JSONObject(jsonContent);
		assertNotNull(json2);
		JSONObject c =  json.getJSONObject("apiResponse").getJSONObject("tree");
		JSONObject c2 = json2.getJSONObject("apiResponse").getJSONObject("tree");
		assertEquals(c.toString(), c2.toString());
		
		String u =  json.getJSONObject("apiResponse").getString("requestURL");
		String u2 = json2.getJSONObject("apiResponse").getString("requestURL");
		assertEquals(u, u2);
		
	}
	
	@Test
	public void testTermXML() throws JSONException, JAXBException {
	    
		Map<String, String> params =  new HashMap<String, String>();
		params.put(GraphController.TERM, "Renato Aragão");
		Client httpClient = new Client("http://localhost:"+port+"/graph/xml/", params, sybyla.http.Constants.GET); 
		httpClient.run();
		String content = httpClient.getResponseContent();
		assertNotNull(content);
		String contentType = httpClient.getResponseType();
		System.out.println(content);
		String expectedContentType =sybyla.http.Constants.XML_MIME_TYPE+";"+sybyla.http.Constants.CHARSET_UTF8;
		assertTrue(expectedContentType.equalsIgnoreCase(contentType));
		ApiResponse response = APIXMLParser.readResponse(content);
		assertNotNull(response);
		assertNull(response.getError());
		assertNotNull(response.getRequestTime());

		TreeResult tree = response.getTree();
		
		assertNotNull(tree);
		
		String name = tree.getName();
		assertEquals("Renato Aragão", name);
		List<TreeResult> children = tree.getChildren();
		assertNotNull(children);
		String[] expectedChildren = {"Mussum", "Dedé Santana","Zacarias"};
		for (TreeResult child: children){
			for(int i=0; i<expectedChildren.length; i++){
				if(expectedChildren[i]!=null && expectedChildren[i].equals(child.getName())){
					expectedChildren[i]=null;
				}
			}
		}
		for(String expectedChild:expectedChildren){
			assertNull(expectedChild);
		}
	}
	
	@Test
	public void testTermXMLJSON() throws JSONException, JAXBException, JsonParseException, JsonMappingException, IOException {
	    
		Map<String, String> params =  new HashMap<String, String>();
		params.put(GraphController.TERM, "Vietnam War");
		
		Client xmlClient = new Client("http://localhost:"+port+"/graph/xml/", params, sybyla.http.Constants.GET); 
		xmlClient.run();
		String content = xmlClient.getResponseContent();
		assertNotNull(content);
		String xmlContentType = xmlClient.getResponseType();
		System.out.println(content);
		String expectedXMLContentType =sybyla.http.Constants.XML_MIME_TYPE+";"+sybyla.http.Constants.CHARSET_UTF8;
		assertTrue(expectedXMLContentType.equalsIgnoreCase(xmlContentType));
		ApiResponse xmlResponse = APIXMLParser.readResponse(content);
		assertNotNull(xmlResponse);
		assertNull(xmlResponse.getError());
		assertNotNull(xmlResponse.getRequestTime());
		assertNotNull(xmlResponse.getRequestURL());
		TreeResult xmlTree = xmlResponse.getTree();
		assertNotNull(xmlTree);
		String xmlRootName = xmlTree.getName();
		
		assertEquals("Vietnam War", xmlRootName);
		
		Client jsonClient = new Client("http://localhost:"+port+"/graph/json/", params, sybyla.http.Constants.GET); 
		jsonClient.run();
		String jsonContent = jsonClient.getResponseContent();
		assertNotNull(jsonContent);
		String jsonContentType = jsonClient.getResponseType();
		String expectedJSONContentType =sybyla.http.Constants.JSON_MIME_TYPE+";"+sybyla.http.Constants.CHARSET_UTF8;
		assertTrue(expectedJSONContentType.equalsIgnoreCase(jsonContentType));
				
		ApiResponse jsonResponse = APIJSONParser.readResponse(jsonContent);
		assertNotNull(jsonResponse);
		assertNull(jsonResponse.getError());
		assertNotNull(jsonResponse.getRequestTime());
		assertNotNull(jsonResponse.getRequestURL());
		TreeResult jsonTree = jsonResponse.getTree();
		assertNotNull(jsonTree);
		String jsonRootName = jsonTree.getName();
		assertEquals("Vietnam War", jsonRootName);
		
		assertTrue(Utils.equals(xmlTree,jsonTree));
	}
	
	@Test
	public void testWrongApp() throws JSONException {
	    
		Map<String, String> params =  new HashMap<String, String>();
		params.put(GraphController.TERM, "Renato Aragão");
		Client httpClient = new Client("http://localhost:"+port+"/blahblahblah/json", params, sybyla.http.Constants.GET); 
		httpClient.run();
		String content = httpClient.getResponseContent();
		int code = httpClient.getResponseStatusCode();
		assertTrue(code==400);
		assertNotNull(content);
		String contentType = httpClient.getResponseType();
		JSONObject json = new JSONObject(content);
		assertNotNull(json);
		String expectedContentType =sybyla.http.Constants.JSON_MIME_TYPE+";"+sybyla.http.Constants.CHARSET_UTF8;
		assertTrue(expectedContentType.equalsIgnoreCase(contentType));
		JSONObject error = json.getJSONObject("apiResponse").getJSONObject("error");
		assertNotNull(error);
		assertFalse(json.has("result"));
		String errorType =  error.getString("errorType");
		assertEquals("IllegalArgumentError", errorType);
		String errorMessage =  error.getString("errorMessage");
		assertTrue(errorMessage.startsWith("Unknown API"));
	}
}
