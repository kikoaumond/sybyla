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

import sybyla.controller.impl.TagController;
import sybyla.http.Client;
import sybyla.jaxb.APIJSONParser;
import sybyla.jaxb.APIXMLParser;
import sybyla.jaxb.ApiResponse;
import sybyla.jaxb.TagResult;

public class TagHandlerTest {
	private static int port =8081;
	private static String text;
	@Before
	public void setUp() throws IOException {
		SybylaServer.setPort(port);
		SybylaServer.setTag(true);
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
	public void testTagsJSON() throws JSONException, IOException {
		
		BufferedReader reader = new BufferedReader(new FileReader("src/test/resources/folha2.txt"));
        StringBuilder sb = new StringBuilder();
        String line;
        while((line=reader.readLine())!=null){
     	   sb.append(line).append("\n");
        }
        text = sb.toString();
		
		Map<String, String> params =  new HashMap<String, String>();
		params.put(TagController.TEXT, text);
		params.put(TagController.LANGUAGE, TagController.PORTUGUESE);
		Client httpClient = new Client("http://localhost:"+port+"/tag/json", params, sybyla.http.Constants.POST); 
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
		JSONArray c =  json.getJSONObject("apiResponse").getJSONArray("tags");
		JSONArray c2 = json2.getJSONObject("apiResponse").getJSONArray("tags");
		assertEquals(c.toString(), c2.toString());
		
		String u =  json.getJSONObject("apiResponse").getString("requestURL");
		String u2 = json2.getJSONObject("apiResponse").getString("requestURL");
		assertEquals(u, u2);
		
	}
	
	@Test
	public void testTagsXML() throws JSONException, IOException, JAXBException {
		
		BufferedReader reader = new BufferedReader(new FileReader("src/test/resources/folha2.txt"));
        StringBuilder sb = new StringBuilder();
        String line;
        while((line=reader.readLine())!=null){
     	   sb.append(line).append("\n");
        }
        text = sb.toString();
		
		Map<String, String> params =  new HashMap<String, String>();
		params.put(TagController.TEXT, text);
		params.put(TagController.LANGUAGE, TagController.PORTUGUESE);
		Client httpClient = new Client("http://localhost:"+port+"/tag/xml/", params, sybyla.http.Constants.POST); 
		httpClient.run();
		String content = httpClient.getResponseContent();
		assertNotNull(content);
		String contentType = httpClient.getResponseType();

		String expectedContentType =sybyla.http.Constants.XML_MIME_TYPE+";"+sybyla.http.Constants.CHARSET_UTF8;
		assertTrue(expectedContentType.equalsIgnoreCase(contentType));
		System.out.println(content);
		ApiResponse response = APIXMLParser.readResponse(content);
		assertNotNull(response);
		assertNull(response.getError());
		assertNotNull(response.getRequestTime());
		List<TagResult> tags = response.getTags();
		assertNotNull(tags);
		assertTrue(tags.size()>0);
		for (int i=0; i< tags.size(); i++){
			TagResult tag =  tags.get(i);
			assertNotNull(tag.getTerm());
			assertTrue(tag.getRelevance()>0);
		}
		
	}
	
	
	@Test
	public void testTagsXMLJSON() throws JSONException, JAXBException, JsonParseException, JsonMappingException, IOException {
	    
		InputStream is  =  new FileInputStream("src/test/resources/folha2.txt");
	    InputStreamReader isr = new InputStreamReader(is,"UTF-8");
		BufferedReader reader = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;
        while((line=reader.readLine())!=null){
     	   sb.append(line).append("\n");
        }
        text = sb.toString();
        PrintStream out = new PrintStream(System.out, true, "UTF-8");
        out.println("Text from file:\n\n"+text);
		Map<String, String> params =  new HashMap<String, String>();
		params.put(TagController.TEXT, text);
		params.put(TagController.LANGUAGE, TagController.PORTUGUESE);

		Client xmlClient = new Client("http://localhost:"+port+"/tag/xml/", params, sybyla.http.Constants.POST); 
		xmlClient.run();
		String content = xmlClient.getResponseContent();
		out.println(content);
		assertNotNull(content);
		String xmlContentType = xmlClient.getResponseType();
		out.println(content);
		String expectedXMLContentType =sybyla.http.Constants.XML_MIME_TYPE+";"+sybyla.http.Constants.CHARSET_UTF8;
		assertTrue(expectedXMLContentType.equalsIgnoreCase(xmlContentType));
		ApiResponse xmlResponse = APIXMLParser.readResponse(content);
		assertNotNull(xmlResponse);
		assertNull(xmlResponse.getError());
		assertNotNull(xmlResponse.getRequestTime());
		assertNotNull(xmlResponse.getRequestURL());
		List<TagResult> tagsXML = xmlResponse.getTags();
		
		assertNotNull(tagsXML);
		assertTrue(tagsXML.size()>0);
		for (int i=0; i< tagsXML.size(); i++){
			TagResult tag =  tagsXML.get(i);
			assertNotNull(tag.getTerm());
			assertTrue(tag.getRelevance()>0);
		}
				
		Client jsonClient = new Client("http://localhost:"+port+"/tag/json/", params, sybyla.http.Constants.POST); 
		jsonClient.run();
		String jc =  jsonClient.getResponseContent();
		out.println(jc);
		String jsonContent = new String(jsonClient.getResponseContent().getBytes(Charset.forName("UTF-8")));
		out.println(jsonContent);
		assertNotNull(jsonContent);
		JSONObject jsonObj =  new JSONObject(jsonContent);
		out.println(jsonObj);
		String jsonContentType = jsonClient.getResponseType();
		String expectedJSONContentType =sybyla.http.Constants.JSON_MIME_TYPE+";"+sybyla.http.Constants.CHARSET_UTF8;
		assertTrue(expectedJSONContentType.equalsIgnoreCase(jsonContentType));
				
		ApiResponse jsonResponse = APIJSONParser.readResponse(jsonContent);
		assertNotNull(jsonResponse);
		assertNull(jsonResponse.getError());
		assertNotNull(jsonResponse.getRequestTime());
		assertNotNull(jsonResponse.getRequestURL());
		
		List<TagResult> tagsJSON = jsonResponse.getTags();
		assertNotNull(tagsJSON);
		assertTrue(tagsJSON.size()>0);
		assertEquals(tagsXML.size(), tagsJSON.size());
		
		for (int i=0; i< tagsXML.size(); i++){
			TagResult tagJSON =  tagsJSON.get(i);
			TagResult tagXML =  tagsXML.get(i);

			assertEquals(tagXML.getTerm(), tagJSON.getTerm());
			assertTrue(tagXML.getRelevance() == tagJSON.getRelevance());
			out.println("JSON: "+tagJSON.getTerm()+", "+ tagJSON.getRelevance()+" XML: "+tagXML.getTerm()+ ", "+tagXML.getRelevance());
		}
		
	}
	
	@Test
	public void testReadFile() throws JSONException, JAXBException, JsonParseException, JsonMappingException, IOException {
	    
		InputStream is  =  new FileInputStream("src/test/resources/folha3.txt");
	    InputStreamReader isr = new InputStreamReader(is,"UTF-8");
	    //InputStreamReader isr = new InputStreamReader(is,"UTF-8");

		BufferedReader reader = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;
        while((line=reader.readLine())!=null){
     	   sb.append(line).append("\n");
        }
        text = sb.toString();
        PrintStream out = new PrintStream(System.out, true, "UTF-8");
        out.println(text);
        String expectedText="Este é um arquivo UTF-8 com caracteres especiais como\n"+
        					"à\n"+
        					 "São Paulo\n"+
        					 "Palácio\n"+
        					 "SÃO PAULO\n"+
        					 "Aéreo\n";

        assertEquals(expectedText, text);
        String s =  "Aéreo";
        out.println(s);
        
	}
}
