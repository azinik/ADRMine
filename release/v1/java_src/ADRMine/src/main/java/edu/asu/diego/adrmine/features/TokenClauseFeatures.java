/**
 * 
 */
package edu.asu.diego.adrmine.features;

import java.util.List;

import edu.asu.diego.adrmine.utils.Negation;
import rainbownlp.core.Artifact;
import rainbownlp.core.FeatureValuePair;
import rainbownlp.core.FeatureValuePair.FeatureName;

import rainbownlp.machineLearning.IFeatureCalculator;
import rainbownlp.machineLearning.MLExample;
import rainbownlp.machineLearning.MLExampleFeature;
import rainbownlp.util.HibernateUtil;
/**
 * @author Azadeh
 * 
 */
public class TokenClauseFeatures implements IFeatureCalculator {
	
	
	public static void main (String[] args) throws Exception
	{
		
//		String experimentgroup = TokenSequenceExampleBuilder.ExperimentGroupADRConcepts;

		String experimentgroup = args[0];
//		List<MLExample> trainExamples = 
//			MLExample.getAllExamples(experimentgroup, true);
//		int count=0;
//		
//		for (MLExample example:trainExamples)
//		{
//		
//			TokenClauseFeatures lbf = new TokenClauseFeatures();
//			lbf.calculateFeatures(example);
////			TokenDeepClusterFeatures cf = new TokenDeepClusterFeatures();
////			cf.calculateFeatures(example);
//			
//			count++;
//			
//			System.out.println("***train "+count+"/"+trainExamples.size());
//			
//		}
//		HibernateUtil.clearLoaderSession();
		//Test
		int count=0;
		List<MLExample> testExamples = 
		MLExample.getAllExamples(experimentgroup, false);
		for (MLExample example:testExamples)
		{
		
			
			TokenClauseFeatures lbf = new TokenClauseFeatures();
			lbf.calculateFeatures(example);
			
//			TokenDeepClusterFeatures cf = new TokenDeepClusterFeatures();
//			cf.calculateFeatures(example);
			
			count++;
			System.out.println("***test "+count+"/"+testExamples.size());
			
		}
	}
	@Override
	public void calculateFeatures(MLExample exampleToProcess) throws Exception {
		
		//get related artifact
		Artifact relatedArtifact = exampleToProcess.getRelatedArtifact();
		
//		boolean is_negated_expanded = Negation.isWordNegatedExpanded(relatedArtifact, relatedArtifact.getParentArtifact());
		boolean is_negated = Negation.isWordNegated(relatedArtifact, relatedArtifact.getParentArtifact());
		
		
//		FeatureValuePair is_negated_exp_feature = FeatureValuePair.getInstance
//				(FeatureName.isTokenNegatedExpanded, is_negated_expanded?"1":"0");
			
//		MLExampleFeature.setFeatureExample(exampleToProcess, is_negated_exp_feature);
		////////
		FeatureValuePair is_negated_feature = FeatureValuePair.getInstance
				(FeatureName.isTokenNegated, is_negated?"1":"0");
			
		MLExampleFeature.setFeatureExample(exampleToProcess, is_negated_feature);
		
		HibernateUtil.clearLoaderSession();		
	}

	
}
