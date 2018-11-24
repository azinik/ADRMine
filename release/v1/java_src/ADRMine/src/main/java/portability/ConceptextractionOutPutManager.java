package portability;

import java.io.IOException;
import java.util.ArrayList;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.asu.diego.adrmine.evaluation.CRFBasedExtractionUtils;
import edu.asu.diego.adrmine.evaluation.MainConceptExtractionEvaluation;
import edu.asu.diego.adrmine.evaluation.CRFBasedExtractionUtils.TargetSemanticType;
import rainbownlp.core.Artifact;
import rainbownlp.core.Phrase;
import rainbownlp.machineLearning.MLExample;
import rainbownlp.util.FileUtil;
import rainbownlp.util.HibernateUtil;
import rainbownlp.util.StringUtil;


public class ConceptextractionOutPutManager {
	public static String experimentgroup = MainConceptExtractionEvaluation.experimentgroup;
	
	
//	public enum ExtractionMethod {
//		LuceneSearchLexicon, SemVecSVM, CRF, SVMCRF,MetaMap,CRFSemanticType,SVMSemanticType,SemVecCRF
//	}

	public static void main (String args[]) throws Exception
	{
		
//		generateAnnotationFormatOutPut(TargetSemanticType.ADR_Indication);
		generateAnnotationFormatOutPut(TargetSemanticType.ADR,experimentgroup);
	}
	public static void generateAnnotationFormatOutPut(TargetSemanticType targetSemanticType, String expermentGroup) throws IOException
	{
		System.out.println("Generating output results ... ");
		
		HibernateUtil.clearLoaderSession();
		List<String> output_extracted_ann_format_lines = new ArrayList<>();
		
		List<MLExample> testExamples = 
				MLExample.getAllExamples(expermentGroup, false);
		
		List<Artifact> testSentences = new ArrayList<>();
		//generating the test sentences
		for (MLExample test_e:testExamples)
		{
			Artifact related_sent = test_e.getRelatedArtifact().getParentArtifact();
			if (!testSentences.contains(related_sent))
			{
				testSentences.add(related_sent);
			}
	
		}
		
		for (Artifact sent:testSentences)
		{
			 List<Phrase> extracted_adrs = CRFBasedExtractionUtils.getCRFExtractedConceptPhrasesForSent
					(sent,expermentGroup,targetSemanticType,false);
			 String text_id = getTextIdFromFilePath(sent.getAssociatedFilePath());
			 for (Phrase extracted_p:extracted_adrs)
			 {
				 String line= text_id+"\t"+extracted_p.getStartCharOffset()+"\t"+
						 extracted_p.getEndCharOffset()+"\t"+
						 "ADR\t"+
						 extracted_p.getPhraseContent();
				 output_extracted_ann_format_lines.add(line);
			 }
		
		}
		//String resultFileTemp =  File.createTempFile("ADRMineOutput_", ".tsv").getPath();
		String resultFile = System.getProperty("user.dir")+"/ExtractedEntities.tsv";
		FileUtil.createFile(resultFile, output_extracted_ann_format_lines);
		System.out.println("Extracted mentions are saved in: "+resultFile);
	}
	public static void generateAnnotationFormatOutPut(TargetSemanticType targetSemanticType)
	{
		HibernateUtil.clearLoaderSession();
		List<String> output_extracted_ann_format_lines = new ArrayList<>();
		
		List<MLExample> testExamples = 
				MLExample.getAllExamples(experimentgroup, false);
		List<Artifact> testSentences = new ArrayList<>();
		//generating the test sentences
		for (MLExample test_e:testExamples)
		{
			Artifact related_sent = test_e.getRelatedArtifact().getParentArtifact();
			if (!testSentences.contains(related_sent))
			{
				testSentences.add(related_sent);
			}
	
		}
		
		for (Artifact sent:testSentences)
		{
			 List<Phrase> extracted_adrs = CRFBasedExtractionUtils.getCRFExtractedConceptPhrasesForSent
					(sent,experimentgroup,targetSemanticType,false);
			 String text_id = getTextIdFromFilePath(sent.getAssociatedFilePath());
			 for (Phrase extracted_p:extracted_adrs)
			 {
				 String line= text_id+"\t"+extracted_p.getStartCharOffset()+"\t"+
						 extracted_p.getEndCharOffset()+"\t"+
						 "ADR\t"+
						 extracted_p.getPhraseContent();
				 output_extracted_ann_format_lines.add(line);
			 }
		
		}
		FileUtil.createFile("/tmp/ADRMineOutput.tsv", output_extracted_ann_format_lines);
			
	}
	public static String getTextIdFromFilePath(String file_path)
	{
		
		String text_id = "";
		if (file_path.endsWith("txt"))
		{
//			/home/azadeh/projects/java/drug-effect-ext/data/off-label-IndivTextFiles-zyprexa/287.txt
			String pattern = ".*\\/(.*)\\.txt$";
		    Pattern p = Pattern.compile(pattern);
		    Matcher m = p.matcher(file_path);
		    if (m.matches())
		    {
		    	text_id = m.group(1);
		    }
		}
		else
			text_id=file_path;

		return text_id;
	}
}
