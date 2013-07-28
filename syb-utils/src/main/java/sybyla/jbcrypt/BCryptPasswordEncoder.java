package sybyla.jbcrypt;

import org.mindrot.jbcrypt.BCrypt;

public class BCryptPasswordEncoder  {
	 
    public String encodePassword(String rawPass)  {
        return BCrypt.hashpw(rawPass, BCrypt.gensalt());
    }
 
    public boolean isPasswordValid(String encPass, String rawPass) {
        return BCrypt.checkpw(rawPass, encPass);
    }
}