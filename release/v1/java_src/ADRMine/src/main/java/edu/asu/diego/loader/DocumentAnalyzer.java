package edu.asu.diego.loader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.prefs.PreferenceChangeEvent;

import rainbownlp.core.Artifact;
import rainbownlp.core.Setting;
import rainbownlp.util.FileUtil;
import rainbownlp.util.HibernateUtil;
import rainbownlp.util.StringUtil;
import rainbownlp.analyzer.IDocumentAnalyzer;
import rainbownlp.analyzer.Tokenizer;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.process.PTBTokenizer;
import preprocessutils.Preprocess;
import preprocessutils.TwitterPreprocessing;



public class DocumentAnalyzer implements IDocumentAnalyzer{
	public boolean isForDemo =false;
	List<Artifact> loadedSentences = new ArrayList<>();
	
	public static void main(String[] args)
	{

		String input_text_files ="/home/azadeh/testFolder";
		
//		String trainingRoot = args[0];
		Setting.TrainingMode = false;
		if(args.length>1 && args[1].equals("test"))
			Setting.TrainingMode = false;

		DocumentAnalyzer doc_proc = new DocumentAnalyzer();
//		doc_proc.processDocuments(input_text_files);
		doc_proc.loadDocumentsFromFlatFile(input_text_files,"\\t","ds");
	}
	


	public void loadDocumentsFromFlatFile(String input_text_file_path,String line_split,String corpusName) {
		
		System.out.println("Loading documents ...");
		
		List<Artifact> loaded_documents = new ArrayList<Artifact>();
		List<String> lines = FileUtil.loadLineByLine(input_text_file_path);
		
		int counter = 0;
		for (String line:lines)
		{
			String[] elements = line.split(line_split);
			if (elements.length < 2) continue;
			
			String text_id = line.split(line_split)[0];
			String content = line.split(line_split)[1];
			
			content = TwitterPreprocessing.filterTweet(content);
			Artifact new_doc = 
	                Artifact.getInstance(Artifact.Type.Document, text_id, 0,corpusName);
			
			try {
				loadSentences(new_doc,content,corpusName);
				System.out.println("loading "+counter+" / "+lines.size());
				HibernateUtil.clearLoaderSession();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
	        loaded_documents.add(new_doc);
	        counter++;
		}

	}


	private  void loadSentences(Artifact parentDoc,String corpusName) throws IOException {
//		Tokenizer docTokenizer = new Tokenizer(parentDoc.getAssociatedFilePath());
		
		String file_content = FileUtil.ReadFileInToString(parentDoc.getAssociatedFilePath());
		Preprocess pre_processed_sent = new Preprocess(file_content);
		
		HashMap<Integer, String> setences = pre_processed_sent.getSentenceIndexMap();
		
		List<Artifact> setencesArtifacts = new ArrayList<Artifact>();
		Artifact previous_sentence = null;
		
		for (Integer sent_index: setences.keySet())
		{	
			String tokenizedSentence = setences.get(sent_index);

			Artifact new_sentence = Artifact.getInstance(Artifact.Type.Sentence,
					parentDoc.getAssociatedFilePath(), sent_index);
			
			new_sentence.setParentArtifact(parentDoc);
			new_sentence.setLineIndex(sent_index);
			new_sentence.setContent(tokenizedSentence);
			if (isForDemo)
			{
				new_sentence.setForDemo(true);
				loadedSentences.add(new_sentence);
			}
			if (previous_sentence != null) {
				new_sentence.setPreviousArtifact(previous_sentence);
				previous_sentence.setNextArtifact(new_sentence);
				HibernateUtil.save(previous_sentence);
			}
			
			HibernateUtil.save(new_sentence);
		
			
			// Hibernate bla bla bla transaction or rollback
			// then always  close your session :

		
			loadWords(new_sentence,sent_index,pre_processed_sent,corpusName);

			setencesArtifacts.add(new_sentence);
			
			previous_sentence = new_sentence;
			HibernateUtil.clearLoaderSession();
		}
//		parentDoc.setChildsArtifact(setencesArtifacts);

	}
	private  void loadSentences(Artifact parentDoc,String documentContent,String corpusName) throws IOException {

		Preprocess pre_processed_sent = new Preprocess(documentContent);
		
		HashMap<Integer, String> setences = pre_processed_sent.getSentenceIndexMap();
		
		List<Artifact> setencesArtifacts = new ArrayList<Artifact>();
		Artifact previous_sentence = null;
		
		for (Integer sent_index: setences.keySet())
		{	
			String tokenizedSentence = setences.get(sent_index);

			Artifact new_sentence = Artifact.getInstance(Artifact.Type.Sentence,
					parentDoc.getAssociatedFilePath(), sent_index);
			
			new_sentence.setParentArtifact(parentDoc);
			new_sentence.setLineIndex(sent_index);
			new_sentence.setContent(tokenizedSentence);
			
			new_sentence.setCorpusName(corpusName);
			if (isForDemo)
			{
				new_sentence.setForDemo(true);
				loadedSentences.add(new_sentence);
			}
			if (previous_sentence != null) {
				new_sentence.setPreviousArtifact(previous_sentence);
				previous_sentence.setNextArtifact(new_sentence);
				HibernateUtil.save(previous_sentence);
			}
			
			HibernateUtil.save(new_sentence);
		
			loadWords(new_sentence,sent_index,pre_processed_sent,corpusName);

			setencesArtifacts.add(new_sentence);
			
			previous_sentence = new_sentence;
			HibernateUtil.clearLoaderSession();
		}
//		parentDoc.setChildsArtifact(setencesArtifacts);

	}
	private void loadWords(Artifact parentSentence, Integer sentIndex, Preprocess pre_processed_sent,String corpusName ) {
		
		List<Artifact> tokensArtifacts = new ArrayList<Artifact>();
		Artifact previous_word = null;
		
		String textContent = "";
		Artifact new_word = null;
			
		List<HasWord> tokens = pre_processed_sent.getSentTokensMap().get(sentIndex);
		
		for(int token_index = 0; token_index< tokens.size();token_index++){
			
			textContent = tokens.get(token_index).toString();
			int start_char = pre_processed_sent.getTokenStartCharIndex(sentIndex, token_index);
			new_word = Artifact.getInstance(
					Artifact.Type.Word, parentSentence.getAssociatedFilePath(),start_char);
			new_word.setContent(textContent);
			new_word.setParentArtifact(parentSentence);
			new_word.setLineIndex(sentIndex);
			new_word.setEndIndex(pre_processed_sent.getTokenEndCharIndex(sentIndex, token_index));
			new_word.setWordIndex(token_index);
			new_word.setCorpusName(corpusName);
			if (isForDemo)
			{
				new_word.setForDemo(true);
			}
			if (previous_word != null) {
				new_word.setPreviousArtifact(previous_word);
				previous_word.setNextArtifact(new_word);
				HibernateUtil.save(previous_word);
			}
			
			HibernateUtil.save(new_word);
				
			tokensArtifacts.add(new_word);
			previous_word = new_word;
			
		}
//		parentSentence.setChildsArtifact(tokensArtifacts);
	}

	@Override
	public int processDocuments(String rootPath){
		int numberOfInstances = 0;
		
//		loadDocuments(rootPath);
//		
////		Tokenizer.fixDashSplitted();
//		
		return numberOfInstances;
	}



	@Override
	public int processDocuments(String rootPath, String corpusName) {
		// TODO Auto-generated method stub
		return 0;
	}

}
