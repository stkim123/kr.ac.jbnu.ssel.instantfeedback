package kr.ac.jbnu.ssel.instantfeedback.tool;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.TaggedWord;
import kr.ac.kangwon.ce.se.badsmell.common.ParserPreprocessor;
import kr.ac.kangwon.ce.se.badsmell.common.StanfordParserMgr;

public class IdentifierParser {
	private static StanfordParserMgr stanfordParser = StanfordParserMgr.getInstance();
	private static ParserPreprocessor ppreprocessor = new ParserPreprocessor();
	
	public List parseIdentifier(String identifier) {
		if (identifier.contains("_"))
			identifier = identifier.replace("_", " ");
		
		String methodAsSentence = ppreprocessor.preprocessMethods(identifier);
		if(methodAsSentence == null){
			return new ArrayList<String>();
		}
		ArrayList<TaggedWord> taggedWords = stanfordParser.parse(methodAsSentence);

		ArrayList<String> words = new ArrayList<String>();
		ArrayList<String> posTags = new ArrayList<String>();

		for (int i = 0; i < taggedWords.size() - 1; i++) {
			TaggedWord taggedWord = taggedWords.get(i);
			String tag = taggedWord.tag();
			String value = taggedWord.value();

			words.add(value);
			posTags.add(tag);
		}

		return words;
	}
}
