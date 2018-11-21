package rainbownlp.machineLearning.convertor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import octopus.semantic.similarity.word2vec.util.BinaryReader;

import org.hibernate.Session;

import rainbownlp.core.Artifact;
import rainbownlp.core.FeatureValuePair;
import rainbownlp.core.FeatureValuePair.FeatureName;
import rainbownlp.core.Phrase;
import rainbownlp.core.Setting;
import rainbownlp.machineLearning.MLExample;
import rainbownlp.machineLearning.MLExampleFeature;
import rainbownlp.machineLearning.WordRepresentation;
import rainbownlp.util.HibernateUtil;
import rainbownlp.util.StringUtil;


public class CRFSuiteFormatConvertor {
	static int numClassRatio = Setting.getValueInteger("numClassesRatio");
	public static WordRepresentation wr = new WordRepresentation(Setting.getValue("wordRepresentationVectorPath"));
	
	
	public static void writeToFile(List<Integer> exampleIdsToWrite, String filePath,String taskName,String conceptTypes) 
		throws IOException
	{
		FileWriter file_writer = new FileWriter(filePath);
//			if(new File(filePath).exists()) return;
			
			
		int count=0;
		for(Integer example_id : exampleIdsToWrite) {
			if (taskName.equals("HealthRelatedCandidates"))
			{
				MLExample example = MLExample.getExampleById(example_id);
				Phrase relatedPhrase = example.getRelatedPhrase();
				
//					String gov_verb = example.getFeatureValueForExample(example,FeatureName.GovVerb.name());
				
//					String crfSuiteFormatLine="";
				String crfSuiteFormatLine = getArtifactAttributes(example,taskName).trim();
				crfSuiteFormatLine = crfSuiteFormatLine.replaceAll("\n+", "");
				
				
				int expectedClass = example.getExpectedClass();
	
				expectedClass = expectedClass+1; //convert to 1-base (e.g. 0 -> 1)
				
				//is it beginning o end of a sequence?
				String begin_end_seq = "\t"+"__BOS__"+"\t"+"__EOS__";
				
				
				crfSuiteFormatLine = expectedClass +"\t"+ crfSuiteFormatLine+begin_end_seq;
				System.out.println("**************** count"+count+"/"+exampleIdsToWrite.size());
				file_writer.write( crfSuiteFormatLine+ "\n");
				file_writer.flush();
				count++;
				
				HibernateUtil.clearLoaderSession();
			}
			else{
				MLExample example = MLExample.getExampleById(example_id);
				Artifact relatedArtifact = example.getRelatedArtifact();
				Artifact next = relatedArtifact.getNextArtifact();
				Artifact prev =  relatedArtifact.getPreviousArtifact();
				
				String prev_content = "</s>";
				String next_content = ".";
				String s_prev_content = "</s>";
				String s_next_content = ".";
				
				if (next!=null)
				{
					next_content=next.getContent();
					if (next.getNextArtifact() != null)
					{
						s_next_content = next.getNextArtifact().getContent();
					}
				}
				if (prev!=null)
				{
					prev_content=prev.getContent();
					if (prev.getPreviousArtifact() != null)
					{
						s_prev_content = prev.getPreviousArtifact().getContent();
					}
				}
//					String gov_verb = example.getFeatureValueForExample(example,FeatureName.GovVerb.name());
				
//					String crfSuiteFormatLine="";
				String crfSuiteFormatLine = getArtifactAttributes(example,taskName).trim();
				crfSuiteFormatLine = crfSuiteFormatLine.replaceAll("\n+", "");
				
				int expectedClass = example.getExpectedClass();
				if (conceptTypes != null &&
						conceptTypes.equals("ADR&Indication"))
				{
					
					if (expectedClass==3)
						expectedClass=1;
					else if (expectedClass==4)
						expectedClass=2;
				}
				expectedClass = expectedClass+1; //convert to 1-base (e.g. 0 -> 1)
				
				//is it beginning o end of a sequence?
				String begin_end_seq = "";
				
				if (prev == null)
				{
					begin_end_seq = "\t"+"__BOS__";
				}
				else if (next==null)
				{
					begin_end_seq = "\t"+"__EOS__";
				}
				crfSuiteFormatLine = expectedClass +"\t"+ crfSuiteFormatLine+begin_end_seq;
				System.out.println("Writing features to file "+count+"/"+exampleIdsToWrite.size());
				file_writer.write( crfSuiteFormatLine+ "\n");
				file_writer.flush();
				count++;
				
				HibernateUtil.clearLoaderSession();
			}


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
		excludeAttributeIds.add(FeatureValuePair.FeatureName.GovVerb.name());
		excludeAttributeIds.add(FeatureValuePair.FeatureName.Token_Lexicon_IDF.name());
//		excludeAttributeIds.add(FeatureValuePair.FeatureName.isTokenInLexicon.name());
		
		excludeAttributeIds.add(FeatureValuePair.FeatureName.isNextTokenInLexicon.name());
	    excludeAttributeIds.add(FeatureValuePair.FeatureName.isSNextTokenInLexicon.name());
	    excludeAttributeIds.add(FeatureValuePair.FeatureName.isPrevTokenInLexicon.name());
	    excludeAttributeIds.add(FeatureValuePair.FeatureName.isSPrevTokenInLexicon.name());
		
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
