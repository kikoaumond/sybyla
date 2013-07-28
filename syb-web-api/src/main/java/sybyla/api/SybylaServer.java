package sybyla.api;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;

import sybyla.graph.neo4j.Neo4jGraphEngine;

public class SybylaServer {
	
	private static final Logger LOGGER = Logger.getLogger(SybylaServer.class);
	private static Server server;
	public static final String PORT="port";
	
	public static final int DEFAULT_PORT=80;
	private static int port=DEFAULT_PORT;
	
	private static boolean graph;
	private static boolean category;
	private static boolean tag;
	private static boolean why;
	private static boolean sentiment;
	private static boolean ey;


	private static String neo4jHome;
	
	public static void main(String[] args) {
		parseOptions(args);
		start();
	}
	
	public static void start(){
		start(false);
	}
	
	public static void start(boolean test) {
		try{
			logEncoding();
	        server = new Server(port);
	        SelectChannelConnector connector = new SelectChannelConnector();
	        connector.setPort(port);
	        connector.setRequestHeaderSize(16384);
	        server.setConnectors(new Connector[]{connector});
	        
	        if (graph){
	        	if (neo4jHome !=null){
	        		Neo4jGraphEngine.setDBPath(neo4jHome);
	        	}
	        	SybylaHandler.setGraphEnabled(true);
	        }
	        
	        if (category){
	        	SybylaHandler.setClusterEnabled(true);
	        }
	        if (tag){
	        	SybylaHandler.setTagEnabled(true);
	        }
	        if (why){
	        	SybylaHandler.setWhyEnabled(true);
	        }
	        if (sentiment){
	        	SybylaHandler.setSentimentEnabled(true);
	        }
	        if (ey){
	        	SybylaHandler.setEYEnabled(true);
	        }
	        
	        String staticDir = SybylaServer.class.getClassLoader().getResource("static").toExternalForm();

	        ResourceHandler resourceHandler = new ResourceHandler();
	        resourceHandler.setDirectoriesListed(false);
	        resourceHandler.setResourceBase(staticDir);
	        resourceHandler.setWelcomeFiles(new String[]{ "index.html"});
	        
	        ContextHandler staticContext = new ContextHandler();
	        staticContext.setContextPath("/s");
	        staticContext.setClassLoader(Thread.currentThread().getContextClassLoader());
	        staticContext.setHandler(resourceHandler);
	        
	        ContextHandler sybylaContext = new ContextHandler();
	        sybylaContext.setContextPath("/");
	        sybylaContext.setClassLoader(Thread.currentThread().getContextClassLoader());
	        sybylaContext.setHandler(new SybylaHandler());
	        
	        ContextHandlerCollection handlers = new ContextHandlerCollection();
	        handlers.addHandler(sybylaContext);
	        handlers.addHandler(staticContext);
	        server.setHandler(handlers);
	        
	        server.start();
	        if(!test){
	        	server.join();
	        }
		} catch(Throwable t){
			LOGGER.error("Error starting SybylaServer",t);
			System.exit(1);
		}
    }
	
	
	public static void stop(){
		if (server != null){
			try {
				server.stop();
			} catch (Exception e) {
				LOGGER.error("Error stopping SybylaServer",e);
				e.printStackTrace();
			}
		}
	}
	
	@SuppressWarnings("static-access")
	private static Options createOptions(){
	    	
	    Option port  = OptionBuilder.withArgName( "port number" ).
	    			hasArg().
	    			withDescription( "port where web app will be listening" ).
	    			create(PORT);
	    
	    Option neo4jHome  = OptionBuilder.withArgName( "Neo4j Graph DB root directory" ).
    			hasArg().
    			withDescription( "root directory of Neo4j Graph DB" ).
    			create(Constants.NEO4J_HOME);
	    	
	    Option graph = new Option( Constants.GRAPH_APP, "start graph app" );
	    Option cluster = new Option( Constants.CATEGORY_APP, "start cluster app" );
	    Option tag = new Option( Constants.TAG_APP, "start tag app" );
	    Option why = new Option( Constants.WHY_APP, "start why app" );
	    Option sentiment = new Option( Constants.SENTIMENT_APP, "start sentiment app" );
	    Option ey = new Option( Constants.EY_APP, "start ey app" );


	    	
	    Options options = new Options();
	    	
	    options.addOption(port);
	    options.addOption(graph);
	    options.addOption(cluster);
	    options.addOption(tag);
	    options.addOption(why);
	    options.addOption(sentiment);
	    options.addOption(ey);

	    options.addOption(neo4jHome);
 	
	    return options;
	}
    
    private static void parseOptions(String[] args){
    	
    	// create the parser
        CommandLineParser parser = new GnuParser();
        Options options = createOptions();
        try {
            // parse the command line arguments
            CommandLine line = parser.parse( options, args );
            
            if( line.hasOption( Constants.PORT ) ) {
            	port = Integer.parseInt(line.getOptionValue( PORT ));
            } else {
            	String p = System.getProperty(PORT);
            	if (p!=null){
                	port = Integer.parseInt(p);

            	}
            }
            LOGGER.info("Listening on port "+port);
            if( line.hasOption( Constants.GRAPH_APP ) ) {
            	graph = true;
            	LOGGER.info("Graph App enabled");
                if( line.hasOption( Constants.NEO4J_HOME ) ) {
                	neo4jHome = line.getOptionValue( Constants.NEO4J_HOME );
                }
                LOGGER.info("Neo4j home is at: "+neo4jHome);
            }
            if( line.hasOption( Constants.CATEGORY_APP ) ) {
            	category = true;
            	LOGGER.info("Category App enabled");
            }
            if( line.hasOption( Constants.TAG_APP ) ) {
            	tag = true;
            	LOGGER.info("Tag App enabled");
            }
            if( line.hasOption( Constants.WHY_APP ) ) {
            	why = true;
            	LOGGER.info("Why App Enabled");
            }
            if( line.hasOption( Constants.SENTIMENT_APP ) ) {
            	sentiment = true;
            	LOGGER.info("Sentiment App Enabled");
            }
            if( line.hasOption( Constants.EY_APP ) ) {
            	ey = true;
            	LOGGER.info("EY App Enabled");
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
    	formatter.printHelp( "java -cp <jar file> sybyla.api.SybylaServer", options );
    }

	public static void setServer(Server server) {
		SybylaServer.server = server;
	}

	public static void setGraph(boolean graph) {
		SybylaServer.graph = graph;
	}

	public static void setTag(boolean tag) {
		SybylaServer.tag = tag;
	}
	
	public static void setCategory(boolean category) {
		SybylaServer.category = category;
	}
	
	public static void setWhy(boolean why) {
		SybylaServer.why = why;
	}
	
	public static void setEY(boolean ey) {
		SybylaServer.ey = ey;
	}
	
	public static void setSentiment(boolean sentiment) {
		SybylaServer.sentiment = sentiment;
	}

	public static void setPort(int port) {
		SybylaServer.port = port;
	}

	public static void setNeo4jHome(String neo4jHome) {
		SybylaServer.neo4jHome = neo4jHome;
	}

	private static void logEncoding(){
		String defaultCharacterEncoding = System.getProperty("file.encoding");
        LOGGER.info("defaultCharacterEncoding by property: " + defaultCharacterEncoding);
        LOGGER.info("defaultCharacterEncoding by code: " + getDefaultCharEncoding());
        LOGGER.info("defaultCharacterEncoding by charSet: " + Charset.defaultCharset());
      
	}
	
	private static String getDefaultCharEncoding(){
        byte [] bArray = {'w'};
        InputStream is = new ByteArrayInputStream(bArray);
        InputStreamReader reader = new InputStreamReader(is);
        String defaultCharacterEncoding = reader.getEncoding();
        return defaultCharacterEncoding;
    }

}
