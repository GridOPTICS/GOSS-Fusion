package pnnl.goss.fusion.itests;

import static org.amdatu.testing.configurator.TestConfigurator.cleanUp;
import static org.amdatu.testing.configurator.TestConfigurator.configuration;
import static org.amdatu.testing.configurator.TestConfigurator.configure;
import static org.amdatu.testing.configurator.TestConfigurator.serviceDependency;








import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.amdatu.testing.configurator.TestConfiguration;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAccount;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthenticatingRealm;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import pnnl.goss.core.ClientFactory;
import pnnl.goss.core.security.GossRealm;
import pnnl.goss.core.server.ServerControl;

public class GossIntegrationTestSupport {
	
	private final BasicFakeRealm gossRealm = new BasicFakeRealm();
	private TestConfiguration testConfig;
	private volatile ClientFactory clientFactory;
	private volatile ServerControl serverControl;
	
	
	public static final String OPENWIRE_CLIENT_CONNECTION = "tcp://localhost:6000";
	public static final String STOMP_CLIENT_CONNECTION = "tcp://localhost:6000";
	
	public class BasicFakeRealm extends AuthorizingRealm implements GossRealm {
		
		private final Map<String, SimpleAccount> builtAccounts = new ConcurrentHashMap<>();
		
		public BasicFakeRealm() {
			SimpleAccount acnt = new SimpleAccount("system", "manager", getName());
			acnt.addStringPermission("*");
			builtAccounts.put("system", acnt);
			
			acnt = new SimpleAccount("reader", "reader", getName());
			acnt.addStringPermission("topic:*");
			acnt.addStringPermission("queue:*");
			acnt.addStringPermission("temp-queu:*");
			
			builtAccounts.put("reader", acnt);
			
			acnt = new SimpleAccount("writer", "writer", getName());
			acnt.addStringPermission("topic:*");
			acnt.addStringPermission("queue:*");
			acnt.addStringPermission("temp-queu:*");
			
			builtAccounts.put("writer", acnt);
		}
		
		public SimpleAccount getAccount(String username){
			return builtAccounts.get(username);
		}

		@Override
		protected AuthenticationInfo doGetAuthenticationInfo(
				AuthenticationToken token) throws AuthenticationException {
			//we can safely cast to a UsernamePasswordToken here, because this class 'supports' UsernamePasswordToken
	        //objects.  See the Realm.supports() method if your application will use a different type of token.
	        UsernamePasswordToken upToken = (UsernamePasswordToken) token;
	        
	        String uname = upToken.getUsername();
	        if(builtAccounts.containsKey(uname)){
	        	return builtAccounts.get(uname);
	        }
	        return null;
		}

		@Override
		public Set<String> getPermissions(String identifier) {
			Set<String> hashSet = new HashSet<>();
			if (builtAccounts.containsKey(identifier)){
				hashSet.addAll(builtAccounts.get(identifier).getStringPermissions());
			}
			
			return hashSet;
		}

		@Override
		public boolean hasIdentifier(String identifier) {
			return builtAccounts.containsKey(identifier);
		}

		@Override
		protected AuthorizationInfo doGetAuthorizationInfo(
				PrincipalCollection principals) {
			//get the principal this realm cares about:
	        String username = (String) getAvailablePrincipal(principals);
	        if (username != null){
	        	return builtAccounts.get(username);
	        }
	        return null;
		}
		
	}
	
	
	public BasicFakeRealm getFakeRealm() {
		return gossRealm;
	}
	
	public TestConfiguration getTestConfig() {
		return testConfig;
	}

	public ClientFactory getClientFactory() {
		return clientFactory;
	}

	public ServerControl getServerControl() {
		return serverControl;
	}

	public TestConfiguration startConfiguration() throws InterruptedException{
		return startConfiguration(true);
	}
	
	public TestConfiguration startConfiguration(boolean applyConfiguration) throws InterruptedException{
		testConfig = configure(this)
						.add(configuration("pnnl.goss.core.server")
								.set("goss.openwire.uri", "tcp://localhost:6000")
								.set("goss.stomp.uri",  "tcp://localhost:6001") //vm:(broker:(tcp://localhost:6001)?persistent=false)?marshal=false")
								.set("goss.start.broker", "true")
								.set("goss.broker.uri", "tcp://localhost:6000"))
						.add(serviceDependency(ServerControl.class))
						.add(configuration(ClientFactory.CONFIG_PID)
								.set("goss.openwire.uri", "tcp://localhost:6000")
								.set("goss.stomp.uri",  "tcp://localhost:6001"))
						.add(serviceDependency(ClientFactory.class))
						.add(serviceDependency(SecurityManager.class));
		
		if (applyConfiguration){
			testConfig.apply();
			
			// Configuration update is asyncronous, so give a bit of time to catch up
			TimeUnit.MILLISECONDS.sleep(500);
		}
		
		return testConfig;
	}
	
	public void cleanupServer(){
		try {
			if (serverControl != null) {serverControl.stop();}
			cleanUp(this);
		}
		catch (Exception e) {
			System.err.println("Ignoring exception!");
		}
		finally {
			if (clientFactory != null){
				clientFactory.destroy();
			}
		}
	}

}
