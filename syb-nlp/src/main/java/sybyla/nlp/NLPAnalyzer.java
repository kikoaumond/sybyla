package sybyla.nlp;

import java.util.Map;
import java.util.Set;

import sybyla.nlp.Sequence;

public abstract class NLPAnalyzer {

	public abstract Set<Sequence> findNounSequences(String text);

	public abstract Map<String, Set<String>> buildDictionary(Set<Sequence> sequences);
	
	public abstract String[] detectSentences(String text);

}
