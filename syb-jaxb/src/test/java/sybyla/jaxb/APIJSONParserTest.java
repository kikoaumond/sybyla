package sybyla.jaxb;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

public class APIJSONParserTest {
    
    @Test
    public void testUTF8() throws IOException{
        InputStream in = new FileInputStream("src/test/resources/utf8.txt");
        BufferedReader reader= new BufferedReader(new InputStreamReader(in));
        StringBuilder response = new StringBuilder();
        String line=null;
        while((line = reader.readLine()) != null){
            response.append(line).append("\n");
        }
        ApiResponse apiResponse = APIJSONParser.readResponse(response.toString());
        List<TagResult> tagResults = apiResponse.getTags();
        Set<String> expectedTags = new HashSet<String>();
        
        expectedTags.add(new String("Palácio".getBytes(Charset.forName("UTF-8"))));
        expectedTags.add(new String("José Sarney".getBytes(Charset.forName("UTF-8"))));
        expectedTags.add(new String("São Paulo".getBytes(Charset.forName("UTF-8"))));
        
        for(TagResult tag:tagResults){
            String term = tag.getTerm();
            expectedTags.remove(term);
            System.out.println(term);
        }
        for(String s: expectedTags){
            System.out.println("Remaining Expected Tag: "+ s);
        }
        assertTrue(expectedTags.size()==0);
        
    }
    
}
