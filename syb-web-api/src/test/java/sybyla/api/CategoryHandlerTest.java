package sybyla.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sybyla.controller.impl.GraphController;
import sybyla.controller.impl.TagController;
import sybyla.http.Client;
import sybyla.jaxb.APIJSONParser;
import sybyla.jaxb.APIXMLParser;
import sybyla.jaxb.ApiResponse;
import sybyla.jaxb.CategoryResult;

public class CategoryHandlerTest {
	private static int port =8081;
	private static String text;
	@Before
	public void setUp() throws IOException {
		SybylaServer.setPort(port);
		SybylaServer.setCategory(true);
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
	public void testCategory() throws JSONException, IOException {
		
		BufferedReader reader = new BufferedReader(new FileReader("src/test/resources/abbottabad.txt"));
        StringBuilder sb = new StringBuilder();
        String line;
        while((line=reader.readLine())!=null){
     	   sb.append(line).append("\n");
        }
        text = sb.toString();
		
		Map<String, String> params =  new HashMap<String, String>();
		params.put(TagController.TEXT, text);
		Client httpClient = new Client("http://localhost:"+port+"/category/json", params, sybyla.http.Constants.POST); 
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
		JSONArray c =  json.getJSONObject("apiResponse").getJSONArray("categories");
		JSONArray c2 = json2.getJSONObject("apiResponse").getJSONArray("categories");
		assertEquals(c.toString(), c2.toString());
		
		String u =  json.getJSONObject("apiResponse").getString("requestURL");
		String u2 = json2.getJSONObject("apiResponse").getString("requestURL");
		assertEquals(u, u2);
		
	}
	
	@Test
	public void testCategoryXML() throws JSONException, JAXBException, IOException {
	    
		BufferedReader reader = new BufferedReader(new FileReader("src/test/resources/abbottabad.txt"));
        StringBuilder sb = new StringBuilder();
        String line;
        while((line=reader.readLine())!=null){
     	   sb.append(line).append("\n");
        }
        text = sb.toString();
		
		Map<String, String> params =  new HashMap<String, String>();
		params.put(TagController.TEXT, text);
		Client httpClient = new Client("http://localhost:"+port+"/category/xml/", params, sybyla.http.Constants.POST); 
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

		List<CategoryResult> results = response.getCategories();
		
		assertNotNull(results);
		
		
		for(CategoryResult category: results){
			
			String c = category.getCategory();
			assertNotNull(c);
			assertTrue(!c.equals(""));
			
			double relevance = category.getRelevance();
			assertTrue(relevance > 0);
		}
		
	}
	
	@Test
	public void testTermXMLJSON() throws JSONException, JAXBException, JsonParseException, JsonMappingException, IOException {
	    
		BufferedReader reader = new BufferedReader(new FileReader("src/test/resources/mccartney.txt"));
        StringBuilder sb = new StringBuilder();
        String line;
        while((line=reader.readLine())!=null){
     	   sb.append(line).append("\n");
        }
        text = sb.toString();	
		Map<String, String> params =  new HashMap<String, String>();

        params.put(GraphController.TEXT, text);
		
		Client xmlClient = new Client("http://localhost:"+port+"/category/xml/", params, sybyla.http.Constants.POST); 
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
		List<CategoryResult> xmlCategory = xmlResponse.getCategories();
		assertNotNull(xmlCategory);
		assertTrue(xmlCategory.size()>0);
		
		Client jsonClient = new Client("http://localhost:"+port+"/category/json/", params, sybyla.http.Constants.POST); 
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
		
		List<CategoryResult> jsonCategory = jsonResponse.getCategories();
		assertNotNull(jsonCategory);
		assertTrue(jsonCategory.size()>0);
		assertTrue(xmlCategory.size() == jsonCategory.size());
		
		for (int i= 0; i< xmlCategory.size(); i++){
			CategoryResult xmlCat = xmlCategory.get(i);
			CategoryResult jsonCat = jsonCategory.get(i);
			assertEquals(xmlCat.getCategory(), jsonCat.getCategory());
			assertTrue(xmlCat.getRelevance() ==  jsonCat.getRelevance());
		}
	}

	@Test
	public void testCategory2() throws JSONException, IOException {
		
		BufferedReader reader = new BufferedReader(new FileReader("src/test/resources/mccartney.txt"));
	    StringBuilder sb = new StringBuilder();
	    String line;
	    while((line=reader.readLine())!=null){
	 	   sb.append(line).append("\n");
	    }
	    text = sb.toString();
		
		Map<String, String> params =  new HashMap<String, String>();
		params.put(TagController.TEXT, text);
		Client httpClient = new Client("http://localhost:"+port+"/category/json", params, sybyla.http.Constants.POST); 
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
		JSONArray c =  json.getJSONObject("apiResponse").getJSONArray("categories");
		JSONArray c2 = json2.getJSONObject("apiResponse").getJSONArray("categories");
		assertEquals(c.toString(), c2.toString());
		assertTrue(c.length()>0);
		
		String u =  json.getJSONObject("apiResponse").getString("requestURL");
		String u2 = json2.getJSONObject("apiResponse").getString("requestURL");
		assertEquals(u, u2);
		
	}

	@Test
	public void testCategory3() throws JSONException, IOException {
		
		BufferedReader reader = new BufferedReader(new FileReader("src/test/resources/popeVisit.txt"));
	    StringBuilder sb = new StringBuilder();
	    String line;
	    while((line=reader.readLine())!=null){
	 	   sb.append(line).append("\n");
	    }
	    text = sb.toString();
		
		Map<String, String> params =  new HashMap<String, String>();
		params.put(TagController.TEXT, text);
		Client httpClient = new Client("http://localhost:"+port+"/category/json", params, sybyla.http.Constants.POST); 
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
		JSONArray c =  json.getJSONObject("apiResponse").getJSONArray("categories");
		JSONArray c2 = json2.getJSONObject("apiResponse").getJSONArray("categories");
		assertEquals(c.toString(), c2.toString());
		assertTrue(c.length()>0);
		
		String u =  json.getJSONObject("apiResponse").getString("requestURL");
		String u2 = json2.getJSONObject("apiResponse").getString("requestURL");
		assertEquals(u, u2);
		
	}

	@Test
	public void testCategory4() throws JSONException, IOException {
		
		BufferedReader reader = new BufferedReader(new FileReader("src/test/resources/longText.txt"));
	    StringBuilder sb = new StringBuilder();
	    String line;
	    while((line=reader.readLine())!=null){
	 	   sb.append(line).append("\n");
	    }
	    text = sb.toString();
		
		Map<String, String> params =  new HashMap<String, String>();
		params.put(TagController.TEXT, text);
		Client httpClient = new Client("http://localhost:"+port+"/category/json", params, sybyla.http.Constants.POST); 
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
		JSONArray c =  json.getJSONObject("apiResponse").getJSONArray("categories");
		JSONArray c2 = json2.getJSONObject("apiResponse").getJSONArray("categories");
		assertEquals(c.toString(), c2.toString());
		assertTrue(c.length()>0);
		
		String u =  json.getJSONObject("apiResponse").getString("requestURL");
		String u2 = json2.getJSONObject("apiResponse").getString("requestURL");
		assertEquals(u, u2);
		
	}

	@Test
	public void testCategory5() throws JSONException, IOException {
		
		BufferedReader reader = new BufferedReader(new FileReader("src/test/resources/AnthonyWeiner.txt"));
	    StringBuilder sb = new StringBuilder();
	    String line;
	    while((line=reader.readLine())!=null){
	 	   sb.append(line).append("\n");
	    }
	    text = sb.toString();
		
		Map<String, String> params =  new HashMap<String, String>();
		params.put(TagController.TEXT, text);
		Client httpClient = new Client("http://localhost:"+port+"/category/json", params, sybyla.http.Constants.POST); 
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
		JSONArray c =  json.getJSONObject("apiResponse").getJSONArray("categories");
		JSONArray c2 = json2.getJSONObject("apiResponse").getJSONArray("categories");
		assertEquals(c.toString(), c2.toString());
		assertTrue(c.length()>0);
		
		String u =  json.getJSONObject("apiResponse").getString("requestURL");
		String u2 = json2.getJSONObject("apiResponse").getString("requestURL");
		assertEquals(u, u2);
		
	}

	@Test
	public void testCategory6() throws JSONException, IOException {
		
		BufferedReader reader = new BufferedReader(new FileReader("src/test/resources/longText2.txt"));
	    StringBuilder sb = new StringBuilder();
	    String line;
	    while((line=reader.readLine())!=null){
	 	   sb.append(line).append("\n");
	    }
	    text = sb.toString();
		
		Map<String, String> params =  new HashMap<String, String>();
		params.put(TagController.TEXT, text);
		Client httpClient = new Client("http://localhost:"+port+"/category/json", params, sybyla.http.Constants.POST); 
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
		JSONArray c =  json.getJSONObject("apiResponse").getJSONArray("categories");
		JSONArray c2 = json2.getJSONObject("apiResponse").getJSONArray("categories");
		assertEquals(c.toString(), c2.toString());
		assertTrue(c.length()>0);
		
		String u =  json.getJSONObject("apiResponse").getString("requestURL");
		String u2 = json2.getJSONObject("apiResponse").getString("requestURL");
		assertEquals(u, u2);
		
	}
}
