package sybyla.db;

import static org.junit.Assert.*;

import org.junit.Test;

public class ConnectionPoolTest {

	@Test
	public void test() throws Exception {
		ConnectionPool pool = new ConnectionPool();
		assertNotNull(pool);
	}

}
