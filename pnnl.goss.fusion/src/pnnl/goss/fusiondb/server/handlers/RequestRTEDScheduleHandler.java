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
package pnnl.goss.fusiondb.server.handlers;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.dm.annotation.api.Component;
import org.apache.felix.dm.annotation.api.ServiceDependency;

import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Request;
import pnnl.goss.core.security.AuthorizationHandler;
import pnnl.goss.core.server.DataSourceRegistry;
import pnnl.goss.core.server.RequestHandler;
import pnnl.goss.fusiondb.datamodel.RTEDSchedule;
import pnnl.goss.fusiondb.requests.RequestRTEDSchedule;
import pnnl.goss.fusiondb.server.datasources.FusionDataSource;

@Component
public class RequestRTEDScheduleHandler implements RequestHandler {

	@ServiceDependency
	private volatile DataSourceRegistry dsRegistry;
	
	@Override
	public Map<Class<? extends Request>, Class<? extends AuthorizationHandler>> getHandles() {
		Map<Class<? extends Request>, Class<? extends AuthorizationHandler>> auths = new HashMap<>();
		
		auths.put(RequestRTEDSchedule.class, AuthorizeAll.class);
		
		return auths;
	}
	
	public DataResponse handle(Request request) {

		Serializable data = null;

		try {
			String dbQuery = "";
			FusionDataSource ds = (FusionDataSource)dsRegistry.get(FusionDataSource.class.getName());
			Connection connection = ds.getConnection();
			Statement stmt = connection.createStatement();
			ResultSet rs = null;
			
			RequestRTEDSchedule request1 = (RequestRTEDSchedule) request;
			
			if (request1.getEndTimeStamp() == null) {
				dbQuery = "select * from fusion.rte_d_total where `TimeStamp` = '"+request1.getStartTimestamp()+"' order by IntervalID";
			} else {

				dbQuery = "select * from fusion.rte_d_total where `TimeStamp` between '"+request1.getStartTimestamp()+"' and '"+request1.getEndTimeStamp()+"' and "+
						"IntervalID <= "+request1.getInterval()+" order by IntervalID";
			}

			System.out.println(dbQuery);
			rs = stmt.executeQuery(dbQuery);
			
			List<String> timestampsList = new ArrayList<String>();
			List<Integer> intervalList = new ArrayList<Integer>();
			List<Double> genList = new ArrayList<Double>();
			List<Double> minList = new ArrayList<Double>();
			List<Double> maxList = new ArrayList<Double>();
			
			while (rs.next()) {
				timestampsList.add(rs.getString(1));
				intervalList.add(rs.getInt(2));
				genList.add(rs.getDouble(3));
				minList.add(rs.getDouble(4));
				maxList.add(rs.getDouble(5));
			}

			RTEDSchedule rtedSchedule = new RTEDSchedule();
			rtedSchedule.setTimestamps(timestampsList.toArray(new String[timestampsList.size()]));
			rtedSchedule.setIntervals(intervalList.toArray(new Integer[intervalList.size()]));
			rtedSchedule.setGenValues(genList.toArray(new Double[genList.size()]));
			rtedSchedule.setMinValues(minList.toArray(new Double[minList.size()]));
			rtedSchedule.setMaxValues(maxList.toArray(new Double[maxList.size()]));
			
			connection.close();
			data = rtedSchedule;
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		DataResponse dataResponse = new DataResponse();
		dataResponse.setData(data);
		return dataResponse;

	}


}
