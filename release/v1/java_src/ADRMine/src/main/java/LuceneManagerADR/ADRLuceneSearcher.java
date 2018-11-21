package LuceneManagerADR;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.AbstractMap.SimpleEntry;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
//import org.apache.lucene.queryParser.ParseException;
//import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import edu.asu.diego.adrmine.core.ADRLexiconEntry;
import rainbownlp.core.Setting;
import rainbownlp.util.StringUtil;

public class ADRLuceneSearcher {
	static Directory indexDirectory;
	private static  String lucenePath;

	public static int topN=60;
	static{  
		
		setLucenePath(Setting.getValue("LuceneIndexFile")+"/adrProjectLexicon");
//		setLucenePath(Setting.getValue("LuceneIndexFile")+"/adrProjectLexiconSpell");
//		lucenePath = Setting.getValue("LuceneIndexFile")+"/adrProjectLexiconExactMatch";


	}
	
	
	public static void main(String[] args ) throws IOException, ParseException
	{	
		String test_sent =  "I gained an two cardiorespiratory arrests";
		DidYouMeanIndexer si = new DidYouMeanIndexer();
		test_sent = si.getTermByTermCorrectSpell(test_sent);
		
		test_sent= StringUtil.cleanString(test_sent);
		
			test_sent = StringUtil.removeStopWords(test_sent);
		
		
		
		String test = StringUtil.getTermByTermWordnet(test_sent);
//		ADRLexiconEntry ADR_le = getLexiconEntryByContent(test_sent);
		ArrayList<ADRLexiconEntry> ret = ADRLuceneSearcher.getTopRatedADRFromLuceneIndex(test,topN,Setting.getValue("LuceneIndexFile")+"/adrProjectLexicon");
//		int doc = getDocFreq("at",ADRCorpusIndex.getLucenePath());
//		System.out.println(doc);
	}

	public static ADRLexiconEntry getLexiconEntryByContent(String queryContent) throws ParseException {
		String adr_content= null;
		queryContent = queryContent.replaceAll("\\W", "");
		if (!queryContent.matches("\\w+")
				|| queryContent.isEmpty())
		{
			return null;
		}
		try {
			indexDirectory = FSDirectory.open(new File(getLucenePath()));
			
			
			QueryParser qp = new QueryParser(Version.LUCENE_36, "content", new StandardAnalyzer(Version.LUCENE_36));
			Query query = qp.parse("content:"+queryContent);
//			FuzzyQuery query = new FuzzyQuery(new Term("content", queryContent),0,2);
			 
			IndexReader reader = IndexReader.open(indexDirectory);
			
			IndexSearcher searcher = new IndexSearcher(reader);
			ScoreDoc[] hits = searcher.search(query, null, 1).scoreDocs;
//			TopScoreDocCollector collector = TopScoreDocCollector.create(1, true);
//			searcher.search(query,collector);
//			ScoreDoc[] hits = collector.topDocs().scoreDocs;
			if(hits.length>0)
			{
				int i=0;
				for (ScoreDoc h:hits)
				{
					float score = h.score;
					Document hitDoc = searcher.doc(hits[i].doc);
					
					adr_content = hitDoc.get("content");
					
					System.out.println(adr_content);
					System.out.println(score);
					String umls_id = hitDoc.get("umls_id");
//					if (score>=9.0)
//					{
						
						System.out.println(adr_content);
//					}
					i++;
				}
				Document hitDoc = searcher.doc(hits[0].doc);
				
				adr_content = hitDoc.get("content");
				String umls_id = hitDoc.get("umls_id");
				
				ADRLexiconEntry lexicon_entry = new ADRLexiconEntry(adr_content, umls_id);
				
				return lexicon_entry;
			}
				
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
//		catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static int getDocFreq(String token,String lecene_path) throws IOException
	{
		indexDirectory = FSDirectory.open(new File(lecene_path));
		
		token =StringUtil.getTermByTermWordnet(token);
		 
		IndexReader reader = IndexReader.open(indexDirectory);
		TermEnum termEnum = reader.terms(new Term("content", token));
		
		int doc_freq = termEnum.docFreq();
		
		return doc_freq;
	}

	public static String getLucenePath() {
		return lucenePath;
	}

	public static void setLucenePath(String lucenePath) {
		ADRLuceneSearcher.lucenePath = lucenePath;
	}
	
	public static ArrayList<ADRLexiconEntry>  getTopRatedADRFromLuceneIndex(String querystr, int hitsPerPage,
			String luceneIndexPath) { 
		ArrayList<ADRLexiconEntry> search_result = new ArrayList<>();
	    try { 
	    	
	      // Instantiate a query parser 
			QueryParser qp = new QueryParser(Version.LUCENE_36, "content", new StandardAnalyzer(Version.LUCENE_36,CharArraySet.EMPTY_SET ));
		  // Parse 
			Query query = qp.parse("content:"+querystr);
//			indexDirectory = FSDirectory.open(new File(Setting.getValue("LuceneIndexFile")+"/adrProjectLexicon"));
			indexDirectory = FSDirectory.open(new File(luceneIndexPath));
	      // Instantiate a searcher 
	  	 IndexReader reader = IndexReader.open(indexDirectory);
		
		 IndexSearcher searcher = new IndexSearcher(reader); 
	      // Ranker 
	      TopScoreDocCollector collector = TopScoreDocCollector.create( 
	          hitsPerPage, true); 
	      // Search! 
	      searcher.search(query, collector); 
	      // Retrieve the top-n documents 
	      ScoreDoc[] hits = collector.topDocs().scoreDocs; 
	 
	      // Display results 
//	      System.out.println("Found " + hits.length + " hits."); 
	      for (int i = 0; i < hits.length; ++i) { 
	        int docId = hits[i].doc; 
	        Document d = searcher.doc(docId); 
	        

			String adr_content = d.get("content");
			String umls_id = d.get("umls_id");
			ADRLexiconEntry lexicon_entry = new ADRLexiconEntry(adr_content, umls_id);
			search_result.add(lexicon_entry);
	        
	        System.out.println((i + 1) + ". " + d.get("content")); 
	      } 
	 
	      // Close the searcher 
	      searcher.close(); 
	    } catch (Exception e) { 
	      System.out.println("Got an Exception: " + e.getMessage()); 
	    } 
	    return search_result;
	  }
	public static boolean isPhraseInIndex(String querystr, int hitsPerPage, String indexPath) { 
		boolean is_in_index=false;
		
		
	    try { 
	    	
	      // Instantiate a query parser 
			QueryParser qp = new QueryParser(Version.LUCENE_36, "content", new StandardAnalyzer(Version.LUCENE_36));
		  // Parse 
			Query query = qp.parse("content:"+querystr);
			indexDirectory = FSDirectory.open(new File(indexPath));
		      // Instantiate a searcher 
		  	 IndexReader reader = IndexReader.open(indexDirectory);
			
			 IndexSearcher searcher = new IndexSearcher(reader); 
			 // Ranker 
			 TopScoreDocCollector collector = TopScoreDocCollector.create( 
	          hitsPerPage, true); 
			 // Search! 
			 searcher.search(query, collector); 
	      // Retrieve the top-n documents 
			 ScoreDoc[] hits = collector.topDocs().scoreDocs; 

		     for (int i = 0; i < hits.length; ++i) { 
		        int docId = hits[i].doc; 
		        Document d = searcher.doc(docId); 
		        String adr_content = d.get("content");
				String umls_id = d.get("umls_id");
				
		        is_in_index =true;

	//	        System.out.println((i + 1) + ". " + d.get("content")); 
		        break;
	      } 
	 
	      // Close the searcher 
	      searcher.close();
	      reader.close();
	      indexDirectory.close();
	      
	    } catch (Exception e) { 
	      System.out.println("Got an Exception: " + e.getMessage()); 
	    } 
	    return is_in_index;
	  }
	public static boolean isPhraseInIndexVerbatim(String querystr, int hitsPerPage, String indexPath) { 
		boolean is_in_index=false;
		
		
	    try { 
	    	
	      // Instantiate a query parser 
			QueryParser qp = new QueryParser(Version.LUCENE_36, "content", new StandardAnalyzer(Version.LUCENE_36));
		  // Parse 
			Query query = qp.parse("content:"+querystr);
			indexDirectory = FSDirectory.open(new File(indexPath));
	      // Instantiate a searcher 
	  	 IndexReader reader = IndexReader.open(indexDirectory);
		
		 IndexSearcher searcher = new IndexSearcher(reader); 
	      // Ranker 
	      TopScoreDocCollector collector = TopScoreDocCollector.create( 
	          hitsPerPage, true); 
	      // Search! 
	      searcher.search(query, collector); 
	      // Retrieve the top-n documents 
	      ScoreDoc[] hits = collector.topDocs().scoreDocs; 
	 
	      // Display results 
	      System.out.println("Found " + hits.length + " hits."); 
	      boolean is_in_index_with_all_tokens = true;
	      for (int i = 0; i < hits.length; ++i) { 
	        int docId = hits[i].doc; 
	        Document d = searcher.doc(docId); 
	        String adr_content = d.get("content");
	        String[] adr_content_tokens =adr_content.split(" ");
	        for (String token:adr_content_tokens)
	        {
	        	if (!querystr.matches(".*"+token+".*"))
	        	{
	        		is_in_index_with_all_tokens=false;
	        		break;
	        	}
	        }
			if (!is_in_index_with_all_tokens)
			{
				continue;
			}
	        is_in_index =true;

	        
	        System.out.println((i + 1) + ". " + d.get("content")); 
	        break;

	      } 
	 
	      // Close the searcher 
	      searcher.close(); 
	    } catch (Exception e) { 
	      System.out.println("Got an Exception: " + e.getMessage()); 
	    } 
	    return is_in_index;
	  }
	
}
