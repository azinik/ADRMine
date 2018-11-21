package edu.asu.diego.adrmine.utils;

import java.util.ArrayList;
import java.util.Arrays;

import rainbownlp.analyzer.sentenceclause.Clause;
import rainbownlp.analyzer.sentenceclause.SentenceClauseManager;
import rainbownlp.core.Artifact;
import rainbownlp.core.Phrase;
import rainbownlp.parser.DependencyLine;
import rainbownlp.util.HibernateUtil;
import rainbownlp.util.StanfordDependencyUtil;
import rainbownlp.util.StringUtil;


public class Negation {
	
	static ArrayList<String> modifier_rels = new ArrayList<String>(Arrays.asList("det","neg","amod","advmod","dep","nn","prep"));
	
	public static ArrayList<String> getNegationModifierRelations()
	{
		return modifier_rels;
	}
	public static boolean isWordNegated( Artifact word,Artifact sent) throws Exception
	{
		boolean is_negated =false;
		
		ArrayList<DependencyLine> sentDepLines =
				StanfordDependencyUtil.parseDepLinesFromString(sent.getStanDependency());
		ArrayList<String> mod_list = getNegationModifierRelations();
			
		//get the original word
		Integer word_offset = word.getWordIndex();
		
		ArrayList<String> previous_tokens = getNPrevTokens(word_offset,sent, 2);

		if (previous_tokens.contains("no") || previous_tokens.contains("not")
				|| previous_tokens.contains("hasnt")
				|| previous_tokens.contains("havent")
				|| previous_tokens.contains("cant")
				|| previous_tokens.contains("dont")
				|| previous_tokens.contains("didnt")
				|| previous_tokens.contains("hadnt")
				|| previous_tokens.contains("couldnt"))
		{
			is_negated = true;
			return is_negated;
		}
		
		//get next words TODO: this should be recursive and in a method
//		ArrayList<String> next_tokens = new ArrayList<>();
//		
//		Artifact nextWord = word.getNextArtifact();
//		if (nextWord != null)
//		{
//			next_tokens.add(nextWord.getContent().toLowerCase());
//			nextWord= nextWord.getNextArtifact();
//			if (nextWord != null)
//			{
//				next_tokens.add(nextWord.getContent().toLowerCase());
//			}
//		}
//		if (next_tokens.contains("no") || next_tokens.contains("not"))
//		{
//			is_negated = true;
//			return is_negated;
//		}	
		//To be compatible with stanford parser
		
		word_offset++;
		for(DependencyLine curLine:sentDepLines)
		{
			if (!mod_list.contains(curLine.relationName))
			{
				continue;
			}
			if (curLine.relationName.equals("neg") && curLine.firstOffset==word_offset)
			{
				is_negated = true;
				break;
			}
			else if (curLine.firstOffset==word_offset)
			{
				if (curLine.secondPart.equals("no") || curLine.secondPart.equals("any") ||
						curLine.secondPart.equals("not") || curLine.secondPart.equals("less"))
				{
					is_negated = true;
					break;
				}
			}

		}
	

		return is_negated;
	}
	public static boolean isWordNegatedExpanded(Artifact word,Artifact sent) throws Exception
	{
		boolean is_negated= false;
		is_negated = isWordNegated( word,sent);
		// Check the governor verb 
		if (!is_negated)
		{
			SentenceClauseManager clauseManager =
				new SentenceClauseManager(sent);
			Artifact gov_verb = getGovernorVerb(word,clauseManager);
			if (gov_verb != null &&
					isWordNegated(gov_verb, sent))
				is_negated = true;
		}
		return is_negated;
	}
	public static ArrayList<String>  getNPrevTokens(int word_index,Artifact sent,int howMany)
	{
		ArrayList<String> prev_tokens = new ArrayList<String>(); 
		Artifact target = sent.getChildByWordIndex(word_index);
		Artifact prev= target.getPreviousArtifact();
		while (howMany!=0 && prev!=null)
		{
			prev_tokens.add(prev.getContent().toLowerCase());
			howMany--;
			prev=prev.getPreviousArtifact();
		}
		return prev_tokens;
	}
	public static Artifact getGovernorVerb
			(Artifact pWord, SentenceClauseManager pClauseManager ) throws Exception
	{
		String gov_verb = null;
	
		// get sentence clauses
		Artifact head = pWord;
		SentenceClauseManager clauseManager=pClauseManager;
		
		Clause related_clause = clauseManager.clauseMap.get(head.getWordIndex()+1);
		Artifact gov_verb_artifact = null;
		if (related_clause!=null)
		{
			gov_verb = related_clause.clauseVerb.verbMainPart;
			gov_verb_artifact = 
				Artifact.findInstance(clauseManager.getRelatedSentence(), 
						related_clause.clauseVerb.offset-1);

			if (!gov_verb.matches(""))
			{
				if (!gov_verb_artifact.getPOS().startsWith("VB"))
				{
					gov_verb = null;
				}
			}
			
			
		}
		if (gov_verb ==null || gov_verb.equals("") )
		{
			gov_verb_artifact= calclateGovVerb(pWord);
		}
	
		
		return gov_verb_artifact;
	}
	public static Artifact calclateGovVerb(Artifact pWord) {
		Artifact gov_verb = null;
		String pos = pWord.getPOS();

		if (pos == null)
			return null;
		if (pos != null && (pos.matches("VB|VBD|VBN|VBP|VBZ")) )
		{
			gov_verb = pWord;
		}
		else
		{
			Artifact next = pWord.getNextArtifact();
			if (next != null && next.getPOS() != null && next.getPOS().startsWith("VB"))
			{
				Artifact next_verb = next.getNextArtifact();
				if (next_verb != null && next_verb.getPOS()!= null &&
						next_verb.getPOS().matches("VBD"))
				{
					gov_verb = next_verb;
				}
				else
				{
					gov_verb = next;
				}
			}
			else
			{
				Artifact prev = pWord.getPreviousArtifact();
				while (prev != null  && prev.getPOS() != null &&
						!prev.getPOS().matches("VB|VBD|VBN|VBP|VBZ") )
				{
					prev = prev.getPreviousArtifact();
				}
				if (prev != null && prev.getPOS().startsWith("VB"))
				{
					gov_verb = prev;
				}
			}
			//if still null
			if (gov_verb == null)
			{
				while (next != null  && next.getPOS() != null && 
						!next.getPOS().matches("VB|VBD|VBN|VBP|VBZ") )
				{
					next = next.getNextArtifact();
				}
				if (next != null && next.getPOS() != null && next.getPOS().startsWith("VB"))
				{
					Artifact next_verb = next.getNextArtifact();
					if (next_verb != null && next_verb.getPOS() !=null && next_verb.getPOS().matches("VBD"))
					{
						gov_verb = next_verb;
					}
					else
					{
						gov_verb = next;
					}
					
				}
			}
		}
		
		return gov_verb;
	}
}
