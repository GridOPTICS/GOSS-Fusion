package pnnl.goss.fusiondb.datamodel;

import java.io.Serializable;

public class InterfacesViolation implements Serializable {

	private static final long serialVersionUID = 2791251857621832571L;
	
	String timestamp;
	int intervalId;
	int interfaceId;
	double probability;
	int size;
	int limit;
	
	public InterfacesViolation(String timestamp, int intervalId, int interfaceId, double probability, int size, int limit) {
		this.timestamp = timestamp;
		this.intervalId = intervalId;
		this.interfaceId = interfaceId;
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
	public int getInterfaceId() {
		return interfaceId;
	}
	public void setInterfaceId(int interfaceId) {
		this.interfaceId = interfaceId;
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
