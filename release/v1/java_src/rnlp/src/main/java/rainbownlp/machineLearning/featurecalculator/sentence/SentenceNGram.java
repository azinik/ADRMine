package rainbownlp.machineLearning.featurecalculator.sentence;

import rainbownlp.core.Artifact;
import rainbownlp.core.FeatureValuePair;
import rainbownlp.core.PhraseLink;
import rainbownlp.machineLearning.IFeatureCalculator;
import rainbownlp.machineLearning.MLExample;
import rainbownlp.machineLearning.MLExampleFeature;
import rainbownlp.util.StringUtil;

public class SentenceNGram implements IFeatureCalculator {

	@Override
	public void calculateFeatures(MLExample exampleToProcess) {
		if(exampleToProcess.getRelatedArtifact() == null)
		{
			PhraseLink sentencesLink = exampleToProcess.getRelatedPhraseLink();
			Artifact firstSentence = sentencesLink.getFirstPhrase().getStartArtifact();
			Artifact secondSentence = sentencesLink.getSecondPhrase().getStartArtifact();
			calculateSentenceNGram(1, firstSentence, exampleToProcess, "FirstSentence1Gram");
			calculateSentenceNGram(1, secondSentence, exampleToProcess, "SecondSentence1Gram");
			calculateSentenceNGram(2, firstSentence, exampleToProcess, "FirstSentence2Gram");
			calculateSentenceNGram(2, secondSentence, exampleToProcess, "SecondSentence2Gram");
		}else
		{
			Artifact sentence = exampleToProcess.getRelatedArtifact();
			calculateSentenceNGram(1, sentence, exampleToProcess, "Sentence1Gram");
			calculateSentenceNGram(2, sentence, exampleToProcess, "Sentence2Gram");
		}
	}
	
	void calculateSentenceNGram(int n, Artifact sentence, MLExample example, String featureName)
	{
		String[] word_text = 
				StringUtil.getTermByTermWordnet(sentence.getContent().toLowerCase()).split(" ");
			
		for(int i=0;i<word_text.length-n;i++)
		{
			String cur_content = "";
			for(int j=0;j<n;j++)
			{
				int new_part_index = i+j;
				if(!word_text[new_part_index].trim().equals(""))
				{
					cur_content = 
						cur_content.concat("_"+word_text[new_part_index].trim());
				}
			}
			cur_content = cur_content.replaceAll("^_", "");
			FeatureValuePair value_pair = FeatureValuePair.getInstance(
					featureName, cur_content, "1");
			MLExampleFeature.setFeatureExample(example,value_pair);

		}
	}

}
