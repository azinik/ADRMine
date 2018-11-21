/**
 * 
 */
package rainbownlp.machineLearning.featurecalculator.link;

import java.util.List;

import rainbownlp.core.Artifact;
import rainbownlp.core.FeatureValuePair;
import rainbownlp.core.Phrase;
import rainbownlp.core.PhraseLink;
import rainbownlp.core.FeatureValuePair.FeatureName;
import rainbownlp.machineLearning.IFeatureCalculator;
import rainbownlp.machineLearning.MLExample;
import rainbownlp.machineLearning.MLExampleFeature;
import rainbownlp.util.FileUtil;

/**
 * @author ehsan-Azadeh
 * 
 */
public class LinkArgumentPOSFeatures implements IFeatureCalculator {
	static int window = 2;
	public static void main (String[] args) throws Exception
	{
		String experimentgroup = "LinkClassificationEventEvent";
		List<MLExample> trainExamples = 
			MLExample.getAllExamples(experimentgroup, true);
		int counter = 0;
		for (MLExample example:trainExamples)
		{
			LinkArgumentPOSFeatures lbf = new LinkArgumentPOSFeatures();
			lbf.calculateFeatures(example);
			counter++;
			FileUtil.logLine(null, "Processed : "+counter +"/"+trainExamples.size());
		}
		List<MLExample> testExamples = 
			MLExample.getAllExamples(experimentgroup, false);
		counter = 0;
		for (MLExample example:testExamples)
		{
			LinkArgumentPOSFeatures lbf = new LinkArgumentPOSFeatures();
			lbf.calculateFeatures(example);
			counter++;
			FileUtil.logLine(null, "Processed : "+counter +"/"+trainExamples.size());
		}
		
	}
	@Override
	public void calculateFeatures(MLExample exampleToProcess) throws Exception {
			PhraseLink phraseLink = exampleToProcess.getRelatedPhraseLink();
			Phrase phrase1 = phraseLink.getFirstPhrase();
			Phrase phrase2 = phraseLink.getSecondPhrase();
			
			// POS of the phrases
		
			FeatureValuePair fromPhrasePOSFeature = FeatureValuePair.getInstance(
					FeatureName.FromPhrasePOS, 
					phrase1.getPOS(), "1");
			MLExampleFeature.setFeatureExample(exampleToProcess, fromPhrasePOSFeature);
			
			FeatureValuePair toPhrasePOSFeature = FeatureValuePair.getInstance(
					FeatureName.ToPhrasePOS, 
					phrase2.getPOS(), "1");
			MLExampleFeature.setFeatureExample(exampleToProcess, toPhrasePOSFeature);
			
//			Artifact start_artifact = phrase1.getStartArtifact();
//			Artifact end_Artifact  = phrase1.getEndArtifact();
//			Artifact cur_artifact = start_artifact;
//			Artifact fromBackwardPointer = start_artifact.getPreviousArtifact();
//			Artifact fromForwardPointer = end_Artifact.getNextArtifact();
//			while(!cur_artifact.equals(end_Artifact))
//			{
//				FeatureValuePair fromPhrasePOS1By1Feature = FeatureValuePair.getInstance(
//						FeatureName.FromPhrasePOS1By1, 
//						cur_artifact.getPOS(), "1");
//				MLExampleFeature.setFeatureExample(exampleToProcess, fromPhrasePOS1By1Feature);
//				cur_artifact = cur_artifact.getNextArtifact();
//			}
//			
//			
//			start_artifact = phrase2.getStartArtifact();
//			end_Artifact  = phrase2.getEndArtifact();
//			cur_artifact = start_artifact;
//			Artifact toBackwardPointer = start_artifact.getPreviousArtifact();
//			Artifact toForwardPointer = end_Artifact.getNextArtifact();
//			
//			
//			while(!cur_artifact.equals(end_Artifact))
//			{
//				FeatureValuePair toPhrasePOS1By1Feature = FeatureValuePair.getInstance(
//						FeatureName.ToPhrasePOS1By1, 
//						cur_artifact.getPOS(), "1");
//				MLExampleFeature.setFeatureExample(exampleToProcess, toPhrasePOS1By1Feature);
//				cur_artifact = cur_artifact.getNextArtifact();
//			}
//			
//			if(fromBackwardPointer!=null)
//			{
//				FeatureValuePair bigramFeature = FeatureValuePair.getInstance(
//						FeatureName.FromPhrasePOSBigramBefore, 
//						fromBackwardPointer.getPOS()+"-"+start_artifact.getPOS(), "1");
//				MLExampleFeature.setFeatureExample(exampleToProcess, bigramFeature);
//			}
//			if(toBackwardPointer!=null)
//			{
//				FeatureValuePair bigramFeature = FeatureValuePair.getInstance(
//						FeatureName.ToPhrasePOSBigramBefore, 
//						toBackwardPointer.getPOS()+"-"+start_artifact.getPOS(), "1");
//				MLExampleFeature.setFeatureExample(exampleToProcess, bigramFeature);
//			}
//			FeatureValuePair bigramFeature = FeatureValuePair.getInstance(
//					FeatureName.FromToPhrasePOSBigram, 
//					phrase1.getPOS()+"-"+phrase2.getPOS(), "1");
//			MLExampleFeature.setFeatureExample(exampleToProcess, bigramFeature);
//		
//			// POS of the window
//			
//			for(int i=0;i<window;i++)
//			{
//				
//				if(fromBackwardPointer!=null)
//				{
//					FeatureValuePair fromPhrasePOSWindowFeature = FeatureValuePair.getInstance(
//							FeatureName.FromPhrasePOSWindowBefore, 
//							fromBackwardPointer.getPOS(), "1");
//					MLExampleFeature.setFeatureExample(exampleToProcess, fromPhrasePOSWindowFeature);
//					fromBackwardPointer = fromBackwardPointer.getPreviousArtifact();
//				}
//				
//				if(toBackwardPointer!=null)
//				{
//					FeatureValuePair toPhrasePOSWindowFeature = FeatureValuePair.getInstance(
//							FeatureName.ToPhrasePOSWindowBefore, 
//							toBackwardPointer.getPOS(), "1");
//					MLExampleFeature.setFeatureExample(exampleToProcess, toPhrasePOSWindowFeature);
//					toBackwardPointer = toBackwardPointer.getPreviousArtifact();
//				}
//				
//				if(fromForwardPointer!=null)
//				{
//					FeatureValuePair windowFeature = FeatureValuePair.getInstance(
//							FeatureName.FromPhrasePOSWindowAfter, 
//							fromForwardPointer.getPOS(), "1");
//					MLExampleFeature.setFeatureExample(exampleToProcess, windowFeature);
//					fromForwardPointer = fromForwardPointer.getNextArtifact();
//				}
//				
//				if(toForwardPointer!=null)
//				{
//					FeatureValuePair windowFeature = FeatureValuePair.getInstance(
//							FeatureName.ToPhrasePOSWindowAfter, 
//							toForwardPointer.getPOS(), "1");
//					MLExampleFeature.setFeatureExample(exampleToProcess, windowFeature);
//					toForwardPointer = toForwardPointer.getNextArtifact();
//				}
//			}
//			
//			
//			//POS between two concepts
//			start_artifact = phrase1.getEndArtifact().getNextArtifact();
//			end_Artifact  = phrase2.getStartArtifact();
//			cur_artifact = start_artifact;
//			while(cur_artifact!=null && !cur_artifact.equals(end_Artifact))
//			{
//				FeatureValuePair posFeature = FeatureValuePair.getInstance(
//						FeatureName.LinkPOSBetween, 
//						cur_artifact.getPOS(), "1");
//				MLExampleFeature.setFeatureExample(exampleToProcess, posFeature);
//				cur_artifact = cur_artifact.getNextArtifact();
//			}
	}

	
}
