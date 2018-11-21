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
import edu.asu.diego.adrmine.evaluation.ADRMineEvaluation;
import edu.asu.diego.adrmine.evaluation.CRFBasedExtractionUtils.TargetSemanticType;
import edu.asu.diego.adrmine.evaluation.MainConceptExtractionEvaluation;
import edu.asu.diego.adrmine.features.TokenADRLexiconFeatures;
import edu.asu.diego.adrmine.features.TokenBasicFeatures;
import edu.asu.diego.adrmine.features.TokenClauseFeatures;
import edu.asu.diego.adrmine.features.TokenDeepClusterFeatures;
import edu.asu.diego.loader.DocumentAnalyzer;


public class TrainADRMine {
	static TargetSemanticType entityType = TargetSemanticType.ADR;
	
	public static void main(String[] args) throws Exception
	{

			
		String inputTextFile = args[0];
		String goldStandardAnnotations = args[1];
		
		String corpusName = args[2];

		HibernateUtil.changeDB=true;
		HibernateUtil.db=args[3];
		HibernateUtil.user=args[4];
		HibernateUtil.pass=args[5];
		
		String CRFSuite_file_path = null;
		if (args.length==7)
		{
			CRFSuite_file_path = args[6];
		}
		HibernateUtil.changeConfigurationDatabase(args[3], args[4], args[5]);
	
		adrMineTrainPipeline(inputTextFile,corpusName,args[3], args[4], args[5],CRFSuite_file_path,goldStandardAnnotations);
	}
	
public static void adrMineTrainPipeline
(String input_test_text_file, String corpus,String db_name,String db_user, String db_pass,String crf_path,String annotationFilePath) throws Exception
{

//	TokenSequenceExampleBuilder.ExperimentGroupADRConcepts = TokenSequenceExampleBuilder.ExperimentGroupADRConcepts+corpus;
//	String experimentgroup =TokenSequenceExampleBuilder.ExperimentGroupADRConcepts;
	TokenSequenceExampleBuilder.ExperimentGroupADRConcepts = corpus;
	String experimentgroup =corpus;
	
//	Setting.TrainingMode = false;
	HibernateUtil.changeDB=true;
	// "jdbc:mysql://localhost/ADRMineDB"
	HibernateUtil.changeConfigurationDatabase("jdbc:mysql://localhost/"+db_name, db_user, db_pass);
	Setting.TrainingMode =true;
	
	//loading Artifacts
	DocumentAnalyzer doc_proc = new DocumentAnalyzer();
	doc_proc.loadDocumentsFromFlatFile(input_test_text_file,"\\t",experimentgroup);
	
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
	tdcf.corpusName = corpus;
	tokenFeatureCalculators.add(tdcf);
	
	if (corpus.matches(".*twitter.*"))
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
	if (crf_path!=null)
	{
		learner_engine.CRFSuiteExecutable_arg = crf_path;
	}
	
	learner_engine.corpusName = corpus;
	learner_engine.setProgramMode("train");
	learner_engine.train(trainExamples);
	
	HibernateUtil.clearLoaderSession();
	
}


}
