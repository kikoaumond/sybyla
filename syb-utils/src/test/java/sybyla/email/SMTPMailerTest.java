package sybyla.email;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.mail.MessagingException;

import org.junit.Test;


public class SMTPMailerTest {
	
	private static final String MANDRILL_TEST_USERNAME="kaumond@kissmetrics.com";
	private static final String MANDRILL_TEST_PASSWORD="b2a7d04a-7714-47f3-9408-e12d81d4f43f";
	private static final String MANDRILL_TEST_HOST="testHost";
	
	
	private static final String SENDGRID_TEST_USERNAME="kmsendgrid";
	private static final String SENDGRID_TEST_PASSWORD="WyY8ZjYB";
	private static final String SENDGRID_TEST_HOST="testHost";

	
	//@Test
	public void testConnectMandrill(){
		SMTPMailer smtpMailer=null;
		try {
			 smtpMailer = new SMTPMailer(MANDRILL_TEST_HOST,
					 					 MANDRILL_TEST_USERNAME, 
					 					 MANDRILL_TEST_PASSWORD);
		} catch (EmailException e) {
			fail(e.getMessage());
		}
		
		try {
			smtpMailer.connect();
			assertTrue(smtpMailer.isConnected());
			smtpMailer.disconnect();
			assertFalse(smtpMailer.isConnected());
		} catch (MessagingException e) {
			fail(e.getMessage());		
		}
	}
	
	//@Test
	public void testConnectSendgrid(){
		SMTPMailer smtpMailer=null;
		try {
			 smtpMailer = new SMTPMailer(SENDGRID_TEST_HOST,
					 					 SENDGRID_TEST_USERNAME, 
					 					 SENDGRID_TEST_PASSWORD);
		} catch (EmailException e) {
			fail(e.getMessage());
		}
		
		try {
			smtpMailer.connect();
			assertTrue(smtpMailer.isConnected());
			smtpMailer.disconnect();
			assertFalse(smtpMailer.isConnected());
		} catch (MessagingException e) {
			fail(e.getMessage());		
		}
	}
	
	@Test (expected = javax.mail.MessagingException.class) 
	public void testConnectBogusCredentials() throws MessagingException{
		SMTPMailer smtpMailer=null;
		try {
			 smtpMailer = new SMTPMailer("bogus","bogus","bogus");
		} catch (EmailException e) {
			fail(e.getMessage());
		}
		
		smtpMailer.connect();
		
		
		fail("Did not catch MessagingException");
	}

}
