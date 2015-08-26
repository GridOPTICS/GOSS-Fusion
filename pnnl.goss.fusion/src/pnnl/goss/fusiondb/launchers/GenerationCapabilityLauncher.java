package pnnl.goss.fusiondb.launchers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.felix.dm.annotation.api.Component;
import org.apache.felix.dm.annotation.api.ServiceDependency;
import org.apache.felix.dm.annotation.api.Start;
import org.apache.felix.dm.annotation.api.Stop;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;

import pnnl.goss.core.Client;
import pnnl.goss.core.Client.PROTOCOL;
import pnnl.goss.core.ClientFactory;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.GossResponseEvent;
import pnnl.goss.core.Request.RESPONSE_FORMAT;
import pnnl.goss.core.UploadRequest;
import pnnl.goss.core.UploadResponse;
import pnnl.goss.core.server.RequestHandlerRegistry;
import pnnl.goss.core.server.ServerControl;
import pnnl.goss.fusiondb.datamodel.RTEDScheduleData;
import pnnl.goss.fusiondb.requests.RequestGenerationCapability;

import com.google.gson.Gson;

@Component
public class GenerationCapabilityLauncher{
	
	Client client = null;
	Client openwireClient = null;
	
	@ServiceDependency
	private volatile ClientFactory clientFactory;
	
	@ServiceDependency
	private volatile RequestHandlerRegistry handler;
	
	@ServiceDependency
	private volatile ServerControl serverControl;
	

	String requestTopic = "/topic/goss/fusion/viz/request/generation";
	String replyTopic = "goss/fusion/viz/reply/generation/";
	String errorTopic = "goss/fusion/viz/reply/generation/error/";
	
	private volatile List<RTEDScheduleData> uploadList = new ArrayList<RTEDScheduleData>();
	
	@Start
	public void start(){
		try {
			Credentials credentials = new UsernamePasswordCredentials("system", "manager");
			client = clientFactory.create(PROTOCOL.STOMP,credentials);
			openwireClient = clientFactory.create(PROTOCOL.OPENWIRE,credentials);
			run();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    }
	
	@Stop
	public void stop(){
		clientFactory.destroy();
	}
	
	
	public void run() {
		Thread thread_test = new Thread(new Runnable() {
			public void run() {
				GossResponseEvent event =  new GossResponseEvent() {
					@Override
					public void onMessage(Serializable response) {
						RequestGenerationCapability request = null;
						try{
							String message = (String)((DataResponse)response).getData();
							Gson gson = new Gson();
							request = gson.fromJson(message, RequestGenerationCapability.class);
							String reply = calculateTotalGeneration(request.getFileContent(),request.getTimestamp());
							client.publishString(replyTopic+request.getUsername(), reply);
							
							
							for(RTEDScheduleData data : uploadList){
								UploadRequest uploadRequest = new UploadRequest(data, "RTEDScheduleData");
								UploadResponse uploadResponse  = (UploadResponse)openwireClient.getResponse(uploadRequest);
								if(uploadResponse.getMessage()!=null)
									client.publish(errorTopic+request.getUsername(), data,RESPONSE_FORMAT.JSON);
							}
							
							
						}catch(Exception e){
							client.publishString(replyTopic+request.getUsername(), e.getMessage());
							e.printStackTrace();
						}
					}
				};
				
				client.subscribeTo(requestTopic, event);
			}
		});
		thread_test.start();
		
	}
	
	/**
	 * Calculates generation capabilities for each zone based on content of the generation file.
	 * Expects following columns in generation file:
	 * 0: "PK_PowerSystemResource",
	 * 1: "ID",
	 * 2: "Company",
	 * 3: "Station",
	 * 4: "FK_ControlArea",
	 * 5: "ControlArea",
	 * 6: "Generator",
	 * 7: "Mode",
	 * 8: "MechMW",
	 * 9: "ElecMW",
	 * 10: "SchedMW",
	 * 11: "OpMinMW",
	 * 12: "OpMaxMW",
	 * 13: "RatedMinMW",
	 * 14: "RatedMaxMW",
	 * 15: "GovernorBlock",
	 * 16: "Ramp",
	 * 17: "SpinRsrv"} 
	 * @param fileContent
	 * @return generation capabilities
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	String calculateTotalGeneration(String fileContent, String timestamp) throws IOException, FileNotFoundException{
		
		String[] lines = fileContent.split("\n");
		 
		 
		
		Integer[] intervals = {5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60};
		 List<Integer> intervalList = Arrays.asList(intervals);
		 	
		 List<String> zones= new ArrayList<String>();
		 	zones.add("palouse");
		 	zones.add("puget");
		 	
		
	     String reply = "ZoneId, IntervalId, MW, Up, Down \n";
	     
	     
	     

		 for(String zone : zones){
			 
				 for(int interval : intervalList){
					 
					 Double generationUpTotal = 0.0;
					 Double generationDownTotal = 0.0;
					 Double genDispatchTotal = 0.0;
					 
					 for (String line : lines) {
						 
						 		String[] values = line.replace("\"", "").split(",");
						 
								 //Check empty row
								 if(line.trim().length() ==0)
									 continue;
								 
								 //check headers
								 if(line.contains("PK_PowerSystemResource"))
									 continue;
								 
								 //Check mode
								 double mode = Double.valueOf(values[7]);
								 if(mode==0.0)
									 continue;
								 
								//Check Ramp to ignore wind plants
								 double ramp = Double.valueOf(values[16]);
								 if(ramp < 1.0)
									 continue;
								 
								 String rowZone = values[5].trim();
								 
								 if(rowZone.toLowerCase().equals(zone)){
									 
									 double mw = Double.valueOf(values[8]);
									 double min = Double.valueOf(values[11]);
									 double max = Double.valueOf(values[12]);
									 
									 double generationUp = mw+interval*ramp;
									 double generationDown = mw-interval*ramp;
									 
									 
									 if(generationDown<min)
										 generationDown = min;
									 
									 if(generationUp>max)
										 generationUp = max;
									 
									 generationUpTotal += generationUp;
									 generationDownTotal += generationDown;
									 genDispatchTotal += mw;
									 
									 
								 }
								 
								 
				      }
					 int zoneId = zones.indexOf(zone)+1;
					 int intervalId = intervalList.indexOf(interval)+1;
					 reply += zoneId+","+intervalId+","+genDispatchTotal.intValue()+","+generationUpTotal.intValue()+","+generationDownTotal.intValue()+"\n";
					 
					 RTEDScheduleData data = new RTEDScheduleData();
					 data.setGenValue(genDispatchTotal);
					 data.setInterval(intervalId);
					 data.setMaxValue(generationUpTotal);
					 data.setMinValue(generationDownTotal);
					 data.setZoneID(zoneId);
					 data.setTimestamp(timestamp);
					 uploadList.add(data);
					 		 
					 					 
			 }
				
		 }
		 
		 return reply;
}
	

}

