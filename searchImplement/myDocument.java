package searchImplement;

import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.print.Doc;

public class myDocument {
	private int docID;
	private String fullPath;
	private double fileVectorLenght;
	private TreeMap<String,VocabularyEntry> matchedTerms;
	private double cosSim;
	
	public myDocument(String documentsFileEntry){
		if(!documentsFileEntry.isEmpty()){
				String delimiter = "\t\n\r\f ";
				StringTokenizer tkz=new StringTokenizer(documentsFileEntry,delimiter);
				this.docID=Integer.parseInt(tkz.nextToken());
				this.fullPath=tkz.nextToken();
				this.fileVectorLenght=Double.parseDouble(tkz.nextToken());
				this.matchedTerms=new TreeMap<String,VocabularyEntry>();
				this.cosSim=0;
			}else{
				System.out.println("IN myDocument CONSTRUCTOR\n THE STRING IS EMPTY");
				System.exit(-100);
		}
	}
	
	public int getDocId(){
		return this.docID;
	}
	
	public String getFullPath(){
		return this.fullPath;
	}
	
	public double getFileVectorLenght(){
		return this.fileVectorLenght;
	}
	
	public TreeMap<String,VocabularyEntry> getMatchedTerms(){
		return this.matchedTerms;
	}
	
	public double getCosSim(){
		return this.cosSim;
	}
	
	public void setCosSim(double c){
		this.cosSim=c;
	}
	
	
	
	
	
}
