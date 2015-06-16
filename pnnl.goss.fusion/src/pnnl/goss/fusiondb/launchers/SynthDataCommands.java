

//import pnnl.goss.core.server.ServerControl;

/*package pnnl.goss.fusiondb.launchers;

import java.io.Serializable;

import org.apache.felix.dm.annotation.api.Component;
import org.apache.felix.dm.annotation.api.ServiceDependency;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;

import pnnl.goss.core.Client;
import pnnl.goss.core.Client.PROTOCOL;
import pnnl.goss.core.GossResponseEvent;
import pnnl.goss.core.client.ClientServiceFactory;

@Component
public class SynthDataCommands {
	
	@ServiceDependency
	private volatile ClientServiceFactory factory;
	Client client;
	
	@ServiceDependency
	private volatile ServerControl serverControl;
    
	public SynthDataCommands(){v
	
		try {
			System.out.println("*************** in test");
			Credentials credentials = new UsernamePasswordCredentials("system", "manager"); 
			client = factory.create(PROTOCOL.STOMP, credentials);
			run();
			System.out.println("*************** after run test");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public  void run(){
		System.out.println("*************** in run test");
		
		
		Thread thread1 = new Thread(new Runnable() {
			
			@Override
			public void run() {
				SynthDataCommands command = new SynthDataCommands();
				GossResponseEvent event = new GossResponseEvent(){
					@Override
					public void onMessage(Serializable message) {
						System.out.println(message);
						String reply = "12";
						command.client.publish("goss/fusion/viz/interfaces_violation", reply);
					}
				};
				
				command.client.subscribeTo("/topic/goss/fusion/viz/data/control",event);
				
			}
		});
		thread1.start();
		
	}
	
	
	
}*/