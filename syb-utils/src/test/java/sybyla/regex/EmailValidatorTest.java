package sybyla.regex;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
 

public class EmailValidatorTest {
 
	private EmailValidator emailValidator;
	private String[] validEmails;
	private String[] invalidEmails;
 
	@Before
        public void initData(){
		
		emailValidator = new EmailValidator();
		
		validEmails =new String[] {
				   "mkyong@yahoo.com", "mkyong-100@yahoo.com",
                   "mkyong.100@yahoo.com" ,"mkyong111@mkyong.com", 
	   "mkyong-100@mkyong.net","mkyong.100@mkyong.com.au",
	   "mkyong@1.com", "mkyong@gmail.com.com", "mike+1@librato.com", "o.b.fischer@swe-blog.net",
	   "nicholas.herring@meteor-ent.com",
	   "mike@familiar-inc.com"
	         };
		
		invalidEmails=new String[] {
				   "mkyong","mkyong@.com.my","mkyong123@gmail.a",
				   "mkyong123@.com","mkyong123@.com.com",
	                           ".mkyong@mkyong.com","mkyong()*@gmail.com",
				    "mkyong@%*.com", "mkyong..2002@gmail.com",
				   "mkyong.@gmail.com","mkyong@mkyong@gmail.com", 
	                           "mkyong@gmail.com.1a"
			        };
        }
 
	@Test
	public void ValidEmailTest() {
 
	   for(String temp : validEmails){
		boolean valid = emailValidator.validate(temp);
		System.out.println("Email is valid : " + temp + " , " + valid);
		Assert.assertTrue( valid);
	   }
 
	}
 
	@Test
	public void InValidEmailTest() {
 
	   for(String temp : invalidEmails){
		   boolean valid = emailValidator.validate(temp);
		   System.out.println("Email is valid : " + temp + " , " + valid);
		   Assert.assertEquals(false, valid);
	   } 
	}	
}