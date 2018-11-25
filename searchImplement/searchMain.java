package searchImplement;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;
import static java.nio.charset.StandardCharsets.*;
import mitos.stemmer.Stemmer;
import org.apache.commons.lang3.StringUtils;

import gr.uoc.csd.hy463.NXMLFileReader;
import gr.uoc.csd.hy463.Topic;
import gr.uoc.csd.hy463.TopicsReader;
import java.util.ArrayList;


public class searchMain {
	public static final String PostingFile = "./CollectionIndex/PostingFile.txt";
	public static final String VocabularyFile = "./CollectionIndex/VocabularyFile.txt";
	public static final String DocumentFile = "./CollectionIndex/DocumentsFile.txt";
	// <Word in a string, word in VocabularyEntry>
	public static TreeMap<String, VocabularyEntry> vocabularyMap = new TreeMap<String, VocabularyEntry>();
	public static TreeMap<Integer,myDocument> RankedResults=new TreeMap<>();

	public static void main(String[] args) throws Exception {
		long startTime = System.nanoTime();
		// =====BHMA 1=====================
		// diavazoume to vocabularyFile kai
		// to vazoume sto treemap vocabulary map
		loadVocabularyFile();
		Stemmer.Initialize();
		
		Writer output = new BufferedWriter(new FileWriter(new File("./CollectionIndex/results.txt")));
		ArrayList<Topic> topics = TopicsReader.readTopics("./CollectionIndex/topics.xml");
		for (Topic topic : topics) {
			String summary=topic.getSummary();
			String type=topic.getType().toString();
			String topicNumber=Integer.toString(topic.getNumber());
			SimpleSearch(type, summary);
			output.write(GetRankedResults(topicNumber));
			RankedResults.clear();
		}
		output.close();
		long sec =1000000000;
		  long  endTime = System.nanoTime();
		  long totalTime = (endTime-startTime);
		  System.out.println("Programm total time: "+totalTime/sec +"  secs.");
	}
	
	public static String GetRankedResults(String topicNo) throws UnsupportedEncodingException, IOException{
		String res="";
		for(int rank : RankedResults.keySet()){
			if(rank>1000)break;
			String path=RankedResults.get(rank).getFullPath();
			NXMLFileReader xmlFile=new NXMLFileReader(new File(path));
			String pmcid=xmlFile.getPMCID();
			String norma=new DecimalFormat("##.####################").format(RankedResults.get(rank).getCosSim());
			res+=topicNo+" 0 " + path+" "+pmcid+" "+ rank +" "+norma+" "+" \n";
		}
		return res;
	}
	public static void SimpleSearch(String type,String summary) {
		String query=type+" "+summary;
		String delimiter = "\t\n\r\f ";
		query = query.replaceAll("[\\-\\[\\]\\/\\+\\.(){}!`~;'<_=>?\\^:,]", " ").replaceAll(" +", " ").trim();
		StringTokenizer queryTokens=new StringTokenizer(query,delimiter);
		Stemmer.Initialize();
		Vector <String> queryStrings=new Vector<>();
		HashMap<String,myDocument> docResult = new HashMap<>();
		while(queryTokens.hasMoreTokens()){
			queryStrings.add(Stemmer.Stem(queryTokens.nextToken()));
		}
		Collections.sort(queryStrings);
		
		if(queryStrings.size()!=0){
			VocabularyEntry vtmp;
			for(int t=0;t<queryStrings.size();t++){
				String qtmp=queryStrings.get(t);
				vtmp = vocabularyMap.get(qtmp);
				if (vtmp == null) {
					System.out.println("Cannot find word: " + qtmp + " In any file");
				} else {
					VocabularyEntry vtmpnext = vocabularyMap.higherEntry(qtmp).getValue();
					Vector<String> rafLines;
//					System.out.println(
//							"==================================================\n"+
//							"========= POSTING FILE ENTRIES RESULTS ===========\n"+
//							"==================================================");
					try (RandomAccessFile raf = new RandomAccessFile(PostingFile, "r")) {
						raf.seek(vtmp.getPointer2postingFile());// set raf file
																// pointer
																// at the first byte
																// of
																// the line we want
																// to
																// read
						rafLines = new Vector<String>();
						if (vtmpnext != null) {
							while (raf.getFilePointer() < vtmpnext.getPointer2postingFile()) {
								rafLines.add(new String(raf.readLine().getBytes(ISO_8859_1), UTF_8));
							}
						} else {
							while (raf.getFilePointer() < raf.length()) {
								rafLines.add(new String(raf.readLine().getBytes(ISO_8859_1), UTF_8));
							}
						}
		
//						for (int i = 0; i < rafLines.size(); i++) {
//							System.out.println(rafLines.get(i));
//						}
						
						
						try (RandomAccessFile ranDoc = new RandomAccessFile(DocumentFile, "r")) {
							// System.out.println("SURPISE");
							for (int index = 0; index < rafLines.size(); index++) {
								ranDoc.seek(Integer.parseInt(StringUtils.substringBefore(rafLines.get(index), " ")) - 1);
								String s = ranDoc.readLine();
								myDocument doc;
								if(!docResult.containsKey(s)){
									doc=new myDocument(s);
									doc.getMatchedTerms().put(qtmp, vtmp);
									docResult.put(s, doc);
									//System.out.println("IF");
									}else{
										//System.out.println("ELSE");
										doc=docResult.get(s);
										if(!doc.getMatchedTerms().containsKey(qtmp)){
											doc.getMatchedTerms().put(qtmp, vtmp);
										}
									}
								//System.out.println(s);
							}
						} catch (Exception e) {
							System.out.println("Could not access Document File");
						}
		
					} catch (Exception e) {
						System.out.println("Cannot access Posting File at simpleSearch, MOTHERFUCKER");
					}
				}
			}
//			System.out.println(	"============================================\n"+
//					"=========== DOCUMENTS RESULTS ==============\n"+
//					"============================================");
			//Calculate cosSim
			double qSummedTerms=Math.sqrt((double)queryStrings.size());
			for(myDocument d:docResult.values()){
				double sim= d.getMatchedTerms().size();
				double norma=d.getFileVectorLenght();
				double cosSim=sim/(norma*qSummedTerms);
				d.setCosSim(cosSim);
			}
			
			TreeMap<Double,myDocument> vectorSpaceModelResults=new TreeMap<Double,myDocument>();
			for(myDocument d : docResult.values()){
				vectorSpaceModelResults.put(d.getCosSim(), d);
			}
			
			Vector<myDocument> noTypeResults=new Vector<>();
			int rank=1;
			
			for(double d:vectorSpaceModelResults.descendingKeySet()){
				myDocument tmpDoc=vectorSpaceModelResults.get(d);
				if(tmpDoc.getMatchedTerms().values().contains(vocabularyMap.get(type))){
					RankedResults.put(rank++, tmpDoc);
				}
				else{
					noTypeResults.add(tmpDoc);
				}
			}
			
			for(int i=0;i<noTypeResults.size();i++){
				RankedResults.put(rank++, noTypeResults.get(i));
			}
		}
		else{
			System.out.println("NO QUERY STRINGS");
		}
	}

	public static void loadVocabularyFile() {
		VocabularyEntry vtmp;
		String vline;
		try (BufferedReader br = new BufferedReader(new FileReader(VocabularyFile))) {
			while ((vline = br.readLine()) != null) {
				// System.out.println(vline);
				String vname = StringUtils.substringBefore(vline, " ");
				int vdf = Integer.parseInt(StringUtils.substringBefore(StringUtils.substringAfter(vline, " "), " "));
				long vpointer = Long.parseLong(StringUtils.substringAfter(StringUtils.substringAfter(vline, " "), " "));
				vtmp = new VocabularyEntry(vname, vdf, vpointer);
				vocabularyMap.put(vtmp.getName(), vtmp);
			}
			System.out.println("Print parsed vocabulary file ");
		} catch (Exception e) {
			System.out.println("Could not read Vocabulary file from memory");
			e.printStackTrace();
		}

	}
	
}
