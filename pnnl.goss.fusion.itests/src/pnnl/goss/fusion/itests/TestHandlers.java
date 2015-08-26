/*package pnnl.goss.fusion.itests;

import static org.amdatu.testing.configurator.TestConfigurator.cleanUp;
import static org.amdatu.testing.configurator.TestConfigurator.configuration;
import static org.amdatu.testing.configurator.TestConfigurator.configure;
import static org.amdatu.testing.configurator.TestConfigurator.serviceDependency;
import static org.junit.Assert.assertNotNull;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import org.amdatu.testing.configurator.TestConfiguration;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.shiro.mgt.SecurityManager;
import org.junit.After;
import org.junit.Before;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import pnnl.goss.core.Client;
import pnnl.goss.core.Client.PROTOCOL;
import pnnl.goss.core.ClientFactory;
import pnnl.goss.core.server.DataSourceBuilder;
import pnnl.goss.core.server.DataSourcePooledJdbc;
import pnnl.goss.core.server.DataSourceRegistry;
import pnnl.goss.core.server.RequestHandlerRegistry;

public class TestHandlers {

	TestConfiguration testConfig;
	private volatile DataSourceRegistry dsRegistry;
	private volatile ClientFactory clientFactory;
	
	private Client client;
	
	// If this were in a regular Component we would do this.
	//@ServiceDependency(name="org.h2.util.OsgiDataSourceFactory")
	//private volatile DataSourceFactory factory;
	private BundleContext context = FrameworkUtil.getBundle(TestHandlers.class).getBundleContext();

	@Before
	public void before() throws InterruptedException, SQLException{
		testConfig = configure(this)
				.add(configuration("pnnl.goss.core.server")
					.set("goss.openwire.uri", "tcp://localhost:6000")
					.set("goss.stomp.uri",  "tcp://localhost:6001")
					.set("goss.start.broker", "true")
					.set("goss.broker.uri", "tcp://localhost:6000"))
				.add(configuration(ClientFactory.CONFIG_PID)
					.set("goss.openwire.uri", "tcp://localhost:6000")
					.set("goss.stomp.uri",  "tcp://localhost:6001"))
				.add(configuration("pnnl.goss.fusion")
					.set("db.uri", "jdbc:h2:mem:")
					.set("db.username", "sa")
					.set("db.password", "sa")
					.set("db.driver", "org.h2.Driver"))
				//.add(serviceDependency(ServerControl.class))
				.add(serviceDependency(ClientFactory.class))
				.add(serviceDependency(SecurityManager.class))
				//.add(serviceDependency(GossRealm.class)) //.setDefaultImplementation(new BasicFakeRealm()))  // Should require BasicFakeRealm
				.add(serviceDependency(DataSourceBuilder.class))
				.add(serviceDependency(RequestHandlerRegistry.class))
				.add(serviceDependency(DataSourceRegistry.class).setRequired(true));

		testConfig.apply();
		// Configuration update is asyncronous, so give a bit of time to catch up
		TimeUnit.MILLISECONDS.sleep(1000);
		
		// Setup the database here.
		TestUtils.setupDatabase(context, getConnection().getConnection());
		
		assertNotNull(clientFactory);
		// After setting credentials the client should be able to send requests.
		
		try {
			Credentials credentials = new UsernamePasswordCredentials("user1", "123");
			client = clientFactory.create(PROTOCOL.OPENWIRE, credentials);
		} catch (Exception e) {
			e.printStackTrace();
		}
//		DataSourcePooledJdbc ds = (DataSourcePooledJdbc) dsRegistry.get("pnnl.goss.fusiondb.server.datasources.FusionDataSource");
//
//		try(Connection conn = ds.getConnection()){
//			TestUtils.setupDatabase(context, conn);
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			fail();
//		}
	}
	
	private DataSourcePooledJdbc getConnection(){
		return (DataSourcePooledJdbc)dsRegistry.get("pnnl.goss.fusiondb.server.datasources.FusionDataSource");
	}

//	@Test
//	public void testDataSourceWorks(){
//		assertNotNull(dsRegistry);
//		DataSourcePooledJdbc pooled = getConnection();
//		assertNotNull(pooled);
////		assertTrue(dsRegistry.getAvailable().size()> 0);
////		assertNotNull(dsObject);
////		assertEquals(DataSourceType.DS_TYPE_JDBC, dsObject.getDataSourceType());
//	}
	
//	@Test
//	public void testGetCapacity(){
//		RequestCapacityRequirement req = new RequestCapacityRequirement("2013-01-21 00:06:00");
//		Response resp = client.getResponse(req);
//		assertNotNull(resp);
//		assertTrue("DataRespons it wasn't", resp instanceof DataResponse);
//		if ((((DataResponse)resp).getData() instanceof DataError)){
//			DataError err = (DataError)((DataResponse)resp).getData();
//			System.out.println("Error Message thrown: "+ err.getMessage());
//			fail("A DataError was thrown on server");
//		}
//		
//		
//	}
	
	

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
*/