package sybyla.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.tika.metadata.Metadata;

public class SimplePageParser {

    private static final String NOSIM_PATTERN_TEXT = "(?is)<\\s*(script|style)(>|\\s>|\\s[^>]*[^>/]>).*?<\\s*/\\s*\\1\\s*[^>]*>";
    private static final String HTML_MARKUP_PATTERN_TEXT = "<[^>]+>";
    private static final String EMPTY_LINES_PATTERN_TEXT = "(?m)^[ \t\u2016]*(\\r|\\n|\\r\\n)";
    private static final String MULTIPLE_NEWLINES_TEXT=  "([\\n\\r]+\\s*[\\n\\r]+)";
    private static final String MULTIPLE_SPACES_TEXT=  "([\\s&&[^\\n]]+)";
    
    private static final Pattern TITLE_PATTERN = Pattern.compile("(?is)<\\s*title\\s*[^>]*>(.+?)<\\s*/\\s*title[^>]*>");
    private static final Pattern BODY_START_PATTERN = Pattern.compile("(?is)<\\s*body\\s*[^>]*>");
    private static final Pattern BODY_END_PATTERN = Pattern.compile("(?is)<\\s*/\\s*body\\s*[^>]*>");
    
    private static final Pattern NOSIM_PATTERN = Pattern.compile(NOSIM_PATTERN_TEXT);
    private static final Pattern HTML_MARKUP_PATTERN = Pattern.compile(HTML_MARKUP_PATTERN_TEXT);
    private static final Pattern EMPTY_LINES_PATTERN = Pattern.compile(EMPTY_LINES_PATTERN_TEXT);
    private static final Pattern MULTIPLE_NEWLINES_PATTERN= Pattern.compile(MULTIPLE_NEWLINES_TEXT);
    private static final Pattern MULTIPLE_SPACES_PATTERN=  Pattern.compile(MULTIPLE_SPACES_TEXT);

    
    public String pageText(String url, Map<String, String> responseHeaders, byte[] content, 
                    int contentLength) throws IOException {
        
        // Convert the input data into text, so we can extract data from it.
        InputStream bais = new ByteArrayInputStream(content, 0, contentLength);
        
       Metadata metadata = new Metadata();
        //metadata.add(Metadata.CONTENT_TYPE, responseHeaders.get(HttpHeaders.CONTENT_TYPE));
        String charset = HTMLUtils.getEncoding(bais, metadata);
        if (charset == null) {
            charset = HTMLUtils.DEFAULT_CHARSET;
        }
        
        String pageText = new String(content, 0, contentLength, charset);
        
        // TODO KKr - what about entities like &amp; - do we care enough? Let's just do '&'
        pageText = pageText.replaceAll("&amp;", "&");
        pageText = pageText.replaceAll("&#160;", " ");
        pageText = pageText.replaceAll("&nbsp;", " ");
        
       // Now we want to use some regex to get the title and the body of the text.
        String title = "";
        Matcher titleMatcher = TITLE_PATTERN.matcher(pageText);
        if (titleMatcher.find()) {
            title = titleMatcher.group(1);
        }
        
        int bodyStart = 0;
        Matcher bodyStartMatcher = BODY_START_PATTERN.matcher(pageText);
        if (bodyStartMatcher.find()) {
            bodyStart = bodyStartMatcher.end();
        }
        
        int bodyEnd = pageText.length();
        Matcher bodyEndMatcher = BODY_END_PATTERN.matcher(pageText);
        if (bodyEndMatcher.find()) {
            bodyEnd = bodyEndMatcher.start();
        }
        
        if ((bodyStart > 0) || (bodyEnd < pageText.length())) {
            pageText = pageText.substring(bodyStart, bodyEnd);
        }
        
        pageText = NOSIM_PATTERN.matcher(pageText).replaceAll(" ");
        pageText = HTML_MARKUP_PATTERN.matcher(pageText).replaceAll(" ");
        pageText = MULTIPLE_NEWLINES_PATTERN.matcher(pageText).replaceAll("\n");
        pageText = MULTIPLE_SPACES_PATTERN.matcher(pageText).replaceAll(" ");
                
        pageText = StringEscapeUtils.unescapeHtml4(pageText);
        
        return pageText;
        
    }


	public String pageText(String htmlText) throws IOException {
	    
	    htmlText = htmlText.replaceAll("&amp;", "&");
	    htmlText = htmlText.replaceAll("&#160;", " ");
	    htmlText = htmlText.replaceAll("&nbsp;", " ");
	    
	   // Now we want to use some regex to get the title and the body of the text.
	    String title = "";
	    Matcher titleMatcher = TITLE_PATTERN.matcher(htmlText);
	    if (titleMatcher.find()) {
	        title = titleMatcher.group(1);
	    }
	    
	    int bodyStart = 0;
	    Matcher bodyStartMatcher = BODY_START_PATTERN.matcher(htmlText);
	    if (bodyStartMatcher.find()) {
	        bodyStart = bodyStartMatcher.end();
	    }
	    
	    int bodyEnd = htmlText.length();
	    Matcher bodyEndMatcher = BODY_END_PATTERN.matcher(htmlText);
	    if (bodyEndMatcher.find()) {
	        bodyEnd = bodyEndMatcher.start();
	    }
	    
	    if ((bodyStart > 0) || (bodyEnd < htmlText.length())) {
	        htmlText = htmlText.substring(bodyStart, bodyEnd);
	    }
	    
	    htmlText = NOSIM_PATTERN.matcher(htmlText).replaceAll(" ");
	    htmlText = HTML_MARKUP_PATTERN.matcher(htmlText).replaceAll(" ");
	    htmlText = MULTIPLE_NEWLINES_PATTERN.matcher(htmlText).replaceAll("\n");
	    htmlText = MULTIPLE_SPACES_PATTERN.matcher(htmlText).replaceAll(" ");
	            
	    htmlText = StringEscapeUtils.unescapeHtml4(htmlText);
	    
	    return htmlText;
	    
	}
    
    
}
