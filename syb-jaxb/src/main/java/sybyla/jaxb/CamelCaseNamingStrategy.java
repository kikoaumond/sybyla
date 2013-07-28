package sybyla.jaxb;

import org.codehaus.jackson.map.MapperConfig;  
import org.codehaus.jackson.map.PropertyNamingStrategy;  
import org.codehaus.jackson.map.introspect.AnnotatedField;  
import org.codehaus.jackson.map.introspect.AnnotatedMethod;  
  
/**  
 * 
 *  
 * String jsonString = "{\"foo_name\":\"fubar\"}";  
 * ObjectMapper mapper = new ObjectMapper();  
 * mapper.setPropertyNamingStrategy(  
 *     new CamelCaseNamingStrategy());  
 * Foo foo = mapper.readValue(jsonString, Foo.class);  
 * System.out.println(mapper.writeValueAsString(foo));  
 * // prints {"foo_name":"fubar"}  
 *   
 * class Foo  
 * {  
 *   private String fooName;  
 *   public String getFooName() {return fooName;}  
 *   public void setFooName(String fooName)   
 *   {this.fooName = fooName;}  
 * }  
 */  
public class CamelCaseNamingStrategy  
    extends PropertyNamingStrategy  
{  
  @Override  
  public String nameForGetterMethod(MapperConfig<?> config,  
      AnnotatedMethod method, String defaultName)  
  {  
    return translate(defaultName);  
  }  
  
  @Override  
  public String nameForSetterMethod(MapperConfig<?> config,  
      AnnotatedMethod method, String defaultName)  
  {  
    return translate(defaultName);  
  }  
  
  @Override  
  public String nameForField(MapperConfig<?> config,  
      AnnotatedField field, String defaultName)  
  {  
    return translate(defaultName);  
  }  
  
  private String translate(String defaultName)  
  {  
    char[] nameChars = defaultName.toCharArray();  
    StringBuilder nameTranslated =  
        new StringBuilder(nameChars.length);
    char begin = nameChars[0];
    if (Character.isUpperCase(begin)){
        nameChars[0]=Character.toLowerCase(begin);
    }
    for (int i=0; i<nameChars.length;i++) {
      char c = nameChars[i];
      nameTranslated.append(c);  
    }  
    return nameTranslated.toString();  
  }  
}  
