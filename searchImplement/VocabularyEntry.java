package searchImplement;

public class VocabularyEntry {
	private String name;
	private int df;
	private long pointer2postingFile;
	
	public VocabularyEntry(String name,int df,long pointer2postingFile){
		this.name=name;
		this.df=df;
		this.pointer2postingFile=pointer2postingFile;
	}
	
	public String getName(){
		return this.name;
	}
	public int getdf(){
		return this.df;
	}
	public long getPointer2postingFile(){
		return this.pointer2postingFile;
	}
	public void setName(String toName){
		this.name=toName;
	}
	public void setdf(int todf){
		this.df=todf;
	}
	public void setPointer2postingFile(long pointer){
		this.pointer2postingFile=pointer;
	}
	
	public String toString(){
		return this.name+" "+this.df+" "+this.pointer2postingFile;
	}
}
