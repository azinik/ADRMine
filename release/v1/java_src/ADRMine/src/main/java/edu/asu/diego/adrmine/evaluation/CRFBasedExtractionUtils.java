package edu.asu.diego.adrmine.evaluation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.asu.diego.adrmine.classification.TokenSequenceExampleBuilder;
import rainbownlp.core.Artifact;
import rainbownlp.core.Phrase;
import rainbownlp.machineLearning.MLExample;
import rainbownlp.util.FileUtil;
import rainbownlp.util.StringUtil;

public class CRFBasedExtractionUtils {
	public static String experimentgroup = TokenSequenceExampleBuilder.ExperimentGroupADRConcepts;
//	public statiSc String experimentgroupLexiconCand = LexiconCandidateSVMExampleBuilder.ExperimentGroupADRConceptsLexiconCandidates;
	public enum TargetSemanticType{
		ADR,
		Indication,
		//healthRelated
		ADR_Indication
	}

	public static void main (String args[])
	{

	}
	
	public static List<String> getCRFExtractedConceptsForSent(Artifact sent, String experimentGroup, TargetSemanticType targetSemanticType,
			boolean forTrain)
	{
		List<String> extracted = new ArrayList<String>();
		
		List<Phrase> extractedPhrases =getCRFExtractedConceptPhrasesForSent(sent, experimentGroup, targetSemanticType,forTrain);
		for (Phrase p:extractedPhrases)
		{
			extracted.add(p.getPhraseContent());
		}
		
		return extracted;
	}
	public static List<String> getCRFExtractedADRsForSent(Artifact sent, String experimentGroup)
	 {
		List<String> found_phrases = new ArrayList<>();
		List<MLExample> sent_related_examples = MLExample.getTokenExamplesBySent(sent,experimentGroup,false); 
		
		if (sent_related_examples == null || sent_related_examples.isEmpty())
		{
			return found_phrases;
		}
		HashMap<Artifact, Integer> token_predicted_class_map = new HashMap<>();

		for (MLExample example:sent_related_examples)
		{
			Integer predicted = example.getPredictedClass();
			token_predicted_class_map.put(example.getRelatedArtifact(), predicted);
		}
		
		List<Artifact> sent_child_tokens = sent.getChildsArtifact();
		List<Artifact> analysed_tokens = new ArrayList<>();
		
		for(Artifact child_token :sent_child_tokens)
		{
			if (analysed_tokens.contains(child_token)) continue;
			
			Integer predicted = token_predicted_class_map.get(child_token);
			if (predicted ==null)
			{
				analysed_tokens.add(child_token);
				continue;
			}
			//the commented section is for health related
//			if (predicted ==1 || predicted ==3)
			if (predicted ==1)
			{
				analysed_tokens.add(child_token);
				// build the phrase
				Artifact start_token = child_token;
				Artifact end_token = child_token;
				
				Artifact current = start_token;
				
				Artifact nextArtifact = start_token.getNextArtifact();
				Integer predicted_next = token_predicted_class_map.get(nextArtifact);
				
//				while (predicted_next != null && (predicted_next ==2 || predicted_next ==4))
				while (predicted_next != null && predicted_next ==2)
				{
					current = nextArtifact;
					analysed_tokens.add(current);
					nextArtifact = current.getNextArtifact();
					predicted_next = token_predicted_class_map.get(nextArtifact);
				}
				end_token = current;
				String found_adr ="";
				Artifact cur_found = start_token;
				while (!cur_found.equals(end_token))
				{
					found_adr += " "+cur_found.getContent();
					cur_found = cur_found.getNextArtifact();
				}
				found_adr += " "+end_token.getContent();
				found_adr= found_adr.trim();
				found_phrases.add(found_adr);
			}
			// It reaches here if p==2 but previous is not 1 or 2
//			if (predicted ==2 || predicted ==4)
			if (predicted ==2 )
			{
				Artifact previous = child_token.getPreviousArtifact();
				if (previous != null)
				{
					Artifact start_token=child_token;
					Integer predicted_prev = token_predicted_class_map.get(previous);
					if (predicted_prev ==null)
					{
						start_token = previous;
					}
				}
				Artifact start_token = child_token;
				Artifact end_token = start_token;
				Artifact current = start_token;
				Artifact nextArtifact = child_token.getNextArtifact();
				Integer predicted_next = token_predicted_class_map.get(nextArtifact);
				//while (predicted_next != null && (predicted_next ==2 || predicted_next ==4))
				while (predicted_next != null && (predicted_next ==2))
				{
					current = nextArtifact;
					analysed_tokens.add(current);
					nextArtifact = current.getNextArtifact();
					predicted_next = token_predicted_class_map.get(nextArtifact);
				}
				end_token = current;
				
				String found_adr ="";
				Artifact cur_found = start_token;
				while (cur_found != end_token)
				{
					found_adr += " "+cur_found.getContent();
					cur_found = cur_found.getNextArtifact();
				}
				found_adr += " "+end_token.getContent();
				found_adr= found_adr.trim();
				found_phrases.add(found_adr);
			}
		}

		
		return found_phrases;
		 
	 }

	public static List<Phrase> getCRFExtractedConceptPhrasesForSent(Artifact sent, String experimentGroup, TargetSemanticType targetSemanticType,
			boolean forTrain)
	 {
		List<Phrase> found_phrases = new ArrayList<Phrase>();
		List<MLExample> sent_related_examples = MLExample.getTokenExamplesBySent(sent,experimentGroup,forTrain); 
		
		if (sent_related_examples == null || sent_related_examples.isEmpty())
		{
			return found_phrases;
		}
		HashMap<Artifact, Integer> token_predicted_class_map = new HashMap<Artifact, Integer>();

		for (MLExample example:sent_related_examples)
		{
			Integer predicted = example.getPredictedClass();
			token_predicted_class_map.put(example.getRelatedArtifact(), predicted);
		}
		
		List<Artifact> sent_child_tokens = sent.getChildsArtifact();
		List<Artifact> analysed_tokens = new ArrayList<Artifact>();
		
		for(Artifact child_token :sent_child_tokens)
		{
			if (analysed_tokens.contains(child_token)) continue;
			
			Integer predicted = token_predicted_class_map.get(child_token);
			if (predicted ==null)
			{
				analysed_tokens.add(child_token);
				continue;
			}
			if (targetSemanticType.equals(TargetSemanticType.ADR))
			{
				if (predicted ==1)
				{
					
					analysed_tokens.add(child_token);
					// build the phrase
					Artifact start_token = child_token;
					Artifact end_token = child_token;
					
					Artifact current = start_token;
					
					Artifact nextArtifact = start_token.getNextArtifact();
					Integer predicted_next = token_predicted_class_map.get(nextArtifact);
					
					while (predicted_next != null && predicted_next ==2)
					{
						current = nextArtifact;
						analysed_tokens.add(current);
						nextArtifact = current.getNextArtifact();
						predicted_next = token_predicted_class_map.get(nextArtifact);
					}
					end_token = current;

					String found_adr ="";
					Artifact cur_found = start_token;
					while (!cur_found.equals(end_token))
					{
						found_adr += " "+cur_found.getContent();
						cur_found = cur_found.getNextArtifact();
					}
					found_adr += " "+end_token.getContent();
					found_adr= found_adr.trim();
					Phrase extracted_phrase =Phrase.getInstance(found_adr, start_token, end_token,"Extracted");
					
					found_phrases.add(extracted_phrase);
				}
				// It reaches here if p==2 but previous is not 1 or 2
				else if (predicted ==2 )
				{
					Artifact previous = child_token.getPreviousArtifact();
					if (previous != null)
					{
						Artifact start_token=child_token;
						Integer predicted_prev = token_predicted_class_map.get(previous);
//						TODO//What is this?
						if (predicted_prev ==null)
						{
							start_token = previous;
						}
					}
					Artifact start_token = child_token;
					Artifact end_token = start_token;
					Artifact current = start_token;
					Artifact nextArtifact = child_token.getNextArtifact();
					Integer predicted_next = token_predicted_class_map.get(nextArtifact);
					//while (predicted_next != null && (predicted_next ==2 || predicted_next ==4))
					while (predicted_next != null && (predicted_next ==2))
					{
						current = nextArtifact;
						analysed_tokens.add(current);
						nextArtifact = current.getNextArtifact();
						predicted_next = token_predicted_class_map.get(nextArtifact);
					}
					end_token = current;
					
					String found_adr ="";
					Artifact cur_found = start_token;
					while (cur_found != end_token)
					{
						found_adr += " "+cur_found.getContent();
						cur_found = cur_found.getNextArtifact();
					}
					found_adr += " "+end_token.getContent();
					found_adr= found_adr.trim();
					Phrase extracted_phrase =Phrase.getInstance(found_adr, start_token, end_token,"Extracted");
					
					found_phrases.add(extracted_phrase);
				}
			}
			//health related
			else if (targetSemanticType.equals(TargetSemanticType.ADR_Indication))
			{
				//the commented section is for health related
				if (predicted ==1 || predicted ==3)
				{
					analysed_tokens.add(child_token);
					// build the phrase
					Artifact start_token = child_token;
					Artifact end_token = child_token;
					
					Artifact current = start_token;
					
					Artifact nextArtifact = start_token.getNextArtifact();
					Integer predicted_next = token_predicted_class_map.get(nextArtifact);
					
					while (predicted_next != null && (predicted_next ==2 || predicted_next ==4))
					{
						current = nextArtifact;
						analysed_tokens.add(current);
						nextArtifact = current.getNextArtifact();
						predicted_next = token_predicted_class_map.get(nextArtifact);
					}
					end_token = current;
					String found_adr ="";
					Artifact cur_found = start_token;
					while (!cur_found.equals(end_token))
					{
						found_adr += " "+cur_found.getContent();
						cur_found = cur_found.getNextArtifact();
					}
					found_adr += " "+end_token.getContent();
					found_adr= found_adr.trim();
					Phrase extracted_phrase =Phrase.getInstance(found_adr, start_token, end_token,"Extracted");
					
					found_phrases.add(extracted_phrase);

				}
				// It reaches here if p==2 but previous is not 1 or 2
				if (predicted ==2 || predicted ==4)
				{
					Artifact previous = child_token.getPreviousArtifact();
					if (previous != null)
					{
						Artifact start_token=child_token;
						Integer predicted_prev = token_predicted_class_map.get(previous);
						if (predicted_prev ==null)
						{
							start_token = previous;
						}
					}
					Artifact start_token = child_token;
					Artifact end_token = start_token;
					Artifact current = start_token;
					Artifact nextArtifact = child_token.getNextArtifact();
					Integer predicted_next = token_predicted_class_map.get(nextArtifact);
					//while (predicted_next != null && (predicted_next ==2 || predicted_next ==4))
					while (predicted_next != null && (predicted_next ==2))
					{
						current = nextArtifact;
						analysed_tokens.add(current);
						nextArtifact = current.getNextArtifact();
						predicted_next = token_predicted_class_map.get(nextArtifact);
					}
					end_token = current;
					
					String found_adr ="";
					Artifact cur_found = start_token;
					while (cur_found != end_token)
					{
						found_adr += " "+cur_found.getContent();
						cur_found = cur_found.getNextArtifact();
					}
					found_adr += " "+end_token.getContent();
					found_adr= found_adr.trim();
					Phrase extracted_phrase =Phrase.getInstance(found_adr, start_token, end_token,"Extracted");
					
					found_phrases.add(extracted_phrase);
				}
			}
			else if (targetSemanticType.equals(TargetSemanticType.Indication))
			{
				//the commented section is for health related
//				if (predicted ==1 || predicted ==3)
				if (predicted ==3)
				{
					analysed_tokens.add(child_token);
					// build the phrase
					Artifact start_token = child_token;
					Artifact end_token = child_token;
					
					Artifact current = start_token;
					
					Artifact nextArtifact = start_token.getNextArtifact();
					Integer predicted_next = token_predicted_class_map.get(nextArtifact);
					
//					while (predicted_next != null && (predicted_next ==2 || predicted_next ==4))
					while (predicted_next != null && predicted_next ==4)
					{
						current = nextArtifact;
						analysed_tokens.add(current);
						nextArtifact = current.getNextArtifact();
						predicted_next = token_predicted_class_map.get(nextArtifact);
					}
					end_token = current;
					String found_adr ="";
					Artifact cur_found = start_token;
					while (!cur_found.equals(end_token))
					{
						found_adr += " "+cur_found.getContent();
						cur_found = cur_found.getNextArtifact();
					}
					found_adr += " "+end_token.getContent();
					found_adr= found_adr.trim();
					Phrase extracted_phrase =Phrase.getInstance(found_adr, start_token, end_token,"Extracted");
					
					found_phrases.add(extracted_phrase);
				}
				// It reaches here if p==2 but previous is not 1 or 2
//				if (predicted ==2 || predicted ==4)
				if (predicted ==4 )
				{
					Artifact previous = child_token.getPreviousArtifact();
					if (previous != null)
					{
						Artifact start_token=child_token;
						Integer predicted_prev = token_predicted_class_map.get(previous);
						if (predicted_prev ==null)
						{
							start_token = previous;
						}
					}
					Artifact start_token = child_token;
					Artifact end_token = start_token;
					Artifact current = start_token;
					Artifact nextArtifact = child_token.getNextArtifact();
					Integer predicted_next = token_predicted_class_map.get(nextArtifact);
					//while (predicted_next != null && (predicted_next ==2 || predicted_next ==4))
					while (predicted_next != null && ( predicted_next ==4))
					{
						current = nextArtifact;
						analysed_tokens.add(current);
						nextArtifact = current.getNextArtifact();
						predicted_next = token_predicted_class_map.get(nextArtifact);
					}
					end_token = current;
					
					String found_indication ="";
					Artifact cur_found = start_token;
					while (cur_found != end_token)
					{
						found_indication += " "+cur_found.getContent();
						cur_found = cur_found.getNextArtifact();
					}
					found_indication += " "+end_token.getContent();
					found_indication= found_indication.trim();
					Phrase extracted_phrase =Phrase.getInstance(found_indication, start_token, end_token,"Extracted");
					
					found_phrases.add(extracted_phrase);
				}
			}

		}	
		
		return found_phrases;
		 
	 }
	public static  HashMap<Artifact, List<String>> getCRFExtractedADRs(List<Artifact> testSentences,boolean forTrain)
	{
		HashMap<Artifact, List<String>> sent_extracted_ADR_map = new HashMap<Artifact, List<String>>();	
		
		//Then we need to group them as phrases
		//Then compare with gold standartd
		for (Artifact sent:testSentences)
		{
			List<String> found_adrs =
					getCRFExtractedConceptsForSent(sent,TokenSequenceExampleBuilder.ExperimentGroupADRConcepts,TargetSemanticType.ADR,forTrain);			
			sent_extracted_ADR_map.put(sent, found_adrs);
		}
		return sent_extracted_ADR_map;
		
	}

	public static String getTextIdFromFilePath(String file_path)
	{
		String text_id = "";
//		/home/azadeh/projects/java/drug-effect-ext/data/off-label-IndivTextFiles-zyprexa/287.txt
		String pattern = ".*\\/(.*)\\.txt$";
	    Pattern p = Pattern.compile(pattern);
	    Matcher m = p.matcher(file_path);
	    if (m.matches())
	    {
	    	text_id = m.group(1);
	    }
		return text_id;
	}
}
