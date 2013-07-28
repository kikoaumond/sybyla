package sybyla.io;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class FileReaderTest {

	@Test
	public void test() throws IOException {
		FastFileReader fileReader = new FastFileReader(new File("src/test/resources/test.txt"));

		String line=null;
		String[] lines = new String[6];
		int i=0;
		do{	
			line = fileReader.readLine();
			if (line==null){
				break;
			}
			lines[i]=line;
			i++;
			System.out.println(line);
		}while (line!=null);
		for(i=0;i<5;i++){
			assertNotNull(lines[i]);
			assertTrue(lines[i].endsWith("end"));
		}
		assertEquals("1 oioajfds dfoksdo wfkwdfjwp end",lines[0]);
		assertEquals("2 iknvksdpi dpfsdpfk pdfskdmf end",lines[1]);
		assertEquals("3 sdsd end",lines[2]);
		assertEquals("4 sdfsdfn end",lines[3]);
		assertEquals("5 dlfksmdf end",lines[4]);
		assertEquals("6 lsdflsmf end",lines[5]);				
	}

	@Test
	public void test2() throws IOException {
		FastFileReader fileReader = new FastFileReader(new File("src/test/resources/FileReaderTestFile.txt"));
	
		String line=null;
		String[] lines = new String[20];
		int i=0;
		do{	
			line = fileReader.readLine();
			if (line==null){
				break;
			}
			lines[i]=line;
			i++;
			System.out.println(line);
		}while (line!=null);
		for(i=0;i<19;i++){
			assertNotNull(lines[i]);
		}
		assertTrue(lines[0].startsWith("Actors from London	408	378	Olivier"));
		assertTrue(lines[0].endsWith("sitcom	0.0055248618784530384"));
		assertTrue(lines[19].startsWith("Welsh footballers	615	554	"));
		assertTrue(lines[19].endsWith("professional debut	0.0035087719298245615"));
					
	}

}
