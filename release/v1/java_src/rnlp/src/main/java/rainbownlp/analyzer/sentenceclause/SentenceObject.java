package rainbownlp.analyzer.sentenceclause;

import java.util.ArrayList;
import rainbownlp.parser.DependencyLine;

public class SentenceObject {
	public String content;
	public Clause clause;
	public ArrayList<String> modifiers;
	public DependencyLine dependencyLine;
	public Integer contentOffset;
	
	public SentenceObject()
	{
		content = new String();
		clause= new Clause ();
		modifiers = new ArrayList<String>();
		dependencyLine = new DependencyLine();
		contentOffset = new Integer(0);

	}
	
}
