package sybyla.feature; /**
 * Created with IntelliJ IDEA.
 * User: kiko
 * Date: 9/10/13
 * Time: 11:32 AM
 * To change this template use File | Settings | File Templates.
 */
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Normalizer {

    public static final Pattern MULTIPLE_SPACES_PATTERN =  Pattern.compile("([\\s]+)");
    public static final Pattern PUNCTUATION =  Pattern.compile("[,;:]");
    public static final Pattern FULL_STOP =  Pattern.compile("(?<!\\.)[.]{1,1}(?!\\.)");

    public static final Pattern PARENTHESES =  Pattern.compile("[()]");
    public static final Pattern QUOTES = Pattern.compile( "[\"“”'’‘]");
    public static final Pattern ELLIPSIS = Pattern.compile("([\\.]{2,})");
    public static final Pattern INTERROGATION = Pattern.compile("[\\?]{1,}[\\s]{0,}[\\?]{0,}");
    public static final Pattern MULTIPLE_INTERROGATIONS = Pattern.compile("[\\?]{1,}[\\s]{0,}[\\?]{1,}");
    public static final Pattern EXCLAMATION = Pattern.compile("[\\!]{1,}");
    public static final Pattern MULTIPLE_EXCLAMATIONS =   Pattern.compile("[\\!]{1,}[\\s]{0,}[\\!]{1,}");
    public static final Pattern EXCLAMATION_INTERROGATION_COMBO1 = Pattern.compile("[\\?]{1,}[\\s]{0,}[\\!]{1,}?");
    public static final Pattern EXCLAMATION_INTERROGATION_COMBO2 = Pattern.compile("[\\!]{1,}[\\s]{0,}[\\?]{1,}?");
    public static final String ISOLATED_PUNCTUATIONS1= "[!?,.\\-_;:<>=*&$#@]{1,}\\s";
    public static final String ISOLATED_PUNCTUATIONS2= "\\s[!?,.\\-_;:<>=*&$#@]{1,}";
    public static final Pattern NON_WORD_CHARACTERS= Pattern.compile("[\\s][^\\w][\\s]",Pattern.UNICODE_CHARACTER_CLASS);
    public static final Pattern PLURAL = Pattern.compile("(^[^\\s]{4,})[s]{1,1}$");
    public static final Pattern SPACE = Pattern.compile("[\\s]");



    public static String normalize(String sentence){

        String s = PUNCTUATION.matcher(sentence).replaceAll(" ");
        s = s.replaceAll(ISOLATED_PUNCTUATIONS1," ");
        s = s.replaceAll(ISOLATED_PUNCTUATIONS2," ");
        s = NON_WORD_CHARACTERS.matcher(s).replaceAll("  ");
        s = QUOTES.matcher(s).replaceAll(" ");
        s = PARENTHESES.matcher(s).replaceAll(" ");
        s =  normalizeEllipsis(s);
        s = normalizeFullStop(s);
        s = normalizeInterrogations(s);
        s = normalizeExclamations(s);
        s = normalizeExclamationInterrogationCombos(s);
        s =  MULTIPLE_SPACES_PATTERN.matcher(s).replaceAll(" ");
        s =  s.toLowerCase().trim();

        return s;
    }

    public static String getSingular(String sentence){

        Matcher m = SPACE.matcher(sentence);
        if (m.find()){
            return null;
        }

        Matcher m2 = PLURAL.matcher(sentence);

        if(m.matches()){
            return m.group(1);
        }

        return null;
    }

    public static String normalizeEllipsis(String sentence){

        Matcher m = ELLIPSIS.matcher(sentence);
        if (m.find()){
            return m.replaceAll(" ... ");
        }
        return sentence;
    }

    public static String normalizeFullStop(String sentence){

        Matcher m = FULL_STOP.matcher(sentence);
        if (m.find()){
            return m.replaceAll(" ");
        }
        return sentence;
    }


    public static String normalizeInterrogations(String sentence){

        Matcher m = INTERROGATION.matcher(sentence);
        if (m.find()){
            String s =  m.replaceAll(" $0 ");
            m =  MULTIPLE_INTERROGATIONS.matcher(s);
            if (m.find()){
                s = m.replaceAll("??");
            }
            return s;
        }
        return sentence;
    }

    public static String normalizeExclamations(String sentence){

        Matcher m = EXCLAMATION.matcher(sentence);
        if (m.find()){
            String s =  m.replaceAll(" $0 ");
            m =  MULTIPLE_EXCLAMATIONS.matcher(s);
            if (m.find()){
                s = m.replaceAll("!!");
            }
            return s;
        }
        return sentence;
    }

    public static String normalizeExclamationInterrogationCombos(String sentence){
        String s =  sentence;

        Matcher m = EXCLAMATION_INTERROGATION_COMBO1.matcher(s);
        if (m.find()){
            s =  m.replaceAll(" ?! ");
        }
        m = EXCLAMATION_INTERROGATION_COMBO2.matcher(s);
        if (m.find()){
            s =  m.replaceAll(" ?! ");
        }

        return s;
    }

}
