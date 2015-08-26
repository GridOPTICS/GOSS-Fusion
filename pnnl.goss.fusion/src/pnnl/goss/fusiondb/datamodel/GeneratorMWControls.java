package pnnl.goss.fusiondb.datamodel;

public class GeneratorMWControls {
	
	String pk_powerSystemResource;
	String id;
	String company;
	String station;
	String fk_controlArea;
	String controlArea;
	String generator;
	double mode;
	double mechMW;
	double elecMW;
	double schedMW;
	double opMinMW;
	double opMaxMW;
	double ratedMinMW;
	double ratedMaxMW;
	String governorBlock;
	String ramp;
	
	double spinRsrv;
	public String getPk_powerSystemResource() {
		return pk_powerSystemResource;
	}
	public void setPk_powerSystemResource(String pk_powerSystemResource) {
		this.pk_powerSystemResource = pk_powerSystemResource;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getCompany() {
		return company;
	}
	public void setCompany(String company) {
		this.company = company;
	}
	public String getStation() {
		return station;
	}
	public void setStation(String station) {
		this.station = station;
	}
	public String getFk_controlArea() {
		return fk_controlArea;
	}
	public void setFk_controlArea(String fk_controlArea) {
		this.fk_controlArea = fk_controlArea;
	}
	public String getControlArea() {
		return controlArea;
	}
	public void setControlArea(String controlArea) {
		this.controlArea = controlArea;
	}
	public String getGenerator() {
		return generator;
	}
	public void setGenerator(String generator) {
		this.generator = generator;
	}
	public double getMode() {
		return mode;
	}
	public void setMode(double mode) {
		this.mode = mode;
	}
	public double getMechMW() {
		return mechMW;
	}
	public void setMechMW(double mechMW) {
		this.mechMW = mechMW;
	}
	public double getElecMW() {
		return elecMW;
	}
	public void setElecMW(double elecMW) {
		this.elecMW = elecMW;
	}
	public double getSchedMW() {
		return schedMW;
	}
	public void setSchedMW(double schedMW) {
		this.schedMW = schedMW;
	}
	public double getOpMinMW() {
		return opMinMW;
	}
	public void setOpMinMW(double opMinMW) {
		this.opMinMW = opMinMW;
	}
	public double getOpMaxMW() {
		return opMaxMW;
	}
	public void setOpMaxMW(double opMaxMW) {
		this.opMaxMW = opMaxMW;
	}
	public double getRatedMinMW() {
		return ratedMinMW;
	}
	public void setRatedMinMW(double ratedMinMW) {
		this.ratedMinMW = ratedMinMW;
	}
	public double getRatedMaxMW() {
		return ratedMaxMW;
	}
	public void setRatedMaxMW(double ratedMaxMW) {
		this.ratedMaxMW = ratedMaxMW;
	}
	public String getGovernorBlock() {
		return governorBlock;
	}
	public void setGovernorBlock(String governorBlock) {
		this.governorBlock = governorBlock;
	}
	public String getRamp() {
		return ramp;
	}
	public void setRamp(String ramp) {
		this.ramp = ramp;
	}
	public double getSpinRsrv() {
		return spinRsrv;
	}
	public void setSpinRsrv(double spinRsrv) {
		this.spinRsrv = spinRsrv;
	}
	
	
	
	
	
	
	
	

}
