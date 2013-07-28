package sybyla.utils;

import java.util.Date;

public class Utils {
	
	public static Long currentTimeSeconds(){
		return new Date().getTime()/1000;
	}
	
	public static Long currentTimeMilliseconds(){
		return new Date().getTime();
	}

	
	public static boolean check(String... s){

		for (String ss: s) {
			if (ss==null || ss.trim().equals("")){
				return false;
			}
		}
		return true;
	}
}
