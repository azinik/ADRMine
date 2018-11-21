package preprocessutils;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.awt.image.RescaleOp;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;


// Notes: This class is resposnible for parsing the HTML review pages and extracting information to save in MYSQL db.
// TODO: try other types of DB

public class Preprocess {
	
	private HashMap<Integer, String> sentenceIndexMap;
	
	private String tokenizedText="";
    private HashMap<Integer, List<HasWord>> sentTokensMap = 
 		new HashMap<Integer, List<HasWord>>();
    
    private HashMap<Integer, HashMap<Integer, Integer>> tokenStartCharMap = 
 		new HashMap<Integer, HashMap<Integer,Integer>>();
	
    private HashMap<Integer, HashMap<Integer, Integer>> tokenEndtCharMap = 
 		new HashMap<Integer, HashMap<Integer,Integer>>();
	public Preprocess(String text)
	{
		preprocessText(text);
	}

	public void preprocessText(String text)
	{			
		Reader reader = new StringReader(text);
		
	    DocumentPreprocessor dp = new DocumentPreprocessor(reader);
	    
	    HashMap<Integer, String> sentences =  new HashMap<Integer, String>();
	    	    
 	    Iterator<List<HasWord>> it = dp.iterator();
 	    Integer sent_index = 0;
 	    
 	    HashMap<Integer, List<HasWord>> sent_tokens = 
 	    		new HashMap<Integer, List<HasWord>>();
 	    
	    while (it.hasNext()) {
	       StringBuilder sentenceSb = new StringBuilder();
	       
	       List<HasWord> sentence = it.next();
	       for (HasWord token : sentence) {
	    	   
	          if(sentenceSb.length()>=1) {
	             sentenceSb.append(" ");
	          }
	          sentenceSb.append(token);
	         
	       }
	       sentences.put(sent_index,sentenceSb.toString());
	       sent_tokens.put(sent_index, sentence);
	       sent_index++;
	       
	    }
	    setSentenceIndexMap(sentences);
	    for(String sentence:sentences.values()) {

	       setTokenizedText(getTokenizedText() + sentence +" ");
	    }
	    setTokenizedText(getTokenizedText().replace(" $", "").trim());
	    setSentTokensMap(sent_tokens);
	    setTokensCharIndex();
	}

	public void setTokenizedText(String tokenizedText) {
		this.tokenizedText = tokenizedText;
	}
	public String getTokenizedText() {
		return tokenizedText;
	}

	public void setSentTokensMap(HashMap<Integer, List<HasWord>> sentTokensMap) {
		this.sentTokensMap = sentTokensMap;
	}
	public HashMap<Integer, List<HasWord>> getSentTokensMap() {
		return sentTokensMap;
	}
	
	public void setTokensCharIndex()
	{

		HashMap<Integer, HashMap<Integer, Integer>> startCharOffsets = 
			new HashMap<Integer, HashMap<Integer,Integer>>();
		
		HashMap<Integer, HashMap<Integer, Integer>> endCharOffsets = 
			new HashMap<Integer, HashMap<Integer,Integer>>();
		
		int currentCharOffset=0;
		for (Integer sent_index:sentTokensMap.keySet())
		{
			List<HasWord> tokens = sentTokensMap.get(sent_index);
			
			HashMap<Integer, Integer> token_start_char_map =  new HashMap<Integer, Integer>();
			HashMap<Integer, Integer> token_end_char_map =  new HashMap<Integer, Integer>();
			
			for(int i=0;i<tokens.size();i++)
			{
				String token = tokens.get(i).toString();
				token_start_char_map.put(i, currentCharOffset);
				token_end_char_map.put(i, currentCharOffset+token.length()-1);
				
				currentCharOffset+=token.length()+1;
			}
			startCharOffsets.put(sent_index, token_start_char_map);
			endCharOffsets.put(sent_index, token_end_char_map);
			
		}
		setTokenStartCharMap(startCharOffsets);
		setTokenEndtCharMap(endCharOffsets);

	}
	public int getTokenStartCharIndex(Integer sentOffset, Integer charIndex)
	{
		int startChar = 
		  tokenStartCharMap.get(sentOffset).get(charIndex);
		return startChar;
		
	}
	public int getTokenEndCharIndex(Integer sentOffset, Integer charIndex)
	{
		int endChar = 
		  getTokenEndtCharMap().get(sentOffset).get(charIndex);
		return endChar;
		
	}
	public void setTokenStartCharMap(HashMap<Integer, HashMap<Integer, Integer>> tokenStartCharMap) {
		this.tokenStartCharMap = tokenStartCharMap;
	}
	public HashMap<Integer, HashMap<Integer, Integer>> getTokenStartCharMap() {
		return tokenStartCharMap;
	}
	public void setTokenEndtCharMap(HashMap<Integer, HashMap<Integer, Integer>> tokenEndtCharMap) {
		this.tokenEndtCharMap = tokenEndtCharMap;
	}
	public HashMap<Integer, HashMap<Integer, Integer>> getTokenEndtCharMap() {
		return tokenEndtCharMap;
	}
	public void setSentenceIndexMap(HashMap<Integer, String> sentenceIndexMap) {
		this.sentenceIndexMap = sentenceIndexMap;
	}
	public HashMap<Integer, String> getSentenceIndexMap() {
		return sentenceIndexMap;
	}


}
