/**
 * 
 */
package rainbownlp.machineLearning.featurecalculator.link;

import java.util.List;

import rainbownlp.core.FeatureValuePair;
import rainbownlp.core.Phrase;
import rainbownlp.core.PhraseLink;
import rainbownlp.core.FeatureValuePair.FeatureName;
import rainbownlp.machineLearning.IFeatureCalculator;
import rainbownlp.machineLearning.MLExample;
import rainbownlp.machineLearning.MLExampleFeature;
import rainbownlp.util.FileUtil;

/**
 * @author Azadeh
 * 
 */
public class LinkGeneralFeatures implements IFeatureCalculator {
	
	public static void main (String[] args) throws Exception
	{
		String experimentgroup = "LinkClassificationEventEvent";
		List<MLExample> trainExamples = 
			MLExample.getAllExamples(experimentgroup, true);
		int counter = 0;
		for (MLExample example:trainExamples)
		{
			LinkGeneralFeatures lbf = new LinkGeneralFeatures();
			lbf.calculateFeatures(example);
			counter++;
			FileUtil.logLine(null, "Processed : "+counter +"/"+trainExamples.size());
		}
		
	}
	@Override
	public void calculateFeatures(MLExample exampleToProcess) {
			
			PhraseLink phraseLink = exampleToProcess.getRelatedPhraseLink();
			Phrase phrase1 = phraseLink.getFromPhrase();
			Phrase phrase2 = phraseLink.getToPhrase();
			
			String relation_location_type = getInterMentionLocationType(phrase1,phrase2);
			
			
			FeatureValuePair argumentTypeFeature = FeatureValuePair.getInstance(
						FeatureName.InterMentionLocationType, 
						relation_location_type,"1");
				
			MLExampleFeature.setFeatureExample(exampleToProcess, argumentTypeFeature);
			
			// The content of the args
			FeatureValuePair fromPhraseContentFeature = FeatureValuePair.getInstance(
					FeatureName.FromPhraseContent, 
					phrase1.getPhraseContent(), "1");
			
			MLExampleFeature.setFeatureExample(exampleToProcess, fromPhraseContentFeature);
			
			FeatureValuePair toPhraseContentFeature = FeatureValuePair.getInstance(
					FeatureName.ToPhraseContent, 
					phrase2.getPhraseContent(), "1");
			
			MLExampleFeature.setFeatureExample(exampleToProcess, toPhraseContentFeature);
			
	}
	//it can retuen BetweenSentence or WithinSentence(within)
	public static String getInterMentionLocationType(Phrase p1, Phrase p2)
	{
		String relation_type = "withinSent";
		if(p1.getStartArtifact().getParentArtifact().equals(p2.getEndArtifact().getParentArtifact()))
		{
			relation_type = "withinSent";
		}
		//TODO we can further analyze
		else
		{
			relation_type = "betweenSent";
		}
		return relation_type;
	}

	
}
