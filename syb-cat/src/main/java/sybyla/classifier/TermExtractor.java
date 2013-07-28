package sybyla.classifier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TermExtractor {
    public Set<String> extract(String[][] tokens, int maxTerms, boolean isCaseSensitive) {
        
        StringBuffer sb = new StringBuffer();
        Set<String> terms = new HashSet<String>();
        for(int i=0;i<tokens.length;i++) {
            for( int j=0; j<tokens[i].length; j++) {
                int p=0;
                sb.delete(0, sb.length());
                while(p<=maxTerms && j+p < tokens[i].length) {
                     String t = tokens[i][j+p];//.replaceAll("[*()\",;:]", " ").trim();
                     //String t = t.replaceAll("\'s", "");
                     if (!isCaseSensitive) {
                         t = t.toLowerCase();
                     }
                     if (p>0){
                    	 sb.append(" ");
                     }
                     sb.append(tokens[i][j].trim());
                     p++;
                     
                     terms.add(sb.toString().trim());
                     
                }
            }
        }
        return terms;
    }
    
    public Map<String, Integer> extractWithCounts(String[][] tokens, int maxTerms, boolean isCaseSensitive) {
        
        StringBuffer sb = new StringBuffer();
        Map<String, Integer> terms = new HashMap<String,Integer>();
        
        for(int i=0;i<tokens.length;i++) {
            for( int j=0; j<tokens[i].length; j++) {
                int p=0;
                sb.delete(0, sb.length());
                while(p<maxTerms && j+p < tokens[i].length) {
                     String t = tokens[i][j+p].replaceAll("[*()\",;:.]", "").trim();
                     t = t.replaceAll("\'s", "");
                     t= t.replaceAll("’s","");
                     if (!isCaseSensitive) {
                         t = t.toLowerCase();
                     }
                     if(p>0){
                         sb.append(" ");
                     }
                     sb.append(t.trim());
                     p++;
                     String s = sb.toString();
                     Integer c = terms.get(s);
                     if (c == null) {
                         c= new Integer(0);
                     }
                     c++;
                     terms.put(s,c); 
                     
                }
            }
        }
        return terms;
    }
}
