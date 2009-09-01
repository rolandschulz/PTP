/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.ptp.rdt.core.tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.ptp.services.core.ServiceModelManager;

/**
 * Manages the connection to the RDT remote server that will
 * be used to run the tests.
 * 
 * @author Mike Kucera
 */
public class ConnectionManager {

	public static final String PROPERTY_CONNECTION_CLASS = "connection.class";
	public static final String PROPERTY_WORKSPACE    = "project.workspace";
	public static final String PROPERTY_SERVICE_MODEL_CONFIG_FILE = "project.servicemodel";
	
	
	private static ConnectionManager instance;

	
	private IRDTTestConnection connection;
	private Properties properties;
	private File serviceModelConfigFile;
	
	
	private ConnectionManager() {}
	
	public static synchronized ConnectionManager getInstance() {
		if(instance == null)
			instance = new ConnectionManager();
		return instance;
	}
	
	
	/**
	 * Loads the properties file, and stores the name of the service model
	 * configuration file.
	 * @throws IOException if there is a problem reading the properties file
	 */
	public void initialize(File propertyFile, File serviceModelFile) throws IOException {
		properties = new Properties();		
		properties.load(new FileInputStream(propertyFile));
		
		this.serviceModelConfigFile = serviceModelFile;
	}
	
	public boolean isConnected() {
		return properties != null 
		    && connection != null 
		    && connection.isConnected();
	}
	
	
	/**
	 * Establishes the connection to the remote RDT server.
	 */
	public void connect() throws ConnectException {
		if(isConnected())
			throw new ConnectException("Already connected");
		if(properties == null)
			throw new ConnectException(PROPERTY_CONNECTION_CLASS + " property is missing");
		
		String connectionClass = properties.getProperty(PROPERTY_CONNECTION_CLASS);
		if(connectionClass == null)
			throw new ConnectException(PROPERTY_CONNECTION_CLASS + " property is missing");
		
		try {
			Class<?> klass = Class.forName(connectionClass);
			connection = (IRDTTestConnection) klass.newInstance();
		} catch(Exception e) {
			throw new ConnectException(e);
		}
		connection.connect(properties);
	}
	
	
	public void disconnect() throws ConnectException {
		try {
			connection.disconnect();
		} catch(ConnectException e) {
			throw e;
		} finally {
			connection = null;
			properties = null;
		}
	}
	
	
	/**
	 * Opens the service model configuration file, replaces variables
	 * and then loads the file into the ServiceModelManager.
	 * @throws ConnectException 
	 */
	public void resetServiceModel(String projectName) throws IOException, ConnectException {
		if(!isConnected())
			throw new ConnectException("Must be connected");
		
		Map<String,String> variables = new HashMap<String,String>();
		variables.put("project_name", projectName);
		variables.put("host_name", connection.getHostName());
		
		FileReader fileReader = new FileReader(serviceModelConfigFile);
		
		// we will pipe the results of variable substitution directly to the
		// ServiceModelManager's xml parser.
		PipedWriter pipedWriter = new PipedWriter();
		final PipedReader pipedReader = new PipedReader();
		pipedWriter.connect(pipedReader);

		final Exception[] cause = new Exception[] {null};
		Thread serviceThread = new Thread("Service Model Load") {
			public void run() {
				try {
					ServiceModelManager.getInstance().loadModelConfiguration(pipedReader);
				} catch (Exception e) {
					cause[0] = e;
				}
			}
		};
		
		try {
			serviceThread.start(); // thread will block until something has been written to the pipe
			
			VariableSubstitution substitution = new VariableSubstitution(variables);
			substitution.substitute(fileReader, pipedWriter);
			
		} catch(Exception e) {
			throw new RuntimeException("Exception processing variables in file: " + serviceModelConfigFile, e);
		} finally {
			pipedWriter.close(); // important, the serviceThread won't die until the write end of the pipe is closed
			fileReader.close();
		}
		
		try {
			try {
				// wait for the xml parsing to finish
				serviceThread.join();
			} catch (InterruptedException e) { // I don't think this can happen
				throw new RuntimeException(e);
			} 
			
			if(cause[0] != null)
				throw new RuntimeException(cause[0]);
		} finally {
			pipedReader.close();
		}	
	}
	
	
	
	
	public IRDTTestConnection getConnection() {
		return connection;
	}
	
	
	public String getWorkspace() {
		return properties.getProperty(PROPERTY_WORKSPACE);
	}
	
	/**
	 * Returns a URI that represents the given path relative to the remote workspace.
	 * @throws URISyntaxException 
	 */
	public URI getWorkspaceURI(String path) throws URISyntaxException  {
		return connection.getURI(getWorkspace() + "/" + path);
	}
	
	
	
}
