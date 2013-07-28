package sybyla.graph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;


public class GraphFileLoader {
	private static final Logger LOGGER = Logger.getLogger(GraphFileLoader.class);
	//defaults
    public static double minCorrelation=3;
    public static int graphDepth=2;
    public static int maxCorrelatedEntities=10;
    private static boolean ignoreCase =  false;


	public static Map<String, List<CorrelatedEntity>> loadGraph(String graphFilesPath) {

	    if (graphFilesPath == null) {
	        LOGGER.error("No path for graph files  supplied");
	        return null;
	    }
	    
	    Map<String, List<CorrelatedEntity>> graph = new HashMap<String,List<CorrelatedEntity>>();   
	    
	    LOGGER.info("Loading graph files in " + graphFilesPath);
	    File graphFilesDir= new File(graphFilesPath);
	    
	    if (!graphFilesDir.isDirectory()) {
	        LOGGER.error("Graph files path "+ graphFilesPath + " is not a directory.");
	    }
	    
	    File[] graphFiles = graphFilesDir.listFiles();
	        
	    Set<File> files =  new HashSet<File>();
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
	        return graph;
	    }
	    int nPairs = 0;
	    for (File file: files) {
	        try {
	            BufferedReader reader = new BufferedReader(new FileReader(file));
	            String line = null;
	            try {
	                do{
	                    line = reader.readLine() ;
	                    if (line ==  null) {
	                        break;
	                    }
	                    try {
	                        nPairs++;
	                        String[] tokens = line.split("\t");
	                        if (tokens.length != 5) {
	                            continue;
	                        }
	                        if (tokens[0].startsWith("part-")) {
	                            tokens[0] =  tokens[0].substring("part-00000:".length());
	                        }
	                        int n12 = Integer.parseInt(tokens[0]);
	                        String term1 = tokens[1];
	                        if (ignoreCase) {
	                            term1 =  term1.toLowerCase();
	                        }
	                    
	                        String term2 = tokens[3];
	                        
	                        if (ignoreCase) {
	                            term1 = term1.toLowerCase();
	                            term2 = term2.toLowerCase();
	                        } 
	                        //int n2 = Integer.parseInt(tokens[4]);
	                        if (n12 >= minCorrelation) {
	                            //double corr1 = ((double)n12)/((double)n1);
	                            CorrelatedEntity ce1 = new CorrelatedEntity(term1,n12);
	                    
	                            //double corr2 = ((double)n12)/((double)n2);
	                            CorrelatedEntity  ce2 = new CorrelatedEntity(term2, n12);
	                    
	                            List<CorrelatedEntity> term1List = graph.get(term1);
	                            if (term1List == null) {
	                                term1List = new ArrayList<CorrelatedEntity>();
	                                graph.put(term1, term1List);
	                            }
	
	                            if (!term1List.contains(ce2)) {
	                                term1List.add(ce2);
	                            }
	                    
	                            List<CorrelatedEntity> term2List = graph.get(term2);
	                            if (term2List == null) {
	                                term2List = new ArrayList<CorrelatedEntity>();
	                                graph.put(term2, term2List);
	                            }
	                        
	                            if(!term2List.contains(ce1)) {
	                                term2List.add(ce1);
	                            }
	                        }
	                    } catch(Exception e) {
	                        LOGGER.error("Could not read graph file "+ file.getName());
	                        LOGGER.error("Line: "+ line);
	                        continue;
	                    }
	                }while (line != null);
	                reader.close();
	            } catch (IOException e) {
	                LOGGER.error("Could not read graph file "+ file.getName());
	                continue;
	            }
	        } catch (FileNotFoundException e) {
	            LOGGER.error("Graph file "+ file.getName() + " was not found.");
	        }
	    }
	    
	    for(List<CorrelatedEntity> ce:graph.values()) {
	        Collections.sort(ce, Collections.reverseOrder());
	    }
	    
	    LOGGER.info("Loaded " + nPairs*2 + " correlated entities " + graphFilesPath);
	    return graph;
	}

}
