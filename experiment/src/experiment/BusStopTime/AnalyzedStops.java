package experiment.BusStopTime;

public enum AnalyzedStops {

	SALOMIA_A1(500300),
	SALOMIA_B1(500301),
	POPULAR_A1(500350),
	POPULAR_B1(500353);
	
	private final long stopId;
	
	private AnalyzedStops(long stopId) {
		this.stopId = stopId;
	}
	
	public long getStopId() {
		return stopId;
	}
	
}
