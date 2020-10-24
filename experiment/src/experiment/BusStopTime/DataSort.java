package experiment.BusStopTime;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class DataSort {

	public final static String ORIGINAL = "data/30-APR-19.csv";
	public final static String SORTED = "data/30-APR-19-sorted.csv";
	
	public static void main(String[] args) {
		sortDatagrams();
	}

	public static void sortDatagrams() {

		File sourceFile = DataSource.getSourceFile(ORIGINAL);
		File sortFile = DataSource.getSourceFile(SORTED);
		ArrayList<String> datagrams = new ArrayList<>();
		BufferedReader br;
		BufferedWriter bw;
		String text = "";

		try {

			br = new BufferedReader(new FileReader(sourceFile));
			bw = new BufferedWriter(new FileWriter(sortFile));

			text = br.readLine();

			System.out.println("read datgrams");
			while (text != null && !text.equals("")) {
				
				String[] data = text.split(",");
				if(data.length>1) {
					datagrams.add(text);
				}
				
				text = br.readLine();
			}

			System.out.println("strat sort");
			sortArray(datagrams);
			
			System.out.println("start write");
			for (int i = 0; i < datagrams.size(); i++) {
				bw.write(datagrams.get(i) + "\n");
			}

			System.out.println("End sort");
			br.close();
			bw.close();

		} catch (Exception e) {
			System.out.println(text);
			e.printStackTrace();
		}

	}

	public static void sortArray(ArrayList<String> datagrams) {

		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yy HH.mm.ss");

		Collections.sort(datagrams, new Comparator<String>() {
			public int compare(String o1, String o2) {
				String data1 = o1.split(",")[10];
				String data2 = o2.split(",")[10];

				try {
					data1 = data1.substring(0, 3) + "04" + data1.substring(6, 18) + data1.substring(25, 28);
					Long date_1 = dateFormat.parse(data1).getTime() / 1000;
					data2 = data2.substring(0, 3) + "04" + data2.substring(6, 18) + data2.substring(25, 28);
					Long date_2 = dateFormat.parse(data2).getTime() / 1000;

					return date_1.compareTo(date_2);

				} catch (Exception e) {
					e.printStackTrace();
				}

				return 0;
			}
		});
	}
}
