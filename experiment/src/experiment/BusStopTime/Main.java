package experiment.BusStopTime;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import experiment.model.Datagram;
import experiment.model.SITMStop;

public class Main {
	
	public static HashMap<Long, SITMStop> stops; //HashMap with the stops
	public static HashMap<Long, ArrayList<Datagram>> stopsBuses; // HashMap with the array of buses in one stop

	public static HashMap<Long, ArrayList<Long[]>> stopsWaitingTimes; // Excess Waiting Time at Bus stop
	public static HashMap<Long, ArrayList<Long[]>> busesWaitingTimes; // Bus Stop Time 
	public static HashMap<Long, ArrayList<Long>> arrivalTime; // Bus Stop Time 

	public static void main(String[] args) throws ParseException {
		init(131);
		readDatagrams(131);
		//arrivalTime();
		excessWaitingTime();
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
		arrivalTime = new HashMap<>();

		for (int i = 0; i < stopsQuery.size(); i++) {
			if (!stops.containsKey(stopsQuery.get(i).getStopId())) {
				stops.put(stopsQuery.get(i).getStopId(), stopsQuery.get(i));
				stopsBuses.put(stopsQuery.get(i).getStopId(), new ArrayList<Datagram>());

				stopsWaitingTimes.put(stopsQuery.get(i).getStopId(), new ArrayList<Long[]>());
				busesWaitingTimes.put(stopsQuery.get(i).getStopId(), new ArrayList<Long[]>());
				arrivalTime.put(stopsQuery.get(i).getStopId(), new ArrayList<Long>());
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

			if(stopId==502300)
				System.out.println("inside "+datagram.getBusId()+" | "+datagram.getDatagramDate()+" | "+datagram.getLatitude()+","+datagram.getLongitude());

			if (!isInStation) { // The bus arrive the stop
				stopsBuses.get(stopId).add(datagram);
				Long[] times = new Long[3];
				times[1] = datagram.getDatagramDateTime();
				stopsWaitingTimes.get(stopId).add(times);
				//arrivalTime.get(stopId).add(datagram.getDatagramDateTime());
				
			}
			
		} else if (!x && isInStation) { // The bus is outside the polygon

			if (!buses.isEmpty()) { // The stop isn't empty
				
				if(stopId==502300)
					System.out.println("=====> outside "+datagram.getBusId()+" | "+datagram.getDatagramDate()+" | "+datagram.getLatitude()+","+datagram.getLongitude());
				
				Long[] times = new Long[3];
				times[0] = datagram.getBusId();
				times[1] = buses.get(datagramIndex).getDatagramDateTime();
				times[2] = datagram.getDatagramDateTime();
				busesWaitingTimes.get(stopId).add(times);
			}

			buses.remove(datagramIndex);

			if (buses.isEmpty()) {// The stop is empty

				if(stopId==502300)
					System.out.println("==============> Empty stop "+datagram.getBusId()+" | "+datagram.getDatagramDate()+" | "+datagram.getLatitude()+","+datagram.getLongitude());

				int lastPosition = stopsWaitingTimes.get(stopId).size() - 1;
				Long[] times = stopsWaitingTimes.get(stopId).get(lastPosition);
				times[0] = datagram.getBusId();
				times[2] = datagram.getDatagramDateTime();
			}

		}
	}

	@SuppressWarnings("deprecation")
	public static void arrivalTime() {
		for (Map.Entry<Long, ArrayList<Long>> entry : arrivalTime.entrySet()) {
			System.out.println("arrivalTime ==>" + entry.getKey());
			
			ArrayList<Long> times = entry.getValue();
			
			for (int i = 0; i < times.size()-1; i++) {
				Date date = new Date(times.get(i)*1000);
				System.out.println(date.getHours()+":"+date.getMinutes()+","+(times.get(i+1)-times.get(i)));
			}
		}
	}
	
	/*
	 * Post analysis, print the results in console 
	 */
	public static void excessWaitingTime() {

		// Time inside the polygon
		for (Map.Entry<Long, ArrayList<Long[]>> entry : busesWaitingTimes.entrySet()) {

			System.out.println("Stop Id " + entry.getKey());

			for (Long[] data : entry.getValue()) {

				if (data[1] != null && data[2] != null) {
					long time = data[2]-data[1];
					long mean_value = (data[1]+data[2])/2;
					//System.out.print("busId "+data[0]);
					//System.out.print(" arrive: "+data[1]);
					//System.out.print(" leave: "+data[2]);
					System.out.print(" Ti: "+mean_value);
					System.out.println(" timeInside: "+time);
				}
			}
			System.out.println();
		}
		
		System.out.println("---------------------------------------------------------------------------------");
		
		// Excess Waiting Time at Bus stop
		/*
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
		*/
	}
}
