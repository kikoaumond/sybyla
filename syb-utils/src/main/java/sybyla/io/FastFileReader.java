package sybyla.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

public class FastFileReader {

	private FileInputStream fin;
	private FileChannel fc;
	private ByteBuffer buffer;
	private CharBuffer charBuffer;
	private int bufferSize = 1024;
	public Charset charset = Charset.forName("UTF-8");
	public CharsetDecoder decoder = charset.newDecoder();
	private StringBuilder line;
	
	private char lineSeparator = '\n';
	
	
	public FastFileReader(String file) throws IOException{
		
		fin = new FileInputStream(file );
		fc = fin.getChannel();
		buffer = ByteBuffer.allocate( bufferSize );		
		line = new StringBuilder();
	    String encoding = System.getProperty("file.encoding");
	    if (encoding !=null){
	    	charset = Charset.forName(encoding);
	    }
	   String newline = System.getProperty("line.separator");
	   if (newline!=null){
		   char[] c = newline.toCharArray();
		   lineSeparator=c[0];
	   }
	}
	
	public FastFileReader(File file) throws IOException{
		
		fin = new FileInputStream( file );
		fc = fin.getChannel();
		buffer = ByteBuffer.allocate( bufferSize );		
		line = new StringBuilder();
	    String encoding = System.getProperty("file.encoding");
	    if (encoding !=null){
	    	charset = Charset.forName(encoding);
	    }
	   String newline = System.getProperty("line.separator");
	   if (newline!=null){
		   char[] c = newline.toCharArray();
		   lineSeparator=c[0];
	   }
	}
		
	
	public int readBuffer() throws IOException{

		buffer.clear();
		if (charBuffer !=null){
			charBuffer.clear();
		}
		int nBytes = fc.read(buffer);
		buffer.flip();
		charBuffer = charset.decode(buffer);
		return nBytes;
	}
	
	public String readLine() throws IOException{

		boolean endOfLine = false;
		int nBytes=0;
		
		if (charBuffer == null || (charBuffer.position() == charBuffer.limit())){
			nBytes = readBuffer();
			if (nBytes<0){
				if (line.length()>0){
					String s = line.toString();
					line.delete(0, line.length());
					return s;
				}
			}
		}
		
		
		while(charBuffer.position() != charBuffer.limit()) {
			char c = charBuffer.get();
			
			if (c == lineSeparator){
				
				endOfLine=true;
				break;
			}
			
			line.append(c);
		}
		if (endOfLine) {
			if (line.length()==0){
				if (nBytes<0){
					return null;
				}else {
					return "";
				}
			} else {
				String s = line.toString();
				line.delete(0, line.length());
				return s;
			}
		}
		if(nBytes>=0){
			return readLine();
		}
		return null;
	}
	public void close() throws IOException{
		fc.close();
		buffer=null;
		charBuffer=null;
	}
	protected void finalize() throws Throwable {
		buffer = null;
	}
}
