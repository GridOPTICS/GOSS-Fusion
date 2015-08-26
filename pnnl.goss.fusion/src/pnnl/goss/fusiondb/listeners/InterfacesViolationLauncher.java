/*package pnnl.goss.fusiondb.listeners;

import java.io.Serializable;
import java.text.SimpleDateFormat;

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
import pnnl.goss.core.Request;
import pnnl.goss.core.Request.RESPONSE_FORMAT;
import pnnl.goss.fusiondb.datamodel.VizRequest;
import pnnl.goss.fusiondb.handlers.RequestInterfacesViolationHandler;
import pnnl.goss.fusiondb.requests.RequestInterfacesViolation;

import com.google.gson.Gson;

@Component
public class InterfacesViolationLauncher{
	
	Client client = null; 
	
	@ServiceDependency
	private volatile ClientFactory clientFactory;
	
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	String controlTopic = "/topic/goss/fusion/viz/data/control";
	String errorTopic = "goss/fusion/viz/data/error";

	String interfacesViolationTopic = "goss/fusion/viz/interfaces_violation";
	
	InterfacesViolationLauncher launcher ;
	*//**
	 * Receives request from Fusion project's web based visualization on controlTopic.
	 * Published data stream for requested type of data.
	 * 
	 * Interfaces violation Request in the form:
	 * 	 {	type:interfaces_violation,
	 * 		timestamp:"MM/dd/yyyy HH:mm:ss", 
	 * 		interval_id:2}
	 * 
	 * Or: 
	 * 	{ 	type:interfaces_violation, 
	 * 		timestamp:"MM/dd/yyyy HH:mm:ss a", 
	 * 		interval_id:5
	 * 		interface_id:2	}
	 * 
	 *//*
	
	@Start
	public void start(){
		try{
			Credentials credentials = new UsernamePasswordCredentials("system", "manager");
			client = clientFactory.create(PROTOCOL.STOMP, credentials);
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
		
    	Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				GossResponseEvent event =  new GossResponseEvent() {
					@Override
					public void onMessage(Serializable response) {
						try{
							String message = (String)((DataResponse)response).getData();
								Gson gson = new Gson();
								final VizRequest vizRequest = gson.fromJson(message, VizRequest.class);
								if(vizRequest.getType().toLowerCase().equals("interfaces_violation")){
									String timestamp = vizRequest.getTimestamp();
									int intervalId = vizRequest.getIntervalId();
									int interfaceIdValue = vizRequest.getInterfaceId();
									int interfaceId = interfaceIdValue!=0 ?  interfaceIdValue : null;
									
									Request request = new RequestInterfacesViolation(timestamp, intervalId, interfaceId);
									RequestInterfacesViolationHandler handler = new RequestInterfacesViolationHandler();
									DataResponse handlerResponse = (DataResponse)handler.handle(request);
									client.publish(interfacesViolationTopic, (Serializable)handlerResponse.getData(),  RESPONSE_FORMAT.JSON);
									System.out.println(gson.toJson(handlerResponse.getData()));

								}
						}
						catch(Exception e){
							client.publishString(controlTopic, e.getMessage());
							e.printStackTrace();
						}
					}
				};

				client.subscribeTo(controlTopic, event);
				
			}
		});
    	
    	thread.start();
    	
    		
		
	
	    
	}
	
	@Stop
	public void stop(){
		clientFactory.destroy();
	}
	
	
	@Override
	public void run() {
		GossResponseEvent event =  new GossResponseEvent() {
			@Override
			public void onMessage(Serializable response) {
				try{
					String message = (String)((DataResponse)response).getData();
						Gson gson = new Gson();
						final VizRequest vizRequest = gson.fromJson(message, VizRequest.class);
						if(vizRequest.getType().toLowerCase().equals("interfaces_violation")){
							String timestamp = vizRequest.getTimestamp();
							int intervalId = vizRequest.getIntervalId();
							int interfaceIdValue = vizRequest.getInterfaceId();
							int interfaceId = interfaceIdValue!=0 ?  interfaceIdValue : null;
							
							Request request = new RequestInterfacesViolation(timestamp, intervalId, interfaceId);
							RequestInterfacesViolationHandler handler = new RequestInterfacesViolationHandler();
							DataResponse handlerResponse = (DataResponse)handler.handle(request);
							client.publish(interfacesViolationTopic, (Serializable)handlerResponse.getData(),  RESPONSE_FORMAT.JSON);
							System.out.println(gson.toJson(handlerResponse.getData()));

						}
				}
				catch(Exception e){
					client.publishString(controlTopic, e.getMessage());
					e.printStackTrace();
				}
			}
		};

		client.subscribeTo(controlTopic, event);
	}

}

*/