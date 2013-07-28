package sybyla.app;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import sybyla.graph.neo4j.Neo4jGraphEngine;
import sybyla.graph.reader.GraphFileReader;

public class Neo4jLoader {
	private static final Logger LOGGER = Logger.getLogger(Neo4jLoader.class);
	
	public static final String PATH="in";
	public static final String BATCH="batch";
	public static final String DB_PATH="out";
	
	private static String graphFilesPath;
	private static String dbPath;
	private static boolean batchMode=false;
	
    public static void main(String[] args){
    	parseOptions(args);

    	if (graphFilesPath == null){
    		LOGGER.error("No graph files path specified.");
    		usage();
    		return;
    	}
    	try {
    		LOGGER.info("Running loader reading graph files from " + graphFilesPath);
			if (batchMode){
				runBatch(graphFilesPath, dbPath);
			} else {
				run(graphFilesPath,dbPath);
			}
		} catch (IOException e) {
			LOGGER.error("error initializing Neo4j loader");
		}
    	
    }
    
    private static void parseOptions(String[] args){
    	
    	// create the parser
        CommandLineParser parser = new GnuParser();
        Options options = createOptions();
        try {
            // parse the command line arguments
            CommandLine line = parser.parse( options, args );
            
            if( line.hasOption( PATH ) ) {
            	graphFilesPath = line.getOptionValue( PATH );
            }
            if( line.hasOption( DB_PATH ) ) {
            	dbPath = line.getOptionValue( DB_PATH );
            }
            if (line.hasOption(BATCH)){
            	batchMode =  true;
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
    	formatter.printHelp( "java -cp <jar file> sybyla.app.Neo4jLoader", options );
    }
    
    public static void run(String graphFilesPath, String dbPath) throws IOException{
    	Neo4jGraphEngine.init(dbPath);
    	boolean batch = false;
    	GraphFileReader.load(graphFilesPath, Neo4jGraphEngine.getNeo4jGraphEngine(),batch);
    }
    
    public static void runBatch(String graphFilesPath, String dbPath) throws IOException{
    	Neo4jGraphEngine.initBatchMode(dbPath, null);
    	boolean batch = true;
    	GraphFileReader.load(graphFilesPath, Neo4jGraphEngine.getNeo4jGraphEngine(), batch);
    	Neo4jGraphEngine.commitBatchInsert();
    }
    
    private static Options createOptions(){
    	
    	OptionBuilder.withArgName( "file" );
    	OptionBuilder.hasArg();
    	OptionBuilder.withDescription( "path where graph files reside" );
    	
    	Option graphFilesPath = OptionBuilder.create( PATH );
    	
    	OptionBuilder.withArgName( "file" );
    	OptionBuilder.hasArg();
    	OptionBuilder.withDescription( "path where Neo4j database will be created" );
    	
    	Option dbPath = OptionBuilder.create( DB_PATH );
                							
    	Option batchMode =  new Option(BATCH, "run loader in batch mode");
    	
    	Options options = new Options();
    	
    	options.addOption(graphFilesPath);
    	options.addOption(dbPath);
    	options.addOption(batchMode);
    	
    	return options;
    }
}
