package jsp;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.odftoolkit.odfdom.doc.OdfTextDocument;

public class ProblemCreator {
	public ProblemCreator(){}
	
	public static Problem create(String filename) throws IOException{
//		TextDocument document = TextDocument.loadDocument("test.odt");
//		FileReader f = new FileReader(new File(filename));
		
		
		System.out.println(text);
//		BufferedReader reader = new BufferedReader(buff);
		int machines;
		int jobs;
//		String line = reader.readLine();
		String[] data = reader.readLine().split(" ");
		System.out.println(data[1]);
		machines = Integer.parseInt(data[0]);
		jobs = Integer.parseInt(data[1]);
		int[][] values = new int[jobs][machines*2];
		for (int j = 0; j < jobs; j++) {
			data = reader.readLine().split(" ");
			for (int i = 0; i < data.length; i++) {
				values[j][i] = Integer.parseInt(data[i]);
			}
		}
		return new Problem(machines, jobs, values);
	}
	
	public static class Problem{
		
		private int machines;
		private int jobs;
		public Problem(int machines, int jobs, int[][] values){
			this.machines = machines;
			this.jobs = jobs;
		}
	}
	
	public static void main(String[] args) throws Exception {
//		ProblemCreator.create("problem1.txt");
		OdfTextDocument od = OdfTextDocument.loadDocument("1.odt");
		BufferedInputStream buff = new BufferedInputStream(od.getContentStream());
		System.out.println(od.getContentRoot().getFirstChild().getTextContent());
		System.out.println(od.getContentRoot().getFirstChild().getTextContent());

		String texts = od.getContentRoot().getTextContent();
		System.out.println(texts);
//		String text = IOUtils.toString(buff);
//		System.out.println(text);

	}
}
