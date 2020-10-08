package experiment.BusStopTime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import experiment.model.SITMLineStop;
import experiment.model.SITMStop;

public class DataSource {

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
}
