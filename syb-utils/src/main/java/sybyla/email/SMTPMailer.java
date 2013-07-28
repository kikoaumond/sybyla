package sybyla.email;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;

import sybyla.email.EmailException;

public class SMTPMailer {
 
	private static final Logger LOGGER = Logger.getLogger(SMTPMailer.class);
	private static final String CONTENT_TYPE="Content-Type";

    //Configuration items for javax.mail
    public static final String PROTOCOL_KEY="mail.transport.protocol";
    public static final String HOST_KEY="mail.smtp.host";
    public static final String PORT_KEY="mail.smtp.port";
    public static final String AUTH_KEY="mail.smtp.auth";
    
    public static final String SMTP="smtp";
    
    public static final int DEFAULT_PORT=587;
    public static final boolean DEFAULT_AUTH=true;
    public static final String DEFAULT_PROTOCOL=SMTP;
    
    private static boolean auth= DEFAULT_AUTH;
    private static String protocol = DEFAULT_PROTOCOL;
    
    private String smtpHost;
    private int port = DEFAULT_PORT;
    private String username;
    private String password;
    
    private boolean debug=false;
    private boolean mock=false;
    
    private Transport transport;
    private Session mailSession;
    
	public SMTPMailer(String host, String username, String password) throws EmailException{
		
		this.smtpHost = host;
		this.username = username;
		this.password = password;
		
		if (!sybyla.utils.Utils.check(host,username, password)){
			String m = "Required parameters not supplied to SMTPMailer:\n"+this;
			throw new EmailException(m);
		}
	}
	
	public SMTPMailer(String host, int port, String username, String password) 
						throws EmailException{
		
		this(host,username, password);
		this.port =  port;
	}
	
    
	public void connect() throws MessagingException {
    	
		Properties props = new Properties();
        props.put(PROTOCOL_KEY, protocol);
        props.put(HOST_KEY, smtpHost);
        props.put(PORT_KEY, port);
        props.put(AUTH_KEY, auth);
 
        Authenticator authenticator = new SMTPAuthenticator();
        mailSession = Session.getInstance(props, authenticator);
        
        if (debug){
        	mailSession.setDebug(true);
        }
        
        transport = mailSession.getTransport();
        transport.connect();
        LOGGER.info("Successfully connected to SMTP host " + smtpHost);
	}
		
	public void disconnect() throws MessagingException {
		
		if (transport!=null && transport.isConnected()){
			transport.close();
		}
	}
	
	public boolean isConnected(){
		
		return(transport !=  null && transport.isConnected());
	}
	
	public void sendSMTP(String from, String to, String subject, String text, String textHTML) throws MessagingException  {
        
        MimeMessage message = new MimeMessage(mailSession);
 
        Multipart multipart = new MimeMultipart("alternative");
        if (text != null) {
        	BodyPart part1 = new MimeBodyPart();
        	part1.setText(text);
        	multipart.addBodyPart(part1);
        }
 
        if (textHTML != null) {
        	BodyPart part2 = new MimeBodyPart();
        	
        	part2.setContent(textHTML,sybyla.http.Constants.HTML_MIME_TYPE);
        	multipart.addBodyPart(part2);
        }
 
        message.setContent(multipart);
        message.setFrom(new InternetAddress(from));
        message.setSubject(subject);
        message.addRecipient(Message.RecipientType.TO,
             new InternetAddress(to));
 
    	connect();

        if (!mock){
        	transport.sendMessage(message,
        			message.getRecipients(Message.RecipientType.TO));
        }
        
        disconnect();    
    }
     
    public void send(MimeMessage msg, String emailAddress) 
    		throws MessagingException  {
    	
    	InternetAddress[] address = {new InternetAddress(emailAddress)};
    	msg.setRecipients(Message.RecipientType.TO, address);
    	msg.saveChanges();

    	if(!mock){
    		transport.sendMessage(msg, address);
    	}

    }
    
    private class SMTPAuthenticator extends javax.mail.Authenticator {
        public PasswordAuthentication getPasswordAuthentication() {
           return new PasswordAuthentication(username, password);
        }
    }
    
    public void setDebug(boolean debug) {
		this.debug = debug;
	}
    
	public void setMock(boolean mock) {
		this.mock = mock;
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("SMTP Mailer: \n");
		sb.append("host: "+smtpHost+"\n");
		sb.append("username: "+username+"\n");
		String p=(password!=null)?"********":"not supplied";
		sb.append("password: "+p);
		return sb.toString();
	}
}