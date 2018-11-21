package rainbownlp.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rainbownlp.core.Artifact;

import rainbownlp.util.HibernateUtil;
//This class will read all the training sentences and parse them and put penn tree and dependency and POS in the databse

public class ParseHandler {
	public ArrayList<WordTag> sentenceWords = new ArrayList<WordTag>();
	public ParseHandler()
	{
		
	}
	public static StanfordParser s_parser = new StanfordParser();
	public static void main(String[] args) throws Exception
	{
////		StanfordParser s_parser = new StanfordParser();
//		//get all sentence artifact
//		List<Artifact> sentences = 
//			Artifact.listByType(Artifact.Type.Sentence,true);
//		ParseHandler ph = new ParseHandler();
//		
//		for (Artifact sentence:sentences)
//		{
//			calculatePOS(s_parser,sentence);
//			
//			//now parse the normalized sentence( here just normalized to head)
//			NormalizedSentence normalized_sent_obj = NormalizedSentence.getInstance(sentence,NormalizationMethod.MethodType.MentionToHead);
//			String normalized_sent = normalized_sent_obj.getNormalizedContent();
//			s_parser.parse(normalized_sent);
//			
//			String nor_dependencies = s_parser.getDependencies();
//			String nor_penn_tree = s_parser.getPenn();
//			normalized_sent_obj.setNormalizedDependency(nor_dependencies);
//			normalized_sent_obj.setNormalizedPennTree(nor_penn_tree);
//			
//			HibernateUtil.save(normalized_sent_obj);
//			
//			HibernateUtil.clearLoaderSession();
//		}

		
	}
	ParseHandler ph = new ParseHandler();
//	public static void calculateStanfordParseAndNormalize(boolean is_training_mode) throws Exception
//	{
//		List<Artifact> sentences = 
//			Artifact.listByType(Artifact.Type.Sentence,is_training_mode);
//		for (Artifact sentence:sentences)
//		{
//			calculatePOS(s_parser,sentence);
//			
//			//now parse the normalized sentence( here just normalized to head)
//			NormalizedSentence normalized_sent_obj = NormalizedSentence.getInstance(sentence,NormalizationMethod.MethodType.MentionToHead);
//			String normalized_sent = normalized_sent_obj.getNormalizedContent();
//			
//			HibernateUtil.clearLoaderSession();
//		}
//
//	}
	
	public static void calculatePOS(StanfordParser p_s_parser, Artifact sentence ) throws Exception
	{
		StanfordParser s_parser = p_s_parser;
		if (s_parser == null)
		{
			s_parser = new StanfordParser();
		}
		s_parser.parse(sentence.getContent());
		//TODO put dependencies
		String pos_tagged_sentence = s_parser.getTagged();
		String dependencies = s_parser.getDependencies();
		String penn_tree = s_parser.getPenn();
		
		sentence.setPOS(pos_tagged_sentence);
		sentence.setStanDependency(dependencies);
		sentence.setStanPennTree(penn_tree);
//		HibernateUtil.save(sentence);
		
		ArrayList<WordTag> w_tags = analyzePOSTaggedSentence(pos_tagged_sentence);
		List<Artifact> tokenArtifacts =sentence.getChildsArtifact();
		
		for (int i=0;i<w_tags.size();i++)
		{
			WordTag wt = w_tags.get(i);
			//get artifact
			
//			Artifact word_in_sent = Artifact.findInstance(sentence, i);
			Artifact word_in_sent =tokenArtifacts.get(i);
			if (word_in_sent.getContent().matches("\\w+") && !word_in_sent.getContent().equals(wt.content))
			{
				throw (new Exception("Related artifact is not found"));
			}
			//set POS
			word_in_sent.setPOS(wt.POS);
			HibernateUtil.save(word_in_sent);
			
			HibernateUtil.clearLoaderSession();
		}
		HibernateUtil.save(sentence);
	}
	
	
	//This will return a list of the word tag objects based on the tagged sentence
	public static ArrayList<WordTag> analyzePOSTaggedSentence(String pTaggedSentence) throws Exception
	{
		String tokens[] = pTaggedSentence.split(" ");
		ArrayList<WordTag> word_tags =  new ArrayList<ParseHandler.WordTag>();
		int count=0;
		for (String token:tokens)
		{
			WordTag wt = new WordTag();
			Pattern p = Pattern.compile("(.*)\\/([^\\/]+)");
			Matcher m = p.matcher(token);
			if (m.matches())
			{
				String content = m.group(1);
				content =  content.replaceAll("\\\\/", "/");
				wt.content = content;
				wt.POS = m.group(2);
				wt.offset = count;
				word_tags.add(wt);
				count++;
			}
			else
			{
				throw (new Exception("the POS tag doesn't match the pattern"));
			}
			
			
		}
		return word_tags;
	}
	private static class WordTag{
		public String content;
		public String POS;
		public int offset;
		public WordTag() {
			// TODO Auto-generated constructor stub
		}
	}
}
