package pnnl.goss.fusiondb.launchers;

import org.apache.felix.dm.annotation.api.Component;
import org.apache.felix.dm.annotation.api.ServiceDependency;
import org.apache.felix.dm.annotation.api.Start;
import org.apache.felix.dm.annotation.api.Stop;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;

import pnnl.goss.core.Client;
import pnnl.goss.core.Client.PROTOCOL;
import pnnl.goss.core.ClientFactory;
import pnnl.goss.core.server.ServerControl;

@Component
public class TestLauncher {
	
	Client client = null; 
	
	@ServiceDependency
	private volatile ClientFactory clientFactory;
	
	@ServiceDependency
	private volatile ServerControl serverControl;
	
	@Start
	public void start(){
		try {
			System.out.println("*************** in start test launcher");
			Credentials credentials = new UsernamePasswordCredentials("system", "manager");
			client = clientFactory.create(PROTOCOL.STOMP,credentials);
			//run();
			System.out.println("*************** after run test launcher");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    	
    	
	    
	}
	
	@Stop
	public void stop(){
		
		clientFactory.destroy();
	}
	

}
