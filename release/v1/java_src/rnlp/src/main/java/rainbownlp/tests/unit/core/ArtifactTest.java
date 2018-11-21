//package rainbownlp.tests.unit.core;
//
//import static org.junit.Assert.*;
//
//import org.junit.Test;
//
//import rainbownlp.core.Artifact;
//import rainbownlp.core.Artifact.Type;
//import rainbownlp.util.HibernateUtil;
//
//public class ArtifactTest   {
//	
//	@Test
//	public void testCreateArtifact() {
//		Artifact doc_artifact = Artifact.getInstance(Type.Document);
//		assertNotNull(doc_artifact);
//
//		doc_artifact.setContent( "this is test. hello test.");
//		
//		HibernateUtil.save(doc_artifact);
//
//		assertEquals(doc_artifact.getContent(), "this is test. hello test.");
//		assertEquals(doc_artifact.getArtifactType(), Type.Document);
//		assertTrue(doc_artifact.getArtifactId()!=-1);
//		
//		Artifact sentence_artifact = Artifact.getInstance(Type.Sentence);
//		sentence_artifact.setContent("this is test.");
//		sentence_artifact.setParentArtifact(doc_artifact);
//		
//		HibernateUtil.save(sentence_artifact);
//		
//		assertEquals(sentence_artifact.getContent(), "this is test.");
//		assertEquals(sentence_artifact.getArtifactType(), Type.Sentence);
//		assertEquals(sentence_artifact.getParentArtifact().getArtifactId(), doc_artifact.getArtifactId());
//		
//		
//		Artifact sentence_artifact2 = Artifact.getInstance(Type.Sentence);
//		sentence_artifact2.setContent("hello test.");
//		sentence_artifact2.setParentArtifact(doc_artifact);
//		sentence_artifact2.setPreviousArtifact(sentence_artifact);
//		
//		HibernateUtil.save(sentence_artifact2);
//			
//		assertEquals(sentence_artifact.getNextArtifact().getArtifactId(),
//				sentence_artifact2.getArtifactId());
//		assertEquals(sentence_artifact2.getPreviousArtifact().getArtifactId(), 
//				sentence_artifact.getArtifactId());
//		
//	}
//
//}
