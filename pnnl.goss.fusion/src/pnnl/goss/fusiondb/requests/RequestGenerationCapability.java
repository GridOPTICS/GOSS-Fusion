package pnnl.goss.fusiondb.requests;

import java.io.Serializable;

public class RequestGenerationCapability implements Serializable{
	
	private static final long serialVersionUID = -596729748650332129L;
	
	String username;
	String fileContent;
	String timestamp;
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getFileContent() {
		return fileContent;
	}
	public void setFileContent(String fileContent) {
		this.fileContent = fileContent;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	
	
	
	
}
