/*
	Copyright (c) 2014, Battelle Memorial Institute
    All rights reserved.
    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:
    1. Redistributions of source code must retain the above copyright notice, this
    list of conditions and the following disclaimer.
    2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.
    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
    ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
    WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE

    DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
    ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
    (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
    LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
    ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
    SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
    The views and conclusions contained in the software and documentation are those
    of the authors and should not be interpreted as representing official policies,
    either expressed or implied, of the FreeBSD Project.
    This material was prepared as an account of work sponsored by an
    agency of the United States Government. Neither the United States
    Government nor the United States Department of Energy, nor Battelle,
    nor any of their employees, nor any jurisdiction or organization
    that has cooperated in the development of these materials, makes
    any warranty, express or implied, or assumes any legal liability
    or responsibility for the accuracy, completeness, or usefulness or
    any information, apparatus, product, software, or process disclosed,
    or represents that its use would not infringe privately owned rights.
    Reference herein to any specific commercial product, process, or
    service by trade name, trademark, manufacturer, or otherwise does
    not necessarily constitute or imply its endorsement, recommendation,
    or favoring by the United States Government or any agency thereof,
    or Battelle Memorial Institute. The views and opinions of authors
    expressed herein do not necessarily state or reflect those of the
    United States Government or any agency thereof.
    PACIFIC NORTHWEST NATIONAL LABORATORY
    operated by BATTELLE for the UNITED STATES DEPARTMENT OF ENERGY
    under Contract DE-AC05-76RL01830
*/
package pnnl.goss.fusiondb.server.datasources;

import java.net.URI;
import java.util.Dictionary;
import java.util.Properties;

import javax.naming.ConfigurationException;

import org.apache.felix.dm.annotation.api.Component;
import org.apache.felix.dm.annotation.api.ConfigurationDependency;
import org.apache.felix.dm.annotation.api.ServiceDependency;
import org.apache.felix.dm.annotation.api.Start;
import org.apache.felix.dm.annotation.api.Stop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.core.server.DataSourceBuilder;
import pnnl.goss.core.server.DataSourceRegistry;

@Component
public class FusionDataSource {

	private static Logger log = LoggerFactory.getLogger(FusionDataSource.class);

	private static final String CONFIG_PID = "pnnl.goss.fusion";
	
	@ServiceDependency
	private volatile DataSourceBuilder builder;
	
	@ServiceDependency
	private volatile DataSourceRegistry registry;

	private String PROP_URI = "db.uri";
	private String PROP_USERNAME = "db.username";
	private String PROP_PASSWORD = "db.password";
	private String PROP_DRIVER = "db.driver";
	private URI dbUri;
	private String dbUser;
	private String dbPass;
	private String dbDriver;

	private boolean nullOrEmpty(String data){
		return (data == null || data.isEmpty());
	}

	@ConfigurationDependency(pid=CONFIG_PID)
	private void update(Dictionary<String, ?> properties) throws ConfigurationException {

		if (properties != null){
			String invalidMessage = "";

			if(nullOrEmpty((String)properties.get(PROP_USERNAME))) {
				invalidMessage += PROP_USERNAME +" must be specified in config file.";
			}
			if(nullOrEmpty((String)properties.get(PROP_PASSWORD))){
				invalidMessage += PROP_PASSWORD +" must be specified in config file.";
			}
			if(nullOrEmpty((String)properties.get(PROP_URI))){
				invalidMessage += PROP_URI +" must be specified in config file.";
			}
			if(!nullOrEmpty((String)properties.get(PROP_DRIVER))) {
				dbDriver = (String)properties.get(PROP_DRIVER);
			}
			else{
				dbDriver = "com.mysql.jdbc.Driver";
			}

			if (!nullOrEmpty(invalidMessage)){
				throw new ConfigurationException(invalidMessage);
			}

			dbUri = URI.create((String)properties.get(PROP_URI));
			dbUser = (String)properties.get(PROP_USERNAME);
			dbPass = (String)properties.get(PROP_PASSWORD);
			System.out.println("CONFIGURED: "+ dbUri);
			log.debug("Updated configurations");

		}
	}

	@Start
	public void start() throws Exception {
		System.out.println("STARTING DATASOURCE: " + dbUri.toString());
		
		Properties properties = new Properties();
		properties.setProperty(DataSourceBuilder.DATASOURCE_NAME, this.getClass().getName());
		properties.setProperty(DataSourceBuilder.DATASOURCE_URL, dbUri.toString());
		properties.setProperty(DataSourceBuilder.DATASOURCE_USER, dbUser);
		properties.setProperty(DataSourceBuilder.DATASOURCE_PASSWORD, dbPass);
		properties.setProperty(DataSourceBuilder.DATASOURCE_DRIVER, dbDriver);
		// Add other specific properties that shoudl be on the object.
		builder.create(this.getClass().getName(), properties);
		
		//System.out.println("factory is? "+factory);
	}
	
	@Stop
	public void stop(){
		registry.remove(this.getClass().getName());
	}

//	@Override
//	public Connection getConnection() throws SQLException {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public String getName() {
//		return this.getClass().getName();
//	}
//
//	@Override
//	public DataSourceType getDataSourceType() {
//		return null;
//	}

//	public void resetInstance(){
//		System.out.println("Resetting GridmwMappingDatasource Instance");
//		if(connectionPool!=null){
//			try {
//				connectionPool.close();
//			} catch (SQLException e) {
//				System.err.println("Error closing gridmw datasource connection");
//			}
//			connectionPool = null;
//		}
//	}
//
//	public Connection getConnection(){
//		try{
//			if (connectionPool == null){
//				connectionPool = getDataSourceConnection(dbUri.toString(), dbUser, dbPass, null);
//			}
//
//			return connectionPool.getConnection();
//		}
//		catch(SQLException e){
//			e.printStackTrace();
//			return null;
//		} catch (Exception e) {
//			log.error("Error creating connection pool", e);
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return null;
//
//	}
//
//	/**
//	 * <p>
//	 * Adds a poolable connection using the passed parameters to connect to the datasource.
//	 * </p>
//	 */
//	private BasicDataSource getDataSourceConnection(String url, String username, String password, String driver) throws Exception {
//		Properties properties = new Properties();
//
//		// Available properties http://commons.apache.org/proper/commons-dbcp/xref-test/org/apache/commons/dbcp/TestBasicDataSourceFactory.html#50
//		if (driver == null || driver.trim().equals("")){
//			properties.setProperty("driverClassName", "com.mysql.jdbc.Driver");
//		}
//		else{
//			properties.setProperty("driverClassName", driver);
//		}
//
//		Class.forName(properties.getProperty("driverClassName"));
//
//		properties.setProperty("url", url);
//		properties.setProperty("username", username);
//		properties.setProperty("password", password);
//
//		properties.setProperty("maxOpenPreparedStatements", "10");
//
//		System.out.println("Connecting datasource to url: "+url+" with user: "+username);
//
//		return (BasicDataSource)BasicDataSourceFactory.createDataSource(properties);
//
//	}
//
//	@Override
//	public DataSourceType getDataSourceType() {
//		return DataSourceType.DS_TYPE_JDBC;
//	}
//
//	@Override
//	public void onRemoved() {
//		resetInstance();
//	}
//
//	@Stop
//	public void stop(){
//		resetInstance();
//	}


}
