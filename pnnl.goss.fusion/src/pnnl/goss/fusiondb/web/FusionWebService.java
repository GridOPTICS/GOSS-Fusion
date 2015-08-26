package pnnl.goss.fusiondb.web;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.amdatu.web.rest.doc.Description;
import org.amdatu.web.rest.doc.ReturnType;

import pnnl.goss.core.UploadResponse;
import pnnl.goss.core.server.HandlerNotFoundException;
import pnnl.goss.core.server.RequestHandlerRegistry;
import pnnl.goss.fusiondb.datamodel.HAInterchangeScheduleData;
import pnnl.goss.fusiondb.datamodel.RTEDScheduleData;

import com.google.gson.JsonObject;

@Path("/fusion/api")
@Produces(MediaType.APPLICATION_JSON)
public class FusionWebService {
	
	private volatile RequestHandlerRegistry handlers;
	
	@POST
	@Path("/upload/ramping")
	@Consumes(MediaType.APPLICATION_JSON)
	@Description("To Be Done.")
	@Produces(MediaType.APPLICATION_JSON)
	@ReturnType(String.class)
	public Response uploadRamping(@Context HttpServletRequest req) {
		
		System.out.println("Received request to upload ramping capability");
		Response response = null;
		JsonObject requestBody = WebUtil.getRequestJsonBody(req);
		List<String> errors = new ArrayList<>();

		if (!requestBody.has("fileContent") ||
				requestBody.get("fileContent").getAsString().isEmpty()){
			errors.add("Invalid file specified");
		}

		if (errors.size() > 0){
			response = Response.status(Response.Status.BAD_REQUEST)
					.entity(errors).build();

		}
		else{
			
			try{

				String reply = calculateTotalGeneration(requestBody.get("fileContent").getAsString(),requestBody.get("timestamp").getAsString());
				String replyStr = "{\"data\":\""+reply+"\"}";
				response = Response.status(Response.Status.OK).entity(replyStr).build();
			
			}
			catch(FileNotFoundException e){
				e.printStackTrace();
			}catch(IOException e){
				e.printStackTrace();
			}
			
			
		}
		
		
		//System.out.println(response.getEntity().toString());
		return response;
	}
	
	@POST
	@Path("/upload/schedule")
	@Consumes(MediaType.APPLICATION_JSON)
	@Description("To Be Done.")
	@Produces(MediaType.APPLICATION_JSON)
	@ReturnType(String.class)
	public Response uploadSchedule(@Context HttpServletRequest req) {
		
		System.out.println("Received request to upload interchange schedule");
		Response response = null;
		JsonObject requestBody = WebUtil.getRequestJsonBody(req);
		List<String> errors = new ArrayList<>();

		if (!requestBody.has("fileContent") ||
				requestBody.get("fileContent").getAsString().isEmpty()){
			errors.add("Invalid file specified");
		}

		if (errors.size() > 0){
			response = Response.status(Response.Status.BAD_REQUEST)
					.entity(errors).build();

		}
		else{
			
			try{
				String[] lines = requestBody.get("fileContent").getAsString().split("\n");
				String timeStr = requestBody.get("timestamp").getAsString();
				String reply = "ZoneId, Timestamp, IntervalId, Interchange ll";
				for(String line : lines){
					line = line.replace("\"", "").replace("\r", "");
					if(line.trim().length()==0)
						continue;
					if(line.toLowerCase().contains("hour"))
						continue;
					
					String[] row = line.split(",");
					HAInterchangeScheduleData data = new HAInterchangeScheduleData();
					
					//Add hour to timestamp
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					timeStr = timeStr.split(" ")[0]+" 00:00:00";
					Date date = dateFormat.parse(timeStr);
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(date);
					calendar.add(Calendar.HOUR, Integer.valueOf(row[3])-1);
					data.setTimestamps(dateFormat.format(calendar.getTime()));
					
					data.setValues(Double.valueOf(row[4]));
					
					Integer zoneId = row[1].toLowerCase().equals("palouse")? 1 : 2;
					data.setZoneId(zoneId);
					
					data.setIntervalId(Integer.valueOf(row[3]));
					
					reply += row[1]+","+dateFormat.format(calendar.getTime())+","+row[3]+","+row[4]+"ll";
					UploadResponse res = (UploadResponse)handlers.handle(HAInterchangeScheduleData.class.getSimpleName(),data);
					if(!res.getMessage().contains("successful")){
						System.err.println(res.getMessage());
					}
					
					
				}
				
				
				String replyStr = "{\"data\":\""+reply+"\"}";
				response = Response.status(Response.Status.OK).entity(replyStr).build();
			
			
			} catch (HandlerNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		
		
		//System.out.println(response.getEntity().toString());
		return response;
	}

	
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Description("To Be Done.")
	@Produces(MediaType.APPLICATION_JSON)
	@ReturnType(String.class)
	public Response test(@Context HttpServletRequest req) {
		
		System.out.println("Received request to upload ramping capability");
		Response response = null;
		//String identifier = (String) req.getAttribute("identifier");
		JsonObject requestBody = WebUtil.getRequestJsonBody(req);

		List<String> errors = new ArrayList<>();

		if (!requestBody.has("model_file_content") ||
				requestBody.get("model_file_content").getAsString().isEmpty()){
			errors.add("Invalid file specified");
		}

		if (errors.size() > 0){
			response = Response.status(Response.Status.BAD_REQUEST)
					.entity(errors).build();

		}
		else{

			//TODO
		}

		return response;
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	private String calculateTotalGeneration(String fileContent, String timestamp) throws IOException, FileNotFoundException{
		
		List<RTEDScheduleData>  uploadList = new ArrayList<RTEDScheduleData>();
		String[] lines = fileContent.split("\n");
		 
		 
		
		Integer[] intervals = {5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60};
		 List<Integer> intervalList = Arrays.asList(intervals);
		 	
		 List<String> zones= new ArrayList<String>();
		 	zones.add("palouse");
		 	zones.add("puget");
		 	
		
	     String reply = "ZoneId, IntervalId, MW, Up, Down ll";
	     
	     
	     

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
					 reply += zoneId+","+intervalId+","+genDispatchTotal.intValue()+","+generationUpTotal.intValue()+","+generationDownTotal.intValue()+"ll";
					 
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
		 
		 try{
				 for(RTEDScheduleData data : uploadList){
						UploadResponse res = (UploadResponse)handlers.handle(RTEDScheduleData.class.getSimpleName(),data);
						//if(uploadResponse.getMessage()!=null)
							//response = Response.status(Response.Status.OK).entity(reply).build();
			}
			}catch(HandlerNotFoundException e){
				e.printStackTrace();
			}
		 
		 return reply;
}

}
