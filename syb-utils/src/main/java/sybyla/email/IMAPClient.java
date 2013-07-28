package sybyla.email;

import java.io.IOException;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import org.apache.log4j.Logger;

import sybyla.email.EmailException;
import sybyla.utils.Utils;

public class IMAPClient {
	
	private static final Logger LOGGER =  Logger.getLogger(IMAPClient.class);
	
	public static final int DEFAULT_PORT=993;
	public static final String INBOX="INBOX";
	
	private String host;
	private int port=DEFAULT_PORT;
	private String username;
	private String password;
	private Store store;
	
	public IMAPClient(String host, String username, String password) throws EmailException{
		
		this.host = host;
		this.username = username;
		this.password =  password;
		if (!Utils.check(host,username, password)){
			throw new EmailException("Missing parameters for IMAP Client: "+ this);
		}
	}
	
	public IMAPClient(String host, int port, String username, String password) throws EmailException{
		this(host, username,password);
		this.port=port;
	}
	
	public void connect() throws MessagingException{
		Properties props = System.getProperties();
		props.setProperty("mail.store.protocol", "imaps");
		//enable SSL
		props.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.setProperty("mail.imap.socketFactory.fallback", "false");
		
		 Session session = Session.getInstance(props, null);
		 store = session.getStore("imaps");
		 store.connect(host, port, username, password);
		 LOGGER.info("Succesfully connected to IMAP server "+host);
	}
	
	public void disconnect() throws MessagingException{
		
		 store.close();
		 LOGGER.info("Succesfully disconnected to IMAP server "+host);
	}
	
	public void listMessages() throws MessagingException, IOException{
		Folder emailFolder = store.getFolder("INBOX");
		emailFolder.open(Folder.READ_ONLY);

		Message[] messages = emailFolder.getMessages();
		for (int i = 0; i < messages.length; i++) {
			Message message = messages[i];
			System.out.println("==============================");
			System.out.println("Email #" + (i + 1));
			System.out.println("Subject: " + message.getSubject());
			System.out.println("From: " + message.getFrom()[0]);
			System.out.println("Text: " + message.getContent().toString());
		}

		emailFolder.close(false);
		store.close();
	}
	
	public void listAndDeleteMessages() throws MessagingException, IOException{
		Folder emailFolder = store.getFolder("INBOX");
		emailFolder.open(Folder.READ_WRITE);

		Message[] messages = emailFolder.getMessages();
		for (int i = 0; i < messages.length; i++) {
			Message message = messages[i];
			message.setFlag(Flags.Flag.DELETED, true);
			System.out.println("==============================");
			System.out.println("Email #" + (i + 1));
			System.out.println("Subject: " + message.getSubject());
			System.out.println("From: " + message.getFrom()[0]);
			System.out.println("Text: " + message.getContent().toString());
		}

		emailFolder.close(true);
		store.close();
	}
	
	public Message[] getMessages() throws MessagingException, IOException{
		Folder emailFolder = store.getFolder(INBOX);
		emailFolder.open(Folder.READ_ONLY);

		Message[] messages = emailFolder.getMessages();		

		emailFolder.close(true);
		store.close();
		return messages;
	}
	

	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("IMAP Client: \n");
		sb.append("host: "+host+"\n");
		sb.append("port: " + port+"\n");
		sb.append("username: "+host+"\n");
		String p=(password!=null)?"********":"not supplied";
		sb.append("password: "+p);
		return sb.toString();
	}
	
}
