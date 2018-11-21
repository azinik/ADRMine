package rainbownlp.analyzer;

import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import rainbownlp.core.Artifact;
import rainbownlp.util.FileUtil;

import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.objectbank.TokenizerFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.WordTokenFactory;

public class Tokenizer {
	String txt_file_path;
	String tokenization_file;
	String original_txt_content = "";
	String compressedText;
	public static void main(String[] args) throws SQLException
	{
		fixDashSplitted();
	}
	static TokenizerFactory<Word> tf;
	
	public static List<Word> getTokens(String sentence)
	{
		if(tf == null)
			tf = PTBTokenizer.factory(false, new WordTokenFactory());
		
		List<Word> tokens_words = tf.getTokenizer(new StringReader(sentence)).tokenize();
		
		
		return tokens_words;
	}
	
	public static void fixDashSplitted() throws SQLException
	{
//		String q = "SELECT * from Artifact where text_content like '%-%' and artifact_type = 'Word'";
//		ResultSet artifacts = Util.db.executeReader(q);
//		while(artifacts.next())
//		{
//			int artifact_id = artifacts.getInt("artifact_id");
//			Artifact curArti = new Artifact(artifact_id);
//			fixMergedNameEntity(curArti);
//		}
	}
	public ArrayList<String> paragraphs = new ArrayList<String>();
	public HashMap<Integer, String> sentences = new HashMap<Integer, String>();
	public HashMap<Integer,List<Integer>> sentences_tokens_indexes = new HashMap<Integer, List<Integer>>();
	public HashMap<Integer,List<Word>> sentences_tokens = new HashMap<Integer,List<Word>>();
	
	public static void fixMergedNameEntity(Artifact curArtifact) throws SQLException
	{
//		String originalContent = curArtifact.getTextContent();
//		String[] parts = originalContent.split("-");
//		String previousContent = "";
//		for(int i=0;i<parts.length;i++)
//		{
//			String content = parts[i];
//			int j=i;
//			do
//			{
//				if((NameEntityTable.possibleNameEntity(content)||
//						EventTriggers.isPossibleTrigger(content))
//						&& !content.equals(originalContent) &&
//						content.length()>1)
//				{
//					Util.log("Fixing:"+originalContent, Level.INFO);
//					if(i==0)
//					{//NE is at the beginning
//						//shorten current artifact
//						curArtifact.setTextContent(content);
//						//add a new artifact at the end 
//						String remainingContent = "";
//						for(int k=j+1;k<parts.length;k++){
//							if(!remainingContent.equals(""))
//								remainingContent+= "-";
//							remainingContent+=parts[k];
//						}
//						
//						Artifact dashArtifact = new Artifact("-",
//								Type.Word, curArtifact.associatedFilePath,
//								curArtifact.getStartIndex()+content.length(),
//								curArtifact.getParentArtifact());
//						if(remainingContent.equals(""))
//						{
//							dashArtifact.setNextArtifact(curArtifact.getNextArtifact());
//							curArtifact.setNextArtifact(dashArtifact);
//						}else{
//							Artifact neArtifact = new Artifact(remainingContent,
//									Type.Word, curArtifact.associatedFilePath,
//									curArtifact.getStartIndex()+content.length()+1,
//									curArtifact.getParentArtifact());
//							neArtifact.setNextArtifact(curArtifact.getNextArtifact());
//							curArtifact.setNextArtifact(dashArtifact);
//							dashArtifact.setNextArtifact(neArtifact);
//						}
//						
//						
//					}
//					if(parts.length>1 && 
//							i==(parts.length-1))
//					{//NE is at the end
//						//shorten current artifact
//						Artifact neArtifact = new Artifact(content,
//								Type.Word, curArtifact.associatedFilePath,
//								curArtifact.getStartIndex()+previousContent.length()+1,
//								curArtifact.getParentArtifact());
//						
//						if(previousContent.equals(""))
//						{
//							curArtifact.setTextContent("-");
//							neArtifact.setNextArtifact(curArtifact.getNextArtifact());
//							curArtifact.setNextArtifact(neArtifact);
//						}else{
//							curArtifact.setTextContent(previousContent);
//							//add a new artifact at the end 
//							
//							Artifact dashArtifact = 
//								new Artifact("-", Type.Word, curArtifact.associatedFilePath,
//									curArtifact.getStartIndex()+previousContent.length(),
//									curArtifact.getParentArtifact());
//							
//							neArtifact.setNextArtifact(curArtifact.getNextArtifact());
//							curArtifact.setNextArtifact(dashArtifact);
//							dashArtifact.setNextArtifact(neArtifact);
//						}
//					}
//					return;
//				}
//				j++;
//				if(j<parts.length)
//					content+="-"+parts[j];
//			}while(j<parts.length);
//			if(!previousContent.equals(""))
//				previousContent+="-";
//			previousContent += parts[i];
//		}
	}
	public Tokenizer(String associatedFilePath) throws IOException {
		txt_file_path = associatedFilePath;
		tokenization_file = txt_file_path.replace(".txt", ".tok");
		if(FileUtil.fileExists(tokenization_file))
			processFileWithTokenization(tokenization_file);
		else
			processFile();
	}
	
	
	public void processFile() throws IOException{
		List<String> lines = FileUtil.loadLineByLine(txt_file_path);
		
		int sentence_start=0;
		for(int line_number = 0;line_number<lines.size();line_number++){
			String line = lines.get(line_number);
			List<Word> tokensInSentence = getTokens(line);
			ArrayList<Integer> tokens_indexes = new ArrayList<Integer>();
			
			for(int token_index = 0;token_index<tokensInSentence.size();token_index++)
			{
				tokens_indexes.add(tokensInSentence.get(token_index).beginPosition()+sentence_start+line_number+1);
			}
			
			sentences_tokens_indexes.put(line_number, tokens_indexes);
			sentences_tokens.put(line_number, tokensInSentence);
			sentences.put(line_number, line);
			sentence_start+= line.length();
		}
		
	}
	
	public void processFileWithTokenization(String tokenizationFilePath) throws IOException{
		List<String> lines_tokenized = 
			FileUtil.loadLineByLine(tokenizationFilePath);
		
		original_txt_content = 
			FileUtil.readWholeFile(txt_file_path).replaceAll("\\s+", " ");
		compressedText = 
			original_txt_content.replaceAll(" |\\n", "");
		if(compressedText.equals("")) return;
		
		int curIndex = 0;
		String compressed_original_sofar = ""+original_txt_content.charAt(curIndex);
		String compressed_tokenized_sofar = "";
		int sentence_start=0;
		for(int line_number = 0;line_number<lines_tokenized.size();line_number++){
			String line = lines_tokenized.get(line_number);
			List<Word> tokensInSentence = new ArrayList<Word>();
			String[] tokensInSentence_str = line.split(" ");
			ArrayList<Integer> tokens_indexes = new ArrayList<Integer>();
			
			for(int token_index = 0;token_index<tokensInSentence_str.length;token_index++)
			{
				if(!tokensInSentence_str[token_index].equals("")) 
				{
					String tmp_compressed_tokenized_sofar = 
						compressed_tokenized_sofar + 
						tokensInSentence_str[token_index].charAt(0);
					while(!compressed_original_sofar.equals(
							tmp_compressed_tokenized_sofar))
					{
						do{
							curIndex++;
							compressed_original_sofar += 
								original_txt_content.charAt(curIndex);
						}while(original_txt_content.charAt(curIndex) == ' ');
						compressed_original_sofar = 
							compressed_original_sofar.replaceAll(" |\\n", "");
					}
				}
				
				tokens_indexes.add(curIndex);
				tokensInSentence.add(new 
						Word(tokensInSentence_str[token_index], curIndex, 
								curIndex+tokensInSentence_str[token_index].length()));
				compressed_tokenized_sofar += tokensInSentence_str[token_index];
			}
			
			sentences_tokens_indexes.put(line_number, tokens_indexes);
			sentences_tokens.put(line_number, tokensInSentence);
			sentences.put(line_number, line);
			sentence_start+= line.length();
		}
		
	}
	public HashMap<Integer, String>  getSentences() {
		
		return sentences;
	}

}
