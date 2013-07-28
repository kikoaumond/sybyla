package sybyla.regex;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
 
public class Template{
 
	  
	  public static final String TEMPLATE_PATTERN = "\\$\\{([A-Za-z0-9_-]+)\\}";
	  public static final String CONDITIONAL_TEMPLATE_PATTERN = "\\?\\{([.]+\\$\\{[A-Za-z0-9_-]+\\}[.]+)\\}";
	  private static final Pattern PATTERN= Pattern.compile(TEMPLATE_PATTERN);;
	  
 
	 public Set<String> find(String s, String p){
		 Set<String> instances = new HashSet<String>();
		 Pattern pttrn = Pattern.compile(p);
		 Matcher matcher = pttrn.matcher(s);
		
		 while (matcher.find()){
			 int g = matcher.groupCount();
			 String var =  matcher.group(g);
			 instances.add(var);
		  }
		 
		 return instances;
	 }

	  public static Set<String> getVariables(String s){
		  Set<String> variables = new HashSet<String>();
		  Matcher matcher = PATTERN.matcher(s);
		  while (matcher.find()){
			 int g = matcher.groupCount();
			 String var =  matcher.group(g);
			 variables.add(var);
		  }
		  
		  return variables;
	  }
	  
	  public static Set<String> getInstances(String regex, String query){
		  Pattern pattern = Pattern.compile(regex);
		  Set<String> variables = new HashSet<String>();
		  Matcher matcher = pattern.matcher(query);
		  while (matcher.find()){
			 int g = matcher.groupCount();
			 String var =  matcher.group(g);
			 variables.add(var);
		  }
		  
		  return variables;
	  }
	  
	  private  static String substitute(final String s, Map<String,String> params){
		  Set<String> vars = getVariables(s);
		  String sub = new String(s);
		  for(String name: params.keySet()){
			 
			  String v = params.get(name);
			  if (v==null) continue;
			  String value = escape(v.toString());
			  
			  sub = sub(sub,name, value);
			  vars.remove(name);
		  }
		  
		  for(String var: vars){
			  String conditionalRegex = conditionalPattern(var);
			  sub = sub.replaceAll(conditionalRegex, "");
		  }
		  
		  return sub; 
	  }
	  
	  public  static <T> String sub(final String s, Map<String,T> params){
		  Map<String,String> m = new HashMap<String,String>();
		  for(String k: params.keySet()){
			  T v = params.get(k);
			  String sv = null;
			  if (v!=null){
				  sv = v.toString();
				  m.put(k, sv);
			  } else {
				  m.put(k, "");
			  }
			  
		  }
		  return substitute(s,m);
	  }
	  
	  public static <T> String sub(final String s, String paramName, T paramValue){
		  
		  StringBuffer sub = new StringBuffer(s);
		  String rs=new String(s);
		  
		  if (paramValue ==  null){
			  return rs;
		  }
		  
		  String p = conditionalPattern(paramName);
		  Pattern pttrn = Pattern.compile(p);
		  Matcher matcher = pttrn.matcher(s);
		  
		  StringBuffer regex = new StringBuffer("\\$\\{");
		  regex.append(paramName).append("\\}");
		  String rg = regex.toString();
		  
		  if (paramValue != null) {
			  String v = escape(paramValue.toString());
			  while(matcher.find()){
				  int g = matcher.groupCount();
				  String instance =  matcher.group(g);
				  String replacement = instance.substring("?{".length(), instance.length()-"}".length());
				  replacement = replacement.replaceAll(rg, v);
				  int start =matcher.start(g);
				  int end = matcher.end(g);
				  sub=sub.replace(start, end, replacement);
			  }
			  
			  rs = sub.toString().replaceAll(rg,v);
			 
					   
		  } 
		return rs;
	  }
	  
	  public static <T> String sub(final String s, String paramName, List<T> values){
		  
		  String sub = new String(s);
		  
		  if (values  ==  null || values.size()==0){
			  StringBuffer regex = new StringBuffer("\\$\\$\\{");
			  regex.append(paramName).append("\\}");
			  sub = sub.replaceAll(regex.toString(), "");
			  return sub;
		  }
		  
		  String p = "$${"+paramName+"}";
		  int pos = sub.indexOf(p);
		  if (pos==-1){
			  return sub;
		  }
		  
		  String q="";
		  if(pos > 0 && sub.charAt(pos-1)=='\'' && sub.length() >= pos+p.length() && sub.charAt(pos+p.length()) == '\'') {
			  q="'";
		  }
		  
		  
		  
		  StringBuffer valueList = new StringBuffer();
		  String v  = values.get(0).toString();
		  if(q.equals("'")) {
			  v =  escape(v);
		  }
		  valueList.append(q).append(values.get(0)).append(q);
		  
		  for(int i=1; i<values.size(); i++) {
			  v=values.get(i).toString();
			  if(q.equals("'")) {
				  v =  escape(v);
			  }
			  valueList.append(", ").append(q).append(v).append(q);
		  }
		  
		  StringBuffer regex = new StringBuffer(q).append("\\$\\$\\{").append(paramName).append("\\}").append(q);
		  String vl = valueList.toString();
		  String rg = regex.toString();
		  sub = sub.replaceAll(rg, vl);
		  
		  return sub;
		   
	  }
	  
		public static String escape(String s) {
			String e = s.replace("'", "\\\'").replace("\"", "\\\"");
			return e;
		}
		
		public static String pattern(String s){
			String conditionalPattern = "$\\{"+s+"\\}";
			return conditionalPattern;
		}
		
		public static String conditionalPattern(String s){
			String conditionalPattern = "\\?\\{.*\\"+pattern(s)+".*\\}";
			return conditionalPattern;
		}
		
		
	  
}
