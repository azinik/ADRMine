package LuceneManagerADR;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.spell.Dictionary;
import org.apache.lucene.search.spell.JaroWinklerDistance;
import org.apache.lucene.search.spell.LevensteinDistance;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.NGramDistance;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.search.spell.SuggestMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import rainbownlp.core.Setting;
import rainbownlp.util.StringUtil;

public class DidYouMeanIndexer {
	String correctText;
	public HashMap<String, String> lemma_correct_wrong_spell_map = new HashMap<>();

    public static void main (String[] args) throws IOException, ParseException
    {
    	DidYouMeanIndexer sc = new DidYouMeanIndexer();
    	String correct = sc.getTermByTermCorrectSpell("diarrhea");
    	System.out.println(correct);
    }
    public static void createSpellIndex(String spellIndexDirectoryPath,String sourceDirectoryPath) throws IOException {

        Directory sourceDirectory = FSDirectory.open(new File(sourceDirectoryPath));
        
        Directory spellIndexDirectory = FSDirectory.open(new File(spellIndexDirectoryPath));
        
        SpellChecker spellChecker = new SpellChecker(spellIndexDirectory);
        IndexReader reader = IndexReader.open(sourceDirectory);
        
       
        LuceneDictionary dictionary = new LuceneDictionary(reader, "content");
        
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_36, new StandardAnalyzer(Version.LUCENE_36,CharArraySet.EMPTY_SET));
        spellChecker.indexDictionary(dictionary, indexWriterConfig, true);
           
        spellChecker.close();
    }

    public static void search(String queryString) throws IOException, ParseException {
        long startTime = System.currentTimeMillis();
        IndexSearcher is = null;
        try {
        	
        	Directory indexDirectory = FSDirectory.open(new File(Setting.getValue("LuceneIndexFile")+"/adrProjectLexicon"));
			
			
			QueryParser qp = new QueryParser(Version.LUCENE_36, "content", new StandardAnalyzer(Version.LUCENE_36));
			
			Query query = qp.parse("content:"+queryString);
			 
			IndexReader reader = IndexReader.open(indexDirectory);
			
			IndexSearcher searcher = new IndexSearcher(reader);
           
            qp.setDefaultOperator(QueryParser.AND_OPERATOR);
            
            ScoreDoc[] hits = searcher.search(query,1).scoreDocs;
           
//            Hits hits = searcher.search(query);
            long endTime = System.currentTimeMillis();
            
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }
    static String luceneDir = System.getProperty("user.dir")+"/LuceneIndexes/EnglishSpellIndex";
    static Directory spell_directory;
    static IndexReader reader;
    static SpellChecker spellChecker;
    static{
    	 try {
			spell_directory = FSDirectory.open(new File(luceneDir));
			reader = IndexReader.open(spell_directory);
			spellChecker= new SpellChecker(spell_directory);
	        spellChecker.setStringDistance(new JaroWinklerDistance());
		} catch (IOException e) {
			//TODO
			System.out.println(e.getMessage());
		}
    }
	
	
    public static String getCorrectSpell(String wordForSuggestions ) throws IOException
    {
    	String correct_spell = wordForSuggestions;
    	
    	if (StringUtil.isStopWord(wordForSuggestions))
    	{
    		return correct_spell;
    	}
  
 
        
        if (!spellChecker.exist(wordForSuggestions))
        {
//        	String[] suggestions = spellChecker.suggestSimilar(wordForSuggestions, 1,reader,"content" ,SuggestMode.SUGGEST_WHEN_NOT_IN_INDEX);
        	String[] suggestions = spellChecker.suggestSimilar(wordForSuggestions, 5,0.7f);
        	if (suggestions!=null && suggestions.length>0) {
                 for (String word : suggestions) {
                	 if (!(word.startsWith(String.valueOf(wordForSuggestions.charAt(0)))) )
                	 {
                		 continue;
                	 }
                	 correct_spell = word;
                	 break;
//                	 System.out.println("suggestions found for word:"+correct_spell);
                 }
             }
             else {
                 System.out.println("No suggestions found for word:"+wordForSuggestions);
             }
        }
       
    	return correct_spell;
    }
    public String getTermByTermCorrectSpell(String phrase) throws IOException
    {
    	
    	phrase = phrase.toLowerCase();
    	
    	String[] words = phrase.split(" ");
		String correctSpellString = "";
		for(int i=0;i<words.length;i++)
		{
			
			String correct = getCorrectSpell(words[i]);
			lemma_correct_wrong_spell_map.put(StringUtil.getTermByTermWordnet(correct), StringUtil.getTermByTermWordnet(words[i]));
			correctSpellString = correctSpellString.concat(correct+" ");
		}
		
		correctText = correctSpellString.trim();
		//%%% This is an special handling for n't
		if (phrase.matches("n't"))
			return phrase;
		return correctText;
    }
    

    

  
}
