/**
 * 
 */
package edu.asu.diego.adrmine.features;

import java.util.List;

import LuceneManagerADR.ADRLuceneSearcher;
import LuceneManagerADR.DidYouMeanIndexer;
import rainbownlp.analyzer.sentenceclause.Clause;
import rainbownlp.analyzer.sentenceclause.SentenceClauseManager;
import rainbownlp.core.Artifact;
import rainbownlp.core.FeatureValuePair;

import rainbownlp.core.FeatureValuePair.FeatureName;

import rainbownlp.machineLearning.IFeatureCalculator;
import rainbownlp.machineLearning.MLExample;
import rainbownlp.machineLearning.MLExampleFeature;
import rainbownlp.util.HibernateUtil;
import rainbownlp.util.StringUtil;
import LuceneManagerADR.DidYouMeanIndexer;

/**
 * @author Azadeh
 * 
 */
public class TokenBasicFeatures implements IFeatureCalculator {
	
	
	public static void main (String[] args) throws Exception
	{
		

	}
	static DidYouMeanIndexer SpellSearcher;
	static{
		System.out.println("****** initializing spell corrector****");
		SpellSearcher= new DidYouMeanIndexer();
	}
	@Override
	public void calculateFeatures(MLExample exampleToProcess) throws Exception {
		
		
		
		//get related artifact
		Artifact relatedArtifact = exampleToProcess.getRelatedArtifact();
		String content = relatedArtifact.getContent();
		
		
		String lemma = content;
		
		if (lemma.matches("\\w+"))
		{
			lemma = StringUtil.getTermByTermWordnet(lemma);
		}
		
		FeatureValuePair contentFeatureOrig = FeatureValuePair.getInstance(
				FeatureName.TokenContent, lemma, "1");
		
		MLExampleFeature.setFeatureExample(exampleToProcess, contentFeatureOrig);
		

		//spell check and tokenize
		String clean_content =  StringUtil.cleanString(relatedArtifact.getContent());
		String[] content_tokens = clean_content.split(" ");
		for (String token: content_tokens)
		{
			
			String spell_checked = SpellSearcher.getTermByTermCorrectSpell(token);
			if (spell_checked.matches("\\w+"))
			{
				spell_checked = StringUtil.getTermByTermWordnet(spell_checked);
			}

			FeatureValuePair contentFeature = FeatureValuePair.getInstance(
					FeatureName.TokenContent, spell_checked, "1");
			
			MLExampleFeature.setFeatureExample(exampleToProcess, contentFeature);
		}
		
	
		///////////////////////
		//Previous
		//////////////////////
		
		Artifact prev_token =  relatedArtifact.getPreviousArtifact();
		String b_lemma = "<s>";
		String second_prev = "<s>";
		String b_lemma_processed = "<s>";
		String second_prev_processsed = "<s>";
		String third_b_lemma = "<s>";
		String third_b_processsed = "<s>";
		
		if (prev_token!=null)
		{
			b_lemma = prev_token.getContent();

			b_lemma_processed =SpellSearcher.getTermByTermCorrectSpell(b_lemma);
			if (b_lemma.matches("\\w+"))
			{
				b_lemma = StringUtil.getTermByTermWordnet(b_lemma);
				b_lemma_processed= StringUtil.getTermByTermWordnet(b_lemma_processed);
			}
			
			Artifact second_prev_artifact = prev_token.getPreviousArtifact();
			if (second_prev_artifact!=null)
			{
				second_prev = second_prev_artifact.getContent();

				second_prev_processsed =SpellSearcher.getTermByTermCorrectSpell(second_prev);
				if (second_prev.matches("\\w+"))
				{
					second_prev = StringUtil.getTermByTermWordnet(second_prev);
					second_prev_processsed = StringUtil.getTermByTermWordnet(second_prev_processsed);
				}
				
				
				//third Prev
				Artifact third_prev_artifact = second_prev_artifact.getPreviousArtifact();
				if (third_prev_artifact!=null)
				{
					third_b_lemma = third_prev_artifact.getContent();

					third_b_processsed =SpellSearcher.getTermByTermCorrectSpell(third_b_lemma);
					if (third_b_lemma.matches("\\w+"))
					{
						third_b_lemma = StringUtil.getTermByTermWordnet(third_b_lemma);
						third_b_processsed = StringUtil.getTermByTermWordnet(third_b_processsed);
					}
					
				}
			}
			
		}
		
		FeatureValuePair prev_contentFeature = FeatureValuePair.getInstance(
				FeatureName.PrevTokenContent, b_lemma, "1");
		
		MLExampleFeature.setFeatureExample(exampleToProcess, prev_contentFeature);
		
		//corrected spell
		FeatureValuePair prev_contentFeature_processed = FeatureValuePair.getInstance(
				FeatureName.PrevTokenContent, b_lemma_processed, "1");
		
		MLExampleFeature.setFeatureExample(exampleToProcess, prev_contentFeature_processed);
		// second prev
		FeatureValuePair second_prev_contentFeature = FeatureValuePair.getInstance(
				FeatureName.SecondPrevTokenContent, second_prev, "1");
		
		MLExampleFeature.setFeatureExample(exampleToProcess, second_prev_contentFeature);
		//correct spell
		FeatureValuePair second_prev_contentFeature_processed = FeatureValuePair.getInstance(
				FeatureName.SecondPrevTokenContent, second_prev_processsed, "1");
		
		MLExampleFeature.setFeatureExample(exampleToProcess, second_prev_contentFeature_processed);
		
//		Third prev
		
		FeatureValuePair third_prev_contentFeature = FeatureValuePair.getInstance(
				FeatureName.ThirdPrevTokenContent, third_b_lemma, "1");
		
		MLExampleFeature.setFeatureExample(exampleToProcess, third_prev_contentFeature);
		//correct spell
		FeatureValuePair third_prev_contentFeature_processed = FeatureValuePair.getInstance(
				FeatureName.ThirdPrevTokenContent, third_b_processsed, "1");
		
		MLExampleFeature.setFeatureExample(exampleToProcess, third_prev_contentFeature_processed);
		
		///////////////////////
		//After
		//////////////////////
		Artifact nextToken = relatedArtifact.getNextArtifact();
		
		String n_lemma = ".";
		String second_next_content = ".";
		String n_lemma_processed = ".";
		String second_next_processed = ".";
		String t_n_lemma = ".";
		String t_next_processed = ".";
		
		if (nextToken!=null)
		{
			n_lemma = nextToken.getContent();
			
//			FeatureValuePair next_contentFeature = FeatureValuePair.getInstance(
//					FeatureName.NextTokenContent, n_lemma, "1");
//				
//			MLExampleFeature.setFeatureExample(exampleToProcess, next_contentFeature);
				
			System.out.println("======>"+n_lemma);
			n_lemma_processed =SpellSearcher.getTermByTermCorrectSpell(n_lemma);
			
			if (n_lemma.matches("\\w+"))
			{
				n_lemma = StringUtil.getTermByTermWordnet(n_lemma);
				n_lemma_processed = StringUtil.getTermByTermWordnet(n_lemma_processed);
			}
			
			Artifact second_next_artifact = nextToken.getNextArtifact();
			if (second_next_artifact!=null)
			{
				second_next_content = second_next_artifact.getContent();
				
//				FeatureValuePair sn_next_contentFeature = FeatureValuePair.getInstance(
//						FeatureName.NextTokenContent, second_next_content, "1");
//					
//				MLExampleFeature.setFeatureExample(exampleToProcess, sn_next_contentFeature);
					
					
				second_next_processed =SpellSearcher.getTermByTermCorrectSpell(second_next_content);
				
				if (second_next_content.matches("\\w+"))
				{
					second_next_content = StringUtil.getTermByTermWordnet(second_next_content);
					second_next_processed = StringUtil.getTermByTermWordnet(second_next_processed);
				}
			
				//Third next
				Artifact third_next_artifact = second_next_artifact.getNextArtifact();
				if (third_next_artifact!=null)
				{
					t_n_lemma = third_next_artifact.getContent();
					
					
					t_next_processed =SpellSearcher.getTermByTermCorrectSpell(t_n_lemma);
					
					if (t_n_lemma.matches("\\w+"))
					{
						t_n_lemma = StringUtil.getTermByTermWordnet(t_n_lemma);
						t_next_processed = StringUtil.getTermByTermWordnet(t_next_processed);
					}
				
				}
			
			}
		
		}
		HibernateUtil.clearLoaderSession();	
		FeatureValuePair next_contentFeature = FeatureValuePair.getInstance(
			FeatureName.NextTokenContent, n_lemma, "1");
		
		MLExampleFeature.setFeatureExample(exampleToProcess, next_contentFeature);
		
		
		FeatureValuePair next_contentFeature_process = FeatureValuePair.getInstance(
				FeatureName.NextTokenContent, n_lemma_processed, "1");
			
			MLExampleFeature.setFeatureExample(exampleToProcess, next_contentFeature_process);
			
		//second next
		FeatureValuePair second_next_contentFeature = FeatureValuePair.getInstance(
				FeatureName.SecondNextTokenContent, second_next_content, "1");
			
		MLExampleFeature.setFeatureExample(exampleToProcess, second_next_contentFeature);
		
		//correct spell
		FeatureValuePair second_next_contentFeature_process = FeatureValuePair.getInstance(
				FeatureName.SecondNextTokenContent, second_next_processed, "1");
			
		MLExampleFeature.setFeatureExample(exampleToProcess, second_next_contentFeature_process);
		
		//Third next
		FeatureValuePair t_next_contentFeature = FeatureValuePair.getInstance(
				FeatureName.ThirdNextTokenContent, t_n_lemma, "1");
			
		MLExampleFeature.setFeatureExample(exampleToProcess, t_next_contentFeature);
			
		
		FeatureValuePair t_next_contentFeature_process = FeatureValuePair.getInstance(
				FeatureName.ThirdNextTokenContent, t_next_processed, "1");
			
		MLExampleFeature.setFeatureExample(exampleToProcess, t_next_contentFeature_process);
		
		HibernateUtil.clearLoaderSession();	
		//add also POS here 
		String pos = relatedArtifact.getPOS();
		
		if (pos == null)
		{
			pos = "UNK";
		}
		FeatureValuePair posFeature = FeatureValuePair.getInstance(
				FeatureName.POS, pos,"1");
		
		MLExampleFeature.setFeatureExample(exampleToProcess, posFeature);
//		
//	    //also calculate the dependency feature
//		String gov_verb =getGovernorVerb(relatedArtifact);
//		String gov_verb_processed = gov_verb;
//		
//		if (gov_verb == null)
//			gov_verb ="UNK";
//		
//		
//		gov_verb_processed =sc.getTermByTermCorrectSpell(gov_verb);
//		gov_verb_processed = StringUtil.getTermByTermWordnet(gov_verb_processed);
//		
//		//I made this commented to see keep inforrmation about tense
////		gov_verb = StringUtil.getTermByTermWordnet(gov_verb);
//		
//		FeatureValuePair govVerb = FeatureValuePair.getInstance
//				(FeatureName.GovVerb, gov_verb, "1");
//			
//		MLExampleFeature.setFeatureExample(exampleToProcess, govVerb);
//		
//		FeatureValuePair govVerb_p = FeatureValuePair.getInstance
//				(FeatureName.GovVerb, gov_verb_processed, "1");
//			
//		MLExampleFeature.setFeatureExample(exampleToProcess, govVerb_p);
		
		HibernateUtil.clearLoaderSession();		
	}

	public static String getGovernorVerb(Artifact token ) throws Exception
	{
		String gov_verb = null;
	
		// get sentence clauses
		Artifact head = token;
		SentenceClauseManager clauseManager;
		
		clauseManager = new SentenceClauseManager(token.getParentArtifact());
		
		Clause related_clause = clauseManager.clauseMap.get(head.getWordIndex()+1);
		
		if (related_clause!=null)
		{
			gov_verb = related_clause.clauseVerb.verbMainPart;

			if (!gov_verb.matches(""))
			{
				Artifact gov_verb_artifact = 
					Artifact.findInstance(clauseManager.getRelatedSentence(), 
							related_clause.clauseVerb.offset-1);
				if (!gov_verb_artifact.getPOS().startsWith("VB"))
				{
					gov_verb = null;
				}
				else
				{
//					pPhrase.setGovVerb(gov_verb_artifact);
//					HibernateUtil.save(pPhrase);
				}
			}
			
			
		}
		if (gov_verb ==null || gov_verb.equals("") )
		{
			Artifact ga= calclateGovVerb(head);
			if (ga != null)
			{
				gov_verb= ga.getContent();
				
			}
		}
		
//		if (gov_verb != null)
//		{
//			gov_verb = StringUtil.getWordLemma(gov_verb);
//		}
		
		return gov_verb;
	}
	public static Artifact calclateGovVerb(Artifact token) {
		Artifact gov_verb = null;
		String pos = token.getPOS();

		if (pos != null && (pos.matches("VB|VBD|VBN|VBP|VBZ") || pos.equals("VB-RP") || pos.equals("VBG-RP")|| pos.equals("VBN-JJ") || pos.equals("VB-IN") || pos.equals("VBN-IN"))
				|| (pos != null && (pos.equals("JJ-TO-VB") ||  pos.startsWith("VBG-TO"))))
		{
			gov_verb = token;
		}
		else
		{
			Artifact next = token.getNextArtifact();
			if (next != null && next.getPOS()!= null && next.getPOS().startsWith("VB"))
			{
				Artifact next_verb = next.getNextArtifact();
				if (next_verb != null && next_verb.getPOS()!= null &&  next_verb.getPOS().matches("VBD"))
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
				Artifact prev = token.getPreviousArtifact();
				while (prev != null  && prev.getPOS() != null && 
						!prev.getPOS().matches("VB|VBD|VBN|VBP|VBZ") )
				{
					prev = prev.getPreviousArtifact();
				}
				if (prev != null && prev.getPOS() != null &&  prev.getPOS().startsWith("VB"))
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
					if (next_verb.getPOS().matches("VBD"))
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
