package sybyla.db;

import static org.junit.Assert.*;

import org.junit.Test;

public class DBEngineTest {

	@Test
	public void test() throws Exception {
		DBEngine.init();
		DBEngine.insertExample();
	}

}
