package LuceneManagerADR;

import rainbownlp.core.Artifact;

public class SlidingWindow {
	private Artifact start;
	private Artifact end;
	private String content;
	
	public SlidingWindow(Artifact pStart, Artifact pEnd)
	{
		start =pStart;
		end =pEnd;
	}
	public Artifact getStart() {
		return start;
	}
	public void setStart(Artifact start) {
		this.start = start;
	}
	public Artifact getEnd() {
		return end;
	}
	public void setEnd(Artifact end) {
		this.end = end;
	}
	public String getContent() {
		String content = start.getContent();
		Artifact next = start.getNextArtifact();
		while (next != null &&
				next!=end)
		{
			content += " "+ next.getContent();
			next = next.getNextArtifact();
		}
		if (!start.equals(end))
		{
			content += " "+ end.getContent();
		}
		return content;
	}

}
