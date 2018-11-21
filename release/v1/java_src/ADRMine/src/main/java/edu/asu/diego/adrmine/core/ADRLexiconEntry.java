package edu.asu.diego.adrmine.core;

public class ADRLexiconEntry {
	
	private String content;

	private String umlsID;

	public ADRLexiconEntry(String annContent, String conceptId)
	{
		content = annContent;
		umlsID = conceptId;
		
	}
	

	public void setContent(String content) {
		this.content = content;
	}
	public String getContent() {
		return content;
	}

	public void setUmlsID(String umlsID) {
		this.umlsID = umlsID;
	}
	public String getUmlsID() {
		return umlsID;
	}


}
