package experiment.model;

public class Datagram {
	
	private String datagramData;
	private long busId;
	private long stopId;
	private long odometer;
	private long  longitude;
	private long latitude;
	private long taskId;
	private long lineId;
	private long tripId;
	
	public Datagram(String datagramData, long busId, long stopId, long odometer, long longitude, long latitude, long taskId, long lineId, long tripId) {
		this.datagramData = datagramData;
		this.busId = busId;
		this.stopId = stopId;
		this.odometer = odometer;
		this.longitude = longitude;
		this.latitude = latitude;
		this.taskId = taskId;
		this.lineId = lineId;
		this.tripId = tripId;
	}

	public String getDatagramData() {
		return datagramData;
	}

	public void setDatagramData(String datagramData) {
		this.datagramData = datagramData;
	}

	public long getBusId() {
		return busId;
	}

	public void setBusId(long busId) {
		this.busId = busId;
	}

	public long getStopId() {
		return stopId;
	}

	public void setStopId(long stopId) {
		this.stopId = stopId;
	}

	public long getOdometer() {
		return odometer;
	}

	public void setOdometer(long odometer) {
		this.odometer = odometer;
	}

	public long getLongitude() {
		return longitude;
	}

	public void setLongitude(long longitude) {
		this.longitude = longitude;
	}

	public long getLatitude() {
		return latitude;
	}

	public void setLatitude(long latitude) {
		this.latitude = latitude;
	}

	public long getTaskId() {
		return taskId;
	}

	public void setTaskId(long taskId) {
		this.taskId = taskId;
	}

	public long getLineId() {
		return lineId;
	}

	public void setLineId(long lineId) {
		this.lineId = lineId;
	}

	public long getTripId() {
		return tripId;
	}

	public void setTripId(long tripId) {
		this.tripId = tripId;
	}
}
