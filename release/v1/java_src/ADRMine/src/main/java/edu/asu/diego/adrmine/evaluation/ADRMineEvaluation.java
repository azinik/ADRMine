package edu.asu.diego.adrmine.evaluation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.asu.diego.adrmine.classification.TokenSequenceExampleBuilder.TokenLabel;
import edu.asu.diego.adrmine.evaluation.CRFBasedExtractionUtils.TargetSemanticType;
import preprocessutils.StanfordParser;
import rainbownlp.core.Artifact;
import rainbownlp.core.Phrase;
import rainbownlp.machineLearning.MLExample;
import rainbownlp.util.FileUtil;
import rainbownlp.util.HibernateUtil;
import rainbownlp.util.StringUtil;



public class ADRMineEvaluation {

	public static boolean tokenizeExpectedADRs = true;
	public static StanfordParser parser = new StanfordParser();

	public static void main (String args[]) throws Exception
	{
//		"/home/azadeh/Documents/test_tweet_annotations.tsv"
		String goldStandardAnnotations = null;
		

		HibernateUtil.changeDB=true;
//		corpusName
		String corpusName = args[0];
		HibernateUtil.db=args[1];
		HibernateUtil.user=args[2];
		HibernateUtil.pass=args[3];
		if (args.length >4){
			goldStandardAnnotations = args[4];
		}
		
		HibernateUtil.changeConfigurationDatabase("jdbc:mysql://localhost/"+args[1], args[2], args[3]);
//		
		List<MLExample> testExamples = 
				MLExample.getAllExamples(corpusName, false);
		if (goldStandardAnnotations!=null){
//			//updating expectedClass in db
			updateExpectedClass(goldStandardAnnotations,corpusName,TargetSemanticType.ADR.toString());
			
			//update the expectedClass field in the MLExample based on the gold standard annotations
			updateExpectedPhrases(goldStandardAnnotations,corpusName,TargetSemanticType.ADR,testExamples);
		}


//		if (testExamples.isEmpty() || testExamples==null){
//			throw new Exception("test classification examples are not loaded ...");
//		}
//		MainConceptExtractionEvaluation ev = new MainConceptExtractionEvaluation();
//		ev.evaluateExtractedPhrases(testExamples,MainConceptExtractionEvaluation.ExtractionMethod.CRF,
//				TargetSemanticType.ADR.toString(),false,corpusName);

		
	}

	public static void updateExpectedClass(String goldStandardFilePath,String corpusName,String targetSemanticType) throws Exception{
		
		List<String> lines = FileUtil.loadLineByLine(goldStandardFilePath);
		String semanticType= targetSemanticType.toString();
		
		for (String ann_line: lines){
			System.out.println(ann_line);
			
			String[] elements =ann_line.split("\t");
			String associatedID = elements[0];
			String content = elements[4];
			int startIndex = Integer.parseInt(elements[1]);
			int endIndex = Integer.parseInt(elements[2]);
			String type = elements[3].toLowerCase();
			//TODO: make this configurable 
			if (!(type.equals("adr")
					||type.equals("indication")
					||type.equals("beneficial")
					||type.equals("interaction"))){
				continue;
			}
			int begin_label = 0;
			int inside_label=0;
			if (type.equals("adr") || type.equals("interaction")){
				begin_label=TokenLabel.BADR.ordinal();
				inside_label = TokenLabel.IADR.ordinal();
			}else if (type.equals("indication") || type.equals("beneficial")){
				begin_label=TokenLabel.BIND.ordinal();
				inside_label = TokenLabel.IIND.ordinal();
			}
			String tokenized_content = parser.getTokenizedSentence(content);
			String[] tokens = tokenized_content.split(" ");
			String startToken = tokens[0].replaceAll("\\.$", "");
			
			Artifact startArtifact=null;
			//special handling
			if (associatedID.equals("humira-51d20e3e53785f584a9af2dd"))
			{
				startArtifact = Artifact.findInstanceByExactContent(Artifact.Type.Word, associatedID,startToken );
			}else{
				 startArtifact = Artifact.findInstanceByContent(Artifact.Type.Word, associatedID,startToken );
			}
			if (startArtifact ==null)
			{
				throw new Exception("start token for the annotated phrase not found");
			}
			MLExample example = MLExample.findInstanceForArtifact(startArtifact, corpusName);
			if (example ==null)
			{
				throw new Exception("the classification candidate for not found"+startArtifact.getArtifactId());
			}
			String saveExpectedQuery = "update MLExample set expectedClass ="+begin_label+" where exampleId="+example.getExampleId();
			
			HibernateUtil.executeNonReader(saveExpectedQuery);
		
			int token_count =1;
			while(token_count<tokens.length){
				Artifact next = startArtifact.getNextArtifact();
				if (next != null){
					example = MLExample.findInstanceForArtifact(next, corpusName);
					
					saveExpectedQuery = "update MLExample set expectedClass ="+inside_label+" where exampleId="+example.getExampleId();
					
					HibernateUtil.executeNonReader(saveExpectedQuery);
				}
				else{
					throw new Exception("next Artifact does not exist");
				}
				token_count++;
			}
		}
		
	}
	//TODO: expand for other entity types
	public static void updateExpectedPhrases(String goldStandardFilePath,String corpusName,
			TargetSemanticType targetSemanticType,List<MLExample> loadedExamples) throws Exception{
		
		List<String> exampleFilePathIds = new ArrayList<>();
		for (MLExample e:loadedExamples){
			exampleFilePathIds.add(e.getAssociatedFilePath());
		}
		
		List<String> lines = FileUtil.loadLineByLine(goldStandardFilePath);
		String semanticType= targetSemanticType.toString();
		int count=0;
		for (String ann_line: lines){
			count++;
			System.out.println("processing gold standard lines "+count+"/"+lines.size());
			
			String[] elements =ann_line.split("\t");
			String associatedID = elements[0];

			String content = elements[4];
			String type = elements[3].toLowerCase();
			//TODO: make this configurable 
			if (!(type.equals("adr")
					||type.equals("indication")
					||type.equals("beneficial")
					||type.equals("interaction"))){
				continue;
			}
			int begin_label = 0;
			int inside_label=0;
			String phraseEntityType=TargetSemanticType.ADR.toString();
			if (type.equals("adr") || type.equals("interaction")){
				begin_label=TokenLabel.BADR.ordinal();
				inside_label = TokenLabel.IADR.ordinal();
			}else if (type.equals("indication") || type.equals("beneficial")){
				begin_label=TokenLabel.BIND.ordinal();
				inside_label = TokenLabel.IIND.ordinal();
				phraseEntityType = TargetSemanticType.Indication.toString();
			}
			
			String tokenized_content = parser.getTokenizedSentence(content);
			String[] tokens = tokenized_content.split(" ");
			String startToken = tokens[0].replaceAll("\\.$", "");
	
			Artifact startArtifact=null;
			//special handling TODO: 
			if (associatedID.equals("humira-51d20e3e53785f584a9af2dd"))
			{
				startArtifact = Artifact.findInstanceByExactContent(Artifact.Type.Word, associatedID,startToken );
			}else{
				 startArtifact = Artifact.findInstanceByContent(Artifact.Type.Word, associatedID,startToken );
			}
			
			if (startArtifact ==null)
			{
				throw new Exception("Artifact not found");
			}
			MLExample example = MLExample.findInstanceForArtifact(startArtifact, corpusName);
			
			String saveExpectedQuery = "update MLExample set expectedClass ="+begin_label+" where exampleId="+example.getExampleId();
			
			HibernateUtil.executeNonReader(saveExpectedQuery);
		
			int token_count =1;
			String phrase_content = startArtifact.getContent();
			Artifact endArtifact= startArtifact;
			Artifact next = startArtifact;
			
			while(token_count<tokens.length){
				next = next.getNextArtifact();
				if (next != null){
					example = MLExample.findInstanceForArtifact(next, corpusName);
					
					saveExpectedQuery = "update MLExample set expectedClass ="+inside_label+" where exampleId="+example.getExampleId();
					
					HibernateUtil.executeNonReader(saveExpectedQuery);
					phrase_content+=" "+next.getContent();
					endArtifact = next;
				}
				else{
					throw new Exception("next Artifact does not exist");
				}
				token_count++;
			}
	
			Phrase.getInstance(phrase_content, startArtifact, endArtifact, phraseEntityType);
		}
		
	}
}		