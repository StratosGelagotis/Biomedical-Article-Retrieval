import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import mitos.stemmer.Stemmer;
//D:/MedicalCollection/00/130964.nxml

public class Word {
	private String name;
	private  static long id = 0 ;
	private long wordId;
	private HashMap<String ,Double> freq ;  // freq lekshs se eggrafo i
	private HashMap<String,HashMap<String,Vector<Long>>> positions; // file name  , htmltag ,position
	private HashMap<String,HashMap<String,Integer> > WordsMap;
	
	public Word(String word){
		Stemmer.Initialize();
		id++;
		this.wordId = id;		
		this.name = Stemmer.Stem(word);		
		WordsMap = new HashMap<String,HashMap<String,Integer>>();	
		freq = new HashMap<String,Double>();
		positions= new HashMap<String,HashMap<String,Vector<Long>>> ();
	}
	
	public long getWordId(){
		return this.wordId;
	}
	
	public HashMap<String,HashMap<String,Integer>> getWordsMap(){
		return this.WordsMap;
	}
	public HashMap<String,HashMap<String,Vector<Long>>> getPositionMap(){
		return this.positions;
	}
	
	public String getName(){
		return this.name;
	}
	
	public void setFreq(String fileName , double freq){
		this.freq.put(fileName, freq);
	}
	
	public HashMap<String,Double> getFreq(){
		return this.freq;
	}
	
	 //A fash b4
	 public String DocumentsFiles(){  
	  return this.name+" "+this.WordsMap.size();  
	 }
	 
	@Override public String toString(){		
		String s="";
		s+="Word: '"+this.name +"' \n";
			
		Set<String> tmp = this.WordsMap.keySet();
		for(String str:tmp){
			s += "In File: "+str+" \n"; 
			HashMap<String, Integer> lola = WordsMap.get(str);
			Set<String> tmp2 = lola.keySet();
			
			for(String str2 : tmp2 ){
				s+="In tag: "+str2+", "+lola.get(str2)+" times\n" ;
			}			
		}
		return s;
	}
	
}
