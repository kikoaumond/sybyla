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
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class PartsModel {
	
	private static final Logger LOGGER =  Logger.getLogger(PartsModel.class);
	
	protected static final String DELIM_PRE="delim_pre";
	protected static final String DELIM_POST="delim_post";
	protected static final String DELIM_PRE_2="delim_pre_2";
	protected static final String DELIM_POST_2="delim_post_2";
	protected static final String FRAG_BEGIN="frag_begin";
	protected static final String FRAG_END="frag_end";
	protected static final String MARKER="marker";
	
	public static final String PART="part";
	
	public static final String PART2="part2";
	
	protected static final String ALL_CAPS_REGEX = "[\\p{Lu}A-Z0-9\\s./-]+";
	protected static final String CAPITALIZED_REGEX="[\\p{Lu}]{1,}[\\p{Lu}\\p{Ll}/.0-9-]{0,}";//[a-z0-9./]{0,}]";
	private static final Pattern ALL_CAPS_PATTERN = Pattern.compile(ALL_CAPS_REGEX);
	public static final Pattern MULTIPLE_SPACES_PATTERN =  Pattern.compile("([\\s]+)");
	public static final String PUNCTUATION_REGEX =  "([,;:-])";
	
	public static final String PARTS_MODEL="/partsModel.txt";
	public static final Map<String, Set<String[]>> modelPhrases=loadModel(PARTS_MODEL);

	public static boolean isMarker(String text){
		Set<String[]> markers = modelPhrases.get(MARKER);
		for(String[] s:markers){
			String t =toTerm(s).toLowerCase();
			if (text.toLowerCase().contains(t)){
				return true;
			}
		}
		return false;
	}
	
	public PartsModel(String modelPath) throws Exception{
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
				    || MARKER.equals(label)
					|| DELIM_PRE_2.equals(label) 
					|| DELIM_POST_2.equals(label);
		return b;
	}
	
	public static List<String> evaluate(String text){
		
		List<String> patterns =new ArrayList<String>();
		
		String n = normalize(text);
		String[] normalized = n.split("\\s");
		String r =  makeReference(text);
		String[] reference =  r.split("\\s");
		
		ScanResult delimPre = PartsModel.scanPhrases(PartsModel.DELIM_PRE, normalized);
		if(delimPre.size()==0){
			delimPre = PartsModel.scanPhrases(PartsModel.DELIM_PRE_2, normalized);
		}
		int minPre=0;
		if(delimPre.size()!=0){
			minPre = delimPre.min();
		} 
		
		ScanResult delimPost = PartsModel.scanPhrases(PartsModel.DELIM_POST, normalized);
		if(delimPost.size()==0){
			delimPost = PartsModel.scanPhrases(PartsModel.FRAG_END, normalized);
		}
		if (delimPost.size()==0){
			delimPost = PartsModel.scanPhrases(PartsModel.DELIM_POST_2, normalized);
		}
		
		int maxPost=normalized.length-1;
		if (delimPost.size()!=0){
			maxPost=delimPost.max();
		}
		
		
		int[] range1 = getRange(delimPre,delimPost, minPre,maxPost);
		if (range1 != null){
			List<String> patterns1 = findPattern(range1[0],range1[1], reference,ALL_CAPS_REGEX );
			patterns.addAll(patterns1);
			int[] range2 = getRange(delimPre,delimPost, range1[1]+1,maxPost);
			if (range2 != null) {
				List<String> patterns2 = findPattern(range2[0],range2[1], reference,ALL_CAPS_REGEX );
				if (patterns2.size()==0){
					while ((range2=getRange(delimPre,delimPost, range2[1]+1,maxPost))!=null){
						patterns2 = findPattern(range2[0],range2[1], reference,ALL_CAPS_REGEX );
						if (patterns2.size()>0) break;
					}
				}
				patterns.addAll(patterns2);
			}
			
		}

		return patterns;
		
	}
	
	
	
	private static int[] getRange(ScanResult pre, ScanResult post, int minPre, int maxPost){
		int[] range =  null;
		List<Integer> prePositions = pre.getPositions();
		if (pre.max()<minPre){
			prePositions.add(minPre);
		}
		List<Integer> postPositions = post.getPositions();
		
		for(int i=0;i<prePositions.size();i++){
			int begin = prePositions.get(i);
			if (begin < minPre){
				continue;
			}
			
			
			for(int j=0; j<postPositions.size();j++){
				int end =  postPositions.get(j);
				if (end > maxPost){
					continue;
				}
				if (end>begin){
					range =  new int[2];
					range[0]=begin;
					range[1]=end;
					return range;
				}
			}
		}
		return range;
	}
	
	private static List<String> findPattern(int begin, int end, String[] text, String pattern){
		
		boolean patternDetected=false;
		
		List<String> patternsList = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();
		for(int i=begin; i<=end; i++){
			if((text[i].matches(pattern) || isMostlyUppercase(text[i])) 
					&& !isMarker(text[i]) && !isNumber(text[i]) && isWord(text[i])){
				
				if(!patternDetected){
					patternDetected=true;
				}
				sb.append(" ").append(text[i]);
				
			} else {
				if (patternDetected){
					patternDetected=false;
				}
				if (sb.length()>0){
					patternsList.add(sb.toString().trim());
					sb.delete(0, sb.length());
				}
			}
		}
		
		return patternsList;
		
	}
	
	private static Map<String,Set<String[]>> loadModel(String modelPath){
		
		Map<String, Set<String[]>> model = new HashMap<String, Set<String[]>>();
		InputStream is = PartsModel.class.getResourceAsStream(modelPath);
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

	protected static ScanResult scanPhrases(String type, String[] sentence){
	
		ScanResult sr = new ScanResult();
		Set<String[]> phrases = modelPhrases.get(type);
		for(int i=0;i<sentence.length;i++){
			for(String[] phrase: phrases){
				if (   phrase.length > sentence.length 
					|| i > sentence.length - phrase.length){
					continue;
				}
				int pos = findPhrase(sentence, i, phrase);
				if (pos!=-1){
					String term = toTerm(phrase);
					sr.add(term, pos);
				}
			}
		}
		
		return sr;
		
	}
	
	private static int findPhrase(String[] sentence, int sentenceIndex, String[] phrase){
		
		if(    sentence  ==  null || phrase ==  null 
			|| sentenceIndex<0 || sentenceIndex>= sentence.length){
			return -1;
		}
		if (sentence.length<phrase.length){
			return -1;
		}
		if (sentenceIndex > sentence.length - phrase.length){
			return -1;
		}
		
		for(int j=0; j< phrase.length; j++){
			if (!phrase[j].equals(sentence[sentenceIndex+j])){
				return -1;
			}
		}
		
		return sentenceIndex+phrase.length-1;
	}

	private static boolean isMostlyUppercase(String word){
		int n = 0;
		for(int i=0; i<word.length(); i++){
			char c = word.charAt(i);
			if (Character.isUpperCase(c) && !Character.isDigit(c)){
				n++;
			}
		}
		if (n>3) return true;
		return false;
	}
	
	private static boolean isNumber(String word){
		int n = 0;
		for(int i=0; i<word.length(); i++){
			char c = word.charAt(i);
			if (!Character.isDigit(c)){
				n++;
			}
		}
		if (n>0) return false;
		return true;
	}
	
	private static boolean isWord(String word){
		int n = 0;
		for(int i=0; i<word.length(); i++){
			char c = word.charAt(i);
			if (Character.isLetter(c) || c=='-'){
				n++;
			}
		}
		if (n>0) return true;
		return false;
	}
	
	static String toTerm(String[] phrase){
		
		if(phrase == null || phrase.length==0){
			return null;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(phrase[0]);
		for(int i=1;i<phrase.length;i++){
			sb.append(" ").append(phrase[i]);
		}
		return sb.toString();
	}

	public static class Part{
		
		private static String name;
		private static String type;
		private static int begin;
		private static int end;
		
		private Part(String name, String type){
			this.name = name;
			this.type = type;
		}

		public static String getName() {
			return name;
		}

		public static String getType() {
			return type;
		}
	}
	
	protected static class ScanResult{
		
		private List<String> markers=new ArrayList<String>();
		private List<Integer> positions = new ArrayList<Integer>();
		private int max=-1;
		private int min=-1;
		private int defaultDistance=3;
		
		public void add(String marker, int position){
			if(marker == null || marker.trim().equals("")|| position < 0){
				return;
			}
			markers.add(marker);
			positions.add(position);
			if (position>max){
				max=position;
			}
			if (position>=0 && (position < min || min ==-1)){
				min=position;
			}
		}
		
		public int findClosest(int position){
			
			return findClosest(position, defaultDistance);
		}
		
		public List<String> getMarkers(){
			return markers;
		}
		
		public List<Integer> getPositions(){
			return positions;
		}
		
		public int size(){
			return markers.size();
		}
		
		public int max(){
			return max;
		}
		
		public int min(){
			return min;
		}
		
		public int findClosest(int position, int limit){
			int closest=-1;
			int minDistance = Integer.MAX_VALUE;
			for(int i=0; i<positions.size(); i++){
				int p = positions.get(i);
				int d = Math.abs(p-position);
				if (d < limit && d<minDistance){
					minDistance = d;
					closest=p;
				}
			}
			return closest;
		}
		
		public int findClosestBefore(int position, int limit){
			int closest=-1;
			int minDistance = Integer.MAX_VALUE;
			for(int i=0; i<positions.size(); i++){
				int p = positions.get(i);
				int d = position-p;
				if (d>0 && d < limit && d<minDistance){
					minDistance = d;
					closest=p;
				}
			}
			return closest;
		}
		
		public String toString(){
			StringBuilder sb = new StringBuilder();
			for(int i=0;i<markers.size();i++){
				sb.append(markers.get(i)).append(" : ").append(positions.get(i)).append("\n");
			}
			return sb.toString();
		}
	}

}

