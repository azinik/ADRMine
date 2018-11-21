package rainbownlp.core;

import java.math.BigInteger;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.Session;
import org.hibernate.annotations.GenericGenerator;

import rainbownlp.analyzer.sentenceclause.SentenceClauseManager;
import rainbownlp.core.Artifact.Type;
import rainbownlp.util.FileUtil;
import rainbownlp.util.HibernateUtil;



@Entity
@Table( name = "SentenceChunk" )
public class SentenceChunk {

	private Artifact startArtifact;
	private Artifact endArtifact;
	private int chunkId;
	private String chunkContent;

	private Artifact parentSentence;
	SentenceClauseManager clauseManager;

	private SentenceChunk previousChunk;
	private SentenceChunk nextChunk;
	int length;
	private boolean hasADR = false;
	
	
	

	public static SentenceChunk getInstance(Artifact artifact1, Artifact artifact2) {
		
		return getInstance(artifact1, artifact2);
	}
	
//	public static SentenceChunk getInstance(int pLinkID) {
//		String hql = "from PhraseLink where startArtifact = "+pLinkID;
//		SentenceChunk phrase_link_obj = 
//			(SentenceChunk)HibernateUtil.executeReader(hql).get(0);
//		return phrase_link_obj;
//	}


	public static SentenceChunk getOrCreateInstance(Artifact artifact1, Artifact artifact2) throws Exception{
		String hql = "from SentenceChunk where startArtifact = "+
				artifact1.getArtifactId()+" and endArtifact="+artifact2.getArtifactId();
		
		List<SentenceChunk> link_objects = 
				(List<SentenceChunk>) HibernateUtil.executeReader(hql);
	    
	    
		SentenceChunk sentChunk_obj;
	    if(link_objects.size()==0)
	    {
	    	sentChunk_obj = new SentenceChunk();
	    	sentChunk_obj.setStartArtifact(artifact1);
	    	sentChunk_obj.setEndArtifact(artifact2);
	    	sentChunk_obj.setParentSentence(artifact1.getParentArtifact());
	    	sentChunk_obj.setChunkContent(sentChunk_obj.calculateChunkContent());
	    	
	    	HibernateUtil.save(sentChunk_obj);
	    	
	    }else
	    {
	    	sentChunk_obj = 
	    		link_objects.get(0);
	    }
	    return sentChunk_obj;
	}
	public static SentenceChunk getOrCreateInstance(Artifact startArtifact){
		String hql = "from SentenceChunk where startArtifact = "+
				startArtifact.getArtifactId();
		
		List<SentenceChunk> chunk_objects = 
				(List<SentenceChunk>) HibernateUtil.executeReader(hql);
	    
	    
		SentenceChunk sentChunk_obj;
	    if(chunk_objects.size()==0)
	    {
	    	sentChunk_obj = new SentenceChunk();
	    	sentChunk_obj.setStartArtifact(startArtifact);
	    	sentChunk_obj.setParentSentence(startArtifact.getParentArtifact());
	    	HibernateUtil.save(sentChunk_obj);
	    	
	    }else
	    {
	    	sentChunk_obj = 
	    			chunk_objects.get(0);
	    }
	    return sentChunk_obj;
	}
	
	
	public static SentenceChunk createInstance(Artifact startArtifact, Artifact endArtifact){
		
		SentenceChunk sentChunk_obj;
		sentChunk_obj = new SentenceChunk();
    	sentChunk_obj.setStartArtifact(startArtifact);
    	sentChunk_obj.setEndArtifact(endArtifact);
    	
    	HibernateUtil.save(sentChunk_obj);
	   
	    return sentChunk_obj;
	}
	
	public String calculateChunkContent() throws Exception
	{
		if(endArtifact==null)
			throw new Exception("The end artifact is missing");
		String content = startArtifact.getContent();
		Artifact curr= startArtifact;
		while (!curr.equals(endArtifact))
		{
			curr = curr.getNextArtifact();
			content = content +" "+curr.getContent();
		}
		return content;
	}
	
	
@Id
@GeneratedValue(generator="increment")
@GenericGenerator(name="increment", strategy = "increment")
public int getChunkId() {
	return chunkId;
}
public void setChunkId(int ChunkId) {
	this.chunkId = ChunkId;
}
	

@OneToOne( cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch=FetchType.LAZY )
@JoinColumn(name="startArtifact")
public Artifact getStartArtifact() {
	return startArtifact;
}
public void setStartArtifact(Artifact startArtifact) {
	this.startArtifact = startArtifact;
}
@OneToOne( cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch=FetchType.LAZY )
@JoinColumn(name="endArtifact")
public Artifact getEndArtifact() {
	return endArtifact;
}
public void setEndArtifact(Artifact endArtifact) {
	this.endArtifact = endArtifact;
}
@Transient
public SentenceClauseManager getClauseManager() throws Exception
{
	if (clauseManager ==null)
	{
		clauseManager =
			new SentenceClauseManager(getParentSentence());
	}
	return clauseManager;
}

@OneToOne( cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch=FetchType.LAZY )
@JoinColumn(name="nextChunk")
public SentenceChunk getNextChunk() {
	return nextChunk;
}

public void setNextChunk(SentenceChunk nextChunk) {
	this.nextChunk = nextChunk;
}

@OneToOne( cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch=FetchType.LAZY )
@JoinColumn(name="previousChunk")
public SentenceChunk getPreviousChunk() {
	return previousChunk;
}

public void setPreviousChunk(SentenceChunk previousChunk) {
	this.previousChunk = previousChunk;
}
@Column(length = 2000)
public String getChunkContent() {
	return chunkContent;
}

public void setChunkContent(String chunkContent) {
	this.chunkContent = chunkContent;
}
@ManyToOne( cascade = {CascadeType.PERSIST, CascadeType.MERGE})
@JoinColumn(name="parentSentence")
public Artifact getParentSentence() {
	return parentSentence;
}

public void setParentSentence(Artifact parentSentence) {
	this.parentSentence = parentSentence;
}

@Transient	
public  List<Phrase> getPhrasesInChunk(String entityType) {
	String hql = "FROM Phrase where startArtifact.parentArtifact=:parent and phraseEntityType=:eType and " +
			" startCharOffset >= :startArtifact and endCharOffset <= :endArtifact ";
	HashMap<String, Object> params = new HashMap<String, Object>();
	params.put("eType", entityType);
	params.put("startArtifact", startArtifact.getStartIndex());
	params.put("endArtifact", endArtifact.getEndIndex());
	params.put("parent", parentSentence.getArtifactId());
	return	 (List<Phrase>) HibernateUtil.executeReader(hql, params);
	 
}

public void calculateHasADR()
{
	List<Phrase> phrases = getPhrasesInChunk("ADR");
	if (phrases.size()>0)
		setHasADR(true);
}

	
public boolean getHasADR() {
	return hasADR;
}

public void setHasADR(boolean hasADR) {
	this.hasADR = hasADR;
}

public static List<SentenceChunk> ListAllChunks(boolean forTrain) {
	String hql = "from SentenceChunk where parentSentence.forTrain = "+forTrain;
		
	List<SentenceChunk> chunk_objects = 
			(List<SentenceChunk>) HibernateUtil.executeReader(hql);
	return chunk_objects;
}
@Transient
public List<Artifact> getChildArtifacts() {
	
	List<Artifact> childsArtifact = 
			(List<Artifact>) HibernateUtil.executeReader( "from Artifact where relatedChunk = "+getChunkId());
	

	return childsArtifact;
}

	
}
