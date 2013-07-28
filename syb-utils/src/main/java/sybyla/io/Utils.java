package sybyla.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;

public class Utils {
	
	private static String streamToString(InputStream is) throws IOException {
	
		StringWriter writer = new StringWriter();
		IOUtils.copy(is,writer,"UTF-8");	
		String text = writer.toString();
		return text;
	}
}
