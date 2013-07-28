package sybyla.error;

@SuppressWarnings("serial")
public class AboveQuotaException extends Exception {
	public AboveQuotaException(String message){
		super(message);
	}
}
