package pnnl.goss.fusiondb.datamodel;

import java.io.Serializable;

public class VizRequest implements Serializable{
	
	private static final long serialVersionUID = -2872405645401318090L;
	
	String type;
	String timestamp;
	Integer range;
	String unit;
	String endTimestamp;
	Integer intervalId;
	Integer interfaceId;
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public Integer getRange() {
		return range;
	}
	public void setRange(Integer range) {
		this.range = range;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	public String getEndTimestamp() {
		return endTimestamp;
	}
	public void setEndTimestamp(String endTimestamp) {
		this.endTimestamp = endTimestamp;
	}
	public Integer getIntervalId() {
		return intervalId;
	}
	public void setIntervalId(Integer intervalId) {
		this.intervalId = intervalId;
	}
	public Integer getInterfaceId() {
		return interfaceId;
	}
	public void setInterfaceId(Integer interfaceId) {
		this.interfaceId = interfaceId;
	}
	
	
	

}
