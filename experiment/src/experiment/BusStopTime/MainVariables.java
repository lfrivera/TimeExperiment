package experiment.BusStopTime;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import experiment.model.Datagram;
import experiment.model.SITMStop;

public class MainVariables {
	
	public static HashMap<Long, SITMStop> stops; //HashMap with the stops
	public static HashMap<Long, ArrayList<Datagram>> stopsBuses; // HashMap with the array of buses in one stop

	public static HashMap<Long, ArrayList<Long[]>> stopsWaitingTimes; // Excess Waiting Time at Bus stop
	public static HashMap<Long, ArrayList<Long[]>> busesWaitingTimes; // Bus Stop Time 

	public static void main(String[] args) throws ParseException {
		init(131);
		readDatagrams(131,500300);
		postAnalysis(500300);
	}

	/*
	 * This method initialize the hash maps with the necessaries stop ids and arrays
	 */
	public static void init(long lineId) {

		ArrayList<SITMStop> stopsQuery = DataSource.findAllStopsByLine(261, lineId);
		stops = new HashMap<>();
		stopsBuses = new HashMap<>(); 

		stopsWaitingTimes = new HashMap<>();
		busesWaitingTimes = new HashMap<>();

		for (int i = 0; i < stopsQuery.size(); i++) {
			if (!stops.containsKey(stopsQuery.get(i).getStopId())) {
				stops.put(stopsQuery.get(i).getStopId(), stopsQuery.get(i));
				stopsBuses.put(stopsQuery.get(i).getStopId(), new ArrayList<Datagram>());

				stopsWaitingTimes.put(stopsQuery.get(i).getStopId(), new ArrayList<Long[]>());
				busesWaitingTimes.put(stopsQuery.get(i).getStopId(), new ArrayList<Long[]>());
			}
		}
	}
	
	/*
	 * This method read the datagrams file
	 */
	public static void readDatagrams(long lineId, long observerStop) throws ParseException {

		String path = "data/02-APR-19-sorted.csv";
		ArrayList<Datagram> datagrams = DataSource.readDatagrams3(lineId,path);

		for (int n = 0; n < datagrams.size(); n++) {

			Datagram datagram = datagrams.get(n);
			long stopId = datagram.getStopId();

			if (stops.containsKey(stopId)) {
				analysisPerBus(datagram, observerStop);
			}
		}
		
	}
	
	/*
	 * This method analyze one datagram
	 */
	public static void analysisPerBus(Datagram datagram, long observerStop) throws ParseException {

		long stopId = datagram.getStopId();
		ArrayList<Datagram> buses = stopsBuses.get(stopId);
		SITMStop stop = stops.get(stopId);

		boolean isInStation = false;
		int datagramIndex = -1;

		for (int i = 0; i < buses.size(); i++) {
			if (buses.get(i).getBusId() == datagram.getBusId()) {
				isInStation = true;
				datagramIndex = i;
				i = buses.size();
			}
		}

		boolean x = isInTheStop(datagram, stop);
		
		if (x) {// the bus is inside the polygon

//			if(stopId==observerStop)
//				System.out.println("inside "+datagram.getBusId()+" | "+datagram.getDatagramDate()+" | "+datagram.getOdometer()+" | "+datagram.getLatitude()+","+datagram.getLongitude());

			if (!isInStation) { // The bus arrive the stop
				stopsBuses.get(stopId).add(datagram);
				Long[] times = new Long[3];
				times[1] = datagram.getDatagramDateTime();
				stopsWaitingTimes.get(stopId).add(times);
				
			}
			
		} else if (!x && isInStation) { // The bus is outside the polygon
			
			if (!buses.isEmpty()) { // The stop isn't empty
				
//				if(stopId==observerStop)
//					System.out.println("=====> outside "+datagram.getBusId()+" | "+datagram.getDatagramDate()+" | "+datagram.getOdometer()+" | "+datagram.getLatitude()+","+datagram.getLongitude());
				
				Long[] times = new Long[4];
				times[0] = datagram.getBusId();//busId
				times[1] = buses.get(datagramIndex).getDatagramDateTime();//arrivalPolygon P
				times[2] = datagram.getDatagramDateTime();//leavePolygon Q
				times[3] = (times[1]+times[2])/2;//Time of arrival, open doors T
				busesWaitingTimes.get(stopId).add(times);
			}

			buses.remove(datagramIndex);

			if (buses.isEmpty()) {// The stop is empty

//				if(stopId==observerStop)
//					System.out.println("==============> Empty stop "+datagram.getBusId()+" | "+datagram.getDatagramDate()+" | "+datagram.getOdometer()+" | "+datagram.getLatitude()+","+datagram.getLongitude());

				int lastPosition = stopsWaitingTimes.get(stopId).size() - 1;
				Long[] times = stopsWaitingTimes.get(stopId).get(lastPosition);
				times[0] = datagram.getBusId();
				times[2] = datagram.getDatagramDateTime();
			}
		}
	}
	
	/*
	 * This method evaluates that the bus is inside the area of ​​the stop or station
	 */
	public static boolean isInTheStop(Datagram datagram, SITMStop stop) {

		double lat1 = stop.getDecimalLatitude();
		double lng1 = stop.getDecimalLongitude();
		double lat2 = datagram.getLatitude();
		double lng2 = datagram.getLongitude();

		double dLat = Math.toRadians(lat2 - lat1);
		double dLng = Math.toRadians(lng2 - lng1);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1))* Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

		return c <= 0.00003 ? true : false;
	}
	
	/*
	 * Post analysis, print the results in console 
	 */
	public static void postAnalysis(long observerStop) {
		order_Times(observerStop);
		time_Of_Polygon(observerStop);
		time_Of_Arrival(observerStop); 
		time_Of_service(observerStop);
		delay_In_Queue(observerStop);
		interarrival_Time_Between_Buses(observerStop);
		//excess_Waiting_Time_at_Bus_stop();
	}
	
	public static void order_Times(long observerStop) {
		
		for (Map.Entry<Long, ArrayList<Long[]>> entry : busesWaitingTimes.entrySet()) {

			if( entry.getKey() == observerStop) {
					
				Collections.sort(entry.getValue(), new Comparator<Long[]>() {
					public int compare(Long[] o1, Long[] o2) {
						return o1[3].compareTo(o2[3]);
					}
				});
				
			}
		}
	}
	
	public static void time_Of_Polygon(long observerStop) {
		
		System.out.println("---------------------------------------------------------------------------------");
		
		for (Map.Entry<Long, ArrayList<Long[]>> entry : busesWaitingTimes.entrySet()) {

			if( entry.getKey() == observerStop) {
				
				System.out.println("Stop Id " + entry.getKey());
				
				for (Long[] data : entry.getValue()) {
					long time = data[2]-data[1];
					System.out.print("arrive: "+data[1]);
					System.out.print(" leave: "+data[2]);
					System.out.println(" timeInside: "+time);
				}
				
				System.out.println();
			}
		}
	}
	
	public static void time_Of_Arrival(long observerStop) {
		
		System.out.println("---------------------------------------------------------------------------------");
		
		for (Map.Entry<Long, ArrayList<Long[]>> entry : busesWaitingTimes.entrySet()) {

			if( entry.getKey() == observerStop) {
				
				System.out.println("(Ti) Stop Id " + entry.getKey());
	
				for (Long[] data : entry.getValue()) {
					System.out.println(" Ti: "+data[3]);
				}
				
				System.out.println();
			}
		}
	}
	
	public static void time_Of_service(long observerStop) {
		
		System.out.println("---------------------------------------------------------------------------------");
		
		for (Map.Entry<Long, ArrayList<Long[]>> entry : busesWaitingTimes.entrySet()) {

			if( entry.getKey() == observerStop) {
				
				System.out.println("(Si) Stop Id " + entry.getKey());
	
				for (int i = 0; i < entry.getValue().size()-1; i++) {
					Long[] dataPrev = entry.getValue().get(i);
					Long[] data = entry.getValue().get(i+1);
					long Si = 0;
					
					if(dataPrev[2]>data[3]) {
						Si = data[2]-dataPrev[2];
					}else {
						Si = data[2]-data[3];
					}
					
//					System.out.println(" Si: "+Si);
					System.out.println(new Date(data[3]*1000).toString()+","+Si);
				}
				
				System.out.println();
			}
		}
	}
	
	public static void delay_In_Queue(long observerStop) {
	
		System.out.println("---------------------------------------------------------------------------------");
		
		for (Map.Entry<Long, ArrayList<Long[]>> entry : busesWaitingTimes.entrySet()) {

			if( entry.getKey() == observerStop) {
				
				System.out.println("(Di) Stop Id " + entry.getKey());
	
				for (int i = 0; i < entry.getValue().size()-1; i++) {
					Long[] dataPrev = entry.getValue().get(i);
					Long[] data = entry.getValue().get(i+1);
					long Di = 0;
					
					if(dataPrev[2]>data[3]) {
						Di = dataPrev[2]-data[3];
					}
					
					System.out.println(" Di: "+Di);
//					System.out.println(new Date(data[3]*1000).toString()+","+Di);
				}
				
				System.out.println();
			}
		}
	}
	
	public static void interarrival_Time_Between_Buses(long observerStop) {
		
		System.out.println("---------------------------------------------------------------------------------");
		
		for (Map.Entry<Long, ArrayList<Long[]>> entry : busesWaitingTimes.entrySet()) {

			if( entry.getKey() == observerStop) {
				
				System.out.println("(Ai) Stop Id " + entry.getKey());
	
				for (int i = 0; i < entry.getValue().size()-1; i++) {
					Long[] dataPrev = entry.getValue().get(i);
					Long[] data = entry.getValue().get(i+1);
					long Ai = data[3]-dataPrev[3];
//					System.out.println(" Ai: "+Ai);
					System.out.println(new Date(dataPrev[3]*1000).toString()+","+Ai);
				}
				
				System.out.println();
			}
			
		}
	}
	
	public static void excess_Waiting_Time_at_Bus_stop() {
		
		System.out.println("---------------------------------------------------------------------------------");
		
		for (Map.Entry<Long, ArrayList<Long[]>> entry : stopsWaitingTimes.entrySet()) {

			long initialTime = 0;
			long lastTime = 0;

			System.out.println("WaitingTime " + entry.getKey());

			for (Long[] data : entry.getValue()) {

				if (data[1] != null && data[2] != null) {

					if (initialTime == 0 && lastTime == 0) {

						initialTime = data[1];
						lastTime = data[2];

					} else if(data[1] > lastTime) {

						long waitingTime = (data[1] - lastTime);
						System.out.println(waitingTime);
						initialTime = data[1];
						lastTime = data[2];
						
					}
				}
			}
			
			System.out.println();
		}
	}
}
