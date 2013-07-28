package sybyla.tag;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import sybyla.nlp.Language;
import sybyla.nlp.NLPAnalyzer;
import sybyla.nlp.OpenNLPAnalyzer3;
import sybyla.nlp.PortugueseOpenNLPAnalyzer;
import sybyla.nlp.Sequence;

public class Tagger {
	
    private static final Logger LOGGER = Logger.getLogger(Tagger.class);
    
    public static final String TERM_FREQUENCY_FILE_KEY="sybyla.term.frequency.file";
    public static final String IGNORE_CASE_KEY="sybyla.term.finder.ignore.case";
    public static final String IGNORE_CASE_PARAM="ignore.case";
    public static final String MIN_FREQUENCY_PARAM="min.frequency";

    public static final String MULTIPLE_SPACES_TEXT=  "([\\s&&[^\\n]]{2,})";
    public static final Pattern MULTIPLE_SPACES_PATTERN =Pattern.compile(MULTIPLE_SPACES_TEXT);
    
    public static final Pattern TWITTER_HASHTAG_PATTERN=Pattern.compile("(?:\\s|\\A)[##]+([A-Za-z0-9-_]+)");
    public static final Pattern TWITTER_USERNAME_PATTERN=Pattern.compile("\\b((@{1,1})(\\w){1,15})\\b");

    public static String frequencyFilePath="/oneWordTermFrequency.txt";
    public static String blacklistedTermsFilePath="/blacklistedTerms.txt";
    public static String honorificsFilePath="/honorifics.txt";
    private static final Set<String> HONORIFICS =  loadHonorifics();

    public static final String TEXT_CLUSTERS_PARAM = "clusters";
          
    private static final Map<String, Long> TERM_FREQUENCIES =  loadFrequencyFile();
    private static final Set<String> BLACKLISTED_TERMS =  loadBlacklistedTerms();

    public static final String PROPER_NOUNS ="properNouns";
    public static final String COMMON_NOUNS = "commonNouns";
    
    public static final String PEOPLE="people";
    public static final String LOCATIONS="locations";
    public static final String ORGANIZATIONS="organizations";
    public static final String QUOTES="quotes";

    private static long minFrequency = 100;
    
    private static Map<String, Long> loadFrequencyFile(){
    	
        InputStream is = Tagger.class.getResourceAsStream(frequencyFilePath);
        
        Map<String, Long> frequencies = new HashMap<String, Long>(11000000);
        
        try {
        	LOGGER.info("Loading frequency file " +frequencyFilePath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

    		Pattern p1 = Pattern.compile("[^a-zA-Z0-9-.\\'’\\p{L}]");
    		//no non-word characters allowed (a-zA-Z0-9-.'’)

    		Pattern p2 = Pattern.compile("([\\d]{5,})");
    		//no strings of numbers longer than 4
    		

            String line = null;
            try {
                while ((line=reader.readLine())!=null){
                    
                    String[] tokens = line.split("\t");
                    String term = tokens[0];
                    term =  term.toLowerCase();

                    if (    p1.matcher(term).find()
                    	||  p2.matcher(term).find()
                    	||  term.matches("[^a-zA-Z]{1,}")
                    	||  term.matches("^[^a-zA-Z].{0,}")
                    	||	term.matches("-?\\d+([\\.|,]{1,}\\d+)?") 
                    	  ){
                    	//LOGGER.info("Discarded term " + term);
                    	continue;
                    }
                    
                    String c = tokens[1];
                    long count = Long.parseLong(c);
                    Long f = frequencies.get(term);
                    if (f==null) {
                    	f = new Long(0);
                    }
                    frequencies.put(term, f+count);
                }
                reader.close();
            } catch (IOException e) {
                LOGGER.error("Could not read term frequency file "+ frequencyFilePath);
            }
        } catch (Exception e) {
            LOGGER.error("Error reading term frequency file :"+ e);
        }
        
		LOGGER.info("Loaded " + frequencies.size() + " term frequencies from " + frequencyFilePath);
		
        frequencies.put("twitter",75296662l);
        frequencies.put("facebook",75296662l);

        
        return frequencies;
    }
    
    private static Set<String> loadHonorifics(){
	    
		Set<String> honorifics = new HashSet<String>();
		InputStream is = OpenNLPAnalyzer3.class.getResourceAsStream(honorificsFilePath);
			
		try {
			LOGGER.info("Loading honorifics file " + honorificsFilePath);
			BufferedReader reader = new BufferedReader(new InputStreamReader(is,"UTF-8"));
			String line = null;
			try {
				while((line=reader.readLine())!=null){
	                              
	               String term =  line.trim();
	               
	                honorifics.add(term);
	                
	            }
	            reader.close();
	        } catch (IOException e) {
	            LOGGER.error("Could not read term honorifics file "+ honorificsFilePath,e);
	        }
			}catch (Exception e) {
				LOGGER.error("Error reading term honorifics file :"+ e);
			}
	    
			LOGGER.info("Loaded " + honorifics.size() + " honorific terms from " + honorificsFilePath);
			return honorifics;
   }
    
    public double getFrequency(String term) {
        if (term == null || term.length()==0) {
            return 0;
        }
        
        String tokens[] = term.split("\\s");
        double p=1;
        double n=0;
        double s=0;
        for (String token:tokens){
            Long c =  TERM_FREQUENCIES.get(token);
            
            long f=0;
            if (c!=null){
                f= c.longValue();
            }
            p=p*f;
            n=n+1;
            s=s+f;
        }
        //double frequency = Math.pow(p, 1/n);
        double frequency = s/n;

        return frequency;
    }
   
    public Map<String,List<Tag>> getTagsByType(String text){
    	return getTagsByType(text,null);
    }
    
    public Map<String,List<Tag>> getTagsByType(String text, Language language)  {
        

    	Map<String,List<Tag>> tags = new HashMap<String,List<Tag>>();
        
        if (text != null) {
            text = MULTIPLE_SPACES_PATTERN.matcher(text).replaceAll(" "); 
        }
        NLPAnalyzer nlp=null;
        if (language == null || language == Language.ENGLISH) {
        	nlp = new OpenNLPAnalyzer3();
        } else if (language == Language.PORTUGUESE){
        	nlp =  new PortugueseOpenNLPAnalyzer();
        } 
        
        if (nlp == null) {
            LOGGER.error("Could not obtain OpenNLPAnalyzer");
            return tags;
        }
        
        Set<Sequence>  sequences = nlp.findNounSequences(text);
        Map<String, Set<String>> dictionary =  nlp.buildDictionary(sequences);
       
        
        Map<String, Integer> counts = new HashMap<String, Integer>();
        
        for (Sequence sequence: sequences) {
        	
        	String s = sequence.toString();
        	if (s == null || OpenNLPAnalyzer3.hasInnerUnmatchedQuotes(s)){
        		continue;
        	}
        	String n = s.toLowerCase();
        	
        	if (BLACKLISTED_TERMS.contains(n)) {
        		continue;
        	}
        
            int count =countOccurrences(sequence,text);
            String type = sequence.getNounType();
            List<Tag> l = tags.get(type);
            
            if (l==null){
            	l = new ArrayList<Tag>();
            	tags.put(type, l);
            }
            
            double frequency = getFrequency(n);
            s = OpenNLPAnalyzer3.removeApostrophes(s);
    		s = OpenNLPAnalyzer3.removeUnmatchedQuotes(s);
            l.add(new Tag(s, count, frequency));
            counts.put(n, count);
            
        }
        Map<String, List<Tag>>  consolidatedTags = new HashMap<String, List<Tag>>();
        
        for (String type:tags.keySet()){
        	
        	List<Tag> tg = tags.get(type);
        	for(Tag tag: tg){
            	tag.recomputeRelevance(counts, dictionary);
            }
            Collections.sort(tg, Collections.reverseOrder());  
            List<Tag> consolidated =  consolidate(tg);
            consolidatedTags.put(type, consolidated);
        }
        
        return consolidatedTags;
    }
    public  Map<String, Set<String>> getEntities(String text){
    	OpenNLPAnalyzer3 nlp =  new OpenNLPAnalyzer3();
    	Map<Short, List<String>> entities = nlp.findAll(text);

    	Map<String, Set<String>> entityMap =  new HashMap<String, Set<String>>();
    	List<String> people = entities.get(OpenNLPAnalyzer3.PERSON);
    	Set<String> p = new HashSet<String>();
    	if (people !=null){
    		p.addAll(people);
    	}
    	entityMap.put(PEOPLE, p);
    	
    	List<String> locations = entities.get(OpenNLPAnalyzer3.LOCATION);
    	Set<String> l = new HashSet<String>();
    	if (locations !=null){
    		l.addAll(locations);
    	}
    	entityMap.put(LOCATIONS, l);
    	
    	List<String> organizations = entities.get(OpenNLPAnalyzer3.ORGANIZATION);
    	Set<String> o = new HashSet<String>();
    	if (organizations !=null){
    		o.addAll(organizations);
    	}
    	entityMap.put(ORGANIZATIONS, o);
    	
    	List<String> quotes = entities.get(OpenNLPAnalyzer3.QUOTE);
    	Set<String> q = new HashSet<String>();
    	if (quotes !=null){
    		q.addAll(quotes);
    	}
    	entityMap.put(QUOTES, q);
    	
    	return entityMap;
    }
    public List<Tag> getTags(String text)  {
        

    	List<Tag> tags = new ArrayList<Tag>();
        
        if (text != null || OpenNLPAnalyzer3.hasInnerUnmatchedQuotes(text)) {
            text = MULTIPLE_SPACES_PATTERN.matcher(text).replaceAll(" "); 
        }
                
        OpenNLPAnalyzer3 nlp = new OpenNLPAnalyzer3();
        
        Set<Sequence>  sequences = nlp.findNounSequences(text);
        Map<String, Set<String>> dictionary =  nlp.buildDictionary(sequences);
        Set<String> nouns = new HashSet<String>();
        
        for (Sequence sequence: sequences){
        	nouns.add(sequence.toString());
        }
        
        Map<String, Integer> counts = new HashMap<String, Integer>();
        
        for (String noun: nouns) {
        	
        	String n = noun.toLowerCase();
        	
        	n = OpenNLPAnalyzer3.removeUnmatchedQuotes(n);

        	if (BLACKLISTED_TERMS.contains(n)) {
        		continue;
        	}
        
            int count =countOccurrences(noun,text);
            double frequency = getFrequency(n);
            
    		noun = OpenNLPAnalyzer3.removeUnmatchedQuotes(noun);
            noun = OpenNLPAnalyzer3.removeApostrophes(noun);

    		Tag t = new Tag(noun, count, frequency);
    		if (t.getRelevance()>0){
    			tags.add(t);
    		}
            counts.put(noun.toLowerCase(), count);
            
        }
        for(Tag tag: tags){
        	tag.recomputeRelevance(counts, dictionary);
        }
    
        Collections.sort(tags, Collections.reverseOrder());   
        tags =  consolidate(tags);
        return tags;
    }
    
    private List<Tag> consolidate(List<Tag> tags){
    	
    	List<Tag> c = new ArrayList<Tag>(tags);
    	
    	for (int i=0; i< tags.size(); i++){
    		Tag t1 = tags.get(i);
    		for (int j=i+1; j <tags.size(); j++){
    			Tag t2 = tags.get(j);
    			if (t1.contains(t2)){
    				c.remove(t2);
    			}
    		}
    	}
    	return c;
    }
        
    public  Map<String,Integer> getCounts(Map<Short,List<String>> termMap, String text) {       

       Map<String, Integer> termCounts = new HashMap<String, Integer>();
       Set<String> terms = new HashSet<String>();
       
       for (Short entityType: termMap.keySet()) {
           List<String> entityList = termMap.get(entityType);
           terms.addAll(entityList);
       }
       

       for(String term: terms){
           int count = countOccurrences(term, text);
           termCounts.put(term, count);
       }
       
        return termCounts;
       
   }
   
   public  Map<String,Integer> getCounts(Set<String> terms, String text) {       
       Map<String, Integer> termCounts = new HashMap<String, Integer>();
       
       for(String term: terms){
           int count = countOccurrences(term, text);
           termCounts.put(term, count);
       }
       
        return termCounts;
       
   }
   
   public static int countOccurrences(Sequence sequence, String text){
	   
	   String s = sequence.toString();
	   if (s==null){
		   return 0;
	   }
	   return countOccurrences(s,text);
   }
   
   
   public static int countOccurrences( String term, String text){
	   
       if ((term == null) || term.trim().equals("") || term.length()==0) return 0;
       
       Pattern pattern = Pattern.compile("(?i)\\b("+Pattern.quote(term)+"([’s|\\'s|s]){0,1})\\b");

       Matcher matcher = pattern.matcher(text);
       int n = 0;
       while (matcher.find()){
    	   n++;
       }
       return n;
       
   }
   

   
	private static Set<String> loadBlacklistedTerms(){
    
		Set<String> blackList = new HashSet<String>();
		InputStream is = Tagger.class.getResourceAsStream(blacklistedTermsFilePath);
		
		try {
			LOGGER.info("Loading blacklisted file " + blacklistedTermsFilePath);
			BufferedReader reader = new BufferedReader(new InputStreamReader(is,"UTF-8"));
			String line = null;
			try {
				while((line=reader.readLine())!=null){
                               
                String term =  line.toLowerCase().trim();
                if (blackList.contains(term)){
                	LOGGER.error("duplicate blacklisted term: "+term);
                }
                blackList.add(term);
                
            }
            reader.close();
        } catch (IOException e) {
            LOGGER.error("Could not read term blacklist file "+ blacklistedTermsFilePath,e);
        }
		}catch (Exception e) {
			LOGGER.error("Error reading term blacklist file :"+ e);
		}
    
		LOGGER.info("Loaded " + blackList.size() + " blacklisted terms from " + blacklistedTermsFilePath);
		return blackList;
	}
	
    public static String removeHonorifics(String name){
    	
    	for(String h: HONORIFICS){
    		if (name.startsWith(h)){
    			return name.substring(h.length()).trim();
    		}
    	}
    	return null;
    }
	
	public static class Tag implements Comparable<Tag>{
	        
        private String term;
	    private int occurrences=0;
	    private double frequency=1;
	    private double relevance=0;
	    private String type="OTHER";
	        
	    public Tag(String term, int occurrences, double frequency) {
	    	
	        this.term = term;

	        this.occurrences =  occurrences;
	        if (frequency >= minFrequency) {
	            this.frequency =  Math.log10(frequency);
	        } else if (frequency==0){
	        	 this.frequency = Math.log10(10);
	        } else {
	            this.frequency = Math.log10(Double.MAX_VALUE);

	        }
	        relevance = ((double)Math.round(((double)this.occurrences)/this.frequency*1000))/1000;
	    }

	    public Tag(String term, int occurrences, double frequency, String type) {
	    	this (term, occurrences, frequency);
	    	this.type=type;
	    }

	    private void recomputeRelevance (Map<String, Integer> counts, 
	    							  Map<String, Set<String>> dictionary) {
	    	
	    	int hc =  getHighestCount(term, counts, dictionary);
	    	if (hc > occurrences){
	    		occurrences += hc;
		        relevance = ((double)occurrences)/frequency;
	    	} 	
	    }
	    
	    private int  getHighestCount(String term, 
	    							 Map<String, Integer> counts, 
	    							 Map<String, Set<String>> dictionary){
	    	
	    	String t = term.toLowerCase();
	    	Set<String> synonyms = dictionary.get(t);
	    	int highestCount = 0;
	    	
	    	if (synonyms ==  null){
	    		return highestCount;
	    	}
	    	
	    	for(String synonym: synonyms){
	    		Integer count = counts.get(synonym);
	    		if (count!=null){
	    			int c =  count.intValue();
	    			if (c > highestCount){
	    				highestCount =  c;
	    			}
	    		}
	    		int h = getHighestCount(synonym, counts, dictionary);
	    		if (h>highestCount){
	    			highestCount = h;
	    		}
	    	}
	    	
	    	return highestCount;
	    	
	    }
	    
	    public boolean contains(Tag t){
	    	
	    	String[] t1 = term.toLowerCase().split("\\s");
	    	String[] t2 = t.term.toLowerCase().split("\\s");
	    	
	    	if (t1.length<t2.length){
	    		return false;
	    	}
	    	
	    	int p =  t1.length-t2.length;
	    	for(int i=0; i<=p; i++){
	    		int n=0;
	    		
	    		while(n<t2.length) {
	    			if (!t1[n+i].equals(t2[n])){
	    				break;
	    			}
	    			n++;
	    		}
	    		if (n==t2.length){
	    			return true;
	    		}
	    	}
	    	return false;
	    }

	    
        @Override
        public int compareTo(Tag t) {
        	
            double x =  this.relevance - t.relevance;
            if (x>0) {
                return 1;
            } else if (x<0){
                return -1;
            }
            return 0;
        }  
        
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Tag)) {
                return false;
            }
            
            Tag t = (Tag) o;
            return this.term.equals(t.term);
        }
        
        @Override
        public int hashCode(){
            return term.hashCode();
        }

		public String getTerm() {
			return term;
		}

		public double getRelevance() {
			return relevance;
		}

		private void setTerm(String term) {
			this.term = term;
		}
		
	}	
}
