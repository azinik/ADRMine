package rainbownlp.machineLearning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.Session;
import org.hibernate.annotations.GenericGenerator;

import rainbownlp.core.FeatureValuePair;
import rainbownlp.util.HibernateUtil;

@Entity
@Table( name = "MLExampleFeature" )
public class MLExampleFeature {
	private Integer exampleFeatureId;
	
	private MLExample relatedExample;
	
	private FeatureValuePair featureValuePair;
	
	public MLExampleFeature()
	{
		
	}
	
	public void setExampleFeatureId(Integer _artifactFeatureId) {
		this.exampleFeatureId = _artifactFeatureId;
	}
	@Id
	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy = "increment")
	public Integer getExampleFeatureId() {
		return exampleFeatureId;
	}
	
	
	public void setRelatedExample(MLExample relatedExample) {
		this.relatedExample = relatedExample;
	}
	@ManyToOne( cascade = {CascadeType.PERSIST, CascadeType.MERGE} )
    @JoinColumn(name="relatedExample")
	public MLExample getRelatedExample() {
		return relatedExample;
	}
	
	
	public void setFeatureValuePair(FeatureValuePair _featureValuePair) {
		featureValuePair = _featureValuePair;
	}
	@ManyToOne( cascade = {CascadeType.PERSIST, CascadeType.MERGE} )
    @JoinColumn(name="featureValuePair")
	public FeatureValuePair getFeatureValuePair() {
		return featureValuePair;
	}

	public static MLExampleFeature setFeatureExample(MLExample pExample,
			FeatureValuePair pNewFeature) {
		return setFeatureExample(pExample, pNewFeature, false);
	}
	public static MLExampleFeature setFeatureExample(MLExample pExample,
			FeatureValuePair pNewFeature, boolean isDenseFeature) {

//		if(!isDenseFeature){
		if(pNewFeature.getFeatureValueAuxiliary()!=null)
			//is multi value
			deleteExampleFeatures(pExample, pNewFeature.getFeatureName()
					,pNewFeature.getFeatureValue());
		else
			deleteExampleFeatures(pExample, pNewFeature.getFeatureName());
//		}
		
		MLExampleFeature artifact_feature = new MLExampleFeature();
		artifact_feature.setFeatureValuePair(pNewFeature);
		artifact_feature.setRelatedExample(pExample);
		
		
		Session session = HibernateUtil.sessionFactory.openSession();
		
		HibernateUtil.save(artifact_feature, session);
		
		session.clear();
		session.close();
		
		return artifact_feature;
	}
	private static void deleteExampleFeatures(MLExample pExample,
			String featureName, String featureValue) {
		
		Session session = HibernateUtil.sessionFactory.openSession();
		session.beginTransaction();
		String hql = "from MLExampleFeature af where af.relatedExample = " +
				pExample.getExampleId()+" AND "+
				"af.featureValuePair.featureName = :featureName"+
				" AND af.featureValuePair.featureValue = :featureValue";
		
		List<MLExampleFeature> artifact_feature_list = 
			session.createQuery(hql).setParameter("featureValue", featureValue)
			.setParameter("featureName", featureName).list();
		
		
		for(MLExampleFeature af : artifact_feature_list)
			session.delete(af);
		
		session.getTransaction().commit();
		session.close();
	}

	/*
	 * delete all attribute name for an specific artifact
	 */
	public static void deleteExampleFeatures(MLExample pExample,
			String featureName) {
		String hql = "from MLExampleFeature as af inner join fetch af.featureValuePair " +
				"where af.relatedExample = " +
				pExample.getExampleId()+" AND "+
				"af.featureValuePair.featureName = :featureName";
		
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("featureName", featureName);
		Session session = HibernateUtil.sessionFactory.openSession();
		
		List<MLExampleFeature> artifact_feature_list = 
			(List<MLExampleFeature>) HibernateUtil.executeReader(hql, params,null, session);
		
		if(artifact_feature_list.size()>0)
		{
			String example_feature_ids = "";
			for(MLExampleFeature af : artifact_feature_list)
				example_feature_ids=example_feature_ids.concat(af.getExampleFeatureId()+",");
			example_feature_ids = example_feature_ids.replaceAll(",$", "");
			HibernateUtil.executeNonReader("delete from MLExampleFeature where exampleFeatureId in ("+
					example_feature_ids+")");
		}
		
		session.close();
	}

	@Override public String toString()
	{
		return " Example = "+relatedExample.toString()+
			" / Feature = "+
			featureValuePair.toString();
	}
	
	@Transient
	public static void truncateTable()
	{
		String hql = "delete from MLExampleFeature";
		HibernateUtil.executeNonReader(hql);
	}

	public static void setBulkFeaturesExample(MLExample exampleToProcess,
			List<FeatureValuePair> features, String featurePrefix) {
//		HibernateUtil.executeNonReader("delete from MLExampleFeature where relatedExample = "+
//				exampleToProcess.getExampleId()+" and featureValuePair in ()");
		
		HibernateUtil.startTransaction();;
		Session session = HibernateUtil.saverSession;
		
		for (FeatureValuePair fvp : features)
		{
			MLExampleFeature artifact_feature = new MLExampleFeature();
			artifact_feature.setFeatureValuePair(fvp);
			artifact_feature.setRelatedExample(exampleToProcess);
			session.save(artifact_feature);
		}
		
		HibernateUtil.endTransaction(); 
		
		
	}

}
