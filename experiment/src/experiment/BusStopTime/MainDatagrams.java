package experiment.BusStopTime;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

import experiment.model.Datagram;
import experiment.model.SITMStop;

class TimeDatagrams {
	private Calendar calendar;
	private int numDatagrams;

	public TimeDatagrams(Calendar calendar, int numDatagrams) {
		super();
		this.calendar = calendar;
		this.numDatagrams = numDatagrams;
	}

	public Calendar getCalendar() {
		return calendar;
	}

	public void setCalendar(Calendar calendar) {
		this.calendar = calendar;
	}

	public int getNumDatagrams() {
		return numDatagrams;
	}

	public void setNumDatagrams(int numDatagrams) {
		this.numDatagrams = numDatagrams;
	}

	public String getDate() {
		int Hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minutes = calendar.get(Calendar.MINUTE);
		if (minutes == 0) {
			return Hour + ":" + minutes + "0";
		}
		return Hour + ":" + minutes;
	}
}

public class MainDatagrams {

	public static HashMap<Long, SITMStop> stops; // HashMap with the stops
	public static HashMap<Long, ArrayList<TimeDatagrams>> stopsDatagrams; // HashMap with list of datagrams
	public static HashMap<Long, Integer> stopsTime; // HashMap with time

	public static int totalDatagrams;

	public static void main(String[] args) throws ParseException {
		init(131);
		readDatagrams(131, 500300);
		results();
	}

	/*
	 * This method initialize the hash maps with the necessaries stop ids and arrays
	 */
	public static void init(long lineId) {

		ArrayList<SITMStop> stopsQuery = DataSource.findAllStopsByLine(261, lineId);
		stops = new HashMap<>();
		stopsDatagrams = new HashMap<>();
		stopsTime = new HashMap<>();

		for (int i = 0; i < stopsQuery.size(); i++) {
			if (!stops.containsKey(stopsQuery.get(i).getStopId())) {
				stops.put(stopsQuery.get(i).getStopId(), stopsQuery.get(i));
				stopsDatagrams.put(stopsQuery.get(i).getStopId(), new ArrayList<>());
				stopsTime.put(stopsQuery.get(i).getStopId(), 0);
			}
		}
	}

	/*
	 * This method read the datagrams file
	 */
	public static void readDatagrams(long lineId, long observerStop) throws ParseException {

		String path = "data/output-30-APR-19-awk-sorted.csv";
		ArrayList<Datagram> datagrams = DataSource.readDatagrams3(lineId, path);

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

		totalDatagrams++;
		datagram.getDatagramDate();

		ArrayList<TimeDatagrams> list = stopsDatagrams.get(stopId);
		int minute = stopsTime.get(stopId);

		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(datagram.getDatagramDate());

		int actualMinute = calendar.get(Calendar.MINUTE);

		if (list.size() == 0) {
			list.add(new TimeDatagrams(calendar, 1));
			stopsTime.put(stopId, (int) (actualMinute / 10) * 10);
			// System.out.println(minute+" ====================");

		} else {

			TimeDatagrams td = list.get(list.size() - 1);
			int limitTime = minute + 10;

			if (minute != 0 && actualMinute <= 9) {
				list.add(new TimeDatagrams(calendar, 1));
				stopsTime.put(stopId, 0);
				// System.out.println(minute+" ++++++++++++++++++");
			}

			if (actualMinute < limitTime) {
				td.setNumDatagrams(td.getNumDatagrams() + 1);
			} else {
				list.add(new TimeDatagrams(calendar, 1));
				stopsTime.put(stopId, (int) (actualMinute / 10) * 10);
				// System.out.println(minute+" ====================");
			}
//				System.out.println(calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE));
		}

	}

	/*
	 * This method print the results
	 */
	public static void results() {

		long[] studiedStops = { 504009, 500200, 500250, 500300 };
		System.out.println("total datagramas T31" + totalDatagrams);
		System.out.println(" ");

		for (long id : studiedStops) {

			ArrayList<TimeDatagrams> datagramsList = stopsDatagrams.get(id);
			System.out.println("StopId: " + id);

			for (TimeDatagrams timeDatagrams : datagramsList) {

				double users = (double) timeDatagrams.getNumDatagrams() / totalDatagrams * 450000;
				int numUsers = (int) users;
//				System.out.println("Date: " + timeDatagrams.getDate() + " | Datagramas: "+ timeDatagrams.getNumDatagrams() + " | Usuarios: " + numUsers);
				System.out.println(timeDatagrams.getDate()+","+numUsers);
			}

			System.out.println();

		}

	}
}
