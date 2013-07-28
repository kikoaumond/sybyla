package sybyla.bytes;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import org.apache.log4j.Logger;


public  class ByteArray implements Comparable<ByteArray> {
    private static final Logger LOGGER = Logger.getLogger(ByteArray.class);
	private final byte[] byteArray;    
    private final int hashCode;
    
    public ByteArray(String s) throws UnsupportedEncodingException{
    	
    	byteArray = s.getBytes("UTF-8");
		hashCode =Arrays.hashCode(byteArray);
		
    }
     
    @Override
    public boolean equals(Object o) {
    	
        if(!(o instanceof ByteArray)){
        	return false;
        }
        
        ByteArray b = (ByteArray) o;
        
        return Arrays.equals(this.byteArray, b.byteArray); 
    }
    
    public byte[] getByteArray(){
    	return byteArray;
    }
    
    @Override
    public int hashCode(){
    	return hashCode;
    }

    @Override
    public int compareTo(ByteArray ba) {
        for (int i = 0, j = 0; i < this.byteArray.length && j < ba.byteArray.length; i++, j++) {
        	int a = (this.byteArray[i] & 0xff);
        	int b = (ba.byteArray[j] & 0xff);
        	if (a != b) {
        		return a - b;
        	}
        }
        return this.byteArray.length - ba.byteArray.length;
    }
    
    @Override
    public String toString(){
    	try {
			return new String(byteArray,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("Error converting ByteArray to String",e);
			return null;
		}
    }
}
