package sybyla.ey;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class CurrencyModel {
	
	private static final Logger LOGGER =  Logger.getLogger(CurrencyModel.class);
	
	protected static final String DELIM_PRE="delim_pre";
	protected static final String DELIM_POST="delim_post";
	protected static final String DELIM_PRE_2="delim_pre_2";
	protected static final String DELIM_POST_2="delim_post_2";
	protected static final String FRAG_BEGIN="frag_begin";
	protected static final String FRAG_END="frag_end";
	protected static final String MARKER="marker";
	
	public static final String PART="part";
	
	public static final String PART2="part2";
	
	protected static final Pattern CURRENCY_PATTERN = Pattern.compile("[\\p{Lu}\\p{Ll}]{0,3}\\${1,1}[ ]{0,}[0-9,.]{1,}");
	protected static final Pattern CENTS_PATTERN = Pattern.compile("([\\.]){1,1}[0-9]{2,2}$");

	public static List<String> findCurrencies(String text){
		
		List<String> currencies =  new ArrayList<String>();
		Matcher m = CURRENCY_PATTERN.matcher(text);
		while(m.find()){
			String c = m.group();
			if (c.endsWith(".")||c.endsWith(",")){
				c = c.substring(0, c.length()-1);
			}
			currencies.add(c);
		}
		
		return currencies;
		
	}
	
	public static String getMaxValue(String text){
		List<String> currencies = findCurrencies(text);
		double max = 0;
		String m="";
		for(String c: currencies){
			double d =  convert(c);
			if (d>max){
				max = d;
				m=c;
			}
		}
		
		return m;
	}
	
	public static double convert(String currency){
		try{
			//fix possible cents (dot instead of comma)
			String t=  currency.trim();
			Matcher m = CENTS_PATTERN.matcher(t);
			if(m.find()){
				int s = m.start();
				t=t.substring(0,s)+","+t.substring(s+1, t.length());
			}
			
			
			int i =t.indexOf("$");
			String c =  t.substring(i+1);
			c = c.replaceAll("\\.", "");
			c = c.replaceAll(",", ".");
			c = c.replaceAll(" ", "");
			if (c.endsWith(".")){
				c = c.substring(0, c.length()-1);
			}
			double d =  Double.parseDouble(c);
			return d;
		}catch(NumberFormatException e){
			return 0;
		}
		
	}
}

