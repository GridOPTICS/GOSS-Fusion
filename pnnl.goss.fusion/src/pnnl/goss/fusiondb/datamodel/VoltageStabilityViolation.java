package pnnl.goss.fusiondb.datamodel;

import java.io.Serializable;

public class VoltageStabilityViolation implements Serializable {

	private static final long serialVersionUID = -2364245662049846190L;
	
	String timestamp;
	int intervalId;
	int busId;
	double probability;
	int size;
	int limit;
	
	public VoltageStabilityViolation(String timestamp, int intervalId, int busId, double probability, int size, int limit) {
		this.timestamp = timestamp;
		this.intervalId = intervalId;
		this.busId = busId;
		this.probability = probability;
		this.size = size;
		this.limit = limit;
	}
	
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public int getIntervalId() {
		return intervalId;
	}
	public void setIntervalId(int intervalId) {
		this.intervalId = intervalId;
	}
	public int getBusId() {
		return busId;
	}
	public void setBusId(int busId) {
		this.busId = busId;
	}
	public double getProbability() {
		return probability;
	}
	public void setProbability(double probability) {
		this.probability = probability;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public int getLimit() {
		return limit;
	}
	public void setLimit(int limit) {
		this.limit = limit;
	}
}
