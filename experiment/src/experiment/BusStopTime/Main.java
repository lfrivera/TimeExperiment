package experiment.BusStopTime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
//import java.util.Map.Entry;

import experiment.model.Datagram;

public class Main{
	
	public final static String DATAGRAMS_PATH = "data/datagrams.csv";
	public final static String LINESTOPS_PATH = "data/linestops.csv";
	
	public static void main(String[] args) {
		readDatagrams(131);
	}
	
	public static File getSourceFile(String path) {
		return new File(path);
	}
	
	@SuppressWarnings("resource")
	public static String firstBus(long lineId) {
		
		File sourceFile = getSourceFile(DATAGRAMS_PATH);
		BufferedReader br;
		String text = "";
		
		try {
			
			br = new BufferedReader(new FileReader(sourceFile));
			text = br.readLine();
			text = br.readLine();
			
			while(text!=null && !text.equals("")){
				
				String[] data = text.split(",");
				
				if(data[7].equals(lineId+"")) {
					return data[1];
				}
				text = br.readLine();
			}

			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static ArrayList<Long> stopSequenceBus(long lineId, String busId){

		ArrayList<Long> stops = new ArrayList<Long>();
		File sourceFile = getSourceFile(DATAGRAMS_PATH);
		BufferedReader br;
		String text = "";
		
		try {
			
			br = new BufferedReader(new FileReader(sourceFile));
			text = br.readLine();
			text = br.readLine();

			
			while(text!=null && !text.equals("")){
				
				String [] data = text.split(",");
				
				if(data[7].equals(lineId+"") && data[1].equals(busId)) {
					if(!stops.contains(Long.parseLong(data[2]))) {
						stops.add(Long.parseLong(data[2]));
					}
				}
				
				text = br.readLine();
			}

			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return stops;
	}
	
	public static ArrayList<Long> stopSequence(long lineId) {
		
		BufferedReader br;
		String text = "";
		File sourceFile = getSourceFile(LINESTOPS_PATH);
		ArrayList<Long> stops = new ArrayList<Long>();
		
		
		try {
			br = new BufferedReader(new FileReader(sourceFile));
			text = br.readLine();
			text = br.readLine();
			
			while (text!=null && !text.equals("")){
				String[] data = text.split(";");
				if(data[3].equals(lineId+"")) {
					if(!stops.contains(Long.parseLong(data[4]))) {
						stops.add(Long.parseLong(data[4]));
					}
				}
				
				text = br.readLine();
			}
			
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		return stops; 
	}
	
	public static ArrayList<Datagram> readDatagrams(long lineId){
		
		HashMap<Long, Datagram> hash = new HashMap<>(); 
		HashMap<Long, LinkedList<Datagram>> hash2 = new HashMap<>(); 
		File sourceFile = getSourceFile(DATAGRAMS_PATH);
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
					long odometer = Long.parseLong(data[3]);
					long longitude = Long.parseLong(data[4]);
					long latitude = Long.parseLong(data[5]);
					long taskId = Long.parseLong(data[6]);
					long tripId = Long.parseLong(data[8]);
					
					Datagram datagram = new Datagram(datagramData, busId, stopId, odometer, longitude, latitude, taskId, lineId, tripId);
					
					
					if(hash.containsKey(stopId)) {
						
						Datagram d = hash.get(stopId);
						
						if(d.getBusId()!=busId) {
							hash.put(stopId,datagram);
							hash2.get(stopId).add(datagram);
						}
						
						
					}else {
						
						hash.put(stopId,datagram);
						LinkedList<Datagram> list = new LinkedList<>();
						list.add(datagram);
						hash2.put(stopId, list);
						
					}
					
//					System.out.println(datagram);
				}
				
				text = br.readLine();
			}

			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		ArrayList<Long> stops = stopSequenceBus(lineId,firstBus(lineId));
		
		for (int i = 0; i < stops.size(); i++) {
			
			if(hash2.containsKey(stops.get(i))) {
				System.out.println("================> "+stops.get(i));
				for (Datagram data : hash2.get(stops.get(i))) {
					if(data.getBusId()==999)
						System.out.println(data);
				}
			}
			
		}
		return null;
	}
}
