package rainbownlp.parser;

import java.util.ArrayList;
import java.util.List;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.SimpleGraph;

import rainbownlp.core.Artifact;
import rainbownlp.core.graph.GraphEdge;

// This uses  Stanford Dependencies (SD)  file
public class DependenciesTreeUtil {
	
	public Artifact sentenceArtifact;
	
	UndirectedGraph<String, GraphEdge> sentenceTree =
            new SimpleGraph<String, GraphEdge>(GraphEdge.class);
	
	List<DependencyLine> dependencies;



	private void makeTrees() {
			
			for(DependencyLine depLine:dependencies)
			{
				if(!sentenceTree.containsVertex(depLine.firstPart+depLine.firstOffset))
					sentenceTree.addVertex(depLine.firstPart+depLine.firstOffset);
				if(!sentenceTree.containsVertex(depLine.secondPart+depLine.secondOffset))
					sentenceTree.addVertex(depLine.secondPart+depLine.secondOffset);
				
				//make link v1 & v2
				sentenceTree.addEdge(depLine.firstPart+depLine.firstOffset, 
						depLine.secondPart+depLine.secondOffset);
			}
	}
	
	
	
	
	

	public List<GraphEdge> getParseTreePath(Artifact headWord1, Artifact headWord2) {
		
		List<GraphEdge> path = null;
		
		//link tokens
		 try {
			int word1Offset = headWord1.getWordIndex()+1;
			int word2Offset = headWord2.getWordIndex()+1;
			
			path = 
				DijkstraShortestPath.findPathBetween(sentenceTree, 
						headWord1.getContent()+word1Offset, 
						headWord2.getContent()+word2Offset);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("error in getParseTreeDistance:"+headWord1+headWord2);
		}
		
		return path;
	}
	
	// find the dependencies of a word within a sentence
	public List<String> getDependencyLinkedWords(String word, int word_index, int sentence_id) throws Exception{
		
		List<String> dependencyType=new ArrayList<String>();
		
		for(DependencyLine dependency : dependencies)
		{
			if(dependency.firstOffset == word_index)
				dependencyType.add(dependency.secondPart);
			if(dependency.secondOffset == word_index)
				dependencyType.add(dependency.firstPart);
		}
				
		return dependencyType;
	}	
	// find the dependencies of a word within a sentence
		public List<String> getDependencyTypes(String word, int sentence_id) throws Exception{
			
			List<String> dependencyType=new ArrayList<String>();
			
			for(DependencyLine dependency : dependencies)
			{
				if(dependency.hasWord(word))
					dependencyType.add(dependency.relationName);
			}
				
			return dependencyType;
		}
}
