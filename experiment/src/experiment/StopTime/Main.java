package experiment.StopTime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Queue;

import experiment.model.Datagram;

public class Main{
	
	public final static String DATAGRAMS_PATH = "datagrams/datagrams.csv";
	
	public static void main(String[] args) {
		readDatagrams(131);
	}
	
	public static File getSourceFile() {
		return new File(DATAGRAMS_PATH);
	}
	
	public static ArrayList<Datagram> readDatagrams(long lineId){
		
		ArrayList<Datagram> operationaTravels = new ArrayList<Datagram>();
		HashMap<Long, Queue<String>> hash = new HashMap<>(); 
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
					long busId = Long.parseLong(data[1]);
					long stopId = Long.parseLong(data[2]);
					long odometer = Long.parseLong(data[3]);;
					long longitude = Long.parseLong(data[4]);;
					long latitude = Long.parseLong(data[5]);;
					long taskId = Long.parseLong(data[6]);;
					long tripId = Long.parseLong(data[7]);;
					
					Datagram datagram = new Datagram(datagramData, busId, stopId, odometer, longitude, latitude, taskId, lineId, tripId);
					operationaTravels.add(datagram);
					
					if(hash.containsKey(stopId)) {
						hash.get(stopId).add(datagramData+"-"+busId);
					}else {
						LinkedList<String> list = new LinkedList<>();
						list.add(datagramData+"-"+busId);
						hash.put(stopId,list);
					}
					
//					System.out.println(datagram);
				}
				
				text = br.readLine();
			}

			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (Entry<Long, Queue<String>> entry : hash.entrySet()) {
			System.out.println("================> "+entry.getKey());
		    for (String data : entry.getValue()) {
				System.out.println(data);
			}
		}
		return null;
	}
}
