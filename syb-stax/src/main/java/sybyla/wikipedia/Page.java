package sybyla.wikipedia;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringEscapeUtils;

public class Page {
	
	private String title;
	private String body;
	private Set<String> categories;
	private Set<String> links;
	private Set<String> synonyms;
	
	public Page(){};
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String text) {

		categories =  WikipediaRegex.findCategoriesInEnglish(text);
		
		if (categories == null || categories.size()==0){
			categories = WikipediaRegex.findCategoriesInPortuguese(text);
		}
		//NOTE: the order of operations below is important.  If the
		//order is changed, there will be errors in parseing the document's body
		String t= WikipediaRegex.deleteAnnotations(text);
		
		links = WikipediaRegex.findInternalLinks(t);
		
		t =  WikipediaRegex.cleanInternalAltLinks(t);

		t =  WikipediaRegex.cleanInternalLinks(t);
		
		t = WikipediaRegex.cleanFormatting(t);
		
		t = WikipediaRegex.deleteCommonHeaders(t);
		
		t = WikipediaRegex.cleanSections(t);
		
		t = WikipediaRegex.deleteMarkup(t);
		
		t = WikipediaRegex.deleteMarkup(t);
		
		t = WikipediaRegex.deleteMetadata(t);
		
		t = StringEscapeUtils.unescapeHtml4(t);
		
		t = WikipediaRegex.deleteReferences(t);
		
		t = WikipediaRegex.deleteHTMLComments(t);
		
		t = WikipediaRegex.deleteHTMLMarkup(t);
		
		t = WikipediaRegex.deleteBullets(t);
		
		
		body=t.trim();
		
	}

	public Set<String> getCategories() {
		return categories;
	}

	public void setCategories(Set<String> categories) {
		this.categories = categories;
	}

	public Set<String> getLinks() {
		return links;
	}

	public void setLinks(Set<String> links) {
		this.links = links;
	}

	public Set<String> getSynonyms() {
		return synonyms;
	}

	public void addSynonym(String synonym) {
		if (synonyms == null){
			synonyms = new HashSet<String>();
		}
		synonyms.add(synonym);
	}
}
