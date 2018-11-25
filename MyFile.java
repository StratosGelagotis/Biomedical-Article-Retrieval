import java.text.DecimalFormat;

public class MyFile {
	
	private String id;
	private String path;
	private static long Id=0;
	private static long byteLinePointer=1;
	private String pointerToDocFile;
	private double FileVectorLen=0.0;
	private int maxWordFreq=0;
	
	
	public MyFile(String path){
		Id++;
		this.path = path;
		this.id = Long.toString(Id);
		this.maxWordFreq = 0;
		this.FileVectorLen = (double)0;
	}
	
	public String getFileId(){
		return this.id;
	}
	public String getPointer2DocFile(){
		return this.pointerToDocFile;
	}
	public void setPointer2DocFile(){
		this.pointerToDocFile=Long.toString(byteLinePointer);
		byteLinePointer+=this.toString().getBytes().length;
	}
	
	public MyFile getMyFile(){
		return this;
	}
	
	public void setFileVectorLen(double len){
		this.FileVectorLen = len; 
	}
	
	public void setMaxWordFreq(int freq){
		this.maxWordFreq = freq;
	}
	
	public String getFileVectorLenString(){
		return Double.toString(this.FileVectorLen);
		//return new DecimalFormat("##########.######").format(this.FileVectorLen);
	}
	
	public double getFileVectorLen(){
		return this.FileVectorLen;
	}
	
	public int getMaxWordFreq(){
		return this.maxWordFreq;
	}
	
	public String getFilePath(){
		return this.path;
	}
	
	public String toString(){
		return this.getFileId()+" "+this.getFilePath()+" "+this.getFileVectorLenString()+"\n";
	}	
	
	
}
