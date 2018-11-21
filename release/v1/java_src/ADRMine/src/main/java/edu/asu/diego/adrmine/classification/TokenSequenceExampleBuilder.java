package edu.asu.diego.adrmine.classification;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//
//import machineLearning.WordRepresentation;
//import machineLearning.chunk.ChunkBinaryExampleBuilder;
//import machineLearning.chunk.ChunkEmbeddingsFeatures;
//import machineLearning.chunk.SentenceChunkFeatures;
import rainbownlp.core.Artifact;
import rainbownlp.core.Artifact.Type;
import rainbownlp.machineLearning.MLExample;
import rainbownlp.util.HibernateUtil;



public class TokenSequenceExampleBuilder {
	//This classifies a chunk as having ADR or not
	public static String ExperimentGroupADRConcepts = "ADRConceptTokenSeq";

	public enum TokenLabel {
		OUT(0),
		BADR(1),
		IADR(2),
		BIND(3),
		IIND(4),
		BBEN(5),
		IBEN(6);
		private static final Map<Integer,TokenLabel> lookup = 
			new HashMap<Integer,TokenLabel>();
		
		static {
	          for(TokenLabel l : EnumSet.allOf(TokenLabel.class))
	               lookup.put(l.getCode(), l);
	     }
		
		private int code;

	     private TokenLabel(int code) {
	          this.code = code;
	     }

	     public int getCode() { return code; }

	     public static TokenLabel getEnum(int code) { 
	          return lookup.get(code); 
	     }	
	     
	}
	public static void createTokenSequenceExamples(boolean forTrain,String corpusName)
	{
		
		//get all child artifacts and check if they are in ADR concepts
		List<Artifact> all_sentences = Artifact.listByTypeByForTrainByCorpus(Type.Sentence,forTrain,corpusName);
		
		List<String> crf_format = new ArrayList<String>();
		int count=0;
		int all =all_sentences.size();
		for (Artifact sent:all_sentences)
		{
			// build one example for each
			HashMap<Artifact, Integer> token_label_map = new HashMap<Artifact, Integer>();
			
			List<Artifact> tokens = sent.getChildsArtifact();
			
			for (Artifact token:tokens)
			{
				token_label_map.put(token, TokenLabel.OUT.ordinal());
			}

			for (Artifact token: token_label_map.keySet())
			{
				MLExample token_seq_example = 
						MLExample.getInstanceForArtifact(token, corpusName);
				Integer label = token_label_map.get(token);
				
				token_seq_example.setExpectedClass(label);
					
				token_seq_example.setPredictedClass(-1);
				
				token_seq_example.setForTrain(forTrain);
				token_seq_example.setAssociatedFilePath(token.getAssociatedFilePath());
				
				MLExample.saveExample(token_seq_example);
			}
			count++;
			System.out.println("Building classification candidates... count: "+count+"/"+all);
			HibernateUtil.clearLoaderSession();
		}

	
	}	
}
