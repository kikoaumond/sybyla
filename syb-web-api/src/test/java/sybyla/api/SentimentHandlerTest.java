package sybyla.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.Charset;
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

import sybyla.controller.impl.SentimentController;
import sybyla.http.Client;
import sybyla.jaxb.APIJSONParser;
import sybyla.jaxb.APIXMLParser;
import sybyla.jaxb.ApiResponse;
import sybyla.jaxb.TagResult;

public class SentimentHandlerTest {
	private static int port =8081;
	private static String text;
	@Before
	public void setUp() throws IOException {
		SybylaServer.setPort(port);
		SybylaServer.setSentiment(true);
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
	public void testSentimentJSON() throws JSONException, IOException {
		
		
        text = "O produto não funcionou";
		
		Map<String, String> params =  new HashMap<String, String>();
		params.put(SentimentController.TEXT, text);
		params.put(SentimentController.LANGUAGE, SentimentController.PORTUGUESE);
		Client httpClient = new Client("http://localhost:"+port+"/sentiment/json", params, sybyla.http.Constants.POST); 
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
		JSONObject c =  json.getJSONObject("apiResponse").getJSONObject("sentimentResult");
		JSONObject c2 = json2.getJSONObject("apiResponse").getJSONObject("sentimentResult");
		assertEquals(c.toString(), c2.toString());
		
		String u =  json.getJSONObject("apiResponse").getString("requestURL");
		String u2 = json2.getJSONObject("apiResponse").getString("requestURL");
		assertEquals(u, u2);
		
	}
	
	@Test
	public void testSentimentJSON2() throws JSONException, IOException {
		
		
        text = "O produto não funcionou";
        String sentiment = "-1";
        String customerKey="test";
        String context = "não funcionou";
		
		Map<String, String> params =  new HashMap<String, String>();
		params.put(SentimentController.TEXT, text);
		params.put(SentimentController.SENTIMENT, sentiment);
		params.put(SentimentController.CONTEXT, context);
		params.put(SentimentController.CUSTOMER_KEY, customerKey);
		params.put(SentimentController.ADD, "true");


		Client httpClient = new Client("http://localhost:"+port+"/sentiment/json", params, sybyla.http.Constants.POST); 
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
		JSONObject c =  json.getJSONObject("apiResponse").getJSONObject("Ok");
		JSONObject c2 = json2.getJSONObject("apiResponse").getJSONObject("Ok");
		assertEquals(c.toString(), c2.toString());
		
		String u =  json.getJSONObject("apiResponse").getString("requestURL");
		String u2 = json2.getJSONObject("apiResponse").getString("requestURL");
		assertEquals(u, u2);
		
	}
}
