package rainbownlp.tests.unit.core;

import static org.junit.Assert.*;

import org.junit.Test;

import rainbownlp.core.Artifact;
import rainbownlp.core.Artifact.Type;
import rainbownlp.core.FeatureValuePair.FeatureName;
import rainbownlp.core.FeatureValuePair;
import rainbownlp.machineLearning.MLExample;
import rainbownlp.machineLearning.MLExampleFeature;
import rainbownlp.util.HibernateUtil;

public class MLExampleFeatureTest   {
	
	@Test
	public void testCreateArtifact() {
		Artifact doc_artifact = Artifact.getInstance(Type.Document);
		doc_artifact.setContent( "this is test. hello test.");
		HibernateUtil.save(doc_artifact);

		FeatureValuePair feature1 = 
			FeatureValuePair.getInstance(FeatureName.TWOGram, "test_test");
		
		assertNotNull(feature1);
		assertTrue(feature1.getFeatureValuePairId()!=-1);
		assertTrue(feature1.getFeatureValue().equals("test_test"));
		assertTrue(feature1.getFeatureName().equals(FeatureName.TWOGram));
		
		MLExample artifact_example = new MLExample();
		artifact_example.setCorpusName("test");
		artifact_example.setRelatedArtifact(doc_artifact);
		
		assertTrue(artifact_example.getRelatedArtifact().equals(doc_artifact));
		
		MLExampleFeature artifact_feature = 
			MLExampleFeature.setFeatureExample(artifact_example, feature1);
		
		assertTrue(artifact_feature.getFeatureValuePair().equals(feature1));
		
		
	}

}
