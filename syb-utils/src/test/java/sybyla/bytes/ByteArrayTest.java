package sybyla.bytes;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class ByteArrayTest {

	@Test
	public void testConstructor() throws UnsupportedEncodingException {
		String s =  "this is a test";
		ByteArray b = new ByteArray(s);
		byte[] ba =  b.getByteArray();
		byte[] expected = s.getBytes("UTF-8");
		assertTrue(Arrays.equals(expected,ba));
		
		int hashCode = b.hashCode();
		int expectedHashCode = Arrays.hashCode(ba);
		assertTrue(hashCode==expectedHashCode);
		
	}
	
	@Test ( expected = java.lang.NullPointerException.class )
	public void testConstructorNullArgument() throws UnsupportedEncodingException {
		String s =  null;
		@SuppressWarnings("unused")
		ByteArray b = new ByteArray(s);
	}
	
	@Test
	public void testTwoEqualObjects() throws UnsupportedEncodingException {
		String s =  "this is a test";
		String s2 = "this is a test";
		
		ByteArray b = new ByteArray(s);
		ByteArray b2 = new ByteArray(s2);
		
		byte[] ba =  b.getByteArray();
		byte[] ba2 =  b2.getByteArray();
		byte[] expected = s.getBytes("UTF-8");
		
		assertTrue(Arrays.equals(expected, ba));
		assertTrue(Arrays.equals(expected, ba2));
		assertTrue(Arrays.equals(ba, ba2));

		assertTrue(b.compareTo(b2)==0);
		
		int hashCode = b.hashCode();
		int hashCode2 = b2.hashCode();
		int expectedHashCode = Arrays.hashCode(ba);
		assertTrue(hashCode==expectedHashCode);
		assertTrue(hashCode2==expectedHashCode);
		assertTrue(hashCode==hashCode2);

	}
	
	@Test
	public void testHashSetEqualsObjects() throws UnsupportedEncodingException {
		String s =  "this is a test";
		String s2 = "this is a test";
		
		ByteArray b = new ByteArray(s);
		ByteArray b2 = new ByteArray(s2);
		
		Set<ByteArray> set = new HashSet<ByteArray>();
		set.add(b);
		boolean contains = set.contains(b2);
		assertTrue(contains);
	}
	
	@Test
	public void testTwoDifferentObjects() throws UnsupportedEncodingException {
		String s =  "this is a test";
		String s2 = "this is a test too";
		
		ByteArray b = new ByteArray(s);
		ByteArray b2 = new ByteArray(s2);
		
		byte[] ba =  b.getByteArray();
		byte[] ba2 =  b2.getByteArray();
		
		
		assertFalse(Arrays.equals(ba, ba2));

		assertTrue(b.compareTo(b2) < 0);
		
		int hashCode = b.hashCode();
		int hashCode2 = b2.hashCode();

		assertFalse(hashCode==hashCode2);

	}
	
	@Test
	public void testArray() throws UnsupportedEncodingException {
		
		String s =  "this is a test";
		String s2 = "this is a test too";
		String s3 = "this is a test three";
		
		ByteArray b = new ByteArray(s);
		ByteArray b2 = new ByteArray(s2);
		ByteArray b3 = new ByteArray(s3);
		
		ByteArray[] array = new ByteArray[10];
		
		array[0]=b;
		array[1]=b2;
		array[2]=b3;
		
		ByteArray[] trimmedArray = Arrays.copyOf(array, 3);

		Arrays.sort(trimmedArray);
		
		assertTrue(trimmedArray.length==3);
		assertTrue(trimmedArray[0].equals(b));
		assertTrue(trimmedArray[1].equals(b3));
		assertTrue(trimmedArray[2].equals(b2));
		
		ByteArray bCopy = new ByteArray("this is a test");
		int bIndex = Arrays.binarySearch(trimmedArray, bCopy);
		assertTrue(bIndex == 0);
		
		ByteArray b2Copy = new ByteArray("this is a test too");
		int b2Index = Arrays.binarySearch(trimmedArray, b2Copy);
		assertTrue(b2Index == 2);
		
		ByteArray b3Copy = new ByteArray("this is a test three");
		int b3Index = Arrays.binarySearch(trimmedArray, b3Copy);
		assertTrue(b3Index == 1);
	}
	
	//@Test
	public void LongRunningMemoryTest() throws UnsupportedEncodingException{
		int size = 50000000;
		String s = "this is a string of average size ";
		showMemory();
		ByteArray[] array = new ByteArray[size];
		showMemory();
		int n=0;
		for(int i=0; i<size; i++){
			try{
				array[i] =  new ByteArray(s+i);
				n++;
				if(n%1000000==0){
					System.out.print(n+ "->");
					showMemory();
				}
			} catch(Throwable t){
				System.out.println(t);
				System.out.println(n);
				showMemory();
			}
		}
		try{
			Arrays.sort(array);
		} catch(Throwable t){
			System.out.println(t);
			System.out.println(n);
			showMemory();
		}
		showMemory();
		
		ByteArray bCopy = new ByteArray("this is a test three 499999");
		Long begin = new Date().getTime();
		int index = Arrays.binarySearch(array, bCopy);
		Long end = new Date().getTime();
		Long t =  end- begin;
		System.out.println("Binary search for "+array.length+" elements completed in "+t+ " ms");
		assertTrue(index == 499998);
	}
	
	public void showMemory(){

		Runtime runtime = Runtime.getRuntime();
		
		long maxMemory = runtime.maxMemory();
		long totalMemory = runtime.totalMemory();
		long freeMemory = runtime.freeMemory();
		long usedMemory = totalMemory - freeMemory;
		
		System.out.println("Used memory "+totalMemory);
		//System.out.println("Allocated memory "+totalMemory);
		//System.out.println("Free memory "+freeMemory);
		//System.out.println("Max memory "+maxMemory);




	}
	
}
