import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
//import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;
import mitos.stemmer.Stemmer;

import gr.uoc.csd.hy463.NXMLFileReader;
import org.apache.commons.lang3.StringUtils;

public class Main {
	
	//static Vector<Word> wordsWithTags = new Vector<>();
	//static Vector<String> allUniqueWords = new Vector<>();
	static Vector<String> StopWordsEn = new Vector<>();
	static Vector<String> StopWordsGr = new Vector<>();
	static TreeMap<String, MyFile> Files = new TreeMap<>(); // filename , object
	static TreeMap<String,Word> fullWords=new TreeMap<>();
	static long positionPointer;
	
	public static void main(String[] args) throws UnsupportedEncodingException, IOException {
		long startTime = System.nanoTime();
		LoadStopWords();
		Stemmer.Initialize();
		File example2 = new File("Z:/gelos/Documents/MedicalCollection");
		listFilesForFolder(example2);
		fileIndexing(); // b6

//		for (String s : fullWords.keySet()) {
//			System.out.println(fullWords.get(s).toString());
//		}

		WriteVocabularyFile(); // b4
		WriteDocumentsFile(); // b5
		postingIndex();// b6
		long sec =1000000000;
		  long  endTime = System.nanoTime();
		  long totalTime = (endTime-startTime);
		  System.out.println("Programm total time: "+totalTime/sec +"  secs.");
	}

	public static void postingIndex() throws FileNotFoundException, IOException {
		String vocFilePath = "./CollectionIndex/VocabularyFile_incomplete.txt";
		try (BufferedReader br = new BufferedReader(new FileReader(vocFilePath))) {
			String line;

			System.out.println("====================================================");
			int linePointerToPostingFile = 0;
			try {
				Writer output = new BufferedWriter(new FileWriter(new File("./CollectionIndex/VocabularyFile.txt")));
				Writer outPostingFile = new BufferedWriter(
						new FileWriter(new File("./CollectionIndex/PostingFile.txt")));
				while ((line = br.readLine()) != null) {
					String str = StringUtils.substringBeforeLast(line, " ");
					output.write(line + " " + linePointerToPostingFile + "\n");

					for (Word tmp: fullWords.values()) {
						
					//Word tmp=fullWords.get(str);
					//if(tmp!=null){
					//	System.out.println("NULL POINTER EXCEPTION");
										
					if (tmp.getName().equals(str)) {
							
							for (String key : tmp.getPositionMap().keySet()) {//For each file the word appears
								
								String toWrite;
								MyFile fl = Files.get(key);
								String docId = fl.getFileId();
								double tf = (tmp.getFreq().get(key)) / (fl.getMaxWordFreq());
								String positions ="";
								for(Vector <Long> v : tmp.getPositionMap().get(key).values()){
									for(int i=0;i<v.size();i++){
										positions+=Long.toString(v.get(i))+" ";
									}
								}
								
								toWrite = fl.getPointer2DocFile()+" "+ str+" "+docId + " " + Double.toString(tf) + " " + positions + "\n";
								
								linePointerToPostingFile+=toWrite.getBytes().length;

								// System.out.println("topost: "+toWrite);
								outPostingFile.write(toWrite);

							} // ths for key

							// }
							// catch (Exception e) {
							// System.out.println("Could not create
							// PostingFile.txt ");
							// }
						} // ths if
					}
				}
				outPostingFile.close();
				output.close();
			} catch (Exception e) {
				System.out.println("Could not create VocabularyFile.txt OR PostingFile.txt ");
			}
		}
	}

	// upologismos tf
	public static void fileIndexing() {
		Vector<Double> weights = new Vector<>();
		Word tmpWord = null;
		double idf = 0, freq = 0;
		int df, N = Files.size();
		
		for (Map.Entry<String, MyFile> entry : Files.entrySet()) {
			String fileName = entry.getKey();
			MyFile myfile = entry.getValue();
			// an periexetai h lejh sto eggrafo
			for (Word q : fullWords.values()) {
				if (q.getWordsMap().containsKey(fileName)) {
					tmpWord = q;//wordsWithTags.get(i);
					df = tmpWord.getWordsMap().size();
					idf = Math.log(N / df);

					// Calculate frequencies
					for (String s : tmpWord.getWordsMap().get(fileName).keySet())
						freq += tmpWord.getWordsMap().get(fileName).get(s);

					// Calculate FreqMax
					for (Word w : fullWords.values()) {
						if (w.getWordsMap().containsKey(fileName)) {
							int tmpFreq = 0;
							// calculate local frequency
							for (String s : w.getWordsMap().get(fileName).keySet()) {
								tmpFreq += w.getWordsMap().get(fileName).get(s);
							} // for
								// set maxFreq
							if (tmpFreq > myfile.getMaxWordFreq()) {
								myfile.setMaxWordFreq(tmpFreq);
							} // if
							w.setFreq(fileName, tmpFreq);
						} // if
					} // for
						// calculate weight
					double weight = (freq / myfile.getMaxWordFreq()) * idf;
					weights.add(weight);
				}
			}
			double sum = 0;
			// System.out.println("weight size is:" + weights.size());
			for (int i = 0; i < weights.size(); i++) {

				sum += (weights.get(i) * weights.get(i));
			}
			myfile.setFileVectorLen(Math.sqrt(sum));
			weights.clear();
		}
	}

	public static void LoadStopWords() throws UnsupportedEncodingException, IOException {

		try (BufferedReader br = new BufferedReader(new FileReader("./StopWords/stopwordsEn.txt"))) {
			String line;
			while ((line = br.readLine()) != null)
				StopWordsEn.addElement(line.toLowerCase());
		}

		try (BufferedReader br = new BufferedReader(new FileReader("./StopWords/stopwordsGr.txt"))) {
			String line;
			while ((line = br.readLine()) != null)
				StopWordsGr.addElement(line.toLowerCase());
		}
	}

	// phase A B5 missing file's vector len
	public static void WriteDocumentsFile() {

		try {
			Writer output = new BufferedWriter(new FileWriter(new File("./CollectionIndex/DocumentsFile.txt")));

			for (String s : Files.keySet()){
				output.write(Files.get(s).toString());
				Files.get(s).setPointer2DocFile();
			}
			output.close();
		} catch (Exception e) {
			System.out.println("Could not create DocumentsFile.txt ");
		}
	}

	// phase A B4
	public static void WriteVocabularyFile() {

//		Collections.sort(wordsWithTags, new Comparator<Object>() {
//			public int compare(Object a, Object b) {
//				return (new String(((Word) a).getName())).compareTo(new String(((Word) b).getName()));
//			}
//		});

		File theDir = new File("CollectionIndex");
		// if the directory does not exist, create it
		if (!theDir.exists()) {
			System.out.println("creating directory: " + "CollectionIndex");
			boolean result = false;
			try {
				theDir.mkdir();
				result = true;
			} catch (SecurityException se) {
			}
			if (result)
				System.out.println("DIR CollectionIndex created");
		}

		try {
			Writer output = new BufferedWriter(
					new FileWriter(new File("./CollectionIndex/VocabularyFile_incomplete.txt")));
//
//			for (int i = 0; i < wordsWithTags.size(); i++)
//				output.write(wordsWithTags.get(i).DocumentsFiles() + "\n");
			
			for(String s:fullWords.keySet())	
				output.write(fullWords.get(s).DocumentsFiles() + "\n");
			
			output.close();
		} catch (Exception e) {
			System.out.println("Could not create VocabularyFile_incomplete.txt");
		}
	}

	public static void listFilesForFolder(File folder) throws UnsupportedEncodingException, IOException {
		String jesus = null;
		int i = 0;
		for (File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				listFilesForFolder(fileEntry);
			} else {
				i++;
				jesus = fileEntry.getAbsolutePath().toString().replace("\\", "/");
				System.out.println("Num of files " + i + "to name : " + fileEntry.getName());
				Files.put(fileEntry.getName(), new MyFile(jesus));
				
				CreateIndex(jesus);
			}
		}
	}

	public static void CreateIndex(String FilePath) throws UnsupportedEncodingException, IOException {
		// D:\MedicalCollection
		File example = new File(FilePath);
		NXMLFileReader xmlFile = new NXMLFileReader(example);
		String pmcid = xmlFile.getPMCID();
		String title = xmlFile.getTitle();
		String abstr = xmlFile.getAbstr();
		String body = xmlFile.getBody();
		String journal = xmlFile.getJournal();
		String publisher = xmlFile.getPublisher();
		ArrayList<String> authors = xmlFile.getAuthors();
		HashSet<String> categories = xmlFile.getCategories();
//
//		findAllUniqueWords(pmcid);
//		findAllUniqueWords(title);
//		findAllUniqueWords(abstr);
//		findAllUniqueWords(body);
//		findAllUniqueWords(journal);
//		findAllUniqueWords(publisher);
//		findAllUniqueWords(authors.toString());
//		findAllUniqueWords(categories.toString());
		positionPointer=1;
		//==============NEW ALGORITHM INSERTED==================
		findAllUniqueWordsWithTags(pmcid,"pmcid",example.getName());
		findAllUniqueWordsWithTags(title,"title",example.getName());
		findAllUniqueWordsWithTags(abstr,"abstr",example.getName());
		findAllUniqueWordsWithTags(body,"body",example.getName());
		findAllUniqueWordsWithTags(journal,"journal",example.getName());
		findAllUniqueWordsWithTags(publisher,"publisher",example.getName());
		findAllUniqueWordsWithTags(authors.toString(),"authors",example.getName());
		findAllUniqueWordsWithTags(categories.toString(), "categories", example.getName());
		//===========END OF NEW ALGORITHM=======================
		
		// Insert each unique word in a class Word Object and in that
		// in a new vector with each object
//		String tmp = null;
//		Word w;
//		HashMap<String, Integer> tag;
//
//		for (int index = 0; index < allUniqueWords.size(); index++) {
//			tmp = allUniqueWords.get(index);
//			w = new Word(tmp);
//			tag = new HashMap<>();
//			if (pmcid.contains(tmp))
//				tag.put("pmcid", StringUtils.countMatches(pmcid, tmp));
//			// apply to the rest of the tags
//			if (title.contains(tmp))
//				tag.put("title", StringUtils.countMatches(title, tmp));
//			if (abstr.contains(tmp))
//				tag.put("abstr", StringUtils.countMatches(abstr, tmp));
//			if (body.contains(tmp))
//				tag.put("body", StringUtils.countMatches(body, tmp));
//			if (journal.contains(tmp))
//				tag.put("journal", StringUtils.countMatches(journal, tmp));
//			if (publisher.contains(tmp))
//				tag.put("publisher", StringUtils.countMatches(publisher, tmp));
//			if (authors.toString().contains(tmp))
//				tag.put("authors", StringUtils.countMatches(authors.toString(), tmp));
//			if (categories.toString().contains(tmp))
//				tag.put("categories", StringUtils.countMatches(categories.toString(), tmp));
//			w.getWordsMap().put(example.getName(), tag);
//
//			boolean ifExists = false;
//			for (int i = 0; i < wordsWithTags.size(); i++) {
//				if (wordsWithTags.elementAt(i).getName().equals(w.getName())) {
//					wordsWithTags.elementAt(i).getWordsMap().put(example.getName(), tag);
//					ifExists = true;
//				}
//			}
//			if (!ifExists)
//				wordsWithTags.addElement(w);
//
//		}
//		allUniqueWords.clear();
	}

	public static void findAllUniqueWordsWithTags(String s, String tags, String FileName) {

		String delimiter = "\t\n\r\f ";
		s = s.replaceAll("[\\-\\[\\]\\/\\+\\.(){}!`~;'<_=>?\\^:,]", " ").replaceAll(" +", " ").trim();
		StringTokenizer tokenizer = new StringTokenizer(s, delimiter);
		Stemmer.Initialize();
		
		while (tokenizer.hasMoreTokens()) {
			Word tmp;
			//       tag, fores
			HashMap <String, Integer> tag;
			//		tag,   vector me fores
			HashMap<String,Vector<Long>> wordPositionMap;
			String currentToken =Stemmer.Stem(tokenizer.nextToken());
			if (!StopWordsEn.contains(currentToken.toLowerCase())
					&& !StopWordsGr.contains(currentToken.toLowerCase())) {
				if (!fullWords.containsKey(currentToken)){
					tmp=new Word(currentToken);
					tag=new HashMap<>();
					tag.put(tags, StringUtils.countMatches(s, currentToken));
					wordPositionMap=new HashMap<>();
					Vector<Long> p=new Vector<>();
					p.add(positionPointer);
					wordPositionMap.put(tags, p);
					
					tmp.getWordsMap().put(FileName, tag);
					tmp.getPositionMap().put(FileName, wordPositionMap);
					fullWords.put(currentToken, tmp);
					
				}
				else{ // an uparxei 
					tmp=fullWords.get(currentToken);
					if(tmp.getWordsMap().keySet().contains(FileName)){//ean exei ksanavrethei sto arxeio h leksi
						if(tmp.getWordsMap().get(FileName).keySet().contains(tags)){//ean uparxei sto idio tag
							tmp.getPositionMap().get(FileName).get(tags).add(positionPointer);
						}else{//ean den uparxei sto idio tag
							Vector<Long> p=new Vector<Long>();
							p.add(positionPointer);
							tmp.getPositionMap().get(FileName).put(tags, p);
							tmp.getWordsMap().get(FileName).put(tags , StringUtils.countMatches(s, currentToken));
						}
					}else{//ean den uparxei sto idio file
						tag=new HashMap<>();
						tag.put(tags, StringUtils.countMatches(s, currentToken));
						tmp.getWordsMap().put(FileName, tag );
						wordPositionMap=new HashMap<>();
						Vector <Long> p=new Vector<>();
						p.add(positionPointer);
						wordPositionMap.put(tags, p);
						tmp.getPositionMap().put(FileName, wordPositionMap);
					}
				}
				// allUniqueWords.add(currentToken);
				// if (categories.toString().contains(tmp))
				// tag.put("categories",
				// StringUtils.countMatches(categories.toString(), tmp));
				// w.getWordsMap().put(example.getName(), tag);
				//

			}
			positionPointer++;
		}

	}
//
//	private static void findAllUniqueWords(String s) {
//		String delimiter = "\t\n\r\f ";
//		s = s.replaceAll("[\\-\\[\\]\\/\\+\\.(){}!`~;'<_=>?\\^:,]", " ").replaceAll(" +", " ").trim();
//
//		StringTokenizer tokenizer = new StringTokenizer(s, delimiter);
//
//		while (tokenizer.hasMoreTokens()) {
//			String currentToken = tokenizer.nextToken();
//			if (!StopWordsEn.contains(currentToken.toLowerCase())
//					&& !StopWordsGr.contains(currentToken.toLowerCase())) {
//				if (!allUniqueWords.contains(currentToken))
//					allUniqueWords.add(currentToken);
//			}
//		}
//	}
}