package sybyla.db;

import static org.junit.Assert.*;

import org.junit.Test;

public class DBEngineTest {
	/*
	@Test
	public void test() throws Exception {
		DBEngine.init();
		DBEngine.insertExample();
	}
	*/
	
	@Test
	public void test2() throws Exception {
		DBEngine.init();
		String customerKey="customerKey";
		String text="Não gostei do filme";
		int sentiment=-1;
		String context="Não gostei";
		DBEngine.insertExample(customerKey, text, sentiment, context);
	}

}
