package experiment.model;

import java.sql.Date;
 
public class SITMOperationalTravels {
	
	private Long opertravelId;
	private Long busId;
	private Long laststopId;
	private String GPS_X;
	private String GPS_Y;
	private Long deviationTime;
	private Long odometervalue;
	private Long lineId;
	private Long taskId;
	private Long tripId;
	private Long rightcourse;
	private Long orientation;
	private Date eventDate;
	private Long eventTime;
	private Date registerDate;
	private Long eventTypeId;
	private Long nearestStopId;
	private Date lastUpDateDate;
	private Long nearestStopMTS;
	private String updNearestFlag;
	private Long logFileId;
	private Long nearestPlanStopId;
	private Long nearestPlanStopMTS;
	private String planStopAuth;
	private Long radiusToleranceMTS;
	private Long timeDiff;
	
	public SITMOperationalTravels () {
		super();
	}

	public SITMOperationalTravels(Long opertravelId, Long busId,  long lineId, String gPS_X, String gPS_Y, Date eventDate) {
		this.opertravelId = opertravelId;
		this.busId = busId;
		this.lineId = lineId;
		this.GPS_X = gPS_X;
		this.GPS_Y = gPS_Y;
		this.eventDate = eventDate;
	}

	public Long getOpertravelId() {
		return opertravelId;
	}

	public void setOpertravelId(Long opertravelId) {
		this.opertravelId = opertravelId;
	}

	public Long getBusId() {
		return busId;
	}

	public void setBusId(Long busId) {
		this.busId = busId;
	}

	public Long getLaststopId() {
		return laststopId;
	}

	public void setLaststopId(Long laststopId) {
		this.laststopId = laststopId;
	}

	public String getGPS_X() {
		return GPS_X;
	}

	public void setGPS_X(String gPS_X) {
		GPS_X = gPS_X;
	}

	public String getGPS_Y() {
		return GPS_Y;
	}

	public void setGPS_Y(String gPS_Y) {
		GPS_Y = gPS_Y;
	}

	public Long getDeviationTime() {
		return deviationTime;
	}

	public void setDeviationTime(Long deviationTime) {
		this.deviationTime = deviationTime;
	}

	public Long getOdometervalue() {
		return odometervalue;
	}

	public void setOdometervalue(Long odometervalue) {
		this.odometervalue = odometervalue;
	}

	public Long getLineId() {
		return lineId;
	}

	public void setLineId(Long lineId) {
		this.lineId = lineId;
	}

	public Long getTaskId() {
		return taskId;
	}

	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}

	public Long getTripId() {
		return tripId;
	}

	public void setTripId(Long tripId) {
		this.tripId = tripId;
	}

	public Long getRightcourse() {
		return rightcourse;
	}

	public void setRightcourse(Long rightcourse) {
		this.rightcourse = rightcourse;
	}

	public Long getOrientation() {
		return orientation;
	}

	public void setOrientation(Long orientation) {
		this.orientation = orientation;
	}

	public Date getEventDate() {
		return eventDate;
	}

	public void setEventDate(Date eventDate) {
		this.eventDate = eventDate;
	}

	public Long getEventTime() {
		return eventTime;
	}

	public void setEventTime(Long eventTime) {
		this.eventTime = eventTime;
	}

	public Date getRegisterDate() {
		return registerDate;
	}

	public void setRegisterDate(Date registerDate) {
		this.registerDate = registerDate;
	}

	public Long getEventTypeId() {
		return eventTypeId;
	}

	public void setEventTypeId(Long eventTypeId) {
		this.eventTypeId = eventTypeId;
	}

	public Long getNearestStopId() {
		return nearestStopId;
	}

	public void setNearestStopId(Long nearestStopId) {
		this.nearestStopId = nearestStopId;
	}

	public Date getLastUpDateDate() {
		return lastUpDateDate;
	}

	public void setLastUpDateDate(Date lastUpDateDate) {
		this.lastUpDateDate = lastUpDateDate;
	}

	public Long getNearestStopMTS() {
		return nearestStopMTS;
	}

	public void setNearestStopMTS(Long nearestStopMTS) {
		this.nearestStopMTS = nearestStopMTS;
	}

	public String getUpdNearestFlag() {
		return updNearestFlag;
	}

	public void setUpdNearestFlag(String updNearestFlag) {
		this.updNearestFlag = updNearestFlag;
	}

	public Long getLogFileId() {
		return logFileId;
	}

	public void setLogFileId(Long logFileId) {
		this.logFileId = logFileId;
	}

	public Long getNearestPlanStopId() {
		return nearestPlanStopId;
	}

	public void setNearestPlanStopId(Long nearestPlanStopId) {
		this.nearestPlanStopId = nearestPlanStopId;
	}

	public Long getNearestPlanStopMTS() {
		return nearestPlanStopMTS;
	}

	public void setNearestPlanStopMTS(Long nearestPlanStopMTS) {
		this.nearestPlanStopMTS = nearestPlanStopMTS;
	}

	public String getPlanStopAuth() {
		return planStopAuth;
	}

	public void setPlanStopAuth(String planStopAuth) {
		this.planStopAuth = planStopAuth;
	}

	public Long getRadiusToleranceMTS() {
		return radiusToleranceMTS;
	}

	public void setRadiusToleranceMTS(Long radiusToleranceMTS) {
		this.radiusToleranceMTS = radiusToleranceMTS;
	}

	public Long getTimeDiff() {
		return timeDiff;
	}

	public void setTimeDiff(Long timeDiff) {
		this.timeDiff = timeDiff;
	}
}
