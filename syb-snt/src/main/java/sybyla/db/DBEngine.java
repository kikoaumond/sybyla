package sybyla.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

public class DBEngine {
	private static ConnectionPool pool;
	private static Logger LOGGER =  Logger.getLogger(DBEngine.class);
	
	public static void init() throws Exception {
		if (pool == null){
			pool  =  new ConnectionPool();
		}
	}
	
	public static void insertExample(String customerKey, String text, int sentiment, String context){
		
		if (customerKey == null || customerKey.trim().length() == 0) customerKey = "";
		if (text == null || text.trim().length()==0) return;
		if (sentiment != -1 && sentiment !=0 && sentiment != 1) return;
		if (context == null || context.trim().length()==0) context = "";

		String query = "INSERT INTO sybylaco_sentiment.sentiment_data \n" +
				"(customer_key, sentiment,text,context) \n" +
				"VALUES \n" +
				"(?, ?, ?,?)";
		
		Connection connection = pool.getConnection();
		try {
			PreparedStatement ps = connection.prepareStatement(query);
			ps.setString(1, customerKey);
			ps.setInt(2, sentiment);
			ps.setString(3, text);
			ps.setString(4, context);
			ps.executeUpdate();			
		} catch (SQLException e) {
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				LOGGER.error("Error closing connection",e);
			}
		}
	}
	/*
	public static Set<Long> insertExample(){
		String query =  "INSERT INTO sybylaco_sentiment.sentiment_data \n" +
				"(customer_key, sentiment,text,context) \n" +
				"VALUES \n" +
				"(\"test\", \"-1\", \"não gostei do filme\",\"não gostei\")";
		return pool.executeInsert(query);
	}
	*/
}
