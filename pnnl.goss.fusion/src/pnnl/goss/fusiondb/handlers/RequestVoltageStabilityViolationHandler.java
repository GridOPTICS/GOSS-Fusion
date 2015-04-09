package pnnl.goss.fusiondb.handlers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.felix.dm.annotation.api.Component;
import org.apache.felix.dm.annotation.api.ServiceDependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.core.DataError;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Request;
import pnnl.goss.core.security.AuthorizationHandler;
import pnnl.goss.core.security.AuthorizeAll;
import pnnl.goss.core.server.DataSourcePooledJdbc;
import pnnl.goss.core.server.DataSourceRegistry;
import pnnl.goss.core.server.RequestHandler;
import pnnl.goss.fusiondb.datamodel.VoltageStabilityViolation;
import pnnl.goss.fusiondb.requests.RequestActualTotal;
import pnnl.goss.fusiondb.requests.RequestVoltageStabilityViolation;
import pnnl.goss.fusiondb.server.datasources.FusionDataSource;

@Component
public class RequestVoltageStabilityViolationHandler implements RequestHandler {

	private static final Logger log = LoggerFactory
			.getLogger(RequestVoltageStabilityViolationHandler.class);

	@ServiceDependency
	private volatile DataSourceRegistry dsRegistry;

	@Override
	public Map<Class<? extends Request>, Class<? extends AuthorizationHandler>> getHandles() {
		Map<Class<? extends Request>, Class<? extends AuthorizationHandler>> auths = new HashMap<>();

		auths.put(RequestActualTotal.class, AuthorizeAll.class);

		return auths;
	}

	public DataResponse handle(Request request) {
		DataResponse response = new DataResponse();

		DataSourcePooledJdbc ds = (DataSourcePooledJdbc) dsRegistry
				.get(FusionDataSource.class.getName());

		try (Connection connection = ds.getConnection()) {
			RequestVoltageStabilityViolation request1 = (RequestVoltageStabilityViolation) request;
			
			try (Statement stmt = connection.createStatement()) {

				ResultSet rs = null;
				String query = null;

				if (request1.getIntervalId() != 0)
					query = "select * from voltage_stability_violation where `timestamp` = '"
							+ request1.getTimestamp()
							+ "' and interval_id = "
							+ request1.getIntervalId();
				else
					query = "select * from voltage_stability_violation where `timestamp` = '"
							+ request1.getTimestamp()
							+ "' order by interval_id";

				log.debug(query);
				rs = stmt.executeQuery(query);

				ArrayList<VoltageStabilityViolation> list = new ArrayList<VoltageStabilityViolation>();
				VoltageStabilityViolation voltageStabilityViolation = null;
				while (rs.next()) {
					String timestamp = rs.getString("timestamp");
					int intervalId = rs.getInt("interval_id");
					int busId = rs.getInt("bus_id");
					double probability = rs.getDouble("probability");
					voltageStabilityViolation = new VoltageStabilityViolation(
							timestamp, intervalId, busId, probability);
					list.add(voltageStabilityViolation);
				}

				response.setData(list);
				connection.close();
			}

		} catch (Exception e) {

			e.printStackTrace();
			DataError error = new DataError(e.getMessage());
			response.setData(error);
			return response;
		}
		return response;
	}

}
