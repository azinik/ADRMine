package machineLearning.NN.evaluation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.asu.diego.adrmine.classification.TokenSequenceExampleBuilder.TokenLabel;
import edu.asu.diego.adrmine.evaluation.Evaluation;
import machineLearning.NN.evaluation.NEREvaluationUtils.Entity;
import machineLearning.NN.evaluation.NEREvaluationUtils.TargetSemanticType;
import preprocessutils.StanfordParser;
import rainbownlp.core.Artifact;
import rainbownlp.core.Phrase;
import rainbownlp.machineLearning.MLExample;
import rainbownlp.util.FileUtil;
import rainbownlp.util.HibernateUtil;
import rainbownlp.util.StringUtil;



public class NEREvaluation {
	public static  int  countFP=0;
	public static int  countFN=0;
	public static boolean tokenizeExpectedADRs = true;
	public static StanfordParser parser = new StanfordParser();
	static{
		HibernateUtil.changeDB=true;
		// "jdbc:mysql://localhost/deextTwitter"
		HibernateUtil.changeConfigurationDatabase("jdbc:mysql://localhost/"+"ADRMineDB", "root", "password");
		
	}
	public static void main (String args[]) throws Exception
	{
//		"/home/azadeh/Documents/Twitter_test_expected_labels.tsv"
		String expected_labels_path = args[0];
		
//		"/home/azadeh/projects/deepnl/results_tw.iob"
		String predicted_labels_path = args[1];
		
//		evaluateExtractedEntitiesFromIOBTags(expected_labels_path,predicted_labels_path,TargetSemanticType.ADR);
//		evaluateExtractedEntitiesFromPhrases("/home/azadeh/Documents/test_tweet_annotations.tsv"
//				, "/home/azadeh/workspace/ADRMine/ADRMineOutput_old.tsv", TargetSemanticType.ADR,"ADRConceptTokenSeqtwitter1");
////		evaluateExtractedEntities("/home/azadeh/Documents/DS_test_expected_labels.tsv","/home/azadeh/projects/deepnl/results-ds.iob",TargetSemanticType.ADR);
//		updateExpectedClass("/home/azadeh/Documents/test_tweet_annotations.tsv",
//				"ADRConceptTokenSeqtwitter1",TargetSemanticType.ADR);
//		updateExpectedPhrases("/home/azadeh/Documents/test_tweet_annotations.tsv",
//				"ADRConceptTokenSeqtwitter1",TargetSemanticType.ADR);
//		getExpectedIOBLabelsForTest("ADRConceptTokenSeq","/home/azadeh/Documents/expected_labels_test_tweet_Nov_2018_iob.txt");
//		evaluateExtractedEntitiesFromIOBTags("/home/azadeh/Documents/expected_labels_test_tweet_Nov_2018_iob.txt",
//				"/home/azadeh/workspace/ADRMine/predictions_Jamia_nov_2018_iob2.txt",
//				TargetSemanticType.ADR);
		
	}
			
	//This method currently handles evaluation of ADRs, TODO: complete it for indication and health related
		public static void evaluateExtractedEntitiesFromIOBTags(String goldStandardFilePath, String predictedLabelsFilePath,TargetSemanticType targetSemanticType) throws Exception
		{

			Evaluation eval = new Evaluation();
			
			List<Entity> expectedEntities = new ArrayList<>();
			List<Entity> predictedEntities  = new ArrayList<>();
			
			expectedEntities = NEREvaluationUtils.extractLabledEntities(targetSemanticType, goldStandardFilePath, false);
				
			predictedEntities = NEREvaluationUtils.extractLabledEntities(targetSemanticType, predictedLabelsFilePath, false);
				
			eval = evaluateEntityList(predictedEntities,expectedEntities,targetSemanticType,eval);

			eval.getEvaluation();

		}
		public static void evaluateExtractedEntitiesFromPhrases(String goldStandardFilePath, String predictedLabelsFilePath,TargetSemanticType targetSemanticType,String corpusName) throws Exception
		{
			HashMap<String, List<Entity>> predictedEnts = NEREvaluationUtils.getTaggedEntities(predictedLabelsFilePath,targetSemanticType.toString());
			HashMap<String, List<Entity>> expectedEnts = NEREvaluationUtils.getTaggedEntities(goldStandardFilePath,targetSemanticType.toString());
			
			Evaluation eval = new Evaluation();
			Set<String> all_sent_ids = new HashSet<>();
			List<Artifact> testSentences = new ArrayList<>();
			
			List<MLExample> testExamples = MLExample.getAllExamples(corpusName, false);

			for (MLExample test_e:testExamples)
			{
				Artifact related_sent = test_e.getRelatedArtifact().getParentArtifact();
				if (!testSentences.contains(related_sent))
				{
					testSentences.add(related_sent);
					all_sent_ids.add(related_sent.getAssociatedFilePath());
				}

			}
			
//			all_sent_ids.addAll(predictedEnts.keySet());
//			all_sent_ids.addAll(expectedEnts.keySet());
		
			for (String postID :all_sent_ids){
				
				List<Entity> expected = expectedEnts.get(postID);
				List<Entity> predicted = predictedEnts.get(postID);
				
				eval = evaluateEntityListByContent(predicted,expected,targetSemanticType,eval);
			}
			eval.getEvaluation();

		}
		
		public static Evaluation evaluateEntityList(List<Entity> predicted, List<Entity> expected,TargetSemanticType targetSemanticType,
				Evaluation eval) throws Exception
		{
			if (predicted== null)
			{
				predicted= new ArrayList<>();
				
			}
			if (expected==null){
				expected = new ArrayList<>();
			}
			List<Entity> foundEntitiesCopy = new ArrayList<>(predicted);
			
			for (Entity expectedEntity:expected)
			{
				System.out.println(expectedEntity.phraseContent);
				boolean expected_found = false;
				

				for (Entity foundEntity:foundEntitiesCopy)
				{
					
					if (!NEREvaluationUtils.areEntitySpansOverlapping(expectedEntity,foundEntity))
					{
						continue;
					}
					System.out.println(foundEntity.phraseContent);
					eval.addTP();
//					logFileLinestp.add(expectedEntity.phraseContent);
					expected_found =true;
					
					foundEntitiesCopy.remove(foundEntity);
					
					break;			
				}
				if (!expected_found)
				{
					eval.addFN();
					countFN++;
//					logFileLinesfn.add(countFN+": "+ expectedEntity.phraseContent);
				}
			}
			for (Entity found:foundEntitiesCopy )
			{
				countFP++;
//				logFileLinesFp.add(countFP+": "+found.starIndex);
			
				eval.addFP();
			}

			return eval;

		}
		public static Evaluation evaluateEntityListByContent(List<Entity> predicted, List<Entity> expected,TargetSemanticType targetSemanticType,
				Evaluation eval) throws Exception
		{
			if (predicted== null)
			{
				predicted= new ArrayList<>();
				
			}
			if (expected==null){
				expected = new ArrayList<>();
			}
			List<String> expected_strs = new ArrayList<>();
			for (Entity e:expected){
				expected_strs.add(e.phraseContent);
			}
			
			List<String> predicted_strs = new ArrayList<>();
			for (Entity e:predicted){
				predicted_strs.add(e.phraseContent);
			}
			List<Entity> foundEntitiesCopy = new ArrayList<>(predicted);
			
			List<String> temp_found_adrs = predicted_strs;
			List<String> tokenizedExpected = new ArrayList<>();
			
			//since we parsed the tweets after annotattion, correctly  extracted can be considered FP
			if (tokenizeExpectedADRs)
			{
				for (String expectedPhrase: expected_strs)
				{
					tokenizedExpected.add(parser.getTokenizedSentence(expectedPhrase));
				}
				
			}
			if (tokenizedExpected.isEmpty())
			{
				tokenizedExpected.addAll(expected_strs);
			}
			for (String expectedPhrase:tokenizedExpected)
			{
				boolean expedted_found = false;
				System.out.println();
				for (String predictedPhrase:predicted_strs)
				{
					String expected_clean = StringUtil.cleanString(expectedPhrase);
					String predicted_clean = StringUtil.cleanString(predictedPhrase);
					if (expected_clean.matches(".*"+predicted_clean+".*")
							|| predicted_clean.matches(".*"+expected_clean+".*"))
					{
						eval.addTP();
//						tps.add(sent.getArtifactId()+" expected:"+expected_clean +"====> extracted: "+extracted);
						temp_found_adrs.remove(predictedPhrase);
						expedted_found =true;

						break;
					}
				}
				if (!expedted_found)
				{
					eval.addFN();
					countFN++;

				}
			}
			//up to here the tps are removed from the list and the rest are false positives
			for (String found:temp_found_adrs )
			{
				countFP++;
//				fps.add(countFP+": "+found);
				
				eval.addFP();
			}
			
			return eval;

		}
		//TODO: expand for other entity types
		public static void updateExpectedClass(String goldStandardFilePath,String corpusName,TargetSemanticType targetSemanticType) throws Exception{
			
			List<String> lines = FileUtil.loadLineByLine(goldStandardFilePath);
			String semanticType= targetSemanticType.toString();
			
			for (String ann_line: lines){
				System.out.println(ann_line);
				
				String[] elements =ann_line.split("\t");
				String associatedID = elements[0];
				String content = elements[4];
				int startIndex = Integer.parseInt(elements[1]);
				int endIndex = Integer.parseInt(elements[2]);
				String type = elements[3];
				if (!semanticType.equals(type)){
					continue;
				}
				String tokenized_content = parser.getTokenizedSentence(content);
				String[] tokens = tokenized_content.split(" ");
				String startToken = tokens[0].replaceAll("\\.$", "");
				Artifact startArtifact = Artifact.findInstanceByContentEnd(Artifact.Type.Word, associatedID,startToken );
				if (startArtifact ==null)
				{
					System.out.println("Artifact not found");
				}
				MLExample example = MLExample.findInstanceForArtifact(startArtifact, corpusName);
				
				String saveExpectedQuery = "update MLExample set expectedClass ="+TokenLabel.BADR.ordinal()+" where exampleId="+example.getExampleId();
				
				HibernateUtil.executeNonReader(saveExpectedQuery);
			
				int token_count =1;
				while(token_count<tokens.length){
					Artifact next = startArtifact.getNextArtifact();
					if (next != null){
						example = MLExample.findInstanceForArtifact(next, corpusName);
						
						saveExpectedQuery = "update MLExample set expectedClass ="+TokenLabel.IADR.ordinal()+" where exampleId="+example.getExampleId();
						
						HibernateUtil.executeNonReader(saveExpectedQuery);
					}
					else{
						throw new Exception("next Artifact does not exist");
					}
					token_count++;
				}
			}
			
		}
		public static void getExpectedIOBLabelsForTest(String corpusName,String resultIOBFilePath){
			List<MLExample> test_examples = MLExample.getAllExamples(corpusName, false);
			System.out.println("seting expected class "+test_examples.size());
			List<String> iob_labels = new ArrayList<>();
			for (MLExample e: test_examples){
				System.out.println(e.getExpectedClass());
				iob_labels.add(NEREvaluationUtils.TokenLabelIOB2.getEnum((e.getExpectedClass())).toString());
			}   
			FileUtil.createFile(resultIOBFilePath, iob_labels);
		}
		

}
