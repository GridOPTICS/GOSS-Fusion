package pnnl.goss.fusiondb.requests;

import pnnl.goss.core.Request;

public class RequestInterfacesViolation extends Request {

	private static final long serialVersionUID = 9222222715322214294L;
	
	String timestamp;
	int intervalId;
	int interfaceId;
	
	public RequestInterfacesViolation(String timestamp, int intervalId){
		this.timestamp = timestamp;
		this.intervalId = intervalId;
	}
	
	public RequestInterfacesViolation(String timestamp, int intervalId, int interfaceId){
		this.timestamp = timestamp;
		this.intervalId = intervalId;
		this.interfaceId = interfaceId;
	}
	
	public RequestInterfacesViolation(String timestamp){
		this.timestamp = timestamp;
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
	
	
	
}
