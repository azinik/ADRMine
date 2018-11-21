package rainbownlp.core;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.Session;
import org.hibernate.annotations.GenericGenerator;
import rainbownlp.util.FileUtil;
import rainbownlp.util.HibernateUtil;

@Entity
@Table( name = "PhraseLink" )
public class PhraseLink {
	int PhraseLinkId;
	Phrase fromPhrase;
	Phrase toPhrase;
	LinkType linkType;
	private LinkType linkTypeReal;
	private LinkType linkTypeClosure;
	private LinkType linkTypeIntegrated;
	private String altLinkID;
	
	
	
	public static PhraseLink getInstance(int pLinkID) {
		String hql = "from PhraseLink where phraseLinkId = "+pLinkID;
		PhraseLink phrase_link_obj = 
			(PhraseLink)HibernateUtil.executeReader(hql).get(0);
		return phrase_link_obj;
	}


	public static PhraseLink getInstance(Phrase pfromPhrase, Phrase pToPhrase){
		String hql = "from PhraseLink where fromPhrase = "+
			pfromPhrase.getPhraseId()+" and toPhrase="+pToPhrase.getPhraseId();
		
		List<PhraseLink> link_objects = 
				(List<PhraseLink>) HibernateUtil.executeReader(hql);
	    
	    
		PhraseLink phraseLink_obj;
	    if(link_objects.size()==0)
	    {
	    	phraseLink_obj = new PhraseLink();
	    	phraseLink_obj.setFromPhrase(pfromPhrase);
	    	phraseLink_obj.setToPhrase(pToPhrase);
	    	phraseLink_obj.setLinkType(LinkType.UNKNOWN);
	    	
	    	
//	    	String last_id = findLargestAltLinkId(pfromPhrase.getStartArtifact().getAssociatedFilePath(),"SECTIME");
//	    	Integer next_id = Integer.parseInt(last_id)+1;
//	    	phraseLink_obj.setAltLinkID("SECTIME"+next_id.toString());
	    	
	    	HibernateUtil.save(phraseLink_obj);
	    	
	    	FileUtil.logLine("/tmp/nonExistingLinks", "from phrase id "+ pfromPhrase.getPhraseId() + 
	    			"to id "+ pToPhrase.getPhraseId());
	    }else
	    {
	    	phraseLink_obj = 
	    		link_objects.get(0);
	    }
	    return phraseLink_obj;
	}
	
	
	public static PhraseLink createInstance(Phrase pfromPhrase, Phrase pToPhrase){
	    
		PhraseLink phraseLink_obj;
	   
    	phraseLink_obj = new PhraseLink();
    	phraseLink_obj.setFromPhrase(pfromPhrase);
    	phraseLink_obj.setToPhrase(pToPhrase);
    	
    	HibernateUtil.save(phraseLink_obj);
    	
    	FileUtil.logLine("/tmp/nonExistingLinks", "from phrase id "+ pfromPhrase.getPhraseId() + 
    			"to id "+ pToPhrase.getPhraseId());
	   
	    return phraseLink_obj;
	}
	
	
	@Id
	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy = "increment")
	public int getPhraseLinkId() {
		return PhraseLinkId;
	}
	public void setPhraseLinkId(int PhraseLinkId) {
		this.PhraseLinkId = PhraseLinkId;
	}
	
//	@ManyToOne( cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch=FetchType.LAZY )
	@ManyToOne( cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name="fromPhrase")
	public Phrase getFromPhrase() {
		return fromPhrase;
	}
	public void setFromPhrase(Phrase firstPhrase) {
		this.fromPhrase = firstPhrase;
	}
	
//	@ManyToOne( cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch=FetchType.LAZY )
	@ManyToOne( cascade = {CascadeType.PERSIST, CascadeType.MERGE} )
    @JoinColumn(name="toPhrase")
	public Phrase getToPhrase() {
		return toPhrase;
	}
	public void setToPhrase(Phrase secondPhrase) {
		this.toPhrase = secondPhrase;
	}
	public LinkType getLinkType() {
		return linkType;
	}
	public void setLinkType(LinkType pLinkType) {
		this.linkType = pLinkType;
	}
	
	public enum LinkType {
		UNKNOWN(0),
		BEFORE(1),
		AFTER(2), 
		OVERLAP(3);
		
		 private static final Map<Integer,LinkType> lookup 
         = new HashMap<Integer,LinkType>();
		static {
	          for(LinkType l : EnumSet.allOf(LinkType.class))
	               lookup.put(l.getCode(), l);
	     }
		
		private int code;

	     private LinkType(int code) {
	          this.code = code;
	     }

	     public int getCode() { return code; }

	     public static LinkType getEnum(int code) { 
	          return lookup.get(code); 
	     }	
	     
	}	
	
	
	public static boolean sentenceHasLink(Artifact sentence) {
		boolean hasLink =false;
		
		String hql = "from PhraseLink where fromPhrase.startArtifact.parentArtifact = "+
				sentence.getArtifactId()+" and linkType <> 0";
		Session session = HibernateUtil.sessionFactory.openSession();
			
		List<PhraseLink> link_objects = 
				(List<PhraseLink>) HibernateUtil.executeReader(hql, null, null, session);
	    
	    if(link_objects.size()!=0)
	    	hasLink = true;
	    
		session.clear();
		session.close();
		return hasLink;
	}


	//This method will return all phraseLinks that in a sentence
	public static List<PhraseLink> findPositivePhraseLinkInSentence(Artifact sentence, LinkType pExcludedLinkType) {
		String hql = "from PhraseLink where fromPhrase.startArtifact.parentArtifact = :sentId "+
		" and toPhrase.endArtifact.parentArtifact= :sentId  and linkType<> :pExcludedLinkType ";
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("sentId", sentence.getArtifactId());
		params.put("pExcludedLinkType",pExcludedLinkType.ordinal());
		
		List<PhraseLink> link_objects = 
		(List<PhraseLink>) HibernateUtil.executeReader(hql,params);
		
		return link_objects;
	}
	public static List<PhraseLink> findAllPhraseLinkInSentence(Artifact sentence) {
		String hql = "from PhraseLink where fromPhrase.startArtifact.parentArtifact = :sentId "+
		" and toPhrase.endArtifact.parentArtifact= :sentId ";
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("sentId", sentence.getArtifactId());
		
		List<PhraseLink> link_objects = 
		(List<PhraseLink>) HibernateUtil.executeReader(hql,params);
		
		return link_objects;
	}

@Transient
String betweenContent = null;
@Transient
	public String getBetweenContent() {
		if(betweenContent == null)
		{
			Artifact curArtifact = getFirstPhrase().getEndArtifact().getNextArtifact();
			if(curArtifact == null) betweenContent = "<BETWEENSENTENCE>";
			
			Artifact to = getSecondPhrase().getStartArtifact();
			if(betweenContent==null)
			{
				betweenContent = "";
				while(curArtifact !=null && !curArtifact.equals(to)){
					betweenContent += curArtifact.getContent() + " ";
					curArtifact = curArtifact.getNextArtifact();
				}
				if(curArtifact==null) betweenContent="<BETWEENSENTENCE>";
				else			
					betweenContent = betweenContent.trim();
			}
		}
		return betweenContent;
	}

	
	@Transient
	public Phrase getFirstPhrase() {
	if(toPhrase.getStartArtifact().getStartIndex()<
			fromPhrase.getStartArtifact().getStartIndex())
				return toPhrase;
		else
			return fromPhrase;
	}

@Transient
	public Phrase getSecondPhrase() {
		if(toPhrase.getStartArtifact().getStartIndex()<
		fromPhrase.getStartArtifact().getStartIndex())
			return fromPhrase;
		else
			return toPhrase;
	}


public boolean linkedWithAnotherPhraseInBetween() {
	String hql = "from PhraseLink where fromPhrase = "+ fromPhrase.getPhraseId()+
			" and toPhrase <> "+ toPhrase.getPhraseId()+" and toPhrase.startArtifact.startIndex < "
			+ toPhrase.getStartArtifact().getStartIndex();
	
	List<PhraseLink> link_objects = 
	(List<PhraseLink>) HibernateUtil.executeReader(hql);
		
	return link_objects.size()>0;
}


public void setAltLinkID(String altLinkID) {
	this.altLinkID = altLinkID;
}


public String getAltLinkID() {
	return altLinkID;
}


public static List<PhraseLink> findAllPhraseLinkInDocument(Artifact doc) {
	String hql = "from PhraseLink where toPhrase.endArtifact.parentArtifact.parentArtifact= :docId ";
	HashMap<String, Object> params = new HashMap<String, Object>();
	params.put("docId", doc.getArtifactId());
	
	List<PhraseLink> link_objects = 
			(List<PhraseLink>) HibernateUtil.executeReader(hql,params);
	
	return link_objects;
}
public static String findLargestAltLinkId(String filePath,String altIdNameGroup)
{
	String largest_id = null;
	String hql = "from PhraseLink where fromPhrase.startArtifact.associatedFilePath= :filePath " +
			" and altLinkId like :altLinkIDGroup order by altLinkId desc ";
	HashMap<String, Object> params = new HashMap<String, Object>();
	params.put("filePath", filePath);
	params.put("altLinkIDGroup", altIdNameGroup+'%');
	
	List<PhraseLink> link_objects = 
		(List<PhraseLink>) HibernateUtil.executeReader(hql,params);
	PhraseLink pl = null;
	String altId = null;
	
	if (link_objects.get(0) != null)
	{
		pl = link_objects.get(0);
		altId =  pl.getAltLinkID();
		largest_id = altId.replaceAll(altIdNameGroup+"(\\d+)", "$1");
	}
	
	
	return largest_id;
}
public static List<PhraseLink> getPhraseLinkBetweenSent() {
	String hql = "from PhraseLink where " +
			" fromPhrase.startArtifact.parentArtifact <>  toPhrase.startArtifact.parentArtifact " +
			" and altLinkID is not null and altLinkID not like 'SECTIME%'" +
			" order by fromPhrase.startArtifact.parentArtifact.parentArtifact";
	List<PhraseLink> link_objects = 
		(List<PhraseLink>) HibernateUtil.executeReader(hql);
	return link_objects;
}
public static List<PhraseLink> getPhraseLinkBetweenSentByType(LinkType type) {
	String hql = "from PhraseLink where " +
			" fromPhrase.startArtifact.parentArtifact <>  toPhrase.startArtifact.parentArtifact " +
			" and altLinkID is not null and altLinkID not like 'SECTIME%' " +
			"and linkType ="+type.ordinal();
	List<PhraseLink> link_objects = 
		(List<PhraseLink>) HibernateUtil.executeReader(hql);
	return link_objects;
}


public static PhraseLink getInstance(Artifact artifact1, Artifact artifact2) {
	Phrase phrase1 = Phrase.getInstance(artifact1.getContent(), artifact1, artifact1);
	Phrase phrase2 = Phrase.getInstance(artifact2.getContent(), artifact2, artifact2);
	
	return getInstance(phrase1, phrase2);
}


public static boolean sentencesLinked(Artifact sentence1, Artifact sentence2) {
	String hql = "from PhraseLink where " +
			" fromPhrase.startArtifact.parentArtifact = "+ sentence1.getArtifactId() +
			" and toPhrase.startArtifact.parentArtifact = " + sentence2.getArtifactId() +
			" and altLinkID is not null and altLinkID not like 'SECTIME%' ";
	Session session = HibernateUtil.sessionFactory.openSession();
	List<PhraseLink> link_objects = 
		(List<PhraseLink>) HibernateUtil.executeReader(hql, null, null, session);
	session.clear();
	session.close();
	return link_objects.size()>0?true:false;
}

public static PhraseLink findPhraseLink(Phrase pfromPhrase, Phrase pToPhrase){
	String hql = "from PhraseLink where fromPhrase = "+
		pfromPhrase.getPhraseId()+" and toPhrase="+pToPhrase.getPhraseId();
	
	List<PhraseLink> link_objects = 
			(List<PhraseLink>) HibernateUtil.executeReader(hql);
    
    
	PhraseLink phraseLink_obj = null;
    if(link_objects.size()!=0)
    {
    	phraseLink_obj = link_objects.get(0);
    	
    }
    return phraseLink_obj;
}
public static PhraseLink findPhraseLinkForExamples(Phrase pfromPhrase, Phrase pToPhrase){
	String hql = "from PhraseLink where forTrain is null and fromPhrase = "+
		pfromPhrase.getPhraseId()+" and toPhrase="+pToPhrase.getPhraseId();
	
	List<PhraseLink> link_objects = 
			(List<PhraseLink>) HibernateUtil.executeReader(hql);
    
    
	PhraseLink phraseLink_obj = null;
    if(link_objects.size()!=0)
    {
    	phraseLink_obj = link_objects.get(0);
    	
    }
//    else
//    {
//    	String hql2 = "from PhraseLink where fromPhrase = "+
//    	pToPhrase.getPhraseId()+" and toPhrase="+pfromPhrase.getPhraseId();
//	
//    	link_objects = 
//			(List<PhraseLink>) HibernateUtil.executeReader(hql);
//    	if(link_objects.size()!=0)
//        {
//        	phraseLink_obj = link_objects.get(0);
//        	
//        }
//    }
    return phraseLink_obj;
}
@Transient
public boolean isLeftToRight() {
	
	return getFirstPhrase().equals(fromPhrase);
}


public void setLinkTypeReal(LinkType linkTypeReal) {
	this.linkTypeReal = linkTypeReal;
}


public LinkType getLinkTypeReal() {
	return linkTypeReal;
}


public void setLinkTypeClosure(LinkType linkTypeClosure) {
	this.linkTypeClosure = linkTypeClosure;
}


public LinkType getLinkTypeClosure() {
	return linkTypeClosure;
}


public void setLinkTypeIntegrated(LinkType linkTypeIntegrated) {
	this.linkTypeIntegrated = linkTypeIntegrated;
}


public LinkType getLinkTypeIntegrated() {
	return linkTypeIntegrated;
}
	
}
