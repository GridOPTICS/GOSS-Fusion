package pnnl.goss.fusiondb.auth;

import java.util.Set;

import org.apache.felix.dm.annotation.api.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.core.Request;
import pnnl.goss.core.security.AuthorizationHandler;

@Component
public class FusionAuthHandler implements AuthorizationHandler {
	private static final Logger log = LoggerFactory.getLogger(FusionAuthHandler.class);

	
	@Override
	public boolean isAuthorized(Request request, Set<String> permissions) {
		String typeStr = request.getClass().getSimpleName();
		
		for(String perm: permissions){
			if(perm.startsWith("fusion")){
				String[] permArr = perm.split(":");
				if(permArr.length<3){
					log.error("Invalid permission specification "+perm);
				}
				String type = permArr[1];
				String allowed = permArr[2];
				if("*".equals(type) || typeStr.equals(type)){
					if("*".equals(allowed) || "read".equals(allowed)){
						return true;
					}
				}
			}
			
		}
		return false;
	}

}
