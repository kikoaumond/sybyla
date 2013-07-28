package sybyla.regex;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CapitalizedWordFinder {
	 
	  private Pattern pattern;

	  private static final String CAPITALIZED_WORD_PATTERN = 
			  "[A-Z+][a-z0-9]{0,}]";
      private static final String CAPITALIZED_WORD_SEQUENCE_PATTERN = 
    		  "((\\s){1,}" +
    		  "(\\p{Lu}){1,}" +
    		  "(\\.){0,1}" +
    		  "([-|[\\p{L}&&[^\\p{Lu}]]" +
    		  "|[\\p{Lu}]]){0,}('s){0,1}){1,}";
    		  
    		  
	  public CapitalizedWordFinder(){
		  pattern = Pattern.compile(CAPITALIZED_WORD_SEQUENCE_PATTERN);
	  }
	  
		 public Set<String> find(String s){
			  s=s.replaceAll("\\s{2,}", " ");
			  s =" "+s;
			 Set<String> instances = new HashSet<String>();
			 Matcher matcher = pattern.matcher(s);
			
			 while (matcher.find()){
				 int g = matcher.groupCount();
				 String var =  matcher.group(0);
				 instances.add(var.trim());
			  }
			 
			 return instances;
		 }
	  
	  public Set<String> find(String[] text){
		  
		Set<String> caps =  new HashSet<String>(); 
		StringBuilder sb= new StringBuilder();
		String space="";
		for (int i=0;i<text.length; i++){
			if (text[i].length()==0) continue;
			if (isCapitalized(text[i])){
				if (!(sb.length()==0 && (text[i].equals("The") || text[i].equals("A")))){
					sb.append(space).append(text[i]);
					space=" ";
				}
			} else if     (sb.length() > 0 
					   && (   text[i].equals("of") 
						   || text[i].equals("on")
						   || text[i].equals("in") 
						   || text[i].equals("the") 
						   || text[i].equals("for"))){
				if (    isCapitalized(text[i+1]) 
					|| (isConnector(text[i+1]) && isCapitalized(text[i+2]))){
					sb.append(space).append(text[i]);
				}
			} else{
				if (sb.length()!=0){
					caps.add(sb.toString());
					sb.delete(0, sb.length());
					space="";
				}
			}		
		}
		if (sb.length()>0){
			caps.add(sb.toString());
		}
		return caps;
	  }
	  

	  
	  public boolean isCapitalized(String word){
		  if (word.length()==0){
			  return false;
		  }
		  char initial = word.charAt(0);
		  if (Character.isUpperCase(initial)) {
			  return true;
		  }
		  return false;
			
	  }
	  
	  public boolean isConnector(String word){
		  if (word.equals("of") || word.equals("the")){
			  return true;
		  }
		  return false;
			
	  }
}
