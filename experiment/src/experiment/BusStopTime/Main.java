package experiment.BusStopTime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
//import java.util.Map.Entry;
import java.util.Locale;

import experiment.model.Datagram;
import experiment.model.SITMLineStop;
import experiment.model.SITMStop;

public class Main{
	
	public final static String DATAGRAMS_PATH = "data/datagrams.csv";
	public final static String LINESTOPS_PATH = "data/linestops.csv";
	
	public static void main(String[] args) {
		readDatagrams(131);
	}
	
	public static File getSourceFile(String path) {
		return new File(path);
	}
	
	public static ArrayList<SITMLineStop> findAllLineStopByPlanVersion(long planVersionId) {

		String path = new File("data/linestops.csv").getAbsolutePath();
		ArrayList<SITMLineStop> lineStops = new ArrayList<>();

		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(path));
			String[] columns = null;
			String line = br.readLine();
			line = br.readLine();

			while (line != null) {
				columns = line.split(";");

				if (!columns[0].isEmpty() && columns[5].equals(planVersionId + "")) {
					
					long lineStopid = Long.parseLong(columns[0]);
					long stopsequence = Long.parseLong(columns[1]);
					long orientation = Long.parseLong(columns[2]);
					long lineid = Long.parseLong(columns[3]);
					long stopid = Long.parseLong(columns[4]);
					long planVersionid = Long.parseLong(columns[5]);
					long lineVariant = Long.parseLong(columns[6]);
					DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yy", Locale.US);
					Date registerDate = new Date(dateFormat.parse(columns[7]).getTime());
					long lineVariantType = Long.parseLong(columns[8]);
					
					lineStops.add(new SITMLineStop(lineStopid, stopsequence, orientation, lineid, stopid,planVersionid, lineVariant, registerDate, lineVariantType));
				}
				
				line = br.readLine();
			}
			
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lineStops;
	}
	
	public static ArrayList<SITMStop> findAllStopsByPlanVersion(long planVersionId) {

		ArrayList<SITMStop> stops = new ArrayList<>();
		String path = new File("data/stops.csv").getAbsolutePath();

		BufferedReader br;

		try {
			br = new BufferedReader(new FileReader(path));
			String[] columns = null;
			String line = br.readLine();
			line = br.readLine();

			while (line != null) {
				columns = line.split(";");

				if (!columns[0].isEmpty() && columns[1].equals(planVersionId + "")
						&& !columns[6].contains("#") && !columns[6].equals("0")
						&& !columns[7].contains("#") && !columns[7].equals("0")) {

					String longName = columns[3];
					String shortName = columns[2];
					long stopId = Long.parseLong(columns[0]);

					double gPSX = 0;
					double gPSY = 0;
					double decimalLongitude = 0;
					double decimalLactitude = 0;

					if (!columns[4].isEmpty()) {
						gPSX = Double.parseDouble(columns[4]) / 10000000;
					}
					if (!columns[5].isEmpty()) {
						gPSY = Double.parseDouble(columns[5]) / 10000000;
					}
					if (!columns[6].isEmpty()) {
						String origi = columns[6].replace(".", "");
						StringBuffer str = new StringBuffer(origi);
						str.insert(3, ".");
						decimalLongitude = Double.parseDouble(str.toString());
					}
					if (!columns[7].isEmpty()) {
						String origi = columns[7].replace(".", "");
						StringBuffer str = new StringBuffer(origi);
						str.insert(1, ".");
						decimalLactitude = Double.parseDouble(str.toString());
					}

					stops.add(new SITMStop(stopId, shortName, longName, gPSX, gPSY, decimalLongitude,decimalLactitude, planVersionId));
				}

				line = br.readLine();
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return stops;
	}
	
	public static ArrayList<SITMStop> findAllStopsByLine(long planVersionId, long lineId) {

		ArrayList<SITMStop> stopsByLine = new ArrayList<>();
		ArrayList<SITMStop> stops = findAllStopsByPlanVersion(planVersionId);
		ArrayList<SITMLineStop> lineStops = findAllLineStopByPlanVersion(planVersionId);

		for (int i = 0; i < lineStops.size(); i++) {
			SITMLineStop lineStop = (SITMLineStop) lineStops.get(i);

			if (lineStop.getLineId() == lineId) {

				for (int j = 0; j < stops.size(); j++) {
					SITMStop stop = (SITMStop) stops.get(j);
					if (stop.getStopId() == lineStop.getStopId()) {
						stopsByLine.add(stop);
					}
				}
			}
		}

		return stopsByLine;
	}
	
	public static boolean isInSameStop(ArrayList<Datagram> datagrams, Datagram datagram, SITMStop stop) {
		
		double longitudeNum = datagram.getLongitude() / 10000000;
		double latitudeNum = datagram.getLatitude()  / 10000000;
		
		boolean lng = (latitudeNum <= (stop.getDecimalLatitude()+0.005)) && (latitudeNum >= (stop.getDecimalLatitude()-0.005));
		boolean ltd = (longitudeNum <= (stop.getDecimalLongitude()+0.005)) && (longitudeNum >= (stop.getDecimalLongitude()-0.005));
			
		if(lng && ltd ) {
				return true;
		}else {
			return false;
		}
	}
	
	public static ArrayList<Datagram> readDatagrams(long lineId){
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		ArrayList<SITMStop> stops = findAllStopsByLine(261,lineId);
		HashMap<Long, SITMStop> stopsLngLat = new HashMap<>(); 
		HashMap<Long, ArrayList<Datagram>> stopsBuses = new HashMap<>(); 
		HashMap<Long, ArrayList<Long[]>> stopsTimes = new HashMap<>(); 
		
		for (int i = 0; i < stops.size(); i++) {
			if(!stopsLngLat.containsKey(stops.get(i).getStopId())) {
				stopsLngLat.put(stops.get(i).getStopId(), stops.get(i));
				stopsBuses.put(stops.get(i).getStopId(), new ArrayList<Datagram>());
				stopsTimes.put(stops.get(i).getStopId(), new ArrayList<Long[]>());
			}	
		}
		
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
					double longitude = Long.parseLong(data[4]);
					double latitude = Long.parseLong(data[5]);
					long taskId = Long.parseLong(data[6]);
					long tripId = Long.parseLong(data[8]);
					
					Datagram datagram = new Datagram(datagramData, busId, stopId, odometer, longitude, latitude, taskId, lineId, tripId);
					
					
					if(stopsBuses.containsKey(stopId)) {
						
						ArrayList<Datagram> datagrams = stopsBuses.get(stopId);
						SITMStop stop = stopsLngLat.get(stopId);
						
						boolean isin = false;
						int datagramIndex = 0;
							
						for (int i = 0; i < datagrams.size(); i++) {
							if(datagrams.get(i).getBusId()==datagram.getBusId()) {
								isin = true;
								datagramIndex = i;
								i = datagrams.size();
							}
						}
						
						boolean x = isInSameStop(datagrams, datagram, stop);
						
						if(x) {
							if(!isin) {
								stopsBuses.get(stopId).add(datagram);
							}
						}else if(!x && isin){
							
							Long[] times = new Long[3];
							times[0] = datagram.getBusId();
							times[1] = dateFormat.parse(datagrams.get(datagramIndex).getDatagramData()).getTime();
							times[2] = dateFormat.parse(datagram.getDatagramData()).getTime();;
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
		
		for (int i = 0; i < stops.size(); i++) {
			
			if(stopsTimes.containsKey(stops.get(i).getStopId())) {
				System.out.println("================> "+stops.get(i).getLongName());
				for (Long[] data : stopsTimes.get(stops.get(i).getStopId())) {
//					if(data.getBusId()==308)
						System.out.println(data[0]+": "+data[1]+"->"+data[2]);
				}
			}
			
		}
		
		return null;
	}
}
