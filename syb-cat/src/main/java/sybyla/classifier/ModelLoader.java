package sybyla.classifier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import sybyla.classifier.CategoryMap.CategoryMapEntry;
import sybyla.io.FastFileReader;
import sybyla.ml.BinaryWinnow;

public class ModelLoader {
	private final static Logger LOGGER = Logger.getLogger(ModelLoader.class);
	
    private static boolean caseSensitive = false;   
    private static boolean useCategoryMap = true;
    private static final String MANUAL_MODEL_FILE="/manualCategoryModels.txt";
    private static final String MANUAL_MODEL_FILE_PORTUGUESE="/manualCategoryModelsPortuguese.txt";

    
    public static void main(String[] args){
    	String categoryModelsPath = args[0];
    	String categoryListFile = args[1];
    	try {
			loadCategoryModels(categoryModelsPath,categoryListFile);
		} catch (IOException e) {
			LOGGER.error("Error loading category models",e);
		}
    }
	
    protected static Set<BinaryWinnow> loadCategoryModels(String categoryModelsPath) {
    	try{
    		return loadCategoryModels(categoryModelsPath, null);
    	}catch(IOException e){
    		LOGGER.error("error loading category models");
    	}
    	return null;
    }
    private static Set<BinaryWinnow> loadManualModels(){
    	Set<BinaryWinnow> manualModels =  loadManualModel(MANUAL_MODEL_FILE);
    	Set<BinaryWinnow> portugueseManualModels = loadManualModel(MANUAL_MODEL_FILE_PORTUGUESE);
    	Set<BinaryWinnow> models = new HashSet<BinaryWinnow>();
    	models.addAll(manualModels);
    	models.addAll(portugueseManualModels);
    	return models;
    }
    
    private static Set<BinaryWinnow> loadManualModel(String fileName){
    	Set<BinaryWinnow> manualModels = new HashSet<BinaryWinnow>();
		try{

			InputStream is = ModelLoader.class.getResourceAsStream(fileName);
			BufferedReader reader = new BufferedReader(new InputStreamReader(is,"UTF-8"));
			String line="";
			
			 while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("#")){
                	continue;
                }
             	String[] terms = line.split("\t");
                 
                 String category = terms[0];
                 if (useCategoryMap){
                 	Set<CategoryMapEntry> categoryEntry = CategoryMap.getCategoryEntry(category);
                 	if (categoryEntry == null){
                 		continue;
                 	}
                 }
                 
                 try {
                 	final BinaryWinnow winnow = BinaryWinnow.load(line, false);

                     
                     if (winnow != null && winnow.get_label().trim().length() > 1 
                         && !winnow.get_label().startsWith("*")
                         && (!winnow.get_label().toLowerCase().contains("people"))
                         && (!winnow.get_label().toLowerCase().contains("article"))
                         && (!winnow.get_label().toLowerCase().contains("alumni"))
                         && (!winnow.get_label().toLowerCase().contains("births"))
                         && (!winnow.get_label().toLowerCase().contains("deaths"))){
                     	
                             manualModels.add(winnow);
                         
                             LOGGER.info("Loaded manual category model "+ winnow.get_label());
                     }
                 } catch (Exception e) {
                     LOGGER.error("Could not parse manual category model file line "+line+ MANUAL_MODEL_FILE,e);
                 }
             } 
		} catch(IOException e){
			LOGGER.error("Error loading manual model file",e );
		}
		return manualModels;
    }
    
    protected static Set<BinaryWinnow> loadCategoryModels(String categoryModelsPath, String categoryListFile) throws IOException {
        
    	Set<BinaryWinnow> categoryModels = new HashSet<BinaryWinnow>();
        Set<BinaryWinnow> manualModels = loadManualModels();
        
        categoryModels.addAll(manualModels);
        
		BufferedWriter out =null;
		
		if (categoryListFile !=null){
        	FileWriter fstream = new FileWriter(categoryListFile);
        	out = new BufferedWriter(fstream);
        }
        if (categoryModelsPath == null) {
            LOGGER.error("No path for category models  supplied");
            return null;
        }
        LOGGER.info("Loading category models in " + categoryModelsPath);
        File categoryModelsDir= new File(categoryModelsPath);
    
        if (!categoryModelsDir.isDirectory()) {
            LOGGER.error("Category model path "+ categoryModelsPath+ " is not a directory.");
        }
    
        File[] categoryModelFiles = categoryModelsDir.listFiles();
        Set<File> files =  new HashSet<File>();
        for (File file : categoryModelFiles) {
            if (file.getName().startsWith("part-")) {
                files.add(file);
            }
            if (file.isDirectory()) {
                File[] subdirFiles = file.listFiles();
                for(File subdirFile: subdirFiles) {
                    if (subdirFile.getName().startsWith("part-")) {
                        files.add(subdirFile);
                    }
                }
            }
        }
      
    
        if(files.size() == 0) {
            LOGGER.error("No category model files found in " + categoryModelsPath);
            return null;
        }
        
        int nModels = 0;
        BinaryWinnow.setCaseSensitve(caseSensitive);
        int nBytes=0;
        for (File file: files) {

            try {
                FastFileReader reader = new FastFileReader(file);
                LOGGER.info("Opened file "+file);
                try {
                	String line =null ;
                	
                    while ((line = reader.readLine()) != null) {
                        
                    	String[] terms = line.split("\t");
                        
                        String category = terms[0];
                        if (useCategoryMap){
                        	Set<CategoryMapEntry> categoryEntry = CategoryMap.getCategoryEntry(category);
                        	if (categoryEntry == null){
                        		continue;
                        	}
                        }
                        if (out!=null){
                        	out.write(category+"\n");
                        	out.flush();
                        }
                        
                        try {
                        	final BinaryWinnow winnow = BinaryWinnow.load(line);

                            
                            if (winnow != null && winnow.get_label().trim().length() > 1 
                                && !winnow.get_label().startsWith("*")
                                && (!winnow.get_label().toLowerCase().contains("people"))
                                && (!winnow.get_label().toLowerCase().contains("article"))
                                && (!winnow.get_label().toLowerCase().contains("alumni"))
                                && (!winnow.get_label().toLowerCase().contains("births"))
                                && (!winnow.get_label().toLowerCase().contains("deaths"))){
                            	
                                    categoryModels.add(winnow);
                                
                                nModels++;
                                nBytes+=winnow.get_nTermBytes();
                                LOGGER.trace("Loaded category model "+ winnow.get_label());
                                LOGGER.trace("Loaded "+ nBytes+" bytes");
                                if (nModels%100==0){
                                	LOGGER.debug("Loaded "+nModels+" models, "+nBytes/1000000+" Mb");
                                }
                            }
                        } catch (BinaryWinnow.WinnowParseException e) {
                            LOGGER.error("Could not parse category model file "+ file.getName());
                        }
                    } 
                } catch (IOException e) {
                    LOGGER.error("Could not read category model file "+ file.getName());
                }
                reader.close();

            } catch (FileNotFoundException e) {
                LOGGER.error("Category model file "+ file.getName() + " was not found.");
            }
        
        }
        LOGGER.info("Finished loading " + nModels + " category models from " + categoryModelsPath);
        if (out != null){
        	out.close();
        }
        return categoryModels;
	}
}
