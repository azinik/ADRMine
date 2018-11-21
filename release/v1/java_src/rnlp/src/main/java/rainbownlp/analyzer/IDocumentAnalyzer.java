package rainbownlp.analyzer;

public interface IDocumentAnalyzer {
	
	/**
	 * Process given documents and load them into Artifact table
	 * @param rootPath root of all documents to be processed
	 * @return number of processed documents
	 * @throws Exception
	 */
	public int processDocuments(String rootPath) throws Exception;

	public int processDocuments(String rootPath, String corpusName);
}
