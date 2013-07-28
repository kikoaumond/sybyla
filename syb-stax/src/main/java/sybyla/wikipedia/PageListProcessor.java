package sybyla.wikipedia;

import java.util.ArrayList;
import java.util.List;

public class PageListProcessor implements PageProcessor{
	
	private List<Page> pages = new ArrayList<Page>();
	
	@Override
	public void processPage(Page page) {
		pages.add(page);	
	}
	
	public List<Page> getPages(){
		return pages;
	}

}
