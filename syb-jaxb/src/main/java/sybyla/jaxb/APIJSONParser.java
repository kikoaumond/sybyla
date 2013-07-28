package sybyla.jaxb;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import sybyla.jaxb.ApiResponse;

public class APIJSONParser {
    
    public static ApiResponse readResponse(String json) throws JsonParseException, JsonMappingException, IOException{

        ObjectMapper mapper = ResponseWriter.makeObjectMapper();
        ApiResponse response = mapper.readValue(new ByteArrayInputStream(json.getBytes("UTF-8")), ApiResponse.class);
        return response;
    }
}
