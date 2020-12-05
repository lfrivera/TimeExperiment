package experiment.BusStopTime;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

import experiment.model.Datagram;
import experiment.model.SITMStop;

public class MainDatagrams {
	
	public static HashMap<Long, SITMStop> stops; //HashMap with the stops
	

	public static void main(String[] args) throws ParseException {
		init(131);
		readDatagrams(131,500300);
	}

	/*
	 * This method initialize the hash maps with the necessaries stop ids and arrays
	 */
	public static void init(long lineId) {

		ArrayList<SITMStop> stopsQuery = DataSource.findAllStopsByLine(261, lineId);
		stops = new HashMap<>();

		for (int i = 0; i < stopsQuery.size(); i++) {
			if (!stops.containsKey(stopsQuery.get(i).getStopId())) {
				stops.put(stopsQuery.get(i).getStopId(), stopsQuery.get(i));
			}
		}
	}
	
	/*
	 * This method read the datagrams file
	 */
	public static void readDatagrams(long lineId, long observerStop) throws ParseException {

		String path = "data/30-APR-19-sorted.csv";
		ArrayList<Datagram> datagrams = DataSource.readDatagrams3(lineId,path);

		for (int n = 0; n < datagrams.size(); n++) {

			Datagram datagram = datagrams.get(n);
			long stopId = datagram.getStopId();

			if (stops.containsKey(stopId)) {
				analysis(datagram);
			}
		}
	}
	
	/*
	 * This method analyze one datagram
	 */
	public static void analysis(Datagram datagram) throws ParseException {

		long stopId = datagram.getStopId();
		SITMStop stop = stops.get(stopId);
		boolean x = isInTheStop(datagram, stop);

		
		System.out.println(stop.getStopId()+" "+stop.getLongName());
		
		if (x) {// the bus is inside the polygon

			
		} else if (!x) { // The bus is outside the polygon
			

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
	
}
