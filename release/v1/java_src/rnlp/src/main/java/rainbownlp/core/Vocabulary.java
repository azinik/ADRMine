package rainbownlp.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rainbownlp.core.Artifact.Type;
import rainbownlp.util.FileUtil;

public class Vocabulary {
	public HashMap<String, Integer> vocabIndexMap = new HashMap<>(); 
	
	public static void main (String[] args)
	{
		Vocabulary v =  new Vocabulary();
		v.loadVocab();
		
		Integer index = v.getWordIndexInVocab("anxiety");
		System.out.println("here is the index"+index);
	}
	public void loadVocab()
	{
		String vocanFilePath = "/home/azadeh/projects/java/drug-effect-ext/data/Twitter/vocabulary.txt";
		if (FileUtil.fileExists(vocanFilePath))
		{
			vocabIndexMap = loadVocabFromFile(vocanFilePath);
			
		}
		else
		{
			List<String> vocabLines = new ArrayList<>();
			
			List<Artifact> all_words = new ArrayList<>();
			all_words = Artifact.listByTypeByForTrain(Type.Word, true);
			int word_index=0;
			for (Artifact word: all_words)
			{
				String content =  word.getContent().toLowerCase();
				
				Integer index = vocabIndexMap.get(content);
				if (index == null)
				{
					index = word_index;
					vocabIndexMap.put(content, index);
					word_index++;
				}
			}
			for (String word:vocabIndexMap.keySet())
			{
				String line =  word +"\t"+vocabIndexMap.get(word).toString();
				vocabLines.add(line);
				System.out.println(line);
			}
			FileUtil.createFile(vocanFilePath, vocabLines);
		}
		
	}
	private HashMap<String, Integer> loadVocabFromFile(String vocanFilePath) {
		
		HashMap<String, Integer> word_indexes = new HashMap<>();
		List<String> vocab_lines =  FileUtil.loadLineByLine(vocanFilePath);
		for (String vocab:vocab_lines)
		{
			String word = vocab.split("\t")[0];
			Integer index = Integer.parseInt(vocab.split("\t")[1]);
			word_indexes.put(word, index);
		}
		return word_indexes;
	}
	public Integer getWordIndexInVocab(String word)
	{
		Integer index = 0;
		if (vocabIndexMap.isEmpty())
		{
			loadVocab();
		}
		index= vocabIndexMap.get(word);
		if (index == null)
			index =-1;
		return index;
	}
}

