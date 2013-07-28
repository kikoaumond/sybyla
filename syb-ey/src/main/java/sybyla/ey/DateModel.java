package sybyla.ey;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class DateModel {
	
	private static final Logger LOGGER =  Logger.getLogger(DateModel.class);
	
	protected static final String DELIM_PRE="delim_pre";
	protected static final String DELIM_POST="delim_post";
	protected static final String DELIM_PRE_2="delim_pre_2";
	protected static final String DELIM_POST_2="delim_post_2";
	protected static final String FRAG_BEGIN="frag_begin";
	protected static final String FRAG_END="frag_end";
	protected static final String END_DATE_MARKER="end_date_marker";
	protected static final String CONTRACT_DATE_MARKER="contract_date_marker";
	protected static final String BEGIN_DATE_MARKER="begin_date_marker";
	protected static final String MONTH="month";
	protected static final String FRAG="frag";
	protected static final String MARKER="marker";
	
	public static final String PART="part";
	
	public static final String PART2="part2";
	
	protected static final String DATE_REGEX="[0-3]{0,1}[0-9]{1,}[/]{1,1}[0-1]{0,1}[0-9]{1,1}[/]{1,1}[1-2]{0,1}[0-9]{0,1}[0-9]{1,1}[0-9]{1,1}";
	protected static final Pattern DATE_PATTERN = Pattern.compile(DATE_REGEX);
	protected static final String DATE_REGEX_2="[0-3]{0,1}[0-9]{1,1}[ ]{0,1}de[ ]{0,1}(janeiro|fevereiro|março|marco|abril|maio|junho|julho|agosto|setembro|outubro|novembro|dezembro){1,1}[ ]{0,1}de[ ]{0,1}[1-2]{0,1}[.]{0,1}[0-9]{0,1}[0-9]{1,1}[0-9]{1,1}";
	protected static final Pattern DATE_PATTERN_2 = Pattern.compile(DATE_REGEX_2);


	protected static final String ALL_CAPS_REGEX = "[\\p{Lu}A-Z0-9\\s./-]+";
	protected static final String CAPITALIZED_REGEX="[\\p{Lu}]{1,}[\\p{Lu}\\p{Ll}/.0-9-]{0,}";//[a-z0-9./]{0,}]";
	private static final Pattern ALL_CAPS_PATTERN = Pattern.compile(ALL_CAPS_REGEX);
	public static final Pattern MULTIPLE_SPACES_PATTERN =  Pattern.compile("([\\s]+)");
	public static final String PUNCTUATION_REGEX =  "([,;:-])";
	
	public static final String PARTS_MODEL="/datesModel.txt";
	public static final Map<String, Set<String[]>> modelPhrases=loadModel(PARTS_MODEL);

	
	public DateModel(String modelPath) throws Exception{
	}
	
	public static List<Tag> findDates(String text){
		
		String t = PartsModel.normalize(text);
		List<Tag> dates =  new ArrayList<Tag>();
		Matcher m = DATE_PATTERN.matcher(t);
		
		while(m.find()){
			String d = m.group();
			int begin = m.start();
			int end = m.end();
			Tag tag = new Tag(d, begin, end);
			dates.add(tag);
		}
		
		m = DATE_PATTERN_2.matcher(t);
		
		while(m.find()){
			String d = m.group();
			int begin = m.start();
			int end = m.end();
			Tag tag = new Tag(d, begin, end);
			dates.add(tag);
		}
		
		return dates;
	}
	
	public static Tag findClosest(List<Tag> tags, String text, String markerType){
		text= PartsModel.normalize(text);
		List<Integer> ml= new ArrayList<Integer>();
		Set<String[]> beginMarkers = modelPhrases.get(markerType);
		for (String[] marker: beginMarkers){
			String m = PartsModel.toTerm(marker);
			int p =text.indexOf(m);
			if (p>0){
				ml.add(p);
			}
		}
		Tag closest = null;
		int min=Integer.MAX_VALUE;
		for (Tag tag: tags){
			int begin = tag.getBegin();
			for (int m:ml){
				if(begin - m > 0 && begin-m < 20 && begin - m < min){
					closest = tag;
					min = begin-m;
				}
			}			
		}
		return closest;
	}
	
	public static String[] findBeginEndDates(List<Tag> dates, String text ){
		text= PartsModel.normalize(text);
		List<Integer> bm= new ArrayList<Integer>();
		Set<String[]> beginMarkers = modelPhrases.get(BEGIN_DATE_MARKER);
		for (String[] marker: beginMarkers){
			String m = PartsModel.toTerm(marker);
			int p =text.indexOf(m);
			if (p>0){
				bm.add(p);
			}
		}
		
		List<Integer> em= new ArrayList<Integer>();
		Set<String[]> endMarkers = modelPhrases.get(END_DATE_MARKER);
		for (String[] marker: endMarkers){
			String m = PartsModel.toTerm(marker);
			int p =text.indexOf(m);
			if (p>0){
				em.add(p);
			}
		}
		
		int[] closest = {Integer.MAX_VALUE, Integer.MAX_VALUE};
		String[] beginEndDates = {"",""};
		for (Tag date: dates){
			int begin = date.getBegin();
			int end =  date.getEnd();
			for (int m:bm){
				if(begin - m > 0 && (begin-m < 80)&& begin - m < closest[0]){
					beginEndDates[0] = date.getTag();
					closest[0] = begin-m;
				}
			}
			
			for (int m:em){
				if(begin - m > 0 && begin-m < 100 && begin - m < closest[1]){
					if (!beginEndDates[0].equals(date.getTag())){
						beginEndDates[1] = date.getTag();
						closest[1] = begin-m;
					}
				}
			}
		}
		
		return beginEndDates;
	}
	
	public static String findContractDate(List<Tag> dates, String text){
		
		Collections.sort(dates);
		if (dates ==  null || dates.size()==0) return "";
		Tag t = dates.get(dates.size()-1);
		int b = t.getBegin();
		int textSize =  text.length();
		float q = (float)b/(float) textSize;
		
		if(q>0.8){
			return t.getTag();
		} else {
			Tag tag =  findClosest(dates,text,CONTRACT_DATE_MARKER);
			if (tag!=null){
				return tag.getTag();
			}
		}
		
		return "";
	}
	
	public static Date getDate(String date){
		
		if (date ==  null) return null;
		
		String[] dd = date.trim().split("/");
		
		String day="dd";
		String month="MM";
		String year="yyyy";
		
		if (dd[0].trim().length()==1){
			day="d";
		}
		
		if (dd[1].trim().length()==1){
			day="M";
		}
		
		if (dd[2].trim().length()==2){
			year="yy";
		}
		
		SimpleDateFormat df =  new SimpleDateFormat(day+"/"+month+"/"+year);
		
		try {
			Date d = df.parse(date);
			return d;
		} catch (ParseException e) {
			return null;
		}
	}

	
	public static String normalize(String sentence){
		String s =  sentence.replaceAll(PUNCTUATION_REGEX," $1 ");
		s =  MULTIPLE_SPACES_PATTERN.matcher(s).replaceAll(" ");
		s =  s.toLowerCase().trim();
		return s;
	}
	
	public static String makeReference(String sentence){
		String s =  sentence.replaceAll(PUNCTUATION_REGEX," $1 ");
		s =  MULTIPLE_SPACES_PATTERN.matcher(s).replaceAll(" ");
		s = s.trim();
		return s;
	}
	
	private static boolean isAllowedLabel(String label){
		
		boolean b = DELIM_PRE.equals(label) 
				    || DELIM_POST.equals(label)
				    || FRAG_BEGIN.equals(label) 
				    || FRAG_END.equals(label) 
				    || FRAG.equals(label) 
				    || FRAG_END.equals(label) 
				    || MONTH.equals(label) 
				    || END_DATE_MARKER.equals(label) 
				    || CONTRACT_DATE_MARKER.equals(label) 
				    || BEGIN_DATE_MARKER.equals(label) 
				    || MARKER.equals(label)
					|| DELIM_PRE_2.equals(label) 
					|| DELIM_POST_2.equals(label);
		return b;
	}
	
	
	
	
	
	
	private static Map<String,Set<String[]>> loadModel(String modelPath){
		
		Map<String, Set<String[]>> model = new HashMap<String, Set<String[]>>();
		InputStream is = DateModel.class.getResourceAsStream(modelPath);
		int n=0;
		LOGGER.info("Loading parts model file " + modelPath);
		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(is,"UTF-8"));
			String line = null;
		
			while((line=reader.readLine())!=null){
				if (line.trim().equals("") || line.startsWith("-")){
					continue;
				}
				String[] tokens = line.split("\\t");
				String label = tokens[0].trim();
	            
				if (!isAllowedLabel(label)){
					throw new Exception("Unrecognized label in line "+line);
				}
	            
				String term = tokens[1].toLowerCase().trim();
			
				String[] phrase = term.split("\\s");
			
				for(int i=0;i<phrase.length;i++){
					phrase[i] = phrase[i].trim();
				}
			
				Set<String[]> phr = model.get(label);
			
				if (phr == null){
					final Set<String[]> p = new HashSet<String[]>();
					model.put(label, p);
					phr = p;
				}
			
				phr.add(phrase);		   	
				n++;
	            
			}
			reader.close();
	
			LOGGER.info("Loaded " + n + " parts markers from " + modelPath);
			return model;
		} catch(Exception e){
			LOGGER.error("Error loading parts model ",e);
		}
		return null;
	}
	
}

