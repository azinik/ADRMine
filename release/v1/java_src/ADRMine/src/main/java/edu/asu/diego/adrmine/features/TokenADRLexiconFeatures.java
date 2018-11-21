/**
 * 
 */
package edu.asu.diego.adrmine.features;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.queryParser.ParseException;
import LuceneManagerADR.ADRLuceneSearcher;
import edu.asu.diego.adrmine.classification.TokenSequenceExampleBuilder;
import edu.asu.diego.adrmine.utils.LexiconBasedUtils;
import rainbownlp.analyzer.sentenceclause.Clause;
import rainbownlp.analyzer.sentenceclause.SentenceClauseManager;
import rainbownlp.core.Artifact;
import rainbownlp.core.FeatureValuePair;
import rainbownlp.core.Phrase;
import rainbownlp.core.PhraseLink;
import rainbownlp.core.FeatureValuePair.FeatureName;
import rainbownlp.core.SentenceChunk;
import rainbownlp.core.Setting;
import rainbownlp.i2b2.sharedtask2012.ClinicalEvent;
import rainbownlp.i2b2.sharedtask2012.TimexPhrase;
import rainbownlp.machineLearning.IFeatureCalculator;
import rainbownlp.machineLearning.MLExample;
import rainbownlp.machineLearning.MLExampleFeature;
import rainbownlp.util.FileUtil;
import rainbownlp.util.HibernateUtil;
import rainbownlp.util.StringUtil;

/**
 * @author Azadeh
 * 
 */
public class TokenADRLexiconFeatures implements IFeatureCalculator {
	String luceneIndexPath;
	public static void main (String[] args) throws Exception
	{
		String experimentgroup = TokenSequenceExampleBuilder.ExperimentGroupADRConcepts;
		List<MLExample> trainExamples = 
			MLExample.getAllExamples(experimentgroup, true);
		int count=0;
		TokenADRLexiconFeatures lbf = new TokenADRLexiconFeatures();
		for (MLExample example:trainExamples)
		{
			
//			TokenADRLexiconFeatures lbf = new TokenADRLexiconFeatures();
			lbf.calculateFeatures(example);
			count++;
			
			HibernateUtil.clearLoaderSession();
		}
//		
		//Test
		count=0;
		List<MLExample> testExamples = 
				MLExample.getAllExamples(experimentgroup, false);
		
		lbf = new TokenADRLexiconFeatures();
		for (MLExample example:testExamples)
		{
			count++;
			
//			TokenADRLexiconFeatures lbf = new TokenADRLexiconFeatures();
			lbf.calculateFeatures(example);
			
			
			System.out.println("test**************************************** "+count+"/"+testExamples.size());
			HibernateUtil.clearLoaderSession();
		}
	}
	@Override
	public void calculateFeatures(MLExample exampleToProcess) throws Exception {
				
		//get related artifact
		Artifact relatedArtifact = exampleToProcess.getRelatedArtifact();
//		String artifact_content = StringUtil.getTermByTermPorter(relatedArtifact.getContent());
		
		String lemma = StringUtil.getTermByTermWordnet(relatedArtifact.getContent());

		FeatureValuePair is_in_lexicon = FeatureValuePair.getInstance
				(FeatureName.isTokenInLexicon, LexiconBasedUtils.isTokenInLexicon
		                 (lemma, getLuceneIndexPath())?"1":"0");
		MLExampleFeature.setFeatureExample(exampleToProcess, is_in_lexicon);
		
	   
	}

	private String  getLuceneIndexPath() throws URISyntaxException, IOException
	{
		if (luceneIndexPath!= null)
			return luceneIndexPath;
		else
		{
			luceneIndexPath = System.getProperty("user.dir")+"/LuceneIndexes/ADRTokenizedLexiconLuneceIndex/";
		}
		return luceneIndexPath;
		
	}
	
	
	
	
}
