/*
	Copyright (c) 2014, Battelle Memorial Institute
    All rights reserved.
    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:
    1. Redistributions of source code must retain the above copyright notice, this
    list of conditions and the following disclaimer.
    2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.
    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
    ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
    WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
     
    DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
    ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
    (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
    LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
    ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
    SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
    The views and conclusions contained in the software and documentation are those
    of the authors and should not be interpreted as representing official policies,
    either expressed or implied, of the FreeBSD Project.
    This material was prepared as an account of work sponsored by an
    agency of the United States Government. Neither the United States
    Government nor the United States Department of Energy, nor Battelle,
    nor any of their employees, nor any jurisdiction or organization
    that has cooperated in the development of these materials, makes
    any warranty, express or implied, or assumes any legal liability
    or responsibility for the accuracy, completeness, or usefulness or
    any information, apparatus, product, software, or process disclosed,
    or represents that its use would not infringe privately owned rights.
    Reference herein to any specific commercial product, process, or
    service by trade name, trademark, manufacturer, or otherwise does
    not necessarily constitute or imply its endorsement, recommendation,
    or favoring by the United States Government or any agency thereof,
    or Battelle Memorial Institute. The views and opinions of authors
    expressed herein do not necessarily state or reflect those of the
    United States Government or any agency thereof.
    PACIFIC NORTHWEST NATIONAL LABORATORY
    operated by BATTELLE for the UNITED STATES DEPARTMENT OF ENERGY
    under Contract DE-AC05-76RL01830
*/
package pnnl.goss.fusiondb.handlers;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.apache.felix.dm.annotation.api.Component;
import org.apache.felix.dm.annotation.api.ServiceDependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.core.Response;
import pnnl.goss.core.UploadResponse;
import pnnl.goss.core.security.AuthorizationHandler;
import pnnl.goss.core.server.DataSourcePooledJdbc;
import pnnl.goss.core.server.DataSourceRegistry;
import pnnl.goss.core.server.RequestUploadHandler;
import pnnl.goss.fusiondb.auth.FusionUploadAuthHandler;
import pnnl.goss.fusiondb.datamodel.CapacityRequirement;
import pnnl.goss.fusiondb.datamodel.GeneratorData;
import pnnl.goss.fusiondb.datamodel.HAInterchangeScheduleData;
import pnnl.goss.fusiondb.datamodel.InterfacesViolation;
import pnnl.goss.fusiondb.datamodel.RTEDScheduleData;
import pnnl.goss.fusiondb.datamodel.VoltageStabilityViolation;
import pnnl.goss.fusiondb.server.datasources.FusionDataSource;

/**
 * Handle the storing of data for the fusion datasource.  The execution uses
 * try-with-resources see http://docs.oracle.com/javase/7/docs/technotes/guides/language/try-with-resources.html
 * for details.
 * 
 * @author Craig Allwardt
 *
 */
@Component
public class FusionUploadHandler implements RequestUploadHandler {
	
	private static final Logger log = LoggerFactory.getLogger(FusionUploadHandler.class);
			
	@ServiceDependency
	private volatile DataSourceRegistry dsRegistry;
	
	@Override
	public Map<String, Class<? extends AuthorizationHandler>> getHandlerDataTypes() {
		Map<String, Class<? extends AuthorizationHandler>> auths = new HashMap<>();
		
		auths.put(CapacityRequirement.class.getSimpleName(), FusionUploadAuthHandler.class);
		auths.put(GeneratorData.class.getSimpleName(), FusionUploadAuthHandler.class);
		auths.put(InterfacesViolation.class.getSimpleName(), FusionUploadAuthHandler.class);
		auths.put(VoltageStabilityViolation.class.getSimpleName(), FusionUploadAuthHandler.class);
		auths.put(RTEDScheduleData.class.getSimpleName(), FusionUploadAuthHandler.class);
		auths.put(HAInterchangeScheduleData.class.getSimpleName(), FusionUploadAuthHandler.class);
		
		return auths;
	}

	@Override
	public Response upload(String dataType, Serializable data) {

		UploadResponse response = null;
		boolean success = false;

		if (dataType.equals(CapacityRequirement.class.getSimpleName())) {
			success = uploadCapacityRequirement((CapacityRequirement) data);
		} else if (dataType.equals(GeneratorData.class.getSimpleName())) {
			success = uploadGeneratorData((GeneratorData) data);
		} else if (dataType.equals(InterfacesViolation.class.getSimpleName())) {
			success = uploadInterfacesViolation((InterfacesViolation) data);
		} else if (dataType.equals(VoltageStabilityViolation.class.getSimpleName())) {
			success = uploadVoltageStabilityViolation((VoltageStabilityViolation) data);
		} else if (dataType.equals(RTEDScheduleData.class.getSimpleName())) {
			success = uploadRTEDScheduleData((RTEDScheduleData) data);
		} else if (dataType.equals(HAInterchangeScheduleData.class.getSimpleName())) {
			success = uploadHAInterchangeScheduleData((HAInterchangeScheduleData) data);
		} else {
			response = new UploadResponse(false);
			response.setMessage("Unknown datatype: " + dataType	+ " specified.");
		}
			
		if (success){
			response = new UploadResponse(true);
			response.setMessage("Upload of "+dataType+" was successful.");
		}
		else{
			response = new UploadResponse(false);
			response.setMessage("Failed to upload "+dataType);
		}

		return response;
	}
	
	/**
	 * Execute an sql update string based upon the uploaded update string.
	 * 
	 * @param queryString
	 * @return
	 */
	private int executeUploadDataSql(String queryString){
		log.debug("EXEC SQL: " + queryString);
		int rows = -1;

		DataSourcePooledJdbc ds = (DataSourcePooledJdbc)dsRegistry.get(FusionDataSource.class.getName());
		
		try (Connection connection = ds.getConnection()){			
			try (Statement stmt = connection.createStatement()){
				rows =  stmt.executeUpdate(queryString);
				if(connection.getAutoCommit()==false)
					connection.commit();
			}
			
			log.debug("ROWS: " + rows);
		} catch(SQLException e){
			log.error("Error executing "+ queryString);			
		} 
		
		return rows;
	}
	
	private boolean uploadCapacityRequirement(CapacityRequirement data){
		
		String queryString = "replace into capacity_requirements(`timestamp`,confidence,interval_id,up,down,zoneid) values "+
								"('"+data.getTimestamp()+"',"+data.getConfidence()+","+data.getIntervalId()+","+data.getUp()+","+data.getDown()+","+data.getZoneId()+")";
		
		log.debug(queryString);
		int rows = executeUploadDataSql(queryString);
		
		return (rows != -1);
	}
	
	private boolean uploadGeneratorData(GeneratorData data) {
		
		String queryString = "replace into generator("
							+ "busnum,"
							+ "genmw,"
							+ "gen_mvr,"
							+ "gen_mvr_max,"
							+ "gen_mvr_min,"
							+ "gen_volt_set,"
							+ "gen_id,"
							+ "gen_status,"
							+ "gen_mw_max,"
							+ "gen_mw_min) values ("+
							+data.getBusNum()+","
							+data.getGenMW()+","
							+data.getGenMVR()+","
							+data.getGenMVRMax()+","
							+data.getGenMVRMin()+","
							+data.getGenVoltSet()+","
							+data.getGenId()+",'"
							+data.getGenStatus()+"',"
							+data.getGenMWMax()+","
							+data.getGenMWMin()+")";
		
		log.debug(queryString);
		int rows = executeUploadDataSql(queryString);
		
		return (rows != -1);		
		
	}
	
	private boolean uploadInterfacesViolation(InterfacesViolation data) {
		
		String queryString = "replace into interfaces_violation("
						+ "`timestamp`,"
						+ "interval_id,"
						+ "interface_id,"
						+ "probability, size, `limit`) values ('"
						+ data.getTimestamp()+"',"
						+ data.getIntervalId()+","
						+ data.getInterfaceId()+","
						+ data.getProbability()+","
						+ data.getSize()+","
						+ data.getLimit()+")";

		log.debug(queryString);
		int rows = executeUploadDataSql(queryString);
		
		return (rows != -1);				
	}
	
	private boolean uploadVoltageStabilityViolation(VoltageStabilityViolation data) {
		String queryString = "replace into voltage_stability_violation("
				+ "`timestamp`,"
				+ "interval_id,"
				+ "bus_id,"
				+ "probability, size, `limit`) values ('"
				+ data.getTimestamp()+"',"
				+ data.getIntervalId()+","
				+ data.getBusId()+","
				+ data.getProbability()+","
				+ data.getSize()+","
				+ data.getLimit()+")";
		
		log.debug(queryString);
		int rows = executeUploadDataSql(queryString);
		
		return (rows != -1);		
	}
	
	private boolean uploadRTEDScheduleData(RTEDScheduleData data) {
		String queryString = "replace into rte_d_total("
				+ "`timestamp`,"
				+ "gen,"
				+ "max,"
				+ "min, intervalid, zoneid) values ('"
				+ data.getTimestamp()+"',"
				+ data.getGenValue()+","
				+ data.getMaxValue()+","
				+ data.getMinValue()+","
				+ data.getInterval()+","
				+ data.getZoneID()+")";
		
		log.debug(queryString);
		int rows = executeUploadDataSql(queryString);
		
		return (rows != -1);		
	}
	
	
	private boolean uploadHAInterchangeScheduleData(HAInterchangeScheduleData data) {
		String queryString = "replace into ha_interchange_schedule("
				+ "`timestamp`,"
				+ "intervalid,"
				+ "`int`,"
				+ "zoneid) values ('"
				+ data.getTimestamps()+"',"
				+ data.getIntervalId()+","
				+ data.getValues()+","
				+ data.getZoneId()+")";
		
		log.debug(queryString);
		int rows = executeUploadDataSql(queryString);
		
		return (rows != -1);		
	}
}
