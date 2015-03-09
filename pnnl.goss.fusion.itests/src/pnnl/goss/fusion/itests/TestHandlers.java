package pnnl.goss.fusion.itests;

import static org.amdatu.testing.configurator.TestConfigurator.cleanUp;
import static org.amdatu.testing.configurator.TestConfigurator.configuration;
import static org.amdatu.testing.configurator.TestConfigurator.configure;
import static org.amdatu.testing.configurator.TestConfigurator.serviceDependency;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import org.amdatu.testing.configurator.TestConfiguration;
import org.apache.shiro.mgt.SecurityManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.jdbc.DataSourceFactory;

import pnnl.goss.core.ClientFactory;
import pnnl.goss.core.server.DataSourceObject;
import pnnl.goss.core.server.DataSourcePooledJdbc;
import pnnl.goss.core.server.DataSourceRegistry;
import pnnl.goss.core.server.DataSourceType;
import pnnl.goss.core.server.ServerControl;

public class TestHandlers {

	TestConfiguration testConfig;
	private volatile DataSourceRegistry dsRegistry;
	private volatile DataSourceObject dsObject;

	// If this were in a regular Component we would do this.
	//@ServiceDependency(name="org.h2.util.OsgiDataSourceFactory")
	//private volatile DataSourceFactory factory;
	private BundleContext context = FrameworkUtil.getBundle(TestHandlers.class).getBundleContext();

	@Before
	public void before() throws InterruptedException{
		testConfig = configure(this)
				.add(configuration("pnnl.goss.core.server")
					.set("goss.openwire.uri", "tcp://localhost:6000")
					.set("goss.stomp.uri",  "tcp://localhost:6001") //vm:(broker:(tcp://localhost:6001)?persistent=false)?marshal=false")
					.set("goss.start.broker", "true")
					.set("goss.broker.uri", "tcp://localhost:6000"))
				.add(configuration(ClientFactory.CONFIG_PID)
					.set("goss.openwire.uri", "tcp://localhost:6000")
					.set("goss.stomp.uri",  "tcp://localhost:6001"))
//				.add(configuration("pnnl.goss.fusion")
//					.set("db.uri", "jdbc:h2:mem:")
//					.set("db.username", "sa")
//					.set("db.password", "sa"))

				.add(serviceDependency(ServerControl.class))
				.add(serviceDependency(ClientFactory.class))
				.add(serviceDependency(SecurityManager.class))

				.add(serviceDependency(DataSourceFactory.class, "(name=org.h2.util.OsgiDataSourceFactory)"))
				//.add(serviceDependency(DataSourceObject.class))
				.add(serviceDependency(DataSourceRegistry.class).setRequired(true));

		testConfig.apply();
		// Configuration update is asyncronous, so give a bit of time to catch up
		TimeUnit.MILLISECONDS.sleep(500);
		DataSourcePooledJdbc ds = (DataSourcePooledJdbc) dsRegistry.get("pnnl.goss.fusiondb.server.datasources.FusionDataSource");

		try(Connection conn = ds.getConnection()){
			TestUtils.setupDatabase(context, conn);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testDataSourceSetProperly(){
		assertNotNull(dsRegistry);
		assertTrue(dsRegistry.getAvailable().size()> 0);
		assertNotNull(dsObject);
		assertEquals(DataSourceType.DS_TYPE_JDBC, dsObject.getDataSourceType());
	}

	@After
	public void after(){
		try {
			//if (serverControl != null) {serverControl.stop();}
			cleanUp(this);
		}
		catch (Exception e) {
			System.err.println("Ignoring exception!");
		}
		finally {
//			if (clientFactory != null){
//				clientFactory.destroy();
//			}
		}
	}

}
