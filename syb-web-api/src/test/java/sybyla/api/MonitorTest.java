package sybyla.api;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

public class MonitorTest {

	@Test
	public void testDiskUsage() throws IOException, InterruptedException {
		String result = Monitor.checkDiskUsage();
		assertTrue(result.contains("Filesystem"));
		System.out.println(result);
	}
	
	@Test
	public void testHost(){
		String result =  Monitor.getHost();
		System.out.println(result);
		assertTrue(result.contains("Canonical Host Name"));
	}
	
	@Test
	public void testProcessId() throws IOException, InterruptedException {
		String result = Monitor.getProcessId();
		assertTrue(result.contains("@"));
		System.out.println(result);
	}
	
	@Test
	public void testCPUUsage() throws IOException, InterruptedException {
		String result = Monitor.getCPUUsage();
		assertTrue(result.contains("%CPU"));
		System.out.println(result);
	}
}
