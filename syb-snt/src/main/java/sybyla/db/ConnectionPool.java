package sybyla.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDriver;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.log4j.Logger;

//
// Here are the dbcp-specific classes.
// Note that they are only used in the setupDriver
// method. In normal use, your classes interact
// only with the standard JDBC API
//


public class ConnectionPool {
	
	public static final String ACTIVE_CONNECTIONS="Active DB Connections";
	public static final String IDLE_CONNECTIONS="Idle DB Connections";
	
	private static final String DEFAULT_DRIVER_CLASS="com.mysql.jdbc.Driver";
	private static final String CONNECTION_PREFIX="jdbc:apache:commons:dbcp:";
	
	private static Logger LOGGER= Logger.getLogger(ConnectionPool.class);
	private static final String DB_URL="db";
	
	private String uri;
	private String name="sentiment_db";
	private static Properties props;
	
	public ConnectionPool() throws Exception{
		super();
		String db = System.getProperty(DB_URL);
		if (db ==  null){
			Properties properties =  config();
			db = properties.getProperty(DB_URL);
		}
		if (db == null){
			LOGGER.error("No database configuration found");
			return;
		}
		uri=db;
		setup();
	}
	
	public ConnectionPool(String uri, String name) throws Exception{
		this.uri=uri;
		this.name=name;
		setup();
	}
	
	public static Properties config() {
		InputStream in =  ConnectionPool.class.getResourceAsStream("/config.properties");
    	Properties properties = new Properties();
			
    	try {
			properties.load(in);
		} catch (IOException e) {
			LOGGER.error("Could not load configuration file config.properties:",e);
		}
    	return properties;
	}
	
	public List<String[]> executeQuery(String query) {
		Connection conn = null;
	    Statement stmt = null;
	    ResultSet rset = null;
	    List<String[]> results = new ArrayList<String[]>();

	    try {
	        conn = DriverManager.getConnection(CONNECTION_PREFIX+name);
	        stmt = conn.createStatement();
	           	            
	        rset = stmt.executeQuery(query);
	        int numcols = rset.getMetaData().getColumnCount();
	        while(rset.next()) {
	        	String[] s= new String[numcols];
	        	for(int i=1;i<=numcols;i++) {
	        		s[i-1] =  rset.getString(i);
	             }
	             results.add(s);
	        }
	    } catch(SQLException e) {
	    	LOGGER.error("error running query "+query,e);
	    } finally {
	    	try { if (rset != null) rset.close(); } catch(Exception e) {LOGGER.error(e); }
	        try { if (stmt != null) stmt.close(); } catch(Exception e) {LOGGER.error(e); }
	        try { if (conn != null) conn.close(); } catch(Exception e) {LOGGER.error(e);}
	    }
	    
	    return results;
	}
	
	public int  executeUpdate(String query) {
		Connection conn = null;
	    Statement stmt = null;
	    int rowCount = 0;
	    try {
	    	
	        conn = DriverManager.getConnection(CONNECTION_PREFIX+name);
	        conn.setAutoCommit(false);
	        stmt = conn.createStatement();
	        rowCount = stmt.executeUpdate(query);
	        conn.commit();
	        
	    } catch(SQLException e) {
	    	try {
	    		if (conn != null) {
					conn.rollback();
	    		}
			} catch (SQLException e1) {
				LOGGER.error("Error rolling back query \n"+ query, e1);
			}
	    	LOGGER.error("Error running query \n"+ query, e);
	    } finally {
	        try { if (stmt != null) stmt.close(); } catch(Exception e) {LOGGER.error(e); }
	        try { if (conn != null) {conn.setAutoCommit(true); conn.close();} } catch(Exception e) {LOGGER.error(e);}
	    }
	    
	    return rowCount;
	}
	
	public Set<Long>  executeInsert(String query) {
		Connection conn = null;
	    Statement stmt = null;
	    int rowCount = 0;
	    Set<Long> ids =  new HashSet<Long>();
	    try {
	    	
	        conn = DriverManager.getConnection(CONNECTION_PREFIX+name);
	        conn.setAutoCommit(false);
	        stmt = conn.createStatement();
	        rowCount = stmt.executeUpdate(query,Statement.RETURN_GENERATED_KEYS);
	        ResultSet keys = stmt.getGeneratedKeys();
	        conn.commit();
	        if (keys != null) {
	        	while (keys.next()) {
	        		Long id =  keys.getLong(1);
	        		ids.add(id);
	        	}
	        }
	        
	    } catch(SQLException e) {
	    	try {
	    		if (conn != null) {
					conn.rollback();
	    		}
			} catch (SQLException e1) {
				LOGGER.error("Error rolling back query \n"+ query, e1);
			}
	    	LOGGER.error("Error running query \n"+ query, e);
	    } finally {
	        try { if (stmt != null) stmt.close(); } catch(Exception e) {LOGGER.error(e); }
	        try { if (conn != null) {conn.setAutoCommit(true); conn.close();} } catch(Exception e) {LOGGER.error(e);}
	    }
	    
	    return ids;
	}
	
	public Connection getConnection() {
		Connection conn = null;
	   
	    try {
	        conn = DriverManager.getConnection(CONNECTION_PREFIX+name);      
	        
	    } catch(SQLException e) {
	    	LOGGER.error(e);
	    } 
	    
	    return conn;
	}
	
	

    public void setup() throws Exception {
    	
    	 try {
             Class.forName(DEFAULT_DRIVER_CLASS);
         } catch (ClassNotFoundException e) {
             LOGGER.error(e);
         }
        //
        // First, we'll need a ObjectPool that serves as the
        // actual pool of connections.
        //
        // We'll use a GenericObjectPool instance, although
        // any ObjectPool implementation will suffice.
        //
    	
    	GenericObjectPool genericPool =  new GenericObjectPool(null);
    	genericPool.setTestWhileIdle(true);
    	genericPool.setTimeBetweenEvictionRunsMillis(50000);
    	genericPool.setMaxActive(4);
    	genericPool.setMinIdle(1);
    	genericPool.setMaxWait(60000l);
        ObjectPool connectionPool = genericPool;
        

        //
        // Next, we'll create a ConnectionFactory that the
        // pool will use to create Connections.
        // We'll use the DriverManagerConnectionFactory,
        // using the connect string passed in the command line
        // arguments.
        //
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(uri,props);

        //
        // Now we'll create the PoolableConnectionFactory, which wraps
        // the "real" Connections created by the ConnectionFactory with
        // the classes that implement the pooling functionality.
        //
		PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory,connectionPool,null,"SELECT 1",false,true);
        poolableConnectionFactory.setValidationQuery("SELECT 1");

        //
        // Finally, we create the PoolingDriver itself...
        //
        Class.forName("org.apache.commons.dbcp.PoolingDriver");
        PoolingDriver driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");
        
        
        
        //
        // ...and register our pool with it.
        //
        driver.registerPool(name,connectionPool);
        
        

        //
        // Now we can just use the connect string "jdbc:apache:commons:dbcp:example"
        // to access our pool of Connections.
        //
    }

    public static Map<String,Integer> getStats(String name) throws SQLException  {
        PoolingDriver driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");
        ObjectPool connectionPool = driver.getConnectionPool(name);
        Map<String,Integer> stats = new HashMap<String,Integer>();
        stats.put(ACTIVE_CONNECTIONS, connectionPool.getNumActive());
        stats.put(IDLE_CONNECTIONS, connectionPool.getNumIdle());
        
        return stats;
    }

    public void shutdown() throws Exception {
        PoolingDriver driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");
        driver.closePool(name);
        
    }
}