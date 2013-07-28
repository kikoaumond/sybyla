package sybyla.email;

import java.io.IOException;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;

import sybyla.email.EmailException;
import sybyla.utils.Utils;
import com.sun.mail.pop3.POP3Store;

public class POPClient {

	public static final String POP3="pop3";
	private String type=POP3;
	
	private String host;
	private String username;
	private String password;
	
	public POPClient(String host, String username, String password) throws EmailException  {
		
		this.host = host;
		this.username =  username;
		this.password = password;
		
		if (!Utils.check(host, username, password)){
			throw new EmailException("Missing parameters supplied to POPClient"+ this);
		}
	}

	public void listMessages() {

		try {
			Properties properties = new Properties();
			properties.put("mail.pop3.host", host);
			Session emailSession = Session.getDefaultInstance(properties);

			POP3Store emailStore = (POP3Store) emailSession.getStore(type);
			emailStore.connect(username, password);

			Folder emailFolder = emailStore.getFolder("INBOX");
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
			emailStore.close();
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("POP Client: \n");
		sb.append("host: "+host+"\n");
		sb.append("type: "+type+"\n");
		sb.append("username: "+username+"\n");
		String p=(password!=null)?"********":"not supplied";
		sb.append("password: "+p);
		return sb.toString();
	}

}
