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


public class TestADRMine {

	public static void main(String[] args) throws Exception
	{

		String modelFilePath = args[0];
		String inputTextFile = args[1];
		String corpusName = args[2];
		String test_gold_annotations =null;

		HibernateUtil.changeDB=true;
		HibernateUtil.db=args[3];
		HibernateUtil.user=args[4];
		HibernateUtil.pass=args[5];
		//The path to the executable crfsuite 
		String CRFSuite_file_path = args[6];
		
		if (args.length==8){
			test_gold_annotations = args[7];
		}
		HibernateUtil.changeConfigurationDatabase("jdbc:mysql://localhost/"+args[3], args[4], args[5]);
	
		ADRMineTestPipeline(inputTextFile,corpusName,args[3], args[4], args[5],
				CRFSuite_file_path,modelFilePath);
		//evaluating
		List<MLExample> testExamples = 
				MLExample.getAllExamples(corpusName, false);
		if (test_gold_annotations!=null){
			
			ADRMineEvaluation.updateExpectedPhrases(test_gold_annotations,corpusName,
					TargetSemanticType.ADR,testExamples);

		}


		if (testExamples.isEmpty() || testExamples==null){
			throw new Exception("test classification examples are not loaded ...");
		}
		MainConceptExtractionEvaluation ev = new MainConceptExtractionEvaluation();
		ev.evaluateExtractedPhrases(testExamples,MainConceptExtractionEvaluation.ExtractionMethod.CRF,
				TargetSemanticType.ADR.toString(),false,corpusName);
		
	}
	
public static void ADRMineTestPipeline
(String input_test_text_file, String corpus,String db_name,
		String db_user, String db_pass,String crf_path,String modelFile) throws Exception
{

//	TokenSequenceExampleBuilder.ExperimentGroupADRConcepts = TokenSequenceExampleBuilder.ExperimentGroupADRConcepts+corpus;
//	String experimentgroup =TokenSequenceExampleBuilder.ExperimentGroupADRConcepts;
	TokenSequenceExampleBuilder.ExperimentGroupADRConcepts = corpus;
	String experimentgroup =corpus;
	
//	Setting.TrainingMode = false;
	HibernateUtil.changeDB=true;
	// "jdbc:mysql://localhost/ADRMineDB"
	HibernateUtil.changeConfigurationDatabase("jdbc:mysql://localhost/"+db_name, db_user, db_pass);
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
	
	if (corpus.matches(".*twitter.*"))
	{
		
		tokenFeatureCalculators.add(new TokenClauseFeatures());
		
		tokenFeatureCalculators.add(new TokenADRLexiconFeatures());
	}
	
	HibernateUtil.clearLoaderSession();
	//Test
	count=0;
	List<MLExample> testExamples = 
					MLExample.getAllExamples(experimentgroup, false);
	System.out.println("size of  test example "+testExamples.size());
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
	if(!modelFile.equals("ADRMineModel")){
		learner_engine.setModelFilePath(modelFile);
		
	}	
////	learner_engine.train(trainExamples);
//	
	try {
		learner_engine.test(testExamples);
	} catch (Exception e) {
		throw new Exception(e.getMessage());
	}
	
////	Evaluator.getEvaluationResult(testExamples).printResult();

//	MainConceptExtractionEvaluation ev = new MainConceptExtractionEvaluation();
//	ev.evaluateExtractedPhrases(testExamples,MainConceptExtractionEvaluation.ExtractionMethod.CRF,TargetSemanticType.ADR.toString(),experimentgroup);
	HibernateUtil.clearLoaderSession();
	
	portability.ConceptextractionOutPutManager.generateAnnotationFormatOutPut(TargetSemanticType.ADR,experimentgroup);

	
}


}
