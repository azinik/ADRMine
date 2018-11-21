package rainbownlp.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rainbownlp.core.Phrase;
import rainbownlp.parser.DependencyLine;


public class StanfordDependencyUtil {
	public static Boolean haveDirectRelation(String target1, String target2,
			ArrayList<DependencyLine> dependencies)
	{
		Boolean has_relation = false;
		for (DependencyLine dep_line:dependencies)
		{
//			TODO: remove
//			if (target1==null || target2==null)
//				continue;
			if ((dep_line.firstPart.equals(target1) &&
					dep_line.secondPart.equals(target2))
					|| (dep_line.firstPart.equals(target2) &&
							dep_line.secondPart.equals(target1)))
			{
				has_relation =true;
				break;
			}
				
		}
		return has_relation;
	}
	public static Boolean haveDirectRelation(Phrase p1, Phrase p2,
			ArrayList<DependencyLine> dependencies)
	{
		Boolean has_relation = false;
		String target1 = p1.getNormalizedHead();
		String target2 = p2.getNormalizedHead();
		Integer offset1 = p1.getNormalOffset()+1;
		Integer offset2 = p2.getNormalOffset()+1;
		for (DependencyLine dep_line:dependencies)
		{
			if ((dep_line.firstPart.equals(target1) &&
					dep_line.firstOffset==offset1 &&
					dep_line.secondPart.equals(target2)
					&& dep_line.secondOffset == offset2)
					|| (dep_line.firstPart.equals(target2) &&
						dep_line.firstOffset == offset2 &&
						dep_line.secondOffset ==offset1 &&
							dep_line.secondPart.equals(target1)))
			{
				has_relation =true;
				break;
			}
				
		}
		return has_relation;
	}
	public static Boolean haveDirectRelation(String target1, Integer offset1,
			String target2, Integer offset2,
			ArrayList<DependencyLine> dependencies)
	{
		Boolean has_relation = false;
		for (DependencyLine dep_line:dependencies)
		{
			if ((dep_line.firstPart.equals(target1) &&
					dep_line.firstOffset==offset1 &&
					dep_line.secondPart.equals(target2)
					&& dep_line.secondOffset == offset2)
					|| (dep_line.firstPart.equals(target2) &&
						dep_line.firstOffset == offset2 &&
						dep_line.secondOffset ==offset1 &&
							dep_line.secondPart.equals(target1)))
			{
				has_relation =true;
				break;
			}
				
		}
		return has_relation;
	}
	//this method returns all governor Dependency lines to a target
	public static List<DependencyLine> getAllGovernors(
			ArrayList<DependencyLine> dep_lines, String target)
	{
		List<DependencyLine> gov_deps = new ArrayList<DependencyLine> ();
		for (DependencyLine dep: dep_lines)
		{
			if (dep.secondPart.equals(target))
			{
				gov_deps.add(dep);
			}
		}
		return gov_deps;
	}
	public static List<DependencyLine> getAllGovernors(
			ArrayList<DependencyLine> dep_lines, String target, int offset)
	{
		List<DependencyLine> gov_deps = new ArrayList<DependencyLine> ();
		for (DependencyLine dep: dep_lines)
		{
//			if (dep.secondPart.equals(target) && dep.secondOffset==offset)
			if (dep.secondOffset==offset)
			{
				gov_deps.add(dep);
			}
		}
		return gov_deps;
	}

	public static List<DependencyLine> getAllDependents(
			ArrayList<DependencyLine> dep_lines, String target, int offset)
	{
		List<DependencyLine> dependent_deps = new ArrayList<DependencyLine> ();
		for (DependencyLine dep: dep_lines)
		{
//			if (dep.firstPart.equals(target) && dep.firstOffset==offset)
			if (dep.firstOffset==offset)
			{
				dependent_deps.add(dep);
			}
		}
		return dependent_deps;
	}

	public static ArrayList<DependencyLine> parseDepLinesFromString(
			String dependency_string) {
		if (dependency_string== null) return new ArrayList<>();
		ArrayList<DependencyLine> dep_lines = new ArrayList<DependencyLine>();
		String[] dependencies; 
		
		dependencies = dependency_string.split("\n");
		
		for(String dependency:dependencies)
		{
			DependencyLine curLine = parseDependencyLine(dependency);
			dep_lines.add(curLine);
		}
		return dep_lines;
	}

	public static Boolean haveGovernorInCommon(
			List<DependencyLine> target1_govs, List<DependencyLine> target2_govs) {
		Boolean have_gov_in_common= false;
		for (DependencyLine dep1: target1_govs)
			for (DependencyLine dep2: target2_govs)
			{
				if(dep1.firstPart.equals(dep2.firstPart)
						&& dep1.firstOffset==dep2.firstOffset)
				{
					have_gov_in_common= true;
					return have_gov_in_common;
				}
					
			}
		return have_gov_in_common;
	}
	//TODO : change this to consider offset also
	public static Boolean areConjuncted(ArrayList<DependencyLine> dep_lines,String target1, String target2,
			String connector) {
		Boolean are_conjucted= false;
		for (DependencyLine dep_line: dep_lines)
		{
			if (dep_line.relationName.equals("conj_"+connector))
			{
				if((dep_line.firstPart.equals(target1) && dep_line.secondPart.equals(target2))
						|| (dep_line.firstPart.equals(target2) && dep_line.secondPart.equals(target1)))
						{
							are_conjucted = true;
							break;
						}
			}
		}
		return are_conjucted;
	}
	public static Boolean areConjuncted(ArrayList<DependencyLine> dep_lines,
			Phrase p1, Phrase p2,
			String connector) {
		Boolean are_conjucted= false;
		String target1 = p1.getNormalizedHead();
		String target2 = p2.getNormalizedHead();
		Integer offset1 = p1.getNormalOffset()+1;
		Integer offset2 = p2.getNormalOffset()+1;
		
		for (DependencyLine dep_line: dep_lines)
		{
			if (dep_line.relationName.equals("conj_"+connector))
			{
				if((dep_line.firstPart.equals(target1) && dep_line.firstOffset==offset1 && dep_line.secondPart.equals(target2) && dep_line.secondOffset==offset2)
						|| (dep_line.firstPart.equals(target2) && dep_line.secondPart.equals(target1)
								&& dep_line.firstOffset==offset2 && dep_line.secondOffset==offset1))
						{
							are_conjucted = true;
							break;
						}
			}
		}
		return are_conjucted;
	}
	public static DependencyLine parseDependencyLine(String dependency) {
		DependencyLine res = new DependencyLine();
//		String[] depParts = dependency.split("\\(|\\)|(, )");
		
		Pattern p = Pattern.compile("(.+)\\((.*)\\-(\\d+)(\\D+)?, (.*)\\-(\\d+)(\\D+)?\\)");
		Matcher m = p.matcher(dependency);
		if(m.matches())
		{
			res.relationName = m.group(1);
			
			res.firstPart = m.group(2);
			res.firstOffset = Integer.parseInt(m.group(3));
			
			res.secondPart = m.group(5);
			res.secondOffset = Integer.parseInt(m.group(6));
		}
		return res;
	}
	public static Boolean areDirectlyConnected(String first_string, int first_offset,
			String second_string, int second_offset, ArrayList<DependencyLine> dep_lines)
	{
		Boolean are_connected=  false;
		for(DependencyLine dep_line:dep_lines)
		{
			if ((((dep_line.firstPart.equals(first_string) && dep_line.firstOffset==first_offset)) &&
					(dep_line.secondPart.equals(second_string) && dep_line.secondOffset== second_offset))
					|| ((dep_line.firstPart.equals(second_string) && dep_line.firstOffset==second_offset ) &&
							(dep_line.secondPart.equals(first_string) && dep_line.secondOffset== first_offset)))
			{
				are_connected =true;
				break;
			}
		}
		return are_connected;
	}
//	TODO:This is not checking the offset!
	public static Boolean areGovernorsDirectlyConnected(String target1,String target2, ArrayList<DependencyLine> dep_lines)
	{
		Boolean are_connected=  false;
		List<DependencyLine> target1_govs = getAllGovernors(dep_lines, target1);
		List<DependencyLine> target2_govs = getAllGovernors(dep_lines, target2);
		
		for(DependencyLine target1_gov:target1_govs)
		{
			for(DependencyLine target2_gov:target2_govs)
			{
				if (areDirectlyConnected(target1_gov.firstPart, target1_gov.firstOffset,
						target2_gov.firstPart, target2_gov.firstOffset, dep_lines))
				{
					are_connected =true;
					return are_connected;
				}
			}
		}
		//check if they are directly connected
		
		return are_connected;
	}
	public static HashMap<Integer, String> getTokens(String pos_tags) throws Exception
	{
		HashMap<Integer, String> tokensMap = new HashMap<Integer, String>();
		
		String[] words_tags = pos_tags.split(" ");
		Pattern p = Pattern.compile("(.*)\\/([^\\/]+)");

		for(int i=0;i<words_tags.length;i++){
			Matcher m = p.matcher(words_tags[i]);
			if (m.matches())
			{
				String content = m.group(1);
				String word =  content.replaceAll("\\\\/", "/");
				tokensMap.put(i+1, word);
			}
			else
			{
				throw (new Exception("the POS tag doesn't match the pattern"));
			}
			
		}
		return tokensMap;
	}
	public static HashMap<Integer, String> getLemmaMap(String pos_tags) throws Exception
	{
		HashMap<Integer, String> lemmaMap = new HashMap<Integer, String>();
		
		String[] words_tags = pos_tags.split(" ");
		Pattern p = Pattern.compile("(.*)\\/([^\\/]+)");
		
		for(int i=0;i<words_tags.length;i++){
			Matcher m = p.matcher(words_tags[i]);
			if (m.matches())
			{
				String content = m.group(1);
				String word =  content.replaceAll("\\\\/", "/");
				lemmaMap.put(i+1, StringUtil.getWordLemma(word));
			}
			else
			{
				throw (new Exception("the POS tag doesn't match the pattern"));
			}
				
		}
		
		return lemmaMap;
	}
	public static HashMap<String, String> getLemmaTokenmaps(String pos_tags) throws Exception
	{
		HashMap<String, String> lemma_token_map = new HashMap<String, String>();
		
		String[] words_tags = pos_tags.split(" ");
		Pattern p = Pattern.compile("(.*)\\/([^\\/]+)");
		
		for(int i=0;i<words_tags.length;i++){
			Matcher m = p.matcher(words_tags[i]);
			if (m.matches())
			{
				String content = m.group(1);
				String word =  content.replaceAll("\\\\/", "/");
				lemma_token_map.put(StringUtil.getWordLemma(word), word);
			}
			else
			{
				throw (new Exception("the POS tag doesn't match the pattern"));
			}
				
		}
		return lemma_token_map;
	}
	//  prep_on(admitted-19, date-23) returns this line for date
	public static List<DependencyLine> getRelatedGovsInPrep(String content, int offset,ArrayList<DependencyLine> dep_lines)
	{
		List<DependencyLine> prep_govs = new ArrayList<DependencyLine>();
		List<DependencyLine> all_govs = getAllGovernors(dep_lines,content,offset);
		for(DependencyLine gov: all_govs)
		{
			if (gov.relationName.startsWith("prep_"))
			{
				prep_govs.add(gov);
			}
		}
		return prep_govs;
	}
	//Time related governers now are advmod and prep_
	public static List<DependencyLine> getTimeRelatedGovs(String content, int offset,ArrayList<DependencyLine> dep_lines)
	{
		List<DependencyLine> time_rel_govs = new ArrayList<DependencyLine>();
		List<DependencyLine> all_govs = getAllGovernors(dep_lines,content,offset);
		for(DependencyLine gov: all_govs)
		{
			if (gov.relationName.startsWith("prep_on")
					|| gov.relationName.startsWith("prep_of")
					|| gov.relationName.startsWith("prep_at") 
					|| gov.relationName.startsWith("prep_in"))
			{
				time_rel_govs.add(gov);
			}
			else if(gov.relationName.startsWith("advmod") 
					|| gov.relationName.startsWith("rel") )
			{
				time_rel_govs.add(gov);
			}
		}
		return time_rel_govs;
	}
	// this will return the whole dep line but we know that we look for the second part
	public static List<DependencyLine> getRelatedDependentsInPrep(String content, int offset,ArrayList<DependencyLine> dep_lines)
	{
		List<DependencyLine> prep_dependents = new ArrayList<DependencyLine>();
		List<DependencyLine> all_dependents =getAllDependents(dep_lines, content, offset);
		for(DependencyLine dependent: all_dependents)
		{
			if (dependent.relationName.startsWith("prep_"))
			{
				prep_dependents.add(dependent);
			}
		}
		return prep_dependents;
	}
	public static List<DependencyLine> getTimeRelatedDependents(String content, int offset,ArrayList<DependencyLine> dep_lines)
	{
		List<DependencyLine> time_rel_deps = new ArrayList<DependencyLine>();
		List<DependencyLine> all_deps = getAllDependents(dep_lines,content,offset);
		for(DependencyLine dependent: all_deps)
		{
			if (dependent.relationName.startsWith("prep_on")
					|| dependent.relationName.startsWith("prep_of")
					|| dependent.relationName.startsWith("prep_at") 
					|| dependent.relationName.startsWith("prep_in"))
			{
				time_rel_deps.add(dependent);
			}

		}
		return time_rel_deps;
	}
	public static HashMap<Integer, String> getRelatedWords(String content, int offset,ArrayList<DependencyLine> dep_lines)
	{
		HashMap<Integer, String> related_words = new HashMap<Integer, String>();
		List<DependencyLine> dependents = getAllDependents(dep_lines, content, offset);
		for(DependencyLine dep: dependents)
		{
			related_words.put(dep.secondOffset, dep.secondPart);
		}
		List<DependencyLine> governers  = getAllGovernors(dep_lines,content,offset);
		for(DependencyLine gov: governers)
		{
			related_words.put(gov.firstOffset, gov.firstPart);
		}
		return related_words;
	}
	public static List<DependencyLine> getAllRelatedDepLines(
			ArrayList<DependencyLine> dep_lines, String target, int offset)
	{
		List<DependencyLine> related = new ArrayList<DependencyLine> ();
		for (DependencyLine dep: dep_lines)
		{
			if ((dep.secondPart.equals(target) && dep.secondOffset==offset)
				|| (dep.firstPart.equals(target) && dep.firstOffset==offset))
			{
				related.add(dep);
			}
		}
		return related;
	}
//	prep_on(admitted,date) returns admitted or prep_of(date,admission)
	public static List<String> getAllArgsInPrep
		(String content, int offset,ArrayList<DependencyLine> dep_lines)
	{
		List<String> args_in_prep = new ArrayList<String>();
		
		List<DependencyLine> dependents_in_pre = StanfordDependencyUtil.getRelatedDependentsInPrep
			(content, offset, dep_lines);
		for (DependencyLine dependent: dependents_in_pre)
		{
			args_in_prep.add(dependent.secondPart);
		}
		List<DependencyLine> govs_in_pre = StanfordDependencyUtil.getRelatedGovsInPrep
		  (content, offset, dep_lines);
		for(DependencyLine gov_dep : govs_in_pre)
		{
			args_in_prep.add(gov_dep.firstPart);
		}
		return args_in_prep;
	}
	//just returns the dep line of p1 is gov
	public static DependencyLine getRelatedDependencyBetween(Phrase phrase1, Phrase Phrase2, ArrayList<DependencyLine> dep_lines)
	{
		DependencyLine between_dep_line= null;
		String target1 = phrase1.getNormalizedHead();
		String target2 = Phrase2.getNormalizedHead();
		Integer offset1 = phrase1.getNormalOffset()+1;
		Integer offset2 = Phrase2.getNormalOffset()+1;
		for(DependencyLine dep_line:dep_lines)
		{
			if ((((dep_line.firstPart.equals(target1) && dep_line.firstOffset==offset1)) &&
					(dep_line.secondPart.equals(target2) && dep_line.secondOffset== offset2)))
			{
				between_dep_line =dep_line;
				break;
			}
		}
		return between_dep_line;
	}
}
