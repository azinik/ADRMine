package rainbownlp.util;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;

import dragon.nlp.tool.Lemmatiser;
import dragon.nlp.tool.PorterStemmer;
import dragon.nlp.tool.lemmatiser.EngLemmatiser;
import weka.core.Stopwords;

public class StringUtil {
	static String lemmatizerResourceFolder ="lemmatiser";
	static EngLemmatiser lemmatiser = null;
	static{
		initialize();
	}
	public static void initialize()
	{	
		try {
			System.out.println("start loading the lemmatiser...");
			System.out.println(System.getProperty("user.dir"));
			lemmatiser = new EngLemmatiser(System.getProperty("user.dir")+"/lemmatiser", true, false);
			System.out.println("Lemmatizer loaded");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
//	static EngLemmatiser lemmatiser = new EngLemmatiser("/rnlp-stb/nlpdata/lemmatiser",	true, false);

	/**
	 * 
	 * @param inputString
	 * @return MD5 hash of given string
	 * @throws UnsupportedEncodingException
	 * @throws NoSuchAlgorithmException
	 */
	public static String getStringDigest(String inputString)
			throws UnsupportedEncodingException, NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(inputString.getBytes(), 0, inputString.length());

		return new BigInteger(1, md.digest()).toString(16);
	}

	/**
	 * Customized definition of stop words for word
	 * @param word
	 * @return
	 */
	public static List<String> customGoogleCommonWord = FileUtil.loadLineByLine
			("/home/azadeh/projects/drug-effect-ext/data/abbvie-experiments/google-comon-words.txt");
	public static boolean isStopWord(String word){
		boolean isStopWord = false;
		
		if(word.length() < 2
				||	Stopwords.isStopword(word)
				|| customGoogleCommonWord.contains(word)
				|| word.matches("\\W+")
				)
			isStopWord = true;
		
		return isStopWord;
	}
	public static String removeStopWords(String text){
		boolean isStopWord = false;
		String[] tokens = text.split(" ");
		String new_text = "";
		
		for (String token:tokens)
		{
			if(token.length() < 2
					||	Stopwords.isStopword(token)
					|| customGoogleCommonWord.contains(token)
					|| token.matches("\\W+")
					)
				continue;
			else
				new_text=new_text+" "+token;
			
		}
		
		return new_text.trim();
	}
	

	
	/**
	 * Porter stem
	 * @param word
	 * @return stemmed word
	 */
	public static String getWordPorterStem(String word)
	{
		PorterStemmer stemmer = new PorterStemmer();
		String stemmed_word = stemmer.stem(word).toLowerCase();
		return stemmed_word;
	}
	public static String prepareSQLString(String sqlString) {
		sqlString = sqlString.replace("\\", "\\\\").
			replace("'", "''").
			replace("%", "\\%").
			replace("_", "\\_");
		return sqlString;
	}

public static String castForRegex(String textContent) {
		
		return textContent.replace("\\","\\\\").replace("/","\\/").replace("*", "\\*").replace("+", "\\+").replace(".", "\\.").replace("?", "\\?")
			.replace(")", "\\)").replace("{", "\\{").replace("}", "\\}")
			.replace("(", "\\(").replace("[", "\\[").replace("]", "\\]").replace("%", "\\%");
	}
public static String decastRegex(String textContent) {
		
		return textContent.replace("\\\\","\\").replace("\\/","/").replace("\\*", "*").replace("\\+", "+").replace("\\.", ".").replace("\\?", "?")
			.replace("\\)", ")").replace("\\_", "_")
			.replace("\\{", "{").replace("\\}", "}").replace("\\(", "(").
			replace("\\[", "[").replace("\\]", "]").replace("\\%", "%");
	}
	
	public static String getTermByTermPorter(String phrase)
	{
		String[] words = phrase.split(" ");
		String rootString = "";
		for(int i=0;i<words.length;i++){
			rootString += StringUtil.getWordPorterStem(words[i])+" ";
		}
		return rootString.trim();
	}
	
	public static String compress(String text) {
		return text.replace(" ", "").replace(" ", "");
	}
	static HashMap<String, String> lemmaCache = new HashMap<String, String>();

	public static String getTermByTermWordnet(String phrase)
	{
		String[] words = phrase.split(" ");
		String rootString = "";
		for(int i=0;i<words.length;i++)
		{
			String lemma = lemmaCache.get(words[i]);
			if (words[i].equals(lemma)
					&& lemma.matches(".*ioning$"))
			{
				lemma=lemma.replaceAll("ioning$", "ion");
			}
			if(lemma == null)
			{
				lemma = lemmatiser.stem(words[i]);
				lemmaCache.put(words[i], lemma);
			}
			rootString = rootString.concat(lemma+" ");
		}
		
		return rootString.trim();
	}
	public static String getWordLemma(String word)
	{
		String word_lemma= word;

		word_lemma= lemmatiser.stem(word);
		return word_lemma;
	}

	public static String orderStringAlphabetically(String text) {
		String words[] =text.split(" ");
        String t;
        int n = words.length;
        int i,j,c;
        for (i=0; i<n-1; i++) 
        {
            for (j=i+1; j<n; j++) 
            {
                c = words[i].compareTo(words[j]);
                if (c >0) 
                {
                    t = words[i];
                    words[i] = words[j];
                    words[j] = t;       
                }   
            }
        }
        String ordered_words = "";
        for (i=0; i<n ;i++) 
        {
        	ordered_words +=words[i]+" ";
        }
		return ordered_words.trim();
	}

	public static String cleanString(String text) {
		String clean = text.replaceAll("[\\d[^\\w\\s]]+", " ").replaceAll("(\\s{2,})", " ").toLowerCase();
		return clean.trim();
	}
}
