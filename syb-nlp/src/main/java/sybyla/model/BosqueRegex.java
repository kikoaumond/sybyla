package sybyla.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BosqueRegex {

	public static final String POS_REGEX="[=]{1,}[<>]{0,1}[A-Z]{1,}:([a-z]{1,})\\(.{1,}\\)\\s{1,}(.{1,})";
	public static final Pattern POS_REGEX_PATTERN=Pattern.compile("[=]{1,}[<>]{0,1}[A-Z]{1,}:([a-z]{1,})\\(.{1,}\\)\\s{1,}(.{1,})");

	public static String[] parse(String s){
		Matcher matcher = POS_REGEX_PATTERN.matcher(s);
		if (matcher.matches()){
			int n = matcher.groupCount();
			if (n==2){
				String pos = matcher.group(1);
				String word = matcher.group(2);
				String[] p = {word, pos};
				return p;
			}
		}
		return null;
	}

}
