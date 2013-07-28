package sybyla.classifier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

public class CategoryList {
	
	private static final Logger LOGGER = Logger.getLogger(CategoryList.class);
	
	private static final String DEFAULT_CATEGORY_ENTITY_MAP_PATH="/mnt/data/current/categoryEntityMap.txt";
	private static final String CATEGORY_ENTITY_MAP_KEY="sybyla.category.entity.map";
	
	private static String categoryEntityMapFile;
	static {
		categoryEntityMapFile = System.getProperty(CATEGORY_ENTITY_MAP_KEY, DEFAULT_CATEGORY_ENTITY_MAP_PATH);
	}
	
	

	public static void main( String[] args) {
		
		String outputFile = args[0];
		
		FileOutputStream os;
		try {
			os = new FileOutputStream(outputFile);
		} catch (FileNotFoundException e1) {
			LOGGER.error("Error opening file "+outputFile+" for writing",e1);
			return;
		}
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
		
		int nDiscardedLines=0;
		String line="";
		BufferedReader reader =  null;
		int nLines=0;
		Set<String> categories=new HashSet<String>();
		
		try{

			InputStream is = new FileInputStream(categoryEntityMapFile);
			reader = new BufferedReader(new InputStreamReader(is),4096);
			
			while((line = reader.readLine())!=null){
				nLines++;
				String[] tokens = line.split("\\t", -1);
				if (tokens == null || tokens.length<2){
					nDiscardedLines++;
					continue;
				}
				String category = tokens[1];
				
				if (category!= null && category.contains("|")){
					continue;
				}
				categories.add(category);
				
				if (nLines%100000==0){
					
					DecimalFormat lineFormatter = new DecimalFormat("##,###,###");
					String l = lineFormatter.format(nLines);
					LOGGER.info("Lines: "+l);
				}
			}
			
			List<String> categoryList = new ArrayList<String>(categories);
			Collections.sort(categoryList);
			int nCategories=0;

			for(String categoryName: categoryList){
				if (categoryName.length()<=256){
					writer.write(categoryName+"\n");
					nCategories++;
				}
				

			}
			LOGGER.info("Wrote "+nCategories+" to category list file "+outputFile);
			LOGGER.info(nDiscardedLines +" malforemed lines in file ");

		} catch(Throwable t){
			LOGGER.error("Error loading category map",t );
			LOGGER.error("Line: " +line);
		}finally{
			if (writer!=null){
				try {
					writer.close();
				} catch (IOException e) {
					LOGGER.error("Error closing writer",e);
				}
			}
		}
	}	
}
