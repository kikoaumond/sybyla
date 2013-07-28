package sybyla.regex;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

 

public class TemplateTest {
 
	private Template template=new Template();
	private String text;
	private Map<String, String> params=new HashMap<String,String>();
	
	@Before()
	public void setup(){
		text = "Dear ${name}, thank you for buying the \"${product}\".";
		params.put("name","Steve Cox");
		params.put("product", "Ronco Bass-O-Matic 76");
		
	}
	
	@Test
	public void templateTest() {
		Set<String> vars = template.getVariables(text);
		assertTrue(vars.size()==2);
	    assertTrue(vars.contains("name"));
	    assertTrue(vars.contains("product"));
	    
	    String sub = template.sub(text, params);
	    assertTrue(sub.equals("Dear Steve Cox, thank you for buying the \"Ronco Bass-O-Matic 76\"."));
	}
	
	@Test
	public void conditionalTemplateTest() {
		String text = "Dear ${name}, thank you for buying the \"${product}\"?{ on ${date}}.";
		Set<String> vars = template.getVariables(text);
		assertTrue(vars.size()==3);
	    assertTrue(vars.contains("name"));
	    assertTrue(vars.contains("product"));
	    assertTrue(vars.contains("date"));

	    String sub = template.sub(text, params);
	    assertTrue(sub.equals("Dear Steve Cox, thank you for buying the \"Ronco Bass-O-Matic 76\"."));
	    
	    params.put("date","01/01/2012");
	    sub = template.sub(text, params);
	    assertTrue(sub.equals("Dear Steve Cox, thank you for buying the \"Ronco Bass-O-Matic 76\" on 01/01/2012."));
	    params.remove("date");
	}
	
	@Test
	public void conditionalTemplateTest2() {}
	
	@Test
	public void templateListTest() {
		
		List<String> values = new ArrayList<String>();
		values.add("1");
		values.add("2");
		
		String t =  "this is a list with values: $${list}";
	    
	    String sub = Template.sub(t,"list", values);
	    assertTrue(sub.equals("this is a list with values: 1, 2"));
	    
	    values.clear();
	    values.add("A");
	    values.add("B");
	    t= "this is a list with values: '$${list}'";
	    sub = Template.sub(t,"list", values);
	    assertTrue(sub.equals("this is a list with values: 'A', 'B'"));
	}
	
	@Test
	public void htmlTagTest(){}
}