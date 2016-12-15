package org.apache.nutch.entitybean;

public class TrainBean {
	private String router; // gio tau tuyen Ha Noi- Lao Cai; vvv
	private String lineUrl;
	private String trainCode; // SPT1, SPT2 vv
	private String departureStation;
	private String arrivalStation;
	private String departureTime; // UNIX_TIMESTAMP () of date-time
	private String arrivalTime; //// UNIX_TIMESTAMP () of date-time
	private String startDate; // dd-mm-yyyy
	private String endDate; // dd-mm-yyyy
	private String startTime; // hh:mm
	private String endTime; // hh:mm
	private double distance; 
	private int dateAdd;
	
	public int getDateAdd() {
		return dateAdd;
	}

	public void setDateAdd(int dateAdd) {
		this.dateAdd = dateAdd;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public String getLineUrl() {
		return lineUrl;
	}

	public void setLineUrl(String lineUrl) {
		this.lineUrl = lineUrl;
	}

	public String getDepartureStation() {
		return departureStation;
	}

	public void setDepartureStation(String departureStation) {
		this.departureStation = departureStation;
	}

	public String getArrivalStation() {
		return arrivalStation;
	}

	public void setArrivalStation(String arrivalStation) {
		this.arrivalStation = arrivalStation;
	}

	public String getDepartureTime() {
		return departureTime;
	}

	public String getRouter() {
		return router;
	}

	public void setRouter(String router) {
		this.router = router;
	}

	public String getTrainCode() {
		return trainCode;
	}

	public void setTrainCode(String trainCode) {
		this.trainCode = trainCode;
	}

	public void setDepartureTime(String departureTime) {
		this.departureTime = departureTime;
	}

	public String getArrivalTime() {
		return arrivalTime;
	}

	public void setArrivalTime(String arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

}
