package sybyla.wikipedia;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WikipediaRegex {
	
	public static final String WIKIPEDIA_MARKUP_REGEX= "(?is)(\\[[^\\[\\]]{0,}\\])";
	public static final Pattern WIKIPEDIA_MARKUP_PATTERN= Pattern.compile(WIKIPEDIA_MARKUP_REGEX);
	
	public static final String WIKIPEDIA_FORMATTING_REGEX= "(?is)[\\']{2,}([^\\']{1,})[\\']{2,}";
	public static final Pattern WIKIPEDIA_FORMATTING_PATTERN= Pattern.compile(WIKIPEDIA_FORMATTING_REGEX);

	public static final String WIKIPEDIA_REFERENCE_REGEX="(?is)(<\\s{0,}ref[^>]{0,}>[^<>]{1,}</ref>)";
	public static final Pattern WIKIPEDIA_REFERENCE_PATTERN= Pattern.compile(WIKIPEDIA_REFERENCE_REGEX);
	
	public static final String WIKIPEDIA_HEADING_REGEX= "(?is)([\\=]{2,})([^\\=]{2,})\\1";
	public static final Pattern WIKIPEDIA_HEADING_PATTERN= Pattern.compile(WIKIPEDIA_HEADING_REGEX);

	public static final Pattern WIKIPEDIA_ALTERNATE_PATTERN= Pattern.compile("(?is)([^\\|\\[\\]]{1,})\\|([^\\|\\[\\]]{1,})");
	
	public static final Pattern WIKIPEDIA_ENGLISH_CATEGORY_PATTERN= Pattern.compile("(?is)\\[\\[[:]{0,1}Category:([^\\[\\]]{1,})[|]{0,1}\\]\\]");
	public static final Pattern WIKIPEDIA_PORTUGUESE_CATEGORY_PATTERN= Pattern.compile("(?is)\\[\\[[:]{0,1}Categoria:([^\\[\\]]{1,})[|]{0,1}\\]\\]");

	public static final String WIKIPEDIA_ANNOTATION_REGEX="(?is)(\\[\\[[\\w]{1,}:[^\\[\\]]{1,}\\]\\])";
	public static final Pattern WIKIPEDIA_ANNOTATION_PATTERN= Pattern.compile(WIKIPEDIA_ANNOTATION_REGEX);

	public static final String WIKIPEDIA_INTERNAL_LINK_REGEX = "(?is)\\[\\[([^:\\[\\]]{1,})\\]\\]";
	public static final Pattern WIKIPEDIA_INTERNAL_LINK_PATTERN= Pattern.compile(WIKIPEDIA_INTERNAL_LINK_REGEX);
	
	public static final String WIKIPEDIA_INTERNAL_ALT_LINK_REGEX = "(?is)\\[\\[[^:\\[\\]|]{1,}\\s{0,}\\|\\s{0,}([^\\[\\]]{1,})\\]\\]";
	public static final Pattern WIKIPEDIA_INTERNAL_ALT_LINK_PATTERN= Pattern.compile(WIKIPEDIA_INTERNAL_ALT_LINK_REGEX);
		
	public static final String WIKIPEDIA_METADATA_REGEX= "(?is)\\{\\{([^\\{\\}]{1,})\\}\\}";
	public static final Pattern WIKIPEDIA_METADATA_PATTERN= Pattern.compile(WIKIPEDIA_METADATA_REGEX);
	
	public static final String WIKIPEDIA_METADATA_REGEX2= "(?is)\\{([^\\{\\}]{1,})\\}";
	public static final Pattern WIKIPEDIA_METADATA_PATTERN2= Pattern.compile(WIKIPEDIA_METADATA_REGEX2);
	
	public static final String HTML_COMMENT_REGEX= "(?s)<!--.*?-->";
	public static final Pattern HTML_COMMENT_PATTERN= Pattern.compile(HTML_COMMENT_REGEX);
	
	
	public static final String WIKIPEDIA_HTML_MARKUP_REGEX="(?is)<\\s{0,}[^>]{1,}\\s{0,}[/]{0,1}\\s{0,}>";
	public static final Pattern WIKIPEDIA_HTML_MARKUP_PATTERN=Pattern.compile(WIKIPEDIA_HTML_MARKUP_REGEX);

	public static final String WIKIPEDIA_COMMON_HEADERS_REGEX="==\\s?((References)|(Further reading)|(External links)|(See also)|(Referências)|(Leitura Adicional)|(Ver também)|(Ligações externas))\\s?==";
	public static final Pattern WIKIPEDIA_COMMON_HEADERS_PATTERN=Pattern.compile(WIKIPEDIA_COMMON_HEADERS_REGEX);
	
	public static final String WIKIPEDIA_BULLET_REGEX="\\r?\\n\\s?([\\*]{1,})";
	
	public static Set<String> findMarkup(String text){
		
		Set<String> markups = new HashSet<String>();
		
		Matcher matcher = WIKIPEDIA_MARKUP_PATTERN.matcher(text);
		
		while( matcher.find()){
			
			String markup = matcher.group(matcher.groupCount());
			Matcher alternate =  WIKIPEDIA_ALTERNATE_PATTERN.matcher(markup);
			
			if (alternate.find()){
				for(int i=1;i<=alternate.groupCount();i++){
					String alt = alternate.group(i);
					markups.add(alt);
				}
			} else {
				markups.add(markup);
			}
		}
		
		return markups;
		
	}
	
	public static Set<String> findReferences(String text){
		return find(text,WIKIPEDIA_REFERENCE_PATTERN);
	}
	
	public static Set<String> findCategoriesInEnglish(String text){
				
		return find(text,WIKIPEDIA_ENGLISH_CATEGORY_PATTERN);
	}
	
	public static Set<String> findCategoriesInPortuguese(String text){
		
		return find(text,WIKIPEDIA_PORTUGUESE_CATEGORY_PATTERN);
	}
	
	public static Set<String> findInternalLinks(String text){
		
		return findWithAlts(text,WIKIPEDIA_INTERNAL_LINK_PATTERN, WIKIPEDIA_ALTERNATE_PATTERN);
	}
	
	public static String cleanInternalLinks(String text){
		
		return replace (text, WIKIPEDIA_INTERNAL_LINK_REGEX, "$1");
	}
	
	public static Set<String> findInternalAltLinks(String text){
		
		return find(text,WIKIPEDIA_INTERNAL_ALT_LINK_PATTERN);
	}
	
	public static String cleanInternalAltLinks(String text){
		
		return replace (text, WIKIPEDIA_INTERNAL_ALT_LINK_REGEX, "$1");
	}
	
	public static String cleanFormatting(String text){
		
		return replace (text, WIKIPEDIA_FORMATTING_REGEX, "$1");
	}
	
	public static String deleteMarkup(String text){
		
		return replace (text, WIKIPEDIA_MARKUP_REGEX, "");
	}
	
	public static String deleteHTMLComments(String text){
		String[] chunks = text.split("(?is)<\\s!\\s-\\s");
		return replace (text,HTML_COMMENT_REGEX , "");
	}
	
	public static String deleteReferences(String text){
		
		return replace(text, WIKIPEDIA_REFERENCE_REGEX, "");
	}
	public static String deleteHTMLMarkup(String text){
		
		return replace(text, WIKIPEDIA_HTML_MARKUP_REGEX, "");
	}
	
	public static String deleteAnnotations(String text){
		
		return replace(text, WIKIPEDIA_ANNOTATION_REGEX, "");
	}
	
	public static String cleanSections(String text){
		
		return replace (text, WIKIPEDIA_HEADING_REGEX, "$2");
	}
	
	public static String deleteMetadata(String text){
		
		String t = replace (text, WIKIPEDIA_METADATA_REGEX, "");
		t =  replace(t, WIKIPEDIA_METADATA_REGEX2, "");
		return t;
	}
	
	public static String deleteBullets(String text){
		
		return replace (text, WIKIPEDIA_BULLET_REGEX, "\n");
	}
	
	public static String deleteCommonHeaders(String text){
		
		return replace (text, WIKIPEDIA_COMMON_HEADERS_REGEX, "");
	}
	
	public static Set<String> findHTMLMarkup(String text){
		
		return find (text, WIKIPEDIA_HTML_MARKUP_PATTERN);
	}
	
	public static Set<String> find(String text, Pattern pattern){
		
		Set<String> markups = new HashSet<String>();
		
		Matcher matcher = pattern.matcher(text);
		
		while( matcher.find()){
			
			String markup = matcher.group(matcher.groupCount());
			
			if (markup==null){
				continue;
			}
			
			if (markup.contains("|")) {
				String[] ss = markup.split("\\|");
				for (int i=0; i<ss.length;i++ ){
					if (!ss[i].trim().equals("")){
						markups.add(ss[i]);
					}
				}
			} else {
				markups.add(markup.trim());
			}
			
		}
		
		return markups;
	}
	
	String[] getAlternates(String s){
		String[] ss=null;
		if (s!=null && s.contains("|")){
			ss = s.split("\\|");
		}
		return ss;
	}
	
	public static Set<String> findWithAlts(String text, Pattern pattern, Pattern altPattern){
		
		Set<String> markups = new HashSet<String>();
		
		Matcher matcher = pattern.matcher(text);
		
		while( matcher.find()){
			String markup = matcher.group(matcher.groupCount());
			Matcher altMatcher = altPattern.matcher(markup);
			if (altMatcher.find()){
				int n =  altMatcher.groupCount();
				for(int i=1;i<=n;i++){
					String alt = altMatcher.group(i);
					markups.add(alt.trim());
				}
			}else{
				markups.add(markup.trim());
			}
		}
		
		return markups;
	}
	
	public static Set<String> findRecursive(String text, Pattern pattern, Set<String> instances){
		
		if (instances == null){
			instances = new HashSet<String>();
		}
		
		Matcher matcher = pattern.matcher(text);
		
		while( matcher.find()){
			
			String markup = matcher.group(matcher.groupCount());
			if (pattern.matcher(markup).find()){
				instances = findRecursive(markup,pattern, instances);
			} else{
				instances.add(markup.trim());
			}
		}
		
		return instances;
	}
	
	public static String replace(String text, String regex, String replacement){
		
		String t = text.replaceAll(regex, replacement);
		return t;
	}
	
	public static String delete(String text, String regex){
		
		return replace(text, regex,"");
	}
	
}
