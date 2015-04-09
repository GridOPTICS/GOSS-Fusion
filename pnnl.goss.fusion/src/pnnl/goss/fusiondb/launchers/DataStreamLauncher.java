package pnnl.goss.fusiondb.launchers;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.felix.dm.annotation.api.Start;
import org.apache.felix.dm.annotation.api.Stop;
import org.apache.http.auth.UsernamePasswordCredentials;

import pnnl.goss.core.Client;
import pnnl.goss.core.Client.PROTOCOL;
import pnnl.goss.core.ClientFactory;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.GossResponseEvent;
import pnnl.goss.core.Request;
import pnnl.goss.core.Request.RESPONSE_FORMAT;
import pnnl.goss.core.server.RequestHandler;
import pnnl.goss.fusiondb.datamodel.VizRequest;
import pnnl.goss.fusiondb.handlers.RequestActualTotalHandler;
import pnnl.goss.fusiondb.handlers.RequestCapacityRequirementHandler;
import pnnl.goss.fusiondb.handlers.RequestForecastTotalHandler;
import pnnl.goss.fusiondb.handlers.RequestRTEDScheduleHandler;
import pnnl.goss.fusiondb.requests.RequestActualTotal;
import pnnl.goss.fusiondb.requests.RequestActualTotal.Type;
import pnnl.goss.fusiondb.requests.RequestCapacityRequirement;
import pnnl.goss.fusiondb.requests.RequestForecastTotal;
import pnnl.goss.fusiondb.requests.RequestRTEDSchedule;

import com.google.gson.Gson;

public class DataStreamLauncher implements Runnable {
	
	private volatile boolean isRunning = false;
	
	Client client = null; 
	private volatile ClientFactory clientFactory;
	
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	String controlTopic = "goss/fusion/viz/control";
	String errorTopic = "goss/fusion/viz/error";

	String historicTopic = "goss/fusion/viz/historic";
	String historicCapaReqTopic  = historicTopic+"/capareq";
	String historicInterchangeTotalTopic = historicTopic+"/inter_total";
	String historicInterchangeScheduleTopic = historicTopic+"/inter_sched";
	String historicActualLoadTopic = historicTopic+"/actual_load";
	String historicActualSolarTopic = historicTopic+"/actual_solar";
	String historicActualWindTopic = historicTopic+"/actual_wind";
	String historicForecastLoadTopic = historicTopic+"/forecast_load";
	String historicForecastSolarTopic = historicTopic+"/forecast_solar";
	String historicForecastWindTopic = historicTopic+"/forecast_wind";

	String currentTopic = "goss/fusion/viz/current";
	String currentCapaReqTopic  = currentTopic+"/capareq";
	String currentInterchangeTotalTopic = currentTopic+"/inter_total";
	String currentInterchangeScheduleTopic = currentTopic+"/inter_sched";
	String currentActualLoadTopic = currentTopic+"/actual/load";
	String currentActualSolarTopic = currentTopic+"/actual/solar";
	String currentActualWindTopic = currentTopic+"/actual/wind";
	String currentForecastLoadTopic = currentTopic+"/forecast/load";
	String currentForecastSolarTopic = currentTopic+"/forecast/solar";
	String currentForecastWindTopic = currentTopic+"/forecast/wind";
	DataStreamLauncher launcher ;
	/**
	 * Receives request from Fusion project's web based visualization on controlTopic.
	 * Published data stream for historic and current data.
	 * 
	 * Historic Request in the form:
	 * 	 {	type:historic,
	 * 		timestamp:"MM/dd/yyyy HH:mm:ss", 
	 * 		range:2
	 * 		unit:hour 	}
	 * 
	 * Current Request in the form: 
	 * 	{ 	type:current, 
	 * 		timestamp:"MM/dd/yyyy HH:mm:ss a", 
	 * 		range:5
	 * 		unit:minute	}
	 * 
	 * To stop current data stream:
	 * "stop stream"
	 * 
	 */
	
	//private volatile GossRequestHandlerRegistrationService registrationService;
	//private volatile GossDataServices dataServices;
	
	
	
	/*
	public DataStreamLauncher(@Requires GossRequestHandlerRegistrationService registrationService, @Requires GossDataServices dataServices){
		this.registrationService = registrationService;
		this.dataServices = dataServices;
		client = new GossClient(PROTOCOL.STOMP);
		client.setConfiguration(this.registrationService.getCoreServerConfig());
	}
	
	private DataStreamLauncher(){
		
	}
	
	@Validate
	public void startLauncher(){
		Thread thread = new Thread(new DataStreamLauncher(this.registrationService, this.dataServices));
		thread.start();
	}
	
	@Invalidate
	public void stopLauncher(){
		isRunning = false;
		if (client != null){
			client.close();
		}
	}*/
	
	@Start
	public void start(){
		client = clientFactory.create(PROTOCOL.STOMP); 
    	client.setCredentials(new UsernamePasswordCredentials("system", "manager"));
    	
	    
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
					if(message.contains("stop stream"))
						isRunning= false;
					else{
						Gson gson = new Gson();
						final VizRequest vizRequest = gson.fromJson(message, VizRequest.class);
						if(vizRequest.getType().toLowerCase().equals("historic")){
							String endTimestamp = vizRequest.getTimestamp();
							Date date = dateFormat.parse(endTimestamp);
							date = new Date(date.getTime()-(vizRequest.getRange()*60*60*1000));
							String timestamp = dateFormat.format(date);
							publishHistoricData(timestamp, endTimestamp);
						}
						if(vizRequest.getType().toLowerCase().equals("current")){
							Thread thread = new Thread(new Runnable() {

								@Override
								public void run() {
									try{
										SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
										isRunning = true;
										String timestamp = vizRequest.getTimestamp();
										Date date = dateFormat.parse(timestamp);
										date = new Date(date.getTime()+(vizRequest.getRange()*60*1000));
										String endTimestamp = dateFormat.format(date);
										publishHistoricData(timestamp, endTimestamp);
										while(isRunning){
											publishCurrentData(timestamp,endTimestamp);
											timestamp = endTimestamp;
											date = dateFormat.parse(timestamp);
											date = new Date(date.getTime()+(vizRequest.getRange()*60*1000));
											endTimestamp = dateFormat.format(date);
										}
									}catch(ParseException p){
										client.publishString(controlTopic, "timestamp is not in correct format mm/dd/yyyy HH:mm:ss");
										p.printStackTrace();
									}
								}
							});
							thread.start();
						}
					}
				}catch(ParseException e){
					client.publishString(controlTopic, "timestamp is not in correct format mm/dd/yyyy HH:mm:ss");
					e.printStackTrace();
				}catch(Exception e){
					client.publishString(controlTopic, e.getMessage());
					e.printStackTrace();
				}
			}
		};

		client.subscribeTo("/topic/goss/fusion/viz/control", event);
	}

	/**
	 * queries and publishes historical data
	 */
	private void publishHistoricData(String timeStamp, String endTimestamp){

		// capacity requirement
		Request request = new RequestCapacityRequirement(timeStamp,endTimestamp);
		RequestCapacityRequirementHandler handler = new RequestCapacityRequirementHandler();
		handler.viz = true;
		DataResponse response = (DataResponse)handler.handle(request);
		client.publish(historicCapaReqTopic, (Serializable)response.getData(),  RESPONSE_FORMAT.JSON);
		Gson gson = new Gson();
		System.out.println(gson.toJson(response.getData()));

		// total rted
		request = new RequestRTEDSchedule(timeStamp, endTimestamp);
		RequestRTEDScheduleHandler rtedhandler = new RequestRTEDScheduleHandler();
		rtedhandler.viz = true;
		response = (DataResponse)rtedhandler.handle(request);
		client.publish(historicInterchangeScheduleTopic, (Serializable)response.getData(),  RESPONSE_FORMAT.JSON);
		System.out.println(gson.toJson(response.getData()));

		// total interchange
		request =  new RequestActualTotal(Type.INTERHCHANGE, timeStamp, endTimestamp);
		RequestActualTotalHandler actualtotalhandler = new RequestActualTotalHandler();
		actualtotalhandler.viz = true;
		response = (DataResponse)actualtotalhandler.handle(request);
		client.publish(historicInterchangeTotalTopic, (Serializable)response.getData(),  RESPONSE_FORMAT.JSON);
		System.out.println(gson.toJson(response.getData()));

		// actual load
		request =  new RequestActualTotal(Type.LOAD, timeStamp, endTimestamp);
		actualtotalhandler = new RequestActualTotalHandler();
		actualtotalhandler.viz = true;
		response = (DataResponse)actualtotalhandler.handle(request);
		client.publish(historicActualLoadTopic, (Serializable)response.getData(),  RESPONSE_FORMAT.JSON);
		System.out.println(gson.toJson(response.getData()));

		// actual wind
		request =  new RequestActualTotal(Type.WIND, timeStamp, endTimestamp);
		actualtotalhandler = new RequestActualTotalHandler();
		actualtotalhandler.viz = true;
		response = (DataResponse)actualtotalhandler.handle(request);
		client.publish(historicActualWindTopic, (Serializable)response.getData(),  RESPONSE_FORMAT.JSON);
		System.out.println(gson.toJson(response.getData()));

		// actual solar
		request =  new RequestActualTotal(Type.SOLAR, timeStamp, endTimestamp);
		actualtotalhandler = new RequestActualTotalHandler();
		actualtotalhandler.viz = true;
		response = (DataResponse)actualtotalhandler.handle(request);
		client.publish(historicActualSolarTopic, (Serializable)response.getData(),  RESPONSE_FORMAT.JSON);
		System.out.println(gson.toJson(response.getData()));

		//forecast load
		request =  new RequestForecastTotal(pnnl.goss.fusiondb.requests.RequestForecastTotal.Type.LOAD, timeStamp, endTimestamp);
		RequestForecastTotalHandler forecasthandler = new RequestForecastTotalHandler();
		forecasthandler.viz = true;
		response = (DataResponse)forecasthandler.handle(request);
		client.publish(historicForecastLoadTopic, (Serializable)response.getData(),  RESPONSE_FORMAT.JSON);
		System.out.println(gson.toJson(response.getData()));

		//forecast solar
		request =  new RequestForecastTotal(pnnl.goss.fusiondb.requests.RequestForecastTotal.Type.SOLAR, timeStamp, endTimestamp);
		forecasthandler = new RequestForecastTotalHandler();
		forecasthandler.viz = true;
		response = (DataResponse)forecasthandler.handle(request);
		client.publish(historicForecastSolarTopic, (Serializable)response.getData(),  RESPONSE_FORMAT.JSON);
		System.out.println(gson.toJson(response.getData()));

		//forecast wind
		request =  new RequestForecastTotal(pnnl.goss.fusiondb.requests.RequestForecastTotal.Type.WIND, timeStamp, endTimestamp);
		forecasthandler = new RequestForecastTotalHandler();
		forecasthandler.viz = true;
		response = (DataResponse)forecasthandler.handle(request);
		client.publish(historicForecastWindTopic, (Serializable)response.getData(),  RESPONSE_FORMAT.JSON);
		System.out.println(gson.toJson(response.getData()));
	}

	private void publishCurrentData(String timeStamp, String endTimestamp){
		
		// capacity requirement
		Request request = new RequestCapacityRequirement(timeStamp,endTimestamp);
		RequestHandler handler = new RequestCapacityRequirementHandler();
		((RequestCapacityRequirementHandler)handler).viz = true;
		DataResponse response = (DataResponse)handler.handle(request);
		client.publish(currentCapaReqTopic, (Serializable)response.getData(),  RESPONSE_FORMAT.JSON);

		// total rted
		request = new RequestRTEDSchedule(timeStamp, endTimestamp);
		handler = new RequestRTEDScheduleHandler();
		((RequestRTEDScheduleHandler)handler).viz = true;
		response = (DataResponse)handler.handle(request);
		client.publish(currentInterchangeScheduleTopic, (Serializable)response.getData(),  RESPONSE_FORMAT.JSON);

		// total interchange
		request =  new RequestActualTotal(Type.INTERHCHANGE, timeStamp, endTimestamp);
		handler = new RequestActualTotalHandler();
		((RequestActualTotalHandler)handler).viz = true;
		response = (DataResponse)handler.handle(request);
		client.publish(currentInterchangeTotalTopic, (Serializable)response.getData(),  RESPONSE_FORMAT.JSON);

		// actual load
		request =  new RequestActualTotal(Type.LOAD, timeStamp, endTimestamp);
		handler = new RequestActualTotalHandler();
		((RequestActualTotalHandler)handler).viz = true;
		response = (DataResponse)handler.handle(request);
		client.publish(currentActualLoadTopic, (Serializable)response.getData(),  RESPONSE_FORMAT.JSON);

		// actual wind
		request =  new RequestActualTotal(Type.WIND, timeStamp, endTimestamp);
		handler = new RequestActualTotalHandler();
		((RequestActualTotalHandler)handler).viz = true;
		response = (DataResponse)handler.handle(request);
		client.publish(currentActualWindTopic, (Serializable)response.getData(),  RESPONSE_FORMAT.JSON);

		// actual solar
		request =  new RequestActualTotal(Type.SOLAR, timeStamp, endTimestamp);
		handler = new RequestActualTotalHandler();
		((RequestActualTotalHandler)handler).viz = true;
		response = (DataResponse)handler.handle(request);
		client.publish(currentActualSolarTopic, (Serializable)response.getData(),  RESPONSE_FORMAT.JSON);

		//forecast load
		request =  new RequestForecastTotal(pnnl.goss.fusiondb.requests.RequestForecastTotal.Type.LOAD, timeStamp, endTimestamp);
		handler = new RequestForecastTotalHandler();
		((RequestForecastTotalHandler)handler).viz = true;
		response = (DataResponse)handler.handle(request);
		client.publish(currentForecastLoadTopic, (Serializable)response.getData(),  RESPONSE_FORMAT.JSON);

		//forecast solar
		request =  new RequestForecastTotal(pnnl.goss.fusiondb.requests.RequestForecastTotal.Type.SOLAR, timeStamp, endTimestamp);
		handler = new RequestForecastTotalHandler();
		((RequestForecastTotalHandler)handler).viz = true;
		response = (DataResponse)handler.handle(request);
		client.publish(currentForecastSolarTopic, (Serializable)response.getData(),  RESPONSE_FORMAT.JSON);

		//forecast wind
		request =  new RequestForecastTotal(pnnl.goss.fusiondb.requests.RequestForecastTotal.Type.WIND, timeStamp, endTimestamp);
		handler = new RequestForecastTotalHandler();
		((RequestForecastTotalHandler)handler).viz = true;
		response = (DataResponse)handler.handle(request);
		client.publish(currentForecastWindTopic, (Serializable)response.getData(),  RESPONSE_FORMAT.JSON);

	}

}
