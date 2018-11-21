package rainbownlp.machineLearning.convertor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import octopus.semantic.similarity.word2vec.util.BinaryReader;

import org.hibernate.Session;

import rainbownlp.core.Artifact;
import rainbownlp.core.FeatureValuePair;
import rainbownlp.core.FeatureValuePair.FeatureName;
import rainbownlp.core.Phrase;
import rainbownlp.core.Setting;
import rainbownlp.core.Vocabulary;
import rainbownlp.machineLearning.MLExample;
import rainbownlp.machineLearning.MLExampleFeature;
import rainbownlp.machineLearning.WordRepresentation;
import rainbownlp.util.FileUtil;
import rainbownlp.util.HibernateUtil;
import rainbownlp.util.StringUtil;


public class PyLearnFormatConvertor {
	public static enum PhraseEmbeddingOption{
		phraseStart,
		PhrasePreviousArtifact,
		PhraseEnd,
		lastArtifactInSent
	}
	static int numClassRatio = Setting.getValueInteger("numClassesRatio");
//	public static WordRepresentation wr = new WordRepresentation("/home/azadeh/projects/java/deext/data/word2VecFiles/vectors.bin");
//	public static WordRepresentation wr = new WordRepresentation(Setting.getValue("wordRepresentationVectorPath"));
	public static WordRepresentation wr = new WordRepresentation("/home/azadeh/projects/dl-ner/word_embeddings.bin");
//	public static WordRepresentation wr = new WordRepresentation("/home/azadeh/projects/dl-ner/phraseCandidates150");
	
	public static Vocabulary v = new Vocabulary();
	
	public static void writeToFile(List<Integer> exampleIdsToWrite, String filePath,String taskName, Integer rightWindowSize,
			HashMap<String, String> wordClusters) 
		throws IOException
	{
//		if(new File(filePath).exists()) return;
		
		FileWriter file_writer = new FileWriter(filePath+"-"+rightWindowSize);
		int count=0;
		for(Integer example_id : exampleIdsToWrite) {
			
			MLExample example = MLExample.getExampleById(example_id);
			Artifact relatedArtifact = example.getRelatedArtifact();
			Artifact next = relatedArtifact.getNextArtifact();
			Artifact prev =  relatedArtifact.getPreviousArtifact();
			
			String prev_content = "beginningofsent";
			String next_content = ".";
			String s_prev_content = "beginningofsent";
			String s_next_content = ".";
			String t_prev_content = "beginningofsent";
			String t_next_content = ".";
			
			if (next!=null)
			{
				next_content=next.getContent();
				if (next.getNextArtifact() != null)
				{
					Artifact s_next = next.getNextArtifact();
					s_next_content = s_next.getContent();
					
					if (s_next.getNextArtifact() != null)
					{
						Artifact t_next= s_next.getNextArtifact();
						t_next_content = t_next.getContent();
					}
				}
			}
			if (prev!=null)
			{
				prev_content=prev.getContent();
				if (prev.getPreviousArtifact() != null)
				{
					Artifact s_prev= prev.getPreviousArtifact();
					
					s_prev_content = s_prev.getContent();
					if (s_prev.getPreviousArtifact() != null)
					{
						Artifact t_prev= s_prev.getPreviousArtifact();
						
						t_prev_content = t_prev.getContent();
					}
				}
			}

			String crfSuiteFormatLine="";
			
			
//			Float s_prev_index_nor= (float) (v.getWordIndexInVocab(s_prev_content)/5870);
//			Float prev_index_nor= (float) (v.getWordIndexInVocab(prev_content)/5870);
//			Float index_nor= (float) (v.getWordIndexInVocab(relatedArtifact.getContent())/5870);
//			Float next_index_nor= (float) (v.getWordIndexInVocab(next_content)/5870);
//			Float s_next_index_nor= (float) (v.getWordIndexInVocab(s_next_content)/5870);
//					
//			crfSuiteFormatLine = 
//					wr.getWordRepresentationValuesTabSeparated(s_prev_content)+"\t"+"-2.00\t"+s_prev_index_nor+"\t"+
//					wr.getWordRepresentationValuesTabSeparated(prev_content)+"\t"+"-1.00\t"+prev_index_nor+"\t"+
//					wr.getWordRepresentationValuesTabSeparated(relatedArtifact.getContent())+"\t"+"0.00\t"+index_nor+"\t"+
//					wr.getWordRepresentationValuesTabSeparated(next_content)+"\t"+"1.00\t"+next_index_nor+"\t"+
//					
//					wr.getWordRepresentationValuesTabSeparated(s_next_content)+"\t"+"2.00\t"+s_next_index_nor+"\t";
			crfSuiteFormatLine = 
					wr.getWordRepresentationValuesTabSeparated(s_prev_content,false)+"\t"+"-2.00\t"+
					wr.getWordRepresentationValuesTabSeparated(prev_content,false)+"\t"+"-1.00\t"+
					wr.getWordRepresentationValuesTabSeparated(relatedArtifact.getContent(),false)+"\t"+"0.00\t"+
					wr.getWordRepresentationValuesTabSeparated(next_content,false)+"\t"+"1.00\t"+
					
					wr.getWordRepresentationValuesTabSeparated(s_next_content,false)+"\t"+"2.00\t";

		
			if (rightWindowSize==3)
			{
				crfSuiteFormatLine =  wr.getWordRepresentationValuesTabSeparated(t_prev_content,false)+"\t"+"-3.00\t"+
						crfSuiteFormatLine+
						wr.getWordRepresentationValuesTabSeparated(t_next_content,false)+"\t"+"3.00\t";
			}
			if (!wordClusters.isEmpty())
			{
				crfSuiteFormatLine= getWordClusterForNN(wordClusters,s_prev_content,150)+"\t"
						+getWordClusterForNN(wordClusters,prev_content,150)+"\t"
						+getWordClusterForNN(wordClusters,relatedArtifact.getContent(),150)+"\t"
						+getWordClusterForNN(wordClusters,next_content,150)+"\t"
						+getWordClusterForNN(wordClusters,s_next_content,150)+"\t"
						+ crfSuiteFormatLine;
			
			}
			crfSuiteFormatLine = crfSuiteFormatLine.replaceAll("\\t+", "\t").replaceAll("\t$", "");
			
			int expectedClass = example.getExpectedClass()+1; //convert to 1-base (e.g. 0 -> 1)
			
			crfSuiteFormatLine = expectedClass +"\t"+ crfSuiteFormatLine;
			System.out.println("**************** count"+count+"/"+exampleIdsToWrite.size());
			file_writer.write( crfSuiteFormatLine+ "\n");
			file_writer.flush();
			count++;
			
			HibernateUtil.clearLoaderSession();
		}
	
		file_writer.flush();
		file_writer.close();
	}
	public static String getWordClusterForNN(HashMap<String, String> wordClusters,String word_content,int numClusters)
	{
		String cluster_representation="";
		int cluster_num=-1;
		String word_cluster_string = wordClusters.get(StringUtil.getTermByTermWordnet(word_content.toLowerCase()));
		if (word_cluster_string!=null)
		{
			cluster_num = Integer.parseInt(word_cluster_string);
		}
		
		for (int i=0;i<numClusters;i++)
		{
			String vector_element="0.00";
			if (i==cluster_num)
			{
				vector_element="1.00";
			}
			cluster_representation=cluster_representation+"\t"+vector_element;
		}
		cluster_representation=cluster_representation.replaceAll("^\t", "");
		return cluster_representation.trim();
	}
	public static void writeToFileForCandidatePhraseExamples
		(List<Integer> exampleIdsToWrite, String filePath,String taskName, Integer rightWindowSize,
				boolean isBinaryClassifier,String targetClass,
				boolean useRNNEmbeddings,HashMap<Artifact, String> tokenEmbeddings, PhraseEmbeddingOption phraseRNNOption) 
			throws Exception
		{
//			if(new File(filePath).exists()) return;
			
			FileWriter file_writer = new FileWriter(filePath+"-"+rightWindowSize);
			int count=0;
			for(Integer example_id : exampleIdsToWrite) {
				
				MLExample example = MLExample.getExampleById(example_id);
				Phrase relatedPhrase = example.getRelatedPhrase();
				
				
				Artifact next = relatedPhrase.getEndArtifact().getNextArtifact();
				Artifact prev =  relatedPhrase.getStartArtifact().getPreviousArtifact();
				
				String prev_content = "beginningofsent";
				String next_content = ".";
				String s_prev_content = "beginningofsent";
				String s_next_content = ".";
				String t_prev_content = "beginningofsent";
				String t_next_content = ".";
				String pyLearn2FormatLine="";
				
//				if (useRNNEmbeddings)
//				{
//					pyLearn2FormatLine = getPhraseEmbeddings(relatedPhrase,
//							phraseRNNOption,tokenEmbeddings);
//				}
//				else
				{
					if (next!=null)
					{
						next_content=next.getContent();
						if (next.getNextArtifact() != null)
						{
							Artifact s_next = next.getNextArtifact();
							s_next_content = s_next.getContent();
							
							if (s_next.getNextArtifact() != null)
							{
								Artifact t_next= s_next.getNextArtifact();
								t_next_content = t_next.getContent();
							}
						}
					}
					if (prev!=null)
					{
						prev_content=prev.getContent();
						if (prev.getPreviousArtifact() != null)
						{
							Artifact s_prev= prev.getPreviousArtifact();
							
							s_prev_content = s_prev.getContent();
							if (s_prev.getPreviousArtifact() != null)
							{
								Artifact t_prev= s_prev.getPreviousArtifact();
								
								t_prev_content = t_prev.getContent();
							}
						}
					}

					
					Artifact startArtifact=relatedPhrase.getStartArtifact();
					Artifact endArtifact=relatedPhrase.getEndArtifact();
					String startContent=startArtifact.getContent();
					String endContent=endArtifact.getContent();
					if (startArtifact.equals(endArtifact))
					{
						endContent="nonExisting";
					}
					
					String combined_phrase_content = relatedPhrase.getPhraseContent().replaceAll(" ", "_");
					pyLearn2FormatLine = getPhraseEmbeddings(relatedPhrase,
							phraseRNNOption,tokenEmbeddings);
					pyLearn2FormatLine = pyLearn2FormatLine+"\t"+
							
//							wr.getWordRepresentationValuesTabSeparated(s_prev_content)+"\t"+"-2.00\t"+
//							wr.getWordRepresentationValuesTabSeparated(prev_content)+"\t"+"-1.00\t"+
							wr.getWordRepresentationValuesTabSeparated(combined_phrase_content,false)+"\t"+"0.00\t"+
							
							wr.getWordRepresentationValuesTabSeparated(next_content,false)+"\t"+"1.00\t"
							+
							
							wr.getWordRepresentationValuesTabSeparated(s_next_content,false)+"\t"+"2.00\t";

					if (rightWindowSize==3)
					{
						pyLearn2FormatLine = 
//								wr.getWordRepresentationValuesTabSeparated(t_prev_content)+"\t"+"-3.00\t"+
								pyLearn2FormatLine
								+
								wr.getWordRepresentationValuesTabSeparated(t_next_content,false)+"\t"+"3.00\t";
					}
					pyLearn2FormatLine = pyLearn2FormatLine.replaceAll("\\t+", "\t").replaceAll("\t$", "");
					
				}
				
				
				int expectedClass = example.getExpectedClass()+1; //convert to 1-base (e.g. 0 -> 1)
				if (isBinaryClassifier)
				{
					//TODO: do not hard code the targetClass
					if (targetClass.equals("ADR"))
					{
						if (expectedClass==2)
						{
							expectedClass=1;
						}
						else
						{
							expectedClass=0;
						}
					}
					else if (targetClass.equals("Indication"))
					{
						if (expectedClass==3)
						{
							expectedClass=1;
						}
						else
						{
							expectedClass=0;
						}
					}
				}
				pyLearn2FormatLine = expectedClass +"\t"+ pyLearn2FormatLine;
				System.out.println("**************** count"+count+"/"+exampleIdsToWrite.size());
				file_writer.write( pyLearn2FormatLine+ "\n");
				file_writer.flush();
				count++;
				
				HibernateUtil.clearLoaderSession();
			}
		
			file_writer.flush();
			file_writer.close();
		}
	public static String getPhraseEmbeddings(Phrase phrase, PhraseEmbeddingOption option,
			HashMap<Artifact, String> token_embeddings) throws Exception
	{
		String embedding = "";
		
		if (option.equals(PhraseEmbeddingOption.PhraseEnd))
		{
			embedding = token_embeddings.get(phrase.getEndArtifact());
		}
		else if (option.equals(PhraseEmbeddingOption.PhrasePreviousArtifact))
		{
			Artifact Prev_artifact = phrase.getStartArtifact().getPreviousArtifact();
			if (Prev_artifact!=null)
				embedding = token_embeddings.get(Prev_artifact);
			else
			{
				
//				TODO:
				String first_embedding = new ArrayList(token_embeddings.entrySet()).get(0).toString();
				String[] embedding_elements = first_embedding.split("\t");
				for (String element:embedding_elements)
				{
					embedding+="0.00"+"\t";
				}
				embedding = embedding.trim();
			}
		}
		return embedding;
		
	}
	public static void writeToFileForCandidatePhraseExamples
	(List<Integer> exampleIdsToWrite, String filePath,String taskName, Integer rightWindowSize,String RNNPretrainedTokenFeaturesFile) 
		throws Exception
	{
//		if(new File(filePath).exists()) return;
	
		//load the features for each token in a hashmap
		HashMap<Integer, String> token_RNNRepresentations = new HashMap<>();
		List<String> RNNRepLines = FileUtil.loadLineByLine(RNNPretrainedTokenFeaturesFile);
		
		for (String RNNLine:RNNRepLines)
		{
			RNNLine=RNNLine.replaceAll(" +", " ");
			String elements[] = RNNLine.split(" ");
			String reps = "";
			String token_artifact_id = elements[0];
			
//			if (elements.length!=37)
//			{
//				throw new Exception();
//			}
			
			if (elements.length<=2)
			{
				
				for (int i=0;i<35;i++)
				{
					reps+="0.00"+"\t";
				}
			}
			else
			{
				
				
				for (int i=2;i<elements.length;i++)
				{
					reps+=elements[i]+"\t";
				}
			}
		
//			reps=reps.replaceAll("\t$", "").trim();
			token_RNNRepresentations.put(Integer.parseInt(token_artifact_id), reps.trim());
			
		}
		//for each phrase get the start token representation and see what we get
		
		FileWriter file_writer = new FileWriter(filePath+"-"+rightWindowSize);
		int count=0;
		for(Integer example_id : exampleIdsToWrite) {
			
			MLExample example = MLExample.getExampleById(example_id);
			Phrase relatedPhrase = example.getRelatedPhrase();
			String pyLearn2FormatLine="";
			Artifact startArtifact=relatedPhrase.getStartArtifact();
			Artifact endArtifact=relatedPhrase.getEndArtifact();
			
			String startContent=startArtifact.getContent();
			String endContent=endArtifact.getContent();
			if (startArtifact.equals(endArtifact))
			{
				endContent="nonExisting";
			}
			
			Artifact next = relatedPhrase.getEndArtifact().getNextArtifact();
			Artifact prev =  relatedPhrase.getStartArtifact().getPreviousArtifact();
			
			String prev_content = "beginningofsent";
			String next_content = ".";
			String s_prev_content = "beginningofsent";
			String s_next_content = ".";
			String t_prev_content = "beginningofsent";
			String t_next_content = ".";
			
			String ini_rep_value = "0.00	0.00	0.00	0.00	0.00	0.00	0.00	0.00	0.00	0.00"
								+ "	0.00	0.00	0.00	0.00	0.00	0.00	0.00	0.00	0.00	0.00"
								+ "	0.00	0.00	0.00	0.00	0.00	0.00	0.00	0.00	0.00	0.00	0.00	0.00	0.00	0.00	0.00";
			String prev_token_reps=ini_rep_value;
			String next_token_reps = ini_rep_value;
			
			if (prev!=null)
			{
				prev_token_reps = token_RNNRepresentations.get(prev.getArtifactId());
			}
			if (next!=null)
			{
				next_token_reps = token_RNNRepresentations.get(next.getArtifactId());
			}
			pyLearn2FormatLine = prev_token_reps+"\t"
					+wr.getWordRepresentationValuesTabSeparated(startContent,false)+"\t"+"0.00\t"+
					next_token_reps;

			pyLearn2FormatLine = pyLearn2FormatLine.replaceAll("\\t+", "\t").replaceAll("\t$", "");
			
			int expectedClass = example.getExpectedClass()+1; //convert to 1-base (e.g. 0 -> 1)
			
			pyLearn2FormatLine = expectedClass +"\t"+ pyLearn2FormatLine;
			
			System.out.println("**************** count"+count+"/"+exampleIdsToWrite.size());
			file_writer.write( pyLearn2FormatLine+ "\n");
			file_writer.flush();
			count++;
			
			HibernateUtil.clearLoaderSession();
		}
	
		file_writer.flush();
		file_writer.close();
	}
	private static String getPrevTokenEmbeddings(Artifact prev) {
		String word = "</s>";
		if (prev != null)
			word = prev.getContent();
		
		
		return wr.getWordRepresentation(word,"B");
	}
	private static String getNextTokenEmbeddings(Artifact next) {
		String word = "</s>";
		if (next != null)
			word = next.getContent();
		
		wr.getWordRepresentation(word,"N");
		return wr.getWordRepresentation(word,"N");
	}

	public static String getContextEmbedding(Artifact current) throws UnsupportedEncodingException, FileNotFoundException {
		String cur_token = current.getContent();
		List<String> cur_emb = wr.getWordVector(cur_token);
		
		String next_word = "</s>";
		String prev_word = "</s>";
//		Artifact nextArtifact = current.getNextArtifact();
		Artifact prevArtifact = current.getPreviousArtifact();
//		
//		if (nextArtifact != null)
//			next_word = nextArtifact.getContent();
		
		
		if (prevArtifact != null)
			prev_word = prevArtifact.getContent();
		else
		{
			return wr.getWordRepresentation(cur_token, "S");
		}
		List<String> prev_emb = wr.getWordVector(prev_word);
		List<String> next_emb = wr.getWordVector(next_word);
		String sum_of_context = "";
		if (cur_emb.size() != prev_emb.size())
			return wr.getWordRepresentation(cur_token, "S");
			
		for (int i=0;i<cur_emb.size();i++)
		{
			Double numericValue = Double.parseDouble(cur_emb.get(i))
					+ Double.parseDouble(prev_emb.get(i));
//					+Double.parseDouble(next_emb.get(i));
			
			sum_of_context += "S"+"d"+i+"="+numericValue.toString()+"\t";
			
		}
		sum_of_context = sum_of_context.replace("\\t$", "");
	
		
//		String newContextEmd = prev_word+"And"+cur_token+"And"+next_word+" ";
//		for (String dim:sum_of_context)
//		{
//			newContextEmd= newContextEmd+" "+dim;
//		}
//		FileUtil.appendLine("/home/azadeh/projects/java/deext/data/word2VecFiles/vectors.bin", newContextEmd);
		return sum_of_context;
	}
	public static void writeToFileDirect(List<Integer> exampleIdsToWrite, String filePath,String taskName) 
			throws IOException
		{
			if(new File(filePath).exists()) return;
			
			FileWriter file_writer = new FileWriter(filePath);
			int count=0;
			for(Integer example_id : exampleIdsToWrite) {
				
				MLExample example = MLExample.getExampleById(example_id);
				
				String CRFSuiteFormatLine = getArtifactAttributes(example,taskName).trim();
				int expectedClass = example.getExpectedClass()+1; //convert to 1-base (e.g. 0 -> 1)
				
				//is it beginning o end of a sequence?
				String begin_end_seq = "";
				
				Artifact relatedArtifact = example.getRelatedArtifact();
				Artifact next = relatedArtifact.getNextArtifact();
				Artifact prev =  relatedArtifact.getPreviousArtifact();
				
				if (prev == null)
				{
					begin_end_seq = "\t"+"__BOS__";
				}
				else if (next==null)
				{
					begin_end_seq = "\t"+"__EOS__";
				}
				CRFSuiteFormatLine = expectedClass +"\t"+ CRFSuiteFormatLine+begin_end_seq;
				System.out.println("**************** count"+count+"/"+exampleIdsToWrite.size());
				file_writer.write( CRFSuiteFormatLine+ "\n");
				file_writer.flush();
				count++;
				
				HibernateUtil.clearLoaderSession();
			}
		
			file_writer.flush();
			file_writer.close();
		}
	
	static List<String> excludeAttributeIds = new ArrayList<String>();
	static List<String> onlyIncludeAttributes = new ArrayList<String>();
	static{
		
//		excludeAttributeIds.add(FeatureValuePair.FeatureName.TokenContent.name());
//		excludeAttributeIds.add(FeatureValuePair.FeatureName.PrevTokenContent.name());
//		excludeAttributeIds.add(FeatureValuePair.FeatureName.NextTokenContent.name());
//		excludeAttributeIds.add(FeatureValuePair.FeatureName.SecondPrevTokenContent.name());
//		excludeAttributeIds.add(FeatureValuePair.FeatureName.SecondNextTokenContent.name());
		excludeAttributeIds.add(FeatureValuePair.FeatureName.MaxLexiconSim.name());
		
		
		
		excludeAttributeIds.add(FeatureValuePair.FeatureName.GovVerbDeepClassNumber.name());
		
		excludeAttributeIds.add(FeatureValuePair.FeatureName.PositiveTokensInChunk.name());
		excludeAttributeIds.add(FeatureValuePair.FeatureName.NegativeTokensInChunk.name());
		excludeAttributeIds.add(FeatureValuePair.FeatureName.PositiveTokensInNbrChunk.name());
		excludeAttributeIds.add(FeatureValuePair.FeatureName.NegativeTokensInNbrChunk.name());

//		excludeAttributeIds.add(FeatureValuePair.FeatureName.isInLexiconEntry.name());
		excludeAttributeIds.add(FeatureValuePair.FeatureName.BinaryLexiconSim.name());
		excludeAttributeIds.add(FeatureValuePair.FeatureName.MaxPhraseLexSim.name());
//		excludeAttributeIds.add(FeatureValuePair.FeatureName.POS.name());
//		excludeAttributeIds.add(FeatureValuePair.FeatureName.GovVerb.name());
		excludeAttributeIds.add(FeatureValuePair.FeatureName.Token_Lexicon_IDF.name());
//		excludeAttributeIds.add(FeatureValuePair.FeatureName.isTokenInLexicon.name());

		
		
//		onlyIncludeAttributes.add(FeatureValuePair.FeatureName.content.name());
//		excludeAttributeIds.add(FeatureValuePair.FeatureName.hasTermInADRDic.name());
//		excludeAttributeIds.add(FeatureValuePair.FeatureName.NonNormalizedNGram2.name());
		
	}

	public static ArrayList<String> usedFeatureNames = new ArrayList<String>();
	
	private static String getArtifactAttributes(MLExample example, String taskName) {
		
		
		String CRFSuiteFormatLine = "";
		Session old_session = MLExample.hibernateSession;
		MLExample.hibernateSession = HibernateUtil.sessionFactory.openSession();
		List<MLExampleFeature> features = example.getNonEmbeddingsExampleFeatures();
		
		for(int i=0;i<features.size();i++) {
			MLExampleFeature feature = features.get(i);
			FeatureValuePair fvp = feature.getFeatureValuePair();
			int featureIndex = 
					fvp.getTempFeatureIndex();

			if(featureIndex==Integer.MAX_VALUE || featureIndex==-1)
				continue;
////			//TODO: revert the comment		
			if(excludeAttributeIds.contains(fvp.getFeatureName())) 
				{
				continue;
				}
			if(fvp.getFeatureName().matches(".*Embeddings.*") ||
			 fvp.getFeatureName().startsWith("BEmbeddings") || fvp.getFeatureName().startsWith("AEmbeddings"))
			{
			continue;
			}
//////			TODO: revert the comment
//			if(onlyIncludeAttributes.size()>0 && !onlyIncludeAttributes.contains(fvp.getFeatureName())) continue;
			
			String featureValue = fvp.getFeatureValue();
			if(fvp.getFeatureValueAuxiliary() == null)
			{
				Double value = (double) Math.round(Double.parseDouble(fvp.getFeatureValue()) * 100) / 100;
				featureValue = value.toString();
			}
//			if (fvp.getFeatureName().matches("POS"))
//			{
//				if (featureValue.startsWith("NN"))
//				{
//					featureValue= "NN";
//				}
//				else if (featureValue.startsWith("VB"))
//				{
//					featureValue= "VB";
//				}
//			}
			
			
			CRFSuiteFormatLine += fvp.getFeatureName() + "=" + featureValue+ "\t";
			
//			Double numericValue = 0.0;
//			if(fvp.getFeatureValueAuxiliary()!=null)
//				numericValue = Double.parseDouble(fvp.getFeatureValueAuxiliary());
//			else
//				numericValue = Double.parseDouble(fvp.getFeatureValue());
//			
//			if (numericValue != 0 && 
//					!Double.isInfinite(numericValue)) {
////				double maxVal = getAttributeMaxValue(attribute_id);
////				numericValue = numericValue/maxVal;
//				
////				CRFSuiteFormatLine += featureIndex + "=" + numericValue+ "\t";
//				CRFSuiteFormatLine += fvp.getFeatureName() + "=" + fvp.getFeatureValue()+ "\t";
//			}
			HibernateUtil.clearLoaderSession();
			
		}
		MLExample.hibernateSession.clear();
		MLExample.hibernateSession.close();
		MLExample.hibernateSession = old_session;
		return CRFSuiteFormatLine;
	}
	private static String getWordVectorString(String content) {
		String rep = "";
		try {
			Float[] values = BinaryReader.readWordVector(content);
			if (values==null)
			{
				return rep;
			}

			for (int i=0;i<values.length;i++)
			{
				rep+="d"+i+"="+values[i]+"\t";
			}
			rep= rep.replace("\\t$", "");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rep;
	}
	

}
