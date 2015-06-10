package pnnl.goss.fusion.itests.commands;

import java.util.ArrayList;

import org.apache.felix.dm.annotation.api.Component;
import org.apache.felix.dm.annotation.api.Property;
import org.apache.felix.dm.annotation.api.ServiceDependency;
import org.apache.felix.service.command.CommandProcessor;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;

import pnnl.goss.core.Client;
import pnnl.goss.core.Client.PROTOCOL;
import pnnl.goss.core.ClientFactory;
import pnnl.goss.core.DataError;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.UploadRequest;
import pnnl.goss.core.UploadResponse;
import pnnl.goss.fusiondb.datamodel.ActualTotal;
import pnnl.goss.fusiondb.datamodel.CapacityRequirement;
import pnnl.goss.fusiondb.datamodel.CapacityRequirementValues;
import pnnl.goss.fusiondb.datamodel.ForecastTotal;
import pnnl.goss.fusiondb.datamodel.GeneratorData;
import pnnl.goss.fusiondb.datamodel.HAInterchangeSchedule;
import pnnl.goss.fusiondb.datamodel.InterfacesViolation;
import pnnl.goss.fusiondb.datamodel.RTEDSchedule;
import pnnl.goss.fusiondb.datamodel.VoltageStabilityViolation;
import pnnl.goss.fusiondb.requests.RequestActualTotal;
import pnnl.goss.fusiondb.requests.RequestCapacityRequirement;
import pnnl.goss.fusiondb.requests.RequestForecastTotal;
import pnnl.goss.fusiondb.requests.RequestGeneratorData;
import pnnl.goss.fusiondb.requests.RequestHAInterchangeSchedule;
import pnnl.goss.fusiondb.requests.RequestInterfacesViolation;
import pnnl.goss.fusiondb.requests.RequestRTEDSchedule;
import pnnl.goss.fusiondb.requests.RequestVoltageStabilityViolation;

@Component(properties={
		@Property(name=CommandProcessor.COMMAND_SCOPE, value="fusion"),
        @Property(name=CommandProcessor.COMMAND_FUNCTION, value={
        		"requestCapacityRequirement","getActualTotal","getForecastTotal","getHAInterchageSchedule",
        		"getRTEDSchedule","uploadCapacityRequirements","uploadGeneratorData","requestGeneratorData",
        		"uploadInterfaceViolation","uploadVoltageViolation","requestInterfaceViolation","requestVoltageViolation"})
}, provides=Object.class)
public class FusionCommands {
	
    @ServiceDependency
    private volatile ClientFactory factory;
    private volatile Client client;
    private static String startTimestamp = "2014-01-01 09:00:00";
	private static String endTimestamp = "2014-09-05 10:10:00";
	private int interval = 12;
	
	public FusionCommands(){
		try{
			Credentials credentials = new UsernamePasswordCredentials("system", "manager");
			client = factory.create(PROTOCOL.OPENWIRE, credentials);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
    public void requestCapacityRequirement(String timestamp){
    	try{
    		RequestCapacityRequirement cap = new RequestCapacityRequirement(timestamp);
		    DataResponse resp = (DataResponse)client.getResponse(cap);
		    if (resp.getData() instanceof DataError) {
		    	System.out.println("DataError response was: "+ ((DataError)resp.getData()).getMessage());
		    }
		    else{
		        System.out.println(resp.getData());
		    }
		    System.out.println("Timestamp: " + timestamp);
    	}catch(Exception e){
    		e.printStackTrace();
    	}
                    
    }
    
    public void getActualTotal(){
    	    for(int i=0; i<10;i++){
		    	RequestActualTotal request = new RequestActualTotal(RequestActualTotal.Type.SOLAR, startTimestamp, endTimestamp);
		    	DataResponse response = (DataResponse)client.getResponse(request);
				if(response.getData() instanceof DataError)
					((DataError)response.getData()).getMessage();
				else{
					ActualTotal data = (ActualTotal)response.getData();
					System.out.println("Solar = "+ data.getValues()[0]);
					
					request = new RequestActualTotal(RequestActualTotal.Type.WIND, startTimestamp, endTimestamp);
					response = (DataResponse)client.getResponse(request);
					data = (ActualTotal)response.getData();
					
					if(data!=null){
						System.out.println("Wind = "+ data.getValues()[0]);
						data=null;
					}
					else
						System.out.println("it's null"+i);
				}
			}
    	
		
	}
    
    public void getForecastTotal(){
    	RequestForecastTotal request = new RequestForecastTotal(RequestForecastTotal.Type.LOAD, startTimestamp, interval, endTimestamp);
		DataResponse response = (DataResponse)client.getResponse(request);
		
		if(response.getData() instanceof DataError){
			((DataError)response.getData()).getMessage();
		}
		else{
		ForecastTotal data = (ForecastTotal)response.getData();
		System.out.println(data.getTimestamps().length);
		System.out.println(data.getValues().length);
		System.out.println(data.getIntervals().length);
		}
	}
    
    public void getHAInterchageSchedule(){
    	RequestHAInterchangeSchedule request = new RequestHAInterchangeSchedule(startTimestamp, endTimestamp);
		DataResponse response = (DataResponse)client.getResponse(request);
		if(response.getData() instanceof DataError){
			((DataError)response.getData()).getMessage();
		}
		else{
		HAInterchangeSchedule data = (HAInterchangeSchedule)response.getData();
		System.out.println(data.getTimestamps().length);
		System.out.println(data.getValues().length);
		}
	}
	
	public void getRTEDSchedule(){
		RequestRTEDSchedule request = new RequestRTEDSchedule(startTimestamp, interval,endTimestamp);
		DataResponse response = (DataResponse)client.getResponse(request);
		if(response.getData() instanceof DataError){
			((DataError)response.getData()).getMessage();
		}
		else{
		RTEDSchedule data = (RTEDSchedule)response.getData();
		System.out.println(data.getTimestamps()[0]);
		System.out.println(data.getIntervals()[0]);
		System.out.println(data.getGenValues()[0]);
		System.out.println(data.getMaxValues()[0]);
		System.out.println(data.getMinValues()[0]);
		}
	}
	
	public void uploadCapacityRequirements(){
		
		String timestamp = "2013-1-21 01:01:01";
		int confidence =200;
		int intervalId=1;
		int up=1;
		int down=1;
		
		CapacityRequirement data = new CapacityRequirement(timestamp,confidence,intervalId,up,down);
		UploadRequest request = new UploadRequest(data, "CapacityRequirement");
		UploadResponse response  = (UploadResponse)client.getResponse(request);
		
		/*if(response.isSuccess())
				client.publish("/topic/goss/fusion/capacity", data,RESPONSE_FORMAT.JSON);*/
		if(response.getMessage()!=null)
			System.out.println(response.getMessage());
		
	}
	
	public void requestCapacityRequirement(){
		String timestamp = "2013-1-21 01:01:01";
		
		RequestCapacityRequirement request = new RequestCapacityRequirement(timestamp);
		DataResponse response = (DataResponse)client.getResponse(request);
		CapacityRequirementValues data =null;
		
		if(response.getData() instanceof DataError){
			((DataError)response.getData()).getMessage();
		}
		else{
		data  = (CapacityRequirementValues)response.getData();
		if(data.getTimestamp().length>0){
			System.out.println(data.getTimestamp()[0]);
		}
		}
		request = new RequestCapacityRequirement(timestamp,RequestCapacityRequirement.Parameter.CONFIDENCE,95);
		response = (DataResponse)client.getResponse(request);
		if(response.getData() instanceof DataError){
			((DataError)response.getData()).getMessage();
		}
		else{
		data  = (CapacityRequirementValues)response.getData();
		if(data.getTimestamp().length>0){
			System.out.println(data.getTimestamp()[0]);
		}
		}
		
		request = new RequestCapacityRequirement(timestamp,RequestCapacityRequirement.Parameter.INTERVAL,1);
		response = (DataResponse)client.getResponse(request);
		if(response.getData() instanceof DataError){
			((DataError)response.getData()).getMessage();
		}
		else{
		data  = (CapacityRequirementValues)response.getData();
		if(data.getTimestamp().length>0){
			System.out.println(data.getTimestamp()[0]);
		}
		}
	}
	
	public void uploadGeneratorData(){
		
		// GeneratorData(busNum, genMW, genMVR, genMVRMax, genMVRMin, genVoltSet, genId, genStatus, genMWMax, genMWMin)
		GeneratorData data = new GeneratorData(-1, 0.0, 0.0, 0.0, 0.0, 0.0, "-1", "Closed", 0.0, 0.0);
		UploadRequest request = new UploadRequest(data, "fusion_GeneratorData");
		UploadResponse response  = (UploadResponse)client.getResponse(request);
		if(response.getMessage()!=null)
			System.out.println(response.getMessage());
	}
	
	public void requestGeneratorData(){
		
		//RequestGeneratorData(busNum, genId)
		RequestGeneratorData request = new RequestGeneratorData(-1, -1);
		DataResponse response  = (DataResponse)client.getResponse(request);
		if(response.getData() instanceof DataError){
			System.out.println(((DataError)response.getData()).getMessage());
		}
		else{
		GeneratorData data = (GeneratorData)response.getData();
		System.out.println(data.getBusNum());
		data.getGenId();
		data.getGenMVR();
		data.getGenMVRMax();
		data.getGenMVRMin();
		data.getGenMW();
		data.getGenMWMax();
		data.getGenMWMin();
		data.getGenStatus();
		}
	}
	
	public void uploadInterfaceViolation(){
		//InterfacesViolation(timestamp, intervalId, interfaceId, probability)
		InterfacesViolation data = new InterfacesViolation(startTimestamp, -1, -1, -1.1, 1,1);
		UploadRequest request = new UploadRequest(data, "InterfacesViolation");
		UploadResponse response  = (UploadResponse)client.getResponse(request);
		if(response.getMessage()!=null)
			System.out.println(response.getMessage());
	}
	
	public void uploadVoltageViolation(){
		//VoltageStabilityViolation(timestamp, intervalId, budId, probability)
		VoltageStabilityViolation data = new VoltageStabilityViolation(startTimestamp, -1, -1, -1.1,1,1);
		UploadRequest request = new UploadRequest(data, "VoltageStabilityViolation");
		UploadResponse response  = (UploadResponse)client.getResponse(request);
		if(response.getMessage()!=null)
			System.out.println(response.getMessage());
	}
	
	public void requestInterfaceViolation(){
		
		RequestInterfacesViolation request = new RequestInterfacesViolation(startTimestamp);
		DataResponse response  = (DataResponse)client.getResponse(request);
		if(response.getData() instanceof DataError){
			System.out.println(((DataError)response.getData()).getMessage());
		}
		else{
			ArrayList<InterfacesViolation> list = (ArrayList<InterfacesViolation>)response.getData();
			System.out.println(list.size());
			for(InterfacesViolation data : list){
				System.out.println(data.getTimestamp()+
				data.getIntervalId()+
				data.getInterfaceId()+
				data.getProbability());
			}
		}
	}
	
	public void requestVoltageViolation(){
		RequestVoltageStabilityViolation request = new RequestVoltageStabilityViolation(startTimestamp);
		DataResponse response  = (DataResponse)client.getResponse(request);
		if(response.getData() instanceof DataError){
			System.out.println(((DataError)response.getData()).getMessage());
		}
		else{
			ArrayList<VoltageStabilityViolation> list = (ArrayList<VoltageStabilityViolation>)response.getData();
			for(VoltageStabilityViolation data : list){
				System.out.println(data.getTimestamp()+
				data.getIntervalId()+
				data.getBusId()+
				data.getProbability());
			}
		}
	}


}
