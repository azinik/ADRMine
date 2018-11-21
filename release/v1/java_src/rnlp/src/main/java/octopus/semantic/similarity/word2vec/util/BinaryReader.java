package octopus.semantic.similarity.word2vec.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class BinaryReader {
	private static final int FILE_INDX_SIZE = 100000;
	static String filePath = "/home/azadeh/projects/java/deext/data/word2VecFiles/GoogleNews-vectors-negative300.bin";
//	static HashMap<String, Long> wordIndex = new HashMap<String, Long>(); 
	public static void main(String[] args) throws IOException{
//		FileInputStream fin=null;
//		DataInputStream din=null;
////		HashMap<String, Float[]> vectors = new HashMap<String, Float[]>(); 
//		try {
//			File file = new File(filePath);
//			fin = new FileInputStream(file);
//			din = new DataInputStream(fin);
//			String sizeLine = din.readLine();
//			Long words = Long.parseLong(sizeLine.split(" ")[0]);
//			Long size = Long.parseLong(sizeLine.split(" ")[1]);
//			System.out.println("words: "+words);
//			System.out.println("size: "+size);
//			Long startIndex = (long) (sizeLine.length()+1);
//			File index = new File("1_index.bin");
//			BufferedWriter ds = new BufferedWriter(new FileWriter(index));
//		    int indexCounted = 1;
//			int startWordIndex = 0;
////			while(index.exists()){
////				indexCounted++;
////				index = new File(indexCounted+"_index.bin");
////				startWordIndex+=FILE_INDX_SIZE;
////			}
//			
//			for (int b = 0 ; b < words; b++) {
//				List<Byte> curWordBytes = new ArrayList<Byte>();
//				byte curByte = din.readByte();
//				String curChar = Character.toString((char)curByte);
//				long curWordStart = startIndex;
//				while(!curChar.equals(" ")){
//					curWordBytes.add(Byte.valueOf(curByte));
//					curByte = din.readByte();
//					curChar = Character.toString((char)curByte);
//					startIndex++;
//				}
//				startIndex++;
//				byte[] byteArray = new byte[curWordBytes.size()];
//				for(int i=0;i<byteArray.length;i++){
//					byteArray[i] = curWordBytes.get(i).byteValue();
//				}
//				String curWord = new String(byteArray, Charset.forName("UTF-8"));	
//				for(int a=0;a<size;a++){
//					din.readFloat();
//					startIndex+=(Float.SIZE/8);
//				}
//				System.out.println(b+"---"+curWord);
////				if(b<startWordIndex){
//					ds.write(curWord.trim()+"->"+curWordStart+"\n");
//					ds.flush();
//					if(b>0&&b%FILE_INDX_SIZE==0){
//						indexCounted++;
//						ds.close();
//						index = new File(indexCounted+"_index.bin");
//						ds = new BufferedWriter(new FileWriter(index));
//					}
////				}
//			}
//			ds.flush();
//			ds.close();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}finally{
//			din.close();
//			fin.close();
//		}
		
		
		System.out.println(Arrays.deepToString(readWordVector("with")));
	}
	static HashMap<String, Long> index = null; 
	public static Float[] readWordVector(String content) throws IOException {
		if(index == null){
			loadIndex();
		}
		Float[] curWordVector = null; 
		Long offset = index.get(content);

		if(offset!=null){
			RandomAccessFile r = new RandomAccessFile(filePath, "r");
			r.seek(offset);
			byte curByte = r.readByte();
			String curChar = Character.toString((char)curByte);
			List<Byte> curWordBytes = new ArrayList<Byte>();
			while(!curChar.equals(" ")){
				curWordBytes.add(Byte.valueOf(curByte));
				curByte = r.readByte();
				curChar = Character.toString((char)curByte);
			}
			byte[] byteArray = new byte[curWordBytes.size()];
			for(int i=0;i<byteArray.length;i++){
				byteArray[i] = curWordBytes.get(i).byteValue();
			}
			String curWord = new String(byteArray, Charset.forName("UTF-8"));	
			System.out.println(curWord);
			assert(curWord.equals(content));
			curWordVector = new Float[300];
			for(int a=0;a<300;a++){
				curWordVector[a] =	r.readFloat();
			}

			r.close();
		}
		return curWordVector;
	}
	private static HashMap<String, Long> loadIndex() throws IOException {
		int indexCounted = 1;
		File indexFile = new File(indexCounted+"_index.bin");
		index = new HashMap<String, Long>();
		while(indexFile.exists()){
			loadIndexFile(indexFile);
			indexCounted++;
			indexFile = new File(indexCounted+"_index.bin");
		}
		return index;
	}


	private static void loadIndexFile(
			File indexFile) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(indexFile));
		String curLine =br.readLine();
		while(curLine!=null){
			index.put(curLine.split("->")[0], Long.valueOf(curLine.split("->")[1]));
			curLine = br.readLine();
		}
		br.close();
	}
}
