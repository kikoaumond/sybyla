package sybyla.db;

import java.util.Map;
import java.util.Set;

public class DBEngine {
	private static ConnectionPool pool;
	
	public static void init() throws Exception {
		if (pool == null){
			pool  =  new ConnectionPool();
		}
	}
	public static Set<Long> insertExample(){
		String query =  "INSERT INTO sybylaco_sentiment.sentiment_data \n" +
				"(customer_key, sentiment,text,context) \n" +
				"VALUES \n" +
				"(\"test\", \"-1\", \"não gostei do filme\",\"não gostei\")";
		return pool.executeInsert(query);
	}
}
