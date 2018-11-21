package rainbownlp.analyzer.sentenceclause;

import java.util.ArrayList;
import java.util.HashMap;
import rainbownlp.parser.DependencyLine;


public class Clause implements Cloneable, Comparable<Clause>{
	
	@Override
	protected Clause clone() throws CloneNotSupportedException {
		Clause newClause = new Clause();
		newClause.clauseSubject = clauseSubject;
		newClause.clauseVerb = clauseVerb;
		newClause.clauseObject = clauseObject;
		newClause.clauseIObjPrep = clauseIObjPrep;
		newClause.conjuctedLines = conjuctedLines;
		newClause.isNegated = isNegated;
		newClause.clauseMark =clauseMark;

		newClause.isPleasant = isPleasant;
		newClause.clauseComplements = clauseComplements;
		newClause.complement = complement;
		newClause.complementOffset = complementOffset;
		newClause.modifierDepMap = modifierDepMap;
		newClause.conjuctedBut = conjuctedBut;
		newClause.clauseSubjStrings = clauseSubjStrings;
		
		newClause.clauseIObjs = clauseIObjs;
		newClause.isMarked = isMarked;
		newClause.adjModifierDepMap=adjModifierDepMap;
		//newClause.offsetMap = offsetMap;

		return newClause;
	}

	public ArrayList<DependencyLine> clauseSubject;
	ArrayList<String> clauseSubjStrings;
	public Verb clauseVerb;
	public ArrayList<SentenceObject> clauseObject;
	public HashMap<SentenceObject, String> clauseIObjPrep;
	public ArrayList<String> clauseIObjs;
	public ArrayList<DependencyLine> conjuctedLines; 
	public boolean isNegated = false;
	public String clauseMark;
	public boolean isMarked = false;

	public boolean isPleasant = false;
	public ArrayList<Clause> clauseComplements;
	public String complement;
	public Integer complementOffset;
	public HashMap<Integer, ArrayList<String>> modifierDepMap;
	//public HashMap<Integer, String> offsetMap; 
	public boolean conjuctedBut = false;
	
	public Clause governer;
	public HashMap<Integer, ArrayList<String>> adjModifierDepMap;
	
	public Clause()
	{
		clauseSubject = new ArrayList<DependencyLine>();
		clauseVerb = new Verb();
		clauseObject = new ArrayList<SentenceObject> ();
		clauseIObjPrep = new HashMap<SentenceObject, String>();
		clauseIObjs = new ArrayList<String>();
		conjuctedLines = new ArrayList<DependencyLine>();
		//E.g. since, if
		clauseMark = new String(); 
		
		clauseComplements = new ArrayList<Clause>();
		complement = new String();
		complementOffset = -1;
		modifierDepMap = new HashMap<Integer, ArrayList<String>>();
		adjModifierDepMap = new HashMap<Integer, ArrayList<String>>();
		clauseSubjStrings = null;
		//offsetMap = new HashMap<Integer, String>();

	}
	public ArrayList<String> getClauseSubjStrings()
	{
		if (clauseSubjStrings ==null)
		{
			clauseSubjStrings = new ArrayList<String>();
			for (DependencyLine sbj_dep:clauseSubject)
			{
				clauseSubjStrings.add(sbj_dep.secondPart.toLowerCase());
			}
		}
		return clauseSubjStrings;
	}
	@Override
	public int compareTo(Clause o) {

		return 0;
	}
	
	public boolean subjFirstPerson()
	{
		boolean is_first_person = false;
		ArrayList<String> subjs_strings = getClauseSubjStrings();

		if(subjs_strings.contains("I") || subjs_strings.contains("i"))
		{
			is_first_person = true;
		}
		return is_first_person; 
	}

	
}
