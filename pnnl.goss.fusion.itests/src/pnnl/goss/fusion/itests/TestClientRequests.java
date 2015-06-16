package pnnl.goss.fusion.itests;
//
import static org.amdatu.testing.configurator.TestConfigurator.configure;
import static org.amdatu.testing.configurator.TestConfigurator.serviceDependency;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.TimeUnit;

//
import org.amdatu.testing.configurator.TestConfiguration;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
//import org.apache.shiro.authc.AuthenticationException;
//import org.apache.shiro.authc.AuthenticationInfo;
//import org.apache.shiro.authc.AuthenticationToken;
//import org.apache.shiro.authc.SimpleAccount;
//import org.apache.shiro.realm.AuthenticatingRealm;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import pnnl.goss.core.Client;
import pnnl.goss.core.Client.PROTOCOL;
import pnnl.goss.core.ClientFactory;
import pnnl.goss.core.DataError;
import pnnl.goss.core.DataResponse;
import pnnl.goss.core.Response;
import pnnl.goss.core.server.ServerControl;
import pnnl.goss.fusiondb.requests.RequestActualTotal;
import pnnl.goss.fusiondb.requests.RequestActualTotal.Type;
import pnnl.goss.fusiondb.requests.RequestCapacityRequirement;

public class TestClientRequests {
	
//	private static final Logger log = LoggerFactory.getLogger(TestClientRequests.class);
	
	private GossIntegrationTestSupport support;
	private TestConfiguration testConfig;
//	private volatile DataSourceRegistry dsRegistry;
	private volatile ClientFactory clientFactory;
	private volatile ServerControl serverControl;


//	
	@Before
	public void before() throws InterruptedException{
		support = new GossIntegrationTestSupport();
//		testConfig = support.startConfiguration();
//		testConfig.apply();
		
		testConfig = configure(this)
				.add(TestSteps.configureServerAndClientPropertiesConfig())
				.add(serviceDependency().setService(SecurityManager.class))
				.add(serviceDependency().setService(ServerControl.class))
				.add(serviceDependency().setService(ClientFactory.class));
		testConfig.apply();
		
		// Configuration update is asyncronous, so give a bit of time to catch up
		TimeUnit.MILLISECONDS.sleep(500);
	}
	
	@After
	public void after(){
		support.cleanupServer();
	}
//	
	@Test
	public void testRequestActualTotalHandler() {
		try{
			
			Credentials credentials = new UsernamePasswordCredentials("user1", "123");
			Client client = clientFactory.create(PROTOCOL.OPENWIRE,credentials);
			System.out.println("Client set creds created");
			RequestActualTotal req = new RequestActualTotal(Type.LOAD, "2015-01-01");
			System.out.println("Client Created request");
			Response response = client.getResponse(req);
			System.out.println("Client Sent request to server");
			assertNotNull(response);
			System.out.println("Response wasn't null");
			assertTrue(response instanceof DataResponse);
			System.out.println("Response was a DataResponse obj");
			DataResponse dataResponse = (DataResponse)response;
			//TODO
	//		assertEquals(message, dataResponse.getData().toString());
			System.out.println("The message was correct");
			System.out.println("TEST_END: clientCanGetEcho");
	//		assertNotNull(support.getServerControl());
	//		assertNotNull(support.getTestConfig());
	//		assertNotNull(support.getClientFactory());
	//		assertNotNull(support.getDSRegistry());
	//		assertTrue(support.getDSRegistry().getAvailable().size() > 0);
	//		for(String k: support.getDSRegistry().getAvailable().keySet()){
	//			System.out.println(k);
	//		}
			//assertNotNull(clientFactory);
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
//
//	
////	@Test
////	public void testCapacityRequirements(){
////		fail();
////	}
//
	
	@Test
	public void testGetCapacity(){
		try{
			Credentials credentials = new UsernamePasswordCredentials("user1", "123");
			Client client = clientFactory.create(PROTOCOL.OPENWIRE, credentials);
			RequestCapacityRequirement req = new RequestCapacityRequirement("2013-01-21 00:06:00");
			Response resp = client.getResponse(req);
			assertNotNull(resp);
			assertTrue("DataRespons it wasn't", resp instanceof DataResponse);
			if ((((DataResponse)resp).getData() instanceof DataError)){
				DataError err = (DataError)((DataResponse)resp).getData();
				System.out.println("Error Message thrown: "+ err.getMessage());
				fail("A DataError was thrown on server");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
	}
	
}
