package experiment.BusStopTime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import experiment.model.Datagram;
import experiment.model.SITMStop;

public class Main {

	public final static String DATAGRAMS_PATH = "data/datagrams.csv";
	public final static String LINESTOPS_PATH = "data/linestops.csv";

	public static void main(String[] args) {
		HashMap<Long, ArrayList<Long[]>> stopsTimes = readDatagrams(131);
		excessWaitingTime(stopsTimes);
	}

	public static boolean isInSameStop(ArrayList<Datagram> datagrams, Datagram datagram, SITMStop stop) {

		double longitudeNum = datagram.getLongitude() / 10000000;
		double latitudeNum = datagram.getLatitude() / 10000000;

		boolean lng = (latitudeNum <= (stop.getDecimalLatitude() + 0.005)) && (latitudeNum >= (stop.getDecimalLatitude() - 0.005));
		boolean ltd = (longitudeNum <= (stop.getDecimalLongitude() + 0.005)) && (longitudeNum >= (stop.getDecimalLongitude() - 0.005));

		if (lng && ltd) {
			return true;
		} else {
			return false;
		}
	}

	public static HashMap<Long, ArrayList<Long[]>> readDatagrams(long lineId) {

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		ArrayList<SITMStop> stops = DataSource.findAllStopsByLine(261, lineId);
		HashMap<Long, SITMStop> stopsLngLat = new HashMap<>();
		HashMap<Long, ArrayList<Datagram>> stopsBuses = new HashMap<>();
		HashMap<Long, ArrayList<Long[]>> stopsTimes = new HashMap<>();

		for (int i = 0; i < stops.size(); i++) {
			if (!stopsLngLat.containsKey(stops.get(i).getStopId())) {
				stopsLngLat.put(stops.get(i).getStopId(), stops.get(i));
				stopsBuses.put(stops.get(i).getStopId(), new ArrayList<Datagram>());
				stopsTimes.put(stops.get(i).getStopId(), new ArrayList<Long[]>());
			}
		}

		File sourceFile = DataSource.getSourceFile(DATAGRAMS_PATH);
		BufferedReader br;
		String text = "";

		try {

			br = new BufferedReader(new FileReader(sourceFile));
			text = br.readLine();
			text = br.readLine();

			while (text != null && !text.equals("")) {

				String[] data = text.split(",");

				if (data[7].equals(lineId + "")) {

					String datagramData = data[0];
					long busId = Long.parseLong(data[1]);
					long stopId = Long.parseLong(data[2]);
					long odometer = Long.parseLong(data[3]);
					double longitude = Long.parseLong(data[4]);
					double latitude = Long.parseLong(data[5]);
					long taskId = Long.parseLong(data[6]);
					long tripId = Long.parseLong(data[8]);

					Datagram datagram = new Datagram(datagramData, busId, stopId, odometer, longitude, latitude, taskId,
							lineId, tripId);

					if (stopsBuses.containsKey(stopId)) {

						ArrayList<Datagram> datagrams = stopsBuses.get(stopId);
						SITMStop stop = stopsLngLat.get(stopId);

						boolean isin = false;
						int datagramIndex = 0;

						for (int i = 0; i < datagrams.size(); i++) {
							if (datagrams.get(i).getBusId() == datagram.getBusId()) {
								isin = true;
								datagramIndex = i;
								i = datagrams.size();
							}
						}

						boolean x = isInSameStop(datagrams, datagram, stop);

						if (x) {
							if (!isin) {
								stopsBuses.get(stopId).add(datagram);
							}
						} else if (!x && isin) {

							Long[] times = new Long[3];
							times[0] = datagram.getBusId();
							times[1] = dateFormat.parse(datagrams.get(datagramIndex).getDatagramData()).getTime();
							times[2] = dateFormat.parse(datagram.getDatagramData()).getTime();
							;
							datagrams.remove(datagramIndex);
							stopsTimes.get(stopId).add(times);
						}

					}
				}

				text = br.readLine();
			}

			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return stopsTimes;
	}
	
	public static void excessWaitingTime(HashMap<Long, ArrayList<Long[]>> stopsTimes) {
		ArrayList<SITMStop> stops = DataSource.findAllStopsByLine(261, 131);
		
		for (int i = 0; i < stops.size(); i++) {

			long initialTime = 0;
			long lastTime = 0;
			
			if(stopsTimes.containsKey(stops.get(i).getStopId())) {
				System.out.println("================> "+stops.get(i).getLongName());
				
				for (Long[] data : stopsTimes.get(stops.get(i).getStopId())) {
	//				if(data.getBusId()==308)
					if(initialTime == 0 && lastTime == 0) {
						initialTime = data[1];
						lastTime = data[2];
					}else {
						if(data[1] >= initialTime && data[1] <= lastTime) {
							lastTime = data[2];
						}else if(data[1] > lastTime){
							System.out.println("actualbus "+data[1]+" fecha inicial anterior "+initialTime+" fechafinal del anterior "+lastTime);
							long waitingTime = (data[1]-lastTime)/1000;
							System.out.println(data[0]+": "+waitingTime);
							initialTime = data[1];
							lastTime = data[2];
						}
					}
						
				}
			}
		}
	}
}
