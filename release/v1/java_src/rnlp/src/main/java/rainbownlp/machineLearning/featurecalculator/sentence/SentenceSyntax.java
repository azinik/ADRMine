package rainbownlp.machineLearning.featurecalculator.sentence;

import rainbownlp.core.Artifact;
import rainbownlp.core.FeatureValuePair;
import rainbownlp.machineLearning.IFeatureCalculator;
import rainbownlp.machineLearning.MLExample;
import rainbownlp.machineLearning.MLExampleFeature;

public class SentenceSyntax implements IFeatureCalculator {

	@Override
	public void calculateFeatures(MLExample exampleToProcess) {
		Artifact sentence = exampleToProcess.getRelatedArtifact();
		
		FeatureValuePair wordCountFeature = FeatureValuePair.getInstance("WordCount", 
				((Integer)sentence.getContent().split(" ").length).toString());
		
		MLExampleFeature.setFeatureExample(exampleToProcess, wordCountFeature);
		
		
		FeatureValuePair lineIndexFeature = FeatureValuePair.getInstance("LineIndex", 
				(sentence.getLineIndex()).toString());
		
		MLExampleFeature.setFeatureExample(exampleToProcess, lineIndexFeature);
	}

}
