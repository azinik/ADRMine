package edu.asu.diego.adrmine.evaluation;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.print.attribute.HashAttributeSet;

import org.apache.lucene.queryParser.ParseException;


import LuceneManagerADR.ADRLuceneSearcher;
import edu.asu.diego.adrmine.classification.TokenSequenceExampleBuilder;
import edu.asu.diego.adrmine.classification.TokenSequenceExampleBuilder.TokenLabel;
import edu.asu.diego.adrmine.evaluation.CRFBasedExtractionUtils.TargetSemanticType;
import preprocessutils.StanfordParser;
import rainbownlp.core.Artifact;
import rainbownlp.core.Phrase;
import rainbownlp.machineLearning.MLExample;
import rainbownlp.util.FileUtil;
import rainbownlp.util.StringUtil;

public class MainConceptExtractionEvaluation {
	public static String experimentgroup = TokenSequenceExampleBuilder.ExperimentGroupADRConcepts;
	private boolean conbineWithLexiconCandClasses = false;
	public static  int  countFP=0;
	public static int  countFN=0;
	public static boolean applyPostProcessingRules= false;
	public static boolean tokenizeExpectedADRs = true;
	public static StanfordParser parser = new StanfordParser();
//	Setting.getValue("LuceneIndexFile")+"/adrProjectLexicon"
	public static String luceneIndexPath = "/home/azadeh/projects/drug-effect-ext/data/LuceneIndexes/abbvie/trimmed/verbatim-google-stop-removed/index";
	
	public enum ExtractionMethod {
		LuceneSearchLexicon, SemVecSVM, CRF, SVMCRF,MetaMap,CRFSemanticType,SVMSemanticType,SemVecCRF
	}

	public static void main (String args[]) throws Exception
	{
		
		// This includes all tokens
		List<MLExample> testExamples = 
				MLExample.getAllExamples(experimentgroup, false);
		

		MainConceptExtractionEvaluation ev = new MainConceptExtractionEvaluation();
		
//		ev.evaluateExtractedPhrases(testExamples,ExtractionMethod.SVMSemanticType,"ADR",false);
//		ev.evaluateExtractedPhrases(testExamples,MainConceptExtractionEvaluation.ExtractionMethod.CRF,"ADR&Indication",false);
//		ev.evaluateExtractedPhrases(testExamples,MainConceptExtractionEvaluation.ExtractionMethod.SVMSemanticType,"ADR",false);
//		ev.evaluateExtractedPhrases(testExamples,MainConceptExtractionEvaluation.ExtractionMethod.LuceneSearchLexicon,"ADR",false);
		
//		ev.evaluateExtractedPhrases(testExamples,ExtractionMethod.SVMSemanticType,"Indication",false);
		ev.evaluateExtractedPhrases(testExamples,ExtractionMethod.CRF,"ADR",false,experimentgroup);
	}
	
	public static void evaluateExtractedPhrases(List<MLExample> testExamples,ExtractionMethod method,String semanticType,
			boolean forTrain,String corpusName) throws Exception
	{
		List<String> logFileLinesFp = new ArrayList<>();
		List<String> logFileLinesFpFN = new ArrayList<>();
		List<String> logFileLinestp = new ArrayList<>();
		List<String> logFileLinesfn = new ArrayList<>();

				
		List<Artifact> testSentences = new ArrayList<>();
//		List<Artifact> testSentences = Artifact.listByType(Artifact.Type.Sentence);
		Evaluation eval = new Evaluation();
		int tp;
		//generating the test sentences
		for (MLExample test_e:testExamples)
		{
			Artifact related_sent = test_e.getRelatedArtifact().getParentArtifact();
			if (!testSentences.contains(related_sent))
			{
				testSentences.add(related_sent);
			}
			if (semanticType.equals("ADR") &&
					(test_e.getExpectedClass() != TokenLabel.BADR.ordinal() 
					&& test_e.getExpectedClass() != TokenLabel.IADR.ordinal())
					&& (test_e.getPredictedClass() != TokenLabel.BADR.ordinal()
					&& test_e.getPredictedClass() != TokenLabel.IADR.ordinal()))
			{
				eval.addTN();
			}
		}
		
		for (Artifact sent:testSentences)
		{
			ArrayList<String> tps = new ArrayList<String>();
			ArrayList<String> fps = new ArrayList<String>();
			ArrayList<String> fns = new ArrayList<String>();

			List<Phrase> expected_adrs= new ArrayList<>();
			List<Phrase> expected_indications = new ArrayList<>();
			List<Phrase> expected_concepts= new ArrayList<>();
			List<Phrase> found_concepts = new ArrayList<>();
			//This is the gold standard
			if (semanticType.equals("ADR"))
				//expected_adrs = getExpectedConcepts(sent, TargetSemanticType.ADR);
				expected_adrs = getExpectedConceptPhrases(sent,TargetSemanticType.ADR);
			else if (semanticType.equals("Indication"))
			{
//				expected_indications = getExpectedConcepts(sent,TargetSemanticType.Indication);
				expected_indications = getExpectedConceptPhrases(sent,TargetSemanticType.Indication);
			}
			else if (semanticType.equals("ADR&Indication"))
			{
//				expected_concepts   = getExpectedConcepts(sent,TargetSemanticType.ADR_Indication);
				expected_concepts   = getExpectedConceptPhrases(sent,TargetSemanticType.ADR_Indication);
			}

			List<Phrase> found_adrs = new ArrayList<>();
			List<Phrase> found_indications = new ArrayList<>();
			
			if (method.equals(ExtractionMethod.CRF))
			{
				
				if (semanticType.equals("ADR"))
				{
					// This list keeps what the CRF has extracted for the sent
					found_adrs = CRFBasedExtractionUtils.getCRFExtractedConceptPhrasesForSent
							(sent,corpusName,TargetSemanticType.ADR,forTrain);
					
				}
				else if (semanticType.equals("Indication"))
				{
					found_indications = CRFBasedExtractionUtils.getCRFExtractedConceptPhrasesForSent
							(sent,corpusName,
							TargetSemanticType.Indication,forTrain);
				}
				else if (semanticType.equals("ADR&Indication"))
				{
					found_concepts = CRFBasedExtractionUtils.getCRFExtractedConceptPhrasesForSent(sent,TokenSequenceExampleBuilder.ExperimentGroupADRConcepts,
							TargetSemanticType.ADR_Indication,forTrain);
				}
			}

			if (semanticType.equals("ADR"))
			{
				List<String> expectedPhraseCont = new ArrayList<>();
				
				List<String> extractedPhraseCont= new ArrayList<>();
				for (Phrase p:expected_adrs)
				{
					expectedPhraseCont.add(p.getPhraseContent());
				}				
				
				for (Phrase p:found_adrs)
				{
					extractedPhraseCont.add(p.getPhraseContent());
					
				}
//				eval = evaluateConcetpExtraction(sent, expectedPhraseCont, extractedPhraseCont, tps, fps, fns, eval);
				eval = evaluateConcetpExtraction(sent, expected_adrs, found_adrs, tps, fps, fns, eval);
			}

			System.out.println(sent.getAssociatedFilePath());
		}
		eval.getEvaluation();
//		FileUtil.createFile("/tmp/TPresultslog.txt", logFileLinestp);
//		
//		FileUtil.createFile("/tmp/FPInstances.txt", logFileLinesFp);
//		FileUtil.createFile("/tmp/FNInstances.txt", logFileLinesfn);
//		FileUtil.createFile("/tmp/errorLog-FP-FN.txt", logFileLinesFpFN);
	}
	public static int CRFCount=0;
	
//	public static Evaluation evaluateConcetpExtraction
//	(Artifact sent, List<String> expectedPhraseCont, List<String> extractedPhraseCont,ArrayList<String> tps,
//			ArrayList<String> fps,ArrayList<String> fns,Evaluation eval) throws UnsupportedEncodingException, FileNotFoundException
	public static Evaluation evaluateConcetpExtraction
	(Artifact sent, List<Phrase> expectedPhrases, List<Phrase> extractedPhrases,ArrayList<String> tps,
			ArrayList<String> fps,ArrayList<String> fns,Evaluation eval) throws UnsupportedEncodingException, FileNotFoundException
	{
		List<String> expectedPhraseCont = new ArrayList<>();
		
		List<String> extractedPhraseCont= new ArrayList<>();
		for (Phrase p:expectedPhrases)
		{
			expectedPhraseCont.add(p.getPhraseContent());
		}
		
		for (Phrase p:extractedPhrases)
		{
			extractedPhraseCont.add(p.getPhraseContent());
			
		}
//	List<String> temp_found_adrs = extractedPhraseCont;
//	List<Phrase> temp_found_adrs = extractedPhrases;
	List<Phrase> temp_found_adrs = new ArrayList<>();
	
	for (Phrase p:extractedPhrases)
	{
		temp_found_adrs.add(p);
	}
	List<String> tokenizedExpected = new ArrayList<>();
	
	//since we parsed the tweets after annotattion, correctly  extracted can be considered FP
	if (tokenizeExpectedADRs)
	{
	for (String expected: expectedPhraseCont)
	{
		tokenizedExpected.add(parser.getTokenizedSentence(expected));
	}
	
	}
	if (tokenizedExpected.isEmpty())
	{
	tokenizedExpected.addAll(expectedPhraseCont);
	}
//	for (String expected:tokenizedExpected)
//	{
//	boolean expedted_found = false;
//	System.out.println();
	for (Phrase expectedPhrase:expectedPhrases)
	{
		boolean expedted_found = false;
		
		String expected= parser.getTokenizedSentence(expectedPhrase.getPhraseContent());
		if (expected.isEmpty()) expected=expectedPhrase.getPhraseContent();
		
		
//		for (String extracted:extractedPhraseCont)
//		{
			
		for (Phrase extractedPhrase:extractedPhrases)
		{
			
			String extracted = extractedPhrase.getPhraseContent();
	
			String expected_clean = StringUtil.cleanString(expected);
			String extracted_clean = StringUtil.cleanString(extracted);
			if (expected_clean.matches(".*"+extracted_clean+".*")
					|| extracted_clean.matches(".*"+expected_clean+".*"))
			{
				if (!arePhraseSpansOverlapping(expectedPhrase,extractedPhrase))
				{
					continue;
				}
				
				eval.addTP();
				tps.add(sent.getArtifactId()+" expected:"+expected_clean +"====> extracted: "+extracted);
				expedted_found =true;
				
				temp_found_adrs.remove(extractedPhrase);
				
				break;
			}
			
		}
	if (!expedted_found)
	{
		eval.addFN();
		countFN++;
	//	fns.add(sent.getAssociatedFilePath()+"$$$$ "+expected);
		fns.add(countFN+": "+ expected);
	}
	}
	//up to here the tps are removed from the list and the rest are false positives
	for (Phrase found:temp_found_adrs )
	{
		countFP++;
		//fps.add(sent.getAssociatedFilePath()+"$$$$  "+found);
		fps.add(countFP+": "+found);
		
		eval.addFP();
	}
	
	return eval;
}
	public static boolean arePhraseSpansOverlapping(Phrase expectedPhrase,Phrase extractedPhrase)
	{
		boolean are_overlapping = false;
		
		int exracted_start=extractedPhrase.getStartCharOffset();
		int extracted_end = extractedPhrase.getEndCharOffset();
		
		//First expand expected
		int expected_start=expectedPhrase.getStartCharOffset();
		int expected_end = expectedPhrase.getEndCharOffset();
		
		if (exracted_start>=expected_start
				&& extracted_end<=expected_end)
		{
			are_overlapping=true;
			return are_overlapping;
		}
		Artifact prev = expectedPhrase.getStartArtifact().getPreviousArtifact();
		if (prev!= null)
		{
			Artifact sec_prev = prev.getPreviousArtifact();
			if (sec_prev!=null)
			{
				expected_start=sec_prev.getStartIndex();
			}
			else
			{
				expected_start=prev.getStartIndex();
			}
		}
		Artifact next= expectedPhrase.getEndArtifact().getNextArtifact();
		if (next!=null)
		{
			Artifact sec_next = next.getNextArtifact();
			if (sec_next!= null)
			{
				expected_end = sec_next.getEndIndex();
			}
			else
			{
				expected_end=next.getEndIndex();
			}
		}
		if (exracted_start>=expected_start
				&& extracted_end<=expected_end)
		{
			are_overlapping=true;
			return are_overlapping;
		}
		//then expand extracted
		Artifact prev_extracted = extractedPhrase.getStartArtifact().getPreviousArtifact();
		if (prev_extracted!= null)
		{
			Artifact sec_prev_extracted = prev_extracted.getPreviousArtifact();
			if (sec_prev_extracted!=null)
			{
				exracted_start=sec_prev_extracted.getStartIndex();
			}
			else
			{
				exracted_start=prev_extracted.getStartIndex();
			}
		}
		Artifact next_extracted= extractedPhrase.getEndArtifact().getNextArtifact();
		if (next_extracted!=null)
		{
			Artifact sec_next_extracted = next_extracted.getNextArtifact();
			if (sec_next_extracted!= null)
			{
				extracted_end = sec_next_extracted.getEndIndex();
			}
			else
			{
				extracted_end=next_extracted.getEndIndex();
			}
		}
		if (expected_start>=exracted_start
				&& expected_end<=extracted_end)
		{
			are_overlapping=true;
		}
		return are_overlapping;
	}
//	 public static List<String> getExpectedConcepts(Artifact sent,String TargetSemanticType) {
//		List<String> expected= new ArrayList<>();
//		ArrayList<Artifact> alreadyAdded = new ArrayList<>();
//		 
//		List<Phrase> annotations = Phrase.getPhrasesInSentence(sent);
//		if (TargetSemanticType.equals("ADR"))
//		{
//			for (Phrase p:annotations)
//			{
//				if(alreadyAdded.contains(p.getStartArtifact()) || alreadyAdded.contains(p.getEndArtifact()))
//				{
//					continue;
//				}
//				if (p.getPhraseEntityType().equals("ADR")
//						|| p.getPhraseEntityType().toLowerCase().startsWith("int")
//						)
//				{
//					String acceptedStringForApproximateMatch = p.getPhraseContent();
//					//for now we try to aaccept two word after and two word before
//					String next ="";
////					Artifact nextArtifact = p.getEndArtifact().getNextArtifact();
////					if (nextArtifact != null)
////					{
////						next = nextArtifact.getContent();
//////						Artifact secondNext =nextArtifact.getNextArtifact();
//////						if (secondNext != null)
//////						{
//////							next+=" "+secondNext.getContent();
//////						}
////					}
//					
////					String prev ="";
////					Artifact prevArtifact = p.getStartArtifact().getPreviousArtifact();
////					if (prevArtifact != null)
////					{
////						if (prevArtifact.getContent().matches("\\w+"))
////						{
////							prev = prevArtifact.getContent();
////						}
////						
//////						Artifact secondPrev =prevArtifact.getPreviousArtifact();
//////						
//////						if (secondPrev != null)
//////						{
//////							prev = secondPrev.getContent()+" "+prev;
//////						}
////					}
////					acceptedStringForApproximateMatch = prev+" " +acceptedStringForApproximateMatch+" "+next;
//					expected.add(acceptedStringForApproximateMatch.trim());
//					
////					Artifact cur = p.getStartArtifact();
////					while (!cur.equals(p.getEndArtifact()))
////					{
////						alreadyAdded.add(cur);
////						cur = cur.getNextArtifact();
////					}
////					alreadyAdded.add(p.getEndArtifact());
//				}
//			}
//		}
//		else if (TargetSemanticType.equals("ADR&Indication"))
//		{
//			for (Phrase p:annotations)
//			{
//				if(alreadyAdded.contains(p.getStartArtifact()) || alreadyAdded.contains(p.getEndArtifact()))
//				{
//					continue;
//				}
//				if (p.getPhraseEntityType().equals("ADR")
//						|| p.getPhraseEntityType().toLowerCase().startsWith("int")
//						|| p.getPhraseEntityType().toLowerCase().startsWith("ind")
//						|| p.getPhraseEntityType().toLowerCase().startsWith("bene")
//						)
//				{
//					String acceptedStringForApproximateMatch = p.getPhraseContent();
//					//for now we try to aaccept two word after and two word before
//					String next ="";
////					Artifact nextArtifact = p.getEndArtifact().getNextArtifact();
////					if (nextArtifact != null)
////					{
////						next = nextArtifact.getContent();
//////						Artifact secondNext =nextArtifact.getNextArtifact();
//////						if (secondNext != null)
//////						{
//////							next+=" "+secondNext.getContent();
//////						}
////					}
//					
////					String prev ="";
////					Artifact prevArtifact = p.getStartArtifact().getPreviousArtifact();
////					if (prevArtifact != null)
////					{
////						if (prevArtifact.getContent().matches("\\w+"))
////						{
////							prev = prevArtifact.getContent();
////						}
////						
//////						Artifact secondPrev =prevArtifact.getPreviousArtifact();
//////						
//////						if (secondPrev != null)
//////						{
//////							prev = secondPrev.getContent()+" "+prev;
//////						}
////					}
////					acceptedStringForApproximateMatch = prev+" " +acceptedStringForApproximateMatch+" "+next;
//					expected.add(acceptedStringForApproximateMatch.trim());
//					
////					Artifact cur = p.getStartArtifact();
////					while (!cur.equals(p.getEndArtifact()))
////					{
////						alreadyAdded.add(cur);
////						cur = cur.getNextArtifact();
////					}
////					alreadyAdded.add(p.getEndArtifact());
//				}
//			}
//		}
//		else if (TargetSemanticType.equals("Indication"))
//		{
//			for (Phrase p:annotations)
//			{
//				if(alreadyAdded.contains(p.getStartArtifact()) || alreadyAdded.contains(p.getEndArtifact()))
//				{
//					continue;
//				}
//				if ( p.getPhraseEntityType().toLowerCase().startsWith("ind")
//						|| p.getPhraseEntityType().toLowerCase().startsWith("bene")
//						)
//				{
//					expected.add(p.getPhraseContent().trim());
//
//				}
//			}
//		}
//		return expected;
//	}
	 

//	 NOTE: We may have another version of this somewhere else in the code base
	 public static List<Phrase> getExpectedConceptPhrases(Artifact sent,TargetSemanticType TargetSemanticType) {
		 List<Phrase> expected= new ArrayList<>();
			ArrayList<Artifact> alreadyAdded = new ArrayList<>();
			 
			List<Phrase> annotations = Phrase.getPhrasesInSentence(sent);
			if (TargetSemanticType.equals(TargetSemanticType.ADR))
			{
				for (Phrase p:annotations)
				{
//					if(alreadyAdded.contains(p.getStartArtifact()) || alreadyAdded.contains(p.getEndArtifact()))
//					{
//						continue;
//					}
					if (p.getPhraseEntityType().equals("ADR")
							|| p.getPhraseEntityType().toLowerCase().startsWith("int")
							)
					{
						String next ="";

						expected.add(p);

					}
				}
			}
			else if (TargetSemanticType.equals(TargetSemanticType.ADR_Indication))
			{
				for (Phrase p:annotations)
				{
					if(alreadyAdded.contains(p.getStartArtifact()) || alreadyAdded.contains(p.getEndArtifact()))
					{
						continue;
					}
					if (p.getPhraseEntityType().equals("ADR")
							|| p.getPhraseEntityType().toLowerCase().startsWith("int")
							|| p.getPhraseEntityType().toLowerCase().startsWith("ind")
							|| p.getPhraseEntityType().toLowerCase().startsWith("bene")
							)
					{

						expected.add(p);
						
					}
				}
			}
			else if (TargetSemanticType.equals(TargetSemanticType.Indication))
			{
				for (Phrase p:annotations)
				{
					if(alreadyAdded.contains(p.getStartArtifact()) || alreadyAdded.contains(p.getEndArtifact()))
					{
						continue;
					}
					if ( p.getPhraseEntityType().toLowerCase().startsWith("ind")
							|| p.getPhraseEntityType().toLowerCase().startsWith("bene")
							)
					{
						expected.add(p);

					}
				}
			}
			return expected;
		}
	 public static List<String> getExpectedConcepts(Artifact sent,TargetSemanticType targetSemanticType) {
	 
		List<Phrase> expected_concepts_phrases =  getExpectedConceptPhrases(sent,targetSemanticType);
		List<String> expected_concept_contents = new ArrayList<>();
		
		for (Phrase p:expected_concepts_phrases)
		{
			expected_concept_contents.add(p.getPhraseContent().trim());
		}
		return expected_concept_contents;
		
	 }
}