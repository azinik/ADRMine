package edu.asu.diego.adrmine.ner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import rainbownlp.core.Artifact;
import rainbownlp.core.FeatureValuePair;
import rainbownlp.core.Setting;
import rainbownlp.machineLearning.CRFSuite;
import rainbownlp.machineLearning.IFeatureCalculator;
import rainbownlp.machineLearning.MLExample;
import rainbownlp.parser.ParseHandler;
import rainbownlp.parser.StanfordParser;

import rainbownlp.util.HibernateUtil;
import edu.asu.diego.adrmine.classification.TokenSequenceExampleBuilder;
import edu.asu.diego.adrmine.evaluation.CRFBasedExtractionUtils.TargetSemanticType;
import edu.asu.diego.adrmine.evaluation.ADRMineEvaluation;
import edu.asu.diego.adrmine.evaluation.MainConceptExtractionEvaluation;
import edu.asu.diego.adrmine.features.TokenADRLexiconFeatures;
import edu.asu.diego.adrmine.features.TokenBasicFeatures;
import edu.asu.diego.adrmine.features.TokenClauseFeatures;
import edu.asu.diego.adrmine.features.TokenDeepClusterFeatures;
import edu.asu.diego.loader.DocumentAnalyzer;


public class MainADRMine {

	public static void main(String[] args) throws Exception
	{
		
		Options options = new Options();

		// add t option
		options.addOption("train", false, "Training mode");
		options.addOption("dbName", true, "database name");
		options.addOption("dbUser", true, "database user name");
		options.addOption("dbPass", true, "database password");
		
		options.addOption("docs", true, "The file containing train/test text contents");
		options.addOption("goldAnns", true, "The gold standard annotation file");
		options.addOption("corpusName", true, "The name for the experiment");
		
		options.addOption("crfPath", true, "The path to the crfsuite executable file");
		options.addOption("modelFile", true, "Trained CRF model file path");
		options.addOption("forTwitter", true, "true if the experiment is on tweets");
		options.addOption("calculateFeatures", true, "Calculating the"
				+ " classification features for the input");
		options.addOption("trainFile", true, "the training instances with features");
		options.addOption("testFile", true, "the testing instances with features");
		
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse( options, args);
		
		String input_documents =cmd.getOptionValue("docs");
		String gold_anns =cmd.getOptionValue("goldAnns");
		String corpusName =cmd.getOptionValue("corpusName");
		String crfExecutableFilePath =cmd.getOptionValue("crfPath");
		String crfTrainedModel =cmd.getOptionValue("modelFile");
		
		String dbName;
		String dbUser;
		String dbPass;
		boolean forTwitter =Boolean.valueOf(cmd.getOptionValue("forTwitter"));
		boolean calculateFeatures =true;
		if (cmd.hasOption("calculateFeatures")){
			calculateFeatures = Boolean.valueOf(cmd.getOptionValue("calculateFeatures"));
		}
		
		HibernateUtil.changeDB=true;
		
		if(!cmd.hasOption("dbName") ||
				!cmd.hasOption("dbUser") ||
				!cmd.hasOption("dbPass")){
			throw new IllegalArgumentException("The database arguments are missing.");
		}else{
			dbName="jdbc:mysql://localhost/"+cmd.getOptionValue("dbName");
			dbUser =cmd.getOptionValue("dbUser");
			dbPass = cmd.getOptionValue("dbPass");
			HibernateUtil.changeConfigurationDatabase(dbName,
					dbUser,dbPass);
		}
		
		if(cmd.hasOption("train")) {
		    System.out.println("-------- training ---------");
			adrMineTrainPipeline(input_documents,corpusName,crfExecutableFilePath,gold_anns,forTwitter);

		}
		else {
			
			System.out.println("-------- testing ---------");
//			adrMineTestPipeline(input_documents,corpusName,crfExecutableFilePath,crfTrainedModel,forTwitter);
//			
			//evaluating
			List<MLExample> testExamples = 
					MLExample.getAllExamples(corpusName, false);
			if (gold_anns!=null){
				
				ADRMineEvaluation.updateExpectedPhrases(gold_anns,corpusName,
						TargetSemanticType.ADR,testExamples);
				HibernateUtil.clearLoaderSession();
				MainConceptExtractionEvaluation ev = new MainConceptExtractionEvaluation();
				ev.evaluateExtractedPhrases(testExamples,MainConceptExtractionEvaluation.ExtractionMethod.CRF,
						TargetSemanticType.ADR.toString(),false,corpusName);
			}
		}
	}
	
public static void adrMineTestPipeline
(String input_test_text_file, String corpus,
		String crf_path,String modelFile,boolean forTweets) throws Exception
{
	TokenSequenceExampleBuilder.ExperimentGroupADRConcepts = corpus;
	String experimentgroup =corpus;

	Setting.TrainingMode = false;
	
	//loading Artifacts
	DocumentAnalyzer doc_proc = new DocumentAnalyzer();
	doc_proc.loadDocumentsFromFlatFile(input_test_text_file,"\\t",experimentgroup);
	
	//preprocessing
	StanfordParser s_parser = new StanfordParser();
	List<Artifact> sentences = 
			Artifact.listByTypeAndForTrain(Artifact.Type.Sentence,false,experimentgroup);

	int total_count = sentences.size();
	int count = 0;

	for (Artifact sentence:sentences)
	{
		try {
			ParseHandler.calculatePOS(s_parser,sentence);
			count++;
			System.out.println("Parsing sentences ... "+count+"/"+total_count);
			
			
		} catch (Exception e) {
			//TODO: 
		}

		HibernateUtil.clearLoaderSession();
	}	
	
	//then building examples for tokens just for test
	TokenSequenceExampleBuilder.createTokenSequenceExamples(false,experimentgroup);
	
	//calculating features
	
	List<IFeatureCalculator> tokenFeatureCalculators = 
			new ArrayList<IFeatureCalculator>();
	//This is to make sure right features are used

	tokenFeatureCalculators.add(new TokenBasicFeatures());
	TokenDeepClusterFeatures tdcf = new TokenDeepClusterFeatures();
	tdcf.corpusName = corpus;
	tokenFeatureCalculators.add(tdcf);
	
	if (forTweets)
	{
		
		tokenFeatureCalculators.add(new TokenClauseFeatures());
		
		tokenFeatureCalculators.add(new TokenADRLexiconFeatures());
	}
	
	HibernateUtil.clearLoaderSession();
	//Test
	count=0;
	List<MLExample> testExamples = 
					MLExample.getAllExamples(experimentgroup, false);
	System.out.println("size of test example "+testExamples.size());
	for (MLExample example:testExamples)
	{
		example.calculateFeatures(tokenFeatureCalculators);
		
		count++;
		System.out.println("Calculating test candidate features "+count+"/"+testExamples.size());
		HibernateUtil.clearLoaderSession();
	}
	
	//running classifier
	
	FeatureValuePair.resetIndexes();
	
	
	CRFSuite learner_engine = (CRFSuite) CRFSuite.getLearnerEngine(experimentgroup);
	if (crf_path!=null)
	{
		learner_engine.CRFSuiteExecutable_arg = crf_path;
	}
	
	learner_engine.corpusName = corpus;
	learner_engine.setProgramMode("just-test");
	//use the default Model
	if(modelFile!=null){
		learner_engine.setModelFilePath(modelFile);
		
	}	

	try {
		learner_engine.test(testExamples);
	} catch (Exception e) {
		throw new Exception(e.getMessage());
	}
	
////	Evaluator.getEvaluationResult(testExamples).printResult();

	HibernateUtil.clearLoaderSession();
	
	portability.ConceptextractionOutPutManager.generateAnnotationFormatOutPut(TargetSemanticType.ADR,experimentgroup);

	
}
//TODO: refactor and merge the train and test pipeline
public static void adrMineTrainPipeline
(String inputDocs, String corpusName,String crfPath,String annotationFilePath,boolean forTweets) throws Exception
{
	TokenSequenceExampleBuilder.ExperimentGroupADRConcepts = corpusName;
	String experimentgroup =corpusName;
	
	Setting.TrainingMode =true;
	
	//loading Artifacts
	DocumentAnalyzer doc_proc = new DocumentAnalyzer();
	doc_proc.loadDocumentsFromFlatFile(inputDocs,"\\t",experimentgroup);
	
	//preprocessing
	StanfordParser s_parser = new StanfordParser();
	List<Artifact> sentences = 
			Artifact.listByTypeAndForTrain(Artifact.Type.Sentence,true,experimentgroup);

	int total_count = sentences.size();
	int count = 0;

	for (Artifact sentence:sentences)
	{
		try {
			ParseHandler.calculatePOS(s_parser,sentence);
			count++;
			System.out.println("Parsing sentences ... "+count+"/"+total_count);
			
			
		} catch (Exception e) {
			//TODO: 
		}

		HibernateUtil.clearLoaderSession();
	}	
	
	// building classification examples
	TokenSequenceExampleBuilder.createTokenSequenceExamples(true,experimentgroup);
	//Setting the expected classes
	ADRMineEvaluation.updateExpectedClass(annotationFilePath, experimentgroup, "ADR");
	
	//calculating features
	
	List<IFeatureCalculator> tokenFeatureCalculators = 
			new ArrayList<IFeatureCalculator>();
	//This is to make sure right features are used

	tokenFeatureCalculators.add(new TokenBasicFeatures());
	TokenDeepClusterFeatures tdcf = new TokenDeepClusterFeatures();
	tdcf.corpusName = corpusName;
	tokenFeatureCalculators.add(tdcf);
	
	if (forTweets)
	{
		
		tokenFeatureCalculators.add(new TokenClauseFeatures());
		
		tokenFeatureCalculators.add(new TokenADRLexiconFeatures());
	}
	
	
	List<MLExample> trainExamples = 
		MLExample.getAllExamples(experimentgroup, true);
	count=0;
	
	for (MLExample example:trainExamples)
	{
		example.calculateFeatures(tokenFeatureCalculators);
		
		count++;
		
		System.out.println("***train "+count+"/"+trainExamples.size());
		
	}
	HibernateUtil.clearLoaderSession();

	//running classifier
	
	FeatureValuePair.resetIndexes();
	
	
	CRFSuite learner_engine = (CRFSuite) CRFSuite.getLearnerEngine(experimentgroup);
	if (crfPath!=null)
	{
		learner_engine.CRFSuiteExecutable_arg = crfPath;
	}
	
	learner_engine.corpusName = corpusName;
	learner_engine.setProgramMode("train");
	learner_engine.train(trainExamples);
	
	HibernateUtil.clearLoaderSession();
	
}


}
