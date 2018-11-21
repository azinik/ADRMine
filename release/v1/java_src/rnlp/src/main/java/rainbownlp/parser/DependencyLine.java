package rainbownlp.parser;

public class DependencyLine {
	public String relationName;
	public String firstPart;
	public String secondPart;
	public int firstOffset;
	public int secondOffset;
	public boolean hasWord(String word) {
		if(firstPart.equals(word) ||
				secondPart.equals(word))
			return true;
		return false;
	}
}
