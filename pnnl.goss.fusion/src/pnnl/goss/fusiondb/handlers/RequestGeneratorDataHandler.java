package pnnl.goss.fusiondb.handlers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
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
import pnnl.goss.core.server.DataSourcePooledJdbc;
import pnnl.goss.core.server.DataSourceRegistry;
import pnnl.goss.core.server.RequestHandler;
import pnnl.goss.fusiondb.auth.FusionAuthHandler;
import pnnl.goss.fusiondb.datamodel.GeneratorData;
import pnnl.goss.fusiondb.requests.RequestActualTotal;
import pnnl.goss.fusiondb.requests.RequestGeneratorData;
import pnnl.goss.fusiondb.server.datasources.FusionDataSource;

@Component
public class RequestGeneratorDataHandler implements RequestHandler {

	private static final Logger log = LoggerFactory
			.getLogger(RequestGeneratorDataHandler.class);

	@ServiceDependency
	private volatile DataSourceRegistry dsRegistry;

	@Override
	public Map<Class<? extends Request>, Class<? extends AuthorizationHandler>> getHandles() {
		Map<Class<? extends Request>, Class<? extends AuthorizationHandler>> auths = new HashMap<>();

		auths.put(RequestActualTotal.class, FusionAuthHandler.class);

		return auths;
	}

	public DataResponse handle(Request request) {

		DataResponse response = new DataResponse();

		DataSourcePooledJdbc ds = (DataSourcePooledJdbc) dsRegistry
				.get(FusionDataSource.class.getName());

		try (Connection connection = ds.getConnection()) {

			GeneratorData data = null;
			RequestGeneratorData request1 = (RequestGeneratorData) request;
			try (Statement stmt = connection.createStatement()) {
				ResultSet rs = null;

				String query = "select * from generator where busnum="
						+ request1.getBusNum() + " and gen_id="
						+ request1.getGenId();

				log.debug("QUERY: "+query);
				rs = stmt.executeQuery(query);

				if (rs.next()) {
					int busNum = rs.getInt("busnum");
					double genMW = rs.getDouble("genmw");
					double genMVR = rs.getDouble("gen_mvr");
					double genMVRMax = rs.getDouble("gen_mvr_max");
					double genMVRMin = rs.getDouble("gen_mvr_min");
					double genVoltSet = rs.getDouble("gen_volt_set");
					String genId = rs.getString("gen_id");
					String genStatus = rs.getString("gen_status");
					double genMWMax = rs.getDouble("gen_mw_max");
					double genMWMin = rs.getDouble("gen_mw_min");
					data = new GeneratorData(busNum, genMW, genMVR, genMVRMax,
							genMVRMin, genVoltSet, genId, genStatus, genMWMax,
							genMWMin);
				}

				response.setData(data);

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
