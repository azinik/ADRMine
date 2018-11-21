package edu.asu.diego.adrmine.utils;

import java.io.IOException;
import java.util.ArrayList;

import java.util.List;

import LuceneManagerADR.ADRLuceneSearcher;
import LuceneManagerADR.DidYouMeanIndexer;
import LuceneManagerADR.SlidingWindow;
import edu.asu.diego.adrmine.core.ADRLexiconEntry;
import rainbownlp.core.Artifact;

import rainbownlp.core.Setting;

import rainbownlp.util.StringUtil;

public class LexiconBasedUtils {
	public static void main (String[] args) throws org.apache.lucene.queryParser.ParseException, IOException
	{
		boolean is_in_lex = isTokenInLexicon("nausia",Setting.getValue("ADRTokenizedLexiconLuneceIndex"));
	}
	// This method checks some constraint and decide whether the found lexicon entry is related to the sentence (window content) or not 
	public static boolean validateLexiconConceptForWindow(String window_content, ADRLexiconEntry lex_entry)
	{
		boolean is_valid= true;
//		String stemed_window = StringUtil.getTermByTermWordnet(window_content);
//		String lex_entry_no_sw = StringUtil.removeStopWords(lex_entry.getContent()).trim();
		
		String stemed_window = window_content;
		String lex_entry_no_sw = lex_entry.getContent().trim();
		
		stemed_window = StringUtil.cleanString(stemed_window);
		//List<String> stemed_window_array = Arrays.asList(stemed_window.split(" "));		
		lex_entry_no_sw = StringUtil.cleanString(lex_entry_no_sw);
		
		String stemed_lex_entry = StringUtil.getTermByTermWordnet(lex_entry_no_sw);
		String[] included_tokens = stemed_lex_entry.trim().split(" ");
		for (String token: included_tokens)
		{
			if (token.isEmpty() || !stemed_window.matches(".*"+token+".*"))
			//if (token.isEmpty() || !stemed_window_array.contains(token))
			{
				is_valid=false;
				break;
			}
		}
		
		return is_valid;
	}
	public static List<SlidingWindow> getSlidingWindows(Artifact sent, int window_size)
	{
		List<SlidingWindow> sliding_phrases = new ArrayList<>();
		
		List<Artifact> childs = sent.getChildsArtifact();
		
		for (int i=0; i<childs.size();i++)
		{
			
			Artifact start = childs.get(i);
			int end_offset = i +window_size-1;
			
			if (i+window_size>=childs.size())
			{
				end_offset = childs.size()-1;
				
				Artifact end = childs.get(end_offset);
				SlidingWindow sw= new SlidingWindow(start, end);
				sliding_phrases.add(sw);
				break;
			}
			else
			{
				Artifact end = childs.get(end_offset);
				SlidingWindow sw= new SlidingWindow(start, end);
				sliding_phrases.add(sw);
			}
			
		
		}
		
		return sliding_phrases;
	}
	public static boolean isTokenInLexicon(String token,String indexDirectory) throws org.apache.lucene.queryParser.ParseException, IOException
	{
		boolean is_in_lexicon = false;

		DidYouMeanIndexer sc =new DidYouMeanIndexer();
		String correct_spell = sc.getTermByTermCorrectSpell(token);
		String lemma  = StringUtil.getTermByTermWordnet(correct_spell);
		
		is_in_lexicon =ADRLuceneSearcher.isPhraseInIndex(lemma,5,indexDirectory);
		
		return is_in_lexicon;
	}

}
