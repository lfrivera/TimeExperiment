package experiment.StopTime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import experiment.model.Datagram;

public class Main{
	
	public final static String DATAGRAMS_PATH = "datagrams/datagrams.csv";
	
	public File getSourceFile() {
		return new File(DATAGRAMS_PATH);
	}
	
	public ArrayList<Datagram> readDatagrams(long lineId){
		
		ArrayList<Datagram> operationaTravels = new ArrayList<Datagram>();
		File sourceFile = getSourceFile();
		BufferedReader br;
		String text = "";
		
		try {
			
			br = new BufferedReader(new FileReader(sourceFile));
			text = br.readLine();
			text = br.readLine();
			
			while(text!=null && !text.equals("")){
				
				String[] data = text.split(",");
				
				if(data[7].equals(lineId+"")) {
					
					String datagramData = data[0];
					long busId = 0;
					long stopId = 0;
					long odometer = 0;
					long longitude = 0;
					long latitude = 0;
					long taskId = 0;
					long tripId = 0;
					
					Datagram datagram = new Datagram(datagramData, busId, stopId, odometer, longitude, latitude, taskId, lineId, tripId);
					operationaTravels.add(datagram);
				}
				
				
				text = br.readLine();
			}

			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
