package rainbownlp.machineLearning;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.Session;
import org.hibernate.annotations.GenericGenerator;

import rainbownlp.core.Artifact;
import rainbownlp.core.FeatureValuePair;
import rainbownlp.core.Phrase;
import rainbownlp.core.PhraseLink;
import rainbownlp.core.PhraseLink.LinkType;
import rainbownlp.core.SentenceChunk;
import rainbownlp.core.Setting;
import rainbownlp.core.Artifact.Type;
import rainbownlp.util.FileUtil;
import rainbownlp.util.HibernateUtil;

@Entity
@Table( name = "MLExample" )
public class MLExample {
	int exampleId;


	int predictedClass;
	int expectedClass;
	boolean forTrain;
	String corpusName;
	String predictionEngine;
	Artifact relatedArtifact;
	PhraseLink relatedPhraseLink;
	private String associatedFilePath;
	private double predictionWeight;
	private int expectedReal;
	private int expectedClosure;
	private int expectedIntegrated;
	
	private SentenceChunk relatedChunk;
	
	private Phrase relatedPhrase;
	
	@Transient
	List<MLExampleFeature> exampleFeatures;
	static public Session hibernateSession; 
	
	String relatedDrug;
	@Transient
	public List<MLExampleFeature> getExampleFeatures()
	{
		if(exampleFeatures==null)
		{
			if(hibernateSession == null)
				hibernateSession = HibernateUtil.sessionFactory.openSession();
			String hql = "from MLExampleFeature where relatedExample = "+
			 getExampleId()+ 
			 " order by featureValuePair.tempFeatureIndex";
//			 " order by featureValuePair.featureValuePairId";
			exampleFeatures = (List<MLExampleFeature>) HibernateUtil.executeReader(hql, null,null, hibernateSession);
		}
		return exampleFeatures;
	}
	@Transient
	public List<MLExampleFeature> getNonEmbeddingsExampleFeatures()
	{
		if(exampleFeatures==null)
		{
			if(hibernateSession == null)
				hibernateSession = HibernateUtil.sessionFactory.openSession();
			String hql = "from MLExampleFeature  where relatedExample = "+
			 getExampleId()+ 
			 " and featureValuePair. featureName not like '%Embeddings%' order by featureValuePair.tempFeatureIndex";
//			 " order by featureValuePair.featureValuePairId";
			exampleFeatures = (List<MLExampleFeature>) HibernateUtil.executeReader(hql, null,null, hibernateSession);
		}
		return exampleFeatures;
	}
	@Transient
	//TODO: improve this, this is a quick way to get a specific one value String feature
	public String getFeatureValueForExample(MLExample e, String featureName)
	{
		
		if(hibernateSession == null || !hibernateSession.isOpen())
			hibernateSession = HibernateUtil.sessionFactory.openSession();
		String hql = "from MLExampleFeature  where relatedExample = "+
		 e.getExampleId()+ 
		 " and featureValuePair. featureName ='"+featureName+"' order by featureValuePair.tempFeatureIndex";
//			 " order by featureValuePair.featureValuePairId";
		List<MLExampleFeature> eFeatures = (List<MLExampleFeature>) HibernateUtil.executeReader(hql, null,null, hibernateSession);
		
		if (eFeatures.isEmpty())
		{
			FileUtil.logLine("/tmp/featureErrors.txt", "no feature for example id:  "+e.getExampleId());
			return "UNK";
		}

		MLExampleFeature feature = eFeatures.get(0);
		FeatureValuePair fvp = feature.getFeatureValuePair();
		
		return fvp.getFeatureValue();
	}
	public String getPredictionEngine() {
		return predictionEngine;
	}

	public void setPredictionEngine(String pPredictionEngine) {
		predictionEngine = pPredictionEngine;
	}


	
	
	public String getCorpusName() {
		return corpusName;
	}
	
	public void setCorpusName(String pCorpusName) {
		corpusName = pCorpusName;
	}
	
	public boolean getForTrain() {
		return forTrain;
	}
//	@ManyToOne( cascade = {CascadeType.PERSIST, CascadeType.MERGE }, fetch=FetchType.LAZY )	
	@ManyToOne( cascade = {CascadeType.PERSIST, CascadeType.MERGE } )
    @JoinColumn(name="relatedArtifact")
	public Artifact getRelatedArtifact() {
		return relatedArtifact;
	}

	public void setRelatedArtifact(Artifact relatedArtifact) {
		this.relatedArtifact = relatedArtifact;
	}

	@ManyToOne( cascade = {CascadeType.PERSIST, CascadeType.MERGE} , fetch=FetchType.LAZY )
    @JoinColumn(name="relatedPhraseLink")
	public PhraseLink getRelatedPhraseLink() {
		return relatedPhraseLink;
	}

	public void setRelatedPhraseLink(PhraseLink relatedPhraseLink) {
		this.relatedPhraseLink = relatedPhraseLink;
	}

	public void setForTrain(boolean isForTrain) {
		forTrain = isForTrain;
	}
	
	public int getPredictedClass() {
		return predictedClass;
	}
	
	public void setPredictedClass(int pPredictedClass) {
		predictedClass = pPredictedClass;
	}
	
	public int getExpectedClass() {
		return expectedClass;
	}
	
	public void setExpectedClass(int pExpectedClass) {
		expectedClass = pExpectedClass;
	}
	@Id
	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy = "increment")
	public int getExampleId() {
		return exampleId;
	}

	public void setExampleId(int exampleId) {
		this.exampleId = exampleId;
	}
	@Temporal(TemporalType.TIMESTAMP)
    Date updateTime;
	
	
	@PrePersist
    protected void onCreate() {
		updateTime = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
    	updateTime = new Date();
    }

	public static MLExample getInstanceForArtifact(Artifact artifact,
			String experimentgroup) {
		String hql = "from MLExample where relatedArtifact = "+
				artifact.getArtifactId() + " and corpusName = '"+
				experimentgroup+"'";
			List<MLExample> example_objects = 
					getExamplesList(hql);
		    
			MLExample example_obj;
		    if(example_objects.size()==0)
		    {
		    	example_obj = new MLExample();
		  
		    	
		    	example_obj.setCorpusName(experimentgroup);
		    	example_obj.setRelatedArtifact(artifact);
		    	
		    	if(Setting.SaveInGetInstance)
			    	saveExample(example_obj);
		    }else
		    {
		    	example_obj = 
		    			example_objects.get(0);
		    }
		    return example_obj;
	}
	public static MLExample findInstanceForArtifact(Artifact artifact,
			String experimentgroup) {
		String hql = "from MLExample where relatedArtifact = "+
				artifact.getArtifactId() + " and corpusName = '"+
				experimentgroup+"'";
			List<MLExample> example_objects = 
					getExamplesList(hql);
		    
			MLExample example_obj;
		    if(example_objects.size()==0)
		    {
		    	example_obj= null;
		    }else
		    {
		    	example_obj = 
		    			example_objects.get(0);
		    }
		    return example_obj;
	}

	public void calculateFeatures(
			List<IFeatureCalculator> featureCalculators) throws Exception {
		for(IFeatureCalculator feature_calculator : featureCalculators)
		{
			feature_calculator.calculateFeatures(this);
			HibernateUtil.clearLoaderSession();
		}
		
	}

	public static MLExample getInstanceForLink(PhraseLink phrase_link,
			String experimentgroup) {
		String hql = "from MLExample where relatedPhraseLink = "+
				phrase_link.getPhraseLinkId() + " and corpusName = '"+
						experimentgroup+"'";
			List<MLExample> example_objects = 
					getExamplesList(hql);
		    
			MLExample example_obj;
		    if(example_objects.size()==0)
		    {
		    	example_obj = new MLExample();
		  
		    	
		    	example_obj.setCorpusName(experimentgroup);
		    	example_obj.setRelatedPhraseLink(phrase_link);
		    	example_obj.setAssociatedFilePath(phrase_link.getFromPhrase().getStartArtifact().getAssociatedFilePath());
		    	
		    	if(Setting.SaveInGetInstance)
		    		saveExample(example_obj);
		    }else
		    {
		    	example_obj = 
		    			example_objects.get(0);
		    }
		    return example_obj;
	}


	public static void saveExample(MLExample example)
	{
		if(hibernateSession == null)
			hibernateSession = HibernateUtil.loaderSession;
		
		HibernateUtil.save(example, hibernateSession);
	}
	static List<MLExample> getExamplesList(String hql)
	{
		List<MLExample> examples;
		if(hibernateSession == null || !hibernateSession.isOpen())
			hibernateSession = HibernateUtil.loaderSession;
		examples = 
			(List<MLExample>) HibernateUtil.executeReader(hql, null, null, hibernateSession);
		return examples;
	}
	static List<MLExample> getExamplesList(String hql, Integer limit)
	{
		List<MLExample> examples;
		if(hibernateSession == null || !hibernateSession.isOpen())
			hibernateSession = HibernateUtil.loaderSession;
		examples = 
			(List<MLExample>) HibernateUtil.executeReader(hql, null, limit, hibernateSession);
		return examples;
	}
	static List<MLExample> getExamplesList(String hql, HashMap<String, Object> params)
	{
		List<MLExample> examples;
		if(hibernateSession == null || !hibernateSession.isOpen())
			hibernateSession = HibernateUtil.loaderSession;
		
		examples = 
			(List<MLExample>) HibernateUtil.executeReader(hql, params, null, hibernateSession);
		return examples;
	}
	public static List<MLExample> temptGetAllExamplesFotTrain(String experimentgroup, boolean for_train)
	{
		String hql = "from MLExample where corpusName = '"+
						experimentgroup+"' and forTrain="+(for_train?1:0)
						+"  and exampleId < 37406 "
						+ " order by relatedArtifact";
		
		return getExamplesList(hql);
	}
	public static List<MLExample> getAllExamples(String experimentgroup, boolean for_train)
	{
		String hql = "from MLExample where corpusName = '"+
						experimentgroup+"' and forTrain="+(for_train?1:0)
						+ " order by relatedArtifact";
		
		return getExamplesList(hql);
	}
	//get limited selected examples 
	public static List<MLExample> getLimitedPreSelectedExamples(String experimentgroup, boolean for_train,String condition)
	{
//		tempIsSelectedForTrain=1
		String hql = "from MLExample where corpusName = '"+
						experimentgroup+"' and forTrain="+(for_train?1:0)
						+ " and "+condition+" order by relatedArtifact";
		
		return getExamplesList(hql);
	}
	public static List<MLExample> getAllExamplesTwitter(String experimentgroup, boolean for_train)
	{
		String hql = "from MLExample where corpusName = '"+
						experimentgroup+"' and forTrain="+(for_train?1:0)
						+" and expectedClosure=1 order by exampleId";
		
		return getExamplesList(hql);
	}
	public static List<MLExample> getAllExamples(boolean for_train)
	{
		String hql = "from MLExample where  forTrain="+(for_train?1:0)
						+"  order by exampleId";
		
		return getExamplesList(hql);
	}
	public static List<MLExample> getExampleById(int example_id, String experimentgroup)
	{
		String hql = "from MLExample where corpusName = '"+
						experimentgroup+"' and exampleId="+example_id
						+" order by exampleId";
		return getExamplesList(hql);
	}
	public static MLExample getExampleById(int example_id)
	{
		String hql = "from MLExample where exampleId="+example_id;
		List<MLExample> example_objects = 
			(List<MLExample>) HibernateUtil.executeReader(hql);
    
		MLExample example_obj=null;
	    if(example_objects.size()!=0)
	    {
	    	example_obj = 
	    		example_objects.get(0);
	    }
	    return example_obj;
	}
	public static List<MLExample> getAllExamples(String experimentgroup, boolean for_train, int limit)
	{
		String hql = "from MLExample where corpusName = '"+
						experimentgroup+"'  and forTrain="+(for_train?1:0)+" order by exampleId";
		return getExamplesList(hql,limit);
	}
	public static List<MLExample> getLastExamples(String experimentgroup, boolean for_train, int limit)
	{
		String hql = "from MLExample where corpusName = '"+
			experimentgroup+"'  and forTrain="+(for_train?1:0)+
				"order by exampleId desc";
		return getExamplesList(hql);
	}
	
	public static List<MLExample> getExampleByExpectedClass(String experimentgroup,boolean for_train, LinkType type)
	{
		String hql = "from MLExample where corpusName = '"+
						experimentgroup+"' and expectedClass="+type.ordinal()
						+" and  forTrain="+(for_train?1:0)+" order by exampleId";
		return getExamplesList(hql);
	}
	
	public static List<MLExample> getExamplesInDocument(String experimentgroup, 
			String doc_path)
	{
		
		String hql = "FROM MLExample "  +
				"where corpusName =:corpusName " +
				" and associatedFilePath = '" +
				doc_path + "' " +
						"order by exampleId desc";
		
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("corpusName", experimentgroup);
		
		return getExamplesList(hql, params);
	}

	public static List<MLExample> getExamplesByDocument(String experimentgroup, 
			boolean for_train, int num_of_documents)
	{
		List<Artifact> docs = Artifact.listByType(Type.Document, for_train);
		if(docs.size()<num_of_documents)
			num_of_documents = docs.size();
		
		String docPaths = "";
		for(int i=0;i<num_of_documents;i++)
			docPaths = docPaths.concat(", '"+docs.get(i).getAssociatedFilePath()+"'");
		docPaths = docPaths.replaceFirst(",", "");
		
		String hql = "FROM MLExample "  +
				"where corpusName =:corpusName " +
				" and forTrain="+(for_train?1:0) +" and associatedFilePath in (" +
						docPaths + ") " +
						"order by associatedFilePath desc";
		
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("corpusName", experimentgroup);
		
		return getExamplesList(hql, params);
	}
	public static List<MLExample> getTokenExamplesBySent(Artifact sent,String experimentgroup, 
			boolean for_train)
	{	
//		String hql = "FROM MLExample "  +
//				"where corpusName =:corpusName " +
//				" and forTrain="+(for_train?1:0) +" and relatedArtifact.parentArtifact=" +sent.getArtifactId()+
//						
//						" order by associatedFilePath desc";
		String hql = "FROM MLExample "  +
				"where corpusName =:corpusName " +
				"  and relatedArtifact.parentArtifact=" +sent.getArtifactId()+
						
						" order by exampleID asc";
		
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("corpusName", experimentgroup);
		
		return getExamplesList(hql, params);
	}
	
	public static List<MLExample> getExamplesByEventTypeByDocument(String experimentgroup, 
			boolean for_train, int num_of_documents, String type1,
			String type2,String order)
	{
		List<Artifact> docs = Artifact.listByType(Type.Document,for_train);
		if(docs.size()<num_of_documents)
			num_of_documents = docs.size();
		
		String docPaths = "";
		if (order.equals("top"))
		{
			for(int i=0;i<num_of_documents;i++)
				docPaths = docPaths.concat(", '"+docs.get(i).getAssociatedFilePath()+"'");
			docPaths = docPaths.replaceFirst(",", "");
		}
		else if(order.equals("last"))
		{
			for(int i=docs.size()-1;i>docs.size()-num_of_documents-1;i--)
				docPaths = docPaths.concat(", '"+docs.get(i).getAssociatedFilePath()+"'");
			docPaths = docPaths.replaceFirst(",", "");
		}
		Integer type1_from_fvpIds = FeatureValuePair.getRelatedFromEventTypeFValuePairIds(type1);
		Integer type1_to_fvpIds = FeatureValuePair.getRelatedToEventTypeFValuePairIds(type1);
		Integer type2_from_fvpIds = FeatureValuePair.getRelatedFromEventTypeFValuePairIds(type2);
		Integer type2_to_fvpIds = FeatureValuePair.getRelatedToEventTypeFValuePairIds(type2);
		
//		String from_fvpIds = "";
//		for(Integer id: fromFeatureValuePairIds)
//		{
//			from_fvpIds = from_fvpIds.concat(", '"+id+"'");
//		}
//		from_fvpIds = from_fvpIds.replaceFirst(",", "");
//		
//
//		String to_fvpIds = "";
//		for(Integer id: toTeatureValuePairIds)
//		{
//			to_fvpIds = to_fvpIds.concat(", '"+id+"'");
//		}
//		to_fvpIds = to_fvpIds.replaceFirst(",", "");
		
		String hql = " FROM MLExample m  "  +
	    "where (( exists (from MLExampleFeature f where m.exampleId =f.relatedExample and featureValuePair in ("+type1_from_fvpIds+"))" +
	    " and exists (from MLExampleFeature f where m.exampleId =f.relatedExample and featureValuePair in ("+type2_to_fvpIds+"))) or" +
	    " (exists (from MLExampleFeature f where m.exampleId =f.relatedExample and featureValuePair in ("+type2_from_fvpIds+")) and " +
	    		"exists (from MLExampleFeature f where m.exampleId =f.relatedExample and featureValuePair in ("+type1_to_fvpIds+")))) " +
	    " and corpusName =:corpusName " +
	    " and forTrain="+(for_train?1:0) +" and " +
	      "associatedFilePath in (" +
	      docPaths + ") " +
      "order by associatedFilePath desc";
		
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("corpusName", experimentgroup);
		
		return getExamplesList(hql, params);
	}
	public static List<MLExample> getLastExamplesByDocument(String experimentgroup, 
			boolean for_train, int num_of_documents)
	{
		List<Artifact> docs = Artifact.listByType(Type.Document,for_train);
		if(docs.size()<num_of_documents)
			num_of_documents = docs.size();
		
		String docPaths = "";
		for(int i=docs.size()-1;i>docs.size()-num_of_documents-1;i--)
			docPaths = docPaths.concat(", '"+docs.get(i).getAssociatedFilePath()+"'");
		docPaths = docPaths.replaceFirst(",", "");
		
		String hql = "FROM MLExample "  +
				"where corpusName =:corpusName " +
				" and forTrain="+(for_train?1:0) +" and associatedFilePath in (" +
						docPaths + ") " +
						"order by associatedFilePath desc";
		
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("corpusName", experimentgroup);
		
		return getExamplesList(hql, params);
	}

	@Override
	public MLExample clone()
	{
		if(relatedArtifact!=null)
			return getInstanceForArtifact(relatedArtifact, corpusName);
		else
			return getInstanceForLink(relatedPhraseLink, corpusName);	}


	
	public static void resetExamplesPredicted(String experimentgroup, boolean for_train) {
		String hql = "update MLExample set predictedClass = -1 where corpusName = '"+
				experimentgroup+"' and forTrain="+(for_train?1:0);
		HibernateUtil.executeNonReader(hql);
	}
	public static void setExamplePredictedClass(int example_id, int predicted) {
		String hql = "update MLExample set predictedClass = "+predicted+" where exampleId="+example_id;
		HibernateUtil.executeNonReader(hql);
	}
	public static void resetExamplesPredictedToDefault(String experimentgroup, boolean for_train, int default_predicted) {
		String hql = "update MLExample set predictedClass = "+default_predicted+" where corpusName = '"+
				experimentgroup+"' and forTrain="+(for_train?1:0);
		HibernateUtil.executeNonReader(hql);
	}

	public void setAssociatedFilePath(String associatedFilePath) {
		this.associatedFilePath = associatedFilePath;
	}

	public String getAssociatedFilePath() {
		return associatedFilePath;
	}
	public static void updateAssociatedFilePath() {
		String hql = "from MLExample ";
		Session tempSession = HibernateUtil.sessionFactory.openSession();
		List<MLExample> examples = 
				(List<MLExample>) HibernateUtil.executeReader(hql, null,null, tempSession);
		
		for (MLExample example: examples)
		{
			PhraseLink related_phrase_link = example.getRelatedPhraseLink();
			Phrase from_phrase = related_phrase_link.getFromPhrase();
			Artifact start_artifact = from_phrase.getStartArtifact();
			
			String file_path = start_artifact.getAssociatedFilePath();
			example.setAssociatedFilePath(file_path);
			HibernateUtil.save(example, tempSession);
			
		}
		tempSession.clear();
		tempSession.close();
	}

	public static MLExample findInstance(PhraseLink phrase_link,
			String experimentgroup) {
		String hql = "from MLExample where relatedPhraseLink = "+
				phrase_link.getPhraseLinkId() + " and corpusName = '"+
						experimentgroup+"'";
			List<MLExample> example_objects = 
					getExamplesList(hql);
		    
			MLExample example_obj=null;
		    if(example_objects.size()!=0)
		    {
		    	example_obj = 
		    			example_objects.get(0);
		    }
		    return example_obj;
	}
	public static MLExample findInstance(PhraseLink phrase_link) {
		String hql = "from MLExample where relatedPhraseLink = "+
				phrase_link.getPhraseLinkId();
		
			List<MLExample> example_objects = 
					getExamplesList(hql);
		    
			MLExample example_obj=null;
		    if(example_objects.size()!=0)
		    {
		    	example_obj = 
		    			example_objects.get(0);
		    }
		    return example_obj;
	}

	public void setPredictionWeight(double predictionWeight) {
		this.predictionWeight = predictionWeight;
	}

	public double getPredictionWeight() {
		return predictionWeight;
	}

	public static List<MLExample> getDecidedExamplesForGraph(Artifact p_sentence) {
		String hql = "from MLExample where predictedClass <> -1 " +
				"and relatedPhraseLink.fromPhrase.startArtifact.parentArtifact = "+
		p_sentence.getArtifactId()+" and relatedPhraseLink.toPhrase.startArtifact.parentArtifact = "+
		p_sentence.getArtifactId();

		List<MLExample> example_objects = 
				getExamplesList(hql);
	    
	    return example_objects;
	}
	 public static List<MLExample> getPhraseExamplesByPredictedClass(Artifact p_sentence, String corpusName, boolean forTrain, Integer predictedClass) {
		 

//		String hql = "from MLExample where (predictedClass = 1 or predictedClass=2 ) and forTrain="+(forTrain?1:0)
		String hql = "from MLExample where (predictedClass = "+predictedClass+" ) and forTrain="+(forTrain?1:0)
					+
				" and corpusName = '"+corpusName+"' and relatedPhrase.startArtifact.parentArtifact = "+p_sentence.getArtifactId();

		List<MLExample> example_objects = 
				getExamplesList(hql);
	    
	    return example_objects;
	}
	 public static List<MLExample> getPhraseExamplesByCorpus(Artifact p_sentence, String corpusName, boolean forTrain) {
		 
		String hql = "from MLExample where forTrain="+(forTrain?1:0)
					+
				" and corpusName = '"+corpusName+"' and relatedPhrase.startArtifact.parentArtifact = "+p_sentence.getArtifactId();

		List<MLExample> example_objects = 
				getExamplesList(hql);
	    
	    return example_objects;
	}

	public void setExpectedReal(int expectedReal) {
		this.expectedReal = expectedReal;
	}

	public int getExpectedReal() {
		return expectedReal;
	}

	public void setExpectedClosure(int expectedClosure) {
		this.expectedClosure = expectedClosure;
	}

	public int getExpectedClosure() {
		return expectedClosure;
	}

	public void setExpectedIntegrated(int expectedIntegrated) {
		this.expectedIntegrated = expectedIntegrated;
	}

	public int getExpectedIntegrated() {
		return expectedIntegrated;
	}
	////////
	public static MLExample getInstanceForChunk(SentenceChunk relatedChunk,
			String experimentgroup) {
		String hql = "from MLExample where relatedChunk = "+
				relatedChunk.getChunkId() + " and corpusName = '"+
				experimentgroup+"'";
			List<MLExample> example_objects = 
					getExamplesList(hql);
		    
			MLExample example_obj;
		    if(example_objects.size()==0)
		    {
		    	example_obj = new MLExample();
		    	example_obj.setCorpusName(experimentgroup);
		    	example_obj.setRelatedChunk(relatedChunk);
		    	
		    	if(Setting.SaveInGetInstance)
			    	saveExample(example_obj);
		    }else
		    {
		    	example_obj = 
		    			example_objects.get(0);
		    }
		    return example_obj;
	}
	public static MLExample getInstanceForPhrase(Phrase relatedPhrase,
			String experimentgroup) {
		String hql = "from MLExample where relatedPhrase = "+
				relatedPhrase.getPhraseId() + " and corpusName = '"+
				experimentgroup+"'";
			List<MLExample> example_objects = 
					getExamplesList(hql);
		    
			MLExample example_obj;
		    if(example_objects.size()==0)
		    {
		    	example_obj = new MLExample();
		    	example_obj.setCorpusName(experimentgroup);
		    	example_obj.setRelatedPhrase(relatedPhrase);
		    	
		    	if(Setting.SaveInGetInstance)
			    	saveExample(example_obj);
		    }else
		    {
		    	example_obj = 
		    			example_objects.get(0);
		    }
		    return example_obj;
	}
	@OneToOne( cascade = {CascadeType.PERSIST, CascadeType.MERGE} , fetch=FetchType.LAZY )
    @JoinColumn(name="relatedChunk")
	public SentenceChunk getRelatedChunk() {
		return relatedChunk;
	}

	public void setRelatedChunk(SentenceChunk relatedChunk) {
		this.relatedChunk = relatedChunk;
	}
	@Transient
	public String getRelatedDrug() {
		return relatedDrug;
	}

	public void setRelatedDrug(String relatedDrug) {
		this.relatedDrug = relatedDrug;
	}
//	@OneToOne( cascade = {CascadeType.PERSIST, CascadeType.MERGE} , fetch=FetchType.LAZY )
	@OneToOne( cascade = {CascadeType.PERSIST, CascadeType.MERGE} )
    @JoinColumn(name="relatedPhrase")
	public Phrase getRelatedPhrase() {
		return relatedPhrase;
	}
	public void setRelatedPhrase(Phrase relatedPhrase) {
		this.relatedPhrase = relatedPhrase;
	}
}