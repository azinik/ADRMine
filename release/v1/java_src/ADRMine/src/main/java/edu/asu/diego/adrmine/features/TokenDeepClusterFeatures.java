/**
 * 
 */
package edu.asu.diego.adrmine.features;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import LuceneManagerADR.DidYouMeanIndexer;
import rainbownlp.core.Artifact;
import rainbownlp.core.FeatureValuePair;

import rainbownlp.core.FeatureValuePair.FeatureName;
import rainbownlp.machineLearning.CRFSuite;
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
public class TokenDeepClusterFeatures implements IFeatureCalculator {
	public  static HashMap<String, String> wordClassMap = new HashMap<>();

	public static String wordDeepClassFilePath = "DSOct314-classes-sorted.txt";
	
	public static boolean lemmatize = true;
	public static boolean correctSpell = false;
	
	public static String corpusName;
	
	public static void main (String[] args) throws Exception
	{
//		String experimentgroup = TokenSequenceExampleBuilder.ExperimentGroupADRConcepts;

		String experimentgroup = args[0];
		TokenDeepClusterFeatures tdf= new TokenDeepClusterFeatures();
		tdf.loadWordClasses(wordDeepClassFilePath);
		List<MLExample> trainExamples = 
			MLExample.getAllExamples(experimentgroup, true);
		int count=0;
//		
		for (MLExample example:trainExamples)
		{
		
			TokenDeepClusterFeatures cf = new TokenDeepClusterFeatures();
			cf.calculateFeatures(example);
			count++;
			
			System.out.println("***train "+count+"/"+trainExamples.size());
			HibernateUtil.clearLoaderSession();
			
		}
		HibernateUtil.clearLoaderSession();
		//Test
		count=0;
		List<MLExample> testExamples = 
		MLExample.getAllExamples(experimentgroup, false);
		for (MLExample example:testExamples)
		{
			TokenDeepClusterFeatures cf = new TokenDeepClusterFeatures();
			cf.calculateFeatures(example);
			
			count++;
			System.out.println("***test "+count+"/"+testExamples.size());
			HibernateUtil.clearLoaderSession();
		}
	}
	//load the classes
	//This deep clusters are previously generated by the tool and saved in a file
	public  void loadWordClasses(String generated_file) throws URISyntaxException
	{
//		String path = getClusterFilePath();
		List<String> lines = FileUtil.loadLineByLine(generated_file);
		for(String line:lines)
		{
			String word = line.split(" ")[0];
			String class_num = line.split(" ")[1];
			wordClassMap.put(word, class_num);
		}
	}
	protected String getClusterFilePath() {
        String cluster_path="";
		String embedding_cluster_file_name = "DSOct314-classes-sorted.txt";
		if (corpusName.matches(".*twitter.*"))
		{
			embedding_cluster_file_name = "TwitterOct0514-classes-sorted.txt";
		}
		
//		String cluster_path= TokenDeepClusterFeatures.class.getClassLoader().getResource("DSOct314-classes-sorted.txt").getPath();
//		if (corpusName.matches(".*twitter.*"))
//		{
//
//			cluster_path =TokenDeepClusterFeatures.class.getClassLoader().getResource("TwitterOct0514-classes-sorted.txt").getPath();
//		}
		
		
		
		try {
			File clusterFileTemp =  File.createTempFile("embeddingClusters", ".txt");
			clusterFileTemp .deleteOnExit();
			cluster_path =clusterFileTemp.getPath();		    
			
			InputStream inputStream = TokenDeepClusterFeatures.class.getClassLoader().getResourceAsStream(embedding_cluster_file_name);
			FileOutputStream outputStream =  new FileOutputStream(clusterFileTemp);

			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = inputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}
			outputStream.flush();
			outputStream.close();
			inputStream.close();

		  } catch (IOException e) {
			  //TODO
		  }
		return cluster_path;
		
	}
	@Override
	public void calculateFeatures(MLExample exampleToProcess) throws Exception {
		
		if (getWordClassMap().isEmpty())
		{
			loadWordClasses(getClusterFilePath());
		}
		//get related artifact
		Artifact relatedArtifact = exampleToProcess.getRelatedArtifact();
		DidYouMeanIndexer sc = new DidYouMeanIndexer();
		
		String lemma = relatedArtifact.getContent();
		if(correctSpell)
			lemma = sc.getTermByTermCorrectSpell(lemma);
		
		if (lemmatize &&
				lemma.matches("\\w+"))
		{
			lemma = StringUtil.getTermByTermWordnet(lemma);
		}
		String class_num = "UNK";
		
		if (getWordClassMap().get(lemma) != null)
		{
			class_num = getWordClassMap().get(lemma);
		}
		FeatureValuePair deepClassFeature = FeatureValuePair.getInstance(
				FeatureName.DeepClassNumber, class_num, "1");
		
		MLExampleFeature.setFeatureExample(exampleToProcess, deepClassFeature);
		HibernateUtil.clearLoaderSession();
		///////////////////////
		//Previous
		//////////////////////
		//before
		Artifact prev_token =  relatedArtifact.getPreviousArtifact();
		String b_lemma = "<s>";
		String second_prev = "<s>";
		String t_prev = "<s>";
		
		if (prev_token!=null)
		{
			b_lemma = prev_token.getContent();
			if (correctSpell)
				b_lemma = sc.getTermByTermCorrectSpell(b_lemma);
			
			if (lemmatize && b_lemma.matches("\\w+"))
			{
				b_lemma = StringUtil.getTermByTermWordnet(b_lemma);
			}
			
			Artifact second_prev_artifact = prev_token.getPreviousArtifact();
			if (second_prev_artifact!=null)
			{
				second_prev = second_prev_artifact.getContent();
				if (correctSpell)
					second_prev = sc.getTermByTermCorrectSpell(second_prev);
				
				if (lemmatize && second_prev.matches("\\w+"))
				{
					second_prev = StringUtil.getTermByTermWordnet(second_prev);
				}
				
				//Third prev
				Artifact third_prev_artifact = second_prev_artifact.getPreviousArtifact();
				if (third_prev_artifact!=null)
				{
					t_prev = third_prev_artifact.getContent();
					if (correctSpell)
						t_prev = sc.getTermByTermCorrectSpell(t_prev);
					
					if (t_prev.matches("\\w+"))
					{
						t_prev = StringUtil.getTermByTermWordnet(t_prev);
					}
					
				}
			}
			
		}
		
		String b_class_num = "UNK";
		String sec_prev_class_num = "UNK";
		String t_prev_class_num = "UNK";
		if (prev_token==null)
		{
			b_class_num = "s_start";
			sec_prev_class_num = "s_start";
			t_prev_class_num= "s_start";
		}
		
		//previous word class
		if (getWordClassMap().get(b_lemma) != null)
		{
			b_class_num = getWordClassMap().get(b_lemma);
		}
		//second previous word class
		if (getWordClassMap().get(second_prev) != null)
		{
			sec_prev_class_num = getWordClassMap().get(second_prev);
		}
		
		
		//Third
		if (getWordClassMap().get(t_prev) != null)
		{
			t_prev_class_num = getWordClassMap().get(t_prev);
		}
//		//prev
		FeatureValuePair prev_deepClassFeature = FeatureValuePair.getInstance(
				FeatureName.PrevDeepClassNumber, b_class_num, "1");
		
		MLExampleFeature.setFeatureExample(exampleToProcess, prev_deepClassFeature);
		HibernateUtil.clearLoaderSession();
		//second prev
		FeatureValuePair sec_deepClassFeature = FeatureValuePair.getInstance(
				FeatureName.SecondPrevDeepClassNumber, sec_prev_class_num, "1");
		
		MLExampleFeature.setFeatureExample(exampleToProcess, sec_deepClassFeature);
//		
		//Third
		FeatureValuePair third_deepClassFeature = FeatureValuePair.getInstance(
				FeatureName.ThirdPrevDeepClassNumber, t_prev_class_num, "1");
		
		MLExampleFeature.setFeatureExample(exampleToProcess, third_deepClassFeature);
		HibernateUtil.clearLoaderSession();
		
		
		///////////////////////
		//After
		//////////////////////
		Artifact nextToken = relatedArtifact.getNextArtifact();
		
		String n_lemma = ".";
		String second_next_content = ".";
		String t_next_content = ".";
		
		if (nextToken!=null)
		{
			n_lemma = nextToken.getContent();
			if (correctSpell)
				n_lemma = sc.getTermByTermCorrectSpell(n_lemma);
			
			if (lemmatize && n_lemma.matches("\\w+"))
			{
				n_lemma = StringUtil.getTermByTermWordnet(n_lemma);
			}
			
			Artifact second_next_artifact = nextToken.getNextArtifact();
			if (second_next_artifact!=null)
			{
				second_next_content = second_next_artifact.getContent();
				if (correctSpell)
					second_next_content = sc.getTermByTermCorrectSpell(second_next_content);
				
				if (lemmatize && second_next_content.matches("\\w+"))
				{
					second_next_content = StringUtil.getTermByTermWordnet(second_next_content);
				}	
			
				Artifact t_next_artifact = second_next_artifact.getNextArtifact();
				if (t_next_artifact!=null)
				{
					t_next_content = t_next_artifact.getContent();
					if (correctSpell)
						t_next_content = sc.getTermByTermCorrectSpell(t_next_content);
					
					if (lemmatize && t_next_content.matches("\\w+"))
					{
						t_next_content = StringUtil.getTermByTermWordnet(t_next_content);
					}	
				
				}
			}
		
		}
		
		String next_class_num = "UNK";
		String sec_next_class_num = "UNK";
		String t_next_class_num = "UNK";
		
		if (nextToken == null)
		{
			next_class_num = "s_end";
			sec_next_class_num = "s_end";
			t_next_class_num = "s_end";
		}
		
//		//next word class
		if (getWordClassMap().get(n_lemma) != null)
		{
			next_class_num = getWordClassMap().get(n_lemma);
		}
		
		//second next word class
		if (getWordClassMap().get(second_next_content) != null)
		{
			sec_next_class_num = getWordClassMap().get(second_next_content);
		}

		//Third
		if (getWordClassMap().get(t_next_content) != null)
		{
			t_next_class_num = getWordClassMap().get(t_next_content);
		}
//		//next
		FeatureValuePair next_deepClassFeature = FeatureValuePair.getInstance(
				FeatureName.NextDeepClassNumber, next_class_num, "1");
		
		MLExampleFeature.setFeatureExample(exampleToProcess, next_deepClassFeature);
		HibernateUtil.clearLoaderSession();
		//second second next
		FeatureValuePair sec_nextdeepClassFeature = FeatureValuePair.getInstance(
				FeatureName.SecondNextDeepClassNumber, sec_next_class_num, "1");
		
		MLExampleFeature.setFeatureExample(exampleToProcess, sec_nextdeepClassFeature);
		
		
		FeatureValuePair t_nextdeepClassFeature = FeatureValuePair.getInstance(
				FeatureName.ThirdNextDeepClassNumber, t_next_class_num, "1");
		
		MLExampleFeature.setFeatureExample(exampleToProcess, t_nextdeepClassFeature);
		
		HibernateUtil.clearLoaderSession();

		
		
		HibernateUtil.clearLoaderSession();		
	}
	public  HashMap<String, String> getWordClassMap() throws URISyntaxException {
		if (wordClassMap.isEmpty())
		{
			loadWordClasses(getClusterFilePath());
		}
		return wordClassMap;
	}


}
