package rainbownlp.parser;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import rainbownlp.util.FileUtil;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.LabeledScoredTreeFactory;
import edu.stanford.nlp.trees.NPTmpRetainingTreeNormalizer;
import edu.stanford.nlp.trees.PennTreeReader;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreePrint;
import edu.stanford.nlp.trees.TreebankLanguagePack;

public class StanfordParser {
	static MaxentTagger tagger;
	static LexicalizedParser lp;
	static TreebankLanguagePack tlp;
	
	static{
		tlp = new PennTreebankLanguagePack();
	}
	
	public String tagged_sentence;
	public Tree bufferTree = null;
	
	public static void main(String[] args)
	{
		StanfordParser parser = new StanfordParser();
		parser.parse("this is a test");
		String tagged = parser.getTagged();
		String dependencies = parser.getDependencies();
		String penn_tree = parser.getPenn();
		
		System.out.println(tagged);
		System.out.println(penn_tree);
		System.out.println(dependencies);
	}
	public StanfordParser()
	{
		if(lp == null)
			lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz"); 
	}
	
	public String getTagged() {

		TreePrint tp = new TreePrint("wordsAndTags");
		Writer parse_string = new StringWriter();
		PrintWriter printWriter = new PrintWriter(parse_string);
		
		tp.printTree(bufferTree, printWriter); // print tree
	
		return parse_string.toString();
		
	}
	
	public static String getTagged(String filename, int sentence_line) {
		List<String> taggs = FileUtil.loadLineByLine(filename.replace(".txt", ".pos"));
		if(sentence_line-1<0)
			return "";
		return taggs.get(sentence_line-1);
	}
	
	public void parse(String sentence)
	{
		if(sentence.equals("")) return;
		// prepare Parser, Tokenizer and Tree printer:
		if(lp == null)
			lp = LexicalizedParser.loadModel("nlpdata/englishFactored.ser.gz"); 

		
		
		// print sentence:
//		System.out.println ("\n\n\n\nORIGINAL:\n\n" + sentence);
		
		
		// put tokens in a list:
		Tokenizer<? extends HasWord> toke = 
	        tlp.getTokenizerFactory().getTokenizer(new StringReader(sentence));
	    List<? extends HasWord> sentence_tokenized = toke.tokenize();
	      
		if(sentence_tokenized.size() ==0 || (sentence_tokenized.size()==1 && sentence_tokenized.get(0).equals(""))) return;
		
		bufferTree = lp.apply(sentence_tokenized);
	}
	
	public String getPenn()
	{
		if(bufferTree==null) return "";
		TreePrint tp = new TreePrint("penn");
		
		Writer parse_string = new StringWriter();
		PrintWriter printWriter = new PrintWriter(parse_string);
		
		tp.printTree(bufferTree, printWriter); // print tree
		
		return parse_string.toString();
	}
	public String getDependencies()
	{
		TreePrint tp = new TreePrint("typedDependenciesCollapsed");
		Writer parse_string = new StringWriter();
		PrintWriter printWriter = new PrintWriter(parse_string);
		
		tp.printTree(bufferTree, printWriter); // print tree
		
		return parse_string.toString();
	}
	
	
	
	public void load(String penn) throws IOException
	{
	    
	    Reader in = new StringReader(penn);
	    PennTreeReader tr = new PennTreeReader(in, new LabeledScoredTreeFactory(),
                new NPTmpRetainingTreeNormalizer());
	    bufferTree = tr.readTree();
	}
	
	public void load(String penn,String tagged) throws IOException
	{
	    
	    Reader in = new StringReader(penn);
	    PennTreeReader tr = new PennTreeReader(in, new LabeledScoredTreeFactory(),
                new NPTmpRetainingTreeNormalizer());
	    bufferTree = tr.readTree();
	    
	    tagged_sentence = tagged;
	}
	
}
