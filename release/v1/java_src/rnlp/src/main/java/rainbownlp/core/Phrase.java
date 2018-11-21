package rainbownlp.core;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Index;

import rainbownlp.util.FileUtil;
import rainbownlp.util.HibernateUtil;

@Entity
@Table( name = "Phrase" )
@Inheritance(strategy=InheritanceType.JOINED)
public class Phrase {
	private String phraseContent;
	
	private Artifact startArtifact;
	private Artifact endArtifact;
	
	private int phraseId;
	
	private String phraseEntityType;
	private String altID;
	private int startCharOffset;
	private int endCharOffset;
	private Artifact headArtifact;
	
	private Integer altLineIndex;
	private Integer altStartWordIndex;
	private Integer altEndWordIndex;
	private String normalizedHead;
	private Integer normalOffset;
	private Artifact govVerb;
	
	//remove these
//	private String umlsId;
//	private String drug;
	
	public Phrase()
	{
		
	}
	/**
	 * Loads Phrase by id
	 * @param pPhraseID
	 * @return
	 */
	public static Phrase getInstance(int pPhraseID) {
		String hql = "from Phrase where phraseId = "+pPhraseID;
		Phrase phrase_obj = 
			(Phrase)HibernateUtil.executeReader(hql).get(0);
		return phrase_obj;
	}

	
	
	/**
	 * Loads or creates the Phrase
	 * @param pPhraseContent
	 * @param pFilePath
	 * @param pStartIndex
	 * @return
	 */
	public static Phrase getInstance(String pPhraseContent, 
			Artifact pStartArtifact, Artifact pEndArtifact){
		String hql = "from Phrase where phraseContent = :phraseContent "+
			" and startArtifact= :startArtifact and endArtifact = :endArtifact ";
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("phraseContent", pPhraseContent);
		params.put("startArtifact", pStartArtifact.getArtifactId());
		params.put("endArtifact", pEndArtifact.getArtifactId());
		
		List<Phrase> phrase_objects = 
				(List<Phrase>) HibernateUtil.executeReader(hql, params);
	    
		Phrase phrase_obj;
	    if(phrase_objects.size()==0)
	    {
	    	phrase_obj = new Phrase();
	    	phrase_obj.setPhraseContent(pPhraseContent);
	    	phrase_obj.setStartArtifact(pStartArtifact);
	    	phrase_obj.setEndArtifact(pEndArtifact);
	    	phrase_obj.setStartCharOffset(pStartArtifact.getStartIndex());
	    	phrase_obj.setEndCharOffset(pEndArtifact.getEndIndex());
	    	
	    	HibernateUtil.save(phrase_obj);
	    	FileUtil.logLine("/tmp/NonExistingPhrase.txt","pPhraseContent"+pPhraseContent
	    			+" pStartArtifact "+pStartArtifact.getArtifactId()+ " pEndArtifact "+pEndArtifact.getArtifactId());
	    }else
	    {
	    	phrase_obj = 
	    		phrase_objects.get(0);
	    }
	    return phrase_obj;
	}

	public static Phrase getInstanceWithoutSavingIndb(String pPhraseContent, 
			Artifact pStartArtifact, Artifact pEndArtifact){
		String hql = "from Phrase where phraseContent = :phraseContent "+
			" and startArtifact= :startArtifact and endArtifact = :endArtifact ";
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("phraseContent", pPhraseContent);
		params.put("startArtifact", pStartArtifact.getArtifactId());
		params.put("endArtifact", pEndArtifact.getArtifactId());
		
		List<Phrase> phrase_objects = 
				(List<Phrase>) HibernateUtil.executeReader(hql, params);
	    
		Phrase phrase_obj;
	    if(phrase_objects.size()==0)
	    {
	    	phrase_obj = new Phrase();
	    	phrase_obj.setPhraseContent(pPhraseContent);
	    	phrase_obj.setStartArtifact(pStartArtifact);
	    	phrase_obj.setEndArtifact(pEndArtifact);
	    	phrase_obj.setStartCharOffset(pStartArtifact.getStartIndex());
	    	phrase_obj.setEndCharOffset(pEndArtifact.getEndIndex());
	    }else
	    {
	    	phrase_obj = 
	    		phrase_objects.get(0);
	    }
	    return phrase_obj;
	}
	//This is for the MADEUP discharge times NOT TO BE USED dor other purpose
	public static Phrase getMadeUpInstance(String pPhraseContent, String altId,String PhraseType){
		String hql = "from Phrase where phraseContent = :phraseContent "+
			" and altID = :paltID and phraseEntityType =:phraseEntityType ";
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("phraseContent", pPhraseContent);
		params.put("paltID",altId);
		params.put("phraseEntityType",PhraseType);
		
		List<Phrase> phrase_objects = 
				(List<Phrase>) HibernateUtil.executeReader(hql, params);
	    
		Phrase phrase_obj;
	    if(phrase_objects.size()==0)
	    {
	    	phrase_obj = new Phrase();
	    	Artifact start = Artifact.getMadeUpInstance(pPhraseContent);
	    	phrase_obj.setStartArtifact(start);
	    	phrase_obj.setEndArtifact(start);
	    	phrase_obj.setPhraseContent(pPhraseContent);
	    	phrase_obj.setAltID(altId);
	    	phrase_obj.setPhraseEntityType(PhraseType);
	    	
	    	HibernateUtil.save(phrase_obj);
	    	
	    }else
	    {
	    	phrase_obj = 
	    		phrase_objects.get(0);
	    }
	    return phrase_obj;
	}
	public static Phrase getInstance(String pPhraseContent, 
			Artifact pStartArtifact, Artifact pEndArtifact, String pPhraseMentionType,
			String pAlt_id){
		String hql = "from Phrase where phraseContent = :phraseContent "+
			" and startArtifact= :startArtifact and endArtifact = :endArtifact " +
			" and phraseEntityType =:phraseEntityType and altID = :altId";
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("phraseContent", pPhraseContent);
		params.put("startArtifact", pStartArtifact.getArtifactId());
		params.put("endArtifact", pEndArtifact.getArtifactId());
		params.put("phraseEntityType", pPhraseMentionType);
		params.put("altId", pAlt_id);
		
		List<Phrase> phrase_objects = 
				(List<Phrase>) HibernateUtil.executeReader(hql, params);
	    
		Phrase phrase_obj;
	    if(phrase_objects.size()==0)
	    {
	    	phrase_obj = new Phrase();
	    	phrase_obj.setPhraseContent(pPhraseContent);
	    	phrase_obj.setStartArtifact(pStartArtifact);
	    	phrase_obj.setEndArtifact(pEndArtifact);
	    	phrase_obj.setPhraseEntityType(pPhraseMentionType);
	    	phrase_obj.setStartCharOffset(pStartArtifact.getStartIndex());
	    	phrase_obj.setEndCharOffset(pEndArtifact.getEndIndex());
	    	HibernateUtil.save(phrase_obj);
	    	FileUtil.logLine("/tmp/NonExistingPhrase.txt","pPhraseContent"+pPhraseContent
	    			+" pStartArtifact "+pStartArtifact.getArtifactId()+ " pEndArtifact "+pEndArtifact.getArtifactId());
	    }else
	    {
	    	phrase_obj = 
	    		phrase_objects.get(0);
	    }
	    return phrase_obj;
	}
	public static Phrase getInstance(String pPhraseContent, 
			Artifact pStartArtifact, Artifact pEndArtifact, String pPhraseMentionType){
		String hql = "from Phrase where phraseContent = :phraseContent "+
			" and startArtifact= :startArtifact and endArtifact = :endArtifact " +
			" and phraseEntityType =:phraseEntityType ";
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("phraseContent", pPhraseContent);
		params.put("startArtifact", pStartArtifact.getArtifactId());
		params.put("endArtifact", pEndArtifact.getArtifactId());
		params.put("phraseEntityType", pPhraseMentionType);
		
		List<Phrase> phrase_objects = 
				(List<Phrase>) HibernateUtil.executeReader(hql, params);
	    
		Phrase phrase_obj;
	    if(phrase_objects.size()==0)
	    {
	    	phrase_obj = new Phrase();
	    	phrase_obj.setPhraseContent(pPhraseContent);
	    	phrase_obj.setStartArtifact(pStartArtifact);
	    	phrase_obj.setEndArtifact(pEndArtifact);
	    	phrase_obj.setPhraseEntityType(pPhraseMentionType);
	    	phrase_obj.setStartCharOffset(pStartArtifact.getStartIndex());
	    	phrase_obj.setEndCharOffset(pEndArtifact.getEndIndex());
	    	
	    	HibernateUtil.save(phrase_obj);
//	    	FileUtil.logLine("/tmp/NonExistingPhrase.txt","pPhraseContent"+pPhraseContent
//	    			+" pStartArtifact "+pStartArtifact.getArtifactId()+ " pEndArtifact "+pEndArtifact.getArtifactId());
	    }else
	    {
	    	phrase_obj = 
	    		phrase_objects.get(0);
	    }
	    return phrase_obj;
	}
	public void setPhraseContent(String content) {
		this.phraseContent = content;
	}
	@Index(name = "phraseContent")
	@Column(name = "phraseContent", nullable = false, length = 1000)
	public String getPhraseContent() {
		if(phraseContent==null) return "";
		return phraseContent;
	}
	
	
	
	public void setStartArtifact(Artifact startArtifact) {
		this.startArtifact = startArtifact;
	}
	@ManyToOne( cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	@JoinColumn(name="startArtifact")
	public Artifact getStartArtifact() {
		return startArtifact;
	}
	public void setEndArtifact(Artifact endArtifactId) {
		this.endArtifact = endArtifactId;
	}
	
	@ManyToOne( cascade = {CascadeType.PERSIST, CascadeType.MERGE} )
	@JoinColumn(name="endArtifact")
	public Artifact getEndArtifact() {
		return endArtifact;
	}
	
	public void setPhraseId(int phraseId) {
		this.phraseId = phraseId;
	}
	
	@Id
	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy = "increment")
	public int getPhraseId() {
		return phraseId;
	}
//	public void setAssociatedFilePath(String _associatedFilePath) {
//		this.associatedFilePath = _associatedFilePath;
//	}
//
//	public String getAssociatedFilePath() {
//		return associatedFilePath;
//	}
	public void setPhraseEntityType(String phraseEntityType) {
		this.phraseEntityType = phraseEntityType;
	}
	public String getPhraseEntityType() {
		return phraseEntityType;
	}
	public static Phrase findInstance(String pFilePath, 
			int pStartLineOffset, int pStartWordOffset,
			int pEndLineOffset, int pEndWordOffset){
		Artifact startArtifcat =
				Artifact.findInstance(Artifact.Type.Word, pFilePath, pStartWordOffset, pStartLineOffset);
		
		Artifact endArtifcat =
			Artifact.findInstance(Artifact.Type.Word, pFilePath, pEndWordOffset, pEndLineOffset);
		
		String hql = "from Phrase where startArtifact =" +
			":startArtifact and endArtifact= :endArtifact";
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("startArtifact", startArtifcat.getArtifactId());
		params.put("endArtifact", endArtifcat.getArtifactId());
		
		List<Phrase> phrase_objects = 
				(List<Phrase>) HibernateUtil.executeReader(hql, params);
	    
	    
		Phrase phrase_obj=null;
	    if(phrase_objects.size()!=0)
	    {
	    	phrase_obj = 
	    		phrase_objects.get(0);
	    }
	    return phrase_obj;
	}
	public static Phrase findInstance(String pPartialFileName, 
			String altId){
		
		String hql = "from Phrase where startArtifact.associatedFilePath like" +
			":filePath and altID = :altID";
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("filePath", '%'+pPartialFileName+'%');
		params.put("altID", altId);
		
		List<Phrase> phrase_objects = 
				(List<Phrase>) HibernateUtil.executeReader(hql, params);
	    
	    
		Phrase phrase_obj=null;
	    if(phrase_objects.size()!=0)
	    {
	    	phrase_obj = 
	    		phrase_objects.get(0);
	    }
	    return phrase_obj;
	}
	public static Phrase findInstance(Artifact startArtifact, Artifact endArtifact,
			String phraseEntityType){
		
		String hql = "from Phrase where startArtifact = :start" +
			" and endArtifact= :end and PhraseEntityType = :phraseEntityType ";
		
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("start", startArtifact.getArtifactId());
		params.put("end", endArtifact.getArtifactId());
		params.put("phraseEntityType", phraseEntityType);
		
		List<Phrase> phrase_objects = 
				(List<Phrase>) HibernateUtil.executeReader(hql, params);
	    
	    
		Phrase phrase_obj=null;
	    if(phrase_objects.size()!=0)
	    {
	    	phrase_obj = 
	    		phrase_objects.get(0);
	    }
	    return phrase_obj;
	}
	
//	public static Phrase findInstance(String pFilePath, 
//			int pStartCharIndex,String pContent){
//		
//		String hql = "from Phrase where startArtifact.startIndex =" +
//			":startCharIndex and startArtifact.associatedFilePath = "+
//			":filePath and phraseContent= :phraseContent";
//		HashMap<String, Object> params = new HashMap<String, Object>();
//		params.put("startCharIndex", pStartCharIndex);
//		params.put("phraseContent", pContent);
//		params.put("filePath", pFilePath);
//		
//		
//		List<Phrase> phrase_objects = 
//				(List<Phrase>) HibernateUtil.executeReader(hql, params);
//	    
//	    
//		Phrase phrase_obj=null;
//	    if(phrase_objects.size()!=0)
//	    {
//	    	phrase_obj = 
//	    		phrase_objects.get(0);
//	    }
//	    return phrase_obj;
//	}
	public static Phrase findInstance(Artifact pStartArtifact,String pContent){
		
		String hql = "from Phrase where startArtifact =" +
			":startArtifact and phraseContent= :phraseContent";
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("startArtifact", pStartArtifact.getArtifactId());
		params.put("phraseContent", pContent);
		
		
		
		List<Phrase> phrase_objects = 
				(List<Phrase>) HibernateUtil.executeReader(hql, params);
	    
	    
		Phrase phrase_obj=null;
	    if(phrase_objects.size()!=0)
	    {
	    	phrase_obj = 
	    		phrase_objects.get(0);
	    }
	    return phrase_obj;
	}
	public void setAltID(String altID) {
		this.altID = altID;
	}
	public String getAltID() {
		return altID;
	}
	public void setStartCharOffset(int startCharOffset) {
		this.startCharOffset = startCharOffset;
	}
	public int getStartCharOffset() {
		return startCharOffset;
	}
	public void setEndCharOffset(int endCharOffset) {
		this.endCharOffset = endCharOffset;
	}
	public int getEndCharOffset() {
		return endCharOffset;
	}
	
	//This method returns all artifacts that are annotated
	@Transient
	public static ArrayList<Artifact> getAnnotatedWordsInSentence(Artifact sentence)
	{
		List<Phrase> phrase_objects = getPhrasesInSentence(sentence);
		
		ArrayList<Artifact> annotated_artifacts = new ArrayList<Artifact>();
		
		for (Phrase p:phrase_objects)
		{
			Artifact start_artifact = p.getStartArtifact();
			Artifact end_artifact =  p.getEndArtifact();
//			annotated_artifacts.add(start_artifact);
			
			Artifact next_artifact =start_artifact;
			while(next_artifact!=null &&
					!next_artifact.equals(end_artifact))
			{
				annotated_artifacts.add(next_artifact);
				next_artifact = next_artifact.getNextArtifact();
			}
			annotated_artifacts.add(end_artifact);
		}
    
	    return annotated_artifacts;
	}
	
	public static List<Phrase> getPhrasesInSentence(Artifact sentence)
	{
		String hql = "from Phrase p where p.startArtifact.parentArtifact =" +
			":sentId and p.endArtifact.parentArtifact =:sentId and p.phraseEntityType<>'Extracted'";
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("sentId", sentence.getArtifactId());
		
		List<Phrase> phrase_objects = 
			(List<Phrase>) HibernateUtil.executeReader(hql, params);
		
		return phrase_objects;
	}
	public static List<Phrase> getOrderedPhrasesInSentence(Artifact sentence)
	{	
		String hql = "from Phrase p where p.startArtifact.parentArtifact =" +
		":sentId and p.endArtifact.parentArtifact =:sentId and phraseEntityType <> 'SECTIME' and p.phraseEntityType<>'Extracted'";
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("sentId", sentence.getArtifactId());
		
		List<Phrase> phrase_objects = (List<Phrase>) HibernateUtil.executeReader(hql, params);
		Collections.sort(phrase_objects, new phraseComparator());
		
		
		return phrase_objects;
	}
	public static Phrase getNextPhraseInSentence(Phrase p,Artifact sentence)
	{	
		Phrase next_Phrase = null;
		List<Phrase> ordered =getOrderedPhrasesInSentence(sentence);
		
		int index_of_p = ordered.indexOf(p);
		if (index_of_p+1< ordered.size())
		{
			next_Phrase = ordered.get(index_of_p+1);
			
		}
		
		
		return next_Phrase;
	}
	public static Phrase getPrevPhraseInSentence(Phrase p,Artifact sentence)
	{	
		Phrase prev_Phrase = null;
		List<Phrase> ordered =getOrderedPhrasesInSentence(sentence);
		
		int index_of_p = ordered.indexOf(p);
		if (index_of_p-1>=0)
		{
			prev_Phrase = ordered.get(index_of_p-1);
			
		}
		
		
		return prev_Phrase;
	}
	public static Artifact getFirstNounInPhrase(Phrase p,Artifact sentence)
	{	
		Artifact first_noun = null;
		Artifact cur_artifact = p.getStartArtifact();
		while (cur_artifact != p.getEndArtifact())
		{
			if(cur_artifact.getPOS().startsWith("NN"))
			{
				first_noun = cur_artifact;
				break;
			}
			cur_artifact = cur_artifact.getNextArtifact();
		}
		if (first_noun ==null)
		{
			first_noun = p.getEndArtifact();
		}

		return first_noun;
	}
	public static Artifact getLastNounInPhrase(Phrase p,Artifact sentence)
	{	
		Artifact last_noun = null;
		Artifact cur_artifact = p.getEndArtifact();
		while (cur_artifact != p.getStartArtifact())
		{
			if(cur_artifact.getPOS().startsWith("NN"))
			{
				last_noun = cur_artifact;
				break;
			}
			cur_artifact = cur_artifact.getPreviousArtifact();
		}
		if (last_noun ==null)
		{
			last_noun = p.getEndArtifact();
		}

		return last_noun;
	}
	public static boolean isNestedPhrase(Phrase p,Artifact sentence)
	{	
		boolean is_nested = false;
		Phrase next_p = getNextPhraseInSentence(p, sentence);
		if(next_p != null && p.getEndArtifact().getWordIndex() >= next_p.getStartArtifact().getWordIndex() )
		{
			is_nested = true;
		}
		if (is_nested==false)
		{
			Phrase prev_p = getPrevPhraseInSentence(p, sentence);
			if(prev_p != null && prev_p.getEndArtifact().getWordIndex() >= p.getStartArtifact().getWordIndex() )
			{
				is_nested = true;
			}
		}
		
		return is_nested;
	}
	@Transient
	public String getPOS() {
		
		Artifact start_artifact = getStartArtifact();
		Artifact end_Artifact  = getEndArtifact();
		Artifact cur_artifact = start_artifact;
		
		String pos = "";
		while(!cur_artifact.equals(end_Artifact))
		{
			pos += cur_artifact.getPOS()+"-";
			cur_artifact = cur_artifact.getNextArtifact();
		}
		pos += cur_artifact.getPOS();
		
		return pos;
		
	}
	//this returns the offsets included in this phrase
	public List<Integer> listWordOffsetsInPhrase()
	{
		List<Integer> included_offsets =  new ArrayList();
		
		Artifact start_artifact = getStartArtifact();
		Artifact end_Artifact  = getEndArtifact();
		Artifact cur_artifact = start_artifact;
		
		while(!cur_artifact.equals(end_Artifact))
		{
			
			included_offsets.add(cur_artifact.getWordIndex());
			cur_artifact = cur_artifact.getNextArtifact();
		}
		included_offsets.add(cur_artifact.getWordIndex());
		return included_offsets;
	}
	//this is somehow duplicate with normalizedhead...
	@ManyToOne( cascade = {CascadeType.PERSIST, CascadeType.MERGE } , fetch=FetchType.LAZY  )
    @JoinColumn(name="headArtifact")
	public Artifact getHeadArtifact() throws SQLException {
		
		if (headArtifact!=null)
		{
			return headArtifact;
		}
		else
		{
			Artifact h= calculateHeadWord();
			setHeadArtifact(h);
//			HibernateUtil.save(this);
			return h;
		}
	}
//	TODO: improve and make it a solid approach
	private Artifact calculateHeadWord() throws SQLException
	{
		Artifact head = getEndArtifact();
		if (getStartArtifact().getArtifactId() == getEndArtifact().getArtifactId())
		{
			return head;
		}
		String phrase_pos= getPOS();
		String type = getPhraseEntityType();
		
		if(type != null && type.equals("EVENT"))
		{	
			if (phrase_pos != null && (phrase_pos.equals("VB-RP") || phrase_pos.equals("VBG-RP")
					|| phrase_pos.equals("VBN-JJ") || phrase_pos.equals("VB-IN") || phrase_pos.equals("VBN-IN"))
					||  phrase_pos.equals("JJ-TO-VB") ||  phrase_pos.startsWith("VBG-TO"))
			{
				head = getStartArtifact();
			}
			else if(!getEndArtifact().getContent().matches(".*\\W$"))
			{
				head = getEndArtifact();
			}
			else
			{
				Artifact cur_artifact = getEndArtifact();
				while(cur_artifact != null && cur_artifact != getStartArtifact() && (cur_artifact.getContent().matches("\\W") ||
						cur_artifact.getContent().matches("\\W.*")))
				{
					cur_artifact = cur_artifact.getPreviousArtifact();
				}
				head = cur_artifact;
			}
//			
		}
		else if (type != null && type.equals("TIMEX3"))
		{
			//get the type of the timex
//			TimexPhrase timex_obj = TimexPhrase.getRelatedTimexFromPhrase(this);
//			TimexPhrase.TimexType  timex_type = timex_obj.getTimexType();
			Artifact sentence = getStartArtifact().getParentArtifact();
//			Phrase next_p= Phrase.getNextPhraseInSentence(this, sentence);
//		
//			if(timex_type != null && timex_type.equals(TimexPhrase.TimexType.DATE))
//			{
				if (getPhraseContent().startsWith("\\d+\\/") || getPhraseContent().startsWith("\\d+-"))
				{
					head = getStartArtifact();
				}
				else
				{
					head = Phrase.getFirstNounInPhrase(this, sentence);
				}
				
//			}
//			else
//			{
//				
//				head = Phrase.getFirstNounInPhrase(this, sentence);
//			}
			
		}
		return head;
	}
	
	public void setHeadArtifact(Artifact headArtifact) {
		this.headArtifact = headArtifact;
	}
	public static List<Phrase> getPhrasesInDocument(String file_path) {
		String hql = "from Phrase p where p.startArtifact.associatedFilePath = :filePath " +
				"and p.phraseEntityType is not null";
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("filePath", file_path);
		
		List<Phrase> phrase_objects = 
			(List<Phrase>) HibernateUtil.executeReader(hql, params);
		
		return phrase_objects;
	}
	@Override
	public String toString()
	{
		return "Id="+getPhraseId()+"/Content="+
			getPhraseContent();
	}
	public void setAltLineIndex(Integer altLineIndex) {
		this.altLineIndex = altLineIndex;
	}
	public Integer getAltLineIndex() {
		return altLineIndex;
	}
	public void setAltStartWordIndex(Integer altStartWordIndex) {
		this.altStartWordIndex = altStartWordIndex;
	}
	public Integer getAltStartWordIndex() {
		return altStartWordIndex;
	}
	public void setAltEndWordIndex(Integer altEndWordIndex) {
		this.altEndWordIndex = altEndWordIndex;
	}
	public Integer getAltEndWordIndex() {
		return altEndWordIndex;
	}
	
	public static boolean isSecTime(Phrase p)
	{
		boolean is_sectime = false;
		Artifact startArtifact = p.getStartArtifact();
		String hql = "from Phrase where startArtifact = "+
		startArtifact.getArtifactId()+" and endArtifact="+
		p.getEndArtifact().getArtifactId()+
		" and phraseEntityType = 'SECTIME'";
		List<Phrase> phrase_obj = 
			(List<Phrase>)HibernateUtil.executeReader(hql);
		if (phrase_obj.size()!=0)
		{
			is_sectime = true;
		}
			
		return is_sectime;
	}
	public void setNormalizedHead(String normalizedHead) {
		this.normalizedHead = normalizedHead;
	}
	public String getNormalizedHead() {
		return normalizedHead;
	}
	public void setNormalOffset(Integer normalOffset) {
		this.normalOffset = normalOffset;
	}
	public Integer getNormalOffset() {
		return normalOffset;
	}
	
	@Override public boolean equals(Object pPhrase)
	{
		if(!(pPhrase instanceof Phrase))
			return false;
		Phrase p = (Phrase)pPhrase;
		return (p.getPhraseId() == phraseId) || 
				(p.getPhraseContent()==phraseContent && 
				p.getStartArtifact().equals(getStartArtifact()) &&
				p.getEndArtifact().equals(getEndArtifact()));
	}
	@Override public int hashCode()
	{
		return phraseId;
	}
	public static class phraseComparator implements Comparator<Phrase>{
	    public int compare(Phrase p1, Phrase p2) {
	        return ((Integer)p1.getStartCharOffset()).compareTo((Integer)(p2.getStartCharOffset()));
	    }
	}
	@Transient	
	public static List<Phrase> getPhrasesBetweenPhrases(Phrase phrase1,Phrase phrase2, String filePath) {
		String hql = "FROM Phrase where startCharOffset > :endphrase1 and endCharOffset<:startphrase2 and startArtifact.associatedFilePath=:filePath";
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("endphrase1", phrase1.getEndCharOffset());
		params.put("startphrase2", phrase2.getStartCharOffset());
		params.put("filePath", filePath);
		return (List<Phrase>) HibernateUtil.executeReader(hql, params);
	}

		
	public Artifact calclateGovVerb() {
		Artifact gov_verb = null;
		String pos = getPOS();
		if (pos==null) return null;
		if (pos != null && (pos.matches("VB|VBD|VBN|VBP|VBZ") || pos.equals("VB-RP") || pos.equals("VBG-RP")|| pos.equals("VBN-JJ") || pos.equals("VB-IN") || pos.equals("VBN-IN"))
				||  pos.equals("JJ-TO-VB") ||  pos.startsWith("VBG-TO"))
		{
			gov_verb = this.getStartArtifact();
		}
		else
		{
			Artifact next = this.getEndArtifact().getNextArtifact();
			if (next != null && next.getPOS()!= null && next.getPOS().startsWith("VB"))
			{
				Artifact next_verb = next.getNextArtifact();
				if (next_verb != null && 
						next_verb.getPOS().matches("VBD"))
				{
					gov_verb = next_verb;
				}
				else
				{
					gov_verb = next;
				}
			}
			else
			{
				Artifact prev = this.getStartArtifact().getPreviousArtifact();
				while (prev != null  && prev.getPOS()!= null &&
						!prev.getPOS().matches("VB|VBD|VBN|VBP|VBZ") )
				{
					prev = prev.getPreviousArtifact();
				}
				if (prev != null && prev.getPOS()!= null && prev.getPOS().startsWith("VB"))
				{
					gov_verb = prev;
				}
			}
			//if still null
			if (gov_verb == null)
			{
				while (next != null  &&
						next.getPOS()!= null && !next.getPOS().matches("VB|VBD|VBN|VBP|VBZ") )
				{
					next = next.getNextArtifact();
				}
				if (next != null && next.getPOS()!= null && next.getPOS().startsWith("VB"))
				{
					Artifact next_verb = next.getNextArtifact();
					if (next_verb.getPOS().matches("VBD"))
					{
						gov_verb = next_verb;
					}
					else
					{
						gov_verb = next;
					}
					
				}
			}
		}
		
		return gov_verb;
	}
	public Phrase getPreviousPhrase(List<Phrase> oregered_list)
	{
		Phrase prev = null;
		int index = oregered_list.indexOf(this)-1;
		if (index !=-1)
		{
			prev = oregered_list.get(index);
		}
		return prev;
		
	}
	@Transient
	public List<Artifact> getAllTokenArtifacts()
	{
		List<Artifact> all_tokens = new ArrayList<>();
		Artifact current =startArtifact;
		
		while (current != null
				&& !current.equals(endArtifact))
		{
			all_tokens.add(current);
			current=current.getNextArtifact();
		}
		
		all_tokens.add(endArtifact);
		
		return all_tokens;
		
	}
	@ManyToOne( cascade = {CascadeType.PERSIST, CascadeType.MERGE} )
    @JoinColumn(name="govVerb")
	public Artifact getGovVerb() {
		if (govVerb==null)
		{
			govVerb = calclateGovVerb();
		}
		return govVerb;
	}
	public void setGovVerb(Artifact govVerb) {
		this.govVerb = govVerb;
	}
//	public String getUmlsId() {
//		return umlsId;
//	}
//	public void setUmlsId(String string) {
//		this.umlsId = string;
//	}
//	public String getDrug() {
//		return drug;
//	}
//	public void setDrug(String drug) {
//		this.drug = drug;
//	}
}
