import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.WriteOutContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class solr {

	PrintWriter logfile;
	int num_keywords, num_files, num_fileswithkeywords;
	Map<String, Integer> keyword_counts;
	Date timestamp;
	List<String> keywords;

	/**
	 * constructor DO NOT MODIFY
	 */
	public solr() {
		keywords = new ArrayList<String>();
		num_keywords = 0;
		num_files = 0;
		num_fileswithkeywords = 0;
		keyword_counts = new HashMap<String, Integer>();
		timestamp = new Date();
		try {
			logfile = new PrintWriter("log.txt");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * destructor DO NOT MODIFY
	 */
	protected void finalize() throws Throwable {
		try {
			logfile.close();
		} finally {
			super.finalize();
		}
	}

	/**
	 * main() function instantiate class and execute DO NOT MODIFY
	 */
	public static void main(String[] args) {
		solr instance = new solr();
		instance.run();
	}

	/**
	 * execute the program DO NOT MODIFY
	 */
	private void run() {

		// Open input file and read keywords
		try {
			BufferedReader keyword_reader = new BufferedReader(new FileReader(
					"keywords.txt"));
			String str;
			while ((str = keyword_reader.readLine()) != null) {
				keywords.add(str);
				num_keywords++;
				keyword_counts.put(str, 0);
			}
			keyword_reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Open all pdf files, process each one
		String outputFile = "output/" + "geotags.txt";	
		PrintWriter outfile_New;
		try {
			outfile_New = new PrintWriter(outputFile);
		
		
		   File pdfdir = new File("./vault");
		   File[] pdfs = pdfdir.listFiles(new PDFFilenameFilter());
		   //outfile_New.println("<doc>");
		   for (File pdf : pdfs) {
			  num_files++;
			  processfile(pdf, outfile_New);
			  System.out.println(num_files+" processed out of 2068");
		   }
		   //outfile_New.print("/<doc>");
		   outfile_New.close();
		
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Print output file
		/*try {
			PrintWriter outfile = new PrintWriter("output.txt");
			outfile.print("Keyword(s) used: ");
			if (num_keywords > 0)
				outfile.print(keywords.get(0));
			for (int i = 1; i < num_keywords; i++)
				outfile.print(", " + keywords.get(i));
			outfile.println();
			outfile.println("No of files processed: " + num_files);
			outfile.println("No of files containing keyword(s): "
					+ num_fileswithkeywords);
			outfile.println();
			outfile.println("No of occurrences of each keyword:");
			outfile.println("----------------------------------");
			for (int i = 0; i < num_keywords; i++) {
				String keyword = keywords.get(i);
				outfile.println("\t" + keyword + ": "
						+ keyword_counts.get(keyword));
			}
			outfile.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}*/
	}

	/**
	 * Process a single file
	 * 
	 * Here, you need to: - use Tika to extract text contents from the file -
	 * (optional) check OCR quality before proceeding - search the extracted
	 * text for the given keywords - update num_fileswithkeywords and
	 * keyword_counts as needed - update log file as needed
	 * 
	 * @param f
	 *            File to be processed
	 */
	private void processfile(File f, PrintWriter outfile) {

		/***** YOUR CODE GOES HERE *****/
		// to update the log file with a search hit, use:
		// updatelog(keyword,f.getName());

		String filename = new String(f.toString());
		//filename = filename.replaceAll(".pdf", "");
		filename = filename.replaceAll(".\\\\vault\\\\", "");  	//<---CHANGED for Windows compatibility.  change back for Mac
		//filename = "output/" + filename + ".txt";				//<---CHANGED for Windows compatibility.  change back for Mac
		outfile.println("<doc>");
		outfile.println("<field name = \"id\">" +filename + "</field>");
		System.out.println(filename);
		try {
			PDFParser parser = new PDFParser();
			InputStream fis = new FileInputStream(f);
			StringWriter writer = new StringWriter();
			ContentHandler handler = new WriteOutContentHandler(writer);

			Metadata metadata = new Metadata();
			parser.parse(fis, handler, metadata, new ParseContext());

			String content = writer.toString();

			content = content.replaceAll("\n", " ");
			//String test = "M&^%$at____---t88 ** hu";
			//test = test.replaceAll("[^A-Za-z0-9]", "");
			System.out.print("Removing unnecessary chars....");
			content = content.replaceAll("[^A-Za-z ]", ""); //remove all non alphanumeric characters
			System.out.print("Done!\n");

			String[] keyword = content.split(" ");
			
			HashMap<String, Integer> keywords_freq = new HashMap<String, Integer>();

			for (int i = 0; i < keyword.length; i++) {
				if (keywords_freq.containsKey(keyword[i])) {
					keywords_freq.put(keyword[i],
							keywords_freq.get(keyword[i]) + 1);
				} else {
					keywords_freq.put(keyword[i], 1);
				}

			}

			//System.out.println("THE SIZE OF THE KEYWORDS (before sort): "+keyword.length);
			int[] freq = new int[keyword.length];

			for (int i = 0; i < keyword.length; i++) {
				freq[i] = keywords_freq.get(keyword[i]);
			}

			for (int c = 0; c < keyword.length - 1; c++) {
				for (int d = 0; d < keyword.length - c - 1; d++) {
					if (freq[d] > freq[d + 1]) /* For descending order use < */
					{
						int swap;
						swap = freq[d];
						freq[d] = freq[d + 1];
						freq[d + 1] = swap;

						String s = "";
						s = keyword[d];
						keyword[d] = keyword[d + 1];
						keyword[d + 1] = s;

					}
				}
			}

			//for (int i = 0; i < keyword.length; i++) {
			//	System.out
		//				.println("keyword:" + keyword[i] + " freq:" + freq[i]);

		//	}

			//PrintWriter outfile = new PrintWriter(filename);
			ArrayList<String> keyword_print = new ArrayList<String>();
			for (int i = keyword.length - 1; i > 0; i--) {
				if (!keyword_print.contains(keyword[i])) {
					keyword_print.add(keyword[i]);
					//outfile.println(keyword[i]);
				}
			}
			//System.out.println("THE SIZE OF THE KEYWORDS ARRAY(initialization): "+keyword_print.size());
			//***************************************************************************** //GEOTAGGING PORTION
			//*****************************************************************************
			
			//	private boolean geoTag(String term){
			Pattern p = Pattern.compile("(\\-?\\d+(\\.\\d+))"); //<--check compatibility with mac: (\-?\d+(\.\d+))
			Matcher m;
			String term;
			boolean found = false;
			System.out.println("Searching doc for word matches...");
			for(int i = 0; i < keyword_print.size(); i++){
				//regex to find the latitude and longitude
				term = keyword_print.get(i);
				BufferedReader br;
				try {
					br = new BufferedReader(new FileReader("US.txt")); //<---Replace with US.txt for full version

					String line;
					ArrayList<String> coords = new ArrayList<String>();
					boolean looking = true;
					while ((line = br.readLine()) != null && looking) {
						// process the line.
						if(term.length() > 4 &&  line.contains(" "+term+" ")){
							m = p.matcher(line);
							//print the line the term occurs
							//System.out.println(line);
							while (m.find()) {
								coords.add(m.group());
							}
							//print the content of the pdf
							outfile.print("<field name = \"content\">");
							String pdf_content;
							for(int k = 0; k < keyword_print.size(); k++){
								pdf_content = keyword_print.get(k);
								outfile.print(pdf_content+" ");
							}
							System.out.println(" Term: "+term);
							outfile.print("</field>");
							//print the coordinates
							for(int k = 0; k < coords.size(); k++){
								//System.out.println(coords.get(k));
								outfile.println("");
								if(k%2 == 0) outfile.print("<field name=\"lat\">");
								else outfile.print("<field name=\"long\">");
								outfile.print(coords.get(k)+"</field>");
							}
							outfile.println("</doc>");
							//System.out.println("DONE WITH THIS LAT/LONG");
							found = true;
							looking = false;
							i = keyword_print.size(); // end the loop
							//outPut(term, coords.get(0), coords.get(1));
						}
					}

					br.close();

				} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			//return false;
			}
			if(!found){
				//print the content of the pdf
				outfile.print("<field name = \"content\">");
				String pdf_content;
				//System.out.println("THE SIZE OF THE KEYWORDS ARRAY: "+keyword_print.size());
				for(int k = 0; k < keyword.length; k++){
					pdf_content = keyword[k];
					outfile.print(pdf_content+" ");
				}
				outfile.print("</field>");
				outfile.println("");
				outfile.println("<field name=\"lat\">0.0</field>");
				outfile.println("<field name=\"long\">0.0</field>");
				outfile.println("</doc>");
			}
			System.out.println("Done with word matching!");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TikaException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Update the log file with search hit Appends a log entry with the system
	 * timestamp, keyword found, and filename of PDF file containing the keyword
	 * DO NOT MODIFY
	 */
	private void updatelog(String keyword, String filename) {
		timestamp.setTime(System.currentTimeMillis());
		logfile.println(timestamp + " -- \"" + keyword + "\" found in file \""
				+ filename + "\"");
		logfile.flush();
	}

	/**
	 * Filename filter that accepts only *.pdf DO NOT MODIFY
	 */
	static class PDFFilenameFilter implements FilenameFilter {
		private Pattern p = Pattern.compile(".*\\.pdf",
				Pattern.CASE_INSENSITIVE);

		public boolean accept(File dir, String name) {
			Matcher m = p.matcher(name);
			return m.matches();
		}
	}
}
