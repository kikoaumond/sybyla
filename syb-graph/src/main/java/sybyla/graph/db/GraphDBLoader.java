package sybyla.graph.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;


public class GraphDBLoader {
	private static final Logger LOGGER = Logger.getLogger(GraphDBLoader.class);

    
    private static final String GRAPH_DATABASE="graph";
    private static final String GRAPH_SCHEMA="graph";
    private static final String GRAPH_USER="graph";
    private static final String GRAPH_PASSWORD="bund4l3l3";
    private static final String GRAPH_HOST="127.0.0.1";
    private static final int GRAPH_PORT=5432;

    private static final String INSERT_SQL="INSERT INTO"+GRAPH_SCHEMA+".graph (term, related, c, t, r) VALUES (?,?,?,?,?);";

    private static final Connection connection = connect();
   
	private static PreparedStatement ps;
	
	public static final String GRAPH_FILES_PATH="in";
	private static String graphFilesPath;
    

    public static void main(String[] args){
    	parseOptions(args);
    	if (graphFilesPath == null){
    		LOGGER.error("No graph files path specified.");
    		usage();
    		return;
    	}
    	prepareStatement();
    	loadGraph(graphFilesPath);
    }
    
    
	private static void loadGraph(String graphFilesPath) {
	    
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
	        return;
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
	                        if (nPairs%100000==0){
	                        	LOGGER.info("Loaded "+nPairs*2 +" into database");
	                        }
	                        String[] tokens = line.split("\t");
	                        if (tokens.length != 5) {
	                        	LOGGER.error("Malformed line in file:\n"+line);
	                            continue;
	                        }
	                        if (tokens[0].startsWith("part-")) {
	                            tokens[0] =  tokens[0].substring("part-00000:".length());
	                        }
	                        
	                        int c = Integer.parseInt(tokens[0]);
	                        String term = tokens[1];
	                        int t = Integer.parseInt(tokens[2]);
	                        String related = tokens[3];
	                        int r = Integer.parseInt(tokens[4]);
	                        insert(term, related, c, t, r);
	                        insert(related, term, c, r, t);

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
	     
	    LOGGER.info("Loaded " + nPairs*2 + " correlated entities " + graphFilesPath);
	}
	
	private static void insert(String term, String related, int c, int t, int r){
		try {
			ps.setString(1, term);
			ps.setString(2, related);
			ps.setInt(3, c);
			ps.setInt(4, t);
			ps.setInt(5, r);
			ps.executeUpdate();
		} catch (SQLException e) {
			LOGGER.error("Error inserting record",e);
		}	
	}
	
	private static void prepareStatement(){
		try {
			ps = connection.prepareStatement(INSERT_SQL);
		} catch (SQLException e) {
			LOGGER.error("Error preparing statement",e);
		}
	}
	
	private static Connection connect(){
			
		Connection connection = null;
		try {
				
			Class.forName("org.postgresql.Driver");
	 
		} catch (ClassNotFoundException e) {
			LOGGER.error("PostgreSQL drive not found");
			return connection;
	 
		}
	 	 	
		try {
	 
			connection = DriverManager.getConnection(
						"jdbc:postgresql://"+GRAPH_HOST+":"+GRAPH_PORT+"/"+GRAPH_DATABASE, 
						GRAPH_USER,
						GRAPH_PASSWORD);
	 
		} catch (SQLException e) {
			LOGGER.error("Error connecting to database",e);
		}
		
		return connection;
	}
	
    private static Options createOptions(){
    	
    	OptionBuilder.withArgName( "file" );
    	OptionBuilder.hasArg();
    	OptionBuilder.withDescription( "path where graph files reside" );
    	
    	Option graphFilesPath = OptionBuilder.create( GRAPH_FILES_PATH );
    	
    	Options options = new Options();
    	
    	options.addOption(graphFilesPath);    	
    	return options;
    }
    
    private static void parseOptions(String[] args){
    	
    	// create the parser
        CommandLineParser parser = new GnuParser();
        Options options = createOptions();
        try {
            // parse the command line arguments
            CommandLine line = parser.parse( options, args );
            
            if( line.hasOption( GRAPH_FILES_PATH ) ) {
            	graphFilesPath = line.getOptionValue( GRAPH_FILES_PATH );
            }
            
        }
        catch( ParseException e ) {
            // oops, something went wrong
            LOGGER.error( "Parsing command line options failed." ,e );
            usage();
        }
    }
    
    private static void usage(){
    	Options options = createOptions();
    	HelpFormatter formatter = new HelpFormatter();
    	formatter.printHelp( "java -cp <jar file> sybyla.graph.db.GraphDBLoader", options );
    }

}
