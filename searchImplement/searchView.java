package searchImplement;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;
import static java.nio.charset.StandardCharsets.*;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import mitos.stemmer.Stemmer;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.lang3.StringUtils;

public class searchView extends JFrame {
	public static final String PostingFile = "./CollectionIndex/PostingFile.txt";
	public static final String VocabularyFile = "./CollectionIndex/VocabularyFile.txt";
	public static final String DocumentFile = "./CollectionIndex/DocumentsFile.txt";
	// <Word in a string, word in VocabularyEntry>
	public static TreeMap<String, VocabularyEntry> vocabularyMap = new TreeMap<String, VocabularyEntry>();
	public static TreeMap<Integer,myDocument> RankedResults=new TreeMap<>();
	private JPanel contentPane;
	private JTextField QueryString;
	private JTextField TypeString;

	public static void main(String[] args) throws Exception {
		// =====BHMA 1=====================
		// diavazoume to vocabularyFile kai
		// to vazoume sto treemap vocabulary map
		loadVocabularyFile();
		Stemmer.Initialize();
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					searchView frame = new searchView();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public static String GetRankedResults(){
		String res="";
		for(int rank : RankedResults.keySet()){
			String path=RankedResults.get(rank).getFullPath();
			String norma=new DecimalFormat("##.####################").format(RankedResults.get(rank).getCosSim());
//			<br><a href='" + url +"'>"+url+"</a>
			res+="Rank: "+rank+"<br><a href='file:///"+path+"'>"+path+"</a>"+"<br>Score:"+norma+"<br><br>";
		System.out.println(path);
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
						System.out.println("Cannot access Posting File at simpleSearch,");
					}
				}
			}
			System.out.println(	"============================================\n"+
					"=========== DOCUMENTS RESULTS ==============\n"+
					"============================================");
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
			
			
				
//				System.out.printf("%.30f\n",d);
//				System.out.println(Double.toString(d));
//				System.out.print("DOCUMENT: ");
//				System.out.printf("%.20f",vectorSpaceModelResults.get(d).getCosSim());
//				System.out.println(" "+vectorSpaceModelResults.get(d).getFullPath());
//				System.out.print("WORDS: ");
//				for(String s : vectorSpaceModelResults.get(d).getMatchedTerms().keySet()){
//					System.out.print(s+" ");
//				}
//				System.out.println("\n----------------------");
//			}
//			System.out.println("Total Documents: "+vectorSpaceModelResults.size());
				
				
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
			// for (VocabularyEntry entry : vocabularyMap.values()) {
			// System.out.println(entry);
			// }
		} catch (Exception e) {
			System.out.println("Could not read Vocabulary file from memory");
			e.printStackTrace();
		}

	}
	public searchView() {
		setTitle("Biomedical Articles Retrieval");
	    
		Border lineBorder = BorderFactory.createLineBorder(Color.blue);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 947, 686);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		contentPane.setBorder(lineBorder);

		JLabel QueryLabel = new JLabel("Summary: ");
		QueryLabel.setFont(new Font("Tahoma", Font.PLAIN, 14));
		QueryLabel.setBounds(10, 11, 75, 36);
		contentPane.add(QueryLabel);

		QueryString = new JTextField();
		//QueryString.setText("");
		QueryString.setFont(new Font("Tahoma", Font.PLAIN, 12));
		QueryString.setBounds(95, 21, 140, 20);
		QueryString.setColumns(10);
		contentPane.add(QueryString);

		JLabel TypeLabel = new JLabel("Type: ");
		TypeLabel.setFont(new Font("Tahoma", Font.PLAIN, 14));
		TypeLabel.setBounds(294, 11, 75, 36);
		contentPane.add(TypeLabel);

		TypeString = new JTextField();
		//TypeString.setText("");
		TypeString.setFont(new Font("Tahoma", Font.PLAIN, 12));
		TypeString.setColumns(10);
		TypeString.setBounds(352, 21, 140, 20);
		contentPane.add(TypeString);
		
		 JLabel lblSearchResults = new JLabel("Search Results");
		  lblSearchResults.setFont(new Font("Tahoma", Font.PLAIN, 16));
		  lblSearchResults.setBounds(402, 52, 137, 22);
		  contentPane.add(lblSearchResults);
		
		class UrlHyperlinkListener implements HyperlinkListener {
			@Override
			public void hyperlinkUpdate(final HyperlinkEvent event) {
				if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					try {
						Desktop.getDesktop().browse(event.getURL().toURI());
					} catch (final IOException e) {
						throw new RuntimeException("Can't open URL", e);
					} catch (final URISyntaxException e) {
						throw new RuntimeException("Can't open URL", e);
					}
				}
			}
		};
		
		JEditorPane textPane = new JEditorPane();
		textPane.setBounds(51, 85, 819, 470);		
		textPane.setContentType("text/html");
		String url = "file:///D:/MedicalCollection/03/1065094.nxml";
		textPane.setEditable(false);
		textPane.setName("Results");
		//textPane.setText("<a href='" + url + "'>D:/MedicalCollection/03/1065094</a>");
		textPane.addHyperlinkListener(new UrlHyperlinkListener());

		contentPane.add(textPane);
		
		JScrollPane scrplz = new JScrollPane(textPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrplz.setBounds(51, 85, 819, 470);
		contentPane.add(scrplz);
		
		JButton btnNewButton = new JButton("Search");
		btnNewButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				String summary = QueryString.getText();
				String type = TypeString.getText();		
				SimpleSearch(type, summary);
				String results=searchView.GetRankedResults();
				textPane.setText(results);
				
			}
		});
		btnNewButton.setBounds(564, 20, 140, 23);
		contentPane.add(btnNewButton);

	}
}
