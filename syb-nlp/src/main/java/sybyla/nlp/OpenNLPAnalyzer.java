package sybyla.nlp;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.parser.AbstractBottomUpParser;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;
import opennlp.tools.util.model.BaseModel;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;



public class OpenNLPAnalyzer {

    private static final Logger LOGGER = Logger.getLogger(OpenNLPAnalyzer.class);
        
    public static final String OPEN_NLP_POS_MODEL_KEY = "sybyla.nlp.pos.model.file";
    public static final String DEFAULT_POS_MODEL = "/en-pos-maxent.bin";
    
    public static final String OPEN_NLP_SENTENCE_DETECTOR_MODEL_KEY = "sybyla.nlp.sentence.detector.model.file";
    public static final String DEFAULT_SENTENCE_DETECTOR_MODEL = "/en-sent.bin";
    
    public static final String OPEN_NLP_PARSER_MODEL_KEY = "sybyla.nlp.parser.model.file";
    public static final String DEFAULT_PARSER_MODEL = "/en-parser-chunking.bin";
    
    public static final String OPEN_NLP_PERSON_NAME_MODEL_KEY = "sybyla.nlp.person.name.model.file";
    public static final String DEFAULT_PERSON_NAME_MODEL = "/en-ner-person.bin";
    
    public static final String OPEN_NLP_ORGANIZATION_MODEL_KEY = "sybyla.nlp.organization.name.model.file";
    public static final String DEFAULT_ORGANIZATION_MODEL = "/en-ner-organization.bin";
    
    public static final String OPEN_NLP_LOCATION_MODEL_KEY ="sybyla.nlp.location.model.file";
    public static final String DEFAULT_LOCATION_MODEL = "/en-ner-location.bin";
    
    public static final String OPEN_NLP_TOKENIZER_MODEL_KEY ="sybyla.nlp.tokenizer.model.file";
    public static final String DEFAULT_TOKENIZER_MODEL = "/en-token.bin";
    
    public final static short NOUN_PHRASE = 0;
    public final static short PERSON = 1;
    public final static short LOCATION = 2;
    public final static short ORGANIZATION = 3;
    public final static short QUOTE = 4;
    public final static short NOUN = 5;
    
    private static final String[] NOUN_PHRASE_TAGS = {"NP"};
   

    private SentenceModel _sentenceModel;
    private SentenceDetectorME _sentenceDetector;
    
    private TokenizerModel _tokenizerModel;
    private TokenizerME _tokenizer;
    
    private POSModel _POSModel;
    private POSTaggerME _POSTagger;
    
    private ParserModel _parserModel;
    private Parser _parser;
    
    private TokenNameFinderModel _personNameModel;
    private TokenNameFinderModel _locationNameModel;
    private TokenNameFinderModel _organizationNameModel;
    private NameFinderME _personNameFinder;
    private NameFinderME _locationNameFinder;
    private NameFinderME _organizationNameFinder;
    
    private String[] _sentences;
    private String[][] _tokens;
    
    private boolean _removeWikipediaCitationMarkup = true;
    private boolean _removeTrailingPunctuation = true;
    private boolean _captureQuotes = true;
    private boolean _doNotSplitPrepositionalPhrases = false;
    private boolean _removeUnmatchedQuotes = true;
    private boolean _removeApostrophes = true;
    private int _sizeLimit = 64;
    
    private static Pattern quoteRegex = Pattern.compile( "\".*?\"");
    
    private boolean _logParses = false;

    public void init(String text) {
        if ((_sentences == null) || (_tokens == null)) {
            _sentences = detectSentences(text);
            _tokens = tokenize(_sentences);
        }
    }
    
    public void reset() {
        _sentences = null;
        _tokens = null;
    }
    
/**
* loads models and initializes the appropriate tools, e.g. the Sentence Detector if the model is a sentence detector model, etc.
* @param model the model to be loaded
* @param modelKey the key for the model system property
* @param defaultModel the default file path for the model
*/
private void setup(BaseModel model, String modelKey, String defaultModel) {
String modelFileProperty = System.getProperty(modelKey);
String modelFile=defaultModel;
        if (modelFileProperty != null) {
            modelFile = modelFileProperty;
        }
        LOGGER.info("Opening model file " + modelFile);
        InputStream modelIn;
        modelIn = OpenNLPAnalyzer.class.getResourceAsStream(modelFile);
        if (modelIn == null) throw new RuntimeException("Unable to load model "+ modelFile);
        try {
           if (modelKey.equals(OPEN_NLP_SENTENCE_DETECTOR_MODEL_KEY)) {
              model = new SentenceModel(modelIn);
              _sentenceModel = (SentenceModel)model;
              _sentenceDetector = new SentenceDetectorME(_sentenceModel);
           }
           else if (modelKey.equalsIgnoreCase(OPEN_NLP_TOKENIZER_MODEL_KEY)) {
              model = new TokenizerModel(modelIn);
              _tokenizerModel = (TokenizerModel) model;
              _tokenizer = new TokenizerME(_tokenizerModel);
           }
           else if (modelKey.equalsIgnoreCase(OPEN_NLP_PARSER_MODEL_KEY)) {
              model = new ParserModel(modelIn);
              _parserModel = (ParserModel) model;
              _parser = ParserFactory.create(_parserModel);
           }
           else if (modelKey.equals(OPEN_NLP_POS_MODEL_KEY)) {
              model = new POSModel(modelIn);
              _POSModel = (POSModel)model;
              _POSTagger = new POSTaggerME(_POSModel);
           }
           else if ( modelKey.equalsIgnoreCase(OPEN_NLP_PERSON_NAME_MODEL_KEY)
                    || modelKey.equalsIgnoreCase(OPEN_NLP_LOCATION_MODEL_KEY)
                    || modelKey.equalsIgnoreCase(OPEN_NLP_ORGANIZATION_MODEL_KEY)) {
                    
               model = new TokenNameFinderModel(modelIn);
               if (modelKey.equals(OPEN_NLP_PERSON_NAME_MODEL_KEY)) {
                  _personNameModel = (TokenNameFinderModel) model;
                  _personNameFinder = new NameFinderME(_personNameModel);
               }
               else if (modelKey.equals(OPEN_NLP_LOCATION_MODEL_KEY)) {
                  _locationNameModel = (TokenNameFinderModel) model;
                  _locationNameFinder = new NameFinderME(_locationNameModel);
               }
               else if(modelKey.equals(OPEN_NLP_ORGANIZATION_MODEL_KEY)) {
                  _organizationNameModel = (TokenNameFinderModel) model;
                  _organizationNameFinder = new NameFinderME(_organizationNameModel);
               }
          }
       }
       catch (IOException e) {
          LOGGER.error("Error loading model file " + modelFile, e);
       }
       finally {
          if (modelIn != null) {
             try {
                modelIn.close();
             }
             catch (IOException e) {
                LOGGER.error("Error closing stream " + modelIn,e);
             }
          }
       }
}

private String streamToString(InputStream is) {
StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(is,writer,"UTF-8");
        } catch (IOException e) {
            LOGGER.error("error reading input stream for sentence detection",e);
            return null;
        }
        String text = writer.toString();
        return text;
}

public String[] detectSentences(InputStream is) {
String text = streamToString(is);
        return detectSentences(text);
}

public List<String> findQuotes(String text) {
return findQuotes(text, true);
  }

public int countOccurrences( String term, String text){

int nOccurrences = 0;
int fromIndex = 0;
int idx = 0;
do {
idx = text.indexOf(term, fromIndex);
if(idx >=0) {
//String t = text.substring(idx,idx+term.length());
nOccurrences++;
fromIndex += idx + term.length();
}
} while(idx>=0 && fromIndex < text.length());
return nOccurrences;
}

public List<String> findQuotes(String text, boolean dedupe) {
Matcher matcher = quoteRegex.matcher(text);
List<String> quotes = new ArrayList<String>();
while(matcher.find()) {
String quote = matcher.group();
quote = quote.substring(1, quote.length()-1);
if (quote.length() <= _sizeLimit) {
quotes.add(matcher.group().replace("\"", "").trim());
}
}
if (dedupe) {
return dedupe(quotes);
}
else {
return quotes;
}
}

public String[] detectSentences(String text) {
if (_sentenceModel == null) {
setup(_sentenceModel,OPEN_NLP_SENTENCE_DETECTOR_MODEL_KEY,DEFAULT_SENTENCE_DETECTOR_MODEL);
}

if (_removeWikipediaCitationMarkup) {
text = removeWikipediaCitationMarkup(text);
}
_sentences = _sentenceDetector.sentDetect(text);
List<String> allSentences = new ArrayList<String>();
//OpenNLP does not handle newline characters, so we break sentences around those as well
for (String sentence: _sentences) {
StringTokenizer tokenizer = new StringTokenizer(sentence,"\n");
while(tokenizer.hasMoreTokens()) {
String s = tokenizer.nextToken();
allSentences.add(s);
}
}
_sentences = allSentences.toArray(new String[allSentences.size()]);
        return _sentences;
}

public String[][] tokenize(String[] sentences) {
if(_tokenizer == null) {
setup(_tokenizerModel, OPEN_NLP_TOKENIZER_MODEL_KEY, DEFAULT_TOKENIZER_MODEL);
}

_tokens = new String[sentences.length][];
for(int i=0; i< sentences.length; i++) {
_tokens[i] = _tokenizer.tokenize(sentences[i]);
}
return _tokens;
}

public String[] tokenize(String sentence) {
if(_tokenizer == null) {
setup(_tokenizerModel, OPEN_NLP_TOKENIZER_MODEL_KEY, DEFAULT_TOKENIZER_MODEL);
}

String [] tokens = _tokenizer.tokenize(sentence);

return tokens;
}

public void tagPOS(String[] tokens, String[] tags, double[] likelihoods) {
if (_POSTagger == null) {
setup(_POSModel, OPEN_NLP_POS_MODEL_KEY, DEFAULT_POS_MODEL);
}
tags = _POSTagger.tag(tokens);
likelihoods = _POSTagger.probs();
}


public String removeWikipediaCitationMarkup(String text)
{
return text.replaceAll("\\[\\d+\\]", "");
}

public String removeTrailingPunctuation(String text) {
        if (_removeTrailingPunctuation) {
            return text.replaceAll("[.,;:?!]*$", "");
        }
        return text;
}

public boolean isQuote(String text) {
return ( (text.startsWith("\"") && text.endsWith("\""))
|| (text.startsWith("\'") && text.endsWith("\'")));
}

protected List<String> dedupe(List<String> terms) {
if ((terms==null) || (terms.size()==0)) return terms;
List<String> deduped = new ArrayList<String>(terms.size());
Collections.sort(terms);
deduped.add(terms.get(0));
for (String term: terms) {
if (!term.equals(deduped.get(deduped.size()-1))) {
deduped.add(term);
}
}
LOGGER.debug(terms.size() + " original terms, " + deduped.size() + " deduped terms");
return deduped;
}

/**
* dedupes a slave list of terms by looking its terms up in a master list;
* if the master list contains the term, it is removed from the slave list.
* The master list may be sorted if necessary, but no elements are removed from it
* @param master - the master reference list
* @param slave - the slave list to be deduped
* @param sorted - specifies if the lists are already sorted; if they are not, lists
* @return the deduped list
*/
protected List<String> dedupe(List<String> master, List<String> slave, boolean sorted) {
if (master == null || master.size() == 0) return new ArrayList<String>(slave);

if (!sorted) {
Collections.sort(master);
Collections.sort(slave);
}

Iterator<String> j = master.iterator();
Iterator<String> i =slave.iterator() ;

String m = j.next();

List<String> deduped = new ArrayList<String>(slave);
Iterator<String> k = deduped.iterator();

while (i.hasNext()) {

String s = i.next();
k.next();
while (s.compareTo(m)>0 && j.hasNext()) {
m = j.next();
}
if(s.compareTo(m)==0) k.remove();
}
LOGGER.debug(slave.size() + " elements in original, " + deduped.size() + " elements after deduping against entities");
return deduped;
}

public List<String> findNames(String[] tokens, short nameType) {
NameFinderME finder=null;
if (nameType == PERSON) {
if (_personNameFinder == null) {
setup(_personNameModel, OPEN_NLP_PERSON_NAME_MODEL_KEY, DEFAULT_PERSON_NAME_MODEL);
}
finder = _personNameFinder;
}
else if (nameType == LOCATION) {
if (_locationNameFinder == null) {
                setup(_locationNameModel, OPEN_NLP_LOCATION_MODEL_KEY, DEFAULT_LOCATION_MODEL);
            }
finder = _locationNameFinder;
}
else if (nameType == ORGANIZATION) {
if (_organizationNameFinder == null) {
                setup(_organizationNameModel, OPEN_NLP_ORGANIZATION_MODEL_KEY, DEFAULT_ORGANIZATION_MODEL);
            }
finder = _organizationNameFinder;
} else return null;

Span[] nameIndexes = finder.find(tokens);
List<String> names = new ArrayList<String>(nameIndexes.length);
for (Span span: nameIndexes) {
StringBuffer sb = new StringBuffer();

int start = span.getStart();
int end = span.getEnd();
for(int i=start; i<end; i++) {
if (i>0 && i<=end-1 && !tokens[i].startsWith("\'")) {
sb.append(" ");
}
sb.append(tokens[i].trim());

}
String w = removeEnclosingCharacters(sb.toString());
if (isWord(w)) {
w = Normalizer.normalize(w, Normalizer.Form.NFC);
names.add(w);
}
}
finder.clearAdaptiveData();
return names;
}

public Map<Short,List<String>> findNamedEntities(InputStream in) {

StringWriter writer = new StringWriter();
try {
IOUtils.copy(in,writer,"UTF-8");
} catch (IOException e) {
LOGGER.error("error reading input stream for sentence detection",e);
return null;
}
String text = writer.toString();
return findNamedEntities(text);

}

public List<String> findNouns(String text) {
return findNouns(text, true);
}

public List<String> findNouns(String text, boolean dedupe) {
init(text);
List<String> nouns = new ArrayList<String>();

if (_POSTagger == null) {
setup(_POSModel, OPEN_NLP_POS_MODEL_KEY, DEFAULT_POS_MODEL);
}

boolean keepGoing = false;
StringBuffer nounSequence = new StringBuffer();
for (int i=0; i<_tokens.length; i++) {
String[] tags = _POSTagger.tag(_tokens[i]);
for (int j=0; j < _tokens[i].length; j++) {
String tag = tags[j];
String word = _tokens[i][j];
// String w = removeEnclosingCharacters(word);
String w = word;

boolean isNoun = isNoun(tag);
                boolean isWord = isWord(w);
                boolean isPossessive = isPossessive(tag);

                if (!isWord || !isSupportedCharacterSet(w)) {
keepGoing = false;
if (nounSequence.length() > 0) {
nouns.add(removeEnclosingCharacters(nounSequence.toString()));
nounSequence.delete(0, nounSequence.length());
}
continue;
}

// boolean isAdjectiveFollowedByNoun=false;
if (isAdjective(tag)) {
if(j+1<tags.length) {
String nextTag=tags[j+1];
if (isNoun(nextTag)) {
//isAdjectiveFollowedByNoun=true;
if (nounSequence.length() > 0) {
nouns.add(removeEnclosingCharacters(nounSequence.toString()));
nounSequence.delete(0, nounSequence.length());
}
nounSequence.append(w);
keepGoing=true;
continue;
}
}
}


if (isNoun || isPossessive) {//(isNoun || isAdjectiveFollowedByNoun)) {
//String w = removeEnclosingCharacters(word);
if (!isPossessive) {
nouns.add(w);
}

if (!keepGoing) {
keepGoing = true;
nounSequence.append(w);
}
else {

//if it's a "'s", do not append a space
if (isPossessive) {
nounSequence.append(w);
} else {
nouns.add(w);
nounSequence.append(" "+w);
}
//w = removeEnclosingCharacters(nounSequence.toString());
nouns.add(removeEnclosingCharacters(nounSequence.toString()));
continue;
}
} else if (keepGoing && isConnective(tags,j)){
//String w = removeEnclosingCharacters(word);

nounSequence.append(" " + w);
continue;

} else {
keepGoing = false;
//String w = removeEnclosingCharacters(nounSequence.toString());
if (nounSequence.length() > 0) {
nouns.add(removeEnclosingCharacters(nounSequence.toString()));
nounSequence.delete(0, nounSequence.length());
}
}
//Let's not use gerund verbs for now. We can uncomment this later
/*if (isGerundVerb(tag)) {
//String w = removeEnclosingCharacters(word);
nouns.add(removeEnclosingCharacters(removeEnclosingCharacters(w)));
if (j-1>=0) {
if (isAdjective(tags[j-1])){
w = removeEnclosingCharacters(_tokens[i][j-1]+" "+w);
nouns.add(removeEnclosingCharacters(w));
}
}
}*/
}
}
if (dedupe) {
return dedupe(nouns);
} else {
return nouns;
}

}

private boolean isGerundVerb(String tag) {
return tag.equals("VBG");
}

private boolean isConnective (String[] tags, int i) {
if (i < tags.length) {
if (isPreposition(tags[i])) {
if (i + 1 < tags.length) {
if (isNoun(tags[i+1]) || isArticle(tags[i+1])) {
return true;
}
}
} else {
if (i-1>=0) {
if (isPreposition(tags[i-1]) && isArticle(tags[i])) {
return true;
}
}
}
}
return false;
}

public List<String> findNounPhrase(InputStream is) {
String text = streamToString(is);
return findNounPhrases(text);
}

public List<String> findNounPhrases(String text) {

List<String> nounPhrases = new ArrayList<String>();

if (_parserModel == null) {
setup(_parserModel,OPEN_NLP_PARSER_MODEL_KEY,DEFAULT_PARSER_MODEL);
}
String[] sentences = detectSentences(text);
for (String sentence: sentences) {
Parse[] parses = ParserTool.parseLine(sentence, _parser, 1);
for(Parse parse: parses) {
List<String> parseNounPhrases = filterNounPhrases(parse);
nounPhrases.addAll(parseNounPhrases);
}
}
nounPhrases = dedupe(nounPhrases);
return nounPhrases;
}

private boolean isNounPhraseTag(String tag) {
int i = Arrays.binarySearch(NOUN_PHRASE_TAGS, tag);
return (i>=0) && (i<NOUN_PHRASE_TAGS.length) && tag.equals(NOUN_PHRASE_TAGS[i]);
}

private boolean isWord(String word) {
String w = word.trim();
return !w.equals("") && !w.equals("·")
               && !w.equals("|") && !w.equals("\n")
               && !w.equals("\t") && !w.equals("*")
               && !w.equals("(") && !w.equals(")");
}

private boolean isNoun(String tag) {

return ( tag.startsWith("NN") || tag.equals("NN") || tag.equals("FW"));
}

private boolean isArticle(String tag) {
return tag.equals("DT");
}

private boolean isPossessive(String tag) {
        return tag.equals("POS");
    }

private boolean isAdjective(String tag) {
return tag.equals("JJ");
}

private boolean isPreposition(String tag) {
return tag.equals("IN");
}

private Parse getFirstToken(Parse p) {
Parse[] children = p.getChildren();
//we stop when we reach the parent of a token
if (children != null && children.length > 0 && children[0].getType().equals(AbstractBottomUpParser.TOK_NODE)) return p;
return getFirstToken(children[0]);
}

private boolean isNounPhraseFollowedByPrepositionalPhrase(Parse p) {
if (p.getType().equals("NP")) {
Parse[] children = p.getChildren();

if (children == null || children.length != 2) return false;

if (children[0].getType().equals("NP") && children[1].getType().equals("PP")) {
Parse[] grandchildren = children[1].getChildren();

if (grandchildren == null || grandchildren.length != 2) return false;
if (grandchildren[0].getType().equals("IN") && grandchildren[1].getType().equals("NP")) {
return true;
}
}
return false;
}
return false;
}


public List<String> filterNounPhrases(Parse parse) {
List<String> nounPhrases = new ArrayList<String>();

if (parse.getType().equals(AbstractBottomUpParser.TOK_NODE)) return nounPhrases;

Span span = parse.getSpan();
String type = parse.getType();
int start = span.getStart();
int end = span.getEnd();
boolean isNounPhrase = isNounPhraseTag(type);
//boolean splitPhrase = true;
if (isNounPhrase) {
//get rid of noun-phrases that are only a pronoun or determiner
Parse[] children = parse.getChildren();
if ((children.length == 1) && (children[0].getType().equals("PRP") || children[0].getType().equals("DT"))) {
return nounPhrases;
}

Parse firstToken = getFirstToken(parse);
//get rid of leading articles and possessive pronouns in noun phrases
if ((firstToken.getType().equals("DT")) || (firstToken.getType().equals("PRP$"))) {
start = firstToken.getSpan().getEnd();
}

//if (_doNotSplitPrepositionalPhrases && isNounPhraseFollowedByPrepositionalPhrase(parse)) {
// splitPhrase = false;
//}

String text = parse.getText().substring(start, end).trim();
text = removeUnmatchedQuotes(text);

text = removeTrailingPunctuation(text);

text = removeApostrophes(text);

text = removeEnclosingCharacters(text);

text = text.trim();

if (text.length()<=_sizeLimit) {
nounPhrases.add(text);
if (_logParses) {
StringBuffer sb = new StringBuffer();
parse.show(sb);
LOGGER.debug(sb);
LOGGER.debug(text);
}
}
}

//if (splitPhrase) {
Parse[] children = parse.getChildren();
for (Parse child: children) {
if (!child.getType().equals(AbstractBottomUpParser.TOK_NODE)) {
List<String> childrenNounPhrases = filterNounPhrases(child);
nounPhrases.addAll(childrenNounPhrases);
}
}
//}
return nounPhrases;
}



public Map<Short,List<String>> findNamedEntities(String text) {

Map<Short,List<String>> names = new HashMap<Short,List<String>>();
init(text);

for(int i=0; i<_sentences.length;i++) {

List<String> people = findNames(_tokens[i],PERSON);
List<String> personNames = names.get(PERSON);
if (personNames == null) {
personNames = new ArrayList<String>();
names.put(PERSON, personNames);
}

for(String s: people) {
s=s.trim();
if (!personNames.contains(s)) {
personNames.add(s);
}
}

List<String> locations = findNames(_tokens[i],LOCATION);
List<String> locationNames = names.get(LOCATION);
           if (locationNames == null) {
               locationNames = new ArrayList<String>();
               names.put(LOCATION, locationNames);
           }

           for(String s: locations) {
               s=s.trim();
               if (!locationNames.contains(s)) {
                   locationNames.add(s);
               }
           }
           
List<String> organizations = findNames(_tokens[i], ORGANIZATION);
List<String> organizationNames = names.get(ORGANIZATION);
           if (organizationNames == null) {
               organizationNames = new ArrayList<String>();
               names.put(ORGANIZATION, organizationNames);
           }
           
           for(String s: organizations) {
               s=s.trim();
               if (!organizationNames.contains(s)) {
                   organizationNames.add(s);
               }
           }
       }
return names;
}

public List<String> findNouns(InputStream is) {
String text = streamToString(is);
return findNouns(text);
}

public Map<Short, List<String>> findAll(InputStream is) {
String text = streamToString(is);
return findAll(text);
}

public synchronized Map<String,Integer> getCounts(Map<Short,List<String>> termMap, String text) {

Map<String, Integer> termCounts = new HashMap<String, Integer>();
Set<String> terms = new HashSet<String>();

for (Short entityType: termMap.keySet()) {
            List<String> entityList = termMap.get(entityType);
            terms.addAll(entityList);
        }

        String trimmedSpaceText = text.trim().replaceAll(" +", " ");

        for(String term: terms){
            int count = countOccurrences(term, trimmedSpaceText);
            termCounts.put(term, count);
        }
        
        return termCounts;

}

public Map<Short,List<String>> findAll(String text){
reset();
init(text);
Map<Short,List<String>> entities = findNamedEntities(text);
List<String> quotes = findQuotes(text);
entities.put(QUOTE, quotes);
// List<String> nounPhrases = findNounPhrases(text);
List<String> nouns = findNouns(text);
for (Short entityType: entities.keySet()) {
List<String> entityList = entities.get(entityType);
Collections.sort(entityList);
nouns = dedupe(entityList,nouns, true);
}
entities.put(NOUN,nouns);

return entities;
}

private String removeEnclosingCharacters(String text) {
String[] characters = {"*","(",")","\""};
for(String character: characters) {
if (text.startsWith(character) && !text.endsWith(character)) {
                return removeEnclosingCharacters(text.substring(1));
            }
            if (!text.startsWith(character) && text.endsWith(character)) {
                return removeEnclosingCharacters(text.substring(0,text.length()-1));
            }
            if (text.startsWith(character) && !text.endsWith(character)) {
                return removeEnclosingCharacters(text.substring(1));
            }
            if (!text.startsWith(character) && text.endsWith(character)) {
                return removeEnclosingCharacters(text.substring(0,text.length()-1));
            }
}
return text;
}

private String removeUnmatchedQuotes(String text){
if (_removeUnmatchedQuotes) {
if (text.startsWith("\"") && !text.endsWith("\"")) {
return text.substring(1);
}
if (!text.startsWith("\"") && text.endsWith("\"")) {
return text.substring(0,text.length()-1);
}
if (text.startsWith("\'") && !text.endsWith("\'")) {
return text.substring(1);
}
if (!text.startsWith("\'") && text.endsWith("\'")) {
return text.substring(0,text.length()-1);
}
}
return text;
}

private String removeApostrophes(String text) {
if (!_removeApostrophes) return text;

if (text.endsWith("\'s")) {
           return text.substring(0,text.length()-2);
}
return text;
}

    public boolean isRemoveWikipediaCitationMarkup() {
        return _removeWikipediaCitationMarkup;
    }

    public void setRemoveWikipediaCitationMarkup(boolean removeWikipediaCitationMarkup) {
        this._removeWikipediaCitationMarkup = removeWikipediaCitationMarkup;
    }

    public boolean is_logParses() {
        return _logParses;
    }

    public void set_logParses(boolean _logParses) {
        this._logParses = _logParses;
    }
    //we support UTF-8. If a string contains UTF-16 characters that are not contained
    //in UTF-8 we flag it as unsupported so it can be removed from the term set
    public boolean isSupportedCharacterSet(String s) {
        String sn = Normalizer.normalize(s, Normalizer.Form.NFC);
        
        try {
            String s8 = new String(s.getBytes("UTF-8"),"UTF-8");
            String s16 = new String(s.getBytes("UTF-16"),"UTF-16");
            if (s8.equals(s16)) {
                return true;
            }
            else {
                return false;
            }
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Could not decode string "+s,e);
            return false;
        }
    }
}
