package experiment.BusStopTime;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import experiment.model.Datagram;
import experiment.model.SITMStop;

public class Main {
	
	public static HashMap<Long, SITMStop> stops; //HashMap with the stops
	public static HashMap<Long, ArrayList<Datagram>> stopsBuses; // HashMap with the array of buses in one stop

	public static HashMap<Long, ArrayList<Long[]>> stopsWaitingTimes; // Excess Waiting Time at Bus stop
	public static HashMap<Long, ArrayList<Long[]>> busesWaitingTimes; // Bus Stop Time 

	public static void main(String[] args) throws ParseException {
		init(131);
		readDatagrams(131);
		postAnalysis();
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
	public static void readDatagrams(long lineId) throws ParseException {

		ArrayList<Datagram> datagrams = DataSource.readDatagrams2(lineId);

		for (int n = 0; n < datagrams.size(); n++) {

			Datagram datagram = datagrams.get(n);
			long stopId = datagram.getStopId();

			if (stopsBuses.containsKey(stopId)) {
				analysisPerBus(datagram);
			}
		}
		
	}
	
	/*
	 * This method analyze one datagram
	 */
	public static void analysisPerBus(Datagram datagram) throws ParseException {

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

//			if(stopId==502300)
//				System.out.println("inside "+datagram.getBusId()+" | "+datagram.getDatagramDate()+" | "+datagram.getOdometer()+" | "+datagram.getLatitude()+","+datagram.getLongitude());

			if (!isInStation) { // The bus arrive the stop
				stopsBuses.get(stopId).add(datagram);
				Long[] times = new Long[3];
				times[1] = datagram.getDatagramDateTime();
				stopsWaitingTimes.get(stopId).add(times);
				
			}
			
		} else if (!x && isInStation) { // The bus is outside the polygon

			if (!buses.isEmpty()) { // The stop isn't empty
				
//				if(stopId==502300)
//					System.out.println("=====> outside "+datagram.getBusId()+" | "+datagram.getDatagramDate()+" | "+datagram.getOdometer()+" | "+datagram.getLatitude()+","+datagram.getLongitude());
				
				Long[] times = new Long[4];
				times[0] = datagram.getBusId();//busId
				times[1] = buses.get(datagramIndex).getDatagramDateTime();//arrivalPolygon
				times[2] = datagram.getDatagramDateTime();//leavePolygon
				times[3] = (times[1]+times[2])/2;//Time of arrival, open doors
				busesWaitingTimes.get(stopId).add(times);
			}

			buses.remove(datagramIndex);

			if (buses.isEmpty()) {// The stop is empty

//				if(stopId==502300)
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
	public static void postAnalysis() {
		order_Times();
		time_Of_Polygon();
		time_Of_Arrival(); 
		time_Of_service();
		delay_In_Queue();
		interarrival_Time_Between_Buses();
		//excess_Waiting_Time_at_Bus_stop();
	}
	
	public static void order_Times() {
		
		for (Map.Entry<Long, ArrayList<Long[]>> entry : busesWaitingTimes.entrySet()) {

			if( entry.getKey() == 500601) {
					
				Collections.sort(entry.getValue(), new Comparator<Long[]>() {
					public int compare(Long[] o1, Long[] o2) {
						return o1[3].compareTo(o2[3]);
					}
				});
				
			}
		}
	}
	
	public static void time_Of_Polygon() {
		
		System.out.println("---------------------------------------------------------------------------------");
		
		for (Map.Entry<Long, ArrayList<Long[]>> entry : busesWaitingTimes.entrySet()) {

			if( entry.getKey() == 500601) {
				
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
	
	public static void time_Of_Arrival() {
		
		System.out.println("---------------------------------------------------------------------------------");
		
		for (Map.Entry<Long, ArrayList<Long[]>> entry : busesWaitingTimes.entrySet()) {

			if( entry.getKey() == 500601) {
				
				System.out.println("Stop Id " + entry.getKey());
	
				for (Long[] data : entry.getValue()) {
					System.out.println(" Ti: "+data[3]);
				}
				
				System.out.println();
			}
		}
	}
	
	public static void time_Of_service() {
		
		System.out.println("---------------------------------------------------------------------------------");
		
		for (Map.Entry<Long, ArrayList<Long[]>> entry : busesWaitingTimes.entrySet()) {

			if( entry.getKey() == 500601) {
				
				System.out.println("Stop Id " + entry.getKey());
	
				for (Long[] data : entry.getValue()) {
					long si = data[2]-data[3];
					System.out.println(" Si: "+si);
				}
				
				System.out.println();
			}
		}
	}
	
	public static void delay_In_Queue() {
	
		System.out.println("---------------------------------------------------------------------------------");
		
		for (Map.Entry<Long, ArrayList<Long[]>> entry : busesWaitingTimes.entrySet()) {

			if( entry.getKey() == 500601) {
				
				System.out.println("Stop Id " + entry.getKey());
	
				for (Long[] data : entry.getValue()) {
					long di = data[3]-data[1];
					System.out.println(" Di: "+di);
				}
				
				System.out.println();
			}
		}
	}
	
	public static void interarrival_Time_Between_Buses() {
		
		System.out.println("---------------------------------------------------------------------------------");
		
		for (Map.Entry<Long, ArrayList<Long[]>> entry : busesWaitingTimes.entrySet()) {

			if( entry.getKey() == 500601) {
				
				System.out.println("Stop Id " + entry.getKey());
	
				for (int i = 0; i < entry.getValue().size()-1; i++) {
					Long[] data = entry.getValue().get(i);
					Long[] dataNext = entry.getValue().get(i+1);
					long Ai = dataNext[3]-data[3];
					System.out.println(" Ai: "+Ai);
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
