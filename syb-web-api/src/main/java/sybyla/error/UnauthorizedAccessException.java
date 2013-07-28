package sybyla.error;

@SuppressWarnings("serial")
public class UnauthorizedAccessException extends Exception {
	public UnauthorizedAccessException(String message){
		super(message);
	}
}
