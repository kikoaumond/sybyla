package sybyla.graph.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

import sybyla.graph.GraphLoader;

public class GraphFileReader {

	private static final Logger LOGGER = Logger.getLogger(GraphFileReader.class);
	private static Set<File> files = new HashSet<File>();
	private static Iterator<File> iterator;
	private static BufferedReader reader;
	private static int nPairs=0;

	
	public static void getGraphFiles(String graphFilesPath) {
	    
	    if (graphFilesPath == null) {
	        LOGGER.error("No path for graph files  supplied");
	        return;
	    }
	    
	    LOGGER.info("Loading graph files in " + graphFilesPath);
	    File graphFilesDir= new File(graphFilesPath);
	    
	    if (!graphFilesDir.isDirectory()) {
	        LOGGER.error("Graph files path "+ graphFilesPath + " is not a directory.");
	    }
	    
	    File[] graphFiles = graphFilesDir.listFiles();
	        
	    for (File file : graphFiles) {
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
	        LOGGER.error("No graph files found in " + graphFilesPath);
	        return;
	    }
	}
	
	private static boolean nextFile() throws FileNotFoundException{
		
		if (iterator == null){
	        iterator = files.iterator();
	    } 
		
		if (iterator == null || !iterator.hasNext()){
			return false;
		}
		
		File file = iterator.next();
		if (file == null){
			return false;
		}
		
        reader  = new BufferedReader(new FileReader(file));
        return true;
	}
	
	private static String read() throws IOException{
		
		if (reader==null){
			return null;		
		}
		
		String line = reader.readLine() ;
	    if (line ==  null){
	    	if (!nextFile()){
	    		return null;
	    	}
	    	line = reader.readLine();
	    }
	    
	    return line;
	}
	
	public static void load(String graphFilesPath, GraphLoader gl, boolean batch) throws NumberFormatException, IOException{
		getGraphFiles(graphFilesPath);
		if (!nextFile()){
			return;
		}
		
	    String line;
	    
		while((line = read())!=null){
			
			String[] tokens = line.split("\t");
            
			if (tokens.length != 5) {
            	LOGGER.error("Malformed line in file:\n"+line);
                continue;
            }
            
			if (tokens[0].startsWith("part-")) {
                tokens[0] =  tokens[0].substring("part-00000:".length());
            }
			
			nPairs++;
            if (nPairs%100000==0){
            	LOGGER.info("Loaded "+nPairs*2 +" graph edges into database");
            }
            
            int c = Integer.parseInt(tokens[0]);
            String term = tokens[1];
            int t = Integer.parseInt(tokens[2]);
            String related = tokens[3];
            int r = Integer.parseInt(tokens[4]);
            if(batch){
            	gl.insertBatch(c, term, t, related, r);
            } else{
            	gl.insert(c, term, t, related, r);
            }
		}	 
		
		LOGGER.info("Loaded "+nPairs*2 +" graph edges into database");
	}
}
