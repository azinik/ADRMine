package rainbownlp.core;

public class SemanticSimilarity {

	public static double getSimilarity(String concept1, String concept2) {
		return (concept1.equals(concept2))?1:0;
	}

}
