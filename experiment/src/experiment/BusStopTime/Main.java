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
	public static HashMap<Long, ArrayList<Long[]>> stopsTimes;

	public static void main(String[] args) throws ParseException {
		init(131);
		HashMap<Long, ArrayList<Long[]>> stopsTimes = readDatagrams(131);
		excessWaitingTime(stopsTimes);
	}

	public static void init(long lineId) {

		stopsQuery = DataSource.findAllStopsByLine(261, lineId);
		stops = new HashMap<>();
		stopsBuses = new HashMap<>();
		stopsTimes = new HashMap<>();

		for (int i = 0; i < stopsQuery.size(); i++) {
			if (!stops.containsKey(stopsQuery.get(i).getStopId())) {
				stops.put(stopsQuery.get(i).getStopId(), stopsQuery.get(i));
				stopsBuses.put(stopsQuery.get(i).getStopId(), new ArrayList<Datagram>());
				stopsTimes.put(stopsQuery.get(i).getStopId(), new ArrayList<Long[]>());
			}
		}
	}
	
	public static boolean isInSameStop(Datagram datagram, SITMStop stop) {

		double longitudeNum = datagram.getLongitude();
		double latitudeNum = datagram.getLatitude();

		boolean lng = (latitudeNum <= (stop.getDecimalLatitude() + 0.0006)) && (latitudeNum >= (stop.getDecimalLatitude() - 0.0006));
		boolean ltd = (longitudeNum <= (stop.getDecimalLongitude() + 0.0006)) && (longitudeNum >= (stop.getDecimalLongitude() - 0.0006));

		if (lng && ltd) {
			return true;
		} else {
			return false;
		}
	}

	public static HashMap<Long, ArrayList<Long[]>> readDatagrams(long lineId) throws ParseException {

		
		ArrayList<Datagram> datagrams = DataSource.findAllDatagrams(lineId);
		
		for (int n = 0; n < datagrams.size(); n++) {
			
			Datagram datagram = datagrams.get(n);
			long stopId = datagram.getStopId();
			
			if (stopsBuses.containsKey(stopId)) {
				analysisPerBus(datagram);
			}
		}

		return stopsTimes;
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

		boolean x = isInSameStop(datagram, stop);

		if (x) { // inside polygon
			
			if(stopId==502300)
				System.out.println("entra "+datagram.getBusId()+" "+datagram.getDatagramDate());
			
			if (!isin) {			
				stopsBuses.get(stopId).add(datagram);
				Long[] times = new Long[3];
				times[1] = dateFormat.parse(datagram.getDatagramDate()).getTime()/1000;
				stopsTimes.get(stopId).add(times);
			}
			
		} else if (!x && isin) { // outside polygon
			
			buses.remove(datagramIndex);
			
			if(stopId==502300)
				System.out.println("=====>sale "+datagram.getBusId()+" "+datagram.getDatagramDate());
			
			if(buses.isEmpty()) {
				
				int lastPosition = stopsTimes.get(stopId).size()-1;
				Long[] times = stopsTimes.get(stopId).get(lastPosition);
				times[0] = datagram.getBusId();
				times[2] = dateFormat.parse(datagram.getDatagramDate()).getTime()/1000;
				
				if(stopId==502300)
					System.out.println("==============> vacio "+datagram.getBusId()+"-"+datagram.getDatagramDate());
			}
				
		}
	}
	
	public static void excessWaitingTime(HashMap<Long, ArrayList<Long[]>> stopsTimes) {
		
		for (Map.Entry<Long, ArrayList<Long[]>> entry : stopsTimes.entrySet()) {

			long initialTime = 0;
			long lastTime = 0;
			
//			if( entry.getKey() == 502300) {
				
				System.out.println("----------> waitingTime " + entry.getKey());

				for (Long[] data : entry.getValue()) {

					if(data[1]!=null && data[2]!=null) {
//						System.out.println(data[0]+" "+data[1]+"->"+data[2]);
						if(initialTime == 0 && lastTime == 0) {
							initialTime = data[1];
							lastTime = data[2];
						}else {
							
							if(data[1] >= initialTime && data[1] <= lastTime) {
								lastTime = data[2];
							}else if(data[1] > lastTime){
								long waitingTime = (data[1]-lastTime);
//								System.out.println(data[0]+": "+waitingTime);
								System.out.println(waitingTime);
								initialTime = data[1];
								lastTime = data[2];
							}
						}
					}
						
				}
//			}
		}
	}
}
