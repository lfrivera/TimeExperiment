package experiment.BusStopTime;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import experiment.model.Datagram;
import experiment.model.SITMStop;

public class Main {

	public final static String DATAGRAMS_PATH = "data/datagrams.csv";
	public final static String LINESTOPS_PATH = "data/linestops.csv";

	public static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	public static ArrayList<SITMStop> stopsQuery;
	public static HashMap<Long, SITMStop> stops;
	public static HashMap<Long, ArrayList<Datagram>> stopsBuses;

	public static HashMap<Long, ArrayList<Long[]>> stopsWaitingTimes;
	public static HashMap<Long, ArrayList<Long[]>> busesWaitingTimes;

	public static void main(String[] args) throws ParseException {
		init(131);
		readDatagrams(131);
		excessWaitingTime();
	}

	public static void init(long lineId) {

		stopsQuery = DataSource.findAllStopsByLine(261, lineId);
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

	public static void readDatagrams(long lineId) throws ParseException {

		ArrayList<Datagram> datagrams = DataSource.findAllDatagrams(lineId);

		for (int n = 0; n < datagrams.size(); n++) {

			Datagram datagram = datagrams.get(n);
			long stopId = datagram.getStopId();

			if (stopsBuses.containsKey(stopId)) {
				analysisPerBus(datagram);
			}
		}
		
	}

	public static void analysisPerBus(Datagram datagram) throws ParseException {

		long stopId = datagram.getStopId();
		ArrayList<Datagram> buses = stopsBuses.get(stopId);
		SITMStop stop = stops.get(stopId);

		boolean isin = false;
		int datagramIndex = -1;

		for (int i = 0; i < buses.size(); i++) {
			if (buses.get(i).getBusId() == datagram.getBusId()) {
				isin = true;
				datagramIndex = i;
				i = buses.size();
			}
		}

		boolean x = isInTheStop(datagram, stop);

		
		if (x) {// the bus is inside the polygon

//			if(stopId==502300)
//				System.out.println("inside "+datagram.getBusId()+" "+datagram.getDatagramDate());

			if (!isin) {
				stopsBuses.get(stopId).add(datagram);
				Long[] times = new Long[3];
				times[1] = dateFormat.parse(datagram.getDatagramDate()).getTime() / 1000;
				stopsWaitingTimes.get(stopId).add(times);
			}
		} else if (!x && isin) {// the bus is outside the polygon

//			if(stopId==502300)
//				System.out.println("=====> outside "+datagram.getBusId()+" "+datagram.getDatagramDate());

			if (!buses.isEmpty()) {
				Long[] times = new Long[3];
				times[0] = datagram.getBusId();
				times[1] = dateFormat.parse(buses.get(datagramIndex).getDatagramDate()).getTime()/ 1000;
				times[2] = dateFormat.parse(datagram.getDatagramDate()).getTime() / 1000;
				busesWaitingTimes.get(stopId).add(times);
			}

			buses.remove(datagramIndex);

			if (buses.isEmpty()) {

//				if(stopId==502300)
//					System.out.println("==============> Empty stop "+datagram.getBusId()+"-"+datagram.getDatagramDate());

				int lastPosition = stopsWaitingTimes.get(stopId).size() - 1;
				Long[] times = stopsWaitingTimes.get(stopId).get(lastPosition);
				times[0] = datagram.getBusId();
				times[2] = dateFormat.parse(datagram.getDatagramDate()).getTime() / 1000;
			}

		}
	}

	public static void excessWaitingTime() {

		for (Map.Entry<Long, ArrayList<Long[]>> entry : busesWaitingTimes.entrySet()) {

			System.out.println("WaitingTime " + entry.getKey());

			for (Long[] data : entry.getValue()) {

				if (data[1] != null && data[2] != null) {
					System.out.println(data[0]+": "+data[1]+"-->"+data[2]);
				}
			}
			System.out.println();
		}
		
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
