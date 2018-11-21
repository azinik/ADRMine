package rainbownlp.machineLearning;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import rainbownlp.util.FileUtil;
import rainbownlp.util.StringDistance;
import rainbownlp.util.StringUtil;

public class WordRepresentation {
	//when this class is created it opens the word vectors file 
	public HashMap<String, List<String>> wordVectors = new HashMap<String, List<String>>();
	public WordRepresentation(String vectorFilePath)
	{
		List<String> vector_file_lines = FileUtil.loadLineByLine(vectorFilePath);
		int line_count=0;
		for (String line: vector_file_lines)
		{
			line_count++;
			if (line_count==1) continue;
			String [] arr = line.split(" ", 2);

			String word =arr[0];
			String embeddings = arr[1];
			List<String> elements  =Arrays.asList(embeddings);
		
			wordVectors.put(word, elements);	
			
		}
	}
	
	public String getWordRepresentation(String word, String option)
	{
		String rep = "";
		List<String> values = wordVectors.get(StringUtil.getTermByTermWordnet(word));
//		if (word.matches("wors.*"))
//		{
//			values = wordVectors.get("worsen");
//		}
		if (values==null)
		{
			values = wordVectors.get(word);

		}
		
		if (values==null)
		{
			
			String most_simi_entry = getWordWithHighestSimilarity(word);
			if (!most_simi_entry.equals(""))
				values = wordVectors.get(most_simi_entry);

		}
		if (values==null)
		{
			
			
			values = wordVectors.get("</s>");
			for (int i=0;i<values.size();i++)
			{
//				Double value = (double) Math.round(Double.parseDouble(values.get(i)) * 100) / 100;
//				rep+=option+"d"+i+"="+value+"\t";
				rep+=option+"d"+i+"="+"0.00"+"\t";
			}
			return rep;
			
		}
	
		for (int i=0;i<values.size();i++)
		{
//			Double value = (double) Math.round(Double.parseDouble(values.get(i)) * 100) / 100;
//			rep+=option+"d"+i+"="+value+"\t";
			rep+=option+"d"+i+"="+values.get(i)+"\t";
		}
		rep= rep.replace("\\t$", "");
		return rep;
	}
	public String getWordRepresentationValuesTabSeparated(String word, boolean NormalizeDigits)
	{
		String rep = "";
		if(NormalizeDigits)
		{
			word = word.replaceAll("\\d", "d");
		}
		List<String> values = wordVectors.get(StringUtil.getTermByTermWordnet(word));

		if (values==null)
		{
			values = wordVectors.get(word);

		}
		if (values==null)
		{
			values = wordVectors.get("<UNK>");
//			String most_simi_entry = getWordWithHighestSimilarity(word);
//			if (!most_simi_entry.equals(""))
//				values = wordVectors.get(most_simi_entry);

		}
//		if (values==null)
//		{
//			
//			String most_simi_entry = getWordWithHighestSimilarity(word);
//			if (!most_simi_entry.equals(""))
//				values = wordVectors.get(most_simi_entry);
//
//		}
//		if (values==null)
//		{
//			
//			
//			values = wordVectors.get("i");
//			for (int i=0;i<values.size();i++)
//			{
////				Double value = (double) Math.round(Double.parseDouble(values.get(i)) * 100) / 100;
////				rep+=option+"d"+i+"="+value+"\t";
//				rep+="0.00"+"\t";
//			}
//			return rep;
//			
//		}
		rep = values.toString().replaceAll(",","").replaceAll("\\[|\\]", "");
		rep = rep.replaceAll(" ", "\t").trim();
//		for (int i=0;i<values.size();i++)
//		{
////			Double value = (double) Math.round(Double.parseDouble(values.get(i)) * 100) / 100;
////			rep+=option+"d"+i+"="+value+"\t";
//			rep+=values.get(i)+"\t";
//		}
//		rep= rep.replace("\\t$", "");
		return rep;
	}
	public List<String> getWordVector(String word)
	{
		
		List<String> values = wordVectors.get(StringUtil.getTermByTermWordnet(word));
		if (values==null)
		{
			return new ArrayList<String>();
		}
	
		return values;
	}
//get the word with the smallest distance to the word
	public String  getWordWithHighestSimilarity(String targetWord)
	{
		targetWord = StringUtil.getTermByTermWordnet(targetWord);
		int count=0;
		String most_similar = "";
		HashMap<String, Integer> entry_similary_map = new HashMap<>();
		//get all the words
		for (String word:wordVectors.keySet())
		{
			int sim =StringDistance.distance(targetWord,word);
			entry_similary_map.put(word, sim);
		}
		Map.Entry<?, Integer> mapEntry =sortAndReturnFirst(entry_similary_map);
		try {
			FileUtil.appendLine("/tmp/sim.txt", targetWord+"   "+mapEntry.toString());
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (mapEntry.getValue() <=3)
		{
			most_similar = mapEntry.getKey().toString();
		}
		return most_similar;
	}
	  public   Map.Entry<?, Integer> sortAndReturnFirst(HashMap<?, Integer> t){

	       //Transfer as List and sort it
	       ArrayList<Map.Entry<?, Integer>> l = new ArrayList(t.entrySet());
	       Collections.sort(l, new Comparator<Map.Entry<?, Integer>>(){

	         public int compare(Map.Entry<?, Integer> o1, Map.Entry<?, Integer> o2) {
	            return o1.getValue().compareTo(o2.getValue());
	        }});
	      
	       return l.get(0);
	    }

	public String getSumVerbTokenRepresentation(String gov_verb, String token) {
		String rep ="";
		List<String> values = wordVectors.get(StringUtil.getTermByTermWordnet(token));

		if (values==null)
		{
			values = wordVectors.get(token);

		}
		
		if (values==null)
		{
			String most_simi_entry = getWordWithHighestSimilarity(token);
			values = wordVectors.get(most_simi_entry);

		}
		List<String> values2 = wordVectors.get(StringUtil.getTermByTermWordnet(gov_verb));
		if (values2==null)
		{
			String most_simi_entry = getWordWithHighestSimilarity(gov_verb);
			values2 = wordVectors.get(most_simi_entry);

		}
		List<String> sum_values = new ArrayList<>();
		for (int i=0;i<values.size();i++)
		{
			Double sum = Double.parseDouble(values.get(i))+ Double.parseDouble(values2.get(i));
			sum_values.add(i, sum.toString());
			rep+="d"+i+"="+sum+"\t";
		}
		rep= rep.replace("\\t$", "");
		return rep;
	}

}
