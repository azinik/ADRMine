package rainbownlp.analyzer.sentenceclause;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rainbownlp.core.Artifact;
import rainbownlp.parser.DependencyLine;
import rainbownlp.parser.StanfordParser;
import rainbownlp.util.StanfordDependencyUtil;


public class SentenceClauseManager {

	private Artifact relatedSentence;
	private String sentContent;
	
	private String posTags;
	private String stanDependenciesStr;
	
	public ArrayList<DependencyLine> sentDepLines = new ArrayList<DependencyLine>();

	ArrayList<Clause> clauses;

	public HashMap<Integer, String> offsetMap = new HashMap<Integer, String>();
	
	//this keeps the offsets as the key and the value is the lemma
	public HashMap<Integer, String> lemmaMap = new HashMap<Integer, String>();
	
	// the same as above just has all the original tokens
	public HashMap<Integer, String> tokenMap = new HashMap<Integer, String>();
	
	//the mapping of lemmas to originals
	//TODO: if we have repeated that are different originals it will be overwritten
	public HashMap<String, String> lemmaTokenMap = new HashMap<String, String>();
	
	// this hash keep the location of each observed offset in the sentence
	public HashMap<Integer, Clause> clauseMap = new HashMap<Integer, Clause>();
	//////////////////////////////////////
	//this array will keep all the lines that the governor or dependent clause could not be resolved
	ArrayList<DependencyLine> phrases = new ArrayList<DependencyLine>();
	public String filename;
	public String[] normalized_dependencies;
	public ArrayList<String> getPhrases()
	{
		ArrayList<String> phrase_strings = new ArrayList<String>();
		for (DependencyLine depLine:phrases)
		{
			if (depLine.firstOffset<depLine.secondOffset)
			{
				phrase_strings.add(depLine.firstPart+" "+depLine.secondPart);
			}
			else
			{
				phrase_strings.add(depLine.secondPart+" "+depLine.firstPart);
			}
		}
		return phrase_strings;
	}
	
	public SentenceClauseManager(Artifact relatedSentence) throws Exception
	{
		setRelatedSentence(relatedSentence);
		setSentContent(relatedSentence.getContent());
		setPosTags(relatedSentence.getPOS());
		loadClauses();
	}

	
	void loadClauses() throws Exception
	{
		
		if (relatedSentence.getPOS() ==null)
		{
//			StanfordParser s_parser = new StanfordParser();
//			s_parser.parse(sentContent);
//			setPosTags(s_parser.getTagged());
//			setStanDependenciesStr(s_parser.getDependencies());
			return;
		
		}
		else
		{
			setPosTags(relatedSentence.getPOS());
			setStanDependenciesStr(relatedSentence.getStanDependency());
		}
		
		tokenMap = StanfordDependencyUtil.getTokens(posTags);

		//populate lemma
		lemmaMap = StanfordDependencyUtil.getLemmaMap(posTags);

		lemmaTokenMap = StanfordDependencyUtil.getLemmaTokenmaps(posTags);
		
		analyzeSentence();
	}
	
//	TODO: generally improve this method, it is not perfect
private void analyzeSentence() throws Exception {
		
		sentDepLines =StanfordDependencyUtil.parseDepLinesFromString(getStanDependenciesStr());
		clauses = new ArrayList<Clause>();
		Clause curClause = new Clause();
		
		ArrayList<DependencyLine> toBeProcessesd = sentDepLines;

		for(int i=0; i<sentDepLines.size();i++)
		{
			DependencyLine curLine = sentDepLines.get(i);
			
			if(curLine.relationName == null) continue;
			offsetMap.put(curLine.firstOffset, curLine.firstPart);
			offsetMap.put(curLine.secondOffset, curLine.secondPart);
			
			if(curLine.relationName.equals("nsubj") || curLine.relationName.equals("xsubj"))
			{
//				if (curLine.firstOffset -curLine.secondOffset>10)
//					continue;
				Clause governor_cl = clauseMap.get(curLine.firstOffset);
				Artifact related_word = relatedSentence.getChildByWordIndex(curLine.firstOffset-1);
				if (related_word != null)
				{
					String pos = related_word.getPOS();
					
					//if the verb is already observed
					if (governor_cl !=null)
					{
						governor_cl.clauseSubject.add(curLine);
						clauseMap.put(curLine.secondOffset, governor_cl);
					}
					else
					{
						governor_cl = new Clause();

						// subj and verb will be added to the new clause
						governor_cl.clauseSubject.add(curLine);
						
						if (pos!= null && (pos.startsWith("VB") || pos.startsWith("MD")))
						{
							governor_cl.clauseVerb.verbMainPart = curLine.firstPart;
							governor_cl.clauseVerb.offset = curLine.firstOffset;
							clauseMap.put(curLine.firstOffset, governor_cl);
							clauseMap.put(curLine.secondOffset, governor_cl);
							
						}
						//TODO: process more
						else if(pos!= null && (pos.startsWith("JJ") || pos.startsWith("NN")))
						{
							//if the relation cop also is present where the first part is the complement
							boolean is_comp = false;
							for (DependencyLine d:sentDepLines)
							{
								if (d.relationName.equals("cop") && d.firstOffset==curLine.firstOffset)
									is_comp = true;
							}
							if (is_comp==true)
							{
								governor_cl.complement = curLine.firstPart;
								governor_cl.complementOffset = curLine.firstOffset;
								clauseMap.put(curLine.firstOffset, governor_cl);
								clauseMap.put(curLine.secondOffset, governor_cl);
							}

							
						}
		
						
					}
					//get all dep lines that are related to this
				}
				

			}
			if(curLine.relationName.equals("dobj")|| 
					curLine.relationName.equals("iobj")||
					curLine.relationName.equals("nsubjpass"))
			{
		
				Clause governor_cl = getGovernorVerbOrComplement(curLine);
				
//				String dep_tag = getPOSTag(curLine.secondOffset);
//				if (dep_tag != null && dep_tag.startsWith("JJ"))
//				{
//					governor_cl.complement = curLine.secondPart;
//					governor_cl.complementOffset= curLine.secondOffset;
//					
//				}
//				else
//				{
					SentenceObject new_object = new SentenceObject();
					new_object.content = curLine.secondPart;
					new_object.contentOffset = curLine.secondOffset;
					governor_cl.clauseObject.add(new_object);

//				}
		
				clauseMap.put(curLine.secondOffset, governor_cl);
			}
	
			if(curLine.relationName.equals("cop"))
			{
				Clause governor = clauseMap.get(curLine.firstOffset);
				Clause dependent = clauseMap.get(curLine.secondOffset);
				
				if (governor != null ||dependent != null)
						
				{
					// it means that we have observed the verb 
					if (dependent != null && governor == null)
					{
						dependent.complement = curLine.firstPart;
						dependent.complementOffset = curLine.firstOffset;
						
						clauseMap.put(curLine.firstOffset, dependent);
					}
					else if(governor != null)
					{
						governor.clauseVerb.verbMainPart = curLine.secondPart;
						governor.clauseVerb.offset = curLine.secondOffset;
						clauseMap.put(curLine.secondOffset, governor);
					}
		
				}
				//we should add the verb and the complement
				else
				{
					curClause = new Clause();
					// complement and verb will be added to the new clause
					curClause.complement =curLine.firstPart;
					curClause.complementOffset = curLine.firstOffset;
					
					curClause.clauseVerb.verbMainPart = curLine.secondPart;
					curClause.clauseVerb.offset = curLine.secondOffset;
					
					clauseMap.put(curLine.firstOffset, curClause);
					clauseMap.put(curLine.secondOffset, curClause);
					
				}
			}
//			toBeProcessesd.remove(i);	
		}
		
//		xcomp, ccomp 
		for(int i=0; i<toBeProcessesd.size();i++)
		{
			DependencyLine curLine = sentDepLines.get(i);
			handleComp(curLine);
//			toBeProcessesd.remove(i);
		}
//		for(DependencyLine curLine:sentDepLines)
//		{
//			handleComp(curLine);
//		}

//		for(DependencyLine curLine:sentDepLines)
//		{
//			handleVerbDependencies(curLine);
//			handleNegation(curLine);
//			handleModifiers(curLine);
//			handleIobj(curLine);
//			handleMarks(curLine);
//		}
		for(int i=0; i<toBeProcessesd.size();i++)
		{
			DependencyLine curLine = sentDepLines.get(i);
			handleVerbDependencies(curLine);
			handleNegation(curLine);
			handleModifiers(curLine);
			handleIobj(curLine);
			handleMarks(curLine);
//			toBeProcessesd.remove(i);
		}
	
		for(int i=0; i<toBeProcessesd.size();i++)
		{
			DependencyLine curLine = sentDepLines.get(i);
			handleNPClMod(curLine);
		}
		//add unique sentence clauses to clause
		for (Clause c : clauseMap.values()) {
			
	       if (!clauses.contains(c) && c!= null) {
	        	clauses.add(c);
	       }
	    }
	}

void handleComp(DependencyLine curLine) throws SQLException
{
	//“He says that you like to swim” ccomp(says, like) 
	Artifact related_word = relatedSentence.getChildByWordIndex(curLine.secondOffset-1);
	if (related_word == null) return;
	String d_tag= related_word.getPOS();
	
	if(curLine.relationName.equals("ccomp")|| curLine.relationName.equals("xcomp"))
	{
		Clause governor_clause= clauseMap.get(curLine.firstOffset);
		Clause dependent_clause = clauseMap.get(curLine.secondOffset);
		if (clauseMap.containsKey(curLine.firstOffset)&&
				clauseMap.containsKey(curLine.secondOffset))
		{		
			governor_clause.clauseComplements.add(dependent_clause);
			dependent_clause.governer = governor_clause;
		//	if (d_tag.startsWith("JJ"))
		//	{
		//		governor_clause.complement = curLine.secondPart;
		//		governor_clause.complementOffset = curLine.secondOffset;
				
		//	}
		}
		else if (clauseMap.containsKey(curLine.firstOffset)&&
				!clauseMap.containsKey(curLine.secondOffset))
		{
			dependent_clause = new Clause();

			if (d_tag != null && d_tag.startsWith("VB"))
			{
				dependent_clause.clauseVerb.verbMainPart =curLine.secondPart;
				dependent_clause.clauseVerb.offset =curLine.secondOffset;
				clauseMap.put(curLine.secondOffset, dependent_clause);
				governor_clause.clauseComplements.add(dependent_clause);
				dependent_clause.governer = governor_clause;
			}
//			if (d_tag.startsWith("JJ"))
//			{
//				governor_clause.complement = curLine.secondPart;
//				governor_clause.complementOffset = curLine.secondOffset;
//				
//			}
			
				
		}
		else if (!clauseMap.containsKey(curLine.firstOffset)&&
				clauseMap.containsKey(curLine.secondOffset))
		{
			governor_clause =  getGovernorVerbOrComplement(curLine);
			ArrayList<Clause> cl_comps = new ArrayList<Clause>();
			if (!governor_clause.clauseComplements.isEmpty())
			{
				cl_comps = governor_clause.clauseComplements;
			}
			cl_comps.add(dependent_clause);
			governor_clause.clauseComplements = cl_comps;
		}
		else if (!clauseMap.containsKey(curLine.firstOffset)&&
				!clauseMap.containsKey(curLine.secondOffset))
		{
			
			//create both clauses and add
			governor_clause =  getGovernorVerbOrComplement(curLine);
			
			dependent_clause = new Clause();
			if (d_tag.startsWith("VB"))
			{
				dependent_clause.clauseVerb.verbMainPart = curLine.secondPart;
				dependent_clause.clauseVerb.offset = curLine.secondOffset;
				clauseMap.put(curLine.secondOffset, dependent_clause);		
			}
//			else
//			{
//				dependent_clause.complement = curLine.secondPart;
//				dependent_clause.complementOffset = curLine.secondOffset;
//			}				
//			clauseMap.put(curLine.secondOffset, dependent_clause);				
		}
		
		
	}
	else
	{
		// throw exception
	}
}
void handleVerbDependencies(DependencyLine depLine) throws SQLException
{
	if(depLine.relationName.equals("prt")|| depLine.relationName.equals("aux")
			|| depLine.relationName.equals("auxpass"))
	{
		Clause governor_clause = getGovernorVerbOrComplement(depLine);
		if(depLine.relationName.equals("aux") || depLine.relationName.equals("auxpass"))
		{
			governor_clause.clauseVerb.auxs.add(depLine.secondPart);
			if (depLine.relationName.equals("auxpass"))
			{
				governor_clause.clauseVerb.isPassive = true;
			}
		}
		else if(depLine.relationName.equals("prt"))
		{
			governor_clause.clauseVerb.prt = depLine.secondPart;
		}

		clauseMap.put(depLine.secondOffset, governor_clause);
	}
}
void handleNegation(DependencyLine depLine) throws SQLException
{
	
	if(depLine.relationName.equals("neg"))
	{
		Clause governor  = getGovernorVerbOrComplement(depLine);
		if (governor.clauseVerb.offset == depLine.firstOffset)
		{
			governor.clauseVerb.isNegated = true;
		}	
		governor.isNegated = true;
		clauseMap.put(depLine.secondOffset, governor);
	}
	if(depLine.relationName.equals("det") && depLine.secondPart.equalsIgnoreCase("no") )
	{
		Clause governor  = clauseMap.get(depLine.firstOffset);
		if (governor != null)
		{
			ArrayList<String> modifiers = new ArrayList<String>();
			if (governor.modifierDepMap.containsKey(depLine.firstOffset))
			{
				modifiers = governor.modifierDepMap.get(depLine.firstOffset);
			}
			modifiers.add(depLine.secondPart);
			governor.modifierDepMap.put(depLine.firstOffset,modifiers);
			governor.isNegated = true;
			clauseMap.put(depLine.secondOffset, governor);
		}
		else
		{
			phrases.add(depLine);
		}
		
	}
	
}
Clause getGovernorVerbOrComplement(DependencyLine depLine) throws SQLException
{
	Clause governor_clause = clauseMap.get(depLine.firstOffset);
	boolean create_new_required =false;
	//if the governor is supposed to be verb but the content of existing is not equal
	if (governor_clause != null)
	{
		Artifact related_word = relatedSentence.getChildByWordIndex(depLine.firstOffset-1);
		String g_tag = related_word.getPOS();
		if (g_tag!= null && (g_tag.startsWith("VB") || g_tag.startsWith("MD")))
		{
			if (governor_clause.clauseVerb.offset != depLine.firstOffset)
			{
				create_new_required =true;
			}
		}
		
	}
	
	if (governor_clause == null || create_new_required)
	{
		governor_clause  = new Clause();
		Artifact related_word =relatedSentence.getChildByWordIndex(depLine.firstOffset-1);
		String g_tag = related_word.getPOS();

		if (g_tag!= null && (g_tag.startsWith("VB") || g_tag.startsWith("MD")))
		{
			governor_clause.clauseVerb.verbMainPart = depLine.firstPart;
			governor_clause.clauseVerb.offset = depLine.firstOffset;
		}
		else//TODO:it shoule be checked more
		{
			governor_clause.complement = depLine.firstPart;
			governor_clause.complementOffset = depLine.firstOffset;
		}
		clauseMap.put(depLine.firstOffset, governor_clause);
	}
	
	return governor_clause;
}

void handleModifiers(DependencyLine depLine) throws SQLException
{
	if (!(depLine.relationName.equals("amod")||
			depLine.relationName.equals("advmod")
			|| depLine.relationName.equals("dep")
			|| depLine.relationName.equals("nn") 
			|| depLine.relationName.equals("det")
			|| depLine.relationName.equals("tmod")
			|| depLine.relationName.equals("poss")
			|| depLine.relationName.startsWith("prepc_")
			|| depLine.relationName.startsWith("prep_")))
	{
		return;
	}
//	TODO: may nor working fine
	if (depLine.relationName.startsWith("prep_"))
	{
		Artifact related_word =relatedSentence.getChildByWordIndex(depLine.firstOffset-1);
		String gov_pos = related_word.getPOS();

//		if (!gov_pos.startsWith("NN"))
//		{
//			return;
//		}
	}

	Clause governor_cl = clauseMap.get(depLine.firstOffset);
	Clause dependent_cl = clauseMap.get(depLine.secondOffset);

	if (governor_cl == null)
	{
		//TODO: Find a solid solution....
		//try to find the related clause of the current governor
		List<DependencyLine> related_dep_lines = StanfordDependencyUtil.getAllGovernors(sentDepLines, depLine.firstPart);
		for (DependencyLine rel_dep:related_dep_lines)
		{
			if(rel_dep.secondOffset==depLine.firstOffset)
			{
				governor_cl = clauseMap.get(rel_dep.firstOffset);
				break;
			}
			
		}
		governor_cl = findMissingClause(depLine);
		//if it is still not found
		if (governor_cl==null)
		{
			phrases.add(depLine);
		}
		
	}

	if (governor_cl != null && governor_cl != null)
	{
		
		ArrayList<String> modifiers = new ArrayList<String>();
		if(governor_cl.modifierDepMap.containsKey(depLine.firstOffset))
		{
			modifiers =governor_cl.modifierDepMap.get(depLine.firstOffset);
		}
		modifiers.add(depLine.secondPart);

		governor_cl.modifierDepMap.put(depLine.firstOffset, modifiers);
		
		if (depLine.relationName.equals("amod")||
				depLine.relationName.equals("advmod")
				|| depLine.relationName.equals("nn") )
		{
			governor_cl.adjModifierDepMap.put(depLine.firstOffset, modifiers);
		}
		if (dependent_cl==null)
		{
			clauseMap.put(depLine.secondOffset, governor_cl);
		}
		
	}
	
}
void handleNPClMod(DependencyLine depLine) throws SQLException
{
	if (!(depLine.relationName.equals("infmod") || depLine.relationName.equals("rcmod")))
	{
		return;
	}
	Clause governor_cl = clauseMap.get(depLine.firstOffset);
	Clause dependent_cl = clauseMap.get(depLine.secondOffset);
	
	if (governor_cl == null)
	{
		governor_cl = findMissingClause(depLine);
		//if it is still not found
		
	}
	if (governor_cl==null)
	{
		phrases.add(depLine);
	}

	else if ((dependent_cl != null && governor_cl !=dependent_cl))
	{
		governor_cl.clauseComplements.add(dependent_cl);
		dependent_cl.governer = governor_cl;
	}
	else if (dependent_cl == null)
	{
		// try to build it
		dependent_cl = buildDependentClause(depLine);
		if (dependent_cl != null)
		{
			clauseMap.put(depLine.secondOffset, dependent_cl);
			governor_cl.clauseComplements.add(dependent_cl);
			dependent_cl.governer = governor_cl;
		}
		else//this should not happen
		{
			phrases.add(depLine);
		}
		
	}
	//they should be different
	if (dependent_cl==governor_cl)
	{
		//get all governors of the second part
		List<DependencyLine> governing_dep_lines =
			StanfordDependencyUtil.getAllGovernors(sentDepLines, depLine.firstPart, depLine.firstOffset);
		// from there select other one if exist
		for(DependencyLine dep:governing_dep_lines)
		{
			if(dep.relationName.equals("nsubj") || dep.relationName.equals("xsubj")||
					dep.relationName.equals("dobj")|| 
					dep.relationName.equals("iobj")||
					dep.relationName.equals("nsubjpass")||
					dep.relationName.equals("cop"))
			{
				//if it is different form the cottenr clause
				if (dep.firstOffset != depLine.secondOffset)
				{
					Clause new_cl = clauseMap.get(dep.firstOffset);
					if (new_cl!=null)
					{
						clauseMap.put(depLine.firstOffset, new_cl);
						
					}
					
				}
			}
		}
	}
}
private Clause findMissingClause(DependencyLine depLine)
{
	Clause cl= null;
	List<DependencyLine> related_dep_lines = StanfordDependencyUtil.getAllGovernors(sentDepLines, depLine.firstPart,depLine.firstOffset);
	for (DependencyLine rel_dep:related_dep_lines)
	{
		
		cl = clauseMap.get(rel_dep.firstOffset);
		break;	
	}
	return cl;
}
//needs to be completed
Clause buildDependentClause(DependencyLine depLine) throws SQLException
{
	Artifact related_word =relatedSentence.getChildByWordIndex(depLine.secondOffset-1);
	String d_pos = related_word.getPOS();

	Clause dependent_clause =null;
	
	if (d_pos != null && (d_pos.startsWith("VB") || d_pos.startsWith("MD")))
	{
		dependent_clause = new Clause();
		dependent_clause.clauseVerb.verbMainPart = depLine.secondPart;
		dependent_clause.clauseVerb.offset = depLine.secondOffset;
	}
	else if (d_pos != null && d_pos.startsWith("JJ") )
	{
		dependent_clause = new Clause();
		dependent_clause.complement = depLine.secondPart;
		dependent_clause.complementOffset = depLine.secondOffset;
	}
	return dependent_clause;
}

void handleIobj(DependencyLine depLine) throws SQLException
{
	if (!(depLine.relationName.startsWith("prep_")))
	{
		return;
	}
	// if governor is a noun it is handled in modifier
	Artifact related_word =relatedSentence.getChildByWordIndex(depLine.firstOffset-1);
	String gov_pos = related_word.getPOS();

	if (gov_pos!= null && gov_pos.startsWith("NN"))
	{
		return;
	}
	Clause gov_cl = clauseMap.get(depLine.firstOffset);
	Clause dep_cl =  clauseMap.get(depLine.secondOffset);
	if(gov_cl != null && dep_cl!= null )
	{
		SentenceObject indirect_object_cl = new SentenceObject();
		indirect_object_cl.clause = dep_cl;
		
		gov_cl.clauseIObjPrep.put(indirect_object_cl,getPrep(depLine.relationName) );
		gov_cl.clauseIObjs.add(depLine.secondPart);
	}
	else if (gov_cl != null && dep_cl== null)
	{
		SentenceObject indirect_object = new SentenceObject();
		indirect_object.content = depLine.secondPart;
		indirect_object.contentOffset = depLine.secondOffset;
		
		gov_cl.clauseIObjPrep.put(indirect_object,getPrep(depLine.relationName) );
		gov_cl.clauseIObjs.add(depLine.secondPart);
	}
	else
	{
		phrases.add(depLine);
	}

}
void handleMarks(DependencyLine depLine)
{
	if (!(depLine.relationName.equals("mark")))
	{
		return;
	}
	Clause gov_cl = clauseMap.get(depLine.firstOffset);
	
	if(gov_cl != null)
	{
		gov_cl.isMarked = true;
		gov_cl.clauseMark = depLine.secondPart;
		clauseMap.put(depLine.secondOffset, gov_cl);
	}

	else
	{
		phrases.add(depLine);
	}

}
public String getPrep(String rel_name)
{
	String prep = null;
	Pattern p = Pattern.compile("prep_(\\w+)");
	Matcher m = p.matcher(rel_name);
	if(m.matches())
	{
		prep = m.group(1);
	}
	
	return prep;
}
String getConj(String rel_name)
{
	String conj = null;
	Pattern p = Pattern.compile("conj_(\\w+)");
	Matcher m = p.matcher(rel_name);
	if(m.matches())
	{
		conj = m.group(1);
	}
	
	return conj;
}
void handleConjuction(DependencyLine depLine)
{

	if(!depLine.relationName.startsWith("conj_"))
	{
		return;
	}
	Clause dep_cl = clauseMap.get(depLine.secondOffset);
	if (dep_cl != null)
	{
		dep_cl.conjuctedBut = true;
	}
	else
	{
		phrases.add(depLine);
	}
}
//it gets an offset as input and returns next lemmatized tokens
public ArrayList<String> getNextLemmaTokens(Integer offset,Integer token_count)
{
	ArrayList<String> next_tokens = new ArrayList<String>();
	Integer sent_token_count = lemmaMap.size();
	if (lemmaMap.containsKey(offset))
	{
		for (int i = offset+1; i <= token_count+ offset && i<sent_token_count ; i++) {
			if(lemmaMap.containsKey(i))
			{
				next_tokens.add(lemmaMap.get(i));
			}
		}
	}
	return next_tokens;
}
// it gets an offset as input and returns next lemmatized tokens
public ArrayList<String> getPreviousLemmaTokens(Integer offset,Integer token_count)
{
	ArrayList<String> prev_tokens = new ArrayList<String>();
	
	if (lemmaMap.containsKey(offset))
	{
		for (int i = offset-1; i >= offset-token_count && i>=0 ; i--) {
			if(lemmaMap.containsKey(i))
			{
				prev_tokens.add(lemmaMap.get(i));
			}
		}
	}
	return prev_tokens;
}
// it gets an offset as input and returns the around lemmatized tokens
public ArrayList<String> getArroundLemmaTokens(Integer offset,Integer token_count)
{
	ArrayList<String> arround_tokens = new ArrayList<String>();
	Integer sent_token_count = lemmaMap.size();
	
	if (lemmaMap.containsKey(offset))
	{
		for (int i = offset-1; i >= offset-token_count && i>=0 ; i--) {
			if(lemmaMap.containsKey(i))
			{
				arround_tokens.add(lemmaMap.get(i));
			}
		}
	}
	if (lemmaMap.containsKey(offset))
	{
		for (int i = offset+1; i <= token_count+ offset && i<sent_token_count ; i++) {
			if(lemmaMap.containsKey(i))
			{
				arround_tokens.add(lemmaMap.get(i));
			}
		}
	}
	return arround_tokens;
}
public String getPOSTag(Integer offset)
{
	if (offset <1)
	{
		return "missing";
	}
	String pos = posTags.split(" ")[offset-1].split("/")[1];
	
	return pos;
}
	
	
	
	public ArrayList<Clause> getClauses() {
		return clauses;
	}
	public String getContent() {
		return getRelatedSentence().getContent();
	}
	

	public SentenceClauseManager()  {
	}

	public void setRelatedSentence(Artifact relatedSentence) {
		this.relatedSentence = relatedSentence;
	}

	public Artifact getRelatedSentence() {
		return relatedSentence;
	}

	public void setSentContent(String sentContent) {
		this.sentContent = sentContent;
	}

	public String getSentContent() {
		return sentContent;
	}

	public void setPosTags(String posTags) {
		this.posTags = posTags;
	}

	public String getPosTags() {
		return posTags;
	}


	public void setStanDependenciesStr(String stanDependenciesStr) {
		this.stanDependenciesStr = stanDependenciesStr;
	}

	public String getStanDependenciesStr() {
		return stanDependenciesStr;
	}


}
