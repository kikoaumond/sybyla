package sybyla.wikipedia;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Set;

import org.apache.log4j.Logger;

public class PageCategoryProcessor implements PageProcessor{
	
	private static final Logger LOGGER = Logger.getLogger(PageCategoryProcessor.class);
	
	private BufferedWriter categoryWriter;
	private BufferedWriter synonymWriter;

	int nPages=0;
	
	public PageCategoryProcessor(String categoryFile, String synonymFile) throws IOException{
		
		categoryWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(categoryFile),"UTF-8"));
		LOGGER.info("Writing to category map file "+categoryFile);
		
		synonymWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(synonymFile)));
		LOGGER.info("Writing to synonym file "+synonymFile);

	}
	
	@Override
	public void processPage(Page page) {
		
		nPages++;
		
		try {
			
			if (nPages%5000==0){
				LOGGER.info(nPages+ " pages processed");
				categoryWriter.flush();
			}
			
			String title = page.getTitle();
			Set<String> categories = page.getCategories();
			
			if (categories!=null){
				for (String category:categories){
					categoryWriter.write(title+"\t"+category+"\n");
				}
			}
			
			Set<String> synonyms =  page.getSynonyms();
			if (synonyms!=null){
				for(String synonym: synonyms){
					synonymWriter.write(title+"\t"+synonym+"\n");
				}
			}
			
		} catch (IOException e) {
			LOGGER.error("Error writing to file ",e);
		}
	}
	
	public void close() throws IOException{
		
		if (categoryWriter !=  null){
			categoryWriter.close();
		}
		if (synonymWriter !=  null){
			synonymWriter.close();
		}
	}
}
