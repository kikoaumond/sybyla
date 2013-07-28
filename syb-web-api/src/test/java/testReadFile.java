


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;




public class testReadFile {
	
	
	public static void main(String[] args){
	    
		InputStream is;
		try {
			String fileName = args[0];
			is = new FileInputStream(fileName);
			InputStreamReader isr = new InputStreamReader(is,"UTF-8");

			BufferedReader reader = new BufferedReader(isr);
	        StringBuilder sb = new StringBuilder();
	        String line;
	        try {
				while((line=reader.readLine())!=null){
				   sb.append(line).append("\n");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        String text = sb.toString();
	        System.out.println(text);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   
       
	}
}
