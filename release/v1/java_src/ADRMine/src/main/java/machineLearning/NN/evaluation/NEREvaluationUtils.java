package machineLearning.NN.evaluation;


import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rainbownlp.util.FileUtil;



public class NEREvaluationUtils {
	public enum TokenLabelIOB2 {
		O(0),
		B_ADR(1),
		I_ADR(2),
		B_IND(3),
		I_IND(4),
		B_BEN(5),
		I_BEN(6);
		private static final Map<Integer,TokenLabelIOB2> lookup = 
			new HashMap<Integer,TokenLabelIOB2>();
		
		static {
	          for(TokenLabelIOB2 l : EnumSet.allOf(TokenLabelIOB2.class))
	               lookup.put(l.getCode(), l);
	     }
		
		private int code;

	     private TokenLabelIOB2(int code) {
	          this.code = code;
	     }

	     public int getCode() { return code; }

	     public static TokenLabelIOB2 getEnum(int code) { 
	          return lookup.get(code); 
	     }	
	     
	}
	public enum TargetSemanticType{
		ADR,
		Indication,
		//healthRelated
		ADR_Indication
	}
	public static String experimentgroup = "ADRConceptTokenSeq";

	public static void main (String args[]) throws Exception
	{	
//		extractLabledEntities(TargetSemanticType.ADR, "/home/azadeh/Documents/expectedIOBLabelsDS.tsv",true);
		convert_predicted_class_to_IOB("/home/azadeh/workspace/ADRMine/predictions_1542410815160.txt",
				"/home/azadeh/workspace/ADRMine/predictions_Jamia_nov_2018_iob2.txt");

	}
	

	//TODO: complete this to handle other entity types
	public static List<Entity> extractLabledEntities(TargetSemanticType targetSemanticType, String labeledTokensFilePath,boolean linesHaveDetails){
		List<String> lines = FileUtil.loadLineByLine(labeledTokensFilePath);
		List<Entity> found_phrases = new ArrayList<>();
		
		
		int line_index=0;
		List<Integer> analysed_lines =new ArrayList<>();
		while(line_index<lines.size()){
			if (analysed_lines.contains(line_index)){
				line_index++;
				continue;
			}
			analysed_lines.add(line_index);
			if (lines.get(line_index).isEmpty())
			{
				line_index++;
				continue;
			}
			String[] cur_token_details = lines.get(line_index).split("\t");
			
			
			int predicted_cur =TokenLabelIOB2.valueOf(cur_token_details[0].replaceAll("-", "_")).getCode();
			
			if (targetSemanticType.equals(TargetSemanticType.ADR))
			{
				if (predicted_cur ==1)
				{
					int start = line_index;
					int end = line_index;
					int cur = start;
					int next = start+1;
					
					
					String phrase_content="";
					if (linesHaveDetails)
						phrase_content = cur_token_details[3];
					String[] next_token_details = lines.get(next).split("\t");
					if (lines.get(next).isEmpty()){
						analysed_lines.add(next);

					}
					else{
						Integer predicted_next  = TokenLabelIOB2.valueOf(next_token_details[0].replaceAll("-", "_")).getCode();
						while (predicted_next ==2)
						{
							
							cur= next;
							next = cur+1;
							cur_token_details = next_token_details;
							analysed_lines.add(cur);
							if (linesHaveDetails)
								phrase_content+=" "+cur_token_details[3];

							if (next<lines.size() && !lines.get(next).isEmpty() ){
								next_token_details = lines.get(next).split("\t");
								predicted_next = TokenLabelIOB2.valueOf(next_token_details[0].replaceAll("-", "_")).getCode();
							}
							else break;
								
						}
					}
					
					end = cur;

					phrase_content= phrase_content.trim();
//					System.out.println(phrase_content);
					Entity extracted_entity=null;
					
					if (linesHaveDetails)
						extracted_entity =new Entity(targetSemanticType,phrase_content,start,end);
					else
						extracted_entity =new Entity(targetSemanticType,start,end);
					
					found_phrases.add(extracted_entity);
				}
				else if (predicted_cur==2){
					int start = line_index-1;
					int end = line_index;
					int cur = line_index;
					int next = start+1;
					
					
					String phrase_content="";
					if (linesHaveDetails)
						phrase_content = cur_token_details[3];
					// build the phrase
					String[] next_token_details = lines.get(next).split("\t");
					if (lines.get(next).isEmpty()){
						analysed_lines.add(next);
//						line_index++;
//						continue;
					}
					else{
						Integer predicted_next  = TokenLabelIOB2.valueOf(next_token_details[0].replaceAll("-", "_")).getCode();
						while (predicted_next ==2)
						{
							
							cur= next;
							next = cur+1;
							cur_token_details = next_token_details;
							analysed_lines.add(cur);
							if (linesHaveDetails)
								phrase_content+=" "+cur_token_details[3];

							if (next<lines.size() && !lines.get(next).isEmpty() ){
								next_token_details = lines.get(next).split("\t");
								predicted_next = TokenLabelIOB2.valueOf(next_token_details[0].replaceAll("-", "_")).getCode();
							}
							else break;
								
						}
					}
					
					end = cur;

					phrase_content= phrase_content.trim();
//					System.out.println(phrase_content);
					Entity extracted_entity=null;
					
					if (linesHaveDetails)
						extracted_entity =new Entity(targetSemanticType,phrase_content,start,end);
					else
						extracted_entity =new Entity(targetSemanticType,start,end);
					
					found_phrases.add(extracted_entity);	
				
					
				}
				
			}
			
			
			
			line_index++;
		}	
		
		return found_phrases;
		 
	}
	public static class Entity{
		public TargetSemanticType semanticType;
		public String phraseContent="";
		public int starIndex;
		public int endIndex;
		public Entity(TargetSemanticType type,String content,int pStartIndex,int pEndIndex){
			this.semanticType= type;
			this.phraseContent = content;
			this.starIndex =pStartIndex;
			this.endIndex =pEndIndex;
		}
		public Entity(TargetSemanticType type,int pStartIndex,int pEndIndex){
			this.semanticType= type;
			
			this.starIndex =pStartIndex;
			this.endIndex =pEndIndex;
		}
	}
	public static boolean areEntitySpansOverlapping(Entity expectedEntity,Entity extractedEntity)
	{
		boolean are_overlapping = false;
		
		int exracted_start=extractedEntity.starIndex;
		int extracted_end = extractedEntity.endIndex;
		
		
		int expected_start=expectedEntity.starIndex;
		int expected_end = expectedEntity.endIndex;
		
//		int start = Math.max(expected_start, exracted_start)-1;
//		int end = Math.min(expected_end, extracted_end)+1;
//		if (end-start>0){
//			are_overlapping=true;
//			return are_overlapping;
//		}
		if (exracted_start>=expected_start
				&& extracted_end<=expected_end)
		{
			are_overlapping=true;
			return are_overlapping;
		}
		//First expand expected span
		expected_start -=2;
		expected_end +=2;
		
		if (exracted_start>=expected_start
				&& extracted_end<=expected_end)
		{
			are_overlapping=true;
			return are_overlapping;
		}
		//then expand extracted span
		exracted_start-=2;
		extracted_end+=2;
		
		if (expected_start>=exracted_start
				&& expected_end<=extracted_end)
		{
			are_overlapping=true;
		}
		if (expectedEntity.phraseContent.matches(".*"+extractedEntity.phraseContent+".*")
				|| extractedEntity.phraseContent.matches(".*"+expectedEntity.phraseContent+".*")){
			if (are_overlapping==false){
				System.out.println(expectedEntity.phraseContent);
			}
			
		}
		else if (are_overlapping==true){
			System.out.println(expectedEntity.phraseContent);
		}
		return are_overlapping;
	}
	public static void convert_predicted_class_to_IOB(String src_predictions_file,String target_prediction_file){
		List<String> lines = FileUtil.loadLineByLine(src_predictions_file);
		List<String> iob_prediction_lines = new ArrayList<>();
		
		for (String line: lines){
			String iob_code = line;
			if (line.matches("^\\d"))
				iob_code = TokenLabelIOB2.getEnum(Integer.parseInt(line)-1).toString();
			iob_prediction_lines.add(iob_code);
		}
		FileUtil.createFile(target_prediction_file, iob_prediction_lines);
	}
	public static HashMap<String, List<Entity>> getTaggedEntities(String labeledFile,String targetType){
		HashMap<String,List<Entity>> sent_entities_map = new HashMap<>();
//		effexor-51d88e8153785f584a9b1708	105	111	ADR	rebound
		List<String> lines = FileUtil.loadLineByLine(labeledFile);
		for (String ann_line: lines){
			
			String[] elements =ann_line.split("\t");
			String associatedID = elements[0];
			String content = elements[4];
			int startIndex = Integer.parseInt(elements[1]);
			int endIndex = Integer.parseInt(elements[2]);
			String type = elements[3];
			if (!targetType.equals(type)){
				continue;
			}
			System.out.println(type);
			
			Entity ent = new Entity(TargetSemanticType.valueOf(type),content,startIndex,endIndex);
			List<Entity> entities = sent_entities_map.get(associatedID);
			if (entities ==null){
				entities = new ArrayList<>();
			}
			entities.add(ent);
			sent_entities_map.put(associatedID, entities);
			
		}
		return sent_entities_map;
	}
}
